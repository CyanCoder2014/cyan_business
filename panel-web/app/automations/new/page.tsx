"use client";
import { PanelShell } from "@/components/panel-shell";
import { AutomationBuilder } from "@/components/automation/automation-builder";
export default function NewAutomationPage(){return <PanelShell activeKey="automation" title="New automation" titleFa="اتوماسیون جدید" subtitle="Build a validated workflow from runtime-supported nodes." subtitleFa="یک جریان معتبر با گره‌های پشتیبانی‌شده بسازید."><AutomationBuilder/></PanelShell>}
