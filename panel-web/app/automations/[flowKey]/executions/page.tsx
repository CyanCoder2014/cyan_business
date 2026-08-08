"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { listExecutions, type AutomationExecution } from "@/lib/automation-api";

export default function FlowExecutionsPage({ params }: { params: { flowKey: string } }) {
  const [items, setItems] = useState<AutomationExecution[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const load = () => { setLoading(true); setError(null); listExecutions(params.flowKey).then(setItems).catch((cause) => setError(cause instanceof Error ? cause.message : String(cause))).finally(() => setLoading(false)); };
  useEffect(load, [params.flowKey]);
  return <PanelShell activeKey="automation" title="Flow executions" titleFa="اجراهای فلو" subtitle={params.flowKey} subtitleFa={params.flowKey}><div className="page-action-bar"><Link className="secondary-pill" href={`/automations/${encodeURIComponent(params.flowKey)}`}>Back to editor</Link></div>{loading?<Skeleton height={320}/>:error?<ErrorState title="Executions unavailable" description={error} retry={load}/>:items.length?<div className="execution-list">{items.map((item)=><Link key={item.executionId} href={`/automations/executions/${item.executionId}`}><strong>{item.executionId}</strong><StatusBadge tone={item.status==="FAILED"?"danger":item.status==="COMPLETED"?"success":"info"}>{item.status}</StatusBadge><time>{item.createdAt?new Date(item.createdAt).toLocaleString():"—"}</time></Link>)}</div>:<EmptyState title="No executions" description="The service returned no executions for this flow in the active scope."/>}</PanelShell>;
}
