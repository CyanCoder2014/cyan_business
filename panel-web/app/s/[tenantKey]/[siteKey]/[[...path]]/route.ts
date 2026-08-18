const base = process.env.STOREFRONT_SERVICE_BASE_URL ?? "http://localhost:9115";

export async function GET(_request: Request, context: { params: { tenantKey: string; siteKey: string; path?: string[] } }) {
  const path = `/${(context.params.path ?? []).map(encodeURIComponent).join("/")}`;
  try {
    const response = await fetch(`${base}/public/storefront/page?path=${encodeURIComponent(path)}`, {
      headers: { "X-Tenant-Key": context.params.tenantKey, "X-Site-Key": context.params.siteKey, Accept: "text/html" },
      cache: "no-store", signal: AbortSignal.timeout(20000)
    });
    return new Response(response.body, { status: response.status, headers: { "Content-Type": response.headers.get("content-type") ?? "text/html; charset=utf-8", "Cache-Control": response.ok ? "public, max-age=60" : "no-store" } });
  } catch {
    return new Response("<!doctype html><title>Site unavailable</title><main><h1>Site unavailable</h1><p>The published site could not be loaded.</p></main>", { status: 502, headers: { "Content-Type": "text/html; charset=utf-8", "Cache-Control": "no-store" } });
  }
}
