"use client";
import { useCallback, useEffect, useState } from "react";
import dynamic from "next/dynamic";
import { PanelShell } from "@/components/panel-shell";
import { ErrorState, Skeleton } from "@/components/ui/primitives";
import { getFlow, type DynamicFlowDefinition } from "@/lib/bpm-api";
const BpmDesigner = dynamic(() => import("@/components/bpm/bpm-designer").then(module => module.BpmDesigner), { ssr: false });
export default function BpmDetail({ params }: { params: { flowKey: string } }) {
  const [flow, setFlow] = useState<DynamicFlowDefinition | null>(null);
  const [error, setError] = useState<string | null>(null);
  const load = useCallback(() => { setError(null); void getFlow(params.flowKey, {}).then(setFlow).catch(reason => setError(reason instanceof Error ? reason.message : String(reason))); }, [params.flowKey]);
  useEffect(() => { load(); }, [load]);
  return <PanelShell activeKey="flows" title="Process designer" titleFa="طراح فرایند" subtitle={params.flowKey} subtitleFa={params.flowKey}>{error ? <ErrorState title="Process unavailable" description={error} retry={load}/> : flow ? <BpmDesigner initial={flow}/> : <Skeleton height={680}/>}</PanelShell>;
}
