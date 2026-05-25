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
      <div className="dashboard-grid">
        <section className="dashboard-main">
          <article className="hero-banner dashboard-hero">
            <div className="split-row">
              <span className="status-pill info">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
            </div>
            <div className="dashboard-hero-body">
              <div>
                <h2 style={{ fontSize: "clamp(2rem, 4vw, 3.1rem)", marginBottom: 14 }}>
                  {locale === "fa" ? "اپ فروشگاهی (نسخه ۰.۱)" : "Shop App (v0.1)"}
                </h2>
                <p className="muted" style={{ maxWidth: "44ch", lineHeight: 1.7 }}>
                  {locale === "fa"
                    ? "اپ کامل با کاتالوگ، سبد خرید، پرداخت، پیگیری سفارش و یکپارچگی‌های عملیاتی."
                    : "A complete shop app with catalog, cart, checkout, payments, and order tracking."}
                </p>
                <div className="pill-row" style={{ margin: "18px 0" }}>
                  <span className="pill">{locale === "fa" ? "۱۲ صفحه" : "12 pages"}</span>
                  <span className="pill">{locale === "fa" ? "۱۸ ماژول" : "18 modules"}</span>
                  <span className="pill">{locale === "fa" ? "۶ یکپارچگی" : "6 integrations"}</span>
                </div>
                <div className="toolbar-row">
                  <Link className="primary-pill wide-pill" href="/projects/new">
                    {locale === "fa" ? "ادامه ساخت" : "Continue building"}
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
            {fallbackStats.map((stat) => (
              <article key={stat.label} className="stat-card">
                <span className="muted">{locale === "fa" ? translateStat(stat.label) : stat.label}</span>
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
              <span className="status-pill info">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
            </div>
            <strong style={{ display: "block", marginTop: 18, fontSize: "1.25rem" }}>
              {locale === "fa" ? "اپ فروشگاهی (نسخه ۰.۱)" : "Shop App (v0.1)"}
            </strong>
            <p className="muted">
              {locale === "fa"
                ? "وب‌سایت، فروشگاه، CRM، فرم‌ها، فلو و ربات تلگرام در این پیش‌نویس قرار گرفته‌اند."
                : "Website, shop, CRM, forms, flows, and Telegram bot are included in this draft."}
            </p>
            <div className="summary-grid dashboard-summary-grid" style={{ marginTop: 16 }}>
              {[
                ["Website", "12 pages"],
                ["Shop", "18 modules"],
                ["CRM", "9 modules"],
                ["Forms", "6 forms"],
                ["Flow", "14 workflows"],
                ["Bot", "Telegram bot"]
              ].map(([title, meta]) => (
                <div key={title} className="mini-card summary-mini">
                  <strong>{title}</strong>
                  <span className="muted-block">{locale === "fa" ? meta.replace(/\d/g, (d) => "۰۱۲۳۴۵۶۷۸۹"[Number(d)] ?? d) : meta}</span>
                </div>
              ))}
            </div>
            <div className="toolbar-row" style={{ marginTop: 16 }}>
              <Link href="/maker" className="secondary-pill">
                {locale === "fa" ? "باز کردن در سازنده" : "Open in Maker"}
              </Link>
              <Link href="/projects/new" className="primary-pill">
                {locale === "fa" ? "ادامه ساخت" : "Continue building"}
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
            <div className="activity-list" style={{ marginTop: 16 }}>
              {dashboardActivities.map((item) => (
                <div key={item.en} className="activity-item">
                  <strong>{locale === "fa" ? item.fa : item.en}</strong>
                  <span className="muted-block">{locale === "fa" ? item.timeFa : item.timeEn}</span>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>
    </PanelShell>
  );
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
