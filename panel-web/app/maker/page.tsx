import { AppShell } from "@/components/app-shell";
import { blueprintCards, makerLanes } from "@/lib/workspace-roadmap";

export default function MakerPage() {
  return (
    <AppShell
      title="Maker Panel"
      subtitle="Mobile-first workspace for manually or AI-assisted editing of app structure."
    >
      <div className="studio-grid">
        <section className="panel rail">
          <p className="section-title">Structure editor roadmap</p>
          <div className="draft-list">
            {makerLanes.slice(0, 3).map((lane) => (
              <div key={lane.title} className="draft-item">
                <strong>
                  <span>{lane.title}</span>
                  <span className="muted">{lane.fa}</span>
                </strong>
                <span className="muted">{lane.description}</span>
                <div className="chip-row">
                  {lane.services.map((service) => (
                    <span key={service} className="tag">
                      {service}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Blueprint drafts</p>
            <div className="timeline">
              {blueprintCards.map((item, index) => (
                <div key={item} className="timeline-step">
                  <strong>{index + 1}. Ready draft</strong>
                  <span>{item}</span>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>
    </AppShell>
  );
}
