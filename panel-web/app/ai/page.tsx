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
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";

export default function AiPage() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const { showToast } = useToast();
  const [sessions, setSessions] = useState<AiConversationSession[]>([]);
  const [blueprints, setBlueprints] = useState<AppBlueprint[]>([]);
  const [active, setActive] = useState<AiConversationSession | null>(null);
  const [draft, setDraft] = useState<ClientAppDraft | null>(null);
  const [heuristicOnly, setHeuristicOnly] = useState(false);
  const [prompt, setPrompt] = useState("");
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [starting, setStarting] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [pendingAnswers, setPendingAnswers] = useState<Record<string, string>>({});
  const [answering, setAnswering] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

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
    } catch (cause) { const { title, message } = describeApiError(cause, "AI Studio could not be loaded."); setError(message); showToast({ tone: "error", title, message }); }
    finally { setLoading(false); }
  }, [siteKey, tenantKey]);

  useEffect(() => { void load(); }, [load, queryVersion]);
  useEffect(() => { if (active?.draftId) void getClientDraft(active.draftId).then(setDraft).catch(() => setDraft(null)); else setDraft(null); }, [active?.draftId]);
  useEffect(() => { setPendingAnswers({}); }, [draft?.draftId, draft?.revision]);
  const start = async () => {
    if (!tenantKey) return;
    setStarting(true); setActionError(null);
    try {
      const value = await createConversationSession({ channelType: "PANEL", tenantKey, siteKey: siteKey || undefined, title: locale === "fa" ? "گفتگوی جدید" : "New conversation" });
      setSessions((current) => [value, ...current]); setActive(value); setDraft(null);
    } catch (cause) { const { title, message } = describeApiError(cause, "A new conversation could not be created."); setActionError(message); showToast({ tone: "error", title, message }); }
    finally { setStarting(false); }
  };
  const send = async () => {
    if (!prompt.trim() || !tenantKey) return;
    const text = prompt.trim();
    setSending(true); setActionError(null);
    try {
      let session = active;
      if (!session) session = await createConversationSession({ channelType: "PANEL", tenantKey, siteKey: siteKey || undefined, title: text.slice(0, 80) });
      await appendConversationMessage(session.sessionId, { role: "USER", content: text });
      const response = await generatePlatformApp({ prompt: text, tenantKey, siteKey: siteKey || undefined, sessionId: session.sessionId, execute: false });
      setHeuristicOnly(response.generationMode === "HEURISTIC");
      const summary = response.nextQuestions.length
        ? response.nextQuestions[0]
        : (locale === "fa"
          ? `پیش‌نویس به‌روزرسانی شد: ${response.dsl.entities.length} موجودیت، ${response.dsl.routes.length} مسیر، ${response.dsl.flows.length} فلو.`
          : `Draft updated: ${response.dsl.entities.length} entities, ${response.dsl.routes.length} routes, ${response.dsl.flows.length} flows.`);
      const refreshed = await appendConversationMessage(response.sessionId || session.sessionId, { role: "ASSISTANT", content: summary });
      setActive(refreshed); setSessions((current) => [refreshed, ...current.filter((item) => item.sessionId !== refreshed.sessionId)]);
      if (response.draftId) setDraft(await getClientDraft(response.draftId));
      setPrompt("");
    } catch (cause) { const { title, message } = describeApiError(cause, "Generation failed."); setActionError(message); showToast({ tone: "error", title, message }); }
    finally { setSending(false); }
  };
  const attach = async (file: File) => {
    if (!tenantKey || !draft) return;
    setUploading(true); setProgress(0); setActionError(null);
    try {
      const scope = { tenantKey, siteKey: siteKey || undefined };
      const prepared = await prepareMediaUpload(file, scope);
      const uploaded = await uploadMediaBytes(file, prepared, scope, setProgress);
      await attachProjectAsset(draft.draftId, { assetKey: uploaded.assetKey, fileName: file.name, mimeType: file.type || "application/octet-stream", sizeBytes: file.size }, scope);
      setDraft(await getClientDraft(draft.draftId));
    } catch (cause) { const { title, message } = describeApiError(cause, "Attachment upload failed."); setActionError(message); showToast({ tone: "error", title, message }); }
    finally { setUploading(false); }
  };

  const submitAnswers = async () => {
    if (!draft) return;
    const answersPatch = Object.fromEntries(draft.pendingQuestionKeys.map((key) => [key, pendingAnswers[key]?.trim()]).filter(([, value]) => value));
    if (!Object.keys(answersPatch).length) return;
    setAnswering(true); setActionError(null);
    try {
      const updatedDraft = await updateClientDraft(draft.draftId, { answersPatch });
      if (active) {
        const refreshed = await appendConversationMessage(active.sessionId, { role: "USER", content: "Answered the required project questions.", answersPatch });
        setActive(refreshed);
        setSessions((current) => current.map((item) => item.sessionId === refreshed.sessionId ? refreshed : item));
      }
      setDraft(updatedDraft);
    } catch (cause) { const { title, message } = describeApiError(cause, "The answers could not be saved."); setActionError(message); showToast({ tone: "error", title, message }); }
    finally { setAnswering(false); }
  };

  const questions = draft?.pendingQuestions ?? active?.pendingQuestions ?? [];
  const fa = locale === "fa";

  // Raw enum names leak the backend's vocabulary into the UI. These are the
  // states a person actually cares about while building.
  const statusLabel = (status: string) => {
    if (status === "WAITING_FOR_ANSWERS") return fa ? "منتظر پاسخ شما" : "Needs your answer";
    if (status === "RESOLVED") return fa ? "آماده" : "Ready";
    if (status === "FAILED") return fa ? "ناموفق" : "Failed";
    if (status === "CLOSED") return fa ? "بسته‌شده" : "Closed";
    return fa ? "در حال کار" : "In progress";
  };
  const statusTone = (status: string) =>
    status === "RESOLVED" ? "success" : status === "WAITING_FOR_ANSWERS" ? "warning" : status === "FAILED" ? "danger" : "neutral";

  // "session-8388e25b-7408-…" is not a name anyone can pick out of a list.
  const sessionTitle = (session: AiConversationSession) =>
    session.latestPrompt?.trim()
      ? session.latestPrompt.trim().split("\n")[0].slice(0, 70)
      : (fa ? "گفتگوی بدون عنوان" : "Untitled conversation");

  // Backend text like "Skipped because its owning microservice is unavailable:
  // content-service entity landing-page" tells a user nothing they can act on.
  const readableAction = (action: string) => {
    const match = /unavailable: ([a-z-]+) (entity|route) ([\w-]+)/i.exec(action);
    if (!match) return action;
    const [, service, kind, name] = match;
    const what = kind.toLowerCase() === "route" ? (fa ? "مسیر" : "the route") : (fa ? "موجودیت" : "the entity");
    return fa
      ? `«${name}» ساخته نشد چون سرویس ${service} در دسترس نیست.`
      : `Could not create ${what} "${name}" — ${service} is not running.`;
  };

  const counts = draft
    ? [
        { n: draft.resolvedDsl.entities.length, label: fa ? "موجودیت" : "entities" },
        { n: draft.resolvedDsl.routes.length, label: fa ? "مسیر" : "routes" },
        { n: draft.resolvedDsl.flows.length, label: fa ? "فلو" : "flows" },
      ]
    : [];
  const builtNothing = Boolean(draft) && counts.every((c) => c.n === 0);

  const answersReady = draft?.pendingQuestionKeys.some((key) => pendingAnswers[key]?.trim());
  const questionBlock = draft && questions.length ? (
    <section className="ai-question-card" aria-label={fa ? "پرسش‌های باقی‌مانده" : "Questions to answer"}>
      <header>
        <ClockIcon size={14} className="tone-warning" />
        <div>
          <strong>{fa ? "برای ادامه به این پاسخ‌ها نیاز است" : "A few answers are needed to continue"}</strong>
          <small>{fa ? "پس از پاسخ، پیش‌نویس دوباره ساخته می‌شود." : "The draft is regenerated once you answer."}</small>
        </div>
      </header>
      {questions.map((question, index) => {
        const key = draft.pendingQuestionKeys[index] ?? `answer-${index}`;
        return (
          <label key={key}>
            <span>{question}</span>
            <input
              aria-label={question}
              value={pendingAnswers[key] ?? ""}
              disabled={answering}
              onChange={(event) => setPendingAnswers((current) => ({ ...current, [key]: event.target.value }))}
            />
          </label>
        );
      })}
      <button className="primary-pill" disabled={answering || !answersReady} onClick={submitAnswers}>
        {answering ? (fa ? "در حال ذخیره…" : "Saving…") : (fa ? "ثبت پاسخ‌ها" : "Submit answers")}
      </button>
    </section>
  ) : null;

  return <PanelShell activeKey="studio" title="AI Studio" titleFa="استودیوی هوش مصنوعی" subtitle="Describe what you want to build. Cyan drafts it, then you refine it." subtitleFa="آنچه می‌خواهید بسازید را توصیف کنید. Cyan آن را پیش‌نویس می‌کند.">
    {heuristicOnly ? <div className="operational-banner error" role="alert"><span>{fa
      ? "هیچ ارائه‌دهنده هوش مصنوعی پیکربندی نشده است. این پیش‌نویس از یک قالب آماده ساخته شده و متن درخواست شما را دنبال نمی‌کند."
      : "No AI provider is configured, so this draft came from a stock template and does not follow your description."}</span><Link className="secondary-pill" href="/ai/providers">{fa ? "افزودن کلید" : "Add a key"}</Link></div> : null}

    <div className="ai-workspace">
      <aside className="ai-session-list">
        <button className="primary-pill" disabled={starting || sending} onClick={start}>
          <SparkleIcon size={15}/>{starting ? (fa ? "در حال ساخت…" : "Creating…") : (fa ? "گفتگوی جدید" : "New conversation")}
        </button>
        {sessions.map((session) => (
          <button key={session.sessionId} disabled={starting || sending}
                  className={active?.sessionId === session.sessionId ? "active" : ""}
                  onClick={() => setActive(session)}>
            <strong>{sessionTitle(session)}</strong>
            <span className="ai-session-status">
              <StatusBadge tone={statusTone(session.status)}>{statusLabel(session.status)}</StatusBadge>
            </span>
          </button>
        ))}
        {!sessions.length ? <p className="ai-rail-hint">{fa ? "هنوز گفتگویی ندارید." : "No conversations yet."}</p> : null}
      </aside>

      <section className="ai-thread">
        {loading ? <Skeleton height={220} />
          : error && !active ? <ErrorState title="AI Studio unavailable" description={error} retry={load} />
          : active ? (
            <div className="message-list">
              {active.messages.map((message) => {
                const isUser = message.role.toUpperCase() === "USER";
                return (
                  <article key={message.messageId} className={`message ${message.role.toLowerCase()}`}>
                    <span className="message-avatar" aria-hidden>{isUser ? (fa ? "ش" : "Y") : <SparkleIcon size={14}/>}</span>
                    <div>
                      <p>{message.content}</p>
                      <time>{message.createdAt ? new Date(message.createdAt).toLocaleTimeString(locale) : ""}</time>
                    </div>
                  </article>
                );
              })}
              {/* Questions belong with the conversation that raised them, not in
                  a separate column where they read as unrelated settings. */}
              {questionBlock}
              {!active.messages.length ? <EmptyState title={fa ? "گفتگو خالی است" : "Start building"} description={fa ? "توصیف کنید چه می‌خواهید بسازید." : "Describe the app you want and Cyan will draft it."} /> : null}
            </div>
          ) : <EmptyState title={fa ? "گفتگویی انتخاب نشده" : "No conversation selected"} description={fa ? "یک گفتگوی جدید شروع کنید." : "Start a new conversation to begin."} />}

        <div className="ai-composer">
          <label className={draft ? "secondary-pill" : "secondary-pill disabled"} title={draft ? "" : (fa ? "ابتدا پروژه بسازید" : "Create a project first")}>
            <input type="file" hidden disabled={!draft || uploading} onChange={(event) => { const file = event.target.files?.[0]; if (file) void attach(file); event.target.value = ""; }} />
            {uploading ? `${progress}%` : <PaperclipIcon size={16}/>}
          </label>
          <textarea value={prompt} onChange={(event) => setPrompt(event.target.value)}
                    placeholder={fa ? "چه چیزی می‌خواهید بسازید؟" : "Describe what you want to build…"} />
          <button className="primary-pill ai-send-button" disabled={sending || !prompt.trim()} onClick={send}>
            {sending ? (fa ? "در حال ساخت" : "Generating") : <><span>{fa ? "ارسال" : "Send"}</span><SendIcon size={15}/></>}
          </button>
        </div>
        {/* Below the composer and clearly labelled: these replace the box's
            contents, which was surprising when they sat above it unlabelled. */}
        {blueprints.length ? (
          <div className="quick-prompts">
            <span className="quick-prompts-label">{fa ? "شروع از قالب:" : "Start from a template:"}</span>
            {blueprints.slice(0, 5).map((blueprint) => {
              const Icon = appTypeIcon(blueprint.appType);
              return <button key={blueprint.blueprintKey} type="button"
                             title={blueprint.description}
                             onClick={() => setPrompt(blueprint.description)}><Icon size={14}/>{blueprint.title}</button>;
            })}
          </div>
        ) : null}
      </section>

      <aside className="project-inspector">
        <header>
          {draft ? (() => { const AppIcon = appTypeIcon(draft.appType); return <span className="project-inspector-icon" aria-hidden><AppIcon size={17}/></span>; })() : null}
          <h2>{draft?.title || (fa ? "خلاصه پروژه" : "Project summary")}</h2>
          {draft ? <StatusBadge tone={statusTone(draft.status)}>{statusLabel(draft.status)}</StatusBadge> : null}
        </header>
        {draft ? <>
          <p>{draft.latestIntent}</p>
          <div className="summary-grid">
            {counts.map((c) => (
              <span key={c.label} className={c.n === 0 ? "is-empty" : ""}><b>{c.n}</b>{c.label}</span>
            ))}
          </div>
          {builtNothing ? (
            <p className="ai-nothing-built">
              <XCircleIcon size={14} className="tone-danger"/>
              {fa ? "هیچ بخشی ساخته نشد. دلیل آن در پایین آمده است." : "Nothing was created yet — see why below."}
            </p>
          ) : null}
          {draft.resolvedDsl.app.capabilities?.length ? (
            <ul className="capability-chip-list">
              {draft.resolvedDsl.app.capabilities.map((capability) => {
                const Icon = capabilityIcon(capability);
                return <li key={capability}><Icon size={13}/>{capabilityLabel(capability)}</li>;
              })}
            </ul>
          ) : null}
          {draft.manualActions.length ? (
            <section className="manual-action-list">
              <h3>{fa ? "نیازمند توجه" : "Needs attention"}</h3>
              <ul>{draft.manualActions.map((action, index) => (
                <li key={index}><ClockIcon size={13} className="tone-warning"/><span>{readableAction(action)}</span></li>
              ))}</ul>
            </section>
          ) : questions.length === 0 ? (
            <p className="manual-action-ready"><CheckCircleIcon size={14} className="tone-success"/>{fa ? "آماده انتشار است." : "Ready to publish."}</p>
          ) : null}
          <Link className="primary-pill" href={`/projects/${draft.draftId}`}>{fa ? "باز کردن پروژه" : "Open project"}</Link>
        </> : <EmptyState title={fa ? "هنوز پروژه‌ای نیست" : "Nothing drafted yet"} description={fa ? "پس از ارسال درخواست، نتیجه اینجا ظاهر می‌شود." : "Send a request and the generated draft appears here."} />}
        {active && active.status !== "CLOSED" ? (
          <button className="secondary-pill" disabled={answering} onClick={async () => {
            setActionError(null);
            try {
              const value = await closeConversationSession(active.sessionId);
              setActive(value);
              setSessions((current) => current.map((item) => item.sessionId === value.sessionId ? value : item));
            } catch (cause) {
              const { title, message } = describeApiError(cause, "Closing the session failed.");
              setActionError(message); showToast({ tone: "error", title, message });
            }
          }}>{fa ? "بستن گفتگو" : "Close conversation"}</button>
        ) : null}
      </aside>
    </div>
    {actionError ? <p className="operational-banner error" role="alert">{actionError}</p> : null}
  </PanelShell>;
}
