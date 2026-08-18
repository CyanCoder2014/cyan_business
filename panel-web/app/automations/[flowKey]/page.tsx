"use client";

import dynamic from "next/dynamic";
import { useSearchParams } from "next/navigation";
import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { useScopeAccess } from "@/components/scope-access-provider";
import { ErrorState, Skeleton } from "@/components/ui/primitives";
import { getActiveAutomationFlow, getAutomationFlow, type AutomationFlow } from "@/lib/automation-api";

const AutomationBuilder = dynamic(() => import("@/components/automation/automation-builder").then(module => module.AutomationBuilder), { ssr: false });

export default function AutomationPage({ params }: { params: { flowKey: string } }) {
  const search = useSearchParams();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }), [tenantKey, siteKey]);
  const [flow, setFlow] = useState<AutomationFlow | null>(null);
  const [error, setError] = useState<string | null>(null);
  const load = useCallback(() => {
    if (!tenantKey) return;
    setError(null);
    const version = Number(search.get("version"));
    (Number.isFinite(version) && version > 0
      ? getAutomationFlow(params.flowKey, version, scope)
      : getActiveAutomationFlow(params.flowKey, scope))
      .then(setFlow).catch(cause => setError(cause instanceof Error ? cause.message : String(cause)));
  }, [params.flowKey, scope, search, tenantKey]);
  useEffect(load, [load, queryVersion]);
  return <PanelShell activeKey="automation" title="Automation editor" titleFa="ویرایشگر اتوماسیون" subtitle="Edit the graph, policies, lifecycle, and test execution." subtitleFa="گراف، سیاست‌ها، چرخه عمر و اجرای آزمایشی را مدیریت کنید.">{error ? <ErrorState title="Automation unavailable" description={error} retry={load}/> : flow ? <AutomationBuilder initial={flow}/> : <Skeleton height={680}/>}</PanelShell>;
}
