"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { SparkleIcon } from "@/components/auth-icons";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { generatePlatformApp, listBlueprints, listClientDrafts } from "@/lib/platform-api";
import type { AppBlueprint, ClientAppDraft, GeneratePlatformAppResponse } from "@/lib/types";

const QUICK_PROMPTS = [
  { key: "shop", en: "Create a shop", fa: "ساخت فروشگاه" },
  { key: "crm", en: "Build a CRM", fa: "ساخت CRM" },
  { key: "bpm", en: "Make a BPM form", fa: "فرم BPM" },
  { key: "bot", en: "Telegram bot", fa: "ربات تلگرام" },
  { key: "landing", en: "Landing page", fa: "صفحه فرود" },
  { key: "pwa", en: "PWA app", fa: "اپ PWA" }
] as const;

const MODULE_TILES = [
  { key: "website", en: "Website", fa: "وب‌سایت", countKey: "routes" as const, suffixEn: "pages", suffixFa: "صفحه" },
  { key: "shop", en: "Shop", fa: "فروشگاه", countKey: "modules" as const, suffixEn: "modules", suffixFa: "ماژول" },
  { key: "crm", en: "CRM", fa: "CRM", countKey: "modules" as const, suffixEn: "module", suffixFa: "ماژول" },
  { key: "forms", en: "Forms", fa: "فرم‌ها", countKey: "modules" as const, suffixEn: "forms", suffixFa: "فرم" },
  { key: "flow", en: "Flow", fa: "فلو", countKey: "services" as const, suffixEn: "flows", suffixFa: "فلو" },
  { key: "bot", en: "Bot", fa: "بات", countKey: "services" as const, suffixEn: "bot", suffixFa: "بات" }
] as const;

export default function AiStudioPage() {
  const { locale } = usePanel();
  const [prompt, setPrompt] = useState("Build a shop with product catalog, cart, checkout, payments, and order tracking.");
  const [status, setStatus] = useState<string | null>(null);
  const [response, setResponse] = useState<GeneratePlatformAppResponse | null>(null);
  const [blueprints, setBlueprints] = useState<AppBlueprint[]>([]);
  const [drafts, setDrafts] = useState<ClientAppDraft[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    Promise.allSettled([listBlueprints(), listClientDrafts()]).then(([blueprintItems, draftItems]) => {
      const errors: string[] = [];
      if (blueprintItems.status === "fulfilled") {
        setBlueprints(blueprintItems.value);
      } else {
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

  const summary = useMemo(() => {
    const dsl = response?.dsl ?? drafts[0]?.resolvedDsl;
    if (!dsl) {
      return {
        title: locale === "fa" ? "پیش‌نویسی وجود ندارد" : "No draft available",
        routes: 0,
        services: 0,
        modules: 0,
        readiness: 0,
        previewPath: "—"
      };
    }

    const routes = dsl.routes.length;
    const services = dsl.delivery.publicApis.length + dsl.delivery.botApis.length;
    const modules = dsl.entities.length;
    const readiness = Math.min(100, Math.round(((routes + services + modules) / 24) * 100));

    return {
      title: dsl.app.title ?? (locale === "fa" ? "اپ تولیدشده" : "Generated app"),
      routes,
      services,
      modules,
      readiness,
      previewPath: drafts[0]?.siteKey ? `/${drafts[0].siteKey}` : "—"
    };
  }, [drafts, locale, response]);

  async function handleGenerate() {
    setLoading(true);
    setStatus(null);
    try {
      const generated = await generatePlatformApp({
        prompt,
        tenantKey: "tenant-demo",
        siteKey: "site-commerce",
        execute: false,
        answers: {
          appType: "SHOP",
          channels: ["website", "pwa", "telegram"],
          locale
        }
      });
      setResponse(generated);
      setStatus(locale === "fa" ? "پیش‌نویس جدید تولید شد." : "New draft generated.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "تولید پیش‌نویس ناموفق بود." : "Draft generation failed.");
    } finally {
      setLoading(false);
    }
  }

  function applyPrompt(nextPrompt: string) {
    setPrompt(nextPrompt);
  }

  return (
    <PanelShell
      activeKey="studio"
      kicker="AI Studio"
      kickerFa="استودیوی هوش مصنوعی"
      title="Build your business app with AI"
      titleFa="کسب‌وکار خود را با هوش مصنوعی بسازید"
      subtitle="Create websites, PWAs, shops, CRM, BPM forms, automations, and Telegram/Bale bots with one structured prompt."
      subtitleFa="وب‌سایت، PWA، فروشگاه، CRM، فرم‌های BPM، اتوماسیون و ربات‌های تلگرام/بله را با یک درخواست ساختارمند تولید کنید."
    >
      <div className="desktop-only page-grid studio-page-grid">
        <section>
          <article className="hero-banner studio-panel">
            <div className="studio-chat-shell">
              <div className="studio-chat-message">
                <div className="studio-chat-avatar ai">AI</div>
                <div>
                  <strong>{locale === "fa" ? "سلام، من Cyan AI هستم." : "Hi! I'm Cyan AI."}</strong>
                  <p className="muted-block">{locale === "fa" ? "امروز چه چیزی می‌خواهید بسازید؟" : "What would you like to build today?"}</p>
                </div>
              </div>
              <div className="studio-chat-message outbound">
                <div>
                  <strong>
                    {locale === "fa"
                      ? "می‌خواهم یک فروشگاه با کاتالوگ، سبد خرید، پرداخت و پیگیری سفارش بسازم."
                      : "I want a shop with product catalog, cart, checkout, payments and order tracking."}
                  </strong>
                  <div className="studio-chat-meta">
                    <span>{locale === "fa" ? "۱۰:۲۴" : "10:24 AM"}</span>
                    <span className="studio-read-receipt" aria-hidden="true">
                      ✓✓
                    </span>
                  </div>
                </div>
              </div>
              <div className="studio-chat-message">
                <div className="studio-chat-avatar ai">AI</div>
                <div>
                  <strong>{locale === "fa" ? "عالی، پیش‌نویس فروشگاه را آماده می‌کنم." : "Great! I'll generate a shop app draft for you."}</strong>
                  <p className="muted-block">
                    {locale === "fa"
                      ? "وب‌سایت، فروشگاه، CRM، فرم‌ها و ربات‌ها در خروجی قرار می‌گیرند."
                      : "Catalog, secure checkout, CRM, forms, and bot channels are included in the output."}
                  </p>
                </div>
              </div>
            </div>

            <div className="studio-chip-row">
              {QUICK_PROMPTS.map((item) => (
                <button key={item.key} type="button" className="studio-chip" onClick={() => applyPrompt(item.en)}>
                  <SparkleIcon size={14} />
                  <span>{locale === "fa" ? item.fa : item.en}</span>
                </button>
              ))}
            </div>

            <div className="studio-composer">
              <textarea
                value={prompt}
                onChange={(event) => setPrompt(event.target.value)}
                placeholder={locale === "fa" ? "اپ یا سناریوی خود را توضیح دهید..." : "Describe your app or ask anything..."}
              />
              <div className="studio-composer-toolbar">
                <div className="studio-composer-actions">
                  <button type="button" className="studio-icon-btn" aria-label={locale === "fa" ? "پیوست" : "Attach file"}>
                    📎
                  </button>
                  <button type="button" className="secondary-pill studio-enhance-btn">
                    <SparkleIcon size={14} />
                    <span>{locale === "fa" ? "بهبود درخواست" : "Enhance prompt"}</span>
                  </button>
                  <button type="button" className="studio-smart-select">
                    {locale === "fa" ? "هوشمند" : "Smart"} ▾
                  </button>
                </div>
                <button type="button" className="studio-send-btn" onClick={handleGenerate} disabled={loading} aria-label={locale === "fa" ? "ارسال" : "Send"}>
                  →
                </button>
              </div>
              <div className="muted-block studio-disclaimer">
                {locale === "fa" ? "Cyan AI ممکن است اشتباه کند. خروجی را پیش از انتشار بررسی کنید." : "Cyan AI can make mistakes. Please review the output."}
              </div>
              {status ? <div className="status-pill info">{status}</div> : null}
            </div>
          </article>

          <section className="studio-summary-grid">
            <article className="studio-status-card">
              <div className="studio-status-head">
                <span className="muted">{locale === "fa" ? "پیش‌نویس DSL" : "Draft DSL"}</span>
                <span className="studio-status-dot success">{locale === "fa" ? "تولید شد" : "Generated"}</span>
              </div>
              <strong>{response?.dsl.app.appKey ?? drafts[0]?.draftId ?? "—"}</strong>
              <button type="button" className="text-link">{locale === "fa" ? "مشاهده فایل" : "View file"}</button>
            </article>
            <article className="studio-status-card">
              <div className="studio-status-head">
                <span className="muted">{locale === "fa" ? "سرویس‌ها" : "Services"}</span>
                <span className="studio-status-dot success">{locale === "fa" ? "آماده" : "Ready"}</span>
              </div>
              <strong>{summary.services}</strong>
              <span className="muted-block">{locale === "fa" ? `${summary.services} سرویس پیکربندی شده` : `${summary.services} services configured`}</span>
              <button type="button" className="text-link">{locale === "fa" ? "مدیریت سرویس‌ها" : "Manage services"}</button>
            </article>
            <article className="studio-status-card">
              <div className="studio-status-head">
                <span className="muted">{locale === "fa" ? "آمادگی انتشار" : "Publish readiness"}</span>
              </div>
              <strong>{summary.readiness}%</strong>
              <span className="muted-block">{locale === "fa" ? `${summary.readiness}% آماده انتشار` : `${summary.readiness}% ready to publish`}</span>
              <div className="studio-progress">
                <span style={{ width: `${summary.readiness}%` }} />
              </div>
            </article>
            <article className="studio-status-card">
              <div className="studio-status-head">
                <span className="muted">{locale === "fa" ? "لینک پیش‌نمایش" : "Preview link"}</span>
              </div>
              <strong className="studio-preview-link">{summary.previewPath}</strong>
              <div className="studio-preview-actions">
                <button type="button" className="text-link">{locale === "fa" ? "باز کردن پیش‌نمایش" : "Open preview"}</button>
                <button type="button" className="studio-icon-btn" aria-label={locale === "fa" ? "کپی" : "Copy link"}>
                  ⧉
                </button>
              </div>
            </article>
          </section>
        </section>

        <aside className="panel-card studio-sidebar">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "خلاصه پیش‌نویس تولیدشده" : "Generated draft summary"}</h3>
            <span className="status-pill info">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
          </div>
          <strong className="studio-sidebar-title">{summary.title}</strong>
          <p className="muted studio-sidebar-copy">
            {locale === "fa"
              ? "اپ کامل با کاتالوگ، سبد خرید، پرداخت، پیگیری سفارش و مسیرهای CRM."
              : "Complete app with catalog, cart, checkout, payments, order tracking, and CRM flows."}
          </p>

          <div className="studio-module-tiles">
            {MODULE_TILES.map((tile, index) => {
              const count = summary[tile.countKey];
              const displayCount = index === 0 ? summary.routes || count : Math.max(1, Math.ceil(count / (index + 1)));
              return (
                <div key={tile.key} className="studio-module-tile">
                  <div className="studio-module-head">
                    <span className="studio-module-icon">{tile.key.slice(0, 1).toUpperCase()}</span>
                    <span className="studio-module-check" aria-hidden="true">
                      ✓
                    </span>
                  </div>
                  <strong>{locale === "fa" ? tile.fa : tile.en}</strong>
                  <span className="muted-block">
                    {displayCount} {locale === "fa" ? tile.suffixFa : tile.suffixEn}
                  </span>
                </div>
              );
            })}
          </div>

          <div className="studio-sidebar-actions">
            <Link href="/maker" className="secondary-pill wide-pill">
              {locale === "fa" ? "باز کردن در Maker" : "Open in Maker"}
            </Link>
            <Link href={drafts[0] ? `/projects/${drafts[0].draftId}` : "/maker"} className="primary-pill wide-pill">
              {locale === "fa" ? "ادامه ساخت ←" : "Continue building →"}
            </Link>
          </div>

          <div className="card-title-row studio-recent-head">
            <h3>{locale === "fa" ? "آخرین تولیدها" : "Recent generations"}</h3>
            <button type="button" className="text-link">
              {locale === "fa" ? "مشاهده همه" : "View all"}
            </button>
          </div>
          <div className="studio-recent-list">
            {drafts.slice(0, 5).map((draft, index) => (
              <div key={`${draft.title}-${index}`} className="studio-recent-item">
                <div>
                  <strong>{draft.title}</strong>
                  <span className="muted-block">{draft.updatedAt ?? (locale === "fa" ? "همین الان" : "Just now")}</span>
                </div>
                <span className={`studio-status-dot ${index === 0 ? "success" : "warning"}`} aria-hidden="true" />
              </div>
            ))}
            {!drafts.length ? (
              <div className="studio-recent-item">
                <div>
                  <strong>{locale === "fa" ? "پیش‌نویسی از backend برنگشته است" : "No drafts returned by backend"}</strong>
                  <span className="muted-block">{locale === "fa" ? "اولین خروجی پس از Generate اینجا نمایش داده می‌شود." : "The first generated draft will appear here."}</span>
                </div>
              </div>
            ) : null}
          </div>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <button type="button" className="icon-pill">
            ←
          </button>
          <strong style={{ fontSize: "2rem" }}>{locale === "fa" ? "استودیوی هوش مصنوعی" : "AI Studio"}</strong>
          <span className="pill">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
        </div>
        <div className="mobile-chat-thread">
          <div className="mobile-list-item">
            <strong>{locale === "fa" ? "Cyan AI" : "Cyan AI"}</strong>
            <span className="muted-block">{locale === "fa" ? "امروز چه چیزی می‌خواهید بسازید؟" : "What would you like to build today?"}</span>
          </div>
          <div className="mobile-list-item outbound-mobile-chat">
            <strong>{locale === "fa" ? "می‌خواهم یک اپ فروشگاهی بسازم" : "I want to build a shop app"}</strong>
            <span className="muted-block">{prompt}</span>
          </div>
          <div className="mobile-list-item">
            <strong>{loading ? (locale === "fa" ? "در حال تولید..." : "Generating...") : locale === "fa" ? "پیش‌نویس آماده است" : "Draft is ready"}</strong>
            <span className="muted-block">{locale === "fa" ? "وب‌سایت، فروشگاه، CRM و فرم‌ها در خروجی قرار می‌گیرند." : "Website, shop, CRM, and forms are included in the output."}</span>
          </div>
          <div className="mobile-tab-strip">
            {QUICK_PROMPTS.slice(0, 3).map((item) => (
              <span key={item.key} className="pill">
                {locale === "fa" ? item.fa : item.en}
              </span>
            ))}
          </div>
          <div className="chat-composer">
            <textarea value={prompt} onChange={(event) => setPrompt(event.target.value)} placeholder={locale === "fa" ? "اپ یا سناریوی خود را توضیح دهید..." : "Describe your app or ask anything..."} />
            <button type="button" className="primary-pill" onClick={handleGenerate} disabled={loading}>
              {loading ? (locale === "fa" ? "در حال تولید..." : "Generating...") : locale === "fa" ? "ارسال" : "Send"}
            </button>
          </div>
        </div>
      </div>
    </PanelShell>
  );
}
