"use client";
import { useEffect,useState } from "react";
import { useSearchParams } from "next/navigation";
import dynamic from "next/dynamic";
import { PanelShell } from "@/components/panel-shell";
const AutomationBuilder = dynamic(() => import("@/components/automation/automation-builder").then(module => module.AutomationBuilder), { ssr: false });
import { ErrorState,Skeleton } from "@/components/ui/primitives";
import { getActiveAutomationFlow,getAutomationFlow,type AutomationFlow } from "@/lib/automation-api";
export default function AutomationPage({params}:{params:{flowKey:string}}){const search=useSearchParams();const [flow,setFlow]=useState<AutomationFlow|null>(null);const [error,setError]=useState<string|null>(null);const load=()=>{setError(null);const version=Number(search.get("version"));(Number.isFinite(version)&&version>0?getAutomationFlow(params.flowKey,version):getActiveAutomationFlow(params.flowKey)).then(setFlow).catch(e=>setError(e instanceof Error?e.message:String(e)))};useEffect(load,[params.flowKey,search]);return <PanelShell activeKey="automation" title="Automation editor" titleFa="ویرایشگر اتوماسیون" subtitle="Edit the graph, policies, lifecycle, and test execution." subtitleFa="گراف، سیاست‌ها، چرخه عمر و اجرای آزمایشی را مدیریت کنید.">{error?<ErrorState title="Automation unavailable" description={error} retry={load}/>:flow?<AutomationBuilder initial={flow}/>:<Skeleton height={680}/>}</PanelShell>}
