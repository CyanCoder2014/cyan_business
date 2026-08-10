"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { Background, Controls, MiniMap, ReactFlow, applyNodeChanges, type Connection, type Node, type NodeChange } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { AsyncButton, CodeViewer, EmptyState, StatusBadge } from "@/components/ui/primitives";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { activateFlow, listActionMetadata, saveFlow, type DynamicFlowDefinition, type FlowStateDraft, type FlowTransitionDraft } from "@/lib/bpm-api";
import { listAutomationFlows, type AutomationFlow } from "@/lib/automation-api";
import { useRouter } from "next/navigation";

const blank = (key = ""): DynamicFlowDefinition => ({ flowKey: key, name: "", description: "", startState: "", states: [], transitions: [], active: false, lifecycleStatus: "DRAFT", layout: {} });
const newState = (index: number): FlowStateDraft => ({ id: `state-${crypto.randomUUID().slice(0, 8)}`, displayName: `State ${index + 1}`, terminal: false, candidateGroups: [], onEnterActions: [], submitMode: "DYNAMIC", accessRule: { canRead: [], canEdit: [], canApprove: [] } });
const splitKeys = (value: string) => value.split(",").map(item => item.trim()).filter(Boolean);
const joinKeys = (value?: string[]) => (value ?? []).join(", ");

export function BpmDesigner({ initial }: { initial?: DynamicFlowDefinition }) {
  const { locale } = usePanel();
  const { tenantKey, siteKey } = useScopeAccess();
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }), [tenantKey, siteKey]);
  const router = useRouter();
  const [flow, setFlow] = useState(initial ?? blank());
  const [selected, setSelected] = useState<string | null>(null);
  const [pending, setPending] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [dirty, setDirty] = useState(!initial);
  const [actionTypes, setActionTypes] = useState<string[]>([]);
  const [automations, setAutomations] = useState<AutomationFlow[]>([]);

  useEffect(() => { Promise.all([listActionMetadata(scope), listAutomationFlows()]).then(([actions, definitions]) => { setActionTypes(actions.map(item => item.type)); setAutomations(definitions.filter(item => item.active)); }).catch(reason => setError(reason instanceof Error ? reason.message : String(reason))); }, [scope]);
  useEffect(() => { const guard = (event: BeforeUnloadEvent) => { if (dirty) event.preventDefault(); }; window.addEventListener("beforeunload", guard); return () => window.removeEventListener("beforeunload", guard); }, [dirty]);

  const nodes = useMemo<Node[]>(() => flow.states.map((state, index) => ({
    id: state.id,
    position: { x: Number(flow.layout?.[state.id]?.x ?? 120 + (index % 3) * 220), y: Number(flow.layout?.[state.id]?.y ?? 100 + Math.floor(index / 3) * 140) },
    data: { label: state.displayName, terminal: state.terminal, start: state.id === flow.startState },
    className: `bpm-state-node ${state.terminal ? "terminal" : ""} ${state.id === flow.startState ? "start" : ""} ${selected === state.id ? "selected" : ""}`
  })), [flow.layout, flow.startState, flow.states, selected]);
  const edges = useMemo(() => flow.transitions.map(transition => ({ id: transition.id, source: transition.fromState, target: transition.toState, label: transition.label, animated: flow.active })), [flow.active, flow.transitions]);
  const selectedState = flow.states.find(state => state.id === selected);
  const onNodesChange = useCallback((changes: NodeChange[]) => { const next = applyNodeChanges(changes, nodes); setFlow(current => ({ ...current, layout: Object.fromEntries(next.map(node => [node.id, node.position])) })); if (changes.some(change => change.type === "position")) setDirty(true); }, [nodes]);
  const connect = (connection: Connection) => { if (!connection.source || !connection.target) return; setFlow(current => ({ ...current, transitions: [...current.transitions, { id: `transition-${crypto.randomUUID().slice(0, 8)}`, fromState: connection.source!, toState: connection.target!, label: locale === "fa" ? "انتقال" : "Transition", allowedGroups: [], allowedRoles: [], conditions: [] }] })); setDirty(true); };
  const updateState = (patch: Partial<FlowStateDraft>) => { setFlow(current => ({ ...current, states: current.states.map(state => state.id === selected ? { ...state, ...patch } : state) })); setDirty(true); };
  const updateTransition = (id: string, patch: Partial<FlowTransitionDraft>) => { setFlow(current => ({ ...current, transitions: current.transitions.map(transition => transition.id === id ? { ...transition, ...patch } : transition) })); setDirty(true); };
  const mutate = async (kind: string, operation: () => Promise<DynamicFlowDefinition>) => { if (pending) return; setPending(kind); setError(null); try { const value = await operation(); setFlow(value); setDirty(false); if (!initial) router.replace(`/bpm/${encodeURIComponent(value.flowKey)}`); } catch (reason) { setError(reason instanceof Error ? reason.message : String(reason)); } finally { setPending(null); } };

  return <section className="bpm-workspace">
    <div className="page-action-bar"><div><StatusBadge tone={flow.active ? "success" : "neutral"}>{flow.lifecycleStatus ?? (flow.active ? "ACTIVE" : "DRAFT")}</StatusBadge><span>v{flow.version ?? 1}</span>{dirty ? <small>{locale === "fa" ? "ذخیره‌نشده" : "Unsaved"}</small> : null}</div><div><AsyncButton pending={pending === "save"} disabled={Boolean(pending) || !flow.flowKey || !flow.name || !flow.states.length} onClick={() => mutate("save", () => saveFlow(flow, scope))}>{locale === "fa" ? "ذخیره" : "Save"}</AsyncButton>{!flow.active && flow.version ? <AsyncButton pending={pending === "activate"} disabled={Boolean(pending)} onClick={() => confirm(locale === "fa" ? "این فرایند فعال شود؟" : "Activate this BPM flow?") && mutate("activate", () => activateFlow(flow.flowKey, flow.version!, scope))}>{locale === "fa" ? "فعال‌سازی" : "Activate"}</AsyncButton> : null}</div></div>
    {error ? <div className="operational-banner error" role="alert"><span>{error}</span><button aria-label={locale === "fa" ? "بستن خطا" : "Dismiss error"} onClick={() => setError(null)}>×</button></div> : null}
    <div className="bpm-designer-grid">
      <aside className="bpm-state-list" aria-label={locale === "fa" ? "فهرست وضعیت‌ها" : "Keyboard state list"}><header><strong>{locale === "fa" ? "وضعیت‌ها" : "States"}</strong><button aria-label={locale === "fa" ? "افزودن وضعیت" : "Add state"} onClick={() => { const state = newState(flow.states.length); setFlow(current => ({ ...current, states: [...current.states, state], startState: current.startState || state.id })); setSelected(state.id); setDirty(true); }}>＋</button></header>{flow.states.map(state => <button key={state.id} className={selected === state.id ? "active" : ""} aria-pressed={selected === state.id} onClick={() => setSelected(state.id)}><span className={state.terminal ? "state-dot terminal" : "state-dot"}/><span><strong>{state.displayName}</strong><small>{state.id}</small></span></button>)}</aside>
      <div className="bpm-canvas">{flow.states.length ? <ReactFlow nodes={nodes} edges={edges} onNodesChange={onNodesChange} onConnect={connect} onNodeClick={(_, node) => setSelected(node.id)} fitView><Background/><Controls/><MiniMap/></ReactFlow> : <EmptyState title={locale === "fa" ? "وضعیتی نیست" : "No states"} description={locale === "fa" ? "اولین وضعیت را اضافه کنید." : "Add the first state to begin designing."}/>}</div>
      <aside className="bpm-inspector">{selectedState ? <StateInspector state={selectedState} flow={flow} locale={locale} update={updateState} close={() => setSelected(null)} remove={() => { setFlow(current => ({ ...current, states: current.states.filter(state => state.id !== selected), transitions: current.transitions.filter(transition => transition.fromState !== selected && transition.toState !== selected), startState: current.startState === selected ? "" : current.startState })); setSelected(null); setDirty(true); }} actionTypes={actionTypes} automations={automations} setStart={() => { setFlow(current => ({ ...current, startState: selectedState.id })); setDirty(true); }}/> : <FlowSettings flow={flow} update={value => { setFlow(value); setDirty(true); }} updateTransition={updateTransition} locale={locale}/>}</aside>
    </div>
  </section>;
}

function StateInspector({ state, flow, locale, update, close, remove, actionTypes, automations, setStart }: { state: FlowStateDraft; flow: DynamicFlowDefinition; locale: string; update: (patch: Partial<FlowStateDraft>) => void; close: () => void; remove: () => void; actionTypes: string[]; automations: AutomationFlow[]; setStart: () => void }) {
  const access = state.accessRule ?? {};
  const updateAccess = (key: "canRead" | "canEdit" | "canApprove", value: string) => update({ accessRule: { ...access, [key]: splitKeys(value) } });
  return <><header><div><small>{locale === "fa" ? "وضعیت" : "State"}</small><h2>{state.displayName}</h2></div><button onClick={close} aria-label={locale === "fa" ? "بستن بازرس" : "Close inspector"}>×</button></header>
    <label><span>{locale === "fa" ? "نام" : "Display name"}</span><input value={state.displayName} onChange={event => update({ displayName: event.target.value })}/></label>
    <label><span>{locale === "fa" ? "کلید" : "Key"}</span><input dir="ltr" value={state.id} disabled/></label>
    <label className="toggle-row"><input type="radio" checked={flow.startState === state.id} onChange={setStart}/><span>{locale === "fa" ? "وضعیت شروع" : "Start state"}</span></label>
    <label className="toggle-row"><input type="checkbox" checked={state.terminal} onChange={event => update({ terminal: event.target.checked })}/><span>{locale === "fa" ? "پایانی" : "Terminal"}</span></label>
    <label><span>{locale === "fa" ? "گروه‌های نامزد (با ویرگول)" : "Candidate groups (comma-separated)"}</span><input dir="ltr" value={joinKeys(state.candidateGroups)} onChange={event => update({ candidateGroups: splitKeys(event.target.value) })}/></label>
    <fieldset><legend>{locale === "fa" ? "قواعد دسترسی" : "Access rules"}</legend><label><span>{locale === "fa" ? "خواندن" : "Can read"}</span><input dir="ltr" value={joinKeys(access.canRead)} onChange={event => updateAccess("canRead", event.target.value)}/></label><label><span>{locale === "fa" ? "ویرایش" : "Can edit"}</span><input dir="ltr" value={joinKeys(access.canEdit)} onChange={event => updateAccess("canEdit", event.target.value)}/></label><label><span>{locale === "fa" ? "تأیید" : "Can approve"}</span><input dir="ltr" value={joinKeys(access.canApprove)} onChange={event => updateAccess("canApprove", event.target.value)}/></label></fieldset>
    <label><span>{locale === "fa" ? "سرویس موجودیت" : "Entity service"}</span><input dir="ltr" value={state.entityService ?? ""} onChange={event => update({ entityService: event.target.value })}/></label>
    <label><span>{locale === "fa" ? "کلید تعریف/فرم" : "Definition / form key"}</span><input dir="ltr" value={state.entityKey ?? state.formKey ?? ""} onChange={event => update({ entityKey: event.target.value, formKey: event.target.value })}/></label>
    <label><span>{locale === "fa" ? "پردازشگر" : "Processor key"}</span><input dir="ltr" value={state.processorKey ?? ""} onChange={event => update({ processorKey: event.target.value })}/></label>
    <label className="toggle-row"><input type="checkbox" checked={Boolean(state.reviewCommentRequired)} onChange={event => update({ reviewCommentRequired: event.target.checked })}/><span>{locale === "fa" ? "نیازمند نظر" : "Review comment required"}</span></label>
    <ActionEditor state={state} update={update} types={actionTypes} automations={automations} locale={locale}/><button className="danger-link" onClick={remove}>{locale === "fa" ? "حذف وضعیت" : "Delete state"}</button>
  </>;
}

function FlowSettings({ flow, update, updateTransition, locale }: { flow: DynamicFlowDefinition; update: (value: DynamicFlowDefinition) => void; updateTransition: (id: string, patch: Partial<FlowTransitionDraft>) => void; locale: string }) {
  return <><header><div><small>{locale === "fa" ? "تنظیمات" : "Flow settings"}</small><h2>{flow.name || "—"}</h2></div></header><label><span>{locale === "fa" ? "کلید" : "Flow key"}</span><input dir="ltr" disabled={Boolean(flow.id)} value={flow.flowKey} onChange={event => update({ ...flow, flowKey: event.target.value })}/></label><label><span>{locale === "fa" ? "نام" : "Name"}</span><input value={flow.name} onChange={event => update({ ...flow, name: event.target.value })}/></label><label><span>{locale === "fa" ? "توضیح" : "Description"}</span><textarea value={flow.description ?? ""} onChange={event => update({ ...flow, description: event.target.value })}/></label>
    <section className="state-actions"><header><strong>{locale === "fa" ? "انتقال‌ها" : "Transitions"}</strong></header>{flow.transitions.map(transition => <article key={transition.id}><label><span>{locale === "fa" ? "عنوان" : "Label"}</span><input value={transition.label} onChange={event => updateTransition(transition.id, { label: event.target.value })}/></label><small dir="ltr">{transition.fromState} → {transition.toState}</small><label><span>{locale === "fa" ? "نقش‌های مجاز" : "Allowed roles"}</span><input dir="ltr" value={joinKeys(transition.allowedRoles)} onChange={event => updateTransition(transition.id, { allowedRoles: splitKeys(event.target.value) })}/></label><label><span>{locale === "fa" ? "گروه‌های مجاز" : "Allowed groups"}</span><input dir="ltr" value={joinKeys(transition.allowedGroups)} onChange={event => updateTransition(transition.id, { allowedGroups: splitKeys(event.target.value) })}/></label><button aria-label={locale === "fa" ? "حذف انتقال" : "Remove transition"} onClick={() => update({ ...flow, transitions: flow.transitions.filter(item => item.id !== transition.id) })}>×</button></article>)}</section>
    <details><summary>{locale === "fa" ? "JSON فرایند" : "Flow JSON"}</summary><CodeViewer value={flow.transitions}/></details>
  </>;
}

function ActionEditor({ state, update, types, automations, locale }: { state: FlowStateDraft; update: (value: Partial<FlowStateDraft>) => void; types: string[]; automations: AutomationFlow[]; locale: string }) {
  const [type, setType] = useState(types[0] ?? "ADD_AUDIT_ENTRY");
  useEffect(() => { if (types.length && !types.includes(type)) setType(types[0]); }, [type, types]);
  const add = () => { const params = type === "RUN_AUTOMATION_BLOCK" ? { flowKey: automations[0]?.flowKey ?? "", executionMode: "SYNC", inputMappings: {}, outputMappings: {} } : {}; update({ onEnterActions: [...(state.onEnterActions ?? []), { type, params }] }); };
  return <section className="state-actions"><header><strong>{locale === "fa" ? "کنش‌های ورود" : "On-enter actions"}</strong></header><div><select aria-label={locale === "fa" ? "نوع کنش" : "Action type"} value={type} onChange={event => setType(event.target.value)}>{types.map(item => <option key={item}>{item}</option>)}</select><button aria-label={locale === "fa" ? "افزودن کنش" : "Add action"} onClick={add}>＋</button></div>{(state.onEnterActions ?? []).map((action, index) => <article key={`${action.type}-${index}`}><strong>{action.type}</strong>{action.type === "RUN_AUTOMATION_BLOCK" ? <select aria-label={locale === "fa" ? "اتوماسیون منتشرشده" : "Published automation"} value={String(action.params.flowKey ?? "")} onChange={event => update({ onEnterActions: (state.onEnterActions ?? []).map((item, itemIndex) => itemIndex === index ? { ...item, params: { ...item.params, flowKey: event.target.value } } : item) })}><option value="">{locale === "fa" ? "انتخاب اتوماسیون" : "Select automation"}</option>{automations.map(automation => <option key={`${automation.flowKey}-${automation.version}`} value={automation.flowKey}>{automation.name}</option>)}</select> : null}<button aria-label={locale === "fa" ? "حذف کنش" : "Remove action"} onClick={() => update({ onEnterActions: (state.onEnterActions ?? []).filter((_, itemIndex) => itemIndex !== index) })}>×</button></article>)}</section>;
}
