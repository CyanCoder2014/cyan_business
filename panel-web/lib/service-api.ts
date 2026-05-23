import type {
  AutomationExecution,
  NotificationDispatchResponse,
  PaymentMethodAdmin,
  PaymentMethodRequest,
  PaymentSessionRequest,
  PaymentSessionResponse,
  SearchQueryResponse,
  SearchSuggestionResponse
} from "@/lib/types";

type ServiceKey =
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
  const response = await fetch(`/api/platform/service/${serviceKey}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.tenantKey ? { "X-Tenant-Key": init.tenantKey } : {}),
      ...(init?.siteKey ? { "X-Site-Key": init.siteKey } : {}),
      ...(init?.headers ?? {})
    },
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
