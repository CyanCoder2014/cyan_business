"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { AsyncButton, ConfirmDialog, EmptyState, ErrorState, Skeleton, ValidationSummary } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError, PlatformApiError, fieldErrorsByPath } from "@/lib/api-error";
import { deleteRecord, getDefinition, listRecordsPage, submitRecord, updateRecord } from "@/lib/dynamic-api";
import type { DynamicEntityDefinition, DynamicEntityRecord, DynamicServiceKey } from "@/lib/types";
import { GeneratedField, type Field } from "@/components/forms/generated-field";

const SORT_OPTIONS = [
  { value: "createdAt,desc", en: "Newest first", fa: "جدیدترین" },
  { value: "createdAt,asc", en: "Oldest first", fa: "قدیمی‌ترین" },
  { value: "updatedAt,desc", en: "Recently updated", fa: "به‌روزرسانی اخیر" },
  { value: "recordKey,asc", en: "Record key A–Z", fa: "کلید رکورد الف تا ی" },
  { value: "recordKey,desc", en: "Record key Z–A", fa: "کلید رکورد ی تا الف" },
  { value: "status,asc", en: "Status", fa: "وضعیت" }
];

export default function DataManager() {
  const params = useParams<{ serviceKey: string; entityKey: string }>();
  const router = useRouter();
  const { locale } = usePanel();
  const { showToast } = useToast();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const service = params.serviceKey as DynamicServiceKey;
  const entity = decodeURIComponent(params.entityKey);
  const [definition, setDefinition] = useState<DynamicEntityDefinition | null>(null);
  const [records, setRecords] = useState<DynamicEntityRecord[]>([]);
  const [selected, setSelected] = useState<DynamicEntityRecord | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [form, setForm] = useState<Record<string, unknown>>({});
  const [formError, setFormError] = useState<PlatformApiError | null>(null);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sort, setSort] = useState("createdAt,desc");
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [bulkDeleteOpen, setBulkDeleteOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [pending, setPending] = useState<"save" | "delete" | "bulk-delete" | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [remove, setRemove] = useState<DynamicEntityRecord | null>(null);
  const scope = { tenantKey: tenantKey || undefined, siteKey: siteKey || undefined };

  const load = useCallback(async () => {
    if (!tenantKey) { setLoading(false); return; }
    setLoading(true);
    setError(null);
    try {
      const [nextDefinition, response] = await Promise.all([
        getDefinition(service, entity, { tenantKey, siteKey: siteKey || undefined }),
        listRecordsPage(service, entity, { tenantKey, siteKey: siteKey || undefined }, page, 25, sort)
      ]);
      setDefinition(nextDefinition);
      setRecords(response.content);
      setTotal(response.totalElements);
      setTotalPages(response.totalPages);
      setSelectedKeys([]);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Records unavailable");
    } finally {
      setLoading(false);
    }
  }, [entity, page, service, siteKey, sort, tenantKey]);

  useEffect(() => { void load(); }, [load, queryVersion]);

  const fields = useMemo(() => ((definition?.definition.fields && typeof definition.definition.fields === "object") ? definition.definition.fields : {}) as Record<string, Field>, [definition]);
  const columns = useMemo(() => Object.keys(fields).slice(0, 6), [fields]);
  const inlineErrors = fieldErrorsByPath(formError);

  function openEditor(record: DynamicEntityRecord | null) {
    setSelected(record);
    setForm(record ? structuredClone(record.data) : Object.fromEntries(Object.entries(fields).filter(([, field]) => field.defaultValue !== undefined).map(([key, field]) => [key, field.defaultValue])));
    setFormError(null);
    setEditorOpen(true);
  }

  function closeEditor() {
    if (pending) return;
    setEditorOpen(false);
    setSelected(null);
    setForm({});
    setFormError(null);
  }

  async function save() {
    if (pending) return;
    setPending("save");
    setFormError(null);
    try {
      if (selected) await updateRecord(service, entity, selected.recordKey, form, scope);
      else await submitRecord(service, entity, crypto.randomUUID(), form, scope);
      showToast({ tone: "success", title: selected ? "Record updated" : "Record created", message: `${definition?.title || entity} was saved successfully.` });
      setEditorOpen(false);
      setSelected(null);
      setForm({});
      await load();
    } catch (caught) {
      const normalized = caught instanceof PlatformApiError ? caught : new PlatformApiError("UNKNOWN", caught instanceof Error ? caught.message : "Record validation failed", 0);
      setFormError(normalized);
      showToast({ tone: "error", title: normalized.kind === "VALIDATION" ? "Check the highlighted fields" : "Record was not saved", message: normalized.message });
    } finally {
      setPending(null);
    }
  }

  async function confirmDelete() {
    if (!remove || pending) return;
    setPending("delete");
    try {
      await deleteRecord(service, entity, remove.recordKey, scope);
      showToast({ tone: "success", title: "Record deleted", message: remove.recordKey });
      setRemove(null);
      await load();
    } catch (caught) {
      const described = describeApiError(caught, "Delete failed");
      showToast({ tone: "error", title: described.title, message: described.message });
    } finally {
      setPending(null);
    }
  }

  async function confirmBulkDelete() {
    if (!selectedKeys.length || pending) return;
    setPending("bulk-delete");
    const failures: string[] = [];
    for (const recordKey of selectedKeys) {
      try { await deleteRecord(service, entity, recordKey, scope); }
      catch { failures.push(recordKey); }
    }
    setBulkDeleteOpen(false);
    if (failures.length) {
      showToast({ tone: "error", title: locale === "fa" ? "حذف گروهی ناقص بود" : "Bulk delete incomplete", message: locale === "fa" ? `${failures.length} رکورد حذف نشد.` : `${failures.length} record(s) could not be deleted.` });
    } else {
      showToast({ tone: "success", title: locale === "fa" ? "رکوردها حذف شدند" : "Records deleted", message: `${selectedKeys.length}` });
    }
    setPending(null);
    await load();
  }

  const toggleSelected = (recordKey: string) => setSelectedKeys((current) => current.includes(recordKey) ? current.filter((key) => key !== recordKey) : [...current, recordKey]);
  const allSelected = records.length > 0 && selectedKeys.length === records.length;

  return <PanelShell activeKey="data" title={definition?.title || entity} titleFa={definition?.title || entity} subtitle={`${service} · ${total} records`} subtitleFa={`${service} · ${total} رکورد`}>
    <div className="data-manager-toolbar">
      <button className="secondary-pill" onClick={() => router.push("/data")}>← {locale === "fa" ? "موجودیت‌ها" : "Entities"}</button>
      <Link className="secondary-pill" href={`/definitions/${encodeURIComponent(service)}/${encodeURIComponent(entity)}`}>{locale === "fa" ? "مشاهده ساختار" : "View data schema"}</Link>
      <button className="secondary-pill" onClick={() => router.push(`/forms?serviceKey=${encodeURIComponent(service)}&entityKey=${encodeURIComponent(entity)}`)}>{locale === "fa" ? "انتشار فرم" : "Publish form"}</button>
      <button className="secondary-pill" onClick={() => router.push(`/bpm/new?entityService=${encodeURIComponent(service)}&entityKey=${encodeURIComponent(entity)}`)}>{locale === "fa" ? "استفاده در BPM" : "Use in BPM"}</button>
      <label className="data-manager-sort"><span>{locale === "fa" ? "مرتب‌سازی" : "Sort"}</span><select value={sort} onChange={(event) => { setSort(event.target.value); setPage(0); }}>{SORT_OPTIONS.map((option) => <option key={option.value} value={option.value}>{locale === "fa" ? option.fa : option.en}</option>)}</select></label>
      {selectedKeys.length ? <button className="secondary-pill danger-link-pill" onClick={() => setBulkDeleteOpen(true)}>{locale === "fa" ? `حذف ${selectedKeys.length} مورد` : `Delete ${selectedKeys.length} selected`}</button> : null}
      <button className="primary-pill" onClick={() => openEditor(null)}>＋ {locale === "fa" ? "رکورد جدید" : "New record"}</button>
    </div>
    {loading ? <Skeleton height={440}/> : error && !definition ? <ErrorState title="Data unavailable" description={error} retry={load}/> : records.length ? <div className="record-grid">
      <div className="record-grid-head"><span><input type="checkbox" aria-label={locale === "fa" ? "انتخاب همه" : "Select all"} checked={allSelected} onChange={() => setSelectedKeys(allSelected ? [] : records.map((record) => record.recordKey))}/></span><span>Key</span>{columns.map((column) => <span key={column}>{column}</span>)}<span/></div>
      {records.map((record) => <div key={record.recordKey} className={`record-grid-row ${selected?.recordKey === record.recordKey ? "selected" : ""}`}>
        <span><input type="checkbox" aria-label={`Select ${record.recordKey}`} checked={selectedKeys.includes(record.recordKey)} onChange={() => toggleSelected(record.recordKey)} onClick={(event) => event.stopPropagation()}/></span>
        <button onClick={() => openEditor(record)}><strong>{record.recordKey}</strong>{columns.map((column) => <span key={column}>{render(record.data[column])}</span>)}</button>
        <span role="button" tabIndex={0} aria-label={`Actions for ${record.recordKey}`} onKeyDown={(event) => { if (event.key === "Enter" || event.key === " ") { event.preventDefault(); event.stopPropagation(); setRemove(record); } }} onClick={(event) => { event.stopPropagation(); setRemove(record); }}>•••</span>
      </div>)}
    </div> : <EmptyState
      title={locale === "fa" ? "رکوردی نیست" : "No records"}
      description={locale === "fa" ? "سرویس برای این تعریف رکوردی برنگرداند." : "The service returned no records for this definition."}
      action={<button className="primary-pill" onClick={() => openEditor(null)}>Create record</button>}
    />}
    <footer className="pagination">
      <button disabled={page === 0 || loading} onClick={() => setPage((value) => value - 1)}>←</button>
      <span>{locale === "fa" ? `صفحه ${page + 1} از ${Math.max(totalPages, 1)}` : `Page ${page + 1} of ${Math.max(totalPages, 1)}`}</span>
      <button disabled={loading || page + 1 >= totalPages} onClick={() => setPage((value) => value + 1)}>→</button>
    </footer>
    <ConfirmDialog open={bulkDeleteOpen} title={locale === "fa" ? "حذف گروهی رکوردها؟" : "Delete selected records?"} body={<p>{locale === "fa" ? `${selectedKeys.length} رکورد به‌طور دائم حذف می‌شود.` : `${selectedKeys.length} record(s) will be permanently deleted.`}</p>} confirmLabel={locale === "fa" ? "حذف" : "Delete"} pending={pending === "bulk-delete"} onClose={() => { if (!pending) setBulkDeleteOpen(false); }} onConfirm={confirmBulkDelete}/>
    {editorOpen ? <div className="record-editor-backdrop" onMouseDown={closeEditor}><aside className="record-editor" role="dialog" aria-modal="true" aria-labelledby="record-editor-title" onMouseDown={(event) => event.stopPropagation()}>
      <header><div><span className="page-kicker">{selected ? "Edit record" : "Create record"}</span><h2 id="record-editor-title">{selected ? selected.recordKey : (locale === "fa" ? "رکورد جدید" : "New record")}</h2></div><button className="dialog-close" aria-label="Close editor" onClick={closeEditor} disabled={Boolean(pending)}>×</button></header>
      <ValidationSummary errors={formError?.fieldErrors ?? []} correlationId={formError?.correlationId}/>
      {formError && !formError.fieldErrors.length ? <div className="operational-banner error" role="alert"><span>{formError.message}</span>{formError.correlationId ? <small dir="ltr">{formError.correlationId}</small> : null}</div> : null}
      <div className="record-form-fields">{Object.entries(fields).map(([key, field]) => <GeneratedField key={key} path={key} name={key} field={field} value={form[key]} error={inlineErrors[key] ?? inlineErrors[`data.${key}`]} onChange={(value) => { setForm((current) => ({ ...current, [key]: value })); if (formError) setFormError(null); }}/>)}</div>
      <div className="record-editor-actions"><button className="secondary-pill" onClick={closeEditor} disabled={Boolean(pending)}>Cancel</button><AsyncButton pending={pending === "save"} pendingLabel="Saving…" onClick={save}>Save record</AsyncButton></div>
    </aside></div> : null}
    <ConfirmDialog open={Boolean(remove)} title="Delete record?" body={<><p>This permanently deletes the selected scoped record.</p><code dir="ltr">{remove?.recordKey}</code></>} confirmLabel="Delete record" pending={pending === "delete"} onClose={() => { if (!pending) setRemove(null); }} onConfirm={confirmDelete}/>
  </PanelShell>;
}

function render(value: unknown) {
  if (value == null || value === "") return "—";
  if (Array.isArray(value)) return `${value.length} items`;
  if (typeof value === "object") return `${Object.keys(value as object).length} fields`;
  return String(value);
}
