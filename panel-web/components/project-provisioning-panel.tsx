"use client";

import { useEffect, useState } from "react";
import { listProvisioningRuns, provisionClientDraft } from "@/lib/platform-api";
import type { ProvisioningRun } from "@/lib/types";

type ProjectProvisioningPanelProps = {
  draftId: string;
};

export function ProjectProvisioningPanel({ draftId }: ProjectProvisioningPanelProps) {
  const [runs, setRuns] = useState<ProvisioningRun[]>([]);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function refreshRuns() {
    try {
      setRuns(await listProvisioningRuns(draftId));
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load provisioning runs");
    }
  }

  useEffect(() => {
    refreshRuns();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draftId]);

  async function provision() {
    setLoading(true);
    setStatus(null);
    try {
      const run = await provisionClientDraft(draftId);
      setRuns((current) => [run, ...current.filter((item) => item.runId !== run.runId)]);
      setStatus(`Provisioning run ${run.runId} is ${run.status}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Provisioning failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="panel rail">
      <div className="editor-toolbar">
        <div>
          <p className="section-title">Provisioning</p>
          <div className="meta">Executes the draft through `ai-orchestrator-service` provisioning runs.</div>
        </div>
        <button type="button" className="btn" onClick={provision} disabled={loading}>
          {loading ? "Provisioning..." : "Provision draft"}
        </button>
      </div>
      {status ? <p className="muted">{status}</p> : null}
      <div className="draft-list">
        {runs.map((run) => (
          <div key={run.runId} className="draft-item">
            <strong>
              <span>{run.runId}</span>
              <span className="muted">{run.status}</span>
            </strong>
            <span className="muted">{run.stepResults?.length ?? 0} provisioning steps</span>
            <span className="muted">{run.result?.deliveryEndpoints?.length ?? 0} delivery endpoints</span>
          </div>
        ))}
      </div>
    </section>
  );
}
