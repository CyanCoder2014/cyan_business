import { NextResponse } from "next/server";
import type { PanelBootstrap, ServiceState } from "@/lib/panel-contracts";

const urls = {
  users: process.env.SSO_USER_SERVICE_BASE_URL ?? "http://localhost:9002",
  sessions: process.env.SSO_SESSION_SERVICE_BASE_URL ?? "http://localhost:9005",
  tenants: process.env.TENANT_SERVICE_BASE_URL ?? "http://localhost:9129",
  storefront: process.env.STOREFRONT_SERVICE_BASE_URL ?? "http://localhost:9115",
  billing: process.env.BILLING_SERVICE_BASE_URL ?? "http://localhost:9130"
};

class UpstreamError extends Error { constructor(public status:number, public url:string){super(`${status}:${url}`)} }
async function getJson<T>(url: string, authorization: string, headers: Record<string, string> = {}): Promise<T> {
  const response = await fetch(url, { headers: { Authorization: authorization, ...headers }, cache: "no-store" });
  if (!response.ok) throw new UpstreamError(response.status,url);
  return response.json() as Promise<T>;
}

export async function GET(request: Request) {
  const authorization = request.headers.get("Authorization") ?? "";
  if (!authorization.startsWith("Bearer ")) return NextResponse.json({ code: "AUTH_REQUIRED", message: "Authentication is required" }, { status: 401 });

  const services: Record<string, ServiceState> = {};
  const warnings: string[] = [];
  try {
    const core = await Promise.allSettled([
      getJson<PanelBootstrap["identity"]>(`${urls.users}/api/sso/users/me`, authorization),
      getJson<PanelBootstrap["access"]>(`${urls.users}/api/sso/iam/me/access?clientId=cyan-panel`, authorization),
      getJson<PanelBootstrap["tenants"]>(`${urls.tenants}/endpoint/tenants`, authorization)
    ]);
    services.identity = core[0].status === "fulfilled" && core[1].status === "fulfilled" ? "AVAILABLE" : "UNAVAILABLE";
    services.tenancy = core[2].status === "fulfilled" ? "AVAILABLE" : "UNAVAILABLE";
    if (core[0].status !== "fulfilled" || core[1].status !== "fulfilled" || core[2].status !== "fulfilled") {
      if (services.identity === "UNAVAILABLE") warnings.push("Identity or access context is temporarily unavailable.");
      if (services.tenancy === "UNAVAILABLE") warnings.push("Tenant context is temporarily unavailable.");
      return NextResponse.json({
        code: "BOOTSTRAP_UNAVAILABLE",
        message: warnings.join(" ") || "The authenticated panel context could not be loaded.",
        services,
        warnings
      }, { status: 503 });
    }
    const identity = core[0].value;
    const access = core[1].value;
    const tenants = core[2].value;
    services.identity = "AVAILABLE";
    services.tenancy = "AVAILABLE";
    const sessionId = request.headers.get("X-Session-Id");
    let activeTenantKey: string | null = null;
    let activeSiteKey: string | null = null;
    if (sessionId) {
      try {
        const scope = await getJson<{ tenantKey: string | null; siteKey: string | null }>(`${urls.sessions}/api/sso/sessions/${encodeURIComponent(sessionId)}/scope`, authorization);
        activeTenantKey = scope.tenantKey;
        activeSiteKey = scope.siteKey;
        services.sessionScope = "AVAILABLE";
      } catch (reason) {
        services.sessionScope = reason instanceof UpstreamError && reason.status === 404 ? "NOT_CONFIGURED" : "UNAVAILABLE";
        warnings.push(reason instanceof UpstreamError && reason.status === 404
          ? "The previous session scope was not found. Select a workspace to save a new scope."
          : "Session scope is temporarily unavailable. Select a workspace before making scoped changes.");
      }
    } else {
      services.sessionScope = "NOT_CONFIGURED";
    }
    if (activeTenantKey && !tenants.some((tenant) => tenant.tenantKey === activeTenantKey)) {
      activeTenantKey = null; activeSiteKey = null;
      warnings.push("The persisted tenant is no longer accessible.");
    }

    let sites: PanelBootstrap["sites"] = [];
    let subscription: PanelBootstrap["subscription"] = null;
    let capabilities: PanelBootstrap["capabilities"] = [];
    let featureFlags: Record<string, unknown> = {};
    let tenantAccess: PanelBootstrap["tenantAccess"] = null;
    if (activeTenantKey) {
      const scoped = { "X-Tenant-Key": activeTenantKey };
      const results = await Promise.allSettled([
        getJson<PanelBootstrap["sites"]>(`${urls.storefront}/endpoint/sites`, authorization, scoped),
        getJson<NonNullable<PanelBootstrap["subscription"]>>(`${urls.billing}/endpoint/billing/tenants/${encodeURIComponent(activeTenantKey)}/subscription`, authorization, scoped),
        getJson<PanelBootstrap["capabilities"]>(`${urls.tenants}/endpoint/tenants/${encodeURIComponent(activeTenantKey)}/capabilities`, authorization),
        getJson<Record<string, unknown>>(`${urls.tenants}/endpoint/tenants/${encodeURIComponent(activeTenantKey)}/feature-flags`, authorization),
        getJson<NonNullable<PanelBootstrap["tenantAccess"]>>(`${urls.tenants}/endpoint/tenants/${encodeURIComponent(activeTenantKey)}/users/${encodeURIComponent(identity.username)}/effective-access`, authorization)
      ]);
      if (results[0].status === "fulfilled") { sites = results[0].value; services.sites = "AVAILABLE"; } else { services.sites = "UNAVAILABLE"; warnings.push("Sites are temporarily unavailable."); }
      if (results[1].status === "fulfilled") { subscription = results[1].value; services.billing = subscription.providerState === "NOT_CONFIGURED" ? "NOT_CONFIGURED" : "AVAILABLE"; } else { services.billing = "UNAVAILABLE"; warnings.push("Billing state is temporarily unavailable."); }
      if (results[2].status === "fulfilled") { capabilities = results[2].value; services.capabilities = "AVAILABLE"; } else { services.capabilities = "UNAVAILABLE"; warnings.push("Capabilities are temporarily unavailable."); }
      if (results[3].status === "fulfilled") featureFlags = results[3].value;
      if (results[4].status === "fulfilled") tenantAccess = results[4].value;
      if (activeSiteKey && !sites.some((site) => site.siteKey === activeSiteKey)) activeSiteKey = null;
    }
    return NextResponse.json({ identity, access, tenantAccess, tenants, sites, activeTenantKey, activeSiteKey, subscription, capabilities, featureFlags, services, warnings } satisfies PanelBootstrap);
  } catch {
    return NextResponse.json({ code: "BOOTSTRAP_UNAVAILABLE", message: "The authenticated panel context could not be loaded." }, { status: 503 });
  }
}
