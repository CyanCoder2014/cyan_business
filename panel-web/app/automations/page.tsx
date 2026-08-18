"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { listAutomationFlows, listExecutions, type AutomationExecution, type AutomationFlow } from "@/lib/automation-api";

export default function AutomationsPage() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion, can } = useScopeAccess();
  const [flows, setFlows] = useState<AutomationFlow[]>([]);
  const [runs, setRuns] = useState<AutomationExecution[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [runtimeWarning, setRuntimeWarning] = useState<string | null>(null);
  const [query, setQuery] = useState("");
  const canOperate = can("operations:use") || can("automation.execute");
  const canManage = can("automation.manage");
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }), [tenantKey, siteKey]);

  const load = useCallback(async () => {
    if (!tenantKey) { setLoading(false); return; }
    setLoading(true); setError(null); setRuntimeWarning(null);
    try {
      setFlows(await listAutomationFlows(scope));
      if (canOperate) {
        try { setRuns(await listExecutions(undefined, undefined, scope)); }
        catch { setRuns([]); setRuntimeWarning(locale === "fa" ? "تاریخچه اجرا بارگیری نشد." : "Execution history could not be loaded."); }
      } else setRuns([]);
    } catch (reason) { setError(reason instanceof Error ? reason.message : String(reason)); }
    finally { setLoading(false); }
  }, [canOperate, locale, scope, tenantKey]);

  useEffect(() => { void load(); }, [load, queryVersion]);
  const latest = useMemo(() => Array.from(new Map(flows.map(flow => [flow.flowKey, flow])).values()).filter(flow => `${flow.name} ${flow.flowKey}`.toLowerCase().includes(query.toLowerCase())), [flows, query]);

  return <PanelShell activeKey="automation" title="Automations" titleFa="اتوماسیون‌ها" subtitle="Design, schedule, run, and diagnose service-backed workflows." subtitleFa="طراحی، زمان‌بندی، اجرا و بررسی جریان‌های واقعی.">
    <div className="page-action-bar"><div><input aria-label="Search automations" value={query} onChange={event => setQuery(event.target.value)} placeholder={locale === "fa" ? "جستجوی اتوماسیون" : "Search automations"}/></div><div>{canOperate ? <Link className="secondary-pill" href="/automations/executions">{locale === "fa" ? "اجراها" : "Executions"}</Link> : null}{canManage?<Link className="primary-pill" href="/automations/new">{locale === "fa" ? "اتوماسیون جدید" : "New automation"}</Link>:null}</div></div>
    {runtimeWarning ? <p className="operational-banner warning" role="status">{runtimeWarning}</p> : null}
    {loading ? <div className="catalog-skeleton"><Skeleton height={88}/><Skeleton height={88}/></div> : error ? <ErrorState title={locale === "fa" ? "اتوماسیون‌ها در دسترس نیستند" : "Automations unavailable"} description={error} retry={load}/> : !tenantKey ? <EmptyState title={locale === "fa" ? "فضای کار انتخاب نشده" : "No workspace selected"} description={locale === "fa" ? "ابتدا فضای کار را انتخاب کنید." : "Select a workspace before loading automations."}/> : latest.length ? <div className="automation-catalog">{latest.map(flow => { const last = runs.find(run => run.automationFlowKey === flow.flowKey); return <Link href={`/automations/${encodeURIComponent(flow.flowKey)}?version=${flow.version}`} key={`${flow.flowKey}-${flow.version}`}><div><strong>{flow.name}</strong><code dir="ltr">{flow.flowKey}</code></div><span>v{flow.version}</span><StatusBadge tone={flow.active ? "success" : "neutral"}>{flow.lifecycleStatus}</StatusBadge><span>{flow.nextScheduledAt ? new Date(flow.nextScheduledAt).toLocaleString(locale) : locale === "fa" ? "بدون زمان‌بندی" : "No schedule"}</span><span>{canOperate ? (last ? last.status : locale === "fa" ? "اجرا نشده" : "Not run") : (locale === "fa" ? "فقط طراحی" : "Designer only")}</span></Link>; })}</div> : <EmptyState title={locale === "fa" ? "اتوماسیونی نیست" : "No automations"} description={locale === "fa" ? "سرویس برای این محدوده فلو برنگرداند." : "The service returned no flows for this scope."} action={canManage ? <Link className="primary-pill" href="/automations/new">{locale === "fa" ? "ایجاد اتوماسیون" : "Create automation"}</Link> : undefined}/>}
  </PanelShell>;
}
