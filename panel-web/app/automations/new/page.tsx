"use client";
import dynamic from "next/dynamic";
import { PanelShell } from "@/components/panel-shell";
const AutomationBuilder = dynamic(() => import("@/components/automation/automation-builder").then(module => module.AutomationBuilder), { ssr: false });
export default function NewAutomationPage(){return <PanelShell activeKey="automation" title="New automation" titleFa="اتوماسیون جدید" subtitle="Build a validated workflow from runtime-supported nodes." subtitleFa="یک جریان معتبر با گره‌های پشتیبانی‌شده بسازید."><AutomationBuilder/></PanelShell>}
