import Link from "next/link";
import { notFound } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { getBotSession } from "@/lib/bot-session-registry";

type BotSessionPageProps = {
  params: {
    sessionId: string;
  };
};

export default async function BotSessionPage({ params }: BotSessionPageProps) {
  const session = await getBotSession(params.sessionId);

  if (!session) {
    notFound();
  }

  return (
    <AppShell title={session.title} subtitle="Persistent bot conversation thread.">
      <div className="studio-grid">
        <section className="panel rail">
          <p className="section-title">Session</p>
          <div className="timeline">
            <div className="timeline-step">
              <strong>Channel</strong>
              <span>{session.channel}</span>
            </div>
            <div className="timeline-step">
              <strong>Status</strong>
              <span>{session.status}</span>
            </div>
            <div className="timeline-step">
              <strong>Scope</strong>
              <span>
                {session.tenantKey} / {session.siteKey}
              </span>
            </div>
            <div className="timeline-step">
              <strong>Linked draft</strong>
              <span>{session.draftId ?? "none"}</span>
            </div>
          </div>

          <div className="hero-actions" style={{ marginTop: 20 }}>
            <Link href="/bot" className="btn">
              Resume in bot studio
            </Link>
            <Link href="/projects" className="ghost-btn">
              Open project registry
            </Link>
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Conversation</p>
            <div className="draft-list">
              {session.messages.map((message) => (
                <div key={message.id} className="draft-item">
                  <strong>
                    <span>{message.role}</span>
                    <span className="muted">{new Date(message.createdAt).toLocaleString()}</span>
                  </strong>
                  <span className="muted">{message.content}</span>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>

      <section style={{ padding: "24px" }}>
        <p className="section-title">Answers</p>
        <pre className="json-view">{JSON.stringify(session.answers, null, 2)}</pre>
      </section>
    </AppShell>
  );
}
