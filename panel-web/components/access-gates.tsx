"use client";

import type { ReactNode } from "react";
import { useScopeAccess } from "@/components/scope-access-provider";

export type AccessState = "allowed" | "auth-required" | "permission-denied" | "plan-locked" | "capability-disabled" | "service-unavailable";

export function resolveAccessState(input: { authenticated: boolean; permission?: boolean; plan?: boolean; capability?: boolean; service?: boolean }): AccessState {
  if (!input.authenticated) return "auth-required";
  if (input.permission === false) return "permission-denied";
  if (input.plan === false) return "plan-locked";
  if (input.capability === false) return "capability-disabled";
  if (input.service === false) return "service-unavailable";
  return "allowed";
}

export function AccessGate({ permission, capability, children, fallback }: { permission?: string; capability?: string; children: ReactNode; fallback?: (state: AccessState) => ReactNode }) {
  const { bootstrap, can, hasCapability } = useScopeAccess();
  const capabilityRecord = capability ? bootstrap?.capabilities.find((item) => item.key === capability) : undefined;
  const state = resolveAccessState({
    authenticated: Boolean(bootstrap?.identity),
    permission: permission ? can(permission) : true,
    plan: capability && capabilityRecord?.source === "PLAN" ? capabilityRecord.enabled : true,
    capability: capability ? Boolean(capabilityRecord) && (capabilityRecord?.source === "PLAN" || Boolean(capabilityRecord?.enabled)) : true,
    service: capability && capabilityRecord ? capabilityRecord.status === "AVAILABLE" : true
  });
  if (state === "allowed") return <>{children}</>;
  return <>{fallback?.(state) ?? <div className="permission-state" role="status">{state.replaceAll("-", " ")}</div>}</>;
}

export const RouteGuard = AccessGate;
export const ActionGuard = AccessGate;
