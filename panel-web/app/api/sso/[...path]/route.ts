import { NextResponse } from "next/server";

const serviceBaseUrls: Record<string, string> = {
  auth: process.env.SSO_AUTH_SERVICE_BASE_URL ?? "http://localhost:9001",
  users: process.env.SSO_USER_SERVICE_BASE_URL ?? "http://localhost:9002",
  captcha: process.env.SSO_CAPTCHA_SERVICE_BASE_URL ?? "http://localhost:9003",
  otp: process.env.SSO_OTP_SERVICE_BASE_URL ?? "http://localhost:9004",
  sessions: process.env.SSO_SESSION_SERVICE_BASE_URL ?? "http://localhost:9005",
  fido: process.env.SSO_FIDO_SERVICE_BASE_URL ?? "http://localhost:9006"
};

type RouteContext = {
  params: {
    path: string[];
  };
};

async function proxy(request: Request, context: RouteContext) {
  const serviceSegment = context.params.path[0];
  const baseUrl = serviceBaseUrls[serviceSegment];
  if (!baseUrl) {
    return NextResponse.json({ message: "Unsupported SSO service" }, { status: 404 });
  }
  const incomingUrl = new URL(request.url);
  const targetPath = context.params.path.map(encodeURIComponent).join("/");
  const targetUrl = `${baseUrl}/api/sso/${targetPath}${incomingUrl.search}`;
  const body = request.method === "GET" || request.method === "HEAD" ? undefined : await request.text();

  const response = await fetch(targetUrl, {
    method: request.method,
    headers: {
      "Content-Type": request.headers.get("Content-Type") ?? "application/json",
      ...(request.headers.get("Authorization") ? { Authorization: request.headers.get("Authorization") as string } : {})
    },
    body,
    cache: "no-store"
  });

  const text = await response.text();
  return new NextResponse(text, {
    status: response.status,
    headers: {
      "Content-Type": response.headers.get("Content-Type") ?? "application/json"
    }
  });
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
