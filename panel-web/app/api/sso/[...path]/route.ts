import { NextResponse } from "next/server";

const gatewayBaseUrl = process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:18001";

type RouteContext = {
  params: {
    path: string[];
  };
};

async function proxy(request: Request, context: RouteContext) {
  const incomingUrl = new URL(request.url);
  const targetPath = context.params.path.map(encodeURIComponent).join("/");
  const targetUrl = `${gatewayBaseUrl}/api/sso/${targetPath}${incomingUrl.search}`;
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
