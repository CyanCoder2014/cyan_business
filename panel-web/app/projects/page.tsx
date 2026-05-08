import Link from "next/link";
import { AppShell } from "@/components/app-shell";
import { listProjectDrafts } from "@/lib/project-registry";

export default async function ProjectsPage() {
  const drafts = await listProjectDrafts();

  return (
    <AppShell
      title="Project Registry"
      subtitle="Backend-owned draft list for reopening app blueprints without regenerating every time."
    >
      <section style={{ padding: "24px" }}>
        <div className="editor-toolbar">
          <div>
            <p className="section-title">Saved projects</p>
            <div className="meta">Stored in the panel backend at `/api/projects`.</div>
          </div>
          <Link href="/projects/new" className="btn">
            New project
          </Link>
        </div>

        <div className="draft-list">
          {drafts.map((draft) => (
            <Link key={draft.id} href={`/projects/${draft.id}`} className="draft-item">
              <strong>
                <span>{draft.title}</span>
                <span className="muted">{draft.status}</span>
              </strong>
              <span className="muted">{draft.prompt}</span>
              <span className="muted">
                {draft.tenantKey} / {draft.siteKey}
              </span>
              <span className="muted">{draft.dsl.app.type}</span>
            </Link>
          ))}
        </div>
      </section>
    </AppShell>
  );
}
