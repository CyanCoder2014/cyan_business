import type {
  DynamicEntityDefinition,
  DynamicEntityRecord,
  DynamicEntityTemplate,
  DynamicServiceKey
} from "@/lib/types";
import { platformFetch } from "@/lib/platform-auth";
import { platformErrorFromResponse } from "@/lib/api-error";

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

export type DynamicPage<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

async function requestJson<T>(serviceKey: DynamicServiceKey, path: string, init?: RequestInit & ScopedRequest): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set("Content-Type", "application/json");
  if (init?.tenantKey) {
    headers.set("X-Tenant-Key", init.tenantKey);
  }
  if (init?.siteKey) {
    headers.set("X-Site-Key", init.siteKey);
  }

  const response = await platformFetch(`/api/platform/dynamic/${serviceKey}${path}`, {
    ...init,
    headers,
    cache: "no-store"
  });

  if (!response.ok) {
    throw await platformErrorFromResponse(response);
  }

  return response.json() as Promise<T>;
}

export function listTemplates(serviceKey: DynamicServiceKey): Promise<DynamicEntityTemplate[]> {
  return requestJson<DynamicEntityTemplate[]>(serviceKey, "/endpoint/entities/templates");
}

export async function listDefinitions(
  serviceKey: DynamicServiceKey,
  scope: ScopedRequest
): Promise<DynamicEntityDefinition[]> {
  const definitions: DynamicEntityDefinition[] = [];
  let page = 0;

  while (true) {
    const response = await requestJson<DynamicPage<DynamicEntityDefinition> | DynamicEntityDefinition[]>(
      serviceKey,
      `/endpoint/entities/definitions?page=${page}&size=200&sort=entityKey,asc`,
      scope
    );
    if (Array.isArray(response)) {
      return response;
    }
    definitions.push(...response.content);
    page += 1;
    if (page >= response.totalPages) {
      return definitions;
    }
  }
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

export function getDefinition(serviceKey:DynamicServiceKey,entityKey:string,scope:ScopedRequest):Promise<DynamicEntityDefinition>{return requestJson(serviceKey,`/endpoint/entities/definitions/${encodeURIComponent(entityKey)}`,scope);}
export function createDefinition(serviceKey:DynamicServiceKey,entityKey:string,definition:Record<string,unknown>,scope:ScopedRequest):Promise<DynamicEntityDefinition>{return requestJson(serviceKey,"/endpoint/entities/definitions",{method:"POST",tenantKey:scope.tenantKey,siteKey:scope.siteKey,body:JSON.stringify({entityKey,definition})});}
export function listDefinitionVersions(serviceKey:DynamicServiceKey,entityKey:string,scope:ScopedRequest):Promise<Array<{revision:number;status:string;definition:string;createdAt:string}>>{return requestJson(serviceKey,`/endpoint/entities/definitions/${encodeURIComponent(entityKey)}/versions`,scope);}
export function publishDefinition(serviceKey:DynamicServiceKey,entityKey:string,scope:ScopedRequest):Promise<DynamicEntityDefinition>{return requestJson(serviceKey,`/endpoint/entities/definitions/${encodeURIComponent(entityKey)}/publish`,{method:"POST",tenantKey:scope.tenantKey,siteKey:scope.siteKey,body:"{}"});}
export function getRecord(serviceKey:DynamicServiceKey,entityKey:string,recordKey:string,scope:ScopedRequest):Promise<DynamicEntityRecord>{return requestJson(serviceKey,`/endpoint/entities/records/${encodeURIComponent(entityKey)}/${encodeURIComponent(recordKey)}`,scope);}
export function listRecordsPage(serviceKey:DynamicServiceKey,entityKey:string,scope:ScopedRequest,page=0,size=25,sort="createdAt,desc"):Promise<DynamicPage<DynamicEntityRecord>>{return requestJson(serviceKey,`/endpoint/entities/records/${encodeURIComponent(entityKey)}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,scope);}

export function saveDefinition(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  definitionText: string,
  scope: ScopedRequest,
  expectedRevision?: number
): Promise<DynamicEntityDefinition> {
  const definition = JSON.parse(definitionText) as Record<string, unknown>;
  return requestJson<DynamicEntityDefinition>(serviceKey, `/endpoint/entities/definitions/${entityKey}`, {
    method: "PUT",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify({
      entityKey,
      tenantKey: scope.tenantKey,
      siteKey: scope.siteKey,
      definition
      , expectedRevision
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

export function replaceRecord(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  recordKey: string,
  data: Record<string, unknown>,
  scope: ScopedRequest
): Promise<DynamicEntityRecord> {
  return requestJson<DynamicEntityRecord>(serviceKey, `/endpoint/entities/records/${entityKey}/${encodeURIComponent(recordKey)}`, {
    method: "PUT",
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

export function updateRecord(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  recordKey: string,
  data: Record<string, unknown>,
  scope: ScopedRequest
): Promise<DynamicEntityRecord> {
  return requestJson<DynamicEntityRecord>(serviceKey, `/endpoint/entities/records/${entityKey}/${encodeURIComponent(recordKey)}`, {
    method: "PATCH",
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

export function deleteRecord(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  recordKey: string,
  scope: ScopedRequest
): Promise<void> {
  return requestJson<void>(serviceKey, `/endpoint/entities/records/${entityKey}/${encodeURIComponent(recordKey)}`, {
    method: "DELETE",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export type RelationLookupItem = { recordKey: string; label: string };
export type RelationLookupPage = { items: RelationLookupItem[]; total: number; page: number; size: number };

/** Searchable picker source for a relation field, scoped to the active tenant/site. */
export function lookupRelationRecords(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  scope: ScopedRequest,
  options: { query?: string; displayField?: string; page?: number; size?: number } = {}
): Promise<RelationLookupPage> {
  const params = new URLSearchParams();
  if (options.query) params.set("q", options.query);
  if (options.displayField) params.set("displayField", options.displayField);
  params.set("page", String(options.page ?? 0));
  params.set("size", String(options.size ?? 20));
  return requestJson(serviceKey, `/endpoint/entities/lookup/${encodeURIComponent(entityKey)}?${params}`, scope);
}

/** Turns already-stored relation keys back into labels so edit forms show names, not raw keys. */
export function resolveRelationRecords(
  serviceKey: DynamicServiceKey,
  entityKey: string,
  keys: string[],
  scope: ScopedRequest,
  displayField?: string
): Promise<RelationLookupItem[]> {
  const params = new URLSearchParams();
  keys.forEach((key) => params.append("keys", key));
  if (displayField) params.set("displayField", displayField);
  return requestJson(serviceKey, `/endpoint/entities/lookup/${encodeURIComponent(entityKey)}/resolve?${params}`, scope);
}

/** Anonymous public-form lookup; only works for relation fields marked publicLookup in the definition. */
export async function lookupPublicRelationRecords(
  slug: string,
  fieldName: string,
  options: { query?: string; page?: number; size?: number } = {}
): Promise<RelationLookupPage> {
  const params = new URLSearchParams();
  if (options.query) params.set("q", options.query);
  params.set("page", String(options.page ?? 0));
  params.set("size", String(options.size ?? 20));
  const response = await fetch(`/api/public/forms/${encodeURIComponent(slug)}/relations/${encodeURIComponent(fieldName)}?${params}`, { cache: "no-store" });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<RelationLookupPage>;
}
