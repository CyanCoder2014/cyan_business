"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { listBotIntegrations, listBotMessages, listMiniAppBuilds } from "@/lib/platform-api";
import type { BotChannelIntegration, BotMiniAppBuild, BotOutboundMessage } from "@/lib/types";

export default function BotExperiencePage() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const [integrations, setIntegrations] = useState<BotChannelIntegration[]>([]);
  const [messages, setMessages] = useState<BotOutboundMessage[]>([]);
  const [builds, setBuilds] = useState<BotMiniAppBuild[]>([]);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    if (!tenantKey) return;
    const scope = { tenantKey, siteKey: siteKey ?? undefined };
    Promise.allSettled([
      listBotIntegrations(scope),
      listBotMessages(scope),
      listMiniAppBuilds(scope)
    ]).then(([integrationsResult, messagesResult, buildsResult]) => {
      const errors: string[] = [];
      if (integrationsResult.status === "fulfilled") {
        setIntegrations(integrationsResult.value);
      } else {
        errors.push(locale === "fa" ? "یکپارچگی‌های کانال بارگیری نشدند." : "Channel integrations could not be loaded.");
      }
      if (messagesResult.status === "fulfilled") {
        setMessages(messagesResult.value);
      } else {
        errors.push(locale === "fa" ? "پیام‌های خروجی بارگیری نشدند." : "Outbound messages could not be loaded.");
      }
      if (buildsResult.status === "fulfilled") {
        setBuilds(buildsResult.value);
      } else {
        errors.push(locale === "fa" ? "بیلدهای مینی‌اپ بارگیری نشدند." : "Mini app builds could not be loaded.");
      }
      setStatus(errors.length ? errors.join(" ") : null);
    });
  }, [locale, queryVersion, siteKey, tenantKey]);

  const telegramIntegration = integrations.find((item) => item.channel === "TELEGRAM") ?? null;
  const baleIntegration = integrations.find((item) => item.channel === "BALE") ?? null;
  const telegramMessages = messages.filter((item) => item.channel === "TELEGRAM").slice(0, 3);
  const baleMessages = messages.filter((item) => item.channel === "BALE").slice(0, 3);
  const publishedBuilds = builds.filter((item) => item.status?.toUpperCase() === "PUBLISHED");
  const deliveryStats = useMemo(() => {
    const total = messages.length;
    const delivered = messages.filter((item) => item.status.toUpperCase().includes("DELIVER")).length;
    const failed = messages.filter((item) => item.status.toUpperCase().includes("FAIL")).length;
    return {
      delivered: total ? `${((delivered / total) * 100).toFixed(1)}%` : "0.0%",
      failed: total ? `${((failed / total) * 100).toFixed(1)}%` : "0.0%"
    };
  }, [messages]);

  return (
    <PanelShell
      activeKey="bot"
      title="Bot Experience"
      titleFa="تجربه بات"
      subtitle="Preview how customers experience Telegram and Bale journeys powered by Cyan."
      subtitleFa="تجربه مشتری در تلگرام و بله را که با Cyan مدیریت می‌شود، پیش‌نمایش کنید."
    >
      <div className="desktop-only page-grid">
        <section className="panel-card">
          {status ? <div className="status-pill info" style={{ marginBottom: 12 }}>{status}</div> : null}
          <div className="pill-row" style={{ marginBottom: 16 }}>
            <span className={telegramIntegration?.active ? "pill status-pill info" : "pill"}>Telegram</span>
            <span className={baleIntegration?.active ? "pill status-pill info" : "pill"}>Bale</span>
          </div>
          <div className="two-column-grid">
            <article className="preview-frame">
              <div className="card-title-row">
                <h3>Telegram</h3>
                <span className={telegramIntegration?.active ? "status-pill success" : "status-pill warning"}>
                  {telegramIntegration?.active ? (locale === "fa" ? "فعال" : "Active") : locale === "fa" ? "غیرفعال" : "Inactive"}
                </span>
              </div>
              <div className="activity-list" style={{ marginTop: 16 }}>
                {telegramIntegration ? (
                  <>
                    <div className="chat-message">
                      <strong>{telegramIntegration.botUsername ?? telegramIntegration.integrationKey}</strong>
                      <div className="muted-block">{telegramIntegration.tokenSecretRef ?? (locale === "fa" ? "توکن امن ثبت نشده" : "No token secret registered")}</div>
                    </div>
                    <div className="pill-row">
                      <span className="pill">{telegramIntegration.clientKey ?? "panel"}</span>
                      <span className="pill">{telegramIntegration.siteKey}</span>
                    </div>
                    {telegramMessages.length ? telegramMessages.map((message) => (
                      <div key={`${message.integrationKey}-${message.id ?? message.externalChatId}`} className="chat-message outbound">
                        <strong>{message.text}</strong>
                        <div className="muted-block">{message.status} • {message.externalChatId}</div>
                      </div>
                    )) : (
                      <div className="mini-card">
                        <strong>{locale === "fa" ? "ارسالی ثبت نشده است" : "No deliveries yet"}</strong>
                        <span className="muted-block">{locale === "fa" ? "پس از ارسال اولین پیام، وضعیت اینجا نمایش داده می‌شود." : "The first outbound delivery will appear here."}</span>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="mini-card">
                    <strong>{locale === "fa" ? "تلگرام متصل نیست" : "Telegram not connected"}</strong>
                    <span className="muted-block">{locale === "fa" ? "ابتدا یک integration در صفحه Apps/Bots ثبت کنید." : "Register an integration in Apps/Bots first."}</span>
                  </div>
                )}
              </div>
            </article>

            <article className="preview-frame" style={{ background: "linear-gradient(180deg, rgba(255,246,252,0.96), rgba(255,250,253,0.96))" }}>
              <div className="card-title-row">
                <h3>Bale</h3>
                <span className={baleIntegration?.active ? "status-pill success" : "status-pill warning"}>
                  {baleIntegration?.active ? (locale === "fa" ? "فعال" : "Active") : locale === "fa" ? "غیرفعال" : "Inactive"}
                </span>
              </div>
              <div className="activity-list" style={{ marginTop: 16 }}>
                {baleIntegration ? (
                  <>
                    <div className="chat-message">
                      <strong>{baleIntegration.botUsername ?? baleIntegration.integrationKey}</strong>
                      <div className="muted-block">{baleIntegration.tokenSecretRef ?? (locale === "fa" ? "توکن امن ثبت نشده" : "No token secret registered")}</div>
                    </div>
                    <div className="pill-row">
                      <span className="pill">{baleIntegration.clientKey ?? "panel"}</span>
                      <span className="pill">{baleIntegration.siteKey}</span>
                    </div>
                    {baleMessages.length ? baleMessages.map((message) => (
                      <div key={`${message.integrationKey}-${message.id ?? message.externalChatId}`} className="chat-message outbound">
                        <strong>{message.text}</strong>
                        <div className="muted-block">{message.status} • {message.externalChatId}</div>
                      </div>
                    )) : (
                      <div className="mini-card">
                        <strong>{locale === "fa" ? "ارسالی ثبت نشده است" : "No deliveries yet"}</strong>
                        <span className="muted-block">{locale === "fa" ? "این کانال هنوز پیام خروجی ندارد." : "This channel has no outbound messages yet."}</span>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="mini-card">
                    <strong>{locale === "fa" ? "بله متصل نیست" : "Bale not connected"}</strong>
                    <span className="muted-block">{locale === "fa" ? "پس از ثبت integration، وضعیت این کانال اینجا نمایش داده می‌شود." : "After registration, channel state will appear here."}</span>
                  </div>
                )}
              </div>
            </article>
          </div>
        </section>

        <aside className="panel-card">
          <div className="toolbar-row">
            <Link href="/integrations" className="secondary-pill">{locale === "fa" ? "باز کردن در Apps/Bots" : "Open in Apps/Bots"}</Link>
          </div>
          <div className="card-title-row">
            <h3>{locale === "fa" ? "قابلیت‌های بات" : "Bot capabilities"}</h3>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "پاسخ هوشمند" : "AI replies"}</strong>
              <span className="muted-block">{integrations.some((item) => item.active) ? (locale === "fa" ? "کانال فعال برای پاسخ یا اعلان آماده است." : "At least one active channel is ready for replies or notifications.") : locale === "fa" ? "هنوز هیچ کانال فعالی ثبت نشده است." : "No active delivery channel is registered yet."}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "هندآف فلو" : "Workflow handoff"}</strong>
              <span className="muted-block">{locale === "fa" ? `${messages.length} پیام خروجی برای تحویل یا پیگیری ثبت شده است.` : `${messages.length} outbound messages are available for delivery tracking.`}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "فرم‌های هوشمند" : "Smart forms"}</strong>
              <span className="muted-block">{publishedBuilds.length ? (locale === "fa" ? `${publishedBuilds.length} مینی‌اپ منتشر شده آماده استفاده است.` : `${publishedBuilds.length} published mini app builds are available.`) : locale === "fa" ? "هنوز مینی‌اپی منتشر نشده است." : "No mini app build has been published yet."}</span>
            </div>
          </div>
          <div className="card-title-row" style={{ marginTop: 18 }}>
            <h3>{locale === "fa" ? "وضعیت اپراتور" : "Operator status"}</h3>
          </div>
          <div className="summary-grid" style={{ marginTop: 12 }}>
            <div className="mini-card"><strong>{localizeDigits(deliveryStats.delivered, locale)}</strong><span className="muted-block">{locale === "fa" ? "تحویل‌شده" : "Delivered"}</span></div>
            <div className="mini-card"><strong>{localizeDigits(deliveryStats.failed, locale)}</strong><span className="muted-block">{locale === "fa" ? "ناموفق" : "Failed"}</span></div>
          </div>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <div>
            <strong style={{ display: "block", fontSize: "2rem" }}>{locale === "fa" ? "تجربه بات" : "Bot Experience"}</strong>
            <span className="muted-block">Telegram / Bale</span>
          </div>
          <Link href="/integrations" className="secondary-pill">{locale === "fa" ? "باز کردن" : "Open"}</Link>
        </div>
        <div className="mobile-tab-strip">
          <span className={telegramIntegration?.active ? "status-pill info" : "pill"}>Telegram</span>
          <span className={baleIntegration?.active ? "status-pill info" : "pill"}>Bale</span>
          <span className="pill">{locale === "fa" ? "قابلیت‌ها" : "Capabilities"}</span>
        </div>
        <div className="mobile-list">
          {messages.slice(0, 3).map((message) => (
            <div key={`${message.channel}-${message.id ?? message.externalChatId}`} className="mobile-list-item">
              <strong>{message.channel}</strong>
              <span className="muted-block">{message.text}</span>
            </div>
          ))}
          {!messages.length ? (
            <div className="mobile-list-item">
              <strong>{locale === "fa" ? "پیامی ثبت نشده است" : "No messages yet"}</strong>
              <span className="muted-block">{locale === "fa" ? "پس از ارسال پیام از API، اینجا نمایش داده می‌شود." : "Messages appear here after API-backed sends."}</span>
            </div>
          ) : null}
        </div>
      </div>
    </PanelShell>
  );
}

function localizeDigits(value: string, locale: "en" | "fa") {
  return locale === "fa" ? value.replace(/\d/g, (digit) => "۰۱۲۳۴۵۶۷۸۹"[Number(digit)] ?? digit) : value;
}
