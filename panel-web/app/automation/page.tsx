"use client";

import { useState } from "react";
import { AppShell } from "@/components/app-shell";
import {
  cancelAutomationExecution,
  getAutomationExecution,
  listAutomationFlows,
  listBatchRuns,
  saveAutomationFlow,
  saveBatchDefinition,
  startAutomationExecution,
  startBatchRun,
  transitionAutomationFlow
} from "@/lib/service-api";
import type { AutomationExecution, BatchRun } from "@/lib/types";

export default function AutomationPage() {
  const [automationFlowKey, setAutomationFlowKey] = useState("welcome-sequence");
  const [blockKey, setBlockKey] = useState("notify-and-tag");
  const [executionMode, setExecutionMode] = useState<"SYNC" | "ASYNC">("SYNC");
  const [managedObjectId, setManagedObjectId] = useState("panel-managed-object");
  const [idempotencyKey, setIdempotencyKey] = useState(`panel-${Date.now().toString(36)}`);
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [inputJson, setInputJson] = useState('{\n  "customerKey": "guest-demo"\n}');
  const [inlineFlowJson, setInlineFlowJson] = useState('{\n  "type": "MAP_OUTPUT",\n  "output": {\n    "accepted": true,\n    "screeningRoute": "FAST_TRACK"\n  }\n}');
  const [execution, setExecution] = useState<AutomationExecution | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [batchDefinitionKey, setBatchDefinitionKey] = useState("morning-customer-sync");
  const [scheduleFlowKey, setScheduleFlowKey] = useState("morning-customer-sync-schedule");
  const [cron, setCron] = useState("0 0 8 * * *");
  const [timezone, setTimezone] = useState("Asia/Tehran");
  const [batchRunKey, setBatchRunKey] = useState(new Date().toISOString().slice(0, 10));
  const [batchSpecJson, setBatchSpecJson] = useState(`{
  "source": {
    "url": "https://source.example.com/api/customers",
    "itemsPath": "content",
    "pageParameter": "page",
    "sizeParameter": "size",
    "pageSize": 200,
    "bearerTokenEnvironmentVariable": "SOURCE_API_TOKEN"
  },
  "destination": {
    "url": "https://target.example.com/api/customers/upsert",
    "method": "POST",
    "itemKeyPath": "externalId",
    "bearerTokenEnvironmentVariable": "TARGET_API_TOKEN"
  },
  "fieldMappings": {
    "externalId": "id",
    "name": "name"
  },
  "chunkSize": 200,
  "retryLimit": 5,
  "skipLimit": 100
}`);
  const [batchRuns, setBatchRuns] = useState<BatchRun[]>([]);

  async function start() {
    setLoading(true);
    setStatus(null);
    try {
      const result = await startAutomationExecution({
        blockKey,
        automationFlowKey,
        flowKey: automationFlowKey,
        executionMode,
        failurePolicy: "CONTINUE",
        correlationKey: `${managedObjectId}:${blockKey}`,
        managedObjectId,
        idempotencyKey,
        tenantKey,
        siteKey,
        input: inputJson.trim() ? JSON.parse(inputJson) : {},
        variables: inputJson.trim() ? JSON.parse(inputJson) : {},
        inlineFragment: inlineFlowJson.trim() ? JSON.parse(inlineFlowJson) : {},
        inlineFlow: inlineFlowJson.trim() ? JSON.parse(inlineFlowJson) : {},
        context: {
          source: "panel-automation-builder",
          managedObjectId
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

  async function saveBatch() {
    setLoading(true);
    setStatus(null);
    try {
      await saveBatchDefinition({
        definitionKey: batchDefinitionKey,
        title: batchDefinitionKey,
        active: true,
        spec: JSON.parse(batchSpecJson)
      }, tenantKey, siteKey);
      setStatus(`Batch definition ${batchDefinitionKey} saved.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to save batch definition");
    } finally {
      setLoading(false);
    }
  }

  async function activateSchedule() {
    setLoading(true);
    setStatus(null);
    try {
      const existing = await listAutomationFlows(tenantKey, siteKey);
      const version = existing
        .filter((flow) => flow.flowKey === scheduleFlowKey)
        .reduce((latest, flow) => Math.max(latest, Number(flow.version ?? 0)), 0) + 1;
      const saved = await saveAutomationFlow({
        flowKey: scheduleFlowKey,
        version,
        name: `Schedule ${batchDefinitionKey}`,
        active: false,
        lifecycleStatus: "DRAFT",
        environment: "default",
        runtimeMode: "VARIABLES",
        entryNodeId: "schedule",
        nodes: [
          { id: "schedule", type: "SCHEDULE_TRIGGER", name: "Every morning", enabled: true, config: { cron, timezone } },
          {
            id: "batch",
            type: "RUN_BATCH_JOB",
            name: "Run durable batch",
            enabled: true,
            config: { definitionKey: batchDefinitionKey, runKey: "{{scheduledAt}}", pollSeconds: 15, resultPath: "batchResult" }
          },
          { id: "end", type: "END", name: "Done", enabled: true, config: {} }
        ],
        edges: [
          { id: "schedule-batch", fromNodeId: "schedule", toNodeId: "batch" },
          { id: "batch-end", fromNodeId: "batch", toNodeId: "end" }
        ]
      }, tenantKey, siteKey);
      const savedVersion = Number(saved.version ?? version);
      await transitionAutomationFlow(scheduleFlowKey, savedVersion, "SUBMIT", tenantKey, siteKey);
      await transitionAutomationFlow(scheduleFlowKey, savedVersion, "APPROVE", tenantKey, siteKey);
      await transitionAutomationFlow(scheduleFlowKey, savedVersion, "ACTIVATE", tenantKey, siteKey);
      setStatus(`Schedule ${scheduleFlowKey} activated with ${cron} (${timezone}).`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to activate schedule");
    } finally {
      setLoading(false);
    }
  }

  async function runBatchNow() {
    setLoading(true);
    setStatus(null);
    try {
      const result = await startBatchRun(batchDefinitionKey, batchRunKey, tenantKey, siteKey);
      setBatchRuns((current) => [result, ...current.filter((item) => item.id !== result.id)]);
      setStatus(`Batch run ${result.id} is ${result.status}. Reusing this run key will not create a duplicate.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to start batch run");
    } finally {
      setLoading(false);
    }
  }

  async function refreshBatchRuns() {
    setLoading(true);
    setStatus(null);
    try {
      setBatchRuns(await listBatchRuns(tenantKey, siteKey));
      setStatus("Batch run history refreshed.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load batch runs");
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
                <label>Execution mode</label>
                <select value={executionMode} onChange={(event) => setExecutionMode(event.target.value as "SYNC" | "ASYNC")}>
                  <option value="SYNC">SYNC</option>
                  <option value="ASYNC">ASYNC</option>
                </select>
              </div>
              <div className="field">
                <label>Managed object id</label>
                <input value={managedObjectId} onChange={(event) => setManagedObjectId(event.target.value)} />
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
              <label>Idempotency key</label>
              <input value={idempotencyKey} onChange={(event) => setIdempotencyKey(event.target.value)} />
            </div>
            <div className="field">
              <label>Input / variables JSON</label>
              <textarea value={inputJson} onChange={(event) => setInputJson(event.target.value)} />
            </div>
            <div className="field">
              <label>Inline flow JSON</label>
              <textarea value={inlineFlowJson} onChange={(event) => setInlineFlowJson(event.target.value)} />
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
      <section className="panel rail" style={{ marginTop: 20 }}>
        <p className="section-title">Durable scheduled ETL</p>
        <p className="muted-block">
          Save a paginated API-to-API batch, activate its six-field Spring cron schedule, and inspect committed, skipped, or failed record counts.
        </p>
        <div className="form-grid">
          <div className="field-grid">
            <div className="field">
              <label>Batch definition key</label>
              <input value={batchDefinitionKey} onChange={(event) => setBatchDefinitionKey(event.target.value)} />
            </div>
            <div className="field">
              <label>Schedule flow key</label>
              <input value={scheduleFlowKey} onChange={(event) => setScheduleFlowKey(event.target.value)} />
            </div>
          </div>
          <div className="field-grid">
            <div className="field">
              <label>Six-field cron</label>
              <input value={cron} onChange={(event) => setCron(event.target.value)} />
            </div>
            <div className="field">
              <label>Timezone</label>
              <input value={timezone} onChange={(event) => setTimezone(event.target.value)} />
            </div>
          </div>
          <div className="field">
            <label>Batch definition JSON</label>
            <textarea value={batchSpecJson} onChange={(event) => setBatchSpecJson(event.target.value)} style={{ minHeight: 320 }} />
          </div>
          <div className="field">
            <label>Manual run key</label>
            <input value={batchRunKey} onChange={(event) => setBatchRunKey(event.target.value)} />
          </div>
          <div className="hero-actions">
            <button type="button" className="btn" onClick={saveBatch} disabled={loading}>Save batch</button>
            <button type="button" className="btn" onClick={activateSchedule} disabled={loading}>Activate schedule</button>
            <button type="button" className="ghost-btn" onClick={runBatchNow} disabled={loading}>Run now</button>
            <button type="button" className="ghost-btn" onClick={refreshBatchRuns} disabled={loading}>Refresh runs</button>
          </div>
          <div className="list">
            {batchRuns.map((run) => (
              <div className="list-item" key={run.id}>
                <strong>{run.definitionKey} · {run.status}</strong>
                <span className="muted-block">
                  {run.runKey} · read {run.readCount} · wrote {run.writeCount} · skipped {run.skipCount}
                  {run.errorMessage ? ` · ${run.errorMessage}` : ""}
                </span>
              </div>
            ))}
          </div>
        </div>
      </section>
    </AppShell>
  );
}
