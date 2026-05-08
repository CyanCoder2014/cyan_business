import type { BotConversationSession, BotMessage, BotMessageRole } from "@/lib/types";

async function requestJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
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

export function listBotSessions(): Promise<BotConversationSession[]> {
  return requestJson<BotConversationSession[]>("/api/bot-sessions");
}

export function getBotSession(sessionId: string): Promise<BotConversationSession> {
  return requestJson<BotConversationSession>(`/api/bot-sessions/${sessionId}`);
}

export function createBotSession(
  session: Omit<BotConversationSession, "id" | "createdAt" | "updatedAt" | "messages">
): Promise<BotConversationSession> {
  return requestJson<BotConversationSession>("/api/bot-sessions", {
    method: "POST",
    body: JSON.stringify(session)
  });
}

export function updateBotSession(
  sessionId: string,
  patch: Partial<BotConversationSession>
): Promise<BotConversationSession> {
  return requestJson<BotConversationSession>(`/api/bot-sessions/${sessionId}`, {
    method: "PATCH",
    body: JSON.stringify(patch)
  });
}

export function appendBotMessage(
  sessionId: string,
  message: {
    role: BotMessageRole;
    content: string;
  }
): Promise<BotConversationSession> {
  return requestJson<BotConversationSession>(`/api/bot-sessions/${sessionId}/messages`, {
    method: "POST",
    body: JSON.stringify(message)
  });
}

export function createMessage(role: BotMessageRole, content: string): BotMessage {
  return {
    id: `msg-${Date.now()}`,
    role,
    content,
    createdAt: new Date().toISOString()
  };
}
