"use client";

import Link from "next/link";
import { type ChangeEvent, useEffect, useMemo, useRef, useState } from "react";
import {
  ArrowRightIcon,
  DocumentIcon,
  GlobeIcon,
  LightningIcon,
  PhoneDeviceIcon,
  PlaneIcon,
  ShieldIcon,
  ShopIcon,
  SparkleIcon,
  UsersIcon
} from "@/components/auth-icons";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { prepareMediaUpload } from "@/lib/media-api";
import {
  generatePlatformApp,
  generatePlatformAppOverWebSocket,
  hasAiStudioWebSocket,
  listBlueprints,
  listClientDrafts
} from "@/lib/platform-api";
import type {
  ClientAppDraft,
  FollowUpQuestion,
  GeneratePlatformAppRequest,
  GeneratePlatformAppResponse,
  PlatformAppType
} from "@/lib/types";

const TENANT_KEY = "tenant-demo";
const SITE_KEY = "site-commerce";

const QUICK_PROMPTS = [
  {
    key: "shop",
    en: "Create a shop",
    fa: "ساخت فروشگاه",
    prompt: "Create a shop with product catalog, cart, checkout, payments, and order tracking."
  },
  {
    key: "crm",
    en: "Build a CRM",
    fa: "ساخت CRM",
    prompt: "Build a CRM for leads, contacts, deals, follow-up tasks, and customer timelines."
  },
  {
    key: "bpm",
    en: "Make a BPM form",
    fa: "فرم BPM",
    prompt: "Make a BPM form with approval states, managed objects, notifications, and audit history."
  },
  {
    key: "bot",
    en: "Telegram bot",
    fa: "ربات تلگرام",
    prompt: "Create a Telegram and Bale bot connected to customer support, order lookup, and automation flows."
  },
  {
    key: "landing",
    en: "Landing page",
    fa: "صفحه فرود",
    prompt: "Create a landing page with hero content, lead capture, SEO metadata, media assets, and publishing routes."
  },
  {
    key: "pwa",
    en: "PWA",
    fa: "اپ PWA",
    prompt: "Create a mobile-friendly PWA with public pages, app shell, customer records, and offline-ready navigation."
  }
] as const;

const MODE_OPTIONS: Array<{ key: string; en: string; fa: string; appType?: PlatformAppType }> = [
  { key: "smart", en: "Smart", fa: "هوشمند" },
  { key: "shop", en: "Shop", fa: "فروشگاه", appType: "SHOP" },
  { key: "website", en: "Website", fa: "وب‌سایت", appType: "WEBSITE" },
  { key: "crm", en: "CRM", fa: "CRM", appType: "CRM" },
  { key: "bpm", en: "BPM", fa: "BPM", appType: "BPM_PORTAL" }
];

const MODULE_TILES = [
  { key: "website", en: "Website", fa: "وب‌سایت", suffixEn: "pages", suffixFa: "صفحه", tone: "blue" },
  { key: "shop", en: "Shop", fa: "فروشگاه", suffixEn: "modules", suffixFa: "ماژول", tone: "pink" },
  { key: "crm", en: "CRM", fa: "CRM", suffixEn: "modules", suffixFa: "ماژول", tone: "violet" },
  { key: "forms", en: "Forms", fa: "فرم‌ها", suffixEn: "forms", suffixFa: "فرم", tone: "orange" },
  { key: "flow", en: "Flow", fa: "فلو", suffixEn: "workflows", suffixFa: "فلو", tone: "indigo" },
  { key: "bot", en: "Bot", fa: "بات", suffixEn: "bot", suffixFa: "بات", tone: "blue" }
] as const;

type StudioAttachment = {
  id: string;
  fileName: string;
  mimeType: string;
  sizeBytes: number;
  status: "uploading" | "uploaded" | "failed";
  assetKey?: string;
  deliveryUrl?: string;
  error?: string;
};

type StudioMessage = {
  id: string;
  role: "assistant" | "user";
  content: string;
  createdAtLabel: string;
  tone?: "thinking" | "error";
  response?: GeneratePlatformAppResponse;
  attachments?: StudioAttachment[];
};

type InspectorMode = "dsl" | "services" | "checklist" | null;

export default function AiStudioPage() {
  const { locale } = usePanel();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [prompt, setPrompt] = useState("I want a shop with product catalog, cart, checkout, payments and order tracking.");
  const [status, setStatus] = useState<string | null>(null);
  const [response, setResponse] = useState<GeneratePlatformAppResponse | null>(null);
  const [drafts, setDrafts] = useState<ClientAppDraft[]>([]);
  const [messages, setMessages] = useState<StudioMessage[]>([]);
  const [attachments, setAttachments] = useState<StudioAttachment[]>([]);
  const [answers, setAnswers] = useState<Record<string, unknown>>({});
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [mode, setMode] = useState("smart");
  const [inspector, setInspector] = useState<InspectorMode>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    Promise.allSettled([listBlueprints(), listClientDrafts({ tenantKey: TENANT_KEY, siteKey: SITE_KEY })]).then(([blueprintItems, draftItems]) => {
      const errors: string[] = [];
      if (blueprintItems.status === "rejected") {
        errors.push(locale === "fa" ? "قالب‌ها بارگیری نشدند." : "Blueprints could not be loaded.");
      }
      if (draftItems.status === "fulfilled") {
        setDrafts(draftItems.value);
      } else {
        errors.push(locale === "fa" ? "پیش‌نویس‌ها بارگیری نشدند." : "Drafts could not be loaded.");
      }
      if (errors.length) {
        setStatus(errors.join(" "));
      }
    });
  }, [locale]);

  const selectedMode = useMemo(() => MODE_OPTIONS.find((item) => item.key === mode) ?? MODE_OPTIONS[0], [mode]);
  const summary = useMemo(() => buildSummary(response, drafts), [drafts, response]);
  const pendingFollowUps = useMemo(() => resolveFollowUps(response), [response]);
  const visibleMessages = useMemo(() => [...seedMessages(locale), ...messages], [locale, messages]);

  useEffect(() => {
    document.querySelectorAll<HTMLElement>("[data-studio-chat-scroll]").forEach((element) => {
      element.scrollTop = element.scrollHeight;
    });
  }, [messages]);

  async function handleGenerate(nextPrompt = prompt, answerPatch?: Record<string, unknown>) {
    const cleanPrompt = nextPrompt.trim();
    if (!cleanPrompt || loading) {
      if (!cleanPrompt) {
        setStatus(locale === "fa" ? "ابتدا درخواست خود را بنویسید." : "Write a prompt before sending.");
      }
      return;
    }

    const inferredPatch = answerPatch ?? inferAnswerPatch(cleanPrompt, pendingFollowUps, answers);
    const nextAnswers = {
      ...answers,
      ...baseAnswers(locale),
      ...(selectedMode.appType ? { appType: selectedMode.appType } : {}),
      ...inferredPatch
    };
    const outgoingAttachments = attachments;
    const userMessage: StudioMessage = {
      id: createId("user"),
      role: "user",
      content: cleanPrompt,
      createdAtLabel: currentTimeLabel(),
      attachments: outgoingAttachments
    };
    const pendingMessageId = createId("assistant");
    const pendingMessage: StudioMessage = {
      id: pendingMessageId,
      role: "assistant",
      content: locale === "fa" ? "در حال تولید پیش‌نویس..." : "Generating the draft...",
      createdAtLabel: currentTimeLabel(),
      tone: "thinking"
    };

    setMessages((current) => [...current, userMessage, pendingMessage]);
    setLoading(true);
    setStatus(null);

    const request: GeneratePlatformAppRequest = {
      prompt: composePromptWithAttachments(cleanPrompt, outgoingAttachments),
      appType: selectedMode.appType,
      tenantKey: TENANT_KEY,
      siteKey: SITE_KEY,
      sessionId: sessionId ?? response?.sessionId ?? undefined,
      execute: false,
      answers: nextAnswers
    };

    try {
      const { generated, transportNotice } = await generateWithPreferredTransport(request);
      setResponse(generated);
      setAnswers(nextAnswers);
      setSessionId(generated.sessionId ?? sessionId);
      setPrompt("");
      setAttachments([]);
      setMessages((current) =>
        current.map((message) =>
          message.id === pendingMessageId
            ? {
                id: createId("assistant"),
                role: "assistant",
                content: assistantResponseText(generated, locale),
                createdAtLabel: currentTimeLabel(),
                response: generated
              }
            : message
        )
      );

      const followUps = resolveFollowUps(generated);
      const completionStatus = followUps.length
        ? locale === "fa"
          ? "برای تکمیل پیش‌نویس به پاسخ شما نیاز است."
          : "The draft needs your answer to continue."
        : locale === "fa"
          ? "پیش‌نویس جدید آماده شد."
          : "New draft generated.";
      setStatus([transportNotice, completionStatus].filter(Boolean).join(" "));
    } catch (error) {
      const message = error instanceof Error ? error.message : locale === "fa" ? "تولید پیش‌نویس ناموفق بود." : "Draft generation failed.";
      setMessages((current) =>
        current.map((item) =>
          item.id === pendingMessageId
            ? {
                id: createId("assistant"),
                role: "assistant",
                content: message,
                createdAtLabel: currentTimeLabel(),
                tone: "error"
              }
            : item
        )
      );
      setStatus(message);
    } finally {
      setLoading(false);
    }
  }

  function applyPrompt(item: (typeof QUICK_PROMPTS)[number]) {
    setPrompt(item.prompt);
    setStatus(locale === "fa" ? "پیشنهاد در کادر پیام قرار گرفت." : "Suggestion added to the composer.");
  }

  function enhancePrompt() {
    const cleanPrompt = prompt.trim();
    const enhanced = cleanPrompt
      ? `${cleanPrompt}\n\nInclude tenant and site scoped routes, dynamic entity definitions, BPM or automation steps when useful, bot delivery options, media/search readiness, and manual actions that must be reviewed before publishing.`
      : "Build a production-ready business app with tenant/site scoped routes, dynamic entity definitions, workflow automation, bot channels, media/search readiness, and a clear publish checklist.";
    setPrompt(enhanced);
    setStatus(locale === "fa" ? "درخواست کامل‌تر شد؛ قبل از ارسال بررسی کنید." : "Prompt enhanced. Review it before sending.");
  }

  async function handleFileSelection(event: ChangeEvent<HTMLInputElement>) {
    const files = Array.from(event.target.files ?? []);
    event.target.value = "";
    if (!files.length) {
      return;
    }

    const pendingItems = files.map((file) => ({
      id: createId("file"),
      fileName: file.name,
      mimeType: file.type || "application/octet-stream",
      sizeBytes: file.size,
      status: "uploading" as const
    }));
    setAttachments((current) => [...current, ...pendingItems]);
    setStatus(locale === "fa" ? "در حال آماده‌سازی فایل‌ها..." : "Preparing attachments...");

    await Promise.all(
      files.map(async (file, index) => {
        const item = pendingItems[index];
        const assetKey = `studio-${Date.now()}-${index}-${slug(file.name)}`;
        try {
          const uploaded = await prepareMediaUpload({
            assetKey,
            assetType: assetTypeFor(file.type),
            originalFileName: file.name,
            mimeType: file.type || "application/octet-stream",
            title: file.name,
            visibility: "PUBLIC",
            sizeBytes: file.size
          });
          setAttachments((current) =>
            current.map((attachment) =>
              attachment.id === item.id
                ? {
                    ...attachment,
                    status: "uploaded",
                    assetKey: uploaded.assetKey,
                    deliveryUrl: uploaded.deliveryUrl
                  }
                : attachment
            )
          );
        } catch (error) {
          setAttachments((current) =>
            current.map((attachment) =>
              attachment.id === item.id
                ? {
                    ...attachment,
                    status: "failed",
                    assetKey,
                    error: error instanceof Error ? error.message : "Upload preparation failed"
                  }
                : attachment
            )
          );
        }
      })
    );

    setStatus(locale === "fa" ? "فایل‌ها به متن گفت‌وگو اضافه شدند." : "Attachments added to the chat context.");
  }

  function removeAttachment(id: string) {
    setAttachments((current) => current.filter((item) => item.id !== id));
  }

  function answerFollowUp(question: FollowUpQuestion, answer: string) {
    if (!question.key) {
      setPrompt(answer);
      return;
    }
    handleGenerate(answer, { [question.key]: answer }).catch(() => null);
  }

  async function copyPreviewLink() {
    if (!summary.previewUrl) {
      setStatus(locale === "fa" ? "لینک پیش‌نمایش هنوز آماده نیست." : "Preview link is not ready yet.");
      return;
    }
    await navigator.clipboard?.writeText(summary.previewUrl).catch(() => null);
    setStatus(locale === "fa" ? "لینک پیش‌نمایش کپی شد." : "Preview link copied.");
  }

  function openPreview() {
    if (!summary.previewUrl) {
      setStatus(locale === "fa" ? "لینک پیش‌نمایش هنوز آماده نیست." : "Preview link is not ready yet.");
      return;
    }
    window.open(summary.previewUrl, "_blank", "noopener,noreferrer");
  }

  return (
    <PanelShell
      activeKey="studio"
      kicker="AI Studio"
      kickerFa="استودیوی هوش مصنوعی"
      title="Build your business app with AI"
      titleFa="کسب‌وکار خود را با هوش مصنوعی بسازید"
      subtitle="Create website, PWA, shop, CRM, BPM/forms, automation, and Telegram/Bale bots with a single prompt. Cyan turns ideas into production-ready apps."
      subtitleFa="وب‌سایت، PWA، فروشگاه، CRM، فرم‌های BPM، اتوماسیون و ربات‌های تلگرام/بله را با یک درخواست بسازید."
    >
      <input ref={fileInputRef} type="file" multiple className="studio-file-input" onChange={handleFileSelection} />

      <div className="desktop-only studio-page-grid">
        <section className="studio-main-column">
          <article className="studio-panel">
            <div className="studio-chat-shell" aria-live="polite" data-studio-chat-scroll>
              {visibleMessages.map((message) => (
                <ChatMessage
                  key={message.id}
                  message={message}
                  locale={locale}
                  pendingFollowUps={message.response ? resolveFollowUps(message.response) : []}
                  onAnswer={answerFollowUp}
                />
              ))}
            </div>

            <div className="studio-chip-row" aria-label={locale === "fa" ? "پیشنهادها" : "Suggestions"}>
              {QUICK_PROMPTS.map((item) => (
                <button key={item.key} type="button" className="studio-chip" onClick={() => applyPrompt(item)}>
                  <ChipIcon itemKey={item.key} />
                  <span>{locale === "fa" ? item.fa : item.en}</span>
                </button>
              ))}
            </div>

            <div className="studio-composer">
              <textarea
                value={prompt}
                onChange={(event) => setPrompt(event.target.value)}
                placeholder={locale === "fa" ? "اپ خود را توضیح دهید یا هر چیزی بپرسید..." : "Describe your app or ask anything..."}
                onKeyDown={(event) => {
                  if (event.key === "Enter" && (event.metaKey || event.ctrlKey)) {
                    event.preventDefault();
                    handleGenerate().catch(() => null);
                  }
                }}
              />

              {attachments.length ? (
                <div className="studio-attachment-list">
                  {attachments.map((attachment) => (
                    <div key={attachment.id} className={`studio-attachment ${attachment.status}`}>
                      <DocumentIcon size={16} />
                      <div>
                        <strong>{attachment.fileName}</strong>
                        <span>{attachment.status === "uploaded" ? attachment.assetKey : attachment.status === "uploading" ? "Preparing..." : attachment.error}</span>
                      </div>
                      <button type="button" aria-label={locale === "fa" ? "حذف فایل" : "Remove attachment"} onClick={() => removeAttachment(attachment.id)}>
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              ) : null}

              <div className="studio-composer-toolbar">
                <div className="studio-composer-actions">
                  <button type="button" className="studio-icon-btn" aria-label={locale === "fa" ? "پیوست فایل" : "Attach file"} onClick={() => fileInputRef.current?.click()}>
                    <DocumentIcon size={18} />
                  </button>
                  <button type="button" className="secondary-pill studio-enhance-btn" onClick={enhancePrompt}>
                    <SparkleIcon size={14} />
                    <span>{locale === "fa" ? "بهبود درخواست" : "Enhance prompt"}</span>
                  </button>
                </div>
                <div className="studio-send-cluster">
                  <label className="studio-smart-select">
                    <GlobeIcon size={16} />
                    <select value={mode} onChange={(event) => setMode(event.target.value)} aria-label={locale === "fa" ? "حالت تولید" : "Generation mode"}>
                      {MODE_OPTIONS.map((item) => (
                        <option key={item.key} value={item.key}>
                          {locale === "fa" ? item.fa : item.en}
                        </option>
                      ))}
                    </select>
                  </label>
                  <button type="button" className="studio-send-btn" onClick={() => handleGenerate().catch(() => null)} disabled={loading} aria-label={locale === "fa" ? "ارسال" : "Send"}>
                    <ArrowRightIcon size={22} />
                  </button>
                </div>
              </div>
              <div className="muted-block studio-disclaimer">
                {locale === "fa" ? "Cyan AI ممکن است اشتباه کند. خروجی را پیش از انتشار بررسی کنید." : "Cyan AI can make mistakes. Please review the output."}
              </div>
              {status ? <div className="studio-status-note">{status}</div> : null}
            </div>
          </article>

          <section className="studio-summary-grid">
            <article className="studio-status-card">
              <div className="studio-card-icon code-icon">{"</>"}</div>
              <div>
                <strong>{locale === "fa" ? "پیش‌نویس DSL" : "Draft DSL"}</strong>
                <span className="muted-block">{summary.fileName}</span>
              </div>
              <button type="button" className="text-link" onClick={() => setInspector("dsl")}>
                {locale === "fa" ? "مشاهده فایل" : "View file"}
              </button>
              <span className="studio-status-dot success">{locale === "fa" ? "تولید شد" : "Generated"}</span>
            </article>
            <article className="studio-status-card">
              <div className="studio-card-icon cube-icon">
                <ShopIcon size={20} />
              </div>
              <div>
                <strong>{locale === "fa" ? "سرویس‌ها" : "Services"}</strong>
                <span className="muted-block">
                  {summary.services} {locale === "fa" ? "سرویس پیکربندی شده" : "services configured"}
                </span>
              </div>
              <button type="button" className="text-link" onClick={() => setInspector("services")}>
                {locale === "fa" ? "مدیریت سرویس‌ها" : "Manage services"}
              </button>
              <span className="studio-status-dot success">{locale === "fa" ? "آماده" : "Ready"}</span>
            </article>
            <article className="studio-status-card">
              <div className="studio-card-icon shield-icon">
                <ShieldIcon size={20} />
              </div>
              <div>
                <strong>{locale === "fa" ? "آمادگی انتشار" : "Publish readiness"}</strong>
                <span className="muted-block">
                  {summary.readiness}% {locale === "fa" ? "آماده انتشار" : "ready to publish"}
                </span>
              </div>
              <button type="button" className="text-link" onClick={() => setInspector("checklist")}>
                {locale === "fa" ? "چک‌لیست" : "View checklist"}
              </button>
              <div className="studio-progress">
                <span style={{ width: `${summary.readiness}%` }} />
              </div>
            </article>
            <article className="studio-status-card">
              <div className="studio-card-icon globe-icon">
                <GlobeIcon size={20} />
              </div>
              <div>
                <strong>{locale === "fa" ? "لینک پیش‌نمایش" : "Preview link"}</strong>
                <span className="muted-block studio-preview-link">{summary.previewLabel}</span>
              </div>
              <button type="button" className="text-link" onClick={openPreview}>
                {locale === "fa" ? "باز کردن" : "Open preview"}
              </button>
              <button type="button" className="studio-copy-btn" aria-label={locale === "fa" ? "کپی لینک" : "Copy preview link"} onClick={copyPreviewLink}>
                ⧉
              </button>
            </article>
          </section>
        </section>

        <aside className="studio-side-column">
          <section className="panel-card studio-sidebar">
            <div className="card-title-row">
              <div className="studio-sidebar-heading">
                <SparkleIcon size={18} />
                <h3>{locale === "fa" ? "خلاصه پیش‌نویس تولیدشده" : "Generated draft summary"}</h3>
              </div>
              <span className="status-pill info">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
            </div>
            <strong className="studio-sidebar-title">{summary.title}</strong>
            <p className="muted studio-sidebar-copy">
              {summary.description ||
                (locale === "fa"
                  ? "اپ کامل با کاتالوگ، سبد خرید، پرداخت و پیگیری سفارش."
                  : "A complete app with catalog, cart, checkout, payments and order tracking.")}
            </p>

            <div className="studio-module-tiles">
              {MODULE_TILES.map((tile) => {
                const count = countForTile(tile.key, summary);
                return (
                  <div key={tile.key} className="studio-module-tile">
                    <div className="studio-module-head">
                      <span className={`studio-module-icon ${tile.tone}`}>
                        <TileIcon itemKey={tile.key} />
                      </span>
                      <span className="studio-module-check" aria-hidden="true">✓</span>
                    </div>
                    <strong>{locale === "fa" ? tile.fa : tile.en}</strong>
                    <span className="muted-block">
                      {count} {locale === "fa" ? tile.suffixFa : tile.suffixEn}
                    </span>
                  </div>
                );
              })}
            </div>

            <div className="studio-sidebar-actions">
              <Link href="/maker" className="secondary-pill wide-pill">
                {locale === "fa" ? "باز کردن در Maker" : "Open in Maker"}
              </Link>
              <Link href={summary.draftId ? `/projects/${summary.draftId}` : "/maker"} className="primary-pill wide-pill">
                <span>{locale === "fa" ? "ادامه ساخت" : "Continue building"}</span>
                <ArrowRightIcon size={18} />
              </Link>
            </div>
          </section>

          <section className="panel-card studio-sidebar recent-card">
            <div className="card-title-row studio-recent-head">
              <div className="studio-sidebar-heading">
                <span className="studio-clock-icon">◷</span>
                <h3>{locale === "fa" ? "آخرین تولیدها" : "Recent generations"}</h3>
              </div>
              <Link href="/projects" className="text-link">
                {locale === "fa" ? "مشاهده همه" : "View all"}
              </Link>
            </div>
            <div className="studio-recent-list">
              {recentItems(drafts, response).map((draft, index) => (
                <Link key={`${draft.id}-${index}`} href={draft.href} className="studio-recent-item">
                  <span className="studio-recent-branch" aria-hidden="true">⌁</span>
                  <div>
                    <strong>{draft.title}</strong>
                    <span className="muted-block">{draft.time}</span>
                  </div>
                  <span className={`studio-recent-dot ${index === 1 ? "warning" : "success"}`} aria-hidden="true" />
                </Link>
              ))}
              {!recentItems(drafts, response).length ? (
                <div className="studio-recent-item empty">
                  <div>
                    <strong>{locale === "fa" ? "پیش‌نویسی از backend برنگشته است" : "No drafts returned by backend"}</strong>
                    <span className="muted-block">{locale === "fa" ? "اولین خروجی پس از ارسال اینجا نمایش داده می‌شود." : "The first generated draft will appear here."}</span>
                  </div>
                </div>
              ) : null}
            </div>
          </section>
        </aside>
      </div>

      <div className="mobile-only mobile-screen studio-mobile-screen">
        <div className="mobile-screen-header">
          <strong>{locale === "fa" ? "استودیوی هوش مصنوعی" : "AI Studio"}</strong>
          <span className="pill">{pendingFollowUps.length ? (locale === "fa" ? "نیازمند پاسخ" : "Needs answer") : locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
        </div>
        <div className="mobile-chat-thread" data-studio-chat-scroll>
          {visibleMessages.map((message) => (
            <ChatMessage
              key={message.id}
              message={message}
              locale={locale}
              pendingFollowUps={message.response ? resolveFollowUps(message.response) : []}
              onAnswer={answerFollowUp}
            />
          ))}
          <div className="studio-chip-row">
            {QUICK_PROMPTS.slice(0, 4).map((item) => (
              <button key={item.key} type="button" className="studio-chip" onClick={() => applyPrompt(item)}>
                <ChipIcon itemKey={item.key} />
                <span>{locale === "fa" ? item.fa : item.en}</span>
              </button>
            ))}
          </div>
          <div className="studio-composer">
            <textarea value={prompt} onChange={(event) => setPrompt(event.target.value)} placeholder={locale === "fa" ? "اپ خود را توضیح دهید..." : "Describe your app or ask anything..."} />
            <div className="studio-composer-toolbar">
              <button type="button" className="studio-icon-btn" aria-label={locale === "fa" ? "پیوست فایل" : "Attach file"} onClick={() => fileInputRef.current?.click()}>
                <DocumentIcon size={18} />
              </button>
              <button type="button" className="primary-pill" onClick={() => handleGenerate().catch(() => null)} disabled={loading}>
                {loading ? (locale === "fa" ? "در حال تولید..." : "Generating...") : locale === "fa" ? "ارسال" : "Send"}
              </button>
            </div>
            {status ? <div className="studio-status-note">{status}</div> : null}
          </div>
        </div>
      </div>

      {inspector ? <InspectorModal mode={inspector} locale={locale} summary={summary} response={response} onClose={() => setInspector(null)} /> : null}
    </PanelShell>
  );
}

function ChatMessage({
  message,
  locale,
  pendingFollowUps,
  onAnswer
}: {
  message: StudioMessage;
  locale: "en" | "fa";
  pendingFollowUps: FollowUpQuestion[];
  onAnswer: (question: FollowUpQuestion, answer: string) => void;
}) {
  const isUser = message.role === "user";
  return (
    <div className={`studio-chat-message ${isUser ? "outbound" : ""} ${message.tone ?? ""}`}>
      {!isUser ? <div className="studio-chat-avatar ai" aria-hidden="true" /> : null}
      <div className="studio-message-stack">
        <div className="studio-message-bubble">
          {message.tone === "thinking" ? <span className="studio-typing-dot" aria-hidden="true" /> : null}
          <p>{message.content}</p>
          {message.attachments?.length ? (
            <div className="studio-message-attachments">
              {message.attachments.map((attachment) => (
                <span key={attachment.id}>
                  <DocumentIcon size={14} />
                  {attachment.fileName}
                </span>
              ))}
            </div>
          ) : null}
        </div>
        {message.response ? (
          <ResponseDetails response={message.response} locale={locale} pendingFollowUps={pendingFollowUps} onAnswer={onAnswer} />
        ) : null}
        <div className="studio-chat-meta">
          <span>{message.createdAtLabel}</span>
          {isUser ? <span className="studio-read-receipt" aria-hidden="true">✓✓</span> : null}
        </div>
      </div>
    </div>
  );
}

function ResponseDetails({
  response,
  locale,
  pendingFollowUps,
  onAnswer
}: {
  response: GeneratePlatformAppResponse;
  locale: "en" | "fa";
  pendingFollowUps: FollowUpQuestion[];
  onAnswer: (question: FollowUpQuestion, answer: string) => void;
}) {
  const dsl = response.dsl;
  const publicApis = safeArray(dsl.delivery?.publicApis);
  const botApis = safeArray(dsl.delivery?.botApis);
  return (
    <div className="studio-response-card">
      <div className="studio-response-grid">
        <div>
          <span>draftId</span>
          <strong>{response.draftId ?? "not persisted"}</strong>
        </div>
        <div>
          <span>sessionId</span>
          <strong>{response.sessionId ?? "none"}</strong>
        </div>
        <div>
          <span>{locale === "fa" ? "مسیرها" : "routes"}</span>
          <strong>{safeArray(dsl.routes).length}</strong>
        </div>
        <div>
          <span>{locale === "fa" ? "سرویس‌ها" : "delivery"}</span>
          <strong>{publicApis.length + botApis.length}</strong>
        </div>
      </div>

      {pendingFollowUps.length ? (
        <div className="studio-followup-block">
          <div className="studio-response-label">followUpQuestions</div>
          {pendingFollowUps.map((question) => (
            <div key={question.key || question.prompt} className="studio-question-card">
              <div className="studio-question-head">
                <span>{question.key || "question"}</span>
                {question.required ? <strong>{locale === "fa" ? "الزامی" : "Required"}</strong> : null}
              </div>
              <p>{question.prompt}</p>
              {question.reason ? <span className="muted-block">{question.reason}</span> : null}
              {question.suggestedAnswers?.length ? (
                <div className="studio-answer-row">
                  {question.suggestedAnswers.map((answer) => (
                    <button key={answer} type="button" onClick={() => onAnswer(question, answer)}>
                      {answer}
                    </button>
                  ))}
                </div>
              ) : null}
            </div>
          ))}
        </div>
      ) : null}

      {response.nextQuestions?.length ? (
        <div className="studio-next-question-block">
          <div className="studio-response-label">nextQuestions</div>
          {response.nextQuestions.map((question) => (
            <span key={question}>{question}</span>
          ))}
        </div>
      ) : null}
    </div>
  );
}

function InspectorModal({
  mode,
  locale,
  summary,
  response,
  onClose
}: {
  mode: Exclude<InspectorMode, null>;
  locale: "en" | "fa";
  summary: ReturnType<typeof buildSummary>;
  response: GeneratePlatformAppResponse | null;
  onClose: () => void;
}) {
  const dsl = summary.dsl;
  return (
    <div className="studio-modal-backdrop" role="presentation" onClick={onClose}>
      <section className="studio-modal" role="dialog" aria-modal="true" aria-label={mode} onClick={(event) => event.stopPropagation()}>
        <div className="card-title-row">
          <h3>{modalTitle(mode, locale)}</h3>
          <button type="button" className="studio-icon-btn" onClick={onClose} aria-label={locale === "fa" ? "بستن" : "Close"}>
            ×
          </button>
        </div>
        {mode === "dsl" ? <pre>{JSON.stringify(dsl ?? response, null, 2)}</pre> : null}
        {mode === "services" ? (
          <div className="studio-modal-list">
            <strong>{locale === "fa" ? "APIهای عمومی" : "Public APIs"}</strong>
            {safeArray(dsl?.delivery?.publicApis).map((item) => <span key={item}>{item}</span>)}
            <strong>{locale === "fa" ? "APIهای بات" : "Bot APIs"}</strong>
            {safeArray(dsl?.delivery?.botApis).map((item) => <span key={item}>{item}</span>)}
            <strong>{locale === "fa" ? "تعریف‌ها" : "Entities"}</strong>
            {safeArray(dsl?.entities).map((item, index) => <span key={index}>{String(item.entityKey ?? item.key ?? item.name ?? `entity-${index + 1}`)}</span>)}
          </div>
        ) : null}
        {mode === "checklist" ? (
          <div className="studio-modal-list">
            <span>{summary.readiness}% {locale === "fa" ? "آماده انتشار" : "ready to publish"}</span>
            {safeArray(dsl?.manualActions).map((item) => <span key={item}>{item}</span>)}
            {!safeArray(dsl?.manualActions).length ? <span>{locale === "fa" ? "اقدام دستی ثبت نشده است." : "No manual actions were returned."}</span> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
}

async function generateWithPreferredTransport(request: GeneratePlatformAppRequest) {
  if (!hasAiStudioWebSocket()) {
    return { generated: await generatePlatformApp(request), transportNotice: "" };
  }

  try {
    return { generated: await generatePlatformAppOverWebSocket(request), transportNotice: "WebSocket" };
  } catch {
    return {
      generated: await generatePlatformApp(request),
      transportNotice: "WebSocket unavailable; used REST API."
    };
  }
}

function buildSummary(response: GeneratePlatformAppResponse | null, drafts: ClientAppDraft[]) {
  const activeDraft = drafts[0] ?? null;
  const dsl = response?.dsl ?? activeDraft?.resolvedDsl ?? null;
  const routes = safeArray(dsl?.routes).length;
  const entities = safeArray(dsl?.entities);
  const flows = safeArray(dsl?.flows).length;
  const publicApis = safeArray(dsl?.delivery?.publicApis);
  const botApis = safeArray(dsl?.delivery?.botApis);
  const services = publicApis.length + botApis.length + flows;
  const followUps = resolveFollowUps(response);
  const readiness = dsl ? Math.max(8, Math.min(100, Math.round(((routes + entities.length + services + flows) / 24) * 100) - followUps.length * 8)) : 0;
  const draftId = response?.draftId ?? activeDraft?.draftId ?? "";
  const title = dsl?.app?.title ?? activeDraft?.title ?? "Shop App (v0.1)";
  const siteKey = dsl?.app?.siteKey ?? activeDraft?.siteKey ?? SITE_KEY;
  const appKey = dsl?.app?.appKey ?? draftId ?? "shop-app-v0-1";
  const previewUrl = dsl ? `https://preview.cyan.app/${siteKey}/${appKey}` : "";

  return {
    dsl,
    draftId,
    title,
    description: activeDraft?.latestIntent ?? "",
    routes,
    entities: entities.length,
    flows,
    services,
    publicApis,
    botApis,
    readiness,
    fileName: `${appKey}.dsl`,
    previewUrl,
    previewLabel: previewUrl || "—"
  };
}

function resolveFollowUps(response: GeneratePlatformAppResponse | null): FollowUpQuestion[] {
  if (!response) {
    return [];
  }
  if (response.followUpQuestions?.length) {
    return response.followUpQuestions;
  }
  return (response.nextQuestions ?? []).map((prompt, index) => ({
    key: "",
    prompt,
    required: true,
    reason: null,
    suggestedAnswers: index === 0 ? [] : []
  }));
}

function countForTile(tileKey: string, summary: ReturnType<typeof buildSummary>) {
  switch (tileKey) {
    case "website":
      return summary.routes;
    case "shop":
      return summary.entities;
    case "crm":
      return Math.max(0, Math.ceil(summary.entities / 2));
    case "forms":
      return summary.flows;
    case "flow":
      return summary.flows + summary.botApis.length;
    case "bot":
      return summary.botApis.length;
    default:
      return 0;
  }
}

function seedMessages(locale: "en" | "fa"): StudioMessage[] {
  return [
    {
      id: "seed-assistant-1",
      role: "assistant",
      content: locale === "fa" ? "سلام، من Cyan AI هستم. امروز چه چیزی می‌خواهید بسازید؟" : "Hi! I'm Cyan AI. What would you like to build today?",
      createdAtLabel: "10:24 AM"
    },
    {
      id: "seed-user-1",
      role: "user",
      content:
        locale === "fa"
          ? "می‌خواهم یک فروشگاه با کاتالوگ محصول، سبد خرید، پرداخت و پیگیری سفارش بسازم."
          : "I want a shop with product catalog, cart, checkout, payments and order tracking.",
      createdAtLabel: "10:24 AM"
    },
    {
      id: "seed-assistant-2",
      role: "assistant",
      content:
        locale === "fa"
          ? "عالی، پیش‌نویس فروشگاه با کاتالوگ، سبد خرید، پرداخت امن و پیگیری سفارش را آماده می‌کنم."
          : "Great! I'll generate a shop app with catalog, cart, secure checkout, payments, and order tracking. Here's a draft for you.",
      createdAtLabel: "10:25 AM"
    }
  ];
}

function inferAnswerPatch(prompt: string, pendingFollowUps: FollowUpQuestion[], answers: Record<string, unknown>) {
  const unanswered = pendingFollowUps.filter((question) => question.key && answers[question.key] == null);
  if (unanswered.length === 1) {
    return { [unanswered[0].key]: prompt };
  }
  return {};
}

function baseAnswers(locale: "en" | "fa") {
  return {
    channels: ["website", "pwa", "telegram"],
    locale
  };
}

function assistantResponseText(response: GeneratePlatformAppResponse, locale: "en" | "fa") {
  const followUps = resolveFollowUps(response);
  if (followUps.length) {
    return locale === "fa"
      ? "پیش‌نویس آماده است، اما برای ادامه باید به سؤال‌های زیر پاسخ دهید."
      : "The draft is ready, but I need the answer below before it can be completed.";
  }
  return locale === "fa" ? "پیش‌نویس آماده است و می‌توانید ساخت را ادامه دهید." : "The draft is ready. You can continue building from here.";
}

function composePromptWithAttachments(prompt: string, attachments: StudioAttachment[]) {
  if (!attachments.length) {
    return prompt;
  }
  const fileContext = attachments
    .map((file) => `- ${file.fileName} (${file.assetKey ?? "local"}, ${file.mimeType}, ${formatBytes(file.sizeBytes)}, ${file.status})`)
    .join("\n");
  return `${prompt}\n\nAttached files:\n${fileContext}`;
}

function recentItems(drafts: ClientAppDraft[], response: GeneratePlatformAppResponse | null) {
  const generated = response
    ? [
        {
          id: response.draftId ?? "latest-response",
          title: response.dsl.app.title ?? "Generated app",
          time: "Just now",
          href: response.draftId ? `/projects/${response.draftId}` : "/projects/new"
        }
      ]
    : [];
  return [
    ...generated,
    ...drafts.slice(0, 5).map((draft) => ({
      id: draft.draftId,
      title: draft.title,
      time: draft.updatedAt ? relativeTime(draft.updatedAt) : "Just now",
      href: `/projects/${draft.draftId}`
    }))
  ].slice(0, 5);
}

function safeArray<T>(value: T[] | null | undefined): T[] {
  return Array.isArray(value) ? value : [];
}

function currentTimeLabel() {
  return new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(new Date());
}

function createId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function slug(value: string) {
  return value.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "") || "attachment";
}

function assetTypeFor(mimeType: string) {
  if (mimeType.startsWith("image/")) return "IMAGE";
  if (mimeType.startsWith("video/")) return "VIDEO";
  if (mimeType.startsWith("audio/")) return "AUDIO";
  if (mimeType.includes("pdf") || mimeType.includes("document") || mimeType.includes("text")) return "DOCUMENT";
  return "OTHER";
}

function formatBytes(size: number) {
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${Math.round(size / 1024)} KB`;
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

function relativeTime(value: string) {
  const time = new Date(value).getTime();
  if (!Number.isFinite(time)) {
    return value;
  }
  const diff = Date.now() - time;
  if (diff < 60_000) return "Just now";
  if (diff < 3_600_000) return `${Math.round(diff / 60_000)}m ago`;
  if (diff < 86_400_000) return `${Math.round(diff / 3_600_000)}h ago`;
  return `${Math.round(diff / 86_400_000)}d ago`;
}

function modalTitle(mode: Exclude<InspectorMode, null>, locale: "en" | "fa") {
  if (mode === "dsl") return locale === "fa" ? "فایل DSL" : "Draft DSL";
  if (mode === "services") return locale === "fa" ? "سرویس‌ها" : "Configured services";
  return locale === "fa" ? "چک‌لیست انتشار" : "Publish checklist";
}

function ChipIcon({ itemKey }: { itemKey: string }) {
  if (itemKey === "shop") return <ShopIcon size={15} />;
  if (itemKey === "crm") return <UsersIcon size={15} />;
  if (itemKey === "bpm") return <DocumentIcon size={15} />;
  if (itemKey === "bot") return <PlaneIcon size={15} />;
  if (itemKey === "pwa") return <PhoneDeviceIcon size={15} />;
  return <GlobeIcon size={15} />;
}

function TileIcon({ itemKey }: { itemKey: string }) {
  if (itemKey === "website") return <GlobeIcon size={18} />;
  if (itemKey === "shop") return <ShopIcon size={18} />;
  if (itemKey === "crm") return <UsersIcon size={18} />;
  if (itemKey === "forms") return <DocumentIcon size={18} />;
  if (itemKey === "bot") return <SparkleIcon size={18} />;
  return <LightningIcon size={18} />;
}
