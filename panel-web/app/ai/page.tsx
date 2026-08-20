"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { prepareMediaUpload, uploadMediaBytes } from "@/lib/media-api";
import { appendConversationMessage, attachProjectAsset, closeConversationSession, createConversationSession, generatePlatformApp, getClientDraft, listBlueprints, listConversationSessions, updateClientDraft } from "@/lib/platform-api";
import type { AiConversationSession, AppBlueprint, ClientAppDraft } from "@/lib/types";
import { appTypeIcon, capabilityIcon, capabilityLabel, CheckCircleIcon, ClockIcon, PaperclipIcon, SendIcon, SparkleIcon, XCircleIcon } from "@/components/nav-icons";

export default function AiPage() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const [sessions, setSessions] = useState<AiConversationSession[]>([]);
  const [blueprints, setBlueprints] = useState<AppBlueprint[]>([]);
  const [active, setActive] = useState<AiConversationSession | null>(null);
  const [draft, setDraft] = useState<ClientAppDraft | null>(null);
  const [prompt, setPrompt] = useState("");
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [starting, setStarting] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [pendingAnswers, setPendingAnswers] = useState<Record<string, string>>({});
  const [answering, setAnswering] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!tenantKey) { setLoading(false); return; }
    setLoading(true); setError(null);
    try {
      const [sessionValues, blueprintValues] = await Promise.all([listConversationSessions({ tenantKey, siteKey: siteKey || undefined }), listBlueprints()]);
      setSessions(sessionValues); setBlueprints(blueprintValues);
      const query = new URLSearchParams(window.location.search);
      const requestedSession = query.get("sessionId");
      const chosen = sessionValues.find((item) => item.sessionId === requestedSession) ?? sessionValues[0] ?? null;
      setActive(chosen);
      const requestedBlueprint = query.get("blueprintKey");
      if (requestedBlueprint) {
        const blueprint = blueprintValues.find((item) => item.blueprintKey === requestedBlueprint);
        if (blueprint) setPrompt(blueprint.description);
      }
    } catch (cause) { setError(cause instanceof Error ? cause.message : "AI Studio could not be loaded."); }
    finally { setLoading(false); }
  }, [siteKey, tenantKey]);

  useEffect(() => { void load(); }, [load, queryVersion]);
  useEffect(() => { if (active?.draftId) void getClientDraft(active.draftId).then(setDraft).catch(() => setDraft(null)); else setDraft(null); }, [active?.draftId]);
  useEffect(() => { setPendingAnswers({}); }, [draft?.draftId, draft?.revision]);
  const start = async () => {
    if (!tenantKey) return;
    setStarting(true); setError(null);
    try {
      const value = await createConversationSession({ channelType: "PANEL", tenantKey, siteKey: siteKey || undefined, title: locale === "fa" ? "گفتگوی جدید" : "New conversation" });
      setSessions((current) => [value, ...current]); setActive(value); setDraft(null);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "A new conversation could not be created."); }
    finally { setStarting(false); }
  };
  const send = async () => {
    if (!prompt.trim() || !tenantKey) return;
    const text = prompt.trim();
    setSending(true); setError(null);
    try {
      let session = active;
      if (!session) session = await createConversationSession({ channelType: "PANEL", tenantKey, siteKey: siteKey || undefined, title: text.slice(0, 80) });
      await appendConversationMessage(session.sessionId, { role: "USER", content: text });
      const response = await generatePlatformApp({ prompt: text, tenantKey, siteKey: siteKey || undefined, sessionId: session.sessionId, execute: false });
      const summary = response.nextQuestions.length
        ? response.nextQuestions[0]
        : (locale === "fa"
          ? `پیش‌نویس به‌روزرسانی شد: ${response.dsl.entities.length} موجودیت، ${response.dsl.routes.length} مسیر، ${response.dsl.flows.length} فلو.`
          : `Draft updated: ${response.dsl.entities.length} entities, ${response.dsl.routes.length} routes, ${response.dsl.flows.length} flows.`);
      const refreshed = await appendConversationMessage(response.sessionId || session.sessionId, { role: "ASSISTANT", content: summary });
      setActive(refreshed); setSessions((current) => [refreshed, ...current.filter((item) => item.sessionId !== refreshed.sessionId)]);
      if (response.draftId) setDraft(await getClientDraft(response.draftId));
      setPrompt("");
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Generation failed."); }
    finally { setSending(false); }
  };
  const attach = async (file: File) => {
    if (!tenantKey || !draft) return;
    setUploading(true); setProgress(0); setError(null);
    try {
      const scope = { tenantKey, siteKey: siteKey || undefined };
      const prepared = await prepareMediaUpload(file, scope);
      const uploaded = await uploadMediaBytes(file, prepared, scope, setProgress);
      await attachProjectAsset(draft.draftId, { assetKey: uploaded.assetKey, fileName: file.name, mimeType: file.type || "application/octet-stream", sizeBytes: file.size }, scope);
      setDraft(await getClientDraft(draft.draftId));
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Attachment upload failed."); }
    finally { setUploading(false); }
  };

  const submitAnswers = async () => {
    if (!draft) return;
    const answersPatch = Object.fromEntries(draft.pendingQuestionKeys.map((key) => [key, pendingAnswers[key]?.trim()]).filter(([, value]) => value));
    if (!Object.keys(answersPatch).length) return;
    setAnswering(true); setError(null);
    try {
      const updatedDraft = await updateClientDraft(draft.draftId, { answersPatch });
      if (active) {
        const refreshed = await appendConversationMessage(active.sessionId, { role: "USER", content: "Answered the required project questions.", answersPatch });
        setActive(refreshed);
        setSessions((current) => current.map((item) => item.sessionId === refreshed.sessionId ? refreshed : item));
      }
      setDraft(updatedDraft);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "The answers could not be saved."); }
    finally { setAnswering(false); }
  };

  const questions = draft?.pendingQuestions ?? active?.pendingQuestions ?? [];
  return <PanelShell activeKey="studio" title="AI Studio" titleFa="استودیوی هوش مصنوعی" subtitle="Build through persisted conversations and backend-validated drafts." subtitleFa="با گفتگوهای پایدار و پیش‌نویس‌های معتبر backend بسازید.">
    <div className="page-action-bar"><span>{locale === "fa" ? "پروفایل‌های API و تولید رسانه به‌صورت مستقل مدیریت می‌شوند." : "API profiles and generated media are managed independently."}</span><Link className="secondary-pill" href="/ai/providers">{locale === "fa" ? "مدیریت ارائه‌دهندگان" : "Manage providers"}</Link></div>
    <div className="ai-workspace">
      <aside className="ai-session-list"><button className="primary-pill" disabled={starting || sending} onClick={start}><SparkleIcon size={15}/>{starting ? (locale === "fa" ? "در حال ساخت…" : "Creating…") : (locale === "fa" ? "گفتگوی جدید" : "New conversation")}</button>{sessions.map((session) => { const StatusIcon = session.status === "RESOLVED" ? CheckCircleIcon : session.status === "WAITING_FOR_ANSWERS" ? ClockIcon : session.status === "FAILED" ? XCircleIcon : null; const tone = session.status === "RESOLVED" ? "tone-success" : session.status === "WAITING_FOR_ANSWERS" ? "tone-warning" : session.status === "FAILED" ? "tone-danger" : ""; return <button key={session.sessionId} disabled={starting || sending} className={active?.sessionId === session.sessionId ? "active" : ""} onClick={() => setActive(session)}><strong>{session.latestPrompt || session.sessionId}</strong><span className="ai-session-status">{StatusIcon ? <StatusIcon size={12} className={tone}/> : null}{session.status}</span></button>; })}</aside>
      <section className="ai-thread">
        {loading ? <Skeleton height={220} /> : error && !active ? <ErrorState title="AI Studio unavailable" description={error} retry={load} /> : active ? <div className="message-list">{active.messages.map((message) => { const isUser = message.role.toUpperCase() === "USER"; return <article key={message.messageId} className={`message ${message.role.toLowerCase()}`}><span className="message-avatar" aria-hidden>{isUser ? (locale === "fa" ? "شما" : "You").slice(0, 1) : <SparkleIcon size={14}/>}</span><div><p>{message.content}</p><time>{message.createdAt ? new Date(message.createdAt).toLocaleTimeString(locale) : ""}</time></div></article>; })}{!active.messages.length ? <EmptyState title={locale === "fa" ? "گفتگو خالی است" : "Conversation is empty"} description={locale === "fa" ? "اولین درخواست خود را ارسال کنید." : "Send the first request to begin."} /> : null}</div> : <EmptyState title={locale === "fa" ? "گفتگویی انتخاب نشده" : "No conversation selected"} description={locale === "fa" ? "یک گفتگوی واقعی ایجاد کنید." : "Create a persisted conversation to begin."} />}
        <div className="quick-prompts">{blueprints.slice(0, 5).map((blueprint) => { const Icon = appTypeIcon(blueprint.appType); return <button key={blueprint.blueprintKey} onClick={() => setPrompt(blueprint.description)}><Icon size={14}/>{blueprint.title}</button>; })}</div>
        <div className="ai-composer"><label className={draft ? "secondary-pill" : "secondary-pill disabled"} title={draft ? "" : (locale === "fa" ? "ابتدا پروژه بسازید" : "Create a project first")}><input type="file" hidden disabled={!draft || uploading} onChange={(event) => { const file = event.target.files?.[0]; if (file) void attach(file); event.target.value = ""; }} />{uploading ? `${progress}%` : <PaperclipIcon size={16}/>}</label><textarea value={prompt} onChange={(event) => setPrompt(event.target.value)} placeholder={locale === "fa" ? "چه چیزی می‌خواهید بسازید؟" : "What would you like to build?"} /><button className="primary-pill ai-send-button" disabled={sending || !prompt.trim()} onClick={send}>{sending ? (locale === "fa" ? "در حال ساخت" : "Generating") : <><span>{locale === "fa" ? "ارسال" : "Send"}</span><SendIcon size={15}/></>}</button></div>
      </section>
      <aside className="project-inspector"><header>{draft ? (()=>{const AppIcon=appTypeIcon(draft.appType);return <span className="project-inspector-icon" aria-hidden><AppIcon size={17}/></span>;})():null}<h2>{draft?.title || (locale === "fa" ? "خلاصه پروژه" : "Project summary")}</h2>{draft ? <StatusBadge tone="info">{draft.status}</StatusBadge> : null}</header>{draft ? <><p>{draft.latestIntent}</p><div className="summary-grid"><span>{draft.resolvedDsl.entities.length} entities</span><span>{draft.resolvedDsl.routes.length} routes</span><span>{draft.resolvedDsl.flows.length} flows</span></div>{draft.resolvedDsl.app.capabilities?.length ? <ul className="capability-chip-list">{draft.resolvedDsl.app.capabilities.map((capability) => { const Icon = capabilityIcon(capability); return <li key={capability}><Icon size={13}/>{capabilityLabel(capability)}</li>; })}</ul> : null}{questions.length ? <section className="follow-up-form"><h3>{locale === "fa" ? "پرسش‌های باقی‌مانده" : "Pending questions"}</h3>{questions.map((question, index) => { const key = draft.pendingQuestionKeys[index] ?? `answer-${index}`; return <label key={key}><span>{question}</span><input aria-label={`Answer: ${question}`} value={pendingAnswers[key] ?? ""} disabled={answering} onChange={(event) => setPendingAnswers((current) => ({ ...current, [key]: event.target.value }))} /></label>})}<button className="primary-pill" disabled={answering || !draft.pendingQuestionKeys.some((key) => pendingAnswers[key]?.trim())} onClick={submitAnswers}>{answering ? (locale === "fa" ? "در حال ذخیره…" : "Saving answers…") : (locale === "fa" ? "ثبت پاسخ‌ها" : "Submit answers")}</button></section> : null}{draft.manualActions.length ? <section className="manual-action-list"><h3>{locale === "fa" ? "پیش از انتشار" : "Before you publish"}</h3><ul>{draft.manualActions.map((action, index) => <li key={index}><ClockIcon size={13} className="tone-warning"/><span>{action}</span></li>)}</ul></section> : questions.length === 0 ? <p className="manual-action-ready"><CheckCircleIcon size={14} className="tone-success"/>{locale === "fa" ? "آماده انتشار است." : "Ready to publish."}</p> : null}<Link className="primary-pill" href={`/projects/${draft.draftId}`}>{locale === "fa" ? "باز کردن پروژه" : "Open project"}</Link></> : <EmptyState title={locale === "fa" ? "هنوز پروژه‌ای نیست" : "No project yet"} description={locale === "fa" ? "خروجی واقعی تولید در اینجا ظاهر می‌شود." : "A generated backend draft will appear here."} />}{active && active.status !== "CLOSED" ? <button className="secondary-pill" disabled={answering} onClick={async () => { const value = await closeConversationSession(active.sessionId); setActive(value); setSessions((current) => current.map((item) => item.sessionId === value.sessionId ? value : item)); }}>{locale === "fa" ? "بستن گفتگو" : "Close session"}</button> : null}</aside>
    </div>
    {error && active ? <p className="operational-banner error" role="alert">{error}</p> : null}
  </PanelShell>;
}
