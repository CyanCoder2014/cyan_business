"use client";
import { useEffect, useState } from "react";
import { dynamicServices } from "@/lib/dynamic-api";
import { listPlatformHealthRuns } from "@/lib/report-api";
import type { DynamicServiceKey } from "@/lib/types";

/**
 * Filters dynamicServices down to the ones the most recent persisted platform
 * health run reported AVAILABLE. Falls back to the full static list while
 * loading, on error, or when no health run has ever been recorded yet — a
 * missing/failed health signal should never hide services that might in
 * fact be reachable.
 */
export function useAvailableDynamicServices(scope: { tenantKey?: string; siteKey?: string }): DynamicServiceKey[] {
  const [available, setAvailable] = useState<DynamicServiceKey[] | null>(null);
  useEffect(() => {
    if (!scope.tenantKey) { setAvailable(null); return; }
    let live = true;
    listPlatformHealthRuns({ tenantKey: scope.tenantKey, siteKey: scope.siteKey })
      .then((page) => {
        if (!live) return;
        const latest = page.items[0];
        if (!latest) { setAvailable(null); return; }
        const healthy = new Set(latest.checks.filter((check) => check.status === "AVAILABLE").map((check) => check.serviceKey));
        const filtered = dynamicServices.filter((key) => healthy.has(key));
        setAvailable(filtered.length ? filtered : null);
      })
      .catch(() => { if (live) setAvailable(null); });
    return () => { live = false; };
  }, [scope.tenantKey, scope.siteKey]);
  return available ?? dynamicServices;
}
