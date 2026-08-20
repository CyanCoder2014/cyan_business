"use client";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { listExecutions, type AutomationExecution } from "@/lib/automation-api";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";

export default function Executions() {
  const { locale } = usePanel(); const { tenantKey, siteKey, queryVersion } = useScopeAccess(); const { showToast } = useToast();
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }), [tenantKey, siteKey]);
  const [items, setItems] = useState<AutomationExecution[]>([]); const [status, setStatus] = useState(""); const [loading, setLoading] = useState(true); const [error, setError] = useState<string | null>(null);
  const load = useCallback(() => { if (!tenantKey) return; setLoading(true); setError(null); listExecutions(undefined, status || undefined, scope).then(setItems).catch(cause => { const { title, message } = describeApiError(cause, "Executions unavailable"); setError(message); showToast({ tone: "error", title, message }); }).finally(() => setLoading(false)); }, [scope, status, tenantKey, showToast]);
  useEffect(load, [load, queryVersion]);
  return <PanelShell activeKey="automation" title="Automation executions" titleFa="اجراهای اتوماسیون" subtitle="Inspect current and historical runs." subtitleFa="اجرای جاری و تاریخچه را بررسی کنید."><div className="page-action-bar"><div><select aria-label="Execution status" value={status} onChange={event => setStatus(event.target.value)}><option value="">{locale === "fa" ? "همه وضعیت‌ها" : "All statuses"}</option>{["RUNNING", "WAITING", "COMPLETED", "FAILED", "CANCELLED"].map(item => <option key={item}>{item}</option>)}</select></div><div><Link className="secondary-pill" href="/automations">{locale === "fa" ? "بازگشت" : "Back"}</Link></div></div>{loading ? <Skeleton height={320}/> : error ? <ErrorState title="Executions unavailable" description={error} retry={load}/> : items.length ? <div className="execution-list">{items.map(item => <Link key={item.executionId} href={`/automations/executions/${item.executionId}`}><div><strong>{item.automationFlowKey ?? "—"}</strong><code>{item.executionId}</code></div><StatusBadge tone={item.status === "FAILED" ? "danger" : item.status === "COMPLETED" ? "success" : "info"}>{item.status}</StatusBadge><time>{item.createdAt ? new Date(item.createdAt).toLocaleString(locale) : "—"}</time></Link>)}</div> : <EmptyState title={locale === "fa" ? "اجرایی نیست" : "No executions"} description={locale === "fa" ? "سرویس اجرایی برای این محدوده برنگرداند." : "The service returned no executions for this scope."}/>}</PanelShell>;
}
