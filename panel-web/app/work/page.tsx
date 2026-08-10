"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { listCartable, type ManagedObjectQueue } from "@/lib/bpm-api";
import { useScopeAccess } from "@/components/scope-access-provider";
import { usePanel } from "@/components/panel-provider";

const emptyPage: ManagedObjectQueue = { content: [], totalElements: 0, page: 0, size: 20 };

export default function WorkQueue() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
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
    catch (reason) { setError(reason instanceof Error ? reason.message : String(reason)); }
    finally { setLoading(false); }
  }, [tenantKey, siteKey, queryVersion, view, query, priority, overdue, page]);
  useEffect(() => { const timer = setTimeout(() => void load(), 180); return () => clearTimeout(timer); }, [load]);

  return <PanelShell activeKey="flows" title="Work queue" titleFa="کارتابل" subtitle="Role-aware, server-filtered business work." subtitleFa="کارهای کسب‌وکار با فیلتر سمت سرور و دسترسی مبتنی بر نقش.">
    <div className="work-filter-bar">
      <div className="ui-tabs">{[["ASSIGNED", "Assigned", "محول‌شده"], ["VISIBLE", "Visible", "قابل مشاهده"], ["ROLE", "My roles", "نقش‌های من"], ["GROUP", "My groups", "گروه‌های من"], ["UNASSIGNED", "Unassigned", "بدون مسئول"], ["COMPLETED", "Completed", "تکمیل‌شده"]].map(([key, en, fa]) => <button key={key} aria-pressed={view === key} onClick={() => { setView(key); setPage(0); }}>{locale === "fa" ? fa : en}</button>)}</div>
      <input aria-label="Search work" value={query} onChange={event => { setQuery(event.target.value); setPage(0); }} placeholder={locale === "fa" ? "جستجوی کار" : "Search work"}/>
      <select aria-label="Priority" value={priority} onChange={event => { setPriority(event.target.value); setPage(0); }}><option value="">{locale === "fa" ? "همه اولویت‌ها" : "All priorities"}</option><option>URGENT</option><option>HIGH</option><option>NORMAL</option><option>LOW</option></select>
      <label className="check-row"><input type="checkbox" checked={overdue} onChange={event => { setOverdue(event.target.checked); setPage(0); }}/><span>{locale === "fa" ? "فقط عقب‌افتاده" : "Overdue only"}</span></label>
    </div>
    {loading ? <Skeleton height={360}/> : error ? <ErrorState title="Work queue unavailable" description={error} retry={load}/> : result.content.length ? <>
      <div className="work-list">{result.content.map(item => <Link href={`/work/${item.id}`} key={item.id}><div><strong>{item.objectType}</strong><span>{item.flowKey}</span></div><span>{item.state}</span><StatusBadge tone={item.priority === "URGENT" ? "danger" : item.priority === "HIGH" ? "warning" : "neutral"}>{item.priority ?? "NORMAL"}</StatusBadge><span>{item.assignee ?? (locale === "fa" ? "بدون مسئول" : "Unassigned")}</span><time>{item.updatedAt ? new Date(item.updatedAt).toLocaleString(locale) : "—"}</time></Link>)}</div>
      <div className="pagination-bar"><button className="secondary-pill" disabled={page === 0} onClick={() => setPage(value => value - 1)}>{locale === "fa" ? "قبلی" : "Previous"}</button><span>{locale === "fa" ? `${result.totalElements} مورد` : `${result.totalElements} items`}</span><button className="secondary-pill" disabled={(page + 1) * result.size >= result.totalElements} onClick={() => setPage(value => value + 1)}>{locale === "fa" ? "بعدی" : "Next"}</button></div>
    </> : <EmptyState title={locale === "fa" ? "کاری در صف نیست" : "No work in this queue"} description={locale === "fa" ? "سرویس موردی برای این فیلتر برنگرداند." : "The service returned no items for this queue."}/>}
  </PanelShell>;
}
