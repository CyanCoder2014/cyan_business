"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { ProjectProvisioningPanel } from "@/components/project-provisioning-panel";
import { getClientDraft, listConversationSessions } from "@/lib/platform-api";
import type { AiConversationSession, ClientAppDraft } from "@/lib/types";

export default function ProjectPage() {
  const params = useParams<{ projectId: string }>();
  const projectId = params.projectId;
  const [draft, setDraft] = useState<ClientAppDraft | null>(null);
  const [sessions, setSessions] = useState<AiConversationSession[]>([]);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    Promise.allSettled([
      getClientDraft(projectId),
      listConversationSessions({ draftId: projectId })
    ]).then(([draftResult, sessionsResult]) => {
      if (draftResult.status === "fulfilled") {
        setDraft(draftResult.value);
      } else {
        setStatus(draftResult.reason instanceof Error ? draftResult.reason.message : "Project draft could not be loaded.");
      }
      if (sessionsResult.status === "fulfilled") {
        setSessions(sessionsResult.value);
      }
    });
  }, [projectId]);

  const summary = useMemo(() => {
    const dsl = draft?.resolvedDsl;
    return {
      routes: dsl?.routes.length ?? 0,
      entities: dsl?.entities.length ?? 0,
      flows: dsl?.flows.length ?? 0,
      publicApis: dsl?.delivery.publicApis.length ?? 0,
      botApis: dsl?.delivery.botApis.length ?? 0
    };
  }, [draft]);

  if (!draft) {
    return (
      <AppShell title="Project workspace" subtitle="Project workspace, draft state, and delivery endpoints.">
        <section className="panel rail">
          <p className="section-title">Draft summary</p>
          <p className="muted">{status ?? "Loading draft..."}</p>
        </section>
      </AppShell>
    );
  }

  return (
    <AppShell title={draft.title} subtitle="Project workspace, draft state, and delivery endpoints.">
      <div className="studio-grid">
        <section className="panel rail">
          <p className="section-title">Draft summary</p>
          <div className="timeline">
            <div className="timeline-step">
              <strong>Prompt</strong>
              <span>{draft.latestIntent}</span>
            </div>
            <div className="timeline-step">
              <strong>Scope</strong>
              <span>
                {draft.tenantKey} / {draft.siteKey}
              </span>
            </div>
            <div className="timeline-step">
              <strong>Status</strong>
              <span>{draft.status}</span>
            </div>
            <div className="timeline-step">
              <strong>Capabilities</strong>
              <span>{draft.resolvedDsl.app.capabilities?.join(", ") ?? "n/a"}</span>
            </div>
            <div className="timeline-step">
              <strong>Updated</strong>
              <span>{draft.updatedAt ?? "—"}</span>
            </div>
            <div className="timeline-step">
              <strong>Pending questions</strong>
              <span>{draft.pendingQuestions.length}</span>
            </div>
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Delivery</p>
            <div className="draft-list">
              {draft.resolvedDsl.delivery.publicApis.map((api) => (
                <div key={api} className="draft-item">
                  <strong>Public API</strong>
                  <span className="muted">{api}</span>
                </div>
              ))}
              {draft.resolvedDsl.delivery.botApis.map((api) => (
                <div key={api} className="draft-item">
                  <strong>Bot API</strong>
                  <span className="muted">{api}</span>
                </div>
              ))}
              {!draft.resolvedDsl.delivery.publicApis.length && !draft.resolvedDsl.delivery.botApis.length ? (
                <div className="draft-item">
                  <strong>No delivery endpoints</strong>
                  <span className="muted">This draft has not produced endpoint metadata yet.</span>
                </div>
              ) : null}
            </div>
          </section>

          <section className="panel rail">
            <p className="section-title">Manual actions</p>
            {draft.manualActions.length ? (
              <ul className="result-list">
                {draft.manualActions.map((action) => (
                  <li key={action}>{action}</li>
                ))}
              </ul>
            ) : (
              <p className="muted">No manual actions for this draft.</p>
            )}
          </section>

          <section className="panel rail">
            <p className="section-title">Conversation sessions</p>
            <div className="draft-list">
              {sessions.map((session) => (
                <div key={session.sessionId} className="draft-item">
                  <strong>
                    <span>{session.sessionId}</span>
                    <span className="muted">{session.status}</span>
                  </strong>
                  <span className="muted">{session.latestPrompt ?? session.latestQuestion ?? "No prompt yet"}</span>
                  <Link href={`/bot/${session.sessionId}`} className="muted">Open session</Link>
                </div>
              ))}
              {!sessions.length ? <p className="muted">No conversation sessions linked to this draft.</p> : null}
            </div>
          </section>
        </aside>
      </div>

      <section style={{ padding: "24px", paddingTop: 0 }}>
        <div className="summary-grid">
          <div className="mini-card"><strong>{summary.routes}</strong><span className="muted-block">Routes</span></div>
          <div className="mini-card"><strong>{summary.entities}</strong><span className="muted-block">Entities</span></div>
          <div className="mini-card"><strong>{summary.flows}</strong><span className="muted-block">Flows</span></div>
          <div className="mini-card"><strong>{summary.publicApis + summary.botApis}</strong><span className="muted-block">Delivery APIs</span></div>
        </div>
      </section>

      <section style={{ padding: "24px" }}>
        <ProjectProvisioningPanel draftId={draft.draftId} />
      </section>

      <section style={{ padding: "24px", paddingTop: 0 }}>
        <p className="section-title">DSL</p>
        <pre className="json-view">{JSON.stringify(draft.resolvedDsl, null, 2)}</pre>
      </section>
    </AppShell>
  );
}
