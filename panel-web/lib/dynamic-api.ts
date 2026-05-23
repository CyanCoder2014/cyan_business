import type {
  DynamicEntityDefinition,
  DynamicEntityRecord,
  DynamicEntityTemplate,
  DynamicServiceKey
} from "@/lib/types";
import { platformAuthHeaders } from "@/lib/platform-auth";

export const dynamicServices: DynamicServiceKey[] = [
  "content-service",
  "catalog-service",
  "crm-service",
  "commerce-service",
  "finance-service",
  "inventory-service",
  "report-service",
  "storefront-service",
  "media-service",
  "cart-service",
  "checkout-service",
  "payment-service",
  "pricing-promotion-service",
  "notification-service",
  "search-index-service",
  "bpm-service"
];

type ScopedRequest = {
  tenantKey?: string;
  siteKey?: string;
};

async function requestJson<T>(serviceKey: DynamicServiceKey, path: string, init?: RequestInit & ScopedRequest): Promise<T> {
  const response = await fetch(`/api/platform/dynamic/${serviceKey}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...platformAuthHeaders(),
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

export function listTemplates(serviceKey: DynamicServiceKey): Promise<DynamicEntityTemplate[]> {
  return requestJson<DynamicEntityTemplate[]>(serviceKey, "/endpoint/entities/templates");
}

export function listDefinitions(
  serviceKey: DynamicServiceKey,
  scope: ScopedRequest
): Promise<DynamicEntityDefinition[]> {
  return requestJson<DynamicEntityDefinition[]>(serviceKey, "/endpoint/entities/definitions", scope);
}

export function createDefinitionFromTemplate(
  serviceKey: DynamicServiceKey,
  templateKey: string,
  entityKey: string,
  scope: ScopedRequest
): Promise<DynamicEntityDefinition> {
  return requestJson<DynamicEntityDefinition>(serviceKey, `/endpoint/entities/templates/${templateKey}/definitions`, {
    method: "POST",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify({
      entityKey,
      tenantKey: scope.tenantKey,
      siteKey: scope.siteKey
    })
  });
}

export function saveDefinition(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  definitionJson: string,
  scope: ScopedRequest
): Promise<DynamicEntityDefinition> {
  return requestJson<DynamicEntityDefinition>(serviceKey, `/endpoint/entities/definitions/${entityKey}`, {
    method: "PUT",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify({
      entityKey,
      tenantKey: scope.tenantKey,
      siteKey: scope.siteKey,
      definitionJson
    })
  });
}

export function listRecords(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  scope: ScopedRequest
): Promise<DynamicEntityRecord[]> {
  return requestJson<DynamicEntityRecord[]>(serviceKey, `/endpoint/entities/records/${entityKey}`, scope);
}

export function submitRecord(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  recordKey: string,
  data: Record<string, unknown>,
  scope: ScopedRequest
): Promise<DynamicEntityRecord> {
  return requestJson<DynamicEntityRecord>(serviceKey, `/endpoint/entities/records/${entityKey}`, {
    method: "POST",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify({
      recordKey,
      tenantKey: scope.tenantKey,
      siteKey: scope.siteKey,
      data
    })
  });
}
