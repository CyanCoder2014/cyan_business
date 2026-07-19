"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { createDefinitionFromTemplate, dynamicServices, listDefinitions, listTemplates, saveDefinition } from "@/lib/dynamic-api";
import { generatePlatformApp } from "@/lib/platform-api";
import {
  getConditionMetadata,
  listActionMetadata,
  listFlows,
  saveFlow,
  type BpmActionStructure,
  type BpmConditionStructure,
  type DynamicFlowDefinition,
  type FlowActionDraft,
  type FlowStateDraft,
  type FlowTransitionDraft
} from "@/lib/bpm-api";
import type { DynamicEntityDefinition, DynamicEntityTemplate, DynamicServiceKey, GeneratePlatformAppResponse } from "@/lib/types";

type FieldSummary = {
  name: string;
  type: string;
  required: boolean;
  description?: string;
};

const scope = { tenantKey: "tenant-demo", siteKey: "site-commerce" };

export default function MakerPage() {
  const { locale } = usePanel();
  const [definitions, setDefinitions] = useState<DynamicEntityDefinition[]>([]);
  const [templates, setTemplates] = useState<DynamicEntityTemplate[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [selectedServiceKey, setSelectedServiceKey] = useState<DynamicServiceKey>("bpm-service");
  const [selectedTemplateKey, setSelectedTemplateKey] = useState("screening-intake-form");
  const [definitionDraft, setDefinitionDraft] = useState("");
  const [aiPrompt, setAiPrompt] = useState("Create an intake form, BPM review flow, approval automation, and notification actions for applicant screening.");
  const [aiDraft, setAiDraft] = useState<GeneratePlatformAppResponse | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [aiLoading, setAiLoading] = useState(false);

  const [flows, setFlows] = useState<DynamicFlowDefinition[]>([]);
  const [selectedFlowKey, setSelectedFlowKey] = useState<string | null>(null);
  const [flowDraft, setFlowDraft] = useState<DynamicFlowDefinition | null>(null);
  const [selectedStateId, setSelectedStateId] = useState<string | null>(null);
  const [selectedTransitionId, setSelectedTransitionId] = useState<string | null>(null);
  const [actions, setActions] = useState<BpmActionStructure[]>([]);
  const [conditions, setConditions] = useState<BpmConditionStructure | null>(null);
  const [selectedActionType, setSelectedActionType] = useState("");
  const [flowReloadToken, setFlowReloadToken] = useState(0);

  useEffect(() => {
    setDefinitions([]);
    setTemplates([]);
    setSelectedIndex(0);
    Promise.allSettled([
      listDefinitions(selectedServiceKey, scope),
      listTemplates(selectedServiceKey)
    ]).then(([definitionsResult, templatesResult]) => {
      if (definitionsResult.status === "fulfilled") {
        setDefinitions(definitionsResult.value);
      } else {
        setStatus(definitionsResult.reason instanceof Error ? definitionsResult.reason.message : locale === "fa" ? "تعریف‌ها بارگیری نشدند." : "Definitions could not be loaded.");
      }
      if (templatesResult.status === "fulfilled") {
        setTemplates(templatesResult.value);
        setSelectedTemplateKey(templatesResult.value[0]?.templateKey ?? (selectedServiceKey === "bpm-service" ? "screening-intake-form" : "catalog-product"));
      }
    });
  }, [locale, selectedServiceKey]);

  useEffect(() => {
    Promise.allSettled([
      listFlows(scope),
      listActionMetadata(scope),
      getConditionMetadata(scope)
    ]).then(([flowsResult, actionsResult, conditionsResult]) => {
      if (flowsResult.status === "fulfilled") {
        setFlows(flowsResult.value);
        setSelectedFlowKey((current) => current ?? flowsResult.value[0]?.flowKey ?? null);
      } else {
        setStatus((current) => joinStatus(current, locale === "fa" ? "فلوها بارگیری نشدند." : "Flows could not be loaded."));
      }

      if (actionsResult.status === "fulfilled") {
        setActions(actionsResult.value);
        setSelectedActionType(actionsResult.value[0]?.type ?? "");
      } else {
        setStatus((current) => joinStatus(current, locale === "fa" ? "اکشن‌های BPM بارگیری نشدند." : "BPM actions could not be loaded."));
      }

      if (conditionsResult.status === "fulfilled") {
        setConditions(conditionsResult.value);
      } else {
        setStatus((current) => joinStatus(current, locale === "fa" ? "شرط‌های BPM بارگیری نشدند." : "BPM conditions could not be loaded."));
      }
    });
  }, [locale, flowReloadToken]);

  const entities = definitions;
  const selected = entities[selectedIndex] ?? null;
  const fields = useMemo(() => toFieldSummaries(selected), [selected]);
  const selectedDefinitionTitle = selected?.title ?? selected?.entityKey ?? (locale === "fa" ? "بدون انتخاب" : "No selection");

  useEffect(() => {
    setDefinitionDraft(selected ? formatDefinition(selected.definition) : "");
  }, [selected]);

  useEffect(() => {
    const currentFlow = flows.find((item) => item.flowKey === selectedFlowKey) ?? flows[0] ?? null;
    setFlowDraft(currentFlow ? cloneFlow(currentFlow) : createMakerStarterFlow(selected));
  }, [flows, selectedFlowKey, selected]);

  useEffect(() => {
    if (!flowDraft) {
      setSelectedStateId(null);
      return;
    }
    setSelectedStateId((current) => current && flowDraft.states.some((state) => state.id === current) ? current : flowDraft.states[0]?.id ?? null);
  }, [flowDraft]);

  useEffect(() => {
    if (!flowDraft) {
      setSelectedTransitionId(null);
      return;
    }
    setSelectedTransitionId((current) => current && flowDraft.transitions.some((transition) => transition.id === current) ? current : flowDraft.transitions[0]?.id ?? null);
  }, [flowDraft]);

  const selectedFlowState = flowDraft?.states.find((state) => state.id === selectedStateId) ?? null;
  const selectedTransition = flowDraft?.transitions.find((transition) => transition.id === selectedTransitionId) ?? null;
  const linkedStateCount = flowDraft?.states.filter((state) => state.entityKey === selected?.entityKey && state.entityService === selected?.serviceKey).length ?? 0;

  async function publishSchema() {
    setStatus(locale === "fa" ? "در حال انتشار..." : "Publishing...");
    if (!selected) {
      setStatus(locale === "fa" ? "تعریفی برای انتشار وجود ندارد." : "No definition is available to publish.");
      return;
    }
    try {
      const saved = await saveDefinition(selected.serviceKey as DynamicServiceKey, selected.entityKey, definitionDraft, {
        tenantKey: selected.tenantKey ?? scope.tenantKey,
        siteKey: selected.siteKey ?? scope.siteKey
      });
      setDefinitions((current) => current.map((item) => (item.entityKey === saved.entityKey ? saved : item)));
      setDefinitionDraft(formatDefinition(saved.definition));
      setStatus(locale === "fa" ? "شِما منتشر شد." : "Schema published.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "انتشار ناموفق بود." : "Publish failed.");
    }
  }

  async function createDefinition() {
    setStatus(locale === "fa" ? "در حال ساخت تعریف..." : "Creating definition...");
    try {
      const created = await createDefinitionFromTemplate(selectedServiceKey, selectedTemplateKey, selectedTemplateKey, scope);
      setDefinitions((current) => {
        const next = [created, ...current.filter((item) => item.entityKey !== created.entityKey)];
        setSelectedIndex(0);
        return next;
      });
      setDefinitionDraft(formatDefinition(created.definition));
      setStatus(locale === "fa" ? "تعریف ایجاد شد." : "Definition created.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ایجاد تعریف ناموفق بود." : "Definition creation failed.");
    }
  }

  async function generateBuilderDraft() {
    setAiLoading(true);
    setStatus(null);
    try {
      const generated = await generatePlatformApp({
        prompt: aiPrompt,
        tenantKey: scope.tenantKey,
        siteKey: scope.siteKey,
        execute: false,
        answers: {
          appType: "FORM_FLOW",
          target: "bpm-service",
          locale,
          includeAutomation: true
        }
      });
      setAiDraft(generated);
      const firstEntity = generated.dsl.entities.find((entity) => typeof entity.serviceKey === "string" && typeof entity.templateKey === "string");
      if (firstEntity?.serviceKey && dynamicServices.includes(firstEntity.serviceKey as DynamicServiceKey)) {
        setSelectedServiceKey(firstEntity.serviceKey as DynamicServiceKey);
        setSelectedTemplateKey(String(firstEntity.templateKey));
      }
      const generatedFlow = firstGeneratedFlow(generated);
      if (generatedFlow) {
        setFlowDraft(generatedFlow);
        setSelectedFlowKey(generatedFlow.flowKey);
      }
      setStatus(locale === "fa" ? "پیش‌نویس فرم، فلو و اتوماسیون با AI تولید شد." : "AI entity, flow, and automation draft generated.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "تولید پیش‌نویس ناموفق بود." : "Draft generation failed.");
    } finally {
      setAiLoading(false);
    }
  }

  async function createFirstAiDefinition() {
    const firstEntity = aiDraft?.dsl.entities.find((entity) => typeof entity.serviceKey === "string" && typeof entity.templateKey === "string" && typeof entity.entityKey === "string");
    if (!firstEntity) {
      setStatus(locale === "fa" ? "پیش‌نویس AI تعریف قابل ساخت ندارد." : "AI draft has no creatable definition.");
      return;
    }
    const serviceKey = String(firstEntity.serviceKey) as DynamicServiceKey;
    if (!dynamicServices.includes(serviceKey)) {
      setStatus(locale === "fa" ? "سرویس تولیدشده در Runtime موجود نیست." : "Generated service is not available in the dynamic runtime.");
      return;
    }
    setStatus(locale === "fa" ? "در حال ساخت اولین فرم AI..." : "Creating first AI form...");
    try {
      const created = await createDefinitionFromTemplate(serviceKey, String(firstEntity.templateKey), String(firstEntity.entityKey), scope);
      setSelectedServiceKey(serviceKey);
      setDefinitions((current) => [created, ...current.filter((item) => item.entityKey !== created.entityKey)]);
      setSelectedIndex(0);
      setDefinitionDraft(formatDefinition(created.definition));
      setStatus(locale === "fa" ? "تعریف فرم AI ساخته شد." : "AI form definition created.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ساخت فرم AI ناموفق بود." : "AI form creation failed.");
    }
  }

  function syncSelectedDefinitionToState() {
    if (!selected || !selectedFlowState) {
      setStatus(locale === "fa" ? "برای همگام‌سازی، یک definition و یک state انتخاب کنید." : "Select both a definition and a state to sync.");
      return;
    }
    setFlowDraft((current) => {
      if (!current) {
        return current;
      }
      return {
        ...current,
        states: current.states.map((state) => state.id === selectedFlowState.id ? {
          ...state,
          formKey: selected.entityKey,
          entityKey: selected.entityKey,
          entityService: selected.serviceKey,
          rendererKey: selected.entityKey,
          rendererService: selected.serviceKey,
          submitMode: "DYNAMIC"
        } : state)
      };
    });
    setStatus(locale === "fa" ? "state با entity maker همگام شد." : "State synced with the selected maker definition.");
  }

  function updateSelectedState(patch: Partial<FlowStateDraft>) {
    if (!selectedFlowState) {
      return;
    }
    setFlowDraft((current) => {
      if (!current) {
        return current;
      }
      return {
        ...current,
        states: current.states.map((state) => state.id === selectedFlowState.id ? { ...state, ...patch } : state)
      };
    });
  }

  function updateSelectedTransition(patch: Partial<FlowTransitionDraft>) {
    if (!selectedTransition) {
      return;
    }
    setFlowDraft((current) => {
      if (!current) {
        return current;
      }
      return {
        ...current,
        transitions: current.transitions.map((transition) => transition.id === selectedTransition.id ? { ...transition, ...patch } : transition)
      };
    });
  }

  function addActionToSelectedState() {
    if (!selectedFlowState || !selectedActionType) {
      return;
    }
    const descriptor = actions.find((item) => item.type === selectedActionType);
    const nextAction: FlowActionDraft = {
      type: selectedActionType,
      params: {
        source: "maker-sync",
        label: descriptor?.description ?? selectedActionType
      }
    };
    updateSelectedState({
      onEnterActions: [...(selectedFlowState.onEnterActions ?? []), nextAction]
    });
    setStatus(locale === "fa" ? "اکشن به state اضافه شد." : "Action added to the selected state.");
  }

  function removeActionFromSelectedState(index: number) {
    if (!selectedFlowState) {
      return;
    }
    updateSelectedState({
      onEnterActions: (selectedFlowState.onEnterActions ?? []).filter((_, actionIndex) => actionIndex !== index)
    });
  }

  async function saveLinkedFlow() {
    if (!flowDraft) {
      setStatus(locale === "fa" ? "فلویی برای ذخیره وجود ندارد." : "There is no flow to save.");
      return;
    }
    setStatus(locale === "fa" ? "در حال ذخیره فلو..." : "Saving linked flow...");
    try {
      const saved = await saveFlow(flowDraft, scope);
      setFlows((current) => [saved, ...current.filter((item) => item.flowKey !== saved.flowKey)]);
      setSelectedFlowKey(saved.flowKey);
      setFlowReloadToken((current) => current + 1);
      setStatus(locale === "fa" ? "فلو همراه با مپینگ entity ذخیره شد." : "Flow saved with entity mapping.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ذخیره فلو ناموفق بود." : "Flow save failed.");
    }
  }

  return (
    <PanelShell
      activeKey="maker"
      title="Unified Entity, Form & Flow Builder"
      titleFa="سازنده یکپارچه موجودیت، فرم و فلو"
      subtitle="Design runtime entities, bind them to BPM states, and compose automation actions from one build path."
      subtitleFa="موجودیت‌های Runtime را طراحی کنید، آن‌ها را به stateهای BPM وصل کنید و اکشن‌های اتوماسیون را از یک مسیر واحد بسازید."
    >
      <div className="desktop-only maker-page-grid">
        <section className="panel-card maker-main-panel">
          <div className="toolbar-row">
            <input placeholder={locale === "fa" ? "جستجوی موجودیت..." : "Search entities..."} />
            <div className="pill-row">
              <button type="button" className="secondary-pill" onClick={createDefinition}>
                {locale === "fa" ? "ساخت از قالب" : "Create from template"}
              </button>
              <button type="button" className="primary-pill" onClick={publishSchema}>
                {locale === "fa" ? "انتشار شِما" : "Publish schema"}
              </button>
            </div>
          </div>
          {status ? <div className="status-pill info" style={{ marginTop: 14 }}>{status}</div> : null}
          <div className="toolbar-row" style={{ marginTop: 14, gap: 12, flexWrap: "wrap" }}>
            <label style={{ display: "grid", gap: 6, minWidth: 240 }}>
              <span className="muted-block">{locale === "fa" ? "سرویس" : "Service"}</span>
              <select value={selectedServiceKey} onChange={(event) => setSelectedServiceKey(event.target.value as DynamicServiceKey)}>
                {dynamicServices.map((serviceKey) => (
                  <option key={serviceKey} value={serviceKey}>{serviceKey}</option>
                ))}
              </select>
            </label>
            <label style={{ display: "grid", gap: 6, minWidth: 240 }}>
              <span className="muted-block">{locale === "fa" ? "قالب" : "Template"}</span>
              <select value={selectedTemplateKey} onChange={(event) => setSelectedTemplateKey(event.target.value)}>
                {templates.map((template) => (
                  <option key={template.templateKey} value={template.templateKey}>
                    {template.title ?? template.templateKey}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <div className="ai-banner" style={{ marginTop: 14 }}>
            <div className="toolbar-row">
              <div>
                <strong>{locale === "fa" ? "تولید همزمان فرم، فلو و اتوماسیون با AI" : "AI entity, flow, and automation generation"}</strong>
                <span className="muted-block">{locale === "fa" ? "خروجی AI حالا باید definition، state mapping و اکشن‌های اتوماسیون را باهم پشتیبانی کند." : "Generate linked definitions, BPM states, and automation actions in one draft."}</span>
              </div>
              <div className="pill-row">
                <button type="button" className="secondary-pill" onClick={generateBuilderDraft} disabled={aiLoading}>
                  {aiLoading ? (locale === "fa" ? "در حال تولید..." : "Generating...") : locale === "fa" ? "تولید یکپارچه" : "Generate unified draft"}
                </button>
                <button type="button" className="secondary-pill" onClick={createFirstAiDefinition} disabled={!aiDraft}>
                  {locale === "fa" ? "ساخت اولین تعریف" : "Create first definition"}
                </button>
              </div>
            </div>
            <textarea
              value={aiPrompt}
              onChange={(event) => setAiPrompt(event.target.value)}
              style={{ marginTop: 12, minHeight: 84 }}
            />
          </div>

          <div className="two-column-grid maker-layout" style={{ marginTop: 18 }}>
            <div className="entity-list">
              {entities.map((definition, index) => (
                <button
                  type="button"
                  key={definition.entityKey}
                  className={index === selectedIndex ? "entity-item active" : "entity-item"}
                  style={{ textAlign: "start" }}
                  onClick={() => setSelectedIndex(index)}
                >
                  <strong>{definition.title ?? definition.entityKey}</strong>
                  <span className="muted-block">{definition.entityKey}</span>
                  <span className="muted-block">{definition.serviceKey}</span>
                </button>
              ))}
              {!entities.length ? (
                <div className="mini-card">
                  <strong>{locale === "fa" ? "تعریفی از backend دریافت نشد" : "No definitions returned by backend"}</strong>
                  <span className="muted-block">{locale === "fa" ? "پس از ساخت definition در سرویس، این فهرست پر می‌شود." : "This list fills after definitions are created in the service."}</span>
                </div>
              ) : null}
            </div>

            <div className="data-table-shell">
              <div className="tab-row">
                <span className="status-pill info">{locale === "fa" ? "فیلدها" : "Fields"}</span>
                <span className="pill">{locale === "fa" ? "اعتبارسنجی" : "Validations"}</span>
                <span className="pill">{locale === "fa" ? "روابط" : "Relations"}</span>
                <span className="pill">{locale === "fa" ? "اتصال BPM" : "BPM sync"}</span>
              </div>
              <div className="toolbar-row" style={{ marginTop: 14 }}>
                <strong>{selectedDefinitionTitle}</strong>
                <div className="pill-row">
                  <span className="pill">{fields.length} {locale === "fa" ? "فیلد" : "fields"}</span>
                  <span className="pill">{linkedStateCount} {locale === "fa" ? "state متصل" : "linked states"}</span>
                </div>
              </div>
              <table className="data-table schema-table" style={{ marginTop: 14 }}>
                <thead>
                  <tr>
                    <th>{locale === "fa" ? "نام فیلد" : "Field name"}</th>
                    <th>{locale === "fa" ? "نوع" : "Type"}</th>
                    <th>{locale === "fa" ? "اجباری" : "Required"}</th>
                    <th>{locale === "fa" ? "شرح" : "Description"}</th>
                  </tr>
                </thead>
                <tbody>
                  {fields.map((field) => (
                    <tr key={field.name}>
                      <td>{field.name}</td>
                      <td>{field.type}</td>
                      <td>{field.required ? "✓" : "—"}</td>
                      <td>{field.description ?? "—"}</td>
                    </tr>
                  ))}
                  {!fields.length ? (
                    <tr>
                      <td colSpan={4}>{locale === "fa" ? "فیلدی برای این definition موجود نیست." : "No fields are available for this definition."}</td>
                    </tr>
                  ) : null}
                </tbody>
              </table>
              <div style={{ marginTop: 14, display: "grid", gap: 8 }}>
                <span className="muted-block">{locale === "fa" ? "JSON تعریف" : "Definition JSON"}</span>
                <textarea
                  value={definitionDraft}
                  onChange={(event) => setDefinitionDraft(event.target.value)}
                  style={{ minHeight: 220, width: "100%", resize: "vertical" }}
                />
              </div>
            </div>
          </div>
        </section>

        <aside className="panel-card maker-side-panel">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "فلو و اتوماسیون همگام با سازنده" : "Flow and automation synced to maker"}</h3>
            <span className={flowDraft ? "status-pill success" : "status-pill warning"}>{flowDraft ? (locale === "fa" ? "آماده" : "Ready") : locale === "fa" ? "خالی" : "Empty"}</span>
          </div>

          <label style={{ display: "grid", gap: 6, marginTop: 16 }}>
            <span className="muted-block">{locale === "fa" ? "فلو" : "Flow"}</span>
            <select value={selectedFlowKey ?? ""} onChange={(event) => setSelectedFlowKey(event.target.value || null)}>
              {flows.map((flow) => (
                <option key={flow.flowKey} value={flow.flowKey}>
                  {flow.name} ({flow.flowKey})
                </option>
              ))}
              {!flows.length ? <option value="">{locale === "fa" ? "فلو موجود نیست" : "No flow available"}</option> : null}
            </select>
          </label>

          <div className="summary-grid" style={{ marginTop: 12 }}>
            <div className="mini-card"><strong>{flowDraft?.states.length ?? 0}</strong><span className="muted-block">{locale === "fa" ? "state" : "states"}</span></div>
            <div className="mini-card"><strong>{flowDraft?.transitions.length ?? 0}</strong><span className="muted-block">{locale === "fa" ? "ترنزیشن" : "transitions"}</span></div>
          </div>

          <div className="flow-list" style={{ marginTop: 16 }}>
            {(flowDraft?.states ?? []).map((state) => (
              <button
                key={state.id}
                type="button"
                className="flow-item"
                style={selectedStateId === state.id ? { borderColor: "var(--accent)", background: "rgba(11, 92, 255, 0.08)" } : undefined}
                onClick={() => setSelectedStateId(state.id)}
              >
                <strong>{state.displayName}</strong>
                <span className="muted-block">{state.formKey ?? state.entityKey ?? "—"}</span>
                <span className="muted-block">
                  {state.entityKey === selected?.entityKey && state.entityService === selected?.serviceKey
                    ? (locale === "fa" ? "متصل به entity انتخابی" : "Linked to selected entity")
                    : state.waitForAutomation
                      ? (locale === "fa" ? "منتظر اتوماسیون" : "Waits for automation")
                      : "—"}
                </span>
              </button>
            ))}
          </div>

          <div className="card-title-row" style={{ marginTop: 20 }}>
            <h3>{locale === "fa" ? "مپینگ state" : "State mapping"}</h3>
            <button type="button" className="secondary-pill" onClick={syncSelectedDefinitionToState} disabled={!selectedFlowState || !selected}>
              {locale === "fa" ? "همگام‌سازی با entity" : "Sync entity to state"}
            </button>
          </div>
          <div className="detail-list" style={{ marginTop: 12 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "state" : "State"}</strong>
              <span className="muted-block">{selectedFlowState?.displayName ?? "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "فرم / entity" : "Form / entity"}</strong>
              <span className="muted-block">{selectedFlowState?.formKey ?? selectedFlowState?.entityKey ?? "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "submit mode" : "Submit mode"}</strong>
              <span className="muted-block">{selectedFlowState?.submitMode ?? "DYNAMIC"}</span>
            </div>
          </div>
          <div className="form-grid" style={{ marginTop: 12 }}>
            <label style={{ display: "grid", gap: 6 }}>
              <span>{locale === "fa" ? "نام state" : "State name"}</span>
              <input value={selectedFlowState?.displayName ?? ""} onChange={(event) => updateSelectedState({ displayName: event.target.value })} />
            </label>
            <label style={{ display: "grid", gap: 6 }}>
              <span>{locale === "fa" ? "فرم / entity key" : "Form / entity key"}</span>
              <input value={selectedFlowState?.formKey ?? ""} onChange={(event) => updateSelectedState({ formKey: event.target.value, entityKey: event.target.value || undefined })} />
            </label>
            <label style={{ display: "grid", gap: 6 }}>
              <span>{locale === "fa" ? "submit mode" : "Submit mode"}</span>
              <select value={selectedFlowState?.submitMode ?? "DYNAMIC"} onChange={(event) => updateSelectedState({ submitMode: event.target.value === "STATIC" ? "STATIC" : "DYNAMIC" })}>
                <option value="DYNAMIC">DYNAMIC</option>
                <option value="STATIC">STATIC</option>
              </select>
            </label>
            <label style={{ display: "grid", gap: 6 }}>
              <span>{locale === "fa" ? "submit URL" : "Submit URL"}</span>
              <input value={selectedFlowState?.submitUrl ?? ""} onChange={(event) => updateSelectedState({ submitUrl: event.target.value || undefined })} />
            </label>
          </div>

          <div className="card-title-row" style={{ marginTop: 20 }}>
            <h3>{locale === "fa" ? "اکشن‌های اتوماسیون" : "Automation actions"}</h3>
          </div>
          <div className="toolbar-row" style={{ marginTop: 12, gap: 8 }}>
            <select value={selectedActionType} onChange={(event) => setSelectedActionType(event.target.value)} style={{ flex: 1 }}>
              {actions.map((action) => (
                <option key={action.type} value={action.type}>{action.type}</option>
              ))}
            </select>
            <button type="button" className="secondary-pill" onClick={addActionToSelectedState} disabled={!selectedFlowState || !selectedActionType}>
              {locale === "fa" ? "افزودن اکشن" : "Add action"}
            </button>
          </div>
          <div className="flow-list" style={{ marginTop: 12 }}>
            {(selectedFlowState?.onEnterActions ?? []).map((action, index) => (
              <div key={`${action.type}-${index}`} className="flow-item">
                <div className="toolbar-row">
                  <strong>{action.type}</strong>
                  <button type="button" className="icon-pill" onClick={() => removeActionFromSelectedState(index)}>×</button>
                </div>
                <span className="muted-block">{actions.find((item) => item.type === action.type)?.description ?? action.type}</span>
              </div>
            ))}
            {!selectedFlowState?.onEnterActions?.length ? (
              <div className="flow-item">
                <strong>{locale === "fa" ? "اکشنی ثبت نشده است" : "No actions attached"}</strong>
                <span className="muted-block">{locale === "fa" ? "این بخش باید اتوماسیون state انتخابی را از همان مسیر entity maker مدیریت کند." : "Manage selected-state automation from the same maker workflow."}</span>
              </div>
            ) : null}
          </div>

          <div className="card-title-row" style={{ marginTop: 20 }}>
            <h3>{locale === "fa" ? "ترنزیشن و شرط" : "Transition and condition"}</h3>
          </div>
          <label style={{ display: "grid", gap: 6, marginTop: 12 }}>
            <span className="muted-block">{locale === "fa" ? "ترنزیشن" : "Transition"}</span>
            <select value={selectedTransitionId ?? ""} onChange={(event) => setSelectedTransitionId(event.target.value || null)}>
              {(flowDraft?.transitions ?? []).map((transition) => (
                <option key={transition.id} value={transition.id}>{transition.label}</option>
              ))}
            </select>
          </label>
          <div className="detail-list" style={{ marginTop: 12 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "مسیر" : "Path"}</strong>
              <span className="muted-block">{selectedTransition ? `${selectedTransition.fromState} → ${selectedTransition.toState}` : "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "اپراتورها" : "Operators"}</strong>
              <span className="muted-block">{conditions?.operators.map((item) => item.key).slice(0, 6).join(", ") ?? "—"}</span>
            </div>
          </div>
          <label style={{ display: "grid", gap: 6, marginTop: 12 }}>
            <span>{locale === "fa" ? "شرط" : "Condition expression"}</span>
            <input value={selectedTransition?.conditionExpression ?? ""} onChange={(event) => updateSelectedTransition({ conditionExpression: event.target.value || undefined })} />
          </label>

          <div className="pill-row" style={{ marginTop: 20 }}>
            <button type="button" className="primary-pill" onClick={saveLinkedFlow} disabled={!flowDraft}>
              {locale === "fa" ? "ذخیره فلو همگام" : "Save synced flow"}
            </button>
          </div>
          {aiDraft ? (
            <>
              <div className="card-title-row" style={{ marginTop: 20 }}>
                <h3>{locale === "fa" ? "خلاصه خروجی AI" : "AI draft summary"}</h3>
                <span className="status-pill info">{aiDraft.dsl.app.type ?? "FORM_FLOW"}</span>
              </div>
              <div className="detail-list" style={{ marginTop: 12 }}>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "تعریف‌ها" : "Definitions"}</strong>
                  <span className="muted-block">{aiDraft.dsl.entities.map((entity) => `${String(entity.serviceKey ?? "")}:${String(entity.templateKey ?? "")}`).join(", ") || "—"}</span>
                </div>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "فلوها" : "Flows"}</strong>
                  <span className="muted-block">{aiDraft.dsl.flows.map((flow) => String(flow.flowKey ?? "flow")).join(", ") || "—"}</span>
                </div>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "اکشن‌های دستی" : "Manual actions"}</strong>
                  <span className="muted-block">{aiDraft.dsl.manualActions.join(", ") || "—"}</span>
                </div>
              </div>
            </>
          ) : null}
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <button type="button" className="icon-pill">←</button>
          <div>
            <strong style={{ display: "block", fontSize: "2rem" }}>{locale === "fa" ? "سازنده یکپارچه" : "Unified Maker"}</strong>
            <span className="muted-block">{selectedDefinitionTitle}</span>
          </div>
          <button type="button" className="icon-pill">…</button>
        </div>
        <div className="pill-row">
          <span className="status-pill info">{locale === "fa" ? "فیلدها" : "Fields"}</span>
          <span className="pill">{locale === "fa" ? "فلو" : "Flow"}</span>
          <span className="pill">{locale === "fa" ? "اتوماسیون" : "Automation"}</span>
        </div>
        <div className="mobile-card">
          <div className="mobile-list">
            {fields.map((field) => (
              <div key={field.name} className="mobile-list-item">
                <div className="toolbar-row">
                  <strong>{field.name}</strong>
                  <span className="status-pill info">{field.type}</span>
                </div>
                <span className="muted-block">{field.description ?? field.name}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="mobile-card compact">
          <div className="toolbar-row">
            <strong>{locale === "fa" ? "فلو متصل" : "Linked flow"}</strong>
            <span>{flowDraft?.flowKey ?? "—"}</span>
          </div>
          <div className="toolbar-row" style={{ marginTop: 10 }}>
            <strong>{locale === "fa" ? "state انتخابی" : "Selected state"}</strong>
            <span>{selectedFlowState?.displayName ?? "—"}</span>
          </div>
        </div>
        <button type="button" className="primary-pill auth-submit" onClick={saveLinkedFlow} disabled={!flowDraft}>
          {locale === "fa" ? "ذخیره فلو و فرم" : "Save flow and form"}
        </button>
      </div>
    </PanelShell>
  );
}

function joinStatus(current: string | null, next: string) {
  return current ? `${current} ${next}` : next;
}

function cloneFlow(flow: DynamicFlowDefinition): DynamicFlowDefinition {
  return JSON.parse(JSON.stringify(flow)) as DynamicFlowDefinition;
}

function createMakerStarterFlow(definition: DynamicEntityDefinition | null): DynamicFlowDefinition {
  const entityKey = definition?.entityKey ?? "screening-intake-form";
  const serviceKey = definition?.serviceKey ?? "bpm-service";
  const title = definition?.title ?? "Maker Sync Flow";
  return {
    flowKey: `${entityKey.replace(/[^a-zA-Z0-9]+/g, "_").toLowerCase()}_flow`,
    name: `${title} Flow`,
    startState: "draft",
    states: [
      {
        id: "draft",
        displayName: "Draft",
        terminal: false,
        formKey: entityKey,
        entityKey,
        entityService: serviceKey,
        rendererKey: entityKey,
        rendererService: serviceKey,
        submitMode: "DYNAMIC"
      },
      {
        id: "review",
        displayName: "Review",
        terminal: false,
        formKey: entityKey,
        entityKey,
        entityService: serviceKey,
        rendererKey: entityKey,
        rendererService: serviceKey,
        submitMode: "DYNAMIC",
        onEnterActions: [{ type: "NOTIFY", params: { source: "maker" } }]
      },
      {
        id: "completed",
        displayName: "Completed",
        terminal: true
      }
    ],
    transitions: [
      { id: "submit", fromState: "draft", toState: "review", label: "Submit" },
      { id: "approve", fromState: "review", toState: "completed", label: "Approve" }
    ]
  };
}

function toFieldSummaries(definition: DynamicEntityDefinition | null): FieldSummary[] {
  if (!definition) {
    return [];
  }
  try {
    const parsed = definition.definition as {
      fields?: Array<Record<string, unknown>> | Record<string, Record<string, unknown>>;
    };
    const fields: Array<Record<string, unknown>> = Array.isArray(parsed.fields)
      ? parsed.fields
      : Object.entries(parsed.fields ?? {}).map(([key, value]) => ({ key, ...value }));
    if (fields.length) {
      return fields.map((field) => ({
        name: String(field.key ?? field.name ?? field.id ?? "field"),
        type: String(field.type ?? "String"),
        required: Boolean(field.required) || Array.isArray(field.validations) && field.validations.some((rule) => typeof rule === "object" && rule !== null && "validation" in rule && rule.validation === "REQUIRED"),
        description: typeof field.label === "string" ? field.label : undefined
      }));
    }
  } catch {
    return [];
  }
  return [];
}

function formatDefinition(definition: Record<string, unknown>): string {
  return JSON.stringify(definition, null, 2);
}

function firstGeneratedFlow(generated: GeneratePlatformAppResponse): DynamicFlowDefinition | null {
  const flowBlueprint = generated.dsl.flows.find((item) => item && typeof item === "object");
  const rawDefinition = flowBlueprint?.flowDefinition;
  if (!rawDefinition || typeof rawDefinition !== "object" || Array.isArray(rawDefinition)) {
    return null;
  }
  const definition = rawDefinition as Record<string, unknown>;
  const states = Array.isArray(definition.states) ? definition.states : [];
  const transitions = Array.isArray(definition.transitions) ? definition.transitions : [];
  return {
    flowKey: String(definition.flowKey ?? flowBlueprint.flowKey ?? "ai-generated-flow"),
    version: typeof definition.version === "number" ? definition.version : 1,
    name: String(definition.name ?? flowBlueprint.flowKey ?? "AI Generated Flow"),
    description: typeof definition.description === "string" ? definition.description : undefined,
    startState: String(definition.startState ?? (states[0] as Record<string, unknown> | undefined)?.id ?? "start"),
    active: Boolean(definition.active),
    states: states.map((state) => normalizeState(state)),
    transitions: transitions.map((transition) => normalizeTransition(transition))
  };
}

function normalizeState(value: unknown): FlowStateDraft {
  const state = value && typeof value === "object" ? value as Record<string, unknown> : {};
  return {
    id: String(state.id ?? "state"),
    displayName: String(state.displayName ?? state.id ?? "State"),
    terminal: Boolean(state.terminal),
    formKey: stringOrUndefined(state.formKey),
    processorKey: stringOrUndefined(state.processorKey),
    candidateGroups: Array.isArray(state.candidateGroups) ? state.candidateGroups.map(String) : undefined,
    onEnterActions: Array.isArray(state.onEnterActions) ? state.onEnterActions as FlowActionDraft[] : undefined,
    entityService: stringOrUndefined(state.entityService),
    entityKey: stringOrUndefined(state.entityKey),
    rendererService: stringOrUndefined(state.rendererService),
    rendererKey: stringOrUndefined(state.rendererKey),
    submitMode: state.submitMode === "STATIC" ? "STATIC" : "DYNAMIC",
    submitUrl: stringOrUndefined(state.submitUrl),
    waitForAutomation: Boolean(state.waitForAutomation)
  };
}

function normalizeTransition(value: unknown): FlowTransitionDraft {
  const transition = value && typeof value === "object" ? value as Record<string, unknown> : {};
  return {
    id: String(transition.id ?? `${String(transition.fromState ?? "from")}-${String(transition.toState ?? "to")}`),
    fromState: String(transition.fromState ?? "from"),
    toState: String(transition.toState ?? "to"),
    label: String(transition.label ?? transition.id ?? "Transition"),
    allowedGroups: Array.isArray(transition.allowedGroups) ? transition.allowedGroups.map(String) : undefined,
    allowedRoles: Array.isArray(transition.allowedRoles) ? transition.allowedRoles.map(String) : undefined,
    conditionExpression: stringOrUndefined(transition.conditionExpression)
  };
}

function stringOrUndefined(value: unknown): string | undefined {
  return typeof value === "string" && value.length > 0 ? value : undefined;
}
