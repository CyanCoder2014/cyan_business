import { NextResponse } from "next/server";
import { getBotSession, updateBotSession } from "@/lib/bot-session-registry";
import type { BotConversationSession } from "@/lib/types";

type RouteContext = {
  params: {
    sessionId: string;
  };
};

export async function GET(_: Request, context: RouteContext) {
  const session = await getBotSession(context.params.sessionId);
  if (!session) {
    return NextResponse.json({ message: "Session not found" }, { status: 404 });
  }
  return NextResponse.json(session);
}

export async function PATCH(request: Request, context: RouteContext) {
  const patch = (await request.json()) as Partial<BotConversationSession>;
  const session = await updateBotSession(context.params.sessionId, patch);
  if (!session) {
    return NextResponse.json({ message: "Session not found" }, { status: 404 });
  }
  return NextResponse.json(session);
}
