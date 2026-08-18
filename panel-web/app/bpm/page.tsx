"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { listAssignedManagedObjects, listFlows, listVisibleManagedObjects, type DynamicFlowDefinition } from "@/lib/bpm-api";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";

export default function BpmCatalog() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion, can } = useScopeAccess();
  const [flows, setFlows] = useState<DynamicFlowDefinition[]>([]);
  const [counts, setCounts] = useState({ assigned: 0, visible: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [runtimeWarning, setRuntimeWarning] = useState<string | null>(null);
  const scope = { tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined };
  const canOperate = can("operations:use") || can("bpm.transition");

  const load = async () => {
    if (!tenantKey) { setLoading(false); return; }
    setLoading(true); setError(null); setRuntimeWarning(null);
    try {
      setFlows(await listFlows(scope));
      if (canOperate) {
        const [assigned, visible] = await Promise.allSettled([listAssignedManagedObjects(scope), listVisibleManagedObjects(scope)]);
        setCounts({
          assigned: assigned.status === "fulfilled" ? assigned.value.length : 0,
          visible: visible.status === "fulfilled" ? visible.value.length : 0,
        });
        if (assigned.status === "rejected" || visible.status === "rejected") {
          setRuntimeWarning(locale === "fa" ? "آمار صف کار به‌طور کامل بارگیری نشد." : "Work queue totals could not be fully loaded.");
        }
      } else setCounts({ assigned: 0, visible: 0 });
    } catch (reason) { setError(reason instanceof Error ? reason.message : String(reason)); }
    finally { setLoading(false); }
  };

  useEffect(() => { void load(); }, [tenantKey, siteKey, queryVersion, canOperate]);

  return <PanelShell activeKey="flows" title="Business processes" titleFa="فرایندهای کسب‌وکار" subtitle="Design stateful workflows separately from operating assigned work." subtitleFa="فرایندها را طراحی و کارهای محول‌شده را جداگانه انجام دهید.">
    <div className="page-action-bar">
      <div>{canOperate ? <><span>{locale === "fa" ? `${counts.assigned} کار محول‌شده` : `${counts.assigned} assigned`}</span><span>{locale === "fa" ? `${counts.visible} قابل مشاهده` : `${counts.visible} visible`}</span></> : <span>{locale === "fa" ? "دسترسی طراحی" : "Designer access"}</span>}</div>
      <div>{canOperate ? <Link className="secondary-pill" href="/work">{locale === "fa" ? "صف کار" : "Work queue"}</Link> : null}<Link className="primary-pill" href="/bpm/new">{locale === "fa" ? "فرایند جدید" : "New process"}</Link></div>
    </div>
    {runtimeWarning ? <p className="operational-banner warning" role="status">{runtimeWarning}</p> : null}
    {loading ? <Skeleton height={300}/> : error ? <ErrorState title="BPM unavailable" description={error} retry={load}/> : !tenantKey ? <EmptyState title="No workspace selected" description="Select a workspace before loading BPM flows."/> : flows.length ? <div className="bpm-catalog">{flows.map(flow => <Link href={`/bpm/${encodeURIComponent(flow.flowKey)}`} key={`${flow.flowKey}-${flow.version}`}><div><strong>{flow.name}</strong><code>{flow.flowKey}</code></div><span>v{flow.version}</span><span>{flow.states.length} {locale === "fa" ? "وضعیت" : "states"}</span><StatusBadge tone={flow.active ? "success" : "neutral"}>{flow.lifecycleStatus ?? (flow.active ? "ACTIVE" : "DRAFT")}</StatusBadge></Link>)}</div> : <EmptyState title={locale === "fa" ? "فرایندی نیست" : "No processes"} description={locale === "fa" ? "سرویس فرایندی برای این محدوده برنگرداند." : "The service returned no BPM flows for this scope."}/>}
  </PanelShell>;
}
