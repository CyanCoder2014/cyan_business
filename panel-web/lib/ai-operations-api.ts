import { platformFetch } from "@/lib/platform-auth";
import { platformErrorFromResponse } from "@/lib/api-error";

export type AiScope = { tenantKey: string; siteKey?: string };
export type AiProviderProfile = { profileKey: string; displayName: string; baseUrl: string; operationPath: string; model: string; secretRef: string; modalities: string[]; enabled: boolean; configurationStatus: string; revision?: number; updatedAt?: string };
export type AiArtifactJob = { jobId: string; status: string; operation: string; providerProfileKey: string; assetKey?: string; deliveryUrl?: string; usage?: Record<string, unknown>; errorCode?: string; errorMessage?: string; createdAt: string; startedAt?: string; completedAt?: string };

const base = "/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator";
async function call<T>(path: string, scope: AiScope, init: RequestInit = {}) {
  const response = await platformFetch(`${base}${path}`, { ...init, cache: "no-store", headers: { "Content-Type": "application/json", "X-Tenant-Key": scope.tenantKey, ...(scope.siteKey ? { "X-Site-Key": scope.siteKey } : {}), ...(init.headers ?? {}) } });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<T>;
}
export const listAiProviderProfiles = (scope: AiScope) => call<AiProviderProfile[]>("/provider-profiles", scope);
export const saveAiProviderProfile = (scope: AiScope, profile: Omit<AiProviderProfile, "configurationStatus" | "revision" | "updatedAt">) => call<AiProviderProfile>(`/provider-profiles/${encodeURIComponent(profile.profileKey)}`, scope, { method: "PUT", body: JSON.stringify(profile) });
export const listAiArtifactJobs = (scope: AiScope) => call<AiArtifactJob[]>("/artifact-jobs", scope);
export const startAiArtifactJob = (scope: AiScope, request: Record<string, unknown>) => call<AiArtifactJob>("/artifact-jobs", scope, { method: "POST", body: JSON.stringify(request) });
export const cancelAiArtifactJob = (scope: AiScope, id: string) => call<AiArtifactJob>(`/artifact-jobs/${encodeURIComponent(id)}/cancel`, scope, { method: "POST", body: "{}" });
