import { NextResponse } from "next/server";
import { createBotSession, listBotSessions } from "@/lib/bot-session-registry";
import type { BotConversationSession } from "@/lib/types";

export async function GET() {
  const sessions = await listBotSessions();
  return NextResponse.json(sessions);
}

export async function POST(request: Request) {
  const body = (await request.json()) as Omit<BotConversationSession, "id" | "createdAt" | "updatedAt" | "messages">;
  const session = await createBotSession(body);
  return NextResponse.json(session, { status: 201 });
}
