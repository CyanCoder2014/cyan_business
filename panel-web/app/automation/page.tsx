"use client";

import { useState } from "react";
import { AppShell } from "@/components/app-shell";
import { cancelAutomationExecution, getAutomationExecution, startAutomationExecution } from "@/lib/service-api";
import type { AutomationExecution } from "@/lib/types";

export default function AutomationPage() {
  const [automationFlowKey, setAutomationFlowKey] = useState("welcome-sequence");
  const [blockKey, setBlockKey] = useState("notify-and-tag");
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [inputJson, setInputJson] = useState('{\n  "customerKey": "guest-demo"\n}');
  const [execution, setExecution] = useState<AutomationExecution | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function start() {
    setLoading(true);
    setStatus(null);
    try {
      const result = await startAutomationExecution({
        blockKey,
        automationFlowKey,
        executionMode: "SYNC",
        failurePolicy: "FAIL_FAST",
        correlationKey: `panel-${Date.now().toString(36)}`,
        tenantKey,
        siteKey,
        input: inputJson.trim() ? JSON.parse(inputJson) : {},
        context: {
          source: "panel-automation-builder"
        }
      });
      setExecution(result);
      setStatus(`Automation execution ${result.executionId ?? "started"} is ${result.status ?? "UNKNOWN"}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to start automation execution");
    } finally {
      setLoading(false);
    }
  }

  async function refresh() {
    if (!execution?.executionId) return;
    setLoading(true);
    setStatus(null);
    try {
      const result = await getAutomationExecution(execution.executionId);
      setExecution(result);
      setStatus(`Execution ${result.executionId ?? ""} refreshed.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to refresh automation execution");
    } finally {
      setLoading(false);
    }
  }

  async function cancel() {
    if (!execution?.executionId) return;
    setLoading(true);
    setStatus(null);
    try {
      const result = await cancelAutomationExecution(execution.executionId);
      setExecution(result);
      setStatus(`Execution ${result.executionId ?? ""} cancelled.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to cancel automation execution");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Automation Builder" subtitle="Provision and observe automation-orchestrator executions without dropping back to raw internal APIs.">
      <div className="studio-grid">
        <section className="panel rail">
          <div className="form-grid">
            <div className="field-grid">
              <div className="field">
                <label>Automation flow key</label>
                <input value={automationFlowKey} onChange={(event) => setAutomationFlowKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Block key</label>
                <input value={blockKey} onChange={(event) => setBlockKey(event.target.value)} />
              </div>
            </div>
            <div className="field-grid">
              <div className="field">
                <label>Tenant key</label>
                <input value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Site key</label>
                <input value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
              </div>
            </div>
            <div className="field">
              <label>Input JSON</label>
              <textarea value={inputJson} onChange={(event) => setInputJson(event.target.value)} />
            </div>
            <div className="hero-actions">
              <button type="button" className="btn" onClick={start} disabled={loading}>Start execution</button>
              <button type="button" className="ghost-btn" onClick={refresh} disabled={loading || !execution?.executionId}>Refresh</button>
              <button type="button" className="ghost-btn" onClick={cancel} disabled={loading || !execution?.executionId}>Cancel</button>
            </div>
            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>
        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Execution snapshot</p>
            <pre className="json-view">{JSON.stringify(execution, null, 2)}</pre>
          </section>
        </aside>
      </div>
    </AppShell>
  );
}
