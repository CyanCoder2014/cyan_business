import Link from "next/link";
import { AppShell } from "@/components/app-shell";
import { productRoadmap } from "@/lib/product-roadmap";

const statusLabels = {
  prototype: "Prototype in repo",
  integration: "Integration work active",
  hardening: "Hardening",
  planned: "Planned gate"
} as const;

export default function RoadmapPage() {
  return (
    <AppShell
      title="Resume roadmap 1, 2, 3, 4 from real repo surfaces."
      subtitle="Track the four launch-critical workstreams against the services and panel routes that already exist."
    >
      <div className="roadmap-stack">
        {productRoadmap.map((track) => (
          <section key={track.id} className="roadmap-card">
            <div className="roadmap-topline">
              <div>
                <p className="eyebrow">Track {track.order}</p>
                <h3>{track.title}</h3>
              </div>
              <div className="roadmap-status">{statusLabels[track.status]}</div>
            </div>

            <p className="lede">{track.summary}</p>

            <div className="roadmap-grid">
              <div className="roadmap-column">
                <p className="section-title">Phases</p>
                {track.phases.map((phase) => (
                  <div key={phase.title} className="roadmap-item">
                    <strong>{phase.title}</strong>
                    <span>{phase.outcome}</span>
                  </div>
                ))}
              </div>

              <div className="roadmap-column">
                <p className="section-title">Dependencies</p>
                {track.dependencies.map((dependency) => (
                  <div key={dependency} className="roadmap-item">
                    <strong>{dependency}</strong>
                  </div>
                ))}

                <p className="section-title" style={{ marginTop: 18 }}>Launch gate</p>
                {track.launchGate.map((item) => (
                  <div key={item} className="roadmap-item">
                    <span>{item}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="roadmap-meta">
              <div>
                <p className="section-title">Panel routes</p>
                <div className="chip-row">
                  {track.routes.map((route) => (
                    <Link key={route} className="chip active" href={route}>
                      {route}
                    </Link>
                  ))}
                </div>
              </div>

              <div>
                <p className="section-title">Owning services</p>
                <div className="chip-row">
                  {track.services.map((service) => (
                    <span key={service} className="chip">
                      {service}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </section>
        ))}
      </div>
    </AppShell>
  );
}
