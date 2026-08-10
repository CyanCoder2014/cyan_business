import { platformFetch } from "@/lib/platform-auth";
import { platformErrorFromResponse } from "@/lib/api-error";

const platformBaseUrl = "/api/platform/service/bpm-service";

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
  lifecycleStatus?: string;
  revision?: number;
  layout?: Record<string, { x?: number; y?: number }>;
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
  assigneeType?: "USER" | "GROUP" | "ROLE";
  payload: Record<string, unknown>;
  accessRule?: {
    canRead?: string[];
    canEdit?: string[];
    canApprove?: string[];
  };
  locked?: boolean;
  lockedBy?: string;
  priority?: string;
  dueAt?: string;
  completedAt?: string;
  auditLog?: string[];
  transitionHistory?: Array<Record<string, unknown>>;
  asyncActionRegistry?: Array<Record<string, unknown>>;
  automationBlockRegistry?: Array<Record<string, unknown>>;
  createdAt?: string;
  updatedAt?: string;
};

export type ManagedObjectQueue = { content: ManagedObject[]; totalElements: number; page: number; size: number };
export type AssignmentTarget = { type: "USER" | "ROLE" | "GROUP"; key: string; displayName: string; active: boolean };

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
  transitionId: string;
  fromState: string;
  toState: string;
  label: string;
  conditionExpression?: string;
  allowedGroups?: string[];
  allowedRoles?: string[];
};
export type ManagedObjectComment={id:string;objectId:string;body:string;authorUserId?:string;createdAt?:string};
export type ManagedObjectAttachment={id:string;objectId:string;assetKey:string;fileName?:string;contentType?:string;sizeBytes?:number;authorUserId?:string;createdAt?:string};

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
    throw await platformErrorFromResponse(response);
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

export function listCartable(params: {
  tenantKey?: string; siteKey?: string; view?: string; state?: string; priority?: string;
  overdue?: boolean; query?: string; page?: number; size?: number;
}): Promise<ManagedObjectQueue> {
  const query = new URLSearchParams({ view: params.view ?? "ASSIGNED", page: String(params.page ?? 0), size: String(params.size ?? 20) });
  if (params.state) query.set("state", params.state);
  if (params.priority) query.set("priority", params.priority);
  if (params.overdue !== undefined) query.set("overdue", String(params.overdue));
  if (params.query) query.set("query", params.query);
  return requestJson<ManagedObjectQueue>(`/endpoint/bpm/managed-objects/cartable?${query}`, { method: "GET", tenantKey: params.tenantKey, siteKey: params.siteKey });
}

export function listAssignmentTargets(type: "USER" | "ROLE" | "GROUP", query: string, scope: { tenantKey?: string; siteKey?: string }) {
  const search = new URLSearchParams({ type });
  if (query.trim()) search.set("query", query.trim());
  return requestJson<AssignmentTarget[]>(`/endpoint/bpm/managed-objects/assignment-targets?${search}`, { method: "GET", tenantKey: scope.tenantKey, siteKey: scope.siteKey });
}

export async function listSiteWorkPortal(params: { tenantKey: string; siteKey: string; view?: string; page?: number; size?: number }) {
  const query = new URLSearchParams({ view: params.view ?? "VISIBLE", page: String(params.page ?? 0), size: String(params.size ?? 20) });
  const response = await platformFetch(`/api/platform/service/storefront-service/endpoint/sites/${encodeURIComponent(params.siteKey)}/portal/work?${query}`, { method: "GET", headers: { "X-Tenant-Key": params.tenantKey, "X-Site-Key": params.siteKey }, cache: "no-store" });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<ManagedObjectQueue>;
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

export function getManagedObject(objectId:string,scope:{tenantKey?:string;siteKey?:string}){return requestJson<ManagedObject>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}`,{method:"GET",...scope})}
export function listComments(objectId:string,scope:{tenantKey?:string;siteKey?:string}){return requestJson<ManagedObjectComment[]>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/comments`,{method:"GET",...scope})}
export function addComment(objectId:string,body:string,scope:{tenantKey?:string;siteKey?:string}){return requestJson<ManagedObjectComment>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/comments`,{method:"POST",body:JSON.stringify({body}),...scope})}
export function listAttachments(objectId:string,scope:{tenantKey?:string;siteKey?:string}){return requestJson<ManagedObjectAttachment[]>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/attachments`,{method:"GET",...scope})}
export function addAttachment(objectId:string,request:{assetKey:string;fileName?:string;contentType?:string;sizeBytes?:number;downloadUrl?:string|null},scope:{tenantKey?:string;siteKey?:string}){return requestJson<ManagedObjectAttachment>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/attachments`,{method:"POST",body:JSON.stringify(request),...scope})}
export function assignManagedObject(objectId:string,assignee:string,assigneeType:"USER"|"GROUP"|"ROLE",scope:{tenantKey?:string;siteKey?:string}){return requestJson<ManagedObject>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/assignment`,{method:"PUT",body:JSON.stringify({assignee,assigneeType}),...scope})}
export function setManagedObjectLock(objectId:string,locked:boolean,scope:{tenantKey?:string;siteKey?:string}){return requestJson<ManagedObject>(`/endpoint/bpm/managed-objects/${encodeURIComponent(objectId)}/${locked?"lock":"unlock"}`,{method:"POST",body:"{}",...scope})}
