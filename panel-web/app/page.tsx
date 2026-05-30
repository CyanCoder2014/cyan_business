"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { dashboardCapabilityCards } from "@/lib/panel-fixtures";
import { listFlows, type DynamicFlowDefinition } from "@/lib/bpm-api";
import { listBotIntegrations, listBotMessages, listClientDrafts } from "@/lib/platform-api";
import type { BotChannelIntegration, BotOutboundMessage, ClientAppDraft } from "@/lib/types";

export default function HomePage() {
  const { locale } = usePanel();
  const [drafts, setDrafts] = useState<ClientAppDraft[]>([]);
  const [integrations, setIntegrations] = useState<BotChannelIntegration[]>([]);
  const [messages, setMessages] = useState<BotOutboundMessage[]>([]);
  const [flows, setFlows] = useState<DynamicFlowDefinition[]>([]);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    const scope = { tenantKey: "tenant-demo", siteKey: "site-commerce" };
    Promise.allSettled([
      listClientDrafts(scope),
      listBotIntegrations(scope),
      listBotMessages(scope),
      listFlows(scope)
    ]).then(([draftsResult, integrationsResult, messagesResult, flowsResult]) => {
      const errors: string[] = [];

      if (draftsResult.status === "fulfilled") {
        setDrafts(draftsResult.value);
      } else {
        errors.push(locale === "fa" ? "پیش‌نویس‌ها بارگیری نشدند." : "Drafts could not be loaded.");
      }
      if (integrationsResult.status === "fulfilled") {
        setIntegrations(integrationsResult.value);
      } else {
        errors.push(locale === "fa" ? "یکپارچگی‌های بات بارگیری نشدند." : "Bot integrations could not be loaded.");
      }
      if (messagesResult.status === "fulfilled") {
        setMessages(messagesResult.value);
      } else {
        errors.push(locale === "fa" ? "پیام‌های بات بارگیری نشدند." : "Bot messages could not be loaded.");
      }
      if (flowsResult.status === "fulfilled") {
        setFlows(flowsResult.value);
      } else {
        errors.push(locale === "fa" ? "فلوها بارگیری نشدند." : "Flows could not be loaded.");
      }

      setStatus(errors.length ? errors.join(" ") : null);
    });
  }, [locale]);

  const latestDraft = useMemo(() => sortByUpdatedAt(drafts)[0] ?? null, [drafts]);
  const activeIntegrations = integrations.filter((item) => item.active);
  const deliveredMessages = messages.filter((item) => item.status.toUpperCase().includes("DELIVER"));
  const failedMessages = messages.filter((item) => item.status.toUpperCase().includes("FAIL"));
  const stats = [
    {
      label: locale === "fa" ? "پیش‌نویس‌ها" : "Drafts",
      value: String(drafts.length),
      delta: latestDraft?.status ?? (locale === "fa" ? "بدون پیش‌نویس" : "No drafts")
    },
    {
      label: locale === "fa" ? "فلوها" : "Flows",
      value: String(flows.length),
      delta: `${flows.filter((item) => item.active).length} ${locale === "fa" ? "فعال" : "active"}`
    },
    {
      label: locale === "fa" ? "کانال‌های بات" : "Bot channels",
      value: String(activeIntegrations.length),
      delta: `${integrations.length} ${locale === "fa" ? "ثبت‌شده" : "registered"}`
    },
    {
      label: locale === "fa" ? "ارسال پیام" : "Message deliveries",
      value: String(deliveredMessages.length),
      delta: `${failedMessages.length} ${locale === "fa" ? "ناموفق" : "failed"}`
    }
  ];
  const summaryCards = buildSummaryCards(latestDraft, locale);
  const activities = buildActivities({ drafts, integrations, messages, flows, locale });

  return (
    <PanelShell
      activeKey="dashboard"
      title="Launch your business app with AI"
      titleFa="کسب‌وکار خود را با هوش مصنوعی بسازید"
      subtitle="Generate websites, shops, CRM portals, BPM forms, bots, and customer-facing channels from one workspace."
      subtitleFa="وب‌سایت، فروشگاه، CRM، فرم‌ها، ربات‌ها و کانال‌های ارتباطی را از یک فضای کاری واحد بسازید."
    >
      <div className="dashboard-grid">
        <section className="dashboard-main">
          <article className="hero-banner dashboard-hero">
            <div className="split-row">
              <span className="status-pill info">{latestDraft?.status ?? (locale === "fa" ? "بدون پیش‌نویس" : "No draft")}</span>
            </div>
            <div className="dashboard-hero-body">
              <div>
                <h2 style={{ fontSize: "clamp(2rem, 4vw, 3.1rem)", marginBottom: 14 }}>
                  {latestDraft?.title ?? (locale === "fa" ? "هنوز پیش‌نویسی ایجاد نشده است" : "No generated draft yet")}
                </h2>
                <p className="muted" style={{ maxWidth: "44ch", lineHeight: 1.7 }}>
                  {latestDraft
                    ? latestDraft.latestIntent
                    : locale === "fa"
                      ? "برای دیدن خلاصه واقعی، یک پیش‌نویس از استودیوی هوش مصنوعی یا قالب‌ها ایجاد کنید."
                      : "Generate a draft from AI Studio or Blueprints to see real workspace state here."}
                </p>
                <div className="pill-row" style={{ margin: "18px 0" }}>
                  <span className="pill">{formatCount(latestDraft?.resolvedDsl?.routes.length ?? 0, locale, locale === "fa" ? "مسیر" : "routes")}</span>
                  <span className="pill">{formatCount(latestDraft?.resolvedDsl?.entities.length ?? 0, locale, locale === "fa" ? "ماژول" : "modules")}</span>
                  <span className="pill">{formatCount(activeIntegrations.length, locale, locale === "fa" ? "یکپارچگی" : "integrations")}</span>
                </div>
                <div className="toolbar-row">
                  <Link className="primary-pill wide-pill" href={latestDraft ? `/projects/${latestDraft.draftId}` : "/projects/new"}>
                    {latestDraft ? (locale === "fa" ? "باز کردن پیش‌نویس" : "Open draft") : locale === "fa" ? "شروع ساخت" : "Start building"}
                  </Link>
                  <button type="button" className="icon-pill">
                    ...
                  </button>
                </div>
              </div>
              <div className="dashboard-orb">
                <div className="dashboard-orb-icon">👜</div>
              </div>
            </div>
          </article>

          <section className="feature-grid dashboard-capability-grid" style={{ marginTop: 18 }}>
            {dashboardCapabilityCards.map((card) => (
              <Link key={card.key} href={cardHref(card.key)} className="capability-card app-tile">
                <span className="tile-icon">{card.icon}</span>
                <strong>{locale === "fa" ? card.titleFa : card.titleEn}</strong>
                <p className="muted">{locale === "fa" ? card.descFa : card.descEn}</p>
              </Link>
            ))}
          </section>

          <section className="stats-grid dashboard-stat-grid" style={{ marginTop: 18 }}>
            {stats.map((stat) => (
              <article key={stat.label} className="stat-card">
                <span className="muted">{stat.label}</span>
                <strong>{locale === "fa" ? toFaDigits(stat.value) : stat.value}</strong>
                <div className="stat-delta">{locale === "fa" ? toFaDigits(stat.delta) : stat.delta}</div>
              </article>
            ))}
          </section>
        </section>

        <aside className="dashboard-rail">
          <section className="panel-card">
            <div className="card-title-row">
              <h3>{locale === "fa" ? "خلاصه پیش‌نویس تولیدشده" : "Generated draft summary"}</h3>
              <span className="status-pill info">{latestDraft?.status ?? (locale === "fa" ? "خالی" : "Empty")}</span>
            </div>
            <strong style={{ display: "block", marginTop: 18, fontSize: "1.25rem" }}>
              {latestDraft?.title ?? (locale === "fa" ? "پیش‌نویس موجود نیست" : "No draft available")}
            </strong>
            <p className="muted">
              {latestDraft
                ? latestDraft.latestIntent
                : locale === "fa"
                  ? "بعد از ساخت اولین پیش‌نویس، مسیرها، موجودیت‌ها و کانال‌ها در اینجا خلاصه می‌شوند."
                  : "After the first draft is generated, routes, entities, and channels will be summarized here."}
            </p>
            <div className="summary-grid dashboard-summary-grid" style={{ marginTop: 16 }}>
              {summaryCards.map(([title, meta]) => (
                <div key={title} className="mini-card summary-mini">
                  <strong>{title}</strong>
                  <span className="muted-block">{locale === "fa" ? toFaDigits(meta) : meta}</span>
                </div>
              ))}
            </div>
            <div className="toolbar-row" style={{ marginTop: 16 }}>
              <Link href="/maker" className="secondary-pill">
                {locale === "fa" ? "باز کردن در سازنده" : "Open in Maker"}
              </Link>
              <Link href="/projects/new" className="primary-pill">
                {latestDraft ? (locale === "fa" ? "پیش‌نویس جدید" : "New draft") : locale === "fa" ? "ایجاد پیش‌نویس" : "Create draft"}
              </Link>
            </div>
          </section>

          <section className="panel-card" style={{ marginTop: 18 }}>
            <div className="card-title-row">
              <h3>{locale === "fa" ? "آخرین فعالیت‌ها" : "Recent activity"}</h3>
              <Link href="/roadmap" className="muted">
                {locale === "fa" ? "مشاهده همه" : "View all"}
              </Link>
            </div>
            {status ? <div className="status-pill info" style={{ marginTop: 12 }}>{status}</div> : null}
            <div className="activity-list" style={{ marginTop: 16 }}>
              {activities.length ? activities.map((item) => (
                <div key={item.en} className="activity-item">
                  <strong>{locale === "fa" ? item.fa : item.en}</strong>
                  <span className="muted-block">{locale === "fa" ? item.timeFa : item.timeEn}</span>
                </div>
              )) : (
                <div className="activity-item">
                  <strong>{locale === "fa" ? "فعالیتی از سرویس‌ها دریافت نشد" : "No service activity available yet"}</strong>
                  <span className="muted-block">{locale === "fa" ? "پس از ایجاد پیش‌نویس یا ارسال پیام، اینجا به‌روزرسانی می‌شود." : "This updates after drafts, flows, or bot deliveries are created."}</span>
                </div>
              )}
            </div>
          </section>
        </aside>
      </div>
    </PanelShell>
  );
}

function buildSummaryCards(latestDraft: ClientAppDraft | null, locale: "en" | "fa") {
  const dsl = latestDraft?.resolvedDsl;
  return [
    [locale === "fa" ? "مسیرها" : "Routes", `${dsl?.routes.length ?? 0} ${locale === "fa" ? "مسیر" : "routes"}`],
    [locale === "fa" ? "موجودیت‌ها" : "Entities", `${dsl?.entities.length ?? 0} ${locale === "fa" ? "موجودیت" : "entities"}`],
    [locale === "fa" ? "فلوها" : "Flows", `${dsl?.flows.length ?? 0} ${locale === "fa" ? "فلو" : "flows"}`],
    [locale === "fa" ? "رابط‌های عمومی" : "Public APIs", `${dsl?.delivery.publicApis.length ?? 0} APIs`],
    [locale === "fa" ? "رابط‌های بات" : "Bot APIs", `${dsl?.delivery.botApis.length ?? 0} APIs`],
    [locale === "fa" ? "اقدام دستی" : "Manual actions", `${latestDraft?.manualActions.length ?? 0}`]
  ];
}

function buildActivities({
  drafts,
  integrations,
  messages,
  flows,
  locale
}: {
  drafts: ClientAppDraft[];
  integrations: BotChannelIntegration[];
  messages: BotOutboundMessage[];
  flows: DynamicFlowDefinition[];
  locale: "en" | "fa";
}) {
  return [
    ...drafts.map((draft) => ({
      en: `Draft ${draft.title} is ${draft.status.toLowerCase()}.`,
      fa: `پیش‌نویس ${draft.title} در وضعیت ${draft.status} است.`,
      at: draft.updatedAt
    })),
    ...integrations.map((integration) => ({
      en: `${integration.channel} integration ${integration.integrationKey} ${integration.active ? "is active" : "is inactive"}.`,
      fa: `یکپارچگی ${integration.channel} با کلید ${integration.integrationKey} ${integration.active ? "فعال است" : "غیرفعال است"}.`,
      at: integration.updatedAt
    })),
    ...messages.map((message) => ({
      en: `${message.channel} delivery ${message.status.toLowerCase()} for ${message.integrationKey}.`,
      fa: `ارسال ${message.channel} برای ${message.integrationKey} با وضعیت ${message.status} ثبت شد.`,
      at: message.updatedAt ?? message.createdAt
    })),
    ...flows.map((flow) => ({
      en: `Flow ${flow.name} version ${flow.version ?? 1} ${flow.active ? "is active" : "is saved"}.`,
      fa: `فلو ${flow.name} نسخه ${flow.version ?? 1} ${flow.active ? "فعال است" : "ذخیره شده است"}.`,
      at: flow.updatedAt
    }))
  ]
    .filter((item) => item.at)
    .sort((a, b) => Date.parse(b.at ?? "") - Date.parse(a.at ?? ""))
    .slice(0, 5)
    .map((item) => ({
      en: item.en,
      fa: item.fa,
      timeEn: formatRelativeTime(item.at ?? ""),
      timeFa: toFaDigits(formatRelativeTime(item.at ?? "", true))
    }));
}

function cardHref(key: string) {
  switch (key) {
    case "studio":
      return "/projects/new";
    case "templates":
      return "/projects";
    case "maker":
      return "/maker";
    case "flows":
      return "/flows";
    case "apps":
      return "/integrations";
    case "data":
      return "/data";
    default:
      return "/";
  }
}

function sortByUpdatedAt<T extends { updatedAt?: string }>(items: T[]) {
  return [...items].sort((a, b) => Date.parse(b.updatedAt ?? "") - Date.parse(a.updatedAt ?? ""));
}

function formatCount(value: number, locale: "en" | "fa", unit: string) {
  const rendered = locale === "fa" ? toFaDigits(String(value)) : String(value);
  return `${rendered} ${unit}`;
}

function toFaDigits(value: string) {
  return value.replace(/\d/g, (digit) => "۰۱۲۳۴۵۶۷۸۹"[Number(digit)] ?? digit);
}

function formatRelativeTime(value: string, fa = false) {
  const timestamp = Date.parse(value);
  if (!Number.isFinite(timestamp)) {
    return fa ? "به تازگی" : "Recently";
  }
  const diffMinutes = Math.max(0, Math.round((Date.now() - timestamp) / 60_000));
  if (diffMinutes < 1) {
    return fa ? "همین حالا" : "Just now";
  }
  if (diffMinutes < 60) {
    return fa ? `${diffMinutes} دقیقه پیش` : `${diffMinutes} minutes ago`;
  }
  const diffHours = Math.round(diffMinutes / 60);
  if (diffHours < 24) {
    return fa ? `${diffHours} ساعت پیش` : `${diffHours} hours ago`;
  }
  const diffDays = Math.round(diffHours / 24);
  return fa ? `${diffDays} روز پیش` : `${diffDays} days ago`;
}
