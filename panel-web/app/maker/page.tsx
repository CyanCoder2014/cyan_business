"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { createDefinitionFromTemplate, dynamicServices, listDefinitions, listTemplates, saveDefinition } from "@/lib/dynamic-api";
import { generatePlatformApp } from "@/lib/platform-api";
import type { DynamicEntityDefinition, DynamicEntityTemplate, DynamicServiceKey, GeneratePlatformAppResponse } from "@/lib/types";

type FieldSummary = {
  name: string;
  type: string;
  required: boolean;
  description?: string;
};

export default function MakerPage() {
  const { locale } = usePanel();
  const [definitions, setDefinitions] = useState<DynamicEntityDefinition[]>([]);
  const [templates, setTemplates] = useState<DynamicEntityTemplate[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [selectedServiceKey, setSelectedServiceKey] = useState<DynamicServiceKey>("bpm-service");
  const [selectedTemplateKey, setSelectedTemplateKey] = useState("screening-intake-form");
  const [definitionDraft, setDefinitionDraft] = useState("");
  const [aiPrompt, setAiPrompt] = useState("Create an intake and review form workflow for applicant screening with automation.");
  const [aiDraft, setAiDraft] = useState<GeneratePlatformAppResponse | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [aiLoading, setAiLoading] = useState(false);

  useEffect(() => {
    setDefinitions([]);
    setTemplates([]);
    setSelectedIndex(0);
    Promise.allSettled([
      listDefinitions(selectedServiceKey, { tenantKey: "tenant-demo", siteKey: "site-commerce" }),
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

  const entities = definitions;
  const selected = entities[selectedIndex] ?? null;
  const fields = useMemo(() => toFieldSummaries(selected), [selected]);

  useEffect(() => {
    setDefinitionDraft(selected?.definitionJson ?? "");
  }, [selected]);

  async function publishSchema() {
    setStatus(locale === "fa" ? "در حال انتشار..." : "Publishing...");
    if (!selected) {
      setStatus(locale === "fa" ? "تعریفی برای انتشار وجود ندارد." : "No definition is available to publish.");
      return;
    }
    try {
      const saved = await saveDefinition(selected.serviceKey as DynamicServiceKey, selected.entityKey, definitionDraft, {
        tenantKey: selected.tenantKey ?? "tenant-demo",
        siteKey: selected.siteKey ?? "site-commerce"
      });
      setDefinitions((current) => current.map((item) => (item.entityKey === saved.entityKey ? saved : item)));
      setDefinitionDraft(saved.definitionJson);
      setStatus(locale === "fa" ? "شِما منتشر شد." : "Schema published.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "انتشار ناموفق بود." : "Publish failed.");
    }
  }

  async function createDefinition() {
    setStatus(locale === "fa" ? "در حال ساخت تعریف..." : "Creating definition...");
    try {
      const created = await createDefinitionFromTemplate(selectedServiceKey, selectedTemplateKey, selectedTemplateKey, {
        tenantKey: "tenant-demo",
        siteKey: "site-commerce"
      });
      setDefinitions((current) => {
        const next = [created, ...current.filter((item) => item.entityKey !== created.entityKey)];
        setSelectedIndex(0);
        return next;
      });
      setDefinitionDraft(created.definitionJson);
      setStatus(locale === "fa" ? "تعریف ایجاد شد." : "Definition created.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ایجاد تعریف ناموفق بود." : "Definition creation failed.");
    }
  }

  async function generateFormDraft() {
    setAiLoading(true);
    setStatus(null);
    try {
      const generated = await generatePlatformApp({
        prompt: aiPrompt,
        tenantKey: "tenant-demo",
        siteKey: "site-commerce",
        execute: false,
        answers: {
          appType: "FORM_FLOW",
          target: "bpm-service",
          locale
        }
      });
      setAiDraft(generated);
      const firstEntity = generated.dsl.entities.find((entity) => typeof entity.serviceKey === "string" && typeof entity.templateKey === "string");
      if (firstEntity?.serviceKey && dynamicServices.includes(firstEntity.serviceKey as DynamicServiceKey)) {
        setSelectedServiceKey(firstEntity.serviceKey as DynamicServiceKey);
        setSelectedTemplateKey(String(firstEntity.templateKey));
      }
      setStatus(locale === "fa" ? "پیش‌نویس فرم با AI تولید شد." : "AI form draft generated.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "تولید فرم ناموفق بود." : "Form generation failed.");
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
      const created = await createDefinitionFromTemplate(serviceKey, String(firstEntity.templateKey), String(firstEntity.entityKey), {
        tenantKey: "tenant-demo",
        siteKey: "site-commerce"
      });
      setSelectedServiceKey(serviceKey);
      setDefinitions((current) => [created, ...current.filter((item) => item.entityKey !== created.entityKey)]);
      setSelectedIndex(0);
      setDefinitionDraft(created.definitionJson);
      setStatus(locale === "fa" ? "تعریف فرم AI ساخته شد." : "AI form definition created.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ساخت فرم AI ناموفق بود." : "AI form creation failed.");
    }
  }

  return (
    <PanelShell
      activeKey="maker"
      title="Maker - Definitions"
      titleFa="سازنده - تعریف موجودیت‌ها"
      subtitle="Design entities, fields, relations, validations, and permissions from the structured runtime contract."
      subtitleFa="موجودیت‌ها، فیلدها، روابط، اعتبارسنجی‌ها و دسترسی‌ها را بر پایه قرارداد ساختاریافته طراحی کنید."
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
                <strong>{locale === "fa" ? "تولید فرم با AI" : "AI form generation"}</strong>
                <span className="muted-block">{locale === "fa" ? "فرم‌های BPM و تعریف‌های Runtime را از درخواست بسازید." : "Generate BPM form definitions and runtime drafts from a prompt."}</span>
              </div>
              <div className="pill-row">
                <button type="button" className="secondary-pill" onClick={generateFormDraft} disabled={aiLoading}>
                  {aiLoading ? (locale === "fa" ? "در حال تولید..." : "Generating...") : locale === "fa" ? "تولید فرم" : "Generate form"}
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
                <span className="pill">{locale === "fa" ? "دسترسی‌ها" : "Permissions"}</span>
              </div>
              <div className="toolbar-row" style={{ marginTop: 14 }}>
                <strong>{selected?.title ?? selected?.entityKey ?? (locale === "fa" ? "بدون انتخاب" : "No selection")}</strong>
                <div className="pill-row">
                  <button type="button" className="secondary-pill">{locale === "fa" ? "مرتب‌سازی" : "Reorder"}</button>
                  <button type="button" className="secondary-pill">{locale === "fa" ? "اقدام گروهی" : "Bulk actions"}</button>
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
            <h3>{locale === "fa" ? "خلاصه API و DSL" : "API & DSL summary"}</h3>
            <span className={selected ? "status-pill success" : "status-pill warning"}>{selected ? (locale === "fa" ? "بارگیری شد" : "Loaded") : locale === "fa" ? "خالی" : "Empty"}</span>
          </div>
          {selected ? (
            <>
              <div className="detail-list" style={{ marginTop: 16 }}>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "کلید موجودیت" : "Entity key"}</strong>
                  <span className="muted-block">{selected.entityKey}</span>
                </div>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "سرویس" : "Service"}</strong>
                  <span className="muted-block">{selected.serviceKey}</span>
                </div>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "دامنه" : "Scope"}</strong>
                  <span className="muted-block">
                    {(selected.tenantKey ?? "tenant-demo")} / {(selected.siteKey ?? "site-commerce")}
                  </span>
                </div>
              </div>
              <pre className="code-block" style={{ marginTop: 16 }}>
{definitionDraft}
              </pre>
            </>
          ) : (
            <div className="mini-card" style={{ marginTop: 16 }}>
              <strong>{locale === "fa" ? "داده‌ای برای خلاصه API وجود ندارد" : "No API summary data available"}</strong>
              <span className="muted-block">{locale === "fa" ? "یک قالب را انتخاب و تعریف را از backend بسازید." : "Select a template and create the definition from backend templates."}</span>
            </div>
          )}
          <div className="card-title-row" style={{ marginTop: 20 }}>
            <h3>{locale === "fa" ? "نقشه شِما" : "Schema map"}</h3>
          </div>
          <div className="summary-grid" style={{ marginTop: 12 }}>
            <div className="mini-card"><strong>{selected?.title ?? "—"}</strong><span className="muted-block">{fields.length} {locale === "fa" ? "فیلد" : "fields"}</span></div>
            <div className="mini-card"><strong>{entities.length}</strong><span className="muted-block">{locale === "fa" ? "تعریف" : "definitions"}</span></div>
          </div>
          {aiDraft ? (
            <>
              <div className="card-title-row" style={{ marginTop: 20 }}>
                <h3>{locale === "fa" ? "خروجی AI" : "AI draft"}</h3>
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
              </div>
            </>
          ) : null}
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <button type="button" className="icon-pill">←</button>
          <div>
            <strong style={{ display: "block", fontSize: "2rem" }}>{locale === "fa" ? "سازنده" : "Maker"}</strong>
            <span className="muted-block">{selected ? `${locale === "fa" ? "موجودیت" : "Entity"}: ${selected.title ?? selected.entityKey}` : locale === "fa" ? "موجودیتی بارگیری نشده" : "No entity loaded"}</span>
          </div>
          <button type="button" className="icon-pill">…</button>
        </div>
        <div className="pill-row">
          <span className="status-pill info">{locale === "fa" ? "فیلدها" : "Fields"}</span>
          <span className="pill">{locale === "fa" ? "اعتبارسنجی" : "Validations"}</span>
          <span className="pill">{locale === "fa" ? "روابط" : "Relations"}</span>
        </div>
        <div className="mobile-card compact">
          <div className="toolbar-row">
            <div>
              <strong>{locale === "fa" ? "دامنه" : "Scope"}</strong>
              <span className="muted-block">{locale === "fa" ? "تعریف محل دسترسی" : "Define where this entity is available."}</span>
            </div>
            <span className="pill">{locale === "fa" ? "سازمانی" : "Organization"}</span>
          </div>
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
                <span className="muted-block">{field.required ? (locale === "fa" ? "اجباری" : "Required") : locale === "fa" ? "اختیاری" : "Optional"}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="mobile-card compact">
          <div className="toolbar-row">
            <strong>{locale === "fa" ? "شناسه API" : "API identifier"}</strong>
            <span>{selected?.entityKey ?? "—"}</span>
          </div>
        </div>
        <button type="button" className="primary-pill auth-submit" onClick={publishSchema} disabled={!selected}>
          {locale === "fa" ? "انتشار شِما" : "Publish schema"}
        </button>
      </div>
    </PanelShell>
  );
}

function toFieldSummaries(definition: DynamicEntityDefinition | null): FieldSummary[] {
  if (!definition) {
    return [];
  }
  try {
    const parsed = JSON.parse(definition.definitionJson) as { fields?: Array<Record<string, unknown>> | Record<string, Record<string, unknown>> };
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
