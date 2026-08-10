"use client";
import dynamic from "next/dynamic";
import { PanelShell } from "@/components/panel-shell";
const BpmDesigner = dynamic(() => import("@/components/bpm/bpm-designer").then(module => module.BpmDesigner), { ssr: false });
export default function NewBpm(){return <PanelShell activeKey="flows" title="New process" titleFa="فرایند جدید" subtitle="Define states, transitions, forms, actions, and automation bridges." subtitleFa="وضعیت‌ها، انتقال‌ها، فرم‌ها و اتصال اتوماسیون را تعریف کنید."><BpmDesigner/></PanelShell>}
