"use client";
import dynamic from "next/dynamic";
import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
const BpmDesigner = dynamic(() => import("@/components/bpm/bpm-designer").then(module => module.BpmDesigner), { ssr: false });
export default function NewBpm(){const [query,setQuery]=useState("");useEffect(()=>setQuery(window.location.search),[]);const params=new URLSearchParams(query);return <PanelShell activeKey="flows" title="New process" titleFa="فرایند جدید" subtitle="Define states, transitions, forms, actions, and automation bridges." subtitleFa="وضعیت‌ها، انتقال‌ها، فرم‌ها و اتصال اتوماسیون را تعریف کنید."><BpmDesigner key={query} prefill={{entityService:params.get("entityService")??undefined,entityKey:params.get("entityKey")??undefined}}/></PanelShell>}
