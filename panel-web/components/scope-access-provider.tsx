"use client";

import { createContext, Fragment, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { AuthenticationRequiredError, getPlatformAuthToken, getPlatformSessionId, getPlatformUsername, platformFetch, setActivePanelScope } from "@/lib/platform-auth";
import { platformErrorFromResponse } from "@/lib/api-error";
import type { PanelBootstrap } from "@/lib/panel-contracts";

type ScopeAccessContextValue = {
  bootstrap: PanelBootstrap | null;
  loading: boolean;
  selectionPending: boolean;
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
const PREFERRED_SCOPE_STORAGE_PREFIX = "cyan.panel.preferredScope.";

function preferredScopeKey(username: string) {
  return `${PREFERRED_SCOPE_STORAGE_PREFIX}${username.trim().toLowerCase()}`;
}

function readPreferredScope(username: string) {
  if (typeof window === "undefined" || !username) return null;
  try {
    const value = JSON.parse(window.localStorage.getItem(preferredScopeKey(username)) ?? "null") as { tenantKey?: unknown; siteKey?: unknown } | null;
    return value && typeof value.tenantKey === "string"
      ? { tenantKey: value.tenantKey, siteKey: typeof value.siteKey === "string" ? value.siteKey : null }
      : null;
  } catch {
    return null;
  }
}

function writePreferredScope(username: string, tenantKey: string, siteKey?: string | null) {
  if (typeof window === "undefined" || !username) return;
  window.localStorage.setItem(preferredScopeKey(username), JSON.stringify({ tenantKey, siteKey: siteKey || null }));
}

const LEGACY_PERMISSION_NAMESPACES: Record<string, ReadonlySet<string>> = {
  "builder:*": new Set(["project", "definition", "record", "bpm", "automation", "site", "bot", "ai"]),
  "operations:*": new Set(["record", "bpm", "automation", "report", "media", "search", "notification"]),
  "commerce:*": new Set(["commerce", "catalog", "cart", "checkout", "payment", "pricing", "inventory"]),
};

export function grantsPermission(grants: Set<string>, permission: string) {
  if (grants.has("*") || grants.has(permission)) return true;
  const separator = permission.includes(":") ? ":" : ".";
  const namespace = permission.split(separator)[0];
  if (grants.has(`${namespace}${separator}*`)) return true;
  return Object.entries(LEGACY_PERMISSION_NAMESPACES).some(([legacyGrant, namespaces]) => grants.has(legacyGrant) && namespaces.has(namespace));
}

export function ScopeAccessProvider({ children }: { children: ReactNode }) {
  const [bootstrap, setBootstrap] = useState<PanelBootstrap | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectionPending, setSelectionPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [queryVersion, setQueryVersion] = useState(0);
  const automaticScopeRef = useRef<string | null>(null);

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
    if (!tenantKey || selectionPending) return;
    setSelectionPending(true);
    try {
      const response = await platformFetch("/api/panel/scope", {
        method: "PUT",
        headers: { "Content-Type": "application/json", "X-Session-Id": getPlatformSessionId() },
        body: JSON.stringify({ tenantKey, siteKey: siteKey || null })
      });
      if (!response.ok) throw await platformErrorFromResponse(response);
      setActivePanelScope(tenantKey, siteKey || null);
      writePreferredScope(getPlatformUsername(), tenantKey, siteKey);
      setQueryVersion((current) => current + 1);
      await refresh();
    } finally {
      setSelectionPending(false);
    }
  }, [refresh, selectionPending]);

  useEffect(() => {
    if (loading || !bootstrap) return;
    let tenantKey = bootstrap.activeTenantKey;
    let siteKey = bootstrap.activeSiteKey;
    if (!tenantKey) {
      const preferred = readPreferredScope(bootstrap.identity.username || getPlatformUsername());
      if (!preferred || !bootstrap.tenants.some((tenant) => tenant.tenantKey === preferred.tenantKey)) return;
      tenantKey = preferred.tenantKey;
      siteKey = null;
    } else if (!siteKey && bootstrap.sites.length === 1) {
      siteKey = bootstrap.sites[0].siteKey;
    } else {
      writePreferredScope(bootstrap.identity.username || getPlatformUsername(), tenantKey, siteKey);
      return;
    }
    const target = `${tenantKey}:${siteKey ?? ""}`;
    if (automaticScopeRef.current === target) return;
    automaticScopeRef.current = target;
    selectScope(tenantKey, siteKey).catch((reason) => {
      automaticScopeRef.current = null;
      setError(reason instanceof Error ? reason.message : "The workspace could not be selected.");
    });
  }, [bootstrap, loading, selectScope]);

  const value = useMemo<ScopeAccessContextValue>(() => {
    const permissions = new Set([...(bootstrap?.access.realmPermissions ?? []), ...(bootstrap?.access.clients.flatMap((client) => client.clientPermissions) ?? []), ...(bootstrap?.tenantAccess?.permissions ?? [])]);
    return {
      bootstrap, loading, selectionPending, error,
      tenantKey: bootstrap?.activeTenantKey ?? null,
      siteKey: bootstrap?.activeSiteKey ?? null,
      queryVersion, refresh, selectScope,
      can: (permission) => grantsPermission(permissions, permission),
      hasCapability: (capability) => bootstrap?.capabilities.some((item) => item.key === capability && item.enabled) ?? false
    };
  }, [bootstrap, error, loading, queryVersion, refresh, selectScope, selectionPending]);

  return <ScopeAccessContext.Provider value={value}><Fragment key={queryVersion}>{children}</Fragment></ScopeAccessContext.Provider>;
}

export function useScopeAccess() {
  const value = useContext(ScopeAccessContext);
  if (!value) throw new Error("useScopeAccess must be used inside ScopeAccessProvider");
  return value;
}
