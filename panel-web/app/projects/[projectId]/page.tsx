import { notFound } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { seedDrafts } from "@/lib/draft-store";
import { getProjectDraft } from "@/lib/project-registry";

type ProjectPageProps = {
  params: {
    projectId: string;
  };
};

export default async function ProjectPage({ params }: ProjectPageProps) {
  const { projectId } = params;
  const draft = (await getProjectDraft(projectId)) ?? seedDrafts().find((item) => item.id === projectId);

  if (!draft) {
    notFound();
  }

  return (
    <AppShell title={draft.title} subtitle="Project workspace, draft state, and delivery endpoints.">
      <div className="studio-grid">
        <section className="panel rail">
          <p className="section-title">Draft summary</p>
          <div className="timeline">
            <div className="timeline-step">
              <strong>Prompt</strong>
              <span>{draft.prompt}</span>
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
              <span>{draft.dsl.app.capabilities?.join(", ") ?? "n/a"}</span>
            </div>
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Delivery</p>
            <div className="draft-list">
              {draft.dsl.delivery.publicApis.map((api) => (
                <div key={api} className="draft-item">
                  <strong>Public API</strong>
                  <span className="muted">{api}</span>
                </div>
              ))}
              {draft.dsl.delivery.botApis.map((api) => (
                <div key={api} className="draft-item">
                  <strong>Bot API</strong>
                  <span className="muted">{api}</span>
                </div>
              ))}
            </div>
          </section>

          <section className="panel rail">
            <p className="section-title">Manual actions</p>
            {draft.dsl.manualActions.length ? (
              <ul className="result-list">
                {draft.dsl.manualActions.map((action) => (
                  <li key={action}>{action}</li>
                ))}
              </ul>
            ) : (
              <p className="muted">No manual actions for this draft.</p>
            )}
          </section>
        </aside>
      </div>

      <section style={{ padding: "24px" }}>
        <p className="section-title">DSL</p>
        <pre className="json-view">{JSON.stringify(draft.dsl, null, 2)}</pre>
      </section>
    </AppShell>
  );
}
