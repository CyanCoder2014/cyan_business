import { NextResponse } from "next/server";

const serviceBaseUrls: Record<string, string> = {
  "sso-user-service": process.env.SSO_USER_SERVICE_BASE_URL ?? "http://localhost:9002",
  "bot-adapter-service": process.env.BOT_ADAPTER_SERVICE_BASE_URL ?? "http://localhost:9126",
  "ai-orchestrator-service": process.env.AI_ORCHESTRATOR_SERVICE_BASE_URL ?? "http://localhost:9121",
  "bpm-service": process.env.BPM_SERVICE_BASE_URL ?? "http://localhost:9119",
  "storefront-service": process.env.STOREFRONT_SERVICE_BASE_URL ?? "http://localhost:9115",
  "notification-service": process.env.NOTIFICATION_SERVICE_BASE_URL ?? "http://localhost:9122",
  "search-index-service": process.env.SEARCH_INDEX_SERVICE_BASE_URL ?? "http://localhost:9125",
  "automation-orchestrator-service": process.env.AUTOMATION_ORCHESTRATOR_SERVICE_BASE_URL ?? "http://localhost:9120",
  "batch-worker-service": process.env.BATCH_WORKER_SERVICE_BASE_URL ?? "http://localhost:9127",
  "payment-service": process.env.PAYMENT_SERVICE_BASE_URL ?? "http://localhost:9114",
  "payment-orchestrator-service": process.env.PAYMENT_ORCHESTRATOR_SERVICE_BASE_URL ?? "http://localhost:9123"
};

type RouteContext = {
  params: {
    serviceKey: string;
    path: string[];
  };
};

async function proxy(request: Request, context: RouteContext) {
  const baseUrl = serviceBaseUrls[context.params.serviceKey];
  if (!baseUrl) {
    return NextResponse.json({ message: "Unsupported service" }, { status: 404 });
  }

  const incomingUrl = new URL(request.url);
  const targetPath = context.params.path.map(encodeURIComponent).join("/");
  const targetUrl = `${baseUrl}/${targetPath}${incomingUrl.search}`;
  const body = request.method === "GET" || request.method === "HEAD" ? undefined : await request.text();
  const response = await fetch(targetUrl, {
    method: request.method,
    headers: {
      "Content-Type": request.headers.get("Content-Type") ?? "application/json",
      ...(request.headers.get("Authorization") ? { Authorization: request.headers.get("Authorization") as string } : {}),
      ...(request.headers.get("X-Tenant-Key") ? { "X-Tenant-Key": request.headers.get("X-Tenant-Key") as string } : {}),
      ...(request.headers.get("X-Site-Key") ? { "X-Site-Key": request.headers.get("X-Site-Key") as string } : {})
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
