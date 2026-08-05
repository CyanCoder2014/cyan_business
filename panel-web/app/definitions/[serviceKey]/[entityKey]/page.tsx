"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { CodeViewer, EmptyState, ErrorState, Skeleton, StatusBadge, Tabs } from "@/components/ui/primitives";
import { getDefinition, listDefinitionVersions, publishDefinition, saveDefinition } from "@/lib/dynamic-api";
import type { DynamicEntityDefinition, DynamicServiceKey } from "@/lib/types";

type Field = {
  id?: string;
  type?: string;
  defaultValue?: unknown;
  validations?: Array<Record<string, unknown>>;
  itemValidations?: Record<string, Field>;
  [key: string]: unknown;
};

type Version = { revision: number; status: string; createdAt: string };

export default function DefinitionEditor() {
  const params = useParams<{ serviceKey: string; entityKey: string }>();
  const router = useRouter();
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const service = params.serviceKey as DynamicServiceKey;
  const entity = decodeURIComponent(params.entityKey);
  const scope = useMemo(() => ({ tenantKey: tenantKey || undefined, siteKey: siteKey || undefined }), [tenantKey, siteKey]);
  const [value, setValue] = useState<DynamicEntityDefinition | null>(null);
  const [draft, setDraft] = useState<Record<string, unknown> | null>(null);
  const [selected, setSelected] = useState<string | null>(null);
  const [tab, setTab] = useState("fields");
  const [versions, setVersions] = useState<Version[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dirty, setDirty] = useState(false);

  const load = useCallback(async () => {
    if (!tenantKey) return;
    setLoading(true);
    setError(null);
    try {
      const [definition, history] = await Promise.all([
        getDefinition(service, entity, scope),
        listDefinitionVersions(service, entity, scope).catch(() => []),
      ]);
      setValue(definition);
      setDraft(structuredClone(definition.definition));
      setVersions(history);
      setDirty(false);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Definition unavailable");
    } finally {
      setLoading(false);
    }
  }, [entity, scope, service, tenantKey]);

  useEffect(() => { void load(); }, [load, queryVersion]);
  useEffect(() => {
    const warn = (event: BeforeUnloadEvent) => {
      if (dirty) {
        event.preventDefault();
        event.returnValue = "";
      }
    };
    window.addEventListener("beforeunload", warn);
    return () => window.removeEventListener("beforeunload", warn);
  }, [dirty]);

  const fields = (draft?.fields && typeof draft.fields === "object" ? draft.fields : {}) as Record<string, Field>;
  const selectedField = selected ? fields[selected] : null;
  const updateFields = (next: Record<string, Field>) => {
    setDraft((current) => ({ ...current!, fields: next }));
    setDirty(true);
  };
  const updateSelected = (patch: Partial<Field>) => {
    if (selected) updateFields({ ...fields, [selected]: { ...fields[selected], ...patch } });
  };
  const moveField = (key: string, offset: number) => {
    const entries = Object.entries(fields);
    const index = entries.findIndex(([candidate]) => candidate === key);
    const target = index + offset;
    if (index < 0 || target < 0 || target >= entries.length) return;
    [entries[index], entries[target]] = [entries[target], entries[index]];
    updateFields(Object.fromEntries(entries));
  };
  const updateSection = (key: string, text: string) => {
    try { setDraft((current) => ({ ...current!, [key]: JSON.parse(text) })); setDirty(true); setError(null); }
    catch { setError(locale === "fa" ? "JSON نامعتبر است" : "Invalid JSON"); }
  };
  const save = async () => {
    if (!draft) return;
    try {
      const saved = await saveDefinition(service, entity, JSON.stringify(draft), scope, value?.revision);
      setValue(saved);
      setDraft(structuredClone(saved.definition));
      setDirty(false);
      setVersions(await listDefinitionVersions(service, entity, scope));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Save failed");
    }
  };
  const publish = async () => {
    if (!window.confirm(locale === "fa" ? "این نسخه منتشر شود؟" : "Publish this definition revision?")) return;
    try {
      setValue(await publishDefinition(service, entity, scope));
      setVersions(await listDefinitionVersions(service, entity, scope));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Publish failed");
    }
  };

  return (
    <PanelShell activeKey="maker" title={value?.title || entity} titleFa={value?.title || entity} subtitle={`${service} · ${entity}`} subtitleFa={`${service} · ${entity}`}>
      <div className="definition-editor-toolbar">
        <button className="secondary-pill" onClick={() => router.push("/definitions")}>← {locale === "fa" ? "تعریف‌ها" : "Definitions"}</button>
        <div>
          <button className="secondary-pill" onClick={load}>{locale === "fa" ? "اعتبارسنجی مجدد" : "Reload & validate"}</button>
          <button className="secondary-pill" disabled={!dirty} onClick={save}>{locale === "fa" ? "ذخیره" : "Save"}</button>
          <button className="primary-pill" disabled={dirty || !value} onClick={publish}>{locale === "fa" ? "انتشار" : "Publish"}</button>
        </div>
      </div>
      {loading ? <Skeleton height={500} /> : error && !draft ? <ErrorState title="Definition unavailable" description={error} retry={load} /> : draft ? (
        <div className="definition-editor">
          <aside className="field-tree">
            <header><h2>{locale === "fa" ? "فیلدها" : "Fields"}</h2><button onClick={() => { const key = window.prompt("Field key"); if (key && !fields[key]) { updateFields({ ...fields, [key]: { id: key, type: "string", validations: [] } }); setSelected(key); } }}>＋</button></header>
            {Object.entries(fields).map(([key, field]) => <button className={selected === key ? "active" : ""} key={key} onClick={() => setSelected(key)} onKeyDown={(event) => { if (event.altKey && event.key === "ArrowUp") { event.preventDefault(); moveField(key, -1); } if (event.altKey && event.key === "ArrowDown") { event.preventDefault(); moveField(key, 1); } }}><span>{key}</span><small>{field.type || "string"}</small>{field.itemValidations ? <em>{Object.keys(field.itemValidations).length} nested</em> : null}</button>)}
          </aside>
          <main className="definition-main">
            <Tabs active={tab} onChange={setTab} items={[{ key: "general", label: locale === "fa" ? "عمومی" : "General" }, { key: "fields", label: locale === "fa" ? "فیلدها" : "Fields" }, { key: "validations", label: locale === "fa" ? "اعتبارسنجی" : "Validations" }, { key: "operations", label: locale === "fa" ? "عملیات" : "Operations" }, { key: "listConfiguration", label: locale === "fa" ? "فهرست" : "List/grid" }, { key: "objectConfiguration", label: locale === "fa" ? "جزئیات" : "Object/detail" }, { key: "relations", label: locale === "fa" ? "روابط" : "Relations" }, { key: "permissions", label: locale === "fa" ? "دسترسی" : "Permissions" }, { key: "scope", label: locale === "fa" ? "محدوده" : "Scope" }, { key: "json", label: "JSON" }, { key: "diff", label: locale === "fa" ? "تغییرات" : "Diff" }, { key: "versions", label: locale === "fa" ? "نسخه‌ها" : "Versions" }]} />
            {tab === "general" ? <div className="editor-form"><label>Title<input value={String(draft.title || "")} onChange={(event) => { setDraft({ ...draft, title: event.target.value }); setDirty(true); }} /></label><label>Entity type<input value={String(draft.entityType || "")} onChange={(event) => { setDraft({ ...draft, entityType: event.target.value }); setDirty(true); }} /></label></div>
              : tab === "fields" ? <div className="field-table">{Object.entries(fields).map(([key, field]) => <button key={key} onClick={() => setSelected(key)}><strong>{key}</strong><span>{field.type || "string"}</span><span>{(field.validations || []).length} rules</span></button>)}</div>
              : ["validations", "operations", "listConfiguration", "objectConfiguration", "permissions", "scope"].includes(tab) ? <textarea className="definition-json-editor" dir="ltr" aria-label={`${tab} JSON`} value={JSON.stringify(draft[tab] ?? (tab === "validations" ? [] : {}), null, 2)} onChange={(event) => updateSection(tab, event.target.value)} />
              : tab === "relations" ? <textarea className="definition-json-editor" dir="ltr" aria-label="relations JSON" value={JSON.stringify(draft.relationDefinitions || {}, null, 2)} onChange={(event) => updateSection("relationDefinitions", event.target.value)} />
              : tab === "diff" ? <CodeViewer value={{ saved: value?.definition ?? {}, current: draft }} />
              : tab === "versions" ? <div className="version-list">{versions.map((version) => <div key={version.revision}><strong>r{version.revision}</strong><StatusBadge tone={version.status === "PUBLISHED" ? "success" : "info"}>{version.status}</StatusBadge><time>{new Date(version.createdAt).toLocaleString(locale)}</time></div>)}</div>
              : <textarea className="definition-json-editor" dir="ltr" value={JSON.stringify(draft, null, 2)} onChange={(event) => { try { setDraft(JSON.parse(event.target.value)); setDirty(true); setError(null); } catch { setError(locale === "fa" ? "JSON نامعتبر است" : "Invalid JSON"); } }} />}
          </main>
          <aside className="field-inspector">
            {selectedField && selected ? <><header><h2>{selected}</h2><button onClick={() => { const next = { ...fields }; delete next[selected]; updateFields(next); setSelected(null); }}>×</button></header><label>Type<select value={selectedField.type || "string"} onChange={(event) => updateSelected({ type: event.target.value })}>{["string", "number", "integer", "boolean", "date", "datetime", "object", "list", "enum", "relation"].map((type) => <option key={type}>{type}</option>)}</select></label><label>Default<input value={selectedField.defaultValue == null ? "" : String(selectedField.defaultValue)} onChange={(event) => updateSelected({ defaultValue: event.target.value })} /></label><label>Validations<textarea dir="ltr" value={JSON.stringify(selectedField.validations || [], null, 2)} onChange={(event) => { try { updateSelected({ validations: JSON.parse(event.target.value) }); } catch {} }} /></label>{selectedField.type === "object" || selectedField.type === "list" ? <label>Nested fields<textarea dir="ltr" value={JSON.stringify(selectedField.itemValidations || {}, null, 2)} onChange={(event) => { try { updateSelected({ itemValidations: JSON.parse(event.target.value) }); } catch {} }} /></label> : null}<p className="muted">{locale === "fa" ? "برای جابه‌جایی فیلد Alt+↑/↓ را در درخت فیلدها بزنید." : "Use Alt+↑/↓ on a field in the tree to reorder it."}</p></> : <EmptyState title={locale === "fa" ? "فیلدی انتخاب نشده" : "No field selected"} description={locale === "fa" ? "برای ویرایش یک فیلد را انتخاب کنید." : "Select a field to edit its metadata."} />}
          </aside>
        </div>
      ) : null}
      {error && draft ? <p role="alert" className="operational-banner error">{error}</p> : null}
    </PanelShell>
  );
}
