import Link from "next/link";
import { AppShell } from "@/components/app-shell";
import { ProjectCards } from "@/components/project-cards";
import { seedDrafts } from "@/lib/draft-store";

export default function HomePage() {
  const drafts = seedDrafts();

  return (
    <AppShell
      title="Build client web apps from structured prompts, not raw glue code."
      subtitle="Generate apps, inspect drafts, and provision the platform from one control surface."
    >
      <div className="stats-row">
        <div className="stat">
          <strong>1 control plane</strong>
          <span>AI orchestrator, storefront, BPM, and service provisioning live behind one panel.</span>
        </div>
        <div className="stat">
          <strong>4 app types</strong>
          <span>Website, shop, CRM, and BPM-assisted mixed business apps are the core flows.</span>
        </div>
        <div className="stat">
          <strong>Draft first</strong>
          <span>Every generated app should be reviewable before execution and publication.</span>
        </div>
      </div>

      <div className="hero-grid">
        <section className="panel rail">
          <h2>Control the build lifecycle</h2>
          <div className="rail-item">
            <strong>Prompt studio</strong>
            <span>Capture business goals, constraints, and answers before hitting the AI endpoint.</span>
          </div>
          <div className="rail-item">
            <strong>Generated DSL</strong>
            <span>Preview entities, routes, flows, and delivery endpoints as structured JSON.</span>
          </div>
          <div className="rail-item">
            <strong>Provisioning trace</strong>
            <span>See what was created, what needs review, and what still requires manual actions.</span>
          </div>
          <div className="hero-actions">
            <Link className="btn" href="/projects/new">
              Open app studio
            </Link>
            <Link className="ghost-btn" href="/projects">
              Inspect a draft project
            </Link>
          </div>
        </section>

        <aside className="panel rail">
          <h2>Current direction</h2>
          <div className="timeline">
            <div className="timeline-step">
              <strong>1. Generate</strong>
              <span>Prompt the AI orchestrator with business intent.</span>
            </div>
            <div className="timeline-step">
              <strong>2. Review</strong>
              <span>Keep the draft in the panel before execution.</span>
            </div>
            <div className="timeline-step">
              <strong>3. Provision</strong>
              <span>Push structured entities, routes, and flows into services.</span>
            </div>
            <div className="timeline-step">
              <strong>4. Publish</strong>
              <span>Expose storefront routes and bot endpoints for the client.</span>
            </div>
          </div>
        </aside>
      </div>

      <section style={{ padding: "24px" }}>
        <p className="section-title">Seeded drafts</p>
        <ProjectCards drafts={drafts} />
        <p className="footer-note">
          The panel currently uses browser-local draft seeds and is wired to the orchestrator endpoint for live generation.
        </p>
      </section>
    </AppShell>
  );
}
