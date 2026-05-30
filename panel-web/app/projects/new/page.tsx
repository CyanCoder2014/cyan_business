"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { generatePlatformApp, listBlueprints, listClientDrafts } from "@/lib/platform-api";
import type { AppBlueprint, ClientAppDraft, GeneratePlatformAppResponse } from "@/lib/types";

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
        modules: 0
      };
    }

    return {
      title: dsl.app.title ?? (locale === "fa" ? "اپ تولیدشده" : "Generated app"),
      routes: dsl.routes.length,
      services: dsl.delivery.publicApis.length + dsl.delivery.botApis.length,
      modules: dsl.entities.length
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

  return (
    <PanelShell
      activeKey="studio"
      title="Build your business app with AI"
      titleFa="کسب‌وکار خود را با هوش مصنوعی بسازید"
      subtitle="Create websites, PWAs, shops, CRM, BPM forms, automations, and Telegram/Bale bots with one structured prompt."
      subtitleFa="وب‌سایت، PWA، فروشگاه، CRM، فرم‌های BPM، اتوماسیون و ربات‌های تلگرام/بله را با یک درخواست ساختارمند تولید کنید."
    >
      <div className="desktop-only page-grid">
        <section>
          <article className="hero-banner studio-panel">
            <div className="chat-shell">
              <div className="chat-message">
                <strong>{locale === "fa" ? "سلام، من Cyan AI هستم." : "Hi! I'm Cyan AI."}</strong>
                <div className="muted-block">
                  {locale === "fa" ? "چه چیزی برای شما بسازم؟" : "What would you like to build today?"}
                </div>
              </div>
              <div className="chat-message outbound">
                <strong>{locale === "fa" ? "می‌خواهم یک فروشگاه کامل بسازم." : "I want a shop with product catalog, cart, checkout, payments and order tracking."}</strong>
                <div className="muted-block">{locale === "fa" ? "۱۰:۲۴" : "10:24 AM"}</div>
              </div>
              <div className="chat-message">
                <strong>{locale === "fa" ? "عالی، پیش‌نویس را آماده می‌کنم." : "Great! I'll generate a shop app draft for you."}</strong>
                <div className="muted-block">
                  {locale === "fa"
                    ? "وب‌سایت، فروشگاه، CRM، فرم‌ها و ربات‌ها در خروجی قرار می‌گیرند."
                    : "Catalog, secure checkout, CRM, forms, and bot channels are included in the output."}
                </div>
              </div>
            </div>

            <div className="pill-row" style={{ marginTop: 18 }}>
              <span className="pill studio-suggestion">{locale === "fa" ? "ساخت فروشگاه" : "Create a shop"}</span>
              <span className="pill studio-suggestion">{locale === "fa" ? "ساخت CRM" : "Build a CRM"}</span>
              <span className="pill studio-suggestion">{locale === "fa" ? "فرم BPM" : "Make a BPM form"}</span>
              <span className="pill studio-suggestion">{locale === "fa" ? "ربات تلگرام" : "Telegram bot"}</span>
              <span className="pill studio-suggestion">PWA</span>
            </div>

            <div className="chat-composer" style={{ marginTop: 16 }}>
              <textarea value={prompt} onChange={(event) => setPrompt(event.target.value)} placeholder={locale === "fa" ? "اپ یا سناریوی خود را توضیح دهید..." : "Describe your app or ask anything..."} />
              <div className="toolbar-row">
                <div className="pill-row">
                  <button type="button" className="secondary-pill">
                    {locale === "fa" ? "بهبود درخواست" : "Enhance prompt"}
                  </button>
                  <span className="pill">{locale === "fa" ? "هوشمند" : "Smart"}</span>
                </div>
                <button type="button" className="primary-pill" onClick={handleGenerate} disabled={loading}>
                  {loading ? (locale === "fa" ? "در حال تولید..." : "Generating...") : locale === "fa" ? "تولید پیش‌نویس" : "Generate draft"}
                </button>
              </div>
              <div className="muted-block studio-disclaimer">
                {locale === "fa" ? "Cyan AI ممکن است اشتباه کند. خروجی را پیش از انتشار بررسی کنید." : "Cyan AI can make mistakes. Please review the output."}
              </div>
              {status ? <div className="status-pill info">{status}</div> : null}
            </div>
          </article>

          <section className="studio-summary-grid" style={{ marginTop: 18 }}>
            <article className="summary-card">
              <span className="muted">{locale === "fa" ? "پیش‌نویس DSL" : "Draft DSL"}</span>
              <strong>{response?.dsl.app.appKey ?? drafts[0]?.draftId ?? "—"}</strong>
              <span className="muted-block">{locale === "fa" ? "خروجی زنده از orchestrator" : "Live output from orchestrator"}</span>
            </article>
            <article className="summary-card">
              <span className="muted">{locale === "fa" ? "سرویس‌ها" : "Services"}</span>
              <strong>{summary.services}</strong>
              <span className="muted-block">{locale === "fa" ? "بر پایه DSL و delivery APIها" : "Based on DSL and delivery APIs"}</span>
            </article>
            <article className="summary-card">
              <span className="muted">{locale === "fa" ? "آمادگی انتشار" : "Publish readiness"}</span>
              <strong>{locale === "fa" ? `${summary.routes} مسیر` : `${summary.routes} routes`}</strong>
              <span className="muted-block">{locale === "fa" ? "بر پایه DSL فعلی" : "Based on the current DSL"}</span>
            </article>
            <article className="summary-card">
              <span className="muted">{locale === "fa" ? "لینک پیش‌نمایش" : "Preview link"}</span>
              <strong style={{ fontSize: "1rem" }}>{drafts[0]?.siteKey ? `/${drafts[0].siteKey}` : "—"}</strong>
              <span className="muted-block">{locale === "fa" ? "پس از provision قابل استفاده است" : "Available after provisioning"}</span>
            </article>
          </section>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "خلاصه پیش‌نویس تولیدشده" : "Generated draft summary"}</h3>
            <span className="status-pill info">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
          </div>
          <strong style={{ display: "block", marginTop: 18, fontSize: "1.25rem" }}>{summary.title}</strong>
          <p className="muted">
            {locale === "fa"
              ? "اپ کامل با کاتالوگ، سبد خرید، پرداخت، پیگیری سفارش و مسیرهای CRM."
              : "Complete app with catalog, cart, checkout, payments, order tracking, and CRM flows."}
          </p>
          <div className="studio-module-grid" style={{ marginTop: 16 }}>
            <div className="mini-card">
              <span className="muted">{locale === "fa" ? "صفحات / مسیرها" : "Routes"}</span>
              <strong>{summary.routes}</strong>
            </div>
            <div className="mini-card">
              <span className="muted">{locale === "fa" ? "سرویس‌ها" : "Services"}</span>
              <strong>{summary.services}</strong>
            </div>
            <div className="mini-card">
              <span className="muted">{locale === "fa" ? "ماژول‌ها" : "Modules"}</span>
              <strong>{summary.modules}</strong>
            </div>
            <div className="mini-card">
              <span className="muted">{locale === "fa" ? "قالب‌های فعال" : "Blueprints"}</span>
              <strong>{blueprints.length}</strong>
            </div>
          </div>

          <div className="card-title-row" style={{ marginTop: 18 }}>
            <h3>{locale === "fa" ? "آخرین تولیدها" : "Recent generations"}</h3>
            <span className="muted">{locale === "fa" ? "خروجی زنده orchestrator" : "Live orchestrator output"}</span>
          </div>
          <div className="activity-list studio-generation-list" style={{ marginTop: 16 }}>
            {drafts.slice(0, 5).map((draft) => (
              <div key={draft.title} className="activity-item">
                <strong>{draft.title}</strong>
                <span className="muted-block">{draft.updatedAt ?? (locale === "fa" ? "به تازگی" : "Recently")}</span>
              </div>
            ))}
            {!drafts.length ? (
              <div className="activity-item">
                <strong>{locale === "fa" ? "پیش‌نویسی از backend برنگشته است" : "No drafts returned by backend"}</strong>
                <span className="muted-block">{locale === "fa" ? "اولین خروجی پس از Generate اینجا نمایش داده می‌شود." : "The first generated draft will appear here."}</span>
              </div>
            ) : null}
          </div>

          <div className="summary-grid dashboard-summary-grid" style={{ marginTop: 20 }}>
            {[
              ["Routes", `${summary.routes}`],
              ["Services", `${summary.services}`],
              ["Modules", `${summary.modules}`],
              ["Blueprints", `${blueprints.length}`]
            ].map(([title, meta]) => (
              <div key={title} className="mini-card">
                <strong>{title}</strong>
                <span className="muted-block">{meta}</span>
              </div>
            ))}
          </div>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <button type="button" className="icon-pill">←</button>
          <strong style={{ fontSize: "2rem" }}>{locale === "fa" ? "استودیوی هوش مصنوعی" : "AI Studio"}</strong>
          <span className="pill">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
        </div>
        <div className="mobile-chat-thread">
          <div className="mobile-list-item">
            <strong>{locale === "fa" ? "Cyan AI" : "Cyan AI"}</strong>
            <span className="muted-block">{locale === "fa" ? "چه چیزی برای شما بسازم؟" : "What would you like to build today?"}</span>
          </div>
          <div className="mobile-list-item" style={{ justifySelf: "end", background: "linear-gradient(135deg, rgba(37,141,247,0.12), rgba(126,73,255,0.1))" }}>
            <strong>{locale === "fa" ? "می‌خواهم یک اپ فروشگاهی بسازم" : "I want to build a shop app"}</strong>
            <span className="muted-block">{prompt}</span>
          </div>
          <div className="mobile-list-item">
            <strong>{locale === "fa" ? "در حال تولید اپ شما..." : "Generating your app..."}</strong>
            <span className="muted-block">{locale === "fa" ? "وب‌سایت، فروشگاه، CRM و فرم‌ها در حال آماده‌سازی هستند." : "Website, shop, CRM, and forms are being prepared."}</span>
          </div>
          <div className="mobile-tab-strip">
            <span className="pill">{locale === "fa" ? "فروشگاه" : "Create a shop"}</span>
            <span className="pill">CRM</span>
            <span className="pill">Telegram</span>
          </div>
          <div className="mobile-card">
            <strong>{locale === "fa" ? "در حال ساخت" : "Generating your app..."}</strong>
            <div className="mobile-list" style={{ marginTop: 14 }}>
              {[
                [locale === "fa" ? "وب‌سایت" : "Website", locale === "fa" ? "آماده" : "Ready"],
                [locale === "fa" ? "فروشگاه" : "Shop", locale === "fa" ? "آماده" : "Ready"],
                ["CRM", locale === "fa" ? "در حال انجام" : "In progress"],
                [locale === "fa" ? "فرم‌ها" : "Forms", locale === "fa" ? "در انتظار" : "Pending"]
              ].map(([a, b]) => (
                <div key={String(a)} className="mobile-list-item">
                  <strong>{a}</strong>
                  <span className="muted-block">{b}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="chat-composer">
            <textarea value={prompt} onChange={(event) => setPrompt(event.target.value)} />
            <button type="button" className="primary-pill" onClick={handleGenerate} disabled={loading}>
              {loading ? (locale === "fa" ? "در حال تولید..." : "Generating...") : locale === "fa" ? "ارسال" : "Send"}
            </button>
          </div>
        </div>
      </div>
    </PanelShell>
  );
}
