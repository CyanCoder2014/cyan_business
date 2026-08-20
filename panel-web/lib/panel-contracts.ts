export type ServiceState = "AVAILABLE" | "UNAVAILABLE" | "NOT_CONFIGURED";

export type TenantSummary = { tenantKey: string; displayName: string; status: string; membershipRole: string };
export type SiteSummary = { tenantKey: string; siteKey: string; name: string; status: string };
export type Capability = { key: string; enabled: boolean; source: string; status: string; limits: Record<string, unknown>; reason?: string };
export type Subscription = { tenantKey: string; planKey: string | null; status: string; features: string[]; limits: Record<string, unknown>; providerState: string; usage: Record<string, number> };
export type PanelIdentity = { username: string; email?: string; phoneNumber?: string; mfaEnabled: boolean; roles: string[]; active: boolean };
export type PanelAccess = { realmRoles: string[]; realmPermissions: string[]; clients: Array<{ clientId: string; clientRoles: string[]; clientPermissions: string[] }> };
export type TenantAccess = { tenantKey: string; username: string; roleKey: string; permissions: string[]; active: boolean };

export type PanelBootstrap = {
  identity: PanelIdentity;
  access: PanelAccess;
  tenantAccess: TenantAccess | null;
  tenants: TenantSummary[];
  sites: SiteSummary[];
  activeTenantKey: string | null;
  activeSiteKey: string | null;
  subscription: Subscription | null;
  capabilities: Capability[];
  featureFlags: Record<string, unknown>;
  services: Record<string, ServiceState>;
  warnings: string[];
};
