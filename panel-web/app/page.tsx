"use client";

import Link from "next/link";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { dashboardActivities, dashboardCapabilityCards, fallbackStats } from "@/lib/panel-fixtures";

export default function HomePage() {
  const { locale } = usePanel();

  return (
    <PanelShell
      activeKey="dashboard"
      title="Launch your business app with AI"
      titleFa="کسب‌وکار خود را با هوش مصنوعی بسازید"
      subtitle="Generate websites, shops, CRM portals, BPM forms, bots, and customer-facing channels from one workspace."
      subtitleFa="وب‌سایت، فروشگاه، CRM، فرم‌ها، ربات‌ها و کانال‌های ارتباطی را از یک فضای کاری واحد بسازید."
    >
      <div className="page-grid">
        <section className="hero-banner">
          <div className="split-row">
            <span className="status-pill info">{locale === "fa" ? "پیش‌نویس" : "Draft ready"}</span>
            <span className="muted">{locale === "fa" ? "نسخه ۱.۰" : "Version 1.0"}</span>
          </div>
          <h2 style={{ fontSize: "clamp(2rem, 4vw, 3.4rem)", marginBottom: 14 }}>
            {locale === "fa" ? (
              <>
                اپ فروشگاه <span className="gradient-text">آماده ساخت</span>
              </>
            ) : (
              <>
                Shop app ready to <span className="gradient-text">continue building</span>
              </>
            )}
          </h2>
          <p className="muted" style={{ maxWidth: "56ch", lineHeight: 1.9 }}>
            {locale === "fa"
              ? "فروشگاه کامل با کاتالوگ، سبد خرید، پرداخت، ردیابی سفارش، CRM و ربات‌های پشتیبانی در یک مسیر یکپارچه."
              : "A complete business app with catalog, cart, checkout, CRM, order tracking, and customer support automation."}
          </p>
          <div className="pill-row" style={{ margin: "18px 0" }}>
            <span className="pill">{locale === "fa" ? "۱۲ صفحه" : "12 pages"}</span>
            <span className="pill">{locale === "fa" ? "۱۸ ماژول" : "18 modules"}</span>
            <span className="pill">{locale === "fa" ? "۶ یکپارچگی" : "6 integrations"}</span>
          </div>
          <div className="toolbar-row">
            <Link className="primary-pill" href="/projects/new">
              {locale === "fa" ? "ادامه ساخت" : "Continue building"}
            </Link>
            <Link className="secondary-pill" href="/projects">
              {locale === "fa" ? "مشاهده قالب‌ها" : "Browse blueprints"}
            </Link>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "آخرین فعالیت‌ها" : "Recent activity"}</h3>
            <Link href="/roadmap" className="muted">
              {locale === "fa" ? "مشاهده همه" : "View all"}
            </Link>
          </div>
          <div className="activity-list" style={{ marginTop: 16 }}>
            {dashboardActivities.map((item) => (
              <div key={item.en} className="activity-item">
                <strong>{locale === "fa" ? item.fa : item.en}</strong>
                <span className="muted-block">{locale === "fa" ? item.timeFa : item.timeEn}</span>
              </div>
            ))}
          </div>
        </aside>
      </div>

      <section className="stats-grid" style={{ marginTop: 18 }}>
        {fallbackStats.map((stat) => (
          <article key={stat.label} className="stat-card">
            <span className="muted">{locale === "fa" ? translateStat(stat.label) : stat.label}</span>
            <strong>{locale === "fa" ? toFaDigits(stat.value) : stat.value}</strong>
            <div className="stat-delta">{locale === "fa" ? toFaDigits(stat.delta) : stat.delta}</div>
          </article>
        ))}
      </section>

      <section className="feature-grid" style={{ marginTop: 18 }}>
        {dashboardCapabilityCards.map((card) => (
          <article key={card.key} className="capability-card">
            <div className="card-title-row">
              <span className="status-pill info">{card.icon}</span>
              <strong>{locale === "fa" ? card.titleFa : card.titleEn}</strong>
            </div>
            <p className="muted" style={{ lineHeight: 1.9 }}>
              {locale === "fa" ? card.descFa : card.descEn}
            </p>
          </article>
        ))}
      </section>

      <section className="page-grid" style={{ marginTop: 18 }}>
        <article className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "ورود سریع به مسیرها" : "Launch-critical routes"}</h3>
          </div>
          <div className="pill-row" style={{ marginTop: 16 }}>
            <Link className="pill" href="/projects/new">
              {locale === "fa" ? "استودیوی هوش مصنوعی" : "AI Studio"}
            </Link>
            <Link className="pill" href="/maker">
              {locale === "fa" ? "سازنده" : "Maker"}
            </Link>
            <Link className="pill" href="/data">
              {locale === "fa" ? "مدیریت داده" : "Data manager"}
            </Link>
            <Link className="pill" href="/flows">
              {locale === "fa" ? "فلوساز" : "Flow Builder"}
            </Link>
            <Link className="pill" href="/integrations">
              {locale === "fa" ? "اپ‌ها / بات‌ها" : "Apps / Bots"}
            </Link>
          </div>
        </article>

        <article className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "آمادگی انتشار" : "Publish readiness"}</h3>
            <span className="status-pill success">{locale === "fa" ? "فعال" : "On track"}</span>
          </div>
          <p className="muted" style={{ lineHeight: 1.9 }}>
            {locale === "fa"
              ? "ساختارهای داده، بات‌ها، مسیرهای سایت و فلوهای اصلی برای انتشار نسخه اولیه در یک مسیر واحد قرار گرفته‌اند."
              : "Core data, site routes, bots, and workflow surfaces are now aligned under one panel-ready delivery path."}
          </p>
        </article>
      </section>
    </PanelShell>
  );
}

function toFaDigits(value: string) {
  return value.replace(/\d/g, (digit) => "۰۱۲۳۴۵۶۷۸۹"[Number(digit)] ?? digit);
}

function translateStat(label: string) {
  switch (label) {
    case "Visitors":
      return "بازدیدها";
    case "Orders":
      return "سفارش‌ها";
    case "Publish readiness":
      return "آماده برای انتشار";
    case "Low-stock alerts":
      return "هشدار کمبود موجودی";
    default:
      return label;
  }
}
