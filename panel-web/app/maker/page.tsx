"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { listDefinitions, saveDefinition } from "@/lib/dynamic-api";
import type { DynamicEntityDefinition } from "@/lib/types";

type FieldSummary = {
  name: string;
  type: string;
  required: boolean;
  description?: string;
};

export default function MakerPage() {
  const { locale } = usePanel();
  const [definitions, setDefinitions] = useState<DynamicEntityDefinition[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    listDefinitions("catalog-service", { tenantKey: "tenant-demo", siteKey: "site-commerce" })
      .then(setDefinitions)
      .catch((error) => setStatus(error instanceof Error ? error.message : locale === "fa" ? "تعریف‌ها بارگیری نشدند." : "Definitions could not be loaded."));
  }, [locale]);

  const entities = definitions;
  const selected = entities[selectedIndex] ?? null;
  const fields = useMemo(() => toFieldSummaries(selected), [selected]);

  async function publishSchema() {
    setStatus(locale === "fa" ? "در حال انتشار..." : "Publishing...");
    if (!selected) {
      setStatus(locale === "fa" ? "تعریفی برای انتشار وجود ندارد." : "No definition is available to publish.");
      return;
    }
    try {
      const saved = await saveDefinition("catalog-service", selected.entityKey, selected.definitionJson, {
        tenantKey: selected.tenantKey ?? "tenant-demo",
        siteKey: selected.siteKey ?? "site-commerce"
      });
      setDefinitions((current) => current.map((item) => (item.entityKey === saved.entityKey ? saved : item)));
      setStatus(locale === "fa" ? "شِما منتشر شد." : "Schema published.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "انتشار ناموفق بود." : "Publish failed.");
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
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "افزودن فیلد" : "Add field"}
              </button>
              <button type="button" className="primary-pill" onClick={publishSchema}>
                {locale === "fa" ? "انتشار شِما" : "Publish schema"}
              </button>
            </div>
          </div>
          {status ? <div className="status-pill info" style={{ marginTop: 14 }}>{status}</div> : null}

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
              <button type="button" className="secondary-pill" style={{ marginTop: 14 }}>
                {locale === "fa" ? "افزودن فیلد" : "Add field"}
              </button>
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
{selected.definitionJson}
              </pre>
            </>
          ) : (
            <div className="mini-card" style={{ marginTop: 16 }}>
              <strong>{locale === "fa" ? "داده‌ای برای خلاصه API وجود ندارد" : "No API summary data available"}</strong>
              <span className="muted-block">{locale === "fa" ? "این صفحه دیگر از definitionهای ساختگی استفاده نمی‌کند." : "This page no longer falls back to fabricated definitions."}</span>
            </div>
          )}
          <div className="card-title-row" style={{ marginTop: 20 }}>
            <h3>{locale === "fa" ? "نقشه شِما" : "Schema map"}</h3>
          </div>
          <div className="summary-grid" style={{ marginTop: 12 }}>
            <div className="mini-card"><strong>{selected?.title ?? "—"}</strong><span className="muted-block">{fields.length} {locale === "fa" ? "فیلد" : "fields"}</span></div>
            <div className="mini-card"><strong>{entities.length}</strong><span className="muted-block">{locale === "fa" ? "تعریف" : "definitions"}</span></div>
          </div>
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
    const parsed = JSON.parse(definition.definitionJson) as { fields?: Array<Record<string, unknown>> };
    const fields = parsed.fields ?? [];
    if (fields.length) {
      return fields.map((field) => ({
        name: String(field.key ?? field.name ?? "field"),
        type: String(field.type ?? "String"),
        required: Boolean(field.required),
        description: typeof field.label === "string" ? field.label : undefined
      }));
    }
  } catch {
    return [];
  }
  return [];
}
