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
    Promise.all([listBlueprints().catch(() => []), listClientDrafts().catch(() => [])]).then(([blueprintItems, draftItems]) => {
      setBlueprints(blueprintItems);
      setDrafts(draftItems);
    });
  }, []);

  const summary = useMemo(() => {
    if (!response?.dsl) {
      return {
        title: locale === "fa" ? "اپ فروشگاه" : "Shop App",
        routes: 12,
        services: 18,
        modules: 9
      };
    }

    return {
      title: response.dsl.app.title ?? (locale === "fa" ? "اپ تولیدشده" : "Generated app"),
      routes: response.dsl.routes.length,
      services: response.dsl.delivery.publicApis.length + response.dsl.delivery.botApis.length,
      modules: response.dsl.entities.length
    };
  }, [locale, response]);

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
      <div className="page-grid">
        <section className="hero-banner">
          <div className="chat-shell">
            <div className="chat-message">
              <strong>{locale === "fa" ? "سلام، من سیان هستم." : "Hi, I'm Cyan AI."}</strong>
              <div className="muted-block">
                {locale === "fa" ? "چه چیزی برای شما بسازم؟" : "What would you like to build today?"}
              </div>
            </div>
            <div className="chat-message outbound">
              <strong>{locale === "fa" ? "فروشگاه کامل با پرداخت و پیگیری سفارش" : "A complete shop with payments and order tracking"}</strong>
              <div className="muted-block">{prompt}</div>
            </div>
            <div className="chat-message">
              <strong>{locale === "fa" ? "پیش‌نویس شما آماده است." : "Your draft is ready."}</strong>
              <div className="muted-block">
                {locale === "fa"
                  ? "وب‌سایت، فروشگاه، CRM، فرم‌ها و کانال‌های پیام‌رسان در این خروجی ساختارمند شده‌اند."
                  : "Website, commerce, CRM, forms, and bot channels are captured in the generated output."}
              </div>
            </div>
          </div>

          <div className="pill-row" style={{ marginTop: 18 }}>
            <span className="pill">{locale === "fa" ? "ساخت فروشگاه" : "Create a shop"}</span>
            <span className="pill">{locale === "fa" ? "ساخت CRM" : "Build a CRM"}</span>
            <span className="pill">{locale === "fa" ? "فرم BPM" : "Make a BPM form"}</span>
            <span className="pill">{locale === "fa" ? "ربات تلگرام" : "Telegram bot"}</span>
            <span className="pill">PWA</span>
          </div>

          <div className="chat-composer" style={{ marginTop: 16 }}>
            <textarea value={prompt} onChange={(event) => setPrompt(event.target.value)} />
            <div className="toolbar-row">
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "بهبود درخواست" : "Enhance prompt"}
              </button>
              <button type="button" className="primary-pill" onClick={handleGenerate} disabled={loading}>
                {loading ? (locale === "fa" ? "در حال تولید..." : "Generating...") : locale === "fa" ? "تولید پیش‌نویس" : "Generate draft"}
              </button>
            </div>
            {status ? <div className="status-pill info">{status}</div> : null}
          </div>

          <section className="summary-grid" style={{ marginTop: 18 }}>
            <article className="summary-card">
              <span className="muted">{locale === "fa" ? "پیش‌نویس DSL" : "Draft DSL"}</span>
              <strong>{response?.dsl.app.appKey ?? "shop_app_v0.1.dsl"}</strong>
            </article>
            <article className="summary-card">
              <span className="muted">{locale === "fa" ? "وضعیت انتشار" : "Publish readiness"}</span>
              <strong>{locale === "fa" ? "۹۲٪" : "92%"}</strong>
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
          <div className="summary-grid" style={{ marginTop: 16 }}>
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
              <strong>{blueprints.length || 6}</strong>
            </div>
          </div>

          <div className="activity-list" style={{ marginTop: 16 }}>
            {(drafts.slice(0, 5).length
              ? drafts.slice(0, 5).map((draft) => ({
                  title: draft.title,
                  time: draft.updatedAt ?? (locale === "fa" ? "به تازگی" : "Recently")
                }))
              : fallbackDrafts(locale)
            ).map((draft) => (
              <div key={draft.title} className="activity-item">
                <strong>{draft.title}</strong>
                <span className="muted-block">{draft.time}</span>
              </div>
            ))}
          </div>
        </aside>
      </div>
    </PanelShell>
  );
}

function fallbackDrafts(locale: "en" | "fa") {
  return [
    { title: locale === "fa" ? "اپ فروشگاه" : "Shop App", time: locale === "fa" ? "همین حالا" : "Just now" },
    { title: locale === "fa" ? "CRM فروش" : "Sales CRM", time: locale === "fa" ? "۲ ساعت پیش" : "2h ago" },
    { title: locale === "fa" ? "فرم منابع انسانی" : "HR onboarding", time: locale === "fa" ? "دیروز" : "Yesterday" }
  ];
}
