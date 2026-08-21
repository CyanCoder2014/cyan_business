"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { Background, BaseEdge, Controls, EdgeLabelRenderer, Handle, MarkerType, MiniMap, Position, ReactFlow, getBezierPath, type Connection, type EdgeChange, type EdgeProps, type Node, type NodeChange, type NodeProps } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import Link from "next/link";
import { AsyncButton, CodeViewer, Dialog, EmptyState, StatusBadge } from "@/components/ui/primitives";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";
import { activateFlow, getConditionMetadata, listActionMetadata, saveFlow, type BpmActionStructure, type BpmConditionStructure, type DynamicFlowDefinition, type FlowConditionDraft, type FlowStateDraft, type FlowTransitionDraft, type MetadataFieldDescriptor } from "@/lib/bpm-api";
import { listAutomationFlows, type AutomationFlow } from "@/lib/automation-api";
import { createDefinition, listDefinitions } from "@/lib/dynamic-api";
import { useAvailableDynamicServices } from "@/lib/use-available-services";
import type { DynamicEntityDefinition, DynamicServiceKey } from "@/lib/types";
import { createProcessor, listProcessors, type ProcessorDefinition } from "@/lib/processor-api";
import { MetadataFieldInput } from "@/components/forms/metadata-field-input";
import { PlayIcon, StopIcon, CircleDotIcon } from "@/components/nav-icons";
import { ArrowRightIcon } from "@/components/auth-icons";
import { useRouter } from "next/navigation";

const blank = (key = ""): DynamicFlowDefinition => ({ flowKey: key, name: "", description: "", startState: "", states: [], transitions: [], active: false, lifecycleStatus: "DRAFT", layout: {} });
const newState = (index: number): FlowStateDraft => ({ id: `state-${crypto.randomUUID().slice(0, 8)}`, displayName: `State ${index + 1}`, terminal: false, candidateGroups: [], onEnterActions: [], submitMode: "DYNAMIC", accessRule: { canRead: [], canEdit: [], canApprove: [] } });
const splitKeys = (value: string) => value.split(",").map(item => item.trim()).filter(Boolean);
const joinKeys = (value?: string[]) => (value ?? []).join(", ");

function BpmStateNode({ data, selected }: NodeProps) {
  const value = data as { label?: string; terminal?: boolean; start?: boolean };
  const Icon = value.start ? PlayIcon : value.terminal ? StopIcon : CircleDotIcon;
  return <div className={`bpm-state-card ${value.terminal ? "terminal" : ""} ${value.start ? "start" : ""} ${selected ? "selected" : ""}`}>
    <Handle type="target" position={Position.Left} isConnectable aria-label="Incoming transition" />
    <span className="bpm-state-kind"><Icon size={16} /></span>
    <div><strong>{String(value.label ?? "State")}</strong><small>{value.start ? "Start" : value.terminal ? "Terminal" : "State"}</small></div>
    <Handle type="source" position={Position.Right} isConnectable aria-label="Outgoing transition" />
  </div>;
}

function TransitionEdge(props: EdgeProps) {
  const [path, labelX, labelY] = getBezierPath(props);
  return <><BaseEdge path={path} markerEnd={props.markerEnd} style={props.style}/><EdgeLabelRenderer><div className={`bpm-transition-label ${props.selected ? "selected" : ""}`} style={{ transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)` }}><span aria-hidden><ArrowRightIcon size={13}/></span><b>{String(props.label ?? "Transition")}</b></div></EdgeLabelRenderer></>;
}

const nodeTypes = { bpmState: BpmStateNode };
const edgeTypes = { transitionEdge: TransitionEdge };

export function BpmDesigner({ initial, prefill }: { initial?: DynamicFlowDefinition; prefill?: { entityService?: string; entityKey?: string } }) {
  const { locale } = usePanel();
  const { tenantKey, siteKey } = useScopeAccess();
  const { showToast } = useToast();
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }), [tenantKey, siteKey]);
  const router = useRouter();
  const [flow, setFlow] = useState(() => {
    if (initial || (!prefill?.entityService && !prefill?.entityKey)) return initial ?? blank();
    const state = { ...newState(0), entityService: prefill.entityService, entityKey: prefill.entityKey, formKey: prefill.entityKey };
    return { ...blank(), states: [state], startState: state.id };
  });
  const [selected, setSelected] = useState<string | null>(null);
  const [selectedTransition, setSelectedTransition] = useState<string | null>(null);
  const [pending, setPending] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [dirty, setDirty] = useState(!initial);
  const [actionStructures, setActionStructures] = useState<BpmActionStructure[]>([]);
  const [automations, setAutomations] = useState<AutomationFlow[]>([]);
  const [conditionMetadata, setConditionMetadata] = useState<BpmConditionStructure | null>(null);

  useEffect(() => { Promise.all([listActionMetadata(scope), getConditionMetadata(scope), listAutomationFlows(scope)]).then(([actions, conditions, definitions]) => { setActionStructures(actions); setConditionMetadata(conditions); setAutomations(definitions.filter(item => item.active || item.lifecycleStatus === "ACTIVE")); }).catch(reason => setError(reason instanceof Error ? reason.message : String(reason))); }, [scope]);
  useEffect(() => { const guard = (event: BeforeUnloadEvent) => { if (dirty) event.preventDefault(); }; window.addEventListener("beforeunload", guard); return () => window.removeEventListener("beforeunload", guard); }, [dirty]);

  const nodes = useMemo<Node[]>(() => flow.states.map((state, index) => ({
    id: state.id,
    type: "bpmState",
    width: 178,
    height: 60,
    position: { x: Number(flow.layout?.[state.id]?.x ?? 120 + (index % 3) * 220), y: Number(flow.layout?.[state.id]?.y ?? 100 + Math.floor(index / 3) * 140) },
    data: { label: state.displayName, terminal: state.terminal, start: state.id === flow.startState },
    selected: selected === state.id
  })), [flow.layout, flow.startState, flow.states, selected]);
  const edges = useMemo(() => flow.transitions.map(transition => ({ id: transition.id, type: "transitionEdge", source: transition.fromState, target: transition.toState, label: transition.label, animated: flow.active, selected: transition.id === selectedTransition, markerEnd: { type: MarkerType.ArrowClosed } })), [flow.active, flow.transitions, selectedTransition]);
  const selectedState = flow.states.find(state => state.id === selected);
  const transition = flow.transitions.find(item => item.id === selectedTransition);
  const onNodesChange = useCallback((changes: NodeChange[]) => {
    const moved = changes.filter((change): change is Extract<NodeChange, { type: "position" }> => change.type === "position" && Boolean(change.position));
    if (moved.length) {
      setFlow(current => ({
        ...current,
        layout: moved.reduce((layout, change) => ({ ...layout, [change.id]: change.position! }), { ...(current.layout ?? {}) })
      }));
      setDirty(true);
    }
    const removed = changes.filter((change): change is Extract<NodeChange, { type: "remove" }> => change.type === "remove").map(change => change.id);
    if (removed.length) {
      setFlow(current => ({
        ...current,
        states: current.states.filter(state => !removed.includes(state.id)),
        transitions: current.transitions.filter(transition => !removed.includes(transition.fromState) && !removed.includes(transition.toState)),
        startState: removed.includes(current.startState) ? "" : current.startState
      }));
      setSelected(current => removed.includes(current ?? "") ? null : current);
      setDirty(true);
    }
  }, []);
  const onEdgesChange = useCallback((changes: EdgeChange[]) => {
    const removed = changes.filter((change): change is Extract<EdgeChange, { type: "remove" }> => change.type === "remove").map(change => change.id);
    if (!removed.length) return;
    setFlow(current => ({ ...current, transitions: current.transitions.filter(transition => !removed.includes(transition.id)) }));
    setSelectedTransition(current => removed.includes(current ?? "") ? null : current);
    setDirty(true);
  }, []);
  const connect = (connection: Connection) => { if (!connection.source || !connection.target || connection.source === connection.target) return; const id = `transition-${crypto.randomUUID().slice(0, 8)}`; setFlow(current => ({ ...current, transitions: [...current.transitions, { id, fromState: connection.source!, toState: connection.target!, label: locale === "fa" ? "انتقال" : "Transition", allowedGroups: [], allowedRoles: [], conditionOperator: "AND", conditions: [] }] })); setSelected(null); setSelectedTransition(id); setDirty(true); };
  const updateState = (patch: Partial<FlowStateDraft>) => { setFlow(current => ({ ...current, states: current.states.map(state => state.id === selected ? { ...state, ...patch } : state) })); setDirty(true); };
  const updateTransition = (id: string, patch: Partial<FlowTransitionDraft>) => { setFlow(current => ({ ...current, transitions: current.transitions.map(transition => transition.id === id ? { ...transition, ...patch } : transition) })); setDirty(true); };
  const mutate = async (kind: string, operation: () => Promise<DynamicFlowDefinition>) => { if (pending) return; setPending(kind); setError(null); try { const value = await operation(); setFlow(value); setDirty(false); showToast({ tone: "success", title: kind === "activate" ? (locale === "fa" ? "فرایند فعال شد" : "Process activated") : (locale === "fa" ? "فرایند ذخیره شد" : "Process saved") }); if (!initial) router.replace(`/bpm/${encodeURIComponent(value.flowKey)}`); } catch (reason) { const described = describeApiError(reason, locale === "fa" ? "عملیات ناموفق بود" : "Action failed"); setError(described.message); showToast({ tone: "error", title: described.title, message: described.message }); } finally { setPending(null); } };

  return <section className="bpm-workspace">
    <div className="page-action-bar"><div><StatusBadge tone={flow.active ? "success" : "neutral"}>{flow.lifecycleStatus ?? (flow.active ? "ACTIVE" : "DRAFT")}</StatusBadge><span>v{flow.version ?? 1}</span>{dirty ? <small>{locale === "fa" ? "ذخیره‌نشده" : "Unsaved"}</small> : null}</div><div><AsyncButton pending={pending === "save"} disabled={Boolean(pending) || !flow.flowKey || !flow.name || !flow.states.length} onClick={() => mutate("save", () => saveFlow(flow, scope))}>{locale === "fa" ? "ذخیره" : "Save"}</AsyncButton>{!flow.active && flow.version ? <AsyncButton pending={pending === "activate"} disabled={Boolean(pending)} onClick={() => confirm(locale === "fa" ? "این فرایند فعال شود؟" : "Activate this BPM flow?") && mutate("activate", () => activateFlow(flow.flowKey, flow.version!, scope))}>{locale === "fa" ? "فعال‌سازی" : "Activate"}</AsyncButton> : null}</div></div>
    {error ? <div className="operational-banner error" role="alert"><span>{error}</span><button aria-label={locale === "fa" ? "بستن خطا" : "Dismiss error"} onClick={() => setError(null)}>×</button></div> : null}
    <div className="bpm-designer-grid">
      <aside className="bpm-state-list" aria-label={locale === "fa" ? "فهرست وضعیت‌ها" : "Keyboard state list"}><header><strong>{locale === "fa" ? "وضعیت‌ها" : "States"}</strong><button aria-label={locale === "fa" ? "افزودن وضعیت" : "Add state"} onClick={() => { const state = newState(flow.states.length); setFlow(current => ({ ...current, states: [...current.states, state], startState: current.startState || state.id })); setSelectedTransition(null); setSelected(state.id); setDirty(true); }}>＋</button></header>{flow.states.map(state => <button key={state.id} className={selected === state.id ? "active" : ""} aria-pressed={selected === state.id} onClick={() => { setSelectedTransition(null); setSelected(state.id); }}><span className={state.terminal ? "state-dot terminal" : "state-dot"}/><span><strong>{state.displayName}</strong><small>{state.id}</small></span></button>)}</aside>
      <div className="bpm-canvas">{flow.states.length ? <><ReactFlow nodeTypes={nodeTypes} edgeTypes={edgeTypes} nodes={nodes} edges={edges} onNodesChange={onNodesChange} onEdgesChange={onEdgesChange} onConnect={connect} onNodeClick={(_, node) => { setSelectedTransition(null); setSelected(node.id); }} onEdgeClick={(_, edge) => { setSelected(null); setSelectedTransition(edge.id); }} onPaneClick={() => { setSelected(null); setSelectedTransition(null); }} connectionRadius={45} deleteKeyCode={["Backspace", "Delete"]} fitView><Background/><Controls/><MiniMap/></ReactFlow><p className="bpm-canvas-hint">{locale === "fa" ? "برای حذف، انتخاب کنید و Delete را بزنید." : "Select a node or connection and press Delete to remove it."}</p></> : <EmptyState title={locale === "fa" ? "وضعیتی نیست" : "No states"} description={locale === "fa" ? "اولین وضعیت را اضافه کنید." : "Add the first state to begin designing."}/>}</div>
      <aside className="bpm-inspector">{selectedState ? <StateInspector state={selectedState} flow={flow} locale={locale} scope={scope} update={updateState} close={() => setSelected(null)} remove={() => { setFlow(current => ({ ...current, states: current.states.filter(state => state.id !== selected), transitions: current.transitions.filter(transition => transition.fromState !== selected && transition.toState !== selected), startState: current.startState === selected ? "" : current.startState })); setSelected(null); setDirty(true); }} actions={actionStructures} automations={automations} setStart={() => { setFlow(current => ({ ...current, startState: selectedState.id })); setDirty(true); }}/> : transition ? <TransitionInspector key={transition.id} transition={transition} states={flow.states} metadata={conditionMetadata} locale={locale} update={patch => updateTransition(transition.id, patch)} close={() => setSelectedTransition(null)} remove={() => { setFlow(current => ({ ...current, transitions: current.transitions.filter(item => item.id !== transition.id) })); setSelectedTransition(null); setDirty(true); }}/> : <FlowSettings flow={flow} update={value => { setFlow(value); setDirty(true); }} selectTransition={id => { setSelected(null); setSelectedTransition(id); }} locale={locale}/>}</aside>
    </div>
  </section>;
}

function StateInspector({ state, flow, locale, scope, update, close, remove, actions, automations, setStart }: { state: FlowStateDraft; flow: DynamicFlowDefinition; locale: string; scope: { tenantKey?: string; siteKey?: string }; update: (patch: Partial<FlowStateDraft>) => void; close: () => void; remove: () => void; actions: BpmActionStructure[]; automations: AutomationFlow[]; setStart: () => void }) {
  const access = state.accessRule ?? {};
  const updateAccess = (key: "canRead" | "canEdit" | "canApprove", value: string) => update({ accessRule: { ...access, [key]: splitKeys(value) } });
  return <><header><div><small>{locale === "fa" ? "وضعیت" : "State"}</small><h2>{state.displayName}</h2></div><button onClick={close} aria-label={locale === "fa" ? "بستن بازرس" : "Close inspector"}>×</button></header>
    <label><span>{locale === "fa" ? "نام" : "Display name"}</span><input value={state.displayName} onChange={event => update({ displayName: event.target.value })}/></label>
    <label><span>{locale === "fa" ? "کلید" : "Key"}</span><input dir="ltr" value={state.id} disabled/></label>
    <label className="toggle-row"><input type="radio" checked={flow.startState === state.id} onChange={setStart}/><span>{locale === "fa" ? "وضعیت شروع" : "Start state"}</span></label>
    <label className="toggle-row"><input type="checkbox" checked={state.terminal} onChange={event => update({ terminal: event.target.checked })}/><span>{locale === "fa" ? "پایانی" : "Terminal"}</span></label>
    <label><span>{locale === "fa" ? "گروه‌های نامزد (با ویرگول)" : "Candidate groups (comma-separated)"}</span><input dir="ltr" value={joinKeys(state.candidateGroups)} onChange={event => update({ candidateGroups: splitKeys(event.target.value) })}/></label>
    <fieldset><legend>{locale === "fa" ? "قواعد دسترسی" : "Access rules"}</legend><label><span>{locale === "fa" ? "خواندن" : "Can read"}</span><input dir="ltr" value={joinKeys(access.canRead)} onChange={event => updateAccess("canRead", event.target.value)}/></label><label><span>{locale === "fa" ? "ویرایش" : "Can edit"}</span><input dir="ltr" value={joinKeys(access.canEdit)} onChange={event => updateAccess("canEdit", event.target.value)}/></label><label><span>{locale === "fa" ? "تأیید" : "Can approve"}</span><input dir="ltr" value={joinKeys(access.canApprove)} onChange={event => updateAccess("canApprove", event.target.value)}/></label></fieldset>
    <EntityBindingFields state={state} scope={scope} locale={locale} update={update}/>
    {state.entityService && (state.entityKey || state.formKey) ? <div className="bpm-linked-resources"><small>{locale === "fa" ? "منابع متصل" : "Linked resources"}</small><div><Link className="secondary-pill" href={`/data/${encodeURIComponent(state.entityService)}/${encodeURIComponent(state.entityKey ?? state.formKey ?? "")}`}>{locale === "fa" ? "داده و فرم" : "Data & form"}</Link><Link className="secondary-pill" href={`/forms?serviceKey=${encodeURIComponent(state.entityService)}&entityKey=${encodeURIComponent(state.entityKey ?? state.formKey ?? "")}`}>{locale === "fa" ? "انتشار فرم" : "Publish form"}</Link><Link className="secondary-pill" href={`/definitions/${encodeURIComponent(state.entityService)}/${encodeURIComponent(state.entityKey ?? state.formKey ?? "")}`}>{locale === "fa" ? "تعریف" : "Definition"}</Link></div></div> : null}
    <ProcessorKeyField state={state} locale={locale} update={update}/>
    <label className="toggle-row"><input type="checkbox" checked={Boolean(state.reviewCommentRequired)} onChange={event => update({ reviewCommentRequired: event.target.checked })}/><span>{locale === "fa" ? "نیازمند نظر" : "Review comment required"}</span></label>
    <ActionEditor state={state} update={update} actions={actions} automations={automations} locale={locale} flowKey={flow.flowKey}/><button className="danger-link" onClick={remove}>{locale === "fa" ? "حذف وضعیت" : "Delete state"}</button>
  </>;
}

function useServiceDefinitions(service: string, scope: { tenantKey?: string; siteKey?: string }) {
  const [definitions, setDefinitions] = useState<DynamicEntityDefinition[]>([]);
  const [reloadToken, setReloadToken] = useState(0);
  useEffect(() => {
    if (!service || !scope.tenantKey) { setDefinitions([]); return; }
    let live = true;
    listDefinitions(service as DynamicServiceKey, scope).then(value => { if (live) setDefinitions(value); }).catch(() => { if (live) setDefinitions([]); });
    return () => { live = false; };
  }, [service, scope.tenantKey, scope.siteKey, reloadToken]);
  return { definitions, refresh: () => setReloadToken(value => value + 1) };
}

function DefinitionPicker({ label, service, entityKey, scope, locale, onChangeKey }: { label: string; service: string; entityKey: string; scope: { tenantKey?: string; siteKey?: string }; locale: string; onChangeKey: (key: string) => void }) {
  const { showToast } = useToast();
  const { definitions, refresh } = useServiceDefinitions(service, scope);
  const [createOpen, setCreateOpen] = useState(false);
  const [createKey, setCreateKey] = useState("");
  const [createTitle, setCreateTitle] = useState("");
  const [createPending, setCreatePending] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const submitCreate = async () => {
    if (!scope.tenantKey || !service || !createKey.trim() || createPending) return;
    setCreatePending(true);
    setCreateError(null);
    try {
      await createDefinition(service as DynamicServiceKey, createKey, { entityKey: createKey, title: createTitle || createKey, fields: {} }, scope);
      onChangeKey(createKey);
      refresh();
      setCreateOpen(false);
      setCreateKey("");
      setCreateTitle("");
      showToast({ tone: "success", title: locale === "fa" ? "تعریف ساخته شد" : "Definition created" });
    } catch (cause) {
      const { title, message } = describeApiError(cause, locale === "fa" ? "ساخت تعریف ناموفق بود" : "Definition creation failed");
      setCreateError(message);
      showToast({ tone: "error", title, message });
    } finally {
      setCreatePending(false);
    }
  };
  return <>
    <label><span>{label}</span>
      <div className="bpm-picker-row">
        <select dir="ltr" disabled={!service} value={entityKey} onChange={event => onChangeKey(event.target.value)}>
          <option value="">{locale === "fa" ? "انتخاب کنید" : "Select a definition"}</option>
          {definitions.map(item => <option key={item.entityKey} value={item.entityKey}>{item.title || item.entityKey}</option>)}
          {entityKey && !definitions.some(item => item.entityKey === entityKey) ? <option value={entityKey}>{entityKey} ({locale === "fa" ? "یافت نشد" : "not found"})</option> : null}
        </select>
        <button type="button" className="secondary-pill" disabled={!service} onClick={() => { setCreateKey(""); setCreateTitle(""); setCreateError(null); setCreateOpen(true); }}>{locale === "fa" ? "+ جدید" : "+ New"}</button>
      </div>
    </label>
    <Dialog open={createOpen} title={locale === "fa" ? "تعریف جدید" : "New definition"} onClose={() => !createPending && setCreateOpen(false)}>
      <div className="phase9-form">
        <label><span>{locale === "fa" ? "کلید موجودیت" : "Entity key"}</span><input dir="ltr" value={createKey} disabled={createPending} onChange={event => setCreateKey(event.target.value.toLowerCase().replace(/[^a-z0-9-]/g, "-"))}/></label>
        <label><span>{locale === "fa" ? "عنوان" : "Title"}</span><input value={createTitle} disabled={createPending} onChange={event => setCreateTitle(event.target.value)}/></label>
        {createError ? <p role="alert" className="field-error">{createError}</p> : null}
        <div className="dialog-actions">
          <button className="secondary-pill" disabled={createPending} onClick={() => setCreateOpen(false)}>{locale === "fa" ? "لغو" : "Cancel"}</button>
          <AsyncButton pending={createPending} disabled={!createKey.trim()} onClick={submitCreate}>{locale === "fa" ? "ایجاد" : "Create"}</AsyncButton>
        </div>
      </div>
    </Dialog>
  </>;
}

function fieldKeysOf(definitions: DynamicEntityDefinition[], entityKey: string): string[] | null {
  const match = definitions.find(item => item.entityKey === entityKey);
  if (!match) return null;
  const fields = (match.definition as Record<string, unknown> | undefined)?.fields;
  return fields && typeof fields === "object" ? Object.keys(fields as Record<string, unknown>) : [];
}

function EntityBindingFields({ state, scope, locale, update }: { state: FlowStateDraft; scope: { tenantKey?: string; siteKey?: string }; locale: string; update: (patch: Partial<FlowStateDraft>) => void }) {
  const dynamicServices = useAvailableDynamicServices(scope);
  const service = state.entityService ?? "";
  const key = state.entityKey ?? state.formKey ?? "";
  const [rendererOpen, setRendererOpen] = useState(Boolean(state.rendererService || state.rendererKey));
  const rendererService = state.rendererService ?? "";
  const rendererKey = state.rendererKey ?? "";
  const { definitions: storageDefinitions } = useServiceDefinitions(service, scope);
  const { definitions: rendererDefinitions } = useServiceDefinitions(rendererService, scope);
  const storageFields = fieldKeysOf(storageDefinitions, key);
  const rendererFields = rendererOpen ? fieldKeysOf(rendererDefinitions, rendererKey) : null;
  const mismatchedFields = storageFields && rendererFields ? rendererFields.filter(field => !storageFields.includes(field)) : [];
  return <>
    <label><span>{locale === "fa" ? "حالت ثبت" : "Submit mode"}</span><select dir="ltr" value={state.submitMode ?? "DYNAMIC"} onChange={event => update({ submitMode: event.target.value as "DYNAMIC" | "STATIC" })}>
      <option value="DYNAMIC">{locale === "fa" ? "فرم پویا (از تعریف موجودیت)" : "Dynamic (from entity definition)"}</option>
      <option value="STATIC">{locale === "fa" ? "نشانی ثابت (صفحه سفارشی)" : "Static (custom page URL)"}</option>
    </select></label>
    {state.submitMode === "STATIC" ? (
      <label><span>{locale === "fa" ? "نشانی ثبت" : "Submit URL"}</span><input dir="ltr" placeholder="https://…" value={state.submitUrl ?? ""} onChange={event => update({ submitUrl: event.target.value })}/></label>
    ) : <>
      <label><span>{locale === "fa" ? "سرویس موجودیت" : "Entity service"}</span><select dir="ltr" value={service} onChange={event => update({ entityService: event.target.value, entityKey: "", formKey: "" })}><option value="">{locale === "fa" ? "انتخاب کنید" : "Select a service"}</option>{dynamicServices.map(item => <option key={item} value={item}>{item}</option>)}</select></label>
      <DefinitionPicker label={locale === "fa" ? "کلید تعریف/فرم" : "Definition / form key"} service={service} entityKey={key} scope={scope} locale={locale} onChangeKey={value => update({ entityKey: value, formKey: value })}/>
      <label className="toggle-row"><input type="checkbox" checked={rendererOpen} onChange={event => { setRendererOpen(event.target.checked); if (!event.target.checked) update({ rendererService: undefined, rendererKey: undefined }); }}/><span>{locale === "fa" ? "رندر فرم از تعریف دیگری انجام شود" : "Render the form from a different definition"}</span></label>
      {rendererOpen ? <>
        <label><span>{locale === "fa" ? "سرویس رندر" : "Renderer service"}</span><select dir="ltr" value={rendererService} onChange={event => update({ rendererService: event.target.value || undefined, rendererKey: "" })}><option value="">{locale === "fa" ? "انتخاب کنید" : "Select a service"}</option>{dynamicServices.map(item => <option key={item} value={item}>{item}</option>)}</select></label>
        <DefinitionPicker label={locale === "fa" ? "کلید تعریف رندر" : "Renderer definition key"} service={rendererService} entityKey={rendererKey} scope={scope} locale={locale} onChangeKey={value => update({ rendererKey: value })}/>
        <p className="bpm-field-hint">{locale === "fa" ? "داده در تعریف موجودیت بالا ذخیره می‌شود؛ فرم از این تعریف رندر می‌شود." : "Data is stored against the entity definition above; the form is rendered from this definition instead."}</p>
        {mismatchedFields.length ? <p className="bpm-field-hint bpm-field-warning">{locale === "fa"
          ? `این فیلدها در تعریف رندر وجود دارند اما در تعریف موجودیت نیستند و ثبت آن‌ها با خطای «فیلد غیرمنتظره» رد می‌شود: ${mismatchedFields.join("، ")}. این فیلدها را به «${key}» اضافه کنید یا رندر را به تعریفی سازگار بدهید.`
          : `These fields exist on the renderer definition but not on the storage definition, so submitting them will fail with "unexpected field": ${mismatchedFields.join(", ")}. Add them to "${key}" or point the renderer at a compatible definition.`}</p> : null}
      </> : null}
    </>}
  </>;
}

function ProcessorKeyField({ state, locale, update }: { state: FlowStateDraft; locale: string; update: (patch: Partial<FlowStateDraft>) => void }) {
  const { showToast } = useToast();
  const [processors, setProcessors] = useState<ProcessorDefinition[]>([]);
  const reload = useCallback(() => { let live = true; listProcessors().then(value => { if (live) setProcessors(value); }).catch(() => { if (live) setProcessors([]); }); return () => { live = false; }; }, []);
  useEffect(() => reload(), [reload]);
  const selected = processors.find(item => item.processorKey === state.processorKey);
  const [createOpen, setCreateOpen] = useState(false);
  const [createKey, setCreateKey] = useState("");
  const [createTargetType, setCreateTargetType] = useState("");
  const [createDescription, setCreateDescription] = useState("");
  const [createPending, setCreatePending] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const submitCreate = async () => {
    if (!createKey.trim() || createPending) return;
    setCreatePending(true);
    setCreateError(null);
    try {
      await createProcessor({ processorKey: createKey, targetType: createTargetType || undefined, description: createDescription || undefined, active: true });
      update({ processorKey: createKey });
      setCreateOpen(false);
      setCreateKey("");
      setCreateTargetType("");
      setCreateDescription("");
      showToast({ tone: "success", title: locale === "fa" ? "پردازشگر ساخته شد" : "Processor created" });
      reload();
    } catch (cause) {
      const { title, message } = describeApiError(cause, locale === "fa" ? "ساخت پردازشگر ناموفق بود" : "Processor creation failed");
      setCreateError(message);
      showToast({ tone: "error", title, message });
    } finally {
      setCreatePending(false);
    }
  };
  return <>
    <label><span>{locale === "fa" ? "پردازشگر" : "Processor key"}</span>
      <div className="bpm-picker-row">
        <select dir="ltr" value={state.processorKey ?? ""} onChange={event => update({ processorKey: event.target.value || undefined })}>
          <option value="">{locale === "fa" ? "بدون پردازشگر" : "No processor"}</option>
          {processors.map(item => <option key={item.processorKey} value={item.processorKey}>{item.processorKey}{item.active === false ? ` (${locale === "fa" ? "غیرفعال" : "inactive"})` : ""}</option>)}
        </select>
        <button type="button" className="secondary-pill" onClick={() => { setCreateKey(""); setCreateTargetType(""); setCreateDescription(""); setCreateError(null); setCreateOpen(true); }}>{locale === "fa" ? "+ جدید" : "+ New"}</button>
      </div>
    </label>
    {selected ? <p className="bpm-field-hint">{[selected.targetType, selected.description].filter(Boolean).join(" — ") || (locale === "fa" ? "بدون توضیح" : "No description")}</p> : null}
    {state.processorKey ? <p className="bpm-field-hint">{locale === "fa" ? "شکست پردازشگر مانع ذخیره می‌شود." : "Processor failure blocks persistence."}</p> : null}
    <Dialog open={createOpen} title={locale === "fa" ? "پردازشگر جدید" : "New processor"} onClose={() => !createPending && setCreateOpen(false)}>
      <div className="phase9-form">
        <label><span>{locale === "fa" ? "کلید پردازشگر" : "Processor key"}</span><input dir="ltr" value={createKey} disabled={createPending} onChange={event => setCreateKey(event.target.value.toLowerCase().replace(/[^a-z0-9-]/g, "-"))}/></label>
        <label><span>{locale === "fa" ? "نوع هدف" : "Target type"}</span><input dir="ltr" value={createTargetType} disabled={createPending} onChange={event => setCreateTargetType(event.target.value)}/></label>
        <label><span>{locale === "fa" ? "توضیح" : "Description"}</span><input value={createDescription} disabled={createPending} onChange={event => setCreateDescription(event.target.value)}/></label>
        {createError ? <p role="alert" className="field-error">{createError}</p> : null}
        <div className="dialog-actions">
          <button className="secondary-pill" disabled={createPending} onClick={() => setCreateOpen(false)}>{locale === "fa" ? "لغو" : "Cancel"}</button>
          <AsyncButton pending={createPending} disabled={!createKey.trim()} onClick={submitCreate}>{locale === "fa" ? "ایجاد" : "Create"}</AsyncButton>
        </div>
      </div>
    </Dialog>
  </>;
}

function FlowSettings({ flow, update, selectTransition, locale }: { flow: DynamicFlowDefinition; update: (value: DynamicFlowDefinition) => void; selectTransition: (id: string) => void; locale: string }) {
  const [from, setFrom] = useState(flow.states[0]?.id ?? "");
  const [to, setTo] = useState(flow.states[1]?.id ?? flow.states[0]?.id ?? "");
  const addTransition = () => {
    if (!from || !to || from === to) return;
    const id = `transition-${crypto.randomUUID().slice(0, 8)}`;
    update({ ...flow, transitions: [...flow.transitions, { id, fromState: from, toState: to, label: locale === "fa" ? "انتقال" : "Transition", allowedGroups: [], allowedRoles: [], conditionOperator: "AND", conditions: [] }] });
    selectTransition(id);
  };
  return <><header><div><small>{locale === "fa" ? "تنظیمات" : "Flow settings"}</small><h2>{flow.name || "—"}</h2></div></header>
    <label><span>{locale === "fa" ? "کلید" : "Flow key"}</span><input dir="ltr" disabled={Boolean(flow.id)} value={flow.flowKey} onChange={event => update({ ...flow, flowKey: event.target.value })}/></label>
    <label><span>{locale === "fa" ? "نام" : "Name"}</span><input value={flow.name} onChange={event => update({ ...flow, name: event.target.value })}/></label>
    <label><span>{locale === "fa" ? "توضیح" : "Description"}</span><textarea value={flow.description ?? ""} onChange={event => update({ ...flow, description: event.target.value })}/></label>
    <section className="transition-keyboard-builder"><header><strong>{locale === "fa" ? "افزودن انتقال" : "Add transition"}</strong><small>{locale === "fa" ? "جایگزین صفحه‌کلید برای رسم خط" : "Keyboard alternative to drawing a line"}</small></header><label><span>{locale === "fa" ? "از" : "From"}</span><select value={from} onChange={event => setFrom(event.target.value)}>{flow.states.map(state => <option value={state.id} key={state.id}>{state.displayName}</option>)}</select></label><label><span>{locale === "fa" ? "به" : "To"}</span><select value={to} onChange={event => setTo(event.target.value)}>{flow.states.map(state => <option value={state.id} key={state.id}>{state.displayName}</option>)}</select></label><button className="secondary-pill" disabled={!from || !to || from === to} onClick={addTransition}>＋ {locale === "fa" ? "انتقال" : "Transition"}</button></section>
    <section className="transition-summary"><header><strong>{locale === "fa" ? "انتقال‌ها" : "Transitions"}</strong><span>{flow.transitions.length}</span></header>{flow.transitions.map(item => <button key={item.id} onClick={() => selectTransition(item.id)}><span aria-hidden><ArrowRightIcon size={13}/></span><span><strong>{item.label}</strong><small dir="ltr">{item.fromState} → {item.toState}</small></span><em>{item.conditions?.length ?? 0} {locale === "fa" ? "شرط" : "conditions"}</em></button>)}</section>
    <details><summary>{locale === "fa" ? "JSON پیشرفته" : "Advanced JSON"}</summary><CodeViewer value={flow.transitions}/></details>
  </>;
}

function TransitionInspector({ transition, states, metadata, locale, update, close, remove }: { transition: FlowTransitionDraft; states: FlowStateDraft[]; metadata: BpmConditionStructure | null; locale: string; update: (patch: Partial<FlowTransitionDraft>) => void; close: () => void; remove: () => void }) {
  const conditions = transition.conditions ?? [];
  const operators = metadata?.operators ?? [];
  const [useExpression, setUseExpression] = useState(Boolean(transition.conditionExpression));
  const addCondition = () => update({ conditions: [...conditions, { field: metadata?.supportedFields?.[0] ?? "payload.", operator: operators[0]?.key ?? "EQUALS", value: "" }] });
  const updateCondition = (index: number, patch: Partial<FlowConditionDraft>) => update({ conditions: conditions.map((condition, itemIndex) => itemIndex === index ? { ...condition, ...patch } : condition) });
  return <><header><div className="transition-inspector-heading"><span aria-hidden><ArrowRightIcon size={13}/></span><div><small>{locale === "fa" ? "تصمیم / انتقال" : "Decision transition"}</small><h2>{transition.label}</h2></div></div><button onClick={close} aria-label={locale === "fa" ? "بستن بازرس" : "Close inspector"}>×</button></header>
    <label><span>{locale === "fa" ? "عنوان" : "Label"}</span><input value={transition.label} onChange={event => update({ label: event.target.value })}/></label>
    <div className="transition-route"><label><span>{locale === "fa" ? "از" : "From"}</span><select value={transition.fromState} onChange={event => update({ fromState: event.target.value })}>{states.map(state => <option value={state.id} key={state.id}>{state.displayName}</option>)}</select></label><span aria-hidden>→</span><label><span>{locale === "fa" ? "به" : "To"}</span><select value={transition.toState} onChange={event => update({ toState: event.target.value })}>{states.map(state => <option value={state.id} key={state.id}>{state.displayName}</option>)}</select></label></div>
    <fieldset><legend>{locale === "fa" ? "دسترسی انتقال" : "Transition access"}</legend><label><span>{locale === "fa" ? "نقش‌های مجاز" : "Allowed roles"}</span><input dir="ltr" value={joinKeys(transition.allowedRoles)} onChange={event => update({ allowedRoles: splitKeys(event.target.value) })}/></label><label><span>{locale === "fa" ? "گروه‌های مجاز" : "Allowed groups"}</span><input dir="ltr" value={joinKeys(transition.allowedGroups)} onChange={event => update({ allowedGroups: splitKeys(event.target.value) })}/></label></fieldset>
    <section className="condition-builder"><header><div><strong>{locale === "fa" ? "شرط‌های تصمیم" : "Decision conditions"}</strong><small>{locale === "fa" ? "شرط‌ها روی همین لوزی ارزیابی می‌شوند." : "Rules are evaluated on this diamond."}</small></div>{!useExpression ? <button aria-label={locale === "fa" ? "افزودن شرط" : "Add condition"} onClick={addCondition}>＋</button> : null}</header>
    <label className="toggle-row"><input type="checkbox" checked={useExpression} onChange={event => { setUseExpression(event.target.checked); if (!event.target.checked) update({ conditionExpression: "" }); }}/><span>{locale === "fa" ? "به‌جای شرط‌های ساختاریافته، عبارت خام بنویس" : "Use a raw expression instead of structured conditions"}</span></label>
    {useExpression
      ? <><label><span>{locale === "fa" ? "عبارت شرط" : "Condition expression"}</span><textarea dir="ltr" value={transition.conditionExpression ?? ""} placeholder="payload.currentFormValues.age >= 18 && payload.currentFormValues.verified == true" onChange={event => update({ conditionExpression: event.target.value })}/></label><p className="bpm-field-hint">{locale === "fa" ? "وقتی این عبارت پر باشد، شرط‌های ساختاریافته زیر نادیده گرفته می‌شوند." : "When this is filled in, the structured conditions below are ignored entirely — the transition is decided by this expression alone."}</p></>
      : conditions.length ? <><label><span>{locale === "fa" ? "منطق" : "Match"}</span><select value={transition.conditionOperator ?? "AND"} onChange={event => update({ conditionOperator: event.target.value as "AND" | "OR" })}><option value="AND">{locale === "fa" ? "همه شرط‌ها" : "All conditions (AND)"}</option><option value="OR">{locale === "fa" ? "حداقل یک شرط" : "Any condition (OR)"}</option></select></label>{conditions.map((condition, index) => <article key={`${condition.field}-${index}`}><input aria-label={locale === "fa" ? "مسیر فیلد" : "Field path"} dir="ltr" list="bpm-condition-fields" value={condition.field} onChange={event => updateCondition(index, { field: event.target.value })}/><select aria-label={locale === "fa" ? "عملگر" : "Operator"} value={condition.operator} onChange={event => updateCondition(index, { operator: event.target.value })}>{operators.map(operator => <option value={operator.key} key={operator.key}>{operator.key}</option>)}</select><input aria-label={locale === "fa" ? "مقدار" : "Value"} dir="ltr" value={condition.value == null ? "" : String(condition.value)} onChange={event => updateCondition(index, { value: event.target.value })}/><button aria-label={locale === "fa" ? "حذف شرط" : "Remove condition"} onClick={() => update({ conditions: conditions.filter((_, itemIndex) => itemIndex !== index) })}>×</button></article>)}</> : <p className="muted">{locale === "fa" ? "بدون شرط، انتقال همیشه مجاز است." : "With no conditions, the transition is always eligible."}</p>}
    <datalist id="bpm-condition-fields">{metadata?.supportedFields?.map(field => <option value={field} key={field}/>)}</datalist></section>
    <button className="danger-link" onClick={remove}>{locale === "fa" ? "حذف انتقال" : "Delete transition"}</button>
  </>;
}

function ActionEditor({ state, update, actions, automations, locale, flowKey }: { state: FlowStateDraft; update: (value: Partial<FlowStateDraft>) => void; actions: BpmActionStructure[]; automations: AutomationFlow[]; locale: string; flowKey?: string }) {
  const availableTypes = actions.map(item => item.type).includes("RUN_AUTOMATION_BLOCK") ? actions.map(item => item.type) : [...actions.map(item => item.type), "RUN_AUTOMATION_BLOCK"];
  const [type, setType] = useState(availableTypes[0] ?? "ADD_AUDIT_ENTRY");
  useEffect(() => { if (availableTypes.length && !availableTypes.includes(type)) setType(availableTypes[0]); }, [availableTypes, type]);
  const structureOf = (actionType: string) => actions.find(item => item.type === actionType);
  const add = () => { const params = type === "RUN_AUTOMATION_BLOCK" ? { flowKey: automations[0]?.flowKey ?? "", executionMode: "SYNC" } : {}; update({ onEnterActions: [...(state.onEnterActions ?? []), { type, params }] }); };
  const setParam = (index: number, key: string, value: unknown) => update({ onEnterActions: (state.onEnterActions ?? []).map((item, itemIndex) => itemIndex === index ? { ...item, params: { ...item.params, [key]: value } } : item) });
  return <section className="state-actions"><header><strong>{locale === "fa" ? "کنش‌های ورود" : "On-enter actions"}</strong></header><div><select aria-label={locale === "fa" ? "نوع کنش" : "Action type"} value={type} onChange={event => setType(event.target.value)}>{availableTypes.map(item => <option key={item}>{item}</option>)}</select><button aria-label={locale === "fa" ? "افزودن کنش" : "Add action"} onClick={add}>＋</button></div>{(state.onEnterActions ?? []).map((action, index) => { const structure = structureOf(action.type); const skipKeys = action.type === "RUN_AUTOMATION_BLOCK" ? new Set(["flowKey", "automationFlowKey", "executionMode", "async"]) : new Set<string>(); return <article key={`${action.type}-${index}`} className="bpm-action-card"><div className="bpm-action-card-head"><strong>{action.type}</strong><button aria-label={locale === "fa" ? "حذف کنش" : "Remove action"} onClick={() => update({ onEnterActions: (state.onEnterActions ?? []).filter((_, itemIndex) => itemIndex !== index) })}>×</button></div>{structure?.description ? <p className="bpm-field-hint">{structure.description}</p> : null}{action.type === "RUN_AUTOMATION_BLOCK" ? <><select aria-label={locale === "fa" ? "اتوماسیون منتشرشده" : "Published automation"} value={String(action.params.automationFlowKey ?? action.params.flowKey ?? "")} onChange={event => update({ onEnterActions: (state.onEnterActions ?? []).map((item, itemIndex) => itemIndex === index ? { ...item, params: { ...item.params, flowKey: event.target.value, automationFlowKey: event.target.value } } : item) })}><option value="">{locale === "fa" ? "انتخاب اتوماسیون" : "Select automation"}</option>{automations.map(automation => <option key={`${automation.flowKey}-${automation.version}`} value={automation.flowKey}>{automation.name} · v{automation.version}</option>)}</select><div className="bpm-action-links">{String(action.params.flowKey ?? "") ? <Link href={`/automations/${encodeURIComponent(String(action.params.flowKey))}`}>{locale === "fa" ? "باز کردن اتوماسیون" : "Open automation"}</Link> : null}<Link href={`/automations/new?returnTo=${encodeURIComponent(flowKey ? `/bpm/${flowKey}` : "/bpm")}`}>{locale === "fa" ? "ساخت اتوماسیون" : "Create automation"}</Link></div><label><span>{locale === "fa" ? "حالت اجرا" : "Execution mode"}</span><select value={String(action.params.executionMode ?? "SYNC")} onChange={event => update({ onEnterActions: (state.onEnterActions ?? []).map((item, itemIndex) => itemIndex === index ? { ...item, params: { ...item.params, executionMode: event.target.value, async: event.target.value === "ASYNC" } } : item) })}><option value="SYNC">SYNC</option><option value="ASYNC">ASYNC</option></select></label></> : null}{(structure?.params ?? []).filter(field => !skipKeys.has(field.key)).map(field => <MetadataFieldInput key={field.key} field={field} value={action.params[field.key]} onChange={value => setParam(index, field.key, value)}/>)}</article>; })}</section>;
}

