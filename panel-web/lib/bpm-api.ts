const platformBaseUrl = process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8001";

export type FlowStateDraft = {
  id: string;
  displayName: string;
  terminal: boolean;
  formKey?: string;
  entityService?: string;
  entityKey?: string;
  submitMode?: "DYNAMIC" | "STATIC";
  submitUrl?: string;
};

export type FlowTransitionDraft = {
  id: string;
  fromState: string;
  toState: string;
  label: string;
  conditionExpression?: string;
  conditionOperator?: "AND" | "OR";
};

export type DynamicFlowDefinition = {
  id?: string;
  tenantKey?: string;
  siteKey?: string;
  flowKey: string;
  version?: number;
  name: string;
  description?: string;
  startState: string;
  states: FlowStateDraft[];
  transitions: FlowTransitionDraft[];
  active?: boolean;
  updatedAt?: string;
};

async function requestJson<T>(path: string, init?: RequestInit & { tenantKey?: string; siteKey?: string }): Promise<T> {
  const response = await fetch(`${platformBaseUrl}${path}`, {
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

export function listFlows(scope: { tenantKey?: string; siteKey?: string }): Promise<DynamicFlowDefinition[]> {
  return requestJson<DynamicFlowDefinition[]>("/endpoint/bpm/flows", {
    method: "GET",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export function saveFlow(
  flow: DynamicFlowDefinition,
  scope: { tenantKey?: string; siteKey?: string }
): Promise<DynamicFlowDefinition> {
  return requestJson<DynamicFlowDefinition>("/endpoint/bpm/flows", {
    method: "POST",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify(flow)
  });
}
