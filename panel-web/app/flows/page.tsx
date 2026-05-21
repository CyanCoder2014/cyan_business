import { AppShell } from "@/components/app-shell";

const flowSteps = [
  "Design BPM states, transitions, active forms, and target submit URLs.",
  "Attach notification, API-call, and assignment actions to transitions.",
  "Route durable integration events through event-service for downstream automation.",
  "Review provisioning runs before publishing workflow changes to users and bots."
];

export default function FlowsPage() {
  return (
    <AppShell title="Flow Builder" subtitle="Edit BPM, automation, and evented side effects for generated apps.">
      <div className="hero-grid">
        <section className="panel rail">
          <p className="section-title">Automation editor</p>
          <div className="timeline">
            {flowSteps.map((step, index) => (
              <div key={step} className="timeline-step">
                <strong>Step {index + 1}</strong>
                <span>{step}</span>
              </div>
            ))}
          </div>
        </section>
        <aside className="panel rail">
          <p className="section-title">Owning services</p>
          <div className="chip-row">
            {["bpm-service", "automation-orchestrator-service", "event-service", "notification-service", "ai-orchestrator-service"].map((service) => (
              <span key={service} className="tag">
                {service}
              </span>
            ))}
          </div>
        </aside>
      </div>
    </AppShell>
  );
}
