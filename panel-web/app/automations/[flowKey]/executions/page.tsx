"use client";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { useScopeAccess } from "@/components/scope-access-provider";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { listExecutions, type AutomationExecution } from "@/lib/automation-api";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";

export default function FlowExecutionsPage({ params }: { params: { flowKey: string } }) {
  const { tenantKey, siteKey, queryVersion } = useScopeAccess(); const { showToast } = useToast(); const scope = useMemo(() => ({ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }), [tenantKey, siteKey]);
  const [items, setItems] = useState<AutomationExecution[]>([]); const [loading, setLoading] = useState(true); const [error, setError] = useState<string | null>(null);
  const load = useCallback(() => { if (!tenantKey) return; setLoading(true); setError(null); listExecutions(params.flowKey, undefined, scope).then(setItems).catch(cause => { const { title, message } = describeApiError(cause, "Executions unavailable"); setError(message); showToast({ tone: "error", title, message }); }).finally(() => setLoading(false)); }, [params.flowKey, scope, tenantKey, showToast]);
  useEffect(load, [load, queryVersion]);
  return <PanelShell activeKey="automation" title="Flow executions" titleFa="اجراهای فلو" subtitle={params.flowKey} subtitleFa={params.flowKey}><div className="page-action-bar"><Link className="secondary-pill" href={`/automations/${encodeURIComponent(params.flowKey)}`}>Back to editor</Link></div>{loading ? <Skeleton height={320}/> : error ? <ErrorState title="Executions unavailable" description={error} retry={load}/> : items.length ? <div className="execution-list">{items.map(item => <Link key={item.executionId} href={`/automations/executions/${item.executionId}`}><strong>{item.executionId}</strong><StatusBadge tone={item.status === "FAILED" ? "danger" : item.status === "COMPLETED" ? "success" : "info"}>{item.status}</StatusBadge><time>{item.createdAt ? new Date(item.createdAt).toLocaleString() : "—"}</time></Link>)}</div> : <EmptyState title="No executions" description="The service returned no executions for this flow in the active scope."/>}</PanelShell>;
}
