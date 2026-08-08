import type { AiConversationSession, BotConversationSession, BotMessage } from "@/lib/types";
import { withServiceInventory } from "@/lib/platform-service-inventory";

const platformBaseUrl = process.env.AI_ORCHESTRATOR_SERVICE_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:9121";

function mapConversationSession(session: AiConversationSession): BotConversationSession {
  return {
    id: session.sessionId,
    channel: normalizeChannel(session.channelType),
    title: session.latestPrompt ?? session.latestQuestion ?? session.draftId ?? session.sessionId,
    tenantKey: session.tenantKey ?? "",
    siteKey: session.siteKey ?? "",
    draftId: session.draftId ?? null,
    status: normalizeStatus(session.status),
    appType: normalizeAppType(session.appTypeHint),
    lastPrompt: session.latestPrompt ?? "",
    answers: session.extractedAnswers ?? {},
    messages: (session.messages ?? []).map((message) => ({
      id: message.messageId,
      role: normalizeRole(message.role),
      content: message.content,
      createdAt: message.createdAt ?? session.updatedAt ?? session.createdAt ?? new Date().toISOString()
    })),
    createdAt: session.createdAt ?? new Date().toISOString(),
    updatedAt: session.updatedAt ?? session.createdAt ?? new Date().toISOString()
  };
}

async function requestPlatformJson<T>(pathName: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${platformBaseUrl}${pathName}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    },
    cache: "no-store"
  });

  if (!response.ok) {
    const body = await response.text().catch(() => "");
    throw new Error(body || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export async function listBotSessions(): Promise<BotConversationSession[]> {
  const sessions = await requestPlatformJson<AiConversationSession[]>("/endpoint/ai-orchestrator/sessions");
  return sessions.map(mapConversationSession);
}

export async function getBotSession(sessionId: string): Promise<BotConversationSession | null> {
  try {
    const session = await requestPlatformJson<AiConversationSession>(`/endpoint/ai-orchestrator/sessions/${encodeURIComponent(sessionId)}`);
    return mapConversationSession(session);
  } catch {
    return null;
  }
}

export async function upsertBotSession(_: BotConversationSession): Promise<BotConversationSession[]> {
  throw new Error("Bot session replacement is not supported; use ai-orchestrator session endpoints directly.");
}

export async function createBotSession(input: Omit<BotConversationSession, "id" | "createdAt" | "updatedAt" | "messages"> & { messages?: BotMessage[] }): Promise<BotConversationSession> {
  const created = await requestPlatformJson<AiConversationSession>("/endpoint/ai-orchestrator/sessions", {
    method: "POST",
    body: JSON.stringify(withServiceInventory({
      channelType: input.channel.toUpperCase(),
      tenantKey: input.tenantKey,
      siteKey: input.siteKey,
      clientKey: "panel",
      draftId: input.draftId,
      appTypeHint: input.appType,
      title: input.title,
      extractedAnswers: input.answers
    }))
  });
  return mapConversationSession(created);
}

export async function appendBotMessage(sessionId: string, message: Omit<BotMessage, "id" | "createdAt">): Promise<BotConversationSession | null> {
  try {
    const session = await requestPlatformJson<AiConversationSession>(`/endpoint/ai-orchestrator/sessions/${encodeURIComponent(sessionId)}/message`, {
      method: "POST",
      body: JSON.stringify(withServiceInventory({
        role: message.role.toUpperCase(),
        content: message.content,
        answersPatch: {}
      }))
    });
    return mapConversationSession(session);
  } catch {
    return null;
  }
}

export async function updateBotSession(sessionId: string, patch: Partial<BotConversationSession>): Promise<BotConversationSession | null> {
  if (patch.answers && Object.keys(patch.answers).length > 0) {
    try {
      const session = await requestPlatformJson<AiConversationSession>(`/endpoint/ai-orchestrator/sessions/${encodeURIComponent(sessionId)}/message`, {
        method: "POST",
        body: JSON.stringify(withServiceInventory({
          role: "SYSTEM",
          content: patch.lastPrompt ?? patch.title ?? "Session update",
          answersPatch: patch.answers
        }))
      });
      return mapConversationSession(session);
    } catch {
      return null;
    }
  }
  return getBotSession(sessionId);
}

function normalizeChannel(value?: string | null): "telegram" | "bale" {
  return value?.toLowerCase() === "bale" ? "bale" : "telegram";
}

function normalizeRole(value?: string | null): "user" | "assistant" | "system" {
  const normalized = value?.toLowerCase();
  if (normalized === "assistant" || normalized === "system") {
    return normalized;
  }
  return "user";
}

function normalizeStatus(value?: string | null): BotConversationSession["status"] {
  switch (value) {
    case "WAITING_FOR_ANSWERS":
    case "RESOLVED":
    case "FAILED":
      return value;
    default:
      return "OPEN";
  }
}

function normalizeAppType(value?: string | null): BotConversationSession["appType"] {
  switch (value) {
    case "WEBSITE":
    case "BLOG":
    case "SHOP":
    case "CRM":
    case "FORM_FLOW":
    case "BPM_PORTAL":
    case "MIXED_BUSINESS_APP":
      return value;
    default:
      return "MIXED_BUSINESS_APP";
  }
}
