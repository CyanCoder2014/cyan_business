"use client";

import { createContext, Fragment, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { AuthenticationRequiredError, getPlatformAuthToken, getPlatformSessionId, platformFetch, setActivePanelScope } from "@/lib/platform-auth";
import { platformErrorFromResponse } from "@/lib/api-error";
import type { PanelBootstrap } from "@/lib/panel-contracts";

type ScopeAccessContextValue = {
  bootstrap: PanelBootstrap | null;
  loading: boolean;
  error: string | null;
  tenantKey: string | null;
  siteKey: string | null;
  queryVersion: number;
  refresh: () => Promise<void>;
  selectScope: (tenantKey: string, siteKey?: string | null) => Promise<void>;
  can: (permission: string) => boolean;
  hasCapability: (capability: string) => boolean;
};

const ScopeAccessContext = createContext<ScopeAccessContextValue | null>(null);

function grantsPermission(grants: Set<string>, permission: string) {
  if (grants.has("*") || grants.has(permission)) return true;
  const separator = permission.includes(":") ? ":" : ".";
  const namespace = permission.split(separator)[0];
  return grants.has(`${namespace}${separator}*`);
}

export function ScopeAccessProvider({ children }: { children: ReactNode }) {
  const [bootstrap, setBootstrap] = useState<PanelBootstrap | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [queryVersion, setQueryVersion] = useState(0);

  const refresh = useCallback(async () => {
    if (!getPlatformAuthToken()) { setLoading(false); return; }
    setLoading(true); setError(null);
    try {
      const response = await platformFetch("/api/panel/bootstrap", { headers: { "X-Session-Id": getPlatformSessionId() }, cache: "no-store" });
      if (!response.ok) throw await platformErrorFromResponse(response);
      const value = await response.json() as PanelBootstrap;
      setBootstrap(value);
      setActivePanelScope(value.activeTenantKey, value.activeSiteKey);
    } catch (reason) {
      setBootstrap(null);
      setActivePanelScope(null, null);
      if (!(reason instanceof AuthenticationRequiredError)) setError(reason instanceof Error ? reason.message : "The panel context could not be loaded.");
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { refresh().catch(() => null); }, [refresh]);

  const selectScope = useCallback(async (tenantKey: string, siteKey?: string | null) => {
    const response = await platformFetch("/api/panel/scope", {
      method: "PUT",
      headers: { "Content-Type": "application/json", "X-Session-Id": getPlatformSessionId() },
      body: JSON.stringify({ tenantKey, siteKey: siteKey || null })
    });
    if (!response.ok) throw await platformErrorFromResponse(response);
    setActivePanelScope(tenantKey, siteKey || null);
    setQueryVersion((current) => current + 1);
    await refresh();
  }, [refresh]);

  const value = useMemo<ScopeAccessContextValue>(() => {
    const permissions = new Set([...(bootstrap?.access.realmPermissions ?? []), ...(bootstrap?.access.clients.flatMap((client) => client.clientPermissions) ?? []), ...(bootstrap?.tenantAccess?.permissions ?? [])]);
    return {
      bootstrap, loading, error,
      tenantKey: bootstrap?.activeTenantKey ?? null,
      siteKey: bootstrap?.activeSiteKey ?? null,
      queryVersion, refresh, selectScope,
      can: (permission) => grantsPermission(permissions, permission),
      hasCapability: (capability) => bootstrap?.capabilities.some((item) => item.key === capability && item.enabled) ?? false
    };
  }, [bootstrap, error, loading, queryVersion, refresh, selectScope]);

  return <ScopeAccessContext.Provider value={value}><Fragment key={queryVersion}>{children}</Fragment></ScopeAccessContext.Provider>;
}

export function useScopeAccess() {
  const value = useContext(ScopeAccessContext);
  if (!value) throw new Error("useScopeAccess must be used inside ScopeAccessProvider");
  return value;
}
