"use client";

import { useEffect, useMemo, useState } from "react";
import {
  Background,
  Controls,
  MiniMap,
  ReactFlow,
  addEdge,
  type Connection,
  type Edge,
  type Node,
  useEdgesState,
  useNodesState
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { AppShell } from "@/components/app-shell";
import {
  activateFlow,
  createManagedObject,
  getActiveManagedObjectForm,
  getConditionMetadata,
  listActionMetadata,
  listManagedObjects,
  listTransitionOptions,
  saveFlow,
  submitManagedObjectForm,
  transitionManagedObject,
  type BpmActionStructure,
  type FlowActionDraft,
  type FlowConditionDraft,
  type FlowStateDraft,
  type FlowTransitionDraft,
  type ManagedObject,
  type ManagedObjectActiveFormResponse,
  type TransitionOptionResponse
} from "@/lib/bpm-api";

const initialNodes: Node[] = [
  { id: "start", type: "input", position: { x: 80, y: 120 }, data: { label: "Start / Intake" } },
  { id: "review", position: { x: 420, y: 80 }, data: { label: "Review request" } },
  { id: "approved", type: "output", position: { x: 780, y: 70 }, data: { label: "Approved" } },
  { id: "rejected", type: "output", position: { x: 780, y: 210 }, data: { label: "Rejected" } }
];

const initialEdges: Edge[] = [
  { id: "start-review", source: "start", target: "review", label: "submit" },
  { id: "review-approved", source: "review", target: "approved", label: "approve" },
  { id: "review-rejected", source: "review", target: "rejected", label: "reject" }
];

const initialStateConfig: Record<string, FlowStateDraft> = {
  start: {
    id: "start",
    displayName: "Start / Intake",
    terminal: false,
    formKey: "custom-form",
    entityService: "content-service",
    entityKey: "custom-form",
    submitMode: "DYNAMIC",
    candidateGroups: ["operators"],
    onEnterActions: [],
    waitForAutomation: false
  },
  review: {
    id: "review",
    displayName: "Review request",
    terminal: false,
    formKey: "custom-form",
    entityService: "content-service",
    entityKey: "custom-form",
    submitMode: "DYNAMIC",
    candidateGroups: ["reviewers"],
    onEnterActions: [],
    waitForAutomation: false
  },
  approved: {
    id: "approved",
    displayName: "Approved",
    terminal: true,
    candidateGroups: ["approvers"],
    onEnterActions: [],
    waitForAutomation: false
  },
  rejected: {
    id: "rejected",
    displayName: "Rejected",
    terminal: true,
    candidateGroups: ["approvers"],
    onEnterActions: [],
    waitForAutomation: false
  }
};

const initialTransitionConfig: Record<string, FlowTransitionDraft> = {
  "start-review": {
    id: "start-review",
    fromState: "start",
    toState: "review",
    label: "submit",
    conditionOperator: "AND",
    conditions: []
  },
  "review-approved": {
    id: "review-approved",
    fromState: "review",
    toState: "approved",
    label: "approve",
    conditionOperator: "AND",
    conditions: []
  },
  "review-rejected": {
    id: "review-rejected",
    fromState: "review",
    toState: "rejected",
    label: "reject",
    conditionOperator: "AND",
    conditions: []
  }
};

function parseList(value: string) {
  return value.split(",").map((item) => item.trim()).filter(Boolean);
}

function stringifyList(value?: string[]) {
  return value?.join(", ") ?? "";
}

function parseJsonRecord(value: string) {
  return value.trim() ? (JSON.parse(value) as Record<string, unknown>) : {};
}

function prettyJson(value: unknown) {
  return JSON.stringify(value, null, 2);
}

function defaultStateConfig(id: string, label: string, terminal = false): FlowStateDraft {
  return {
    id,
    displayName: label,
    terminal,
    submitMode: "DYNAMIC",
    candidateGroups: [],
    onEnterActions: [],
    waitForAutomation: false
  };
}

export default function FlowsPage() {
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [flowKey, setFlowKey] = useState("custom-approval-flow");
  const [flowName, setFlowName] = useState("Custom Approval Flow");
  const [description, setDescription] = useState("Created from the visual flow maker and wired to managed-object APIs.");
  const [objectType, setObjectType] = useState("FORM_SUBMISSION");
  const [objectRecordKey, setObjectRecordKey] = useState("draft-intake-1");
  const [managedPayloadJson, setManagedPayloadJson] = useState("{\n  \"customerName\": \"Retail Demo\",\n  \"requestedPlan\": \"plus\"\n}");
  const [formDataJson, setFormDataJson] = useState("{\n  \"customerName\": \"Retail Demo\",\n  \"mobile\": \"09121234567\"\n}");
  const [transitionContextJson, setTransitionContextJson] = useState("{\n  \"source\": \"panel-web\"\n}");
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [metadataLoading, setMetadataLoading] = useState(false);
  const [actionMetadata, setActionMetadata] = useState<BpmActionStructure[]>([]);
  const [conditionOperators, setConditionOperators] = useState<string[]>([]);
  const [supportedFields, setSupportedFields] = useState<string[]>([]);
  const [managedObjects, setManagedObjects] = useState<ManagedObject[]>([]);
  const [activeForm, setActiveForm] = useState<ManagedObjectActiveFormResponse | null>(null);
  const [transitionOptions, setTransitionOptions] = useState<TransitionOptionResponse[]>([]);
  const [selectedManagedObjectId, setSelectedManagedObjectId] = useState<string | null>(null);
  const [selectedElement, setSelectedElement] = useState<{ type: "node" | "edge"; id: string } | null>({ type: "node", id: "review" });
  const [actionDrafts, setActionDrafts] = useState<Record<string, string>>({});
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [stateConfig, setStateConfig] = useState<Record<string, FlowStateDraft>>(initialStateConfig);
  const [transitionConfig, setTransitionConfig] = useState<Record<string, FlowTransitionDraft>>(initialTransitionConfig);

  const selectedState = selectedElement?.type === "node" ? stateConfig[selectedElement.id] : null;
  const selectedTransition = selectedElement?.type === "edge" ? transitionConfig[selectedElement.id] : null;
  const selectedManagedObject = managedObjects.find((item) => item.id === selectedManagedObjectId) ?? null;

  useEffect(() => {
    async function loadMetadata() {
      setMetadataLoading(true);
      try {
        const [actions, conditionMeta] = await Promise.all([
          listActionMetadata({ tenantKey, siteKey }),
          getConditionMetadata({ tenantKey, siteKey })
        ]);
        setActionMetadata(actions);
        setConditionOperators(conditionMeta.operators);
        setSupportedFields(conditionMeta.supportedFields);
      } catch (error) {
        setStatus(error instanceof Error ? error.message : "Failed to load BPM metadata");
      } finally {
        setMetadataLoading(false);
      }
    }
    loadMetadata();
  }, [tenantKey, siteKey]);

  const flowDefinition = useMemo(() => ({
    flowKey,
    version: 1,
    name: flowName,
    description,
    startState: nodes[0]?.id ?? "start",
    active: true,
    states: nodes.map((node) => ({
      ...defaultStateConfig(node.id, String(node.data.label ?? node.id), node.type === "output"),
      ...stateConfig[node.id],
      id: node.id,
      displayName: String(node.data.label ?? node.id),
      terminal: node.type === "output"
    })),
    transitions: edges.map((edge) => ({
      ...transitionConfig[edge.id],
      id: edge.id,
      fromState: edge.source,
      toState: edge.target,
      label: String(edge.label ?? transitionConfig[edge.id]?.label ?? "transition")
    }))
  }), [description, edges, flowKey, flowName, nodes, stateConfig, transitionConfig]);

  function onConnect(connection: Connection) {
    const id = `${connection.source}-${connection.target}-${Date.now()}`;
    setEdges((current) => addEdge({ ...connection, id, label: "transition" }, current));
    setTransitionConfig((current) => ({
      ...current,
      [id]: {
        id,
        fromState: connection.source ?? "",
        toState: connection.target ?? "",
        label: "transition",
        conditionOperator: "AND",
        conditions: []
      }
    }));
    setSelectedElement({ type: "edge", id });
  }

  function addNode(kind: "form" | "condition" | "action" | "terminal") {
    const id = `${kind}-${Date.now().toString(36)}`;
    const label = kind === "form" ? "Collect form" : kind === "condition" ? "Check condition" : kind === "action" ? "Run action" : "Done";
    setNodes((current) => [
      ...current,
      {
        id,
        type: kind === "terminal" ? "output" : "default",
        position: { x: 180 + current.length * 90, y: 180 + current.length * 35 },
        data: { label }
      }
    ]);
    setStateConfig((current) => ({
      ...current,
      [id]: defaultStateConfig(id, label, kind === "terminal")
    }));
    setSelectedElement({ type: "node", id });
  }

  function updateNodeLabel(nodeId: string, label: string) {
    setNodes((current) => current.map((node) => (node.id === nodeId ? { ...node, data: { ...node.data, label } } : node)));
    setStateConfig((current) => ({
      ...current,
      [nodeId]: {
        ...defaultStateConfig(nodeId, label),
        ...current[nodeId],
        displayName: label
      }
    }));
  }

  function updateSelectedState(patch: Partial<FlowStateDraft>) {
    if (!selectedState) return;
    setStateConfig((current) => ({
      ...current,
      [selectedState.id]: {
        ...defaultStateConfig(selectedState.id, selectedState.displayName, selectedState.terminal),
        ...current[selectedState.id],
        ...patch
      }
    }));
  }

  function updateSelectedTransition(patch: Partial<FlowTransitionDraft>) {
    if (!selectedTransition) return;
    setTransitionConfig((current) => ({
      ...current,
      [selectedTransition.id]: {
        ...current[selectedTransition.id],
        ...patch
      }
    }));
    if (patch.label) {
      setEdges((current) => current.map((edge) => (edge.id === selectedTransition.id ? { ...edge, label: patch.label } : edge)));
    }
  }

  function addAction(type: string) {
    if (!selectedState) return;
    const nextActions = [...(selectedState.onEnterActions ?? []), { type, params: {} }];
    updateSelectedState({ onEnterActions: nextActions });
  }

  function updateAction(index: number, patch: Partial<FlowActionDraft>) {
    if (!selectedState) return;
    const nextActions = [...(selectedState.onEnterActions ?? [])];
    nextActions[index] = { ...nextActions[index], ...patch };
    updateSelectedState({ onEnterActions: nextActions });
  }

  function removeAction(index: number) {
    if (!selectedState) return;
    updateSelectedState({ onEnterActions: (selectedState.onEnterActions ?? []).filter((_, itemIndex) => itemIndex !== index) });
    setActionDrafts((current) => {
      const next = { ...current };
      delete next[`${selectedState.id}:${index}`];
      return next;
    });
  }

  function commitActionParams(index: number, raw: string) {
    try {
      updateAction(index, { params: parseJsonRecord(raw) });
      setStatus("Action params updated.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Action params JSON is invalid");
    }
  }

  function addCondition() {
    if (!selectedTransition) return;
    const nextConditions = [
      ...(selectedTransition.conditions ?? []),
      { field: "payload.status", operator: conditionOperators[0] ?? "EQ", value: "READY" }
    ];
    updateSelectedTransition({ conditions: nextConditions });
  }

  function updateCondition(index: number, patch: Partial<FlowConditionDraft>) {
    if (!selectedTransition) return;
    const nextConditions = [...(selectedTransition.conditions ?? [])];
    nextConditions[index] = { ...nextConditions[index], ...patch };
    updateSelectedTransition({ conditions: nextConditions });
  }

  function removeCondition(index: number) {
    if (!selectedTransition) return;
    updateSelectedTransition({ conditions: (selectedTransition.conditions ?? []).filter((_, itemIndex) => itemIndex !== index) });
  }

  async function publishFlow(activateAfterSave: boolean) {
    setLoading(true);
    setStatus(null);
    try {
      const saved = await saveFlow(flowDefinition, { tenantKey, siteKey });
      if (activateAfterSave && saved.version) {
        await activateFlow(saved.flowKey, saved.version, { tenantKey, siteKey });
      }
      setStatus(activateAfterSave ? "Flow published and activated in bpm-service." : "Flow published to bpm-service.");
      await refreshManagedObjects();
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to publish flow");
    } finally {
      setLoading(false);
    }
  }

  async function refreshManagedObjects() {
    try {
      const items = await listManagedObjects({ tenantKey, siteKey });
      setManagedObjects(items);
      if (!selectedManagedObjectId && items[0]) {
        setSelectedManagedObjectId(items[0].id);
      }
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load managed objects");
    }
  }

  async function createTestObject() {
    setLoading(true);
    setStatus(null);
    try {
      const created = await createManagedObject({
        flowKey,
        objectType,
        objectRef: {
          service: selectedState?.entityService ?? "content-service",
          entityKey: selectedState?.entityKey ?? "custom-form",
          recordKey: objectRecordKey
        },
        payload: parseJsonRecord(managedPayloadJson)
      }, { tenantKey, siteKey });
      setSelectedManagedObjectId(created.id);
      await refreshManagedObjects();
      setStatus(`Managed object ${created.id} created and flow started.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to create managed object");
    } finally {
      setLoading(false);
    }
  }

  async function loadManagedObjectRuntime(objectId: string) {
    setSelectedManagedObjectId(objectId);
    setLoading(true);
    setStatus(null);
    try {
      const [form, options] = await Promise.all([
        getActiveManagedObjectForm(objectId, { tenantKey, siteKey }).catch(() => null),
        listTransitionOptions(objectId, { tenantKey, siteKey })
      ]);
      setActiveForm(form);
      setTransitionOptions(options);
      setStatus(`Loaded runtime data for managed object ${objectId}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load managed object runtime");
    } finally {
      setLoading(false);
    }
  }

  async function submitForm() {
    if (!selectedManagedObjectId) return;
    setLoading(true);
    setStatus(null);
    try {
      const response = await submitManagedObjectForm(selectedManagedObjectId, {
        formData: parseJsonRecord(formDataJson),
        context: parseJsonRecord(transitionContextJson)
      }, { tenantKey, siteKey });
      setActiveForm(null);
      await refreshManagedObjects();
      await loadManagedObjectRuntime(response.object.id);
      setStatus(`Submitted active form ${response.submittedFormId ?? ""} for managed object ${response.object.id}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to submit managed object form");
    } finally {
      setLoading(false);
    }
  }

  async function performTransition(nextState: string) {
    if (!selectedManagedObjectId) return;
    setLoading(true);
    setStatus(null);
    try {
      const object = await transitionManagedObject(selectedManagedObjectId, {
        nextState,
        context: parseJsonRecord(transitionContextJson)
      }, { tenantKey, siteKey });
      await refreshManagedObjects();
      await loadManagedObjectRuntime(object.id);
      setStatus(`Managed object moved to ${object.state}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to transition managed object");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Flow Maker" subtitle="Create BPM, forms, automation actions, and managed-object runtime tests from one canvas.">
      <div className="builder-shell flow-builder-shell">
        <aside className="builder-palette">
          <p className="section-title">Nodes</p>
          {(["form", "condition", "action", "terminal"] as const).map((kind) => (
            <button key={kind} type="button" className="builder-tool" onClick={() => addNode(kind)}>
              <span>{kind === "form" ? "Tt" : kind === "condition" ? "?" : kind === "action" ? "Run" : "OK"}</span>
              {kind}
            </button>
          ))}
          <p className="section-title">Scope</p>
          <div className="field">
            <label>Tenant</label>
            <input value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
          </div>
          <div className="field">
            <label>Site</label>
            <input value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
          </div>
          <div className="builder-tip">
            <strong>Metadata-driven BPM</strong>
            <span>{metadataLoading ? "Loading action and condition structures..." : `${actionMetadata.length} action templates and ${conditionOperators.length} operators loaded from bpm-service.`}</span>
          </div>
        </aside>

        <section className="builder-canvas flow-canvas-wrap">
          <div className="builder-toolbar">
            <div className="segmented">
              <button type="button" className="active">Flow designer</button>
              <button type="button">Managed object tester</button>
            </div>
            <div className="hero-actions" style={{ marginTop: 0 }}>
              <button type="button" className="ghost-btn" onClick={() => publishFlow(false)} disabled={loading}>
                {loading ? "Saving..." : "Save flow"}
              </button>
              <button type="button" className="btn" onClick={() => publishFlow(true)} disabled={loading}>
                {loading ? "Publishing..." : "Publish + activate"}
              </button>
            </div>
          </div>
          <div className="ai-banner">AI Applied - States, actions, transition conditions, and managed-object runtime are wired to the actual BPM APIs.</div>

          <div className="form-preview-card">
            <div className="field-grid">
              <div className="field">
                <label>Flow key</label>
                <input value={flowKey} onChange={(event) => setFlowKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Flow name</label>
                <input value={flowName} onChange={(event) => setFlowName(event.target.value)} />
              </div>
            </div>
            <div className="field">
              <label>Description</label>
              <textarea value={description} onChange={(event) => setDescription(event.target.value)} />
            </div>

            <div className="flow-canvas">
              <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                onNodeClick={(_, node) => setSelectedElement({ type: "node", id: node.id })}
                onEdgeClick={(_, edge) => setSelectedElement({ type: "edge", id: edge.id })}
                fitView
              >
                <Background gap={18} size={1} />
                <Controls />
                <MiniMap />
              </ReactFlow>
            </div>

            <div className="result-grid">
              <div className="result-card">
                <h4>Managed object tester</h4>
                <div className="form-grid">
                  <div className="field-grid">
                    <div className="field">
                      <label>Object type</label>
                      <input value={objectType} onChange={(event) => setObjectType(event.target.value)} />
                    </div>
                    <div className="field">
                      <label>Record key</label>
                      <input value={objectRecordKey} onChange={(event) => setObjectRecordKey(event.target.value)} />
                    </div>
                  </div>
                  <div className="field">
                    <label>Initial payload JSON</label>
                    <textarea value={managedPayloadJson} onChange={(event) => setManagedPayloadJson(event.target.value)} />
                  </div>
                  <div className="hero-actions">
                    <button type="button" className="btn" onClick={createTestObject} disabled={loading}>Create test object</button>
                    <button type="button" className="ghost-btn" onClick={refreshManagedObjects} disabled={loading}>Refresh objects</button>
                  </div>
                </div>
              </div>

              <div className="result-card">
                <h4>Active form and transitions</h4>
                <div className="field">
                  <label>Form submission JSON</label>
                  <textarea value={formDataJson} onChange={(event) => setFormDataJson(event.target.value)} />
                </div>
                <div className="field">
                  <label>Transition context JSON</label>
                  <textarea value={transitionContextJson} onChange={(event) => setTransitionContextJson(event.target.value)} />
                </div>
                <div className="hero-actions">
                  <button type="button" className="btn" onClick={submitForm} disabled={loading || !selectedManagedObjectId}>Submit active form</button>
                </div>
                <pre className="json-view">{prettyJson(activeForm)}</pre>
              </div>
            </div>

            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>

        <aside className="builder-inspector">
          {selectedState ? (
            <>
              <p className="section-title">State inspector</p>
              <div className="form-grid">
                <div className="field">
                  <label>Display name</label>
                  <input value={selectedState.displayName} onChange={(event) => updateNodeLabel(selectedState.id, event.target.value)} />
                </div>
                <div className="field-grid">
                  <div className="field">
                    <label>Form key</label>
                    <input value={selectedState.formKey ?? ""} onChange={(event) => updateSelectedState({ formKey: event.target.value })} />
                  </div>
                  <div className="field">
                    <label>Processor key</label>
                    <input value={selectedState.processorKey ?? ""} onChange={(event) => updateSelectedState({ processorKey: event.target.value })} />
                  </div>
                </div>
                <div className="field-grid">
                  <div className="field">
                    <label>Entity service</label>
                    <input value={selectedState.entityService ?? ""} onChange={(event) => updateSelectedState({ entityService: event.target.value })} />
                  </div>
                  <div className="field">
                    <label>Entity key</label>
                    <input value={selectedState.entityKey ?? ""} onChange={(event) => updateSelectedState({ entityKey: event.target.value })} />
                  </div>
                </div>
                <div className="field-grid">
                  <div className="field">
                    <label>Submit mode</label>
                    <select value={selectedState.submitMode ?? "DYNAMIC"} onChange={(event) => updateSelectedState({ submitMode: event.target.value as "DYNAMIC" | "STATIC" })}>
                      <option value="DYNAMIC">DYNAMIC</option>
                      <option value="STATIC">STATIC</option>
                    </select>
                  </div>
                  <div className="field">
                    <label>Submit URL</label>
                    <input value={selectedState.submitUrl ?? ""} onChange={(event) => updateSelectedState({ submitUrl: event.target.value })} />
                  </div>
                </div>
                <div className="field">
                  <label>Candidate groups</label>
                  <input value={stringifyList(selectedState.candidateGroups)} onChange={(event) => updateSelectedState({ candidateGroups: parseList(event.target.value) })} />
                </div>
                <div className="chip-row">
                  <button type="button" className={`chip ${selectedState.reviewCommentRequired ? "active" : ""}`} onClick={() => updateSelectedState({ reviewCommentRequired: !selectedState.reviewCommentRequired })}>
                    Review comment required
                  </button>
                  <button type="button" className={`chip ${selectedState.waitForAutomation ? "active" : ""}`} onClick={() => updateSelectedState({ waitForAutomation: !selectedState.waitForAutomation })}>
                    Wait for automation
                  </button>
                </div>
              </div>

              <p className="section-title" style={{ marginTop: 20 }}>On-enter actions</p>
              <div className="chip-row">
                {actionMetadata.map((item) => (
                  <button key={item.type} type="button" className="chip" onClick={() => addAction(item.type)}>
                    {item.type}
                  </button>
                ))}
              </div>
              <div className="draft-list">
                {(selectedState.onEnterActions ?? []).map((action, index) => (
                  <div key={`${action.type}-${index}`} className="draft-item">
                    <strong>
                      <span>{action.type}</span>
                      <button type="button" className="chip" onClick={() => removeAction(index)}>Remove</button>
                    </strong>
                    <div className="field">
                      <label>Params JSON</label>
                      <textarea
                        value={actionDrafts[`${selectedState.id}:${index}`] ?? prettyJson(action.params)}
                        onChange={(event) => setActionDrafts((current) => ({
                          ...current,
                          [`${selectedState.id}:${index}`]: event.target.value
                        }))}
                        onBlur={(event) => commitActionParams(index, event.target.value)}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </>
          ) : null}

          {selectedTransition ? (
            <>
              <p className="section-title" style={{ marginTop: selectedState ? 24 : 0 }}>Transition inspector</p>
              <div className="form-grid">
                <div className="field">
                  <label>Label</label>
                  <input value={selectedTransition.label} onChange={(event) => updateSelectedTransition({ label: event.target.value })} />
                </div>
                <div className="field-grid">
                  <div className="field">
                    <label>Allowed groups</label>
                    <input value={stringifyList(selectedTransition.allowedGroups)} onChange={(event) => updateSelectedTransition({ allowedGroups: parseList(event.target.value) })} />
                  </div>
                  <div className="field">
                    <label>Allowed roles</label>
                    <input value={stringifyList(selectedTransition.allowedRoles)} onChange={(event) => updateSelectedTransition({ allowedRoles: parseList(event.target.value) })} />
                  </div>
                </div>
                <div className="field">
                  <label>Condition expression</label>
                  <input value={selectedTransition.conditionExpression ?? ""} onChange={(event) => updateSelectedTransition({ conditionExpression: event.target.value })} />
                </div>
                <div className="field">
                  <label>Condition operator</label>
                  <select value={selectedTransition.conditionOperator ?? "AND"} onChange={(event) => updateSelectedTransition({ conditionOperator: event.target.value as "AND" | "OR" })}>
                    <option value="AND">AND</option>
                    <option value="OR">OR</option>
                  </select>
                </div>
              </div>

              <div className="hero-actions">
                <button type="button" className="chip" onClick={addCondition}>Add condition</button>
              </div>
              <div className="draft-list">
                {(selectedTransition.conditions ?? []).map((condition, index) => (
                  <div key={`${condition.field}-${index}`} className="draft-item">
                    <strong>
                      <span>Condition {index + 1}</span>
                      <button type="button" className="chip" onClick={() => removeCondition(index)}>Remove</button>
                    </strong>
                    <div className="field">
                      <label>Field</label>
                      <input list="supported-fields" value={condition.field} onChange={(event) => updateCondition(index, { field: event.target.value })} />
                    </div>
                    <div className="field">
                      <label>Operator</label>
                      <select value={condition.operator} onChange={(event) => updateCondition(index, { operator: event.target.value })}>
                        {conditionOperators.map((item) => <option key={item} value={item}>{item}</option>)}
                      </select>
                    </div>
                    <div className="field">
                      <label>Value</label>
                      <input value={String(condition.value ?? "")} onChange={(event) => updateCondition(index, { value: event.target.value })} />
                    </div>
                  </div>
                ))}
              </div>
              <datalist id="supported-fields">
                {supportedFields.map((item) => <option key={item} value={item} />)}
              </datalist>
            </>
          ) : null}

          <p className="section-title" style={{ marginTop: 24 }}>Managed objects</p>
          <div className="draft-list">
            {managedObjects.map((item) => (
              <button
                key={item.id}
                type="button"
                className={`draft-item ${selectedManagedObjectId === item.id ? "active" : ""}`}
                onClick={() => loadManagedObjectRuntime(item.id)}
              >
                <strong>
                  <span>{item.objectType}</span>
                  <span className="muted">{item.state}</span>
                </strong>
                <span className="muted">{item.flowKey} / {item.id}</span>
              </button>
            ))}
          </div>

          <p className="section-title" style={{ marginTop: 20 }}>Available transitions</p>
          <div className="draft-list">
            {transitionOptions.map((item) => (
              <button key={`${item.nextState}-${item.label}`} type="button" className="draft-item" onClick={() => performTransition(item.nextState)}>
                <strong>
                  <span>{item.label}</span>
                  <span className="muted">{item.nextState}</span>
                </strong>
                <span className="muted">{item.conditionExpression || "No inline expression"}</span>
              </button>
            ))}
          </div>

          <p className="section-title" style={{ marginTop: 20 }}>Selected managed object</p>
          <pre className="json-view">{prettyJson(selectedManagedObject)}</pre>
          <p className="section-title" style={{ marginTop: 20 }}>BPM JSON</p>
          <pre className="json-view">{prettyJson(flowDefinition)}</pre>
        </aside>
      </div>
    </AppShell>
  );
}
