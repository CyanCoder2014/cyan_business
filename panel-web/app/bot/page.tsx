"use client";

import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";

export default function BotExperiencePage() {
  const { locale } = usePanel();

  return (
    <PanelShell
      activeKey="integrations"
      title="Bot Experience"
      titleFa="تجربه بات"
      subtitle="Preview how customers experience Telegram and Bale journeys powered by Cyan."
      subtitleFa="تجربه مشتری در تلگرام و بله را که با Cyan مدیریت می‌شود، پیش‌نمایش کنید."
    >
      <div className="page-grid">
        <section className="panel-card">
          <div className="two-column-grid">
            <article className="preview-frame">
              <div className="card-title-row">
                <h3>Telegram</h3>
                <span className="status-pill success">{locale === "fa" ? "زنده" : "Live"}</span>
              </div>
              <div className="activity-list" style={{ marginTop: 16 }}>
                <div className="chat-message">
                  <strong>{locale === "fa" ? "سلام، من بات فروشگاه هستم." : "Hi, I'm Acme Store Bot."}</strong>
                  <div className="muted-block">{locale === "fa" ? "چطور کمکتان کنم؟" : "How can I help you today?"}</div>
                </div>
                <div className="pill-row">
                  <span className="pill">{locale === "fa" ? "پیگیری سفارش" : "Track order"}</span>
                  <span className="pill">{locale === "fa" ? "مرور محصولات" : "Browse products"}</span>
                </div>
                <div className="chat-message outbound">
                  <strong>{locale === "fa" ? "پیگیری سفارش #ACM12345" : "Track my order #ACM12345"}</strong>
                </div>
                <div className="chat-message">
                  <strong>{locale === "fa" ? "سفارش در مسیر است" : "Your order is in transit"}</strong>
                  <div className="muted-block">{locale === "fa" ? "تحویل فردا" : "Arriving tomorrow"}</div>
                </div>
              </div>
            </article>

            <article className="preview-frame" style={{ background: "linear-gradient(180deg, rgba(255,246,252,0.96), rgba(255,250,253,0.96))" }}>
              <div className="card-title-row">
                <h3>Bale</h3>
                <span className="status-pill success">{locale === "fa" ? "زنده" : "Live"}</span>
              </div>
              <div className="activity-list" style={{ marginTop: 16 }}>
                <div className="chat-message">
                  <strong>{locale === "fa" ? "چطور می‌توانیم کمک کنیم؟" : "How can we assist you today?"}</strong>
                </div>
                <div className="pill-row">
                  <span className="pill">{locale === "fa" ? "درخواست مرجوعی" : "Request return"}</span>
                  <span className="pill">{locale === "fa" ? "تعویض کالا" : "Replace item"}</span>
                </div>
                <div className="chat-message outbound">
                  <strong>{locale === "fa" ? "کالای اشتباه دریافت کردم." : "I received the wrong item."}</strong>
                </div>
              </div>
            </article>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "قابلیت‌های بات" : "Bot capabilities"}</h3>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "پاسخ هوشمند" : "AI replies"}</strong>
              <span className="muted-block">{locale === "fa" ? "پاسخ فوری بر پایه داده‌ها و دستورها" : "Instant replies using your data and instructions."}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "هندآف فلو" : "Workflow handoff"}</strong>
              <span className="muted-block">{locale === "fa" ? "انتقال درخواست پیچیده به تیم یا عامل درست" : "Escalates complex requests to the right team or agent."}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "فرم‌های هوشمند" : "Smart forms"}</strong>
              <span className="muted-block">{locale === "fa" ? "جمع‌آوری ساختاریافته اطلاعات" : "Collects structured information with dynamic forms."}</span>
            </div>
          </div>
        </aside>
      </div>
    </PanelShell>
  );
}
