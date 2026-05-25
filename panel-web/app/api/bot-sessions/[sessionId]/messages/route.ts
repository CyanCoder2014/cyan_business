import { NextResponse } from "next/server";
import { appendBotMessage } from "@/lib/bot-session-registry";
import type { BotMessageRole } from "@/lib/types";

type RouteContext = {
  params: {
    sessionId: string;
  };
};

export async function POST(request: Request, context: RouteContext) {
  const body = (await request.json()) as {
    role: BotMessageRole;
    content: string;
  };
  const session = await appendBotMessage(context.params.sessionId, body);
  if (!session) {
    return NextResponse.json({ message: "Session not found" }, { status: 404 });
  }
  return NextResponse.json(session, { status: 201 });
}
