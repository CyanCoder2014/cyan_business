import { platformFetch } from "@/lib/platform-auth";

const platformBaseUrl = process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8001";

export type StorefrontResolvedRoute = {
  tenantKey?: string;
  siteKey?: string;
  path: string;
  route: Record<string, unknown>;
  target: Record<string, unknown>;
  theme: Record<string, unknown>;
};

export type StorefrontRenderedPage = StorefrontResolvedRoute & {
  html: string;
};

type ScopedRequest = {
  tenantKey?: string;
  siteKey?: string;
};

async function requestJson<T>(path: string, scope: ScopedRequest): Promise<T> {
  const response = await platformFetch(`${platformBaseUrl}${path}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      ...(scope.tenantKey ? { "X-Tenant-Key": scope.tenantKey } : {}),
      ...(scope.siteKey ? { "X-Site-Key": scope.siteKey } : {})
    },
    cache: "no-store"
  });

  if (!response.ok) {
    const body = await response.text().catch(() => "");
    throw new Error(body || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function storefrontPageUrl(path: string) {
  return `${platformBaseUrl}/public/storefront/page?path=${encodeURIComponent(path)}`;
}

export function resolveStorefrontRoute(path: string, scope: ScopedRequest): Promise<StorefrontResolvedRoute> {
  return requestJson<StorefrontResolvedRoute>(`/public/storefront/resolve?path=${encodeURIComponent(path)}`, scope);
}

export function renderStorefrontRoute(path: string, scope: ScopedRequest): Promise<StorefrontRenderedPage> {
  return requestJson<StorefrontRenderedPage>(`/public/storefront/render?path=${encodeURIComponent(path)}`, scope);
}
