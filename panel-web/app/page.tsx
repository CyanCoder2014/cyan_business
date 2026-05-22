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
          <strong>1 maker panel</strong>
          <span>AI chat, manual maker, data manager, flows, and client channels live behind one panel.</span>
        </div>
        <div className="stat">
          <strong>6 channels</strong>
          <span>Website, PWA, shop, CRM portal, Telegram bot, and Bale bot are the launch focus.</span>
        </div>
        <div className="stat">
          <strong>2 languages</strong>
          <span>Farsi/English controls and light/dark UI are first-class panel requirements.</span>
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
              Open AI studio
            </Link>
            <Link className="ghost-btn" href="/roadmap">
              Resume roadmap
            </Link>
            <Link className="ghost-btn" href="/maker">
              Open maker panel
            </Link>
            <Link className="ghost-btn" href="/integrations">
              Manage apps and bots
            </Link>
          </div>
        </section>

        <aside className="panel rail">
          <h2>Current direction</h2>
          <div className="timeline">
            <div className="timeline-step">
              <strong>1. Visual website/page builder</strong>
              <span>Push the site builder from record publisher to real preview, theme reuse, and publish controls.</span>
            </div>
            <div className="timeline-step">
              <strong>2. Outbound Telegram/Bale messaging</strong>
              <span>Complete secure send pipelines on top of the existing webhook and session groundwork.</span>
            </div>
            <div className="timeline-step">
              <strong>3. Advanced form/flow builder</strong>
              <span>Unify entity definitions, BPM states, and automation actions into one operator path.</span>
            </div>
            <div className="timeline-step">
              <strong>4. Test harness and readiness gate</strong>
              <span>Block market claims until panel, gateway, storefront, bot, and multilingual checks pass.</span>
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
