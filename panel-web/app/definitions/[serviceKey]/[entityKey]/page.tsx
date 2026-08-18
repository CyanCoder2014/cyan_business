"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { SchemaTreeEditor, type SchemaField } from "@/components/definitions/schema-tree-editor";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { AsyncButton, CodeViewer, ConfirmDialog, ErrorState, Skeleton, StatusBadge, Tabs } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { getDefinition, listDefinitionVersions, publishDefinition, saveDefinition } from "@/lib/dynamic-api";
import type { DynamicEntityDefinition, DynamicServiceKey } from "@/lib/types";

type Version = { revision: number; status: string; createdAt: string };

export default function DefinitionEditor() {
  const params = useParams<{ serviceKey: string; entityKey: string }>();
  const router = useRouter();
  const { locale } = usePanel();
  const { showToast } = useToast();
  const { tenantKey, siteKey, queryVersion, can } = useScopeAccess();
  const service = params.serviceKey as DynamicServiceKey;
  const entity = decodeURIComponent(params.entityKey);
  const scope = useMemo(() => ({ tenantKey: tenantKey || undefined, siteKey: siteKey || undefined }), [tenantKey, siteKey]);
  const canManage = can("definition.manage");
  const [value, setValue] = useState<DynamicEntityDefinition | null>(null);
  const [draft, setDraft] = useState<Record<string, unknown> | null>(null);
  const [tab, setTab] = useState("schema");
  const [versions, setVersions] = useState<Version[]>([]);
  const [loading, setLoading] = useState(true);
  const [pending, setPending] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [dirty, setDirty] = useState(false);
  const [publishOpen, setPublishOpen] = useState(false);
  const [advancedText, setAdvancedText] = useState("");

  const load = useCallback(async () => {
    if (!tenantKey) return;
    setLoading(true); setError(null);
    try {
      const [definition, history] = await Promise.all([
        getDefinition(service, entity, scope),
        listDefinitionVersions(service, entity, scope).catch(() => []),
      ]);
      const next = structuredClone(definition.definition) as Record<string, unknown>;
      setValue(definition); setDraft(next); setAdvancedText(JSON.stringify(next, null, 2)); setVersions(history); setDirty(false);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Definition unavailable"); }
    finally { setLoading(false); }
  }, [entity, scope, service, tenantKey]);

  useEffect(() => { void load(); }, [load, queryVersion]);
  useEffect(() => { const warn = (event: BeforeUnloadEvent) => { if (dirty) { event.preventDefault(); event.returnValue = ""; } }; window.addEventListener("beforeunload", warn); return () => window.removeEventListener("beforeunload", warn); }, [dirty]);

  const changeDraft = (next: Record<string, unknown>) => { setDraft(next); setAdvancedText(JSON.stringify(next, null, 2)); setDirty(true); setError(null); };
  const fields = (draft?.fields && typeof draft.fields === "object" ? draft.fields : {}) as Record<string, SchemaField>;
  const save = async () => {
    if (!draft || pending || !canManage) return;
    setPending("save"); setError(null);
    try {
      const saved = await saveDefinition(service, entity, JSON.stringify(draft), scope, value?.revision);
      const next = structuredClone(saved.definition) as Record<string, unknown>;
      setValue(saved); setDraft(next); setAdvancedText(JSON.stringify(next, null, 2)); setDirty(false);
      setVersions(await listDefinitionVersions(service, entity, scope));
      showToast({ tone: "success", title: locale === "fa" ? "تعریف ذخیره شد" : "Definition saved" });
    } catch (cause) {
      const message = cause instanceof Error ? cause.message : "Save failed"; setError(message);
      showToast({ tone: "error", title: locale === "fa" ? "ذخیره ناموفق بود" : "Save failed", message });
    } finally { setPending(null); }
  };
  const publish = async () => {
    if (!canManage || pending) return;
    setPending("publish"); setError(null);
    try {
      setValue(await publishDefinition(service, entity, scope));
      setVersions(await listDefinitionVersions(service, entity, scope)); setPublishOpen(false);
      showToast({ tone: "success", title: locale === "fa" ? "تعریف منتشر شد" : "Definition published" });
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Publish failed"); }
    finally { setPending(null); }
  };
  const applyAdvanced = () => {
    try { const parsed = JSON.parse(advancedText); if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") throw new Error(); setDraft(parsed); setDirty(true); setError(null); }
    catch { setError(locale === "fa" ? "JSON پیشرفته معتبر نیست." : "Advanced JSON is invalid."); }
  };

  const tabs = [
    { key: "schema", label: locale === "fa" ? "ساختار" : "Schema" },
    { key: "general", label: locale === "fa" ? "عمومی" : "General" },
    { key: "display", label: locale === "fa" ? "نمایش" : "Display" },
    { key: "access", label: locale === "fa" ? "دسترسی" : "Access" },
    { key: "versions", label: locale === "fa" ? "نسخه‌ها" : "Versions" },
    { key: "advanced", label: locale === "fa" ? "پیشرفته" : "Advanced" },
  ];

  return <PanelShell activeKey="maker" title={value?.title || entity} titleFa={value?.title || entity} subtitle={`${service} · ${entity}`} subtitleFa={`${service} · ${entity}`}>
    <div className="definition-editor-toolbar"><button className="secondary-pill" onClick={() => router.push("/definitions")}>← {locale === "fa" ? "تعریف‌ها" : "Definitions"}</button><div><button className="secondary-pill" disabled={Boolean(pending)} onClick={load}>{locale === "fa" ? "بارگذاری مجدد" : "Reload"}</button>{canManage?<><AsyncButton className="secondary-pill" pending={pending === "save"} pendingLabel={locale === "fa" ? "ذخیره…" : "Saving…"} disabled={!dirty || Boolean(pending)} onClick={save}>{locale === "fa" ? "ذخیره پیش‌نویس" : "Save draft"}</AsyncButton><button className="primary-pill" disabled={dirty || !value || Boolean(pending)} onClick={() => setPublishOpen(true)}>{locale === "fa" ? "انتشار" : "Publish"}</button></>:null}</div></div>
    {!canManage?<div className="operational-banner warning" role="status">{locale === "fa" ? "این تعریف فقط‌خواندنی است؛ مجوز definition.manage برای ویرایش لازم است." : "This definition is read-only; editing requires definition.manage."}</div>:null}
    {error && draft ? <div className="operational-banner error" role="alert"><span>{error}</span><button aria-label="Dismiss" onClick={() => setError(null)}>×</button></div> : null}
    {loading ? <Skeleton height={620}/> : error && !draft ? <ErrorState title={locale === "fa" ? "تعریف در دسترس نیست" : "Definition unavailable"} description={error} retry={load}/> : draft ? <section className={`definition-visual-editor ${canManage ? "" : "read-only"}`}>
      <Tabs active={tab} onChange={setTab} items={tabs}/>
      {tab === "schema" ? <SchemaTreeEditor fields={fields} locale={locale} onChange={next => canManage && changeDraft({ ...draft, fields: next })}/>
        : tab === "general" ? <div className="definition-focused-form"><label><span>{locale === "fa" ? "عنوان" : "Title"}</span><input disabled={!canManage} value={String(draft.title ?? "")} onChange={event => changeDraft({ ...draft, title: event.target.value })}/></label><label><span>{locale === "fa" ? "نوع موجودیت" : "Entity type"}</span><input dir="ltr" disabled={!canManage} value={String(draft.entityType ?? "")} onChange={event => changeDraft({ ...draft, entityType: event.target.value })}/></label><div className="definition-summary-strip"><span><strong>{Object.keys(fields).length}</strong>{locale === "fa" ? "فیلد اصلی" : "root fields"}</span><span><strong>{value?.revision ?? 0}</strong>{locale === "fa" ? "نسخه" : "revision"}</span><span><StatusBadge tone={value?.active ? "success" : "neutral"}>{value?.active ? "PUBLISHED" : "DRAFT"}</StatusBadge>{locale === "fa" ? "وضعیت" : "status"}</span></div></div>
        : tab === "display" ? <ConfigCards locale={locale} disabled={!canManage} draft={draft} changeDraft={changeDraft} sections={["listConfiguration", "objectConfiguration"]}/>
        : tab === "access" ? <ConfigCards locale={locale} disabled={!canManage} draft={draft} changeDraft={changeDraft} sections={["permissions", "scope", "relationDefinitions"]}/>
        : tab === "versions" ? <div className="version-list">{versions.map(version => <div key={version.revision}><strong>r{version.revision}</strong><StatusBadge tone={version.status === "PUBLISHED" ? "success" : "info"}>{version.status}</StatusBadge><time>{new Date(version.createdAt).toLocaleString(locale)}</time></div>)}</div>
        : <div className="advanced-definition-editor"><div className="operational-banner warning" role="status">{locale === "fa" ? "این نمای پیشرفته کل قرارداد تعریف را ویرایش می‌کند. برای کار روزمره از درخت ساختار استفاده کنید." : "This advanced view edits the full definition contract. Use the schema tree for normal work."}</div><textarea dir="ltr" disabled={!canManage} value={advancedText} onChange={event => setAdvancedText(event.target.value)}/><div><button className="secondary-pill" disabled={!canManage} onClick={applyAdvanced}>{locale === "fa" ? "اعمال JSON" : "Apply JSON"}</button><details><summary>{locale === "fa" ? "مقایسه با نسخه ذخیره‌شده" : "Compare with saved"}</summary><CodeViewer value={{ saved: value?.definition ?? {}, current: draft }}/></details></div></div>}
    </section> : null}
    <ConfirmDialog open={publishOpen} title={locale === "fa" ? "انتشار این تعریف؟" : "Publish this definition?"} body={locale === "fa" ? "نسخه ذخیره‌شده برای داده‌های جدید فعال می‌شود." : "The saved revision becomes active for new records."} confirmLabel={locale === "fa" ? "انتشار" : "Publish"} pending={pending === "publish"} onClose={() => setPublishOpen(false)} onConfirm={publish}/>
  </PanelShell>;
}

function ConfigCards({ locale, disabled, draft, changeDraft, sections }: { locale: "en" | "fa"; disabled: boolean; draft: Record<string, unknown>; changeDraft: (next: Record<string, unknown>) => void; sections: string[] }) {
  return <div className="definition-config-cards">{sections.map(section => <section key={section}><header><div><strong>{section.replace(/([A-Z])/g, " $1")}</strong><small>{locale === "fa" ? "پیکربندی اختیاری" : "Optional configuration"}</small></div></header><KeyValueEditor disabled={disabled} value={draft[section] && typeof draft[section] === "object" && !Array.isArray(draft[section]) ? draft[section] as Record<string, unknown> : {}} onChange={value => changeDraft({ ...draft, [section]: value })}/></section>)}</div>;
}

function KeyValueEditor({ value, disabled, onChange }: { value: Record<string, unknown>; disabled: boolean; onChange: (value: Record<string, unknown>) => void }) {
  const entries = Object.entries(value);
  const update = (index: number, key: string, nextValue: string) => onChange(Object.fromEntries(entries.map((entry, itemIndex) => itemIndex === index ? [key, nextValue] : entry)));
  return <div className="key-value-editor">{entries.map(([key, item], index) => <div key={`${key}-${index}`}><input disabled={disabled} dir="ltr" aria-label="Configuration key" value={key} onChange={event => update(index, event.target.value, String(item ?? ""))}/><input disabled={disabled} dir="ltr" aria-label="Configuration value" value={typeof item === "object" ? JSON.stringify(item) : String(item ?? "")} onChange={event => update(index, key, event.target.value)}/><button disabled={disabled} aria-label="Remove configuration" onClick={() => onChange(Object.fromEntries(entries.filter((_, itemIndex) => itemIndex !== index)))}>×</button></div>)}<button className="secondary-pill" disabled={disabled} onClick={() => onChange({ ...value, [`property${entries.length + 1}`]: "" })}>＋ Add property</button></div>;
}
