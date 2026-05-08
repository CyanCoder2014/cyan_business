import { promises as fs } from "fs";
import path from "path";
import { seedDrafts } from "@/lib/draft-store";
import type { BotConversationSession, BotMessage } from "@/lib/types";

const registryPath = path.join(process.cwd(), "data", "bot-sessions.json");

async function ensureDir() {
  await fs.mkdir(path.dirname(registryPath), { recursive: true });
}

function nowIso() {
  return new Date().toISOString();
}

function seedSessions(): BotConversationSession[] {
  const now = "2025-05-08T00:00:00.000Z";
  const seedDraft = seedDrafts()[0];
  return [
    {
      id: "session-telegram-retail",
      channel: "telegram",
      title: "Retail onboarding thread",
      tenantKey: "tenant-demo",
      siteKey: "site-retail",
      draftId: seedDraft.id,
      status: "WAITING_FOR_ANSWERS",
      appType: "MIXED_BUSINESS_APP",
      lastPrompt: "Build a CRM and storefront app for a local retailer.",
      answers: {
        businessName: "Retail Demo",
        preferredDomain: "retail-demo.example.com"
      },
      messages: [
        {
          id: "msg-1",
          role: "assistant",
          content: "What type of app do you want to create?",
          createdAt: now
        },
        {
          id: "msg-2",
          role: "user",
          content: "Build a CRM and storefront app for a local retailer.",
          createdAt: now
        }
      ],
      createdAt: now,
      updatedAt: now
    }
  ];
}

async function readRegistryFile(): Promise<BotConversationSession[]> {
  try {
    const raw = await fs.readFile(registryPath, "utf8");
    const parsed = JSON.parse(raw) as BotConversationSession[];
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") {
      return [];
    }
    throw error;
  }
}

async function writeRegistryFile(sessions: BotConversationSession[]) {
  await ensureDir();
  await fs.writeFile(registryPath, `${JSON.stringify(sessions, null, 2)}\n`, "utf8");
}

export async function listBotSessions(): Promise<BotConversationSession[]> {
  const stored = await readRegistryFile();
  if (stored.length > 0) {
    return stored;
  }
  const seeded = seedSessions();
  await writeRegistryFile(seeded);
  return seeded;
}

export async function getBotSession(sessionId: string): Promise<BotConversationSession | null> {
  const sessions = await listBotSessions();
  return sessions.find((session) => session.id === sessionId) ?? null;
}

export async function upsertBotSession(session: BotConversationSession): Promise<BotConversationSession[]> {
  const sessions = await listBotSessions();
  const nextSessions = [session, ...sessions.filter((item) => item.id !== session.id)];
  await writeRegistryFile(nextSessions);
  return nextSessions;
}

export async function createBotSession(input: Omit<BotConversationSession, "id" | "createdAt" | "updatedAt" | "messages"> & { messages?: BotMessage[] }): Promise<BotConversationSession> {
  const session: BotConversationSession = {
    id: `session-${input.channel}-${input.siteKey}-${Date.now()}`,
    createdAt: nowIso(),
    updatedAt: nowIso(),
    messages: input.messages ?? [],
    ...input
  };
  const sessions = await listBotSessions();
  await writeRegistryFile([session, ...sessions]);
  return session;
}

export async function appendBotMessage(sessionId: string, message: Omit<BotMessage, "id" | "createdAt">): Promise<BotConversationSession | null> {
  const sessions = await listBotSessions();
  const index = sessions.findIndex((session) => session.id === sessionId);
  if (index === -1) {
    return null;
  }
  const nextSession: BotConversationSession = {
    ...sessions[index],
    messages: [
      ...sessions[index].messages,
      {
        id: `msg-${Date.now()}`,
        createdAt: nowIso(),
        ...message
      }
    ],
    updatedAt: nowIso()
  };
  sessions[index] = nextSession;
  await writeRegistryFile(sessions);
  return nextSession;
}

export async function updateBotSession(sessionId: string, patch: Partial<BotConversationSession>): Promise<BotConversationSession | null> {
  const sessions = await listBotSessions();
  const index = sessions.findIndex((session) => session.id === sessionId);
  if (index === -1) {
    return null;
  }
  const nextSession: BotConversationSession = {
    ...sessions[index],
    ...patch,
    updatedAt: nowIso()
  };
  sessions[index] = nextSession;
  await writeRegistryFile(sessions);
  return nextSession;
}
