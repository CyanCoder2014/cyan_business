"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { AsyncButton, Dialog, EmptyState, ErrorState, Field, Select, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";
import { createManagedObject, listCartable, listFlows, type DynamicFlowDefinition, type ManagedObjectQueue } from "@/lib/bpm-api";
import { useScopeAccess } from "@/components/scope-access-provider";
import { usePanel } from "@/components/panel-provider";
import { useRouter } from "next/navigation";

const emptyPage: ManagedObjectQueue = { content: [], totalElements: 0, page: 0, size: 20 };

export default function WorkQueue() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion, can } = useScopeAccess();
  const { showToast } = useToast();
  const router = useRouter();
  const [createOpen, setCreateOpen] = useState(false);
  const [result, setResult] = useState(emptyPage);
  const [view, setView] = useState("ASSIGNED");
  const [query, setQuery] = useState("");
  const [priority, setPriority] = useState("");
  const [overdue, setOverdue] = useState(false);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const scope = { tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined };
  const load = useCallback(async () => {
    if (!tenantKey) { setLoading(false); return; }
    setLoading(true); setError(null);
    try { setResult(await listCartable({ ...scope, view, query, priority, overdue: overdue || undefined, page, size: 20 })); }
    catch (reason) { const { title, message } = describeApiError(reason, locale === "fa" ? "کارتابل در دسترس نیست" : "Work queue unavailable"); setError(message); showToast({ tone: "error", title, message }); }
    finally { setLoading(false); }
  }, [tenantKey, siteKey, queryVersion, view, query, priority, overdue, page, locale, showToast]);
  useEffect(() => { const timer = setTimeout(() => void load(), 180); return () => clearTimeout(timer); }, [load]);

  return <PanelShell activeKey="work" title="Work queue" titleFa="کارتابل" subtitle="Role-aware, server-filtered business work." subtitleFa="کارهای کسب‌وکار با فیلتر سمت سرور و دسترسی مبتنی بر نقش.">
    <div className="work-filter-bar">
      <div className="ui-tabs">{[["ASSIGNED", "Assigned", "محول‌شده"], ["VISIBLE", "Visible", "قابل مشاهده"], ["ROLE", "My roles", "نقش‌های من"], ["GROUP", "My groups", "گروه‌های من"], ["UNASSIGNED", "Unassigned", "بدون مسئول"], ["COMPLETED", "Completed", "تکمیل‌شده"]].map(([key, en, fa]) => <button key={key} aria-pressed={view === key} onClick={() => { setView(key); setPage(0); }}>{locale === "fa" ? fa : en}</button>)}</div>
      <input aria-label="Search work" value={query} onChange={event => { setQuery(event.target.value); setPage(0); }} placeholder={locale === "fa" ? "جستجوی کار" : "Search work"}/>
      <select aria-label="Priority" value={priority} onChange={event => { setPriority(event.target.value); setPage(0); }}><option value="">{locale === "fa" ? "همه اولویت‌ها" : "All priorities"}</option><option>URGENT</option><option>HIGH</option><option>NORMAL</option><option>LOW</option></select>
      <label className="check-row"><input type="checkbox" checked={overdue} onChange={event => { setOverdue(event.target.checked); setPage(0); }}/><span>{locale === "fa" ? "فقط عقب‌افتاده" : "Overdue only"}</span></label>
      {can("bpm.manage") ? <button className="primary-pill" onClick={() => setCreateOpen(true)}>＋ {locale === "fa" ? "مورد جدید" : "New work item"}</button> : null}
    </div>
    {createOpen ? <NewWorkItemDialog locale={locale} scope={{ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }} onClose={() => setCreateOpen(false)} onCreated={(objectId) => { setCreateOpen(false); showToast({ tone: "success", title: locale === "fa" ? "مورد کار ایجاد شد" : "Work item created" }); router.push(`/work/${objectId}`); }} onError={(cause) => { const described = describeApiError(cause, locale === "fa" ? "ایجاد ناموفق بود" : "Creation failed"); showToast({ tone: "error", title: described.title, message: described.message }); }}/> : null}
    {loading ? <Skeleton height={360}/> : error ? <ErrorState title="Work queue unavailable" description={error} retry={load}/> : result.content.length ? <>
      <div className="work-list">{result.content.map(item => <Link href={`/work/${item.id}`} key={item.id}><div><strong>{item.title || item.objectType}</strong><span>{item.title ? item.objectType : item.flowKey}</span></div><span>{item.state}</span><StatusBadge tone={item.priority === "URGENT" ? "danger" : item.priority === "HIGH" ? "warning" : "neutral"}>{item.priority ?? "NORMAL"}</StatusBadge><span>{item.assignee ?? (locale === "fa" ? "بدون مسئول" : "Unassigned")}</span><time>{item.updatedAt ? new Date(item.updatedAt).toLocaleString(locale) : "—"}</time></Link>)}</div>
      <div className="pagination-bar"><button className="secondary-pill" disabled={page === 0} onClick={() => setPage(value => value - 1)}>{locale === "fa" ? "قبلی" : "Previous"}</button><span>{locale === "fa" ? `${result.totalElements} مورد` : `${result.totalElements} items`}</span><button className="secondary-pill" disabled={(page + 1) * result.size >= result.totalElements} onClick={() => setPage(value => value + 1)}>{locale === "fa" ? "بعدی" : "Next"}</button></div>
    </> : <EmptyState title={locale === "fa" ? "کاری در صف نیست" : "No work in this queue"} description={locale === "fa" ? "سرویس موردی برای این فیلتر برنگرداند." : "The service returned no items for this queue."} action={can("bpm.manage") ? <button className="primary-pill" onClick={() => setCreateOpen(true)}>＋ {locale === "fa" ? "مورد جدید" : "New work item"}</button> : undefined}/>}
  </PanelShell>;
}

function NewWorkItemDialog({ locale, scope, onClose, onCreated, onError }: { locale: "en" | "fa"; scope: { tenantKey?: string; siteKey?: string }; onClose: () => void; onCreated: (objectId: string) => void; onError: (cause: unknown) => void }) {
  const [flows, setFlows] = useState<DynamicFlowDefinition[]>([]);
  const [flowKey, setFlowKey] = useState("");
  const [objectType, setObjectType] = useState("");
  const [title, setTitle] = useState("");
  const [pending, setPending] = useState(false);
  useEffect(() => { listFlows(scope).then(value => setFlows(value.filter(item => item.active))).catch(() => setFlows([])); }, [scope.tenantKey, scope.siteKey]);
  const create = async () => {
    if (!flowKey || !objectType.trim() || pending) return;
    setPending(true);
    try { const created = await createManagedObject({ flowKey, objectType: objectType.trim(), title: title.trim() || undefined }, scope); onCreated(created.id); }
    catch (cause) { onError(cause); }
    finally { setPending(false); }
  };
  return <Dialog open title={locale === "fa" ? "مورد کار جدید" : "New work item"} description={locale === "fa" ? "یک نمونه واقعی از فرایند BPM را شروع کنید." : "Start a real instance of a BPM process."} onClose={onClose}>
    <div className="dialog-form">
      <Select label={locale === "fa" ? "فرایند" : "Process"} value={flowKey} onChange={event => setFlowKey(event.target.value)}>
        <option value="">{locale === "fa" ? "انتخاب کنید" : "Select a process"}</option>
        {flows.map(flow => <option key={flow.flowKey} value={flow.flowKey}>{flow.name || flow.flowKey}</option>)}
      </Select>
      <Field label={locale === "fa" ? "نوع مورد" : "Object type"} placeholder={locale === "fa" ? "مثلاً: درخواست مرخصی" : "e.g. leave-request"} value={objectType} onChange={event => setObjectType(event.target.value)}/>
      <Field label={locale === "fa" ? "عنوان (اختیاری، بعداً قابل ویرایش)" : "Title (optional, editable later)"} placeholder={locale === "fa" ? "مثلاً: مرخصی سارا — سه‌شنبه" : "e.g. Sara's leave request — Tuesday"} value={title} onChange={event => setTitle(event.target.value)}/>
      {!flows.length ? <p className="bpm-field-hint">{locale === "fa" ? "هیچ فرایند فعالی یافت نشد. ابتدا یک فرایند BPM بسازید و فعال کنید." : "No active process was found. Design and activate a BPM process first."}</p> : null}
      <div className="dialog-actions">
        <button className="secondary-pill" onClick={onClose}>{locale === "fa" ? "لغو" : "Cancel"}</button>
        <AsyncButton className="primary-pill" pending={pending} disabled={!flowKey || !objectType.trim()} onClick={create}>{locale === "fa" ? "ایجاد" : "Create"}</AsyncButton>
      </div>
    </div>
  </Dialog>;
}
