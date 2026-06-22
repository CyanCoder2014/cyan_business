import { platformFetch } from "@/lib/platform-auth";

const platformBaseUrl = process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8001";

export type FlowActionDraft = {
  type: string;
  params: Record<string, unknown>;
};

export type MetadataFieldDescriptor = {
  key: string;
  type: string;
  required: boolean;
  description: string;
  example?: unknown;
};

export type FlowConditionDraft = {
  field: string;
  operator: string;
  value?: unknown;
};

export type FlowStateDraft = {
  id: string;
  displayName: string;
  terminal: boolean;
  formKey?: string;
  processorKey?: string;
  reviewCommentRequired?: boolean;
  candidateGroups?: string[];
  onEnterActions?: FlowActionDraft[];
  accessRule?: {
    canRead?: string[];
    canEdit?: string[];
    canApprove?: string[];
  };
  entityService?: string;
  entityKey?: string;
  rendererService?: string;
  rendererKey?: string;
  submitMode?: "DYNAMIC" | "STATIC";
  submitUrl?: string;
  waitForAutomation?: boolean;
};

export type FlowTransitionDraft = {
  id: string;
  fromState: string;
  toState: string;
  label: string;
  allowedGroups?: string[];
  allowedRoles?: string[];
  conditionExpression?: string;
  conditionOperator?: "AND" | "OR";
  conditions?: FlowConditionDraft[];
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

export type BpmActionStructure = {
  type: string;
  aliases?: string[];
  description: string;
  commonFields?: MetadataFieldDescriptor[];
  params?: MetadataFieldDescriptor[];
};

export type BpmConditionStructure = {
  conditionFields?: MetadataFieldDescriptor[];
  operators: Array<{
    key: string;
    valueShape: string;
    description: string;
    example?: unknown;
  }>;
  logicalOperators: Array<"AND" | "OR">;
  expressionSyntaxSupported?: boolean;
  supportedFields: string[];
};

export type ManagedObjectRef = {
  service?: string;
  entityKey?: string;
  recordKey?: string;
};

export type ManagedObject = {
  id: string;
  tenantKey?: string;
  siteKey?: string;
  objectType: string;
  objectRef?: ManagedObjectRef;
  flowKey: string;
  state: string;
  processInstanceId?: string;
  assignee?: string;
  payload: Record<string, unknown>;
  accessRule?: {
    canRead?: string[];
    canEdit?: string[];
    canApprove?: string[];
  };
  locked?: boolean;
  auditLog?: string[];
  transitionHistory?: Array<Record<string, unknown>>;
  asyncActionRegistry?: Array<Record<string, unknown>>;
  automationBlockRegistry?: Array<Record<string, unknown>>;
  createdAt?: string;
  updatedAt?: string;
};

export type ManagedObjectActiveFormResponse = {
  objectId: string;
  objectType: string;
  flowKey: string;
  state: string;
  formKey?: string;
  processorKey?: string;
  submittedFormId?: string;
  accessRule?: {
    canRead?: string[];
    canEdit?: string[];
    canApprove?: string[];
  };
  rendererDefinition?: Record<string, unknown>;
  entityService?: string;
  entityKey?: string;
  submitMode?: string;
};

export type ManagedObjectFormSubmissionResponse = {
  object: ManagedObject;
  submittedFormId?: string;
  currentFormValues?: Record<string, unknown>;
};

export type TransitionOptionResponse = {
  nextState: string;
  label: string;
  conditionExpression?: string;
  allowedGroups?: string[];
  allowedRoles?: string[];
};

async function requestJson<T>(path: string, init?: RequestInit & { tenantKey?: string; siteKey?: string }): Promise<T> {
  const response = await platformFetch(`${platformBaseUrl}${path}`, {
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

export function getFlow(flowKey: string, scope: { tenantKey?: string; siteKey?: string }): Promise<DynamicFlowDefinition> {
  return requestJson<DynamicFlowDefinition>(`/endpoint/bpm/flows/${encodeURIComponent(flowKey)}`, {
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

export function activateFlow(
  flowKey: string,
  version: number,
  scope: { tenantKey?: string; siteKey?: string }
): Promise<DynamicFlowDefinition> {
  return requestJson<DynamicFlowDefinition>(`/endpoint/bpm/flows/${encodeURIComponent(flowKey)}/activate/${version}`, {
    method: "POST",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify({})
  });
}

export function listActionMetadata(scope: { tenantKey?: string; siteKey?: string }): Promise<BpmActionStructure[]> {
  return requestJson<BpmActionStructure[]>("/endpoint/bpm/metadata/state-actions", {
    method: "GET",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export function getConditionMetadata(scope: { tenantKey?: string; siteKey?: string }): Promise<BpmConditionStructure> {
  return requestJson<BpmConditionStructure>("/endpoint/bpm/metadata/transition-conditions", {
    method: "GET",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export function listManagedObjects(scope: { tenantKey?: string; siteKey?: string }): Promise<ManagedObject[]> {
  return requestJson<ManagedObject[]>("/endpoint/bpm/managed-objects", {
    method: "GET",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export function listAssignedManagedObjects(scope: { tenantKey?: string; siteKey?: string }): Promise<ManagedObject[]> {
  return requestJson<ManagedObject[]>("/endpoint/bpm/managed-objects/assigned-to-me", {
    method: "GET",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export function listVisibleManagedObjects(scope: { tenantKey?: string; siteKey?: string }): Promise<ManagedObject[]> {
  return requestJson<ManagedObject[]>("/endpoint/bpm/managed-objects/visible-to-me", {
    method: "GET",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export function createManagedObject(
  request: {
    flowKey: string;
    objectType: string;
    objectRef?: ManagedObjectRef;
    payload?: Record<string, unknown>;
  },
  scope: { tenantKey?: string; siteKey?: string }
): Promise<ManagedObject> {
  return requestJson<ManagedObject>("/endpoint/bpm/managed-objects", {
    method: "POST",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify(request)
  });
}

export function getActiveManagedObjectForm(
  objectId: string,
  scope: { tenantKey?: string; siteKey?: string }
): Promise<ManagedObjectActiveFormResponse> {
  return requestJson<ManagedObjectActiveFormResponse>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/active-form`, {
    method: "GET",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export function submitManagedObjectForm(
  objectId: string,
  request: {
    formData: Record<string, unknown>;
    nextState?: string;
    context?: Record<string, unknown>;
  },
  scope: { tenantKey?: string; siteKey?: string }
): Promise<ManagedObjectFormSubmissionResponse> {
  return requestJson<ManagedObjectFormSubmissionResponse>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/active-form/submissions`, {
    method: "POST",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify(request)
  });
}

export function listTransitionOptions(
  objectId: string,
  scope: { tenantKey?: string; siteKey?: string }
): Promise<TransitionOptionResponse[]> {
  return requestJson<TransitionOptionResponse[]>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/transitions`, {
    method: "GET",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey
  });
}

export function transitionManagedObject(
  objectId: string,
  request: {
    nextState: string;
    context?: Record<string, unknown>;
  },
  scope: { tenantKey?: string; siteKey?: string }
): Promise<ManagedObject> {
  return requestJson<ManagedObject>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/transitions`, {
    method: "POST",
    tenantKey: scope.tenantKey,
    siteKey: scope.siteKey,
    body: JSON.stringify(request)
  });
}
