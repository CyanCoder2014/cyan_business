"use client";

import { useState } from "react";
import { AppShell } from "@/components/app-shell";
import { listBotIntegrations, listBotMessages, listClientDrafts } from "@/lib/platform-api";
import { listActionMetadata, listFlows, listManagedObjects } from "@/lib/bpm-api";
import { renderStorefrontRoute, resolveStorefrontRoute } from "@/lib/storefront-api";

type HarnessCheck = {
  key: string;
  title: string;
  status: "idle" | "running" | "passed" | "failed";
  detail?: string;
  payload?: unknown;
};

const initialChecks: HarnessCheck[] = [
  { key: "drafts", title: "AI drafts registry", status: "idle" },
  { key: "storefront-resolve", title: "Storefront resolve", status: "idle" },
  { key: "storefront-render", title: "Storefront render", status: "idle" },
  { key: "bpm-flows", title: "BPM flows", status: "idle" },
  { key: "bpm-actions", title: "BPM metadata", status: "idle" },
  { key: "bpm-managed-objects", title: "Managed objects", status: "idle" },
  { key: "bot-integrations", title: "Bot integrations", status: "idle" },
  { key: "bot-messages", title: "Bot outbound deliveries", status: "idle" }
];

export default function QaPage() {
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [path, setPath] = useState("/");
  const [checks, setChecks] = useState<HarnessCheck[]>(initialChecks);
  const [running, setRunning] = useState(false);

  function updateCheck(key: string, patch: Partial<HarnessCheck>) {
    setChecks((current) => current.map((item) => (item.key === key ? { ...item, ...patch } : item)));
  }

  async function runCheck(
    key: string,
    runner: () => Promise<unknown>,
    success: (payload: unknown) => string
  ) {
    updateCheck(key, { status: "running", detail: "Running...", payload: undefined });
    try {
      const payload = await runner();
      updateCheck(key, { status: "passed", detail: success(payload), payload });
    } catch (error) {
      updateCheck(key, {
        status: "failed",
        detail: error instanceof Error ? error.message : "Request failed",
        payload: undefined
      });
    }
  }

  async function runHarness() {
    setRunning(true);
    setChecks(initialChecks);
    try {
      await runCheck("drafts", () => listClientDrafts({ tenantKey, siteKey }), (payload) => `${Array.isArray(payload) ? payload.length : 0} drafts returned.`);
      await runCheck("storefront-resolve", () => resolveStorefrontRoute(path, { tenantKey, siteKey }), () => `Route resolved for ${path}.`);
      await runCheck("storefront-render", () => renderStorefrontRoute(path, { tenantKey, siteKey }), () => `Rendered storefront payload loaded for ${path}.`);
      await runCheck("bpm-flows", () => listFlows({ tenantKey, siteKey }), (payload) => `${Array.isArray(payload) ? payload.length : 0} flows returned.`);
      await runCheck("bpm-actions", () => listActionMetadata({ tenantKey, siteKey }), (payload) => `${Array.isArray(payload) ? payload.length : 0} BPM action templates returned.`);
      await runCheck("bpm-managed-objects", () => listManagedObjects({ tenantKey, siteKey }), (payload) => `${Array.isArray(payload) ? payload.length : 0} managed objects returned.`);
      await runCheck("bot-integrations", () => listBotIntegrations({ tenantKey, siteKey }), (payload) => `${Array.isArray(payload) ? payload.length : 0} bot integrations returned.`);
      await runCheck("bot-messages", () => listBotMessages({ tenantKey, siteKey }), (payload) => `${Array.isArray(payload) ? payload.length : 0} outbound deliveries returned.`);
    } finally {
      setRunning(false);
    }
  }

  return (
    <AppShell title="Integration Test Panel" subtitle="Run platform smoke checks for storefront, BPM, AI draft registry, and bot channels from one workspace.">
      <div className="studio-grid">
        <section className="panel rail">
          <div className="editor-toolbar">
            <div>
              <p className="section-title">Harness scope</p>
              <div className="meta">Uses the same gateway and endpoint/public contracts that the panel relies on.</div>
            </div>
            <button type="button" className="btn" onClick={runHarness} disabled={running}>
              {running ? "Running..." : "Run smoke harness"}
            </button>
          </div>

          <div className="form-grid">
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
              <label>Storefront path</label>
              <input value={path} onChange={(event) => setPath(event.target.value)} />
            </div>

            <div className="result-grid">
              {checks.map((check) => (
                <div key={check.key} className="result-card">
                  <h4>{check.title}</h4>
                  <p className="muted">{check.status.toUpperCase()}</p>
                  <p className="muted">{check.detail ?? "Not run yet."}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        <aside className="sidebar">
          {checks.map((check) => (
            <section key={check.key} className="panel rail">
              <p className="section-title">{check.title}</p>
              <pre className="json-view">{JSON.stringify(check.payload, null, 2)}</pre>
            </section>
          ))}
        </aside>
      </div>
    </AppShell>
  );
}
