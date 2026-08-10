"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { PanelShell } from "@/components/panel-shell";
import { CodeViewer, EmptyState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { usePanel } from "@/components/panel-provider";
import { getConversationSession } from "@/lib/platform-api";
import type { AiConversationSession } from "@/lib/types";

export default function BotSessionPage() {
  const { locale } = usePanel();
  const { sessionId } = useParams<{ sessionId: string }>();
  const [session, setSession] = useState<AiConversationSession | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => { getConversationSession(sessionId).then(setSession).catch(reason => setError(reason instanceof Error ? reason.message : String(reason))); }, [sessionId]);
  return <PanelShell activeKey="studio" title={session?.latestPrompt ?? (locale === "fa" ? "نشست هوش" : "AI session")} titleFa={session?.latestPrompt ?? "نشست هوش"} subtitle={sessionId} subtitleFa={sessionId}>
    {error ? <EmptyState title={locale === "fa" ? "نشست بارگذاری نشد" : "Session could not be loaded"} description={error} action={<Link className="secondary-pill" href="/ai">{locale === "fa" ? "بازگشت به استودیو" : "Back to AI Studio"}</Link>}/> : !session ? <Skeleton height={420}/> : <div className="studio-grid">
      <section className="panel-card"><div className="card-title-row"><h2>{locale === "fa" ? "گفتگو" : "Conversation"}</h2><StatusBadge tone={session.status === "COMPLETED" ? "success" : "info"}>{session.status}</StatusBadge></div><div className="draft-list">{session.messages.map(message => <article className="draft-item" key={message.messageId}><strong><span>{message.role}</span><time>{message.createdAt ? new Date(message.createdAt).toLocaleString(locale === "fa" ? "fa-IR" : "en") : "—"}</time></strong><p>{message.content}</p></article>)}</div></section>
      <aside className="panel-card"><h2>{locale === "fa" ? "جزئیات نشست" : "Session details"}</h2><dl className="phase9-details"><div><dt>{locale === "fa" ? "کانال" : "Channel"}</dt><dd>{session.channelType ?? "PANEL"}</dd></div><div><dt>{locale === "fa" ? "پروژه" : "Linked project"}</dt><dd>{session.draftId ?? "—"}</dd></div><div><dt>{locale === "fa" ? "پرسش‌های باز" : "Pending questions"}</dt><dd>{session.pendingQuestions.length}</dd></div></dl>{session.pendingQuestions.length ? <ul className="result-list">{session.pendingQuestions.map(question => <li key={question}>{question}</li>)}</ul> : null}<Link className="primary-pill" href={`/ai?sessionId=${encodeURIComponent(sessionId)}`}>{locale === "fa" ? "ادامه در استودیو" : "Resume in AI Studio"}</Link></aside>
      <section className="panel-card"><h2>{locale === "fa" ? "پاسخ‌های استخراج‌شده" : "Extracted answers"}</h2><CodeViewer value={session.extractedAnswers ?? {}}/></section>
    </div>}
  </PanelShell>;
}
