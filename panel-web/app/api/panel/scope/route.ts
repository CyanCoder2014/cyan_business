import { NextResponse } from "next/server";

const sessionsUrl = process.env.SSO_SESSION_SERVICE_BASE_URL ?? "http://localhost:9005";

async function proxy(request: Request) {
  const authorization = request.headers.get("Authorization");
  const sessionId = request.headers.get("X-Session-Id");
  if (!authorization || !sessionId) return NextResponse.json({ code: "SESSION_REQUIRED", message: "An authenticated session is required" }, { status: 401 });
  const response = await fetch(`${sessionsUrl}/api/sso/sessions/${encodeURIComponent(sessionId)}/scope`, {
    method: request.method,
    headers: { Authorization: authorization, "Content-Type": "application/json" },
    body: request.method === "GET" ? undefined : await request.text(),
    cache: "no-store"
  });
  return new NextResponse(await response.text(), { status: response.status, headers: { "Content-Type": response.headers.get("Content-Type") ?? "application/json" } });
}
export const GET = proxy;
export const PUT = proxy;
