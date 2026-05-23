import { NextResponse } from "next/server";

const serviceBaseUrls: Record<string, string> = {
  "content-service": process.env.CONTENT_SERVICE_BASE_URL ?? "http://localhost:9101",
  "catalog-service": process.env.CATALOG_SERVICE_BASE_URL ?? "http://localhost:9102",
  "crm-service": process.env.CRM_SERVICE_BASE_URL ?? "http://localhost:9103",
  "commerce-service": process.env.COMMERCE_SERVICE_BASE_URL ?? "http://localhost:9104",
  "finance-service": process.env.FINANCE_SERVICE_BASE_URL ?? "http://localhost:9105",
  "inventory-service": process.env.INVENTORY_SERVICE_BASE_URL ?? "http://localhost:9106",
  "report-service": process.env.REPORT_SERVICE_BASE_URL ?? "http://localhost:9107",
  "storefront-service": process.env.STOREFRONT_SERVICE_BASE_URL ?? "http://localhost:9115",
  "media-service": process.env.MEDIA_SERVICE_BASE_URL ?? "http://localhost:9116",
  "cart-service": process.env.CART_SERVICE_BASE_URL ?? "http://localhost:9117",
  "checkout-service": process.env.CHECKOUT_SERVICE_BASE_URL ?? "http://localhost:9118",
  "bpm-service": process.env.BPM_SERVICE_BASE_URL ?? "http://localhost:9119",
  "payment-service": process.env.PAYMENT_SERVICE_BASE_URL ?? "http://localhost:9114",
  "pricing-promotion-service": process.env.PRICING_PROMOTION_SERVICE_BASE_URL ?? "http://localhost:9124",
  "notification-service": process.env.NOTIFICATION_SERVICE_BASE_URL ?? "http://localhost:9122",
  "search-index-service": process.env.SEARCH_INDEX_SERVICE_BASE_URL ?? "http://localhost:9123"
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
    return NextResponse.json({ message: "Unsupported dynamic service" }, { status: 404 });
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
