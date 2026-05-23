import type {
  AutomationExecution,
  ClientSummary,
  IamUserAccessSummary,
  NotificationDispatchResponse,
  PaymentMethodAdmin,
  PaymentMethodRequest,
  PaymentSessionRequest,
  PaymentSessionResponse,
  RealmSummary,
  RoleCatalogSummary,
  SearchQueryResponse,
  SearchSuggestionResponse,
  UserSummary
} from "@/lib/types";
import { platformAuthHeaders } from "@/lib/platform-auth";

type ServiceKey =
  | "sso-user-service"
  | "bot-adapter-service"
  | "storefront-service"
  | "notification-service"
  | "search-index-service"
  | "automation-orchestrator-service"
  | "payment-service"
  | "payment-orchestrator-service";

type ScopedRequest = {
  tenantKey?: string;
  siteKey?: string;
};

async function requestJson<T>(serviceKey: ServiceKey, path: string, init?: RequestInit & ScopedRequest): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set("Content-Type", "application/json");
  for (const [key, value] of Object.entries(platformAuthHeaders())) {
    headers.set(key, value);
  }
  if (init?.tenantKey) {
    headers.set("X-Tenant-Key", init.tenantKey);
  }
  if (init?.siteKey) {
    headers.set("X-Site-Key", init.siteKey);
  }

  const response = await fetch(`/api/platform/service/${serviceKey}${path}`, {
    ...init,
    headers,
    cache: "no-store"
  });

  if (!response.ok) {
    const body = await response.text().catch(() => "");
    throw new Error(body || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function sendNotification(request: Record<string, unknown>) {
  return requestJson<NotificationDispatchResponse>("notification-service", "/endpoint/notifications/send", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function getNotificationMessage(messageKey: string) {
  return requestJson<Record<string, unknown>>("notification-service", `/endpoint/notifications/messages/${encodeURIComponent(messageKey)}`, {
    method: "GET"
  });
}

export function searchIndex(params: {
  q?: string;
  entityTypes?: string;
  filterKey?: string;
  filterValue?: string;
  sort?: string;
  page?: number;
  size?: number;
  tenantKey?: string;
  siteKey?: string;
}) {
  const search = new URLSearchParams();
  if (params.q) search.set("q", params.q);
  if (params.entityTypes) search.set("entityTypes", params.entityTypes);
  if (params.filterKey) search.set("filterKey", params.filterKey);
  if (params.filterValue) search.set("filterValue", params.filterValue);
  if (params.sort) search.set("sort", params.sort);
  search.set("page", String(params.page ?? 0));
  search.set("size", String(params.size ?? 20));
  return requestJson<SearchQueryResponse>("search-index-service", `/endpoint/search-index/search?${search.toString()}`, {
    method: "GET",
    tenantKey: params.tenantKey,
    siteKey: params.siteKey
  });
}

export function suggestIndex(params: { q: string; limit?: number; tenantKey?: string; siteKey?: string }) {
  const search = new URLSearchParams({ q: params.q, limit: String(params.limit ?? 8) });
  return requestJson<SearchSuggestionResponse>("search-index-service", `/endpoint/search-index/suggest?${search.toString()}`, {
    method: "GET",
    tenantKey: params.tenantKey,
    siteKey: params.siteKey
  });
}

export function syncSearchIndex(sourceServiceKey: string, sourceEntityKey: string) {
  return requestJson<Record<string, unknown>>("search-index-service", `/endpoint/search-index/sync/${encodeURIComponent(sourceServiceKey)}/${encodeURIComponent(sourceEntityKey)}`, {
    method: "POST",
    body: JSON.stringify({})
  });
}

export function startAutomationExecution(request: Record<string, unknown>) {
  return requestJson<AutomationExecution>("automation-orchestrator-service", "/endpoint/automation-orchestrator/executions/start", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function getAutomationExecution(executionId: string) {
  return requestJson<AutomationExecution>("automation-orchestrator-service", `/endpoint/automation-orchestrator/executions/${encodeURIComponent(executionId)}`, {
    method: "GET"
  });
}

export function cancelAutomationExecution(executionId: string) {
  return requestJson<AutomationExecution>("automation-orchestrator-service", `/endpoint/automation-orchestrator/executions/${encodeURIComponent(executionId)}/cancel`, {
    method: "POST",
    body: JSON.stringify({})
  });
}

export function listPaymentMethods() {
  return requestJson<PaymentMethodAdmin[]>("payment-service", "/endpoint/payment/admin/methods", {
    method: "GET"
  });
}

export function createPaymentMethod(request: PaymentMethodRequest) {
  return requestJson<PaymentMethodAdmin>("payment-service", "/endpoint/payment/admin/methods", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function updatePaymentMethod(methodKey: string, request: PaymentMethodRequest) {
  return requestJson<PaymentMethodAdmin>("payment-service", `/endpoint/payment/admin/methods/${encodeURIComponent(methodKey)}`, {
    method: "PUT",
    body: JSON.stringify(request)
  });
}

export function initiatePaymentSession(request: PaymentSessionRequest) {
  return requestJson<PaymentSessionResponse>("payment-orchestrator-service", "/endpoint/payment-orchestrator/sessions/initiate", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function listIamRealms() {
  return requestJson<RealmSummary[]>("sso-user-service", "/api/sso/iam/realms", { method: "GET" });
}

export function upsertIamRealm(request: { realmKey: string; displayName: string; description?: string; active: boolean }) {
  return requestJson<RealmSummary>("sso-user-service", "/api/sso/iam/realms", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function listIamClients(realmKey?: string) {
  const suffix = realmKey ? `?realmKey=${encodeURIComponent(realmKey)}` : "";
  return requestJson<ClientSummary[]>("sso-user-service", `/api/sso/iam/clients${suffix}`, { method: "GET" });
}

export function upsertIamClient(request: {
  clientId: string;
  realmKey: string;
  displayName: string;
  description?: string;
  active: boolean;
  publicClient: boolean;
  redirectUris: string[];
}) {
  return requestJson<ClientSummary>("sso-user-service", "/api/sso/iam/clients", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function listIamRealmRoles(realmKey: string) {
  return requestJson<RoleCatalogSummary[]>("sso-user-service", `/api/sso/iam/realm-roles?realmKey=${encodeURIComponent(realmKey)}`, { method: "GET" });
}

export function upsertIamRealmRole(request: {
  scopeType: "REALM";
  scopeKey: string;
  roleKey: string;
  displayName: string;
  description?: string;
  active: boolean;
  permissions: string[];
}) {
  return requestJson<RoleCatalogSummary>("sso-user-service", "/api/sso/iam/realm-roles", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function listIamClientRoles(clientId: string) {
  return requestJson<RoleCatalogSummary[]>("sso-user-service", `/api/sso/iam/client-roles?clientId=${encodeURIComponent(clientId)}`, { method: "GET" });
}

export function upsertIamClientRole(request: {
  scopeType: "CLIENT";
  scopeKey: string;
  roleKey: string;
  displayName: string;
  description?: string;
  active: boolean;
  permissions: string[];
}) {
  return requestJson<RoleCatalogSummary>("sso-user-service", "/api/sso/iam/client-roles", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function listIamMemberships(params?: { username?: string; realmKey?: string }) {
  const search = new URLSearchParams();
  if (params?.username) search.set("username", params.username);
  if (params?.realmKey) search.set("realmKey", params.realmKey);
  const suffix = search.size ? `?${search.toString()}` : "";
  return requestJson<Array<{ username: string; realmKey: string; active: boolean; defaultRealm: boolean }>>("sso-user-service", `/api/sso/iam/memberships${suffix}`, { method: "GET" });
}

export function upsertIamMembership(request: { username: string; realmKey: string; active: boolean; defaultRealm: boolean }) {
  return requestJson<{ username: string; realmKey: string; active: boolean; defaultRealm: boolean }>("sso-user-service", "/api/sso/iam/memberships", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function assignIamRealmRole(realmKey: string, request: { username: string; roleKey: string }) {
  return requestJson<IamUserAccessSummary>("sso-user-service", `/api/sso/iam/realms/${encodeURIComponent(realmKey)}/assign-role`, {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function assignIamClientRole(clientId: string, request: { username: string; roleKey: string }) {
  return requestJson<IamUserAccessSummary>("sso-user-service", `/api/sso/iam/clients/${encodeURIComponent(clientId)}/assign-role`, {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function resolveIamAccess(username: string, clientId?: string) {
  const suffix = clientId ? `?clientId=${encodeURIComponent(clientId)}` : "";
  return requestJson<IamUserAccessSummary>("sso-user-service", `/api/sso/iam/users/${encodeURIComponent(username)}/access${suffix}`, { method: "GET" });
}

export function listIamUsers() {
  return requestJson<UserSummary[]>("sso-user-service", "/api/sso/users", { method: "GET" });
}

export function createIamUser(request: {
  username: string;
  password: string;
  email?: string;
  phoneNumber?: string;
  mfaEnabled: boolean;
  roles: string[];
}) {
  return requestJson<UserSummary>("sso-user-service", "/api/sso/users", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function provisionManagedIamUser(request: {
  username: string;
  password: string;
  email?: string;
  phoneNumber?: string;
  mfaEnabled: boolean;
  realmKey: string;
  clientId?: string;
  realmRoles: string[];
  clientRoles: string[];
}) {
  return requestJson<IamUserAccessSummary>("sso-user-service", "/api/sso/iam/users", {
    method: "POST",
    body: JSON.stringify(request)
  });
}
