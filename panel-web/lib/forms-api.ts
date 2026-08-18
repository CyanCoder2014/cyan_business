import { platformErrorFromResponse } from "@/lib/api-error";
import { platformFetch } from "@/lib/platform-auth";

export type FormVisibility = "PUBLIC" | "AUTHENTICATED";
export type PublishedFormSummary = {
  slug: string; tenantKey: string; siteKey: string; serviceKey: string; entityKey: string;
  title: string; description?: string | null; visibility: FormVisibility; status: "PUBLISHED" | "ARCHIVED";
  createdAt: string; updatedAt: string;
};
export type PublishedFormView = {
  slug: string; title: string; description?: string | null; visibility: FormVisibility;
  serviceKey: string; entityKey: string; definition: Record<string, unknown>;
};
type Scope = { tenantKey: string; siteKey?: string | null };

function scopeHeaders(scope: Scope) {
  return { "X-Tenant-Key": scope.tenantKey, ...(scope.siteKey ? { "X-Site-Key": scope.siteKey } : {}) };
}
async function authenticated<T>(path: string, scope: Scope, init: RequestInit = {}): Promise<T> {
  const response = await platformFetch(`/api/platform/service/storefront-service/endpoint/forms${path}`, {
    ...init, cache: "no-store", headers: { "Content-Type": "application/json", ...scopeHeaders(scope), ...(init.headers ?? {}) }
  });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.status === 204 ? undefined as T : response.json() as Promise<T>;
}

export const listPublishedForms = (scope: Scope) => authenticated<PublishedFormSummary[]>("", scope);
export const getMemberForm = (slug: string, scope: Scope) => authenticated<PublishedFormView>(`/${encodeURIComponent(slug)}`, scope);
export const publishForm = (scope: Scope, request: { slug: string; serviceKey: string; entityKey: string; title: string; description?: string; visibility: FormVisibility }) => authenticated<PublishedFormSummary>(`/${encodeURIComponent(request.slug)}`, scope, { method: "PUT", body: JSON.stringify(request) });
export const archiveForm = (slug: string, scope: Scope) => authenticated<void>(`/${encodeURIComponent(slug)}`, scope, { method: "DELETE" });
export const submitMemberForm = (slug: string, data: Record<string, unknown>, scope: Scope, idempotencyKey: string) => authenticated<{submissionKey:string;status:string}>(`/${encodeURIComponent(slug)}/submissions`, scope, { method: "POST", headers: { "Idempotency-Key": idempotencyKey }, body: JSON.stringify(data) });

export async function getPublicForm(slug: string): Promise<PublishedFormView> {
  const response = await fetch(`/api/public/forms/${encodeURIComponent(slug)}`, { cache: "no-store" });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json();
}
export async function submitPublicForm(slug: string, data: Record<string, unknown>, idempotencyKey: string) {
  const response = await fetch(`/api/public/forms/${encodeURIComponent(slug)}/submissions`, { method: "POST", headers: { "Content-Type": "application/json", "Idempotency-Key": idempotencyKey }, body: JSON.stringify(data) });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<{submissionKey:string;status:string}>;
}

export const publicFormPath = (slug: string) => `/f/${encodeURIComponent(slug)}`;
export const memberFormPath = (slug: string) => `/forms/${encodeURIComponent(slug)}`;
export function publicSitePath(tenantKey: string, siteKey: string, path = "/") {
  const suffix = path.split("/").filter(Boolean).map(encodeURIComponent).join("/");
  return `/s/${encodeURIComponent(tenantKey)}/${encodeURIComponent(siteKey)}${suffix ? `/${suffix}` : ""}`;
}
