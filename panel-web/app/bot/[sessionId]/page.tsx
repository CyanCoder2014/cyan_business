"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { getConversationSession } from "@/lib/platform-api";
import type { AiConversationSession } from "@/lib/types";

export default function BotSessionPage() {
  const params = useParams<{ sessionId: string }>();
  const sessionId = params.sessionId;
  const [session, setSession] = useState<AiConversationSession | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    getConversationSession(sessionId)
      .then(setSession)
      .catch((error) => setStatus(error instanceof Error ? error.message : "Session could not be loaded."));
  }, [sessionId]);

  if (!session) {
    return (
      <AppShell title="Bot session" subtitle="Persistent bot conversation thread.">
        <section className="panel rail">
          <p className="section-title">Session</p>
          <p className="muted">{status ?? "Loading session..."}</p>
        </section>
      </AppShell>
    );
  }

  return (
    <AppShell title={session.latestPrompt ?? session.sessionId} subtitle="Persistent bot conversation thread.">
      <div className="studio-grid">
        <section className="panel rail">
          <p className="section-title">Session</p>
          <div className="timeline">
            <div className="timeline-step">
              <strong>Channel</strong>
              <span>{session.channelType ?? "PANEL"}</span>
            </div>
            <div className="timeline-step">
              <strong>Status</strong>
              <span>{session.status}</span>
            </div>
            <div className="timeline-step">
              <strong>Scope</strong>
              <span>
                {session.tenantKey ?? "tenant-demo"} / {session.siteKey ?? "site-commerce"}
              </span>
            </div>
            <div className="timeline-step">
              <strong>Linked draft</strong>
              <span>{session.draftId ?? "none"}</span>
            </div>
            <div className="timeline-step">
              <strong>Pending questions</strong>
              <span>{session.pendingQuestions.length}</span>
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
                <div key={message.messageId} className="draft-item">
                  <strong>
                    <span>{message.role}</span>
                    <span className="muted">{message.createdAt ? new Date(message.createdAt).toLocaleString() : "—"}</span>
                  </strong>
                  <span className="muted">{message.content}</span>
                </div>
              ))}
              {!session.messages.length ? <p className="muted">No messages stored for this session.</p> : null}
            </div>
          </section>
        </aside>
      </div>

      <section style={{ padding: "24px" }}>
        <p className="section-title">Answers</p>
        <pre className="json-view">{JSON.stringify(session.extractedAnswers ?? {}, null, 2)}</pre>
      </section>

      <section style={{ padding: "24px", paddingTop: 0 }}>
        <p className="section-title">Pending questions</p>
        {session.pendingQuestions.length ? (
          <ul className="result-list">
            {session.pendingQuestions.map((question) => (
              <li key={question}>{question}</li>
            ))}
          </ul>
        ) : (
          <p className="muted">This session has no pending follow-up questions.</p>
        )}
      </section>
    </AppShell>
  );
}
