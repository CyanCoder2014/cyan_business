"use client";

import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { listBotIntegrations, listBotMessages, listMiniAppBuilds, registerBotWebhook, sendBotMessage, upsertBotIntegration, upsertMiniAppBuild } from "@/lib/platform-api";
import type { BotChannelIntegration, BotMiniAppBuild, BotOutboundMessage } from "@/lib/types";

export default function IntegrationsPage() {
  const { locale } = usePanel();
  const [integrations, setIntegrations] = useState<BotChannelIntegration[]>([]);
  const [messages, setMessages] = useState<BotOutboundMessage[]>([]);
  const [miniApps, setMiniApps] = useState<BotMiniAppBuild[]>([]);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      listBotIntegrations({ tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => []),
      listBotMessages({ tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => []),
      listMiniAppBuilds({ tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => [])
    ]).then(([integrationItems, messageItems, miniAppItems]) => {
      setIntegrations(integrationItems);
      setMessages(messageItems);
      setMiniApps(miniAppItems);
    });
  }, []);

  const cards = integrations.length ? integrations : fallbackIntegrations;
  const selected = cards[0];

  async function addDemoChannel() {
    setStatus(locale === "fa" ? "در حال ذخیره کانال..." : "Saving channel...");
    try {
      const saved = await upsertBotIntegration({
        channel: "TELEGRAM",
        integrationKey: "telegram-main",
        tenantKey: "tenant-demo",
        siteKey: "site-commerce",
        appTypeHint: "SHOP",
        botUsername: "@cyan_assistant_bot",
        tokenSecretRef: "vault://bots/retail-demo",
        miniAppUrl: "https://preview.cyan.app/mini-app",
        miniAppEnabled: true,
        active: true
      });
      setIntegrations((current) => [saved, ...current.filter((item) => item.integrationKey !== saved.integrationKey)]);
      setStatus(locale === "fa" ? "کانال ذخیره شد." : "Channel saved.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ذخیره کانال ناموفق بود." : "Channel save failed.");
    }
  }

  async function testSelectedChannel() {
    setStatus(locale === "fa" ? "در حال ارسال پیام تست..." : "Sending test message...");
    try {
      const sent = await sendBotMessage({
        channel: selected.channel,
        integrationKey: selected.integrationKey,
        externalChatId: "@john_doe",
        text: "Order #12345 has been confirmed."
      });
      await registerBotWebhook(selected.channel, selected.integrationKey).catch(() => null);
      setMessages((current) => [
        {
          channel: selected.channel,
          integrationKey: selected.integrationKey,
          externalChatId: sent.externalChatId,
          text: sent.messageText,
          status: sent.status,
          attemptCount: sent.attemptCount
        },
        ...current
      ]);
      setStatus(locale === "fa" ? "پیام تست ارسال شد." : "Test message sent.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ارسال پیام ناموفق بود." : "Message send failed.");
    }
  }

  async function provisionMiniApp() {
    setStatus(locale === "fa" ? "در حال تامین مینی‌اپ..." : "Provisioning mini app...");
    try {
      const build = await upsertMiniAppBuild({
        channel: "TELEGRAM",
        integrationKey: "telegram-main",
        buildKey: "retail-demo",
        title: "Retail Demo Mini App",
        launchUrl: "https://preview.cyan.app/mini-app",
        manifest: {
          theme: "cyan",
          routes: ["/", "/orders", "/support"]
        }
      });
      setMiniApps((current) => [build, ...current.filter((item) => item.buildKey !== build.buildKey)]);
      setStatus(locale === "fa" ? "مینی‌اپ آماده شد." : "Mini app provisioned.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "تامین مینی‌اپ ناموفق بود." : "Mini app provisioning failed.");
    }
  }

  return (
    <PanelShell
      activeKey="integrations"
      title="Client Apps / Bots"
      titleFa="اپ‌ها / بات‌های مشتری"
      subtitle="Connect and manage website, Telegram, Bale, mini-app, and mobile channels from one operational control room."
      subtitleFa="وب‌سایت، تلگرام، بله، مینی‌اپ و کانال‌های موبایل را از یک اتاق فرمان واحد متصل و مدیریت کنید."
    >
      <div className="desktop-only page-grid">
        <section className="panel-card">
          <div className="toolbar-row">
            <div className="pill-row">
              <span className="status-pill success">{locale === "fa" ? "AI Orchestrator سالم" : "AI Orchestrator healthy"}</span>
              <span className="status-pill success">{locale === "fa" ? "اعلان سالم" : "Notifications healthy"}</span>
              <span className="status-pill success">{locale === "fa" ? "BPM سالم" : "BPM healthy"}</span>
            </div>
            <button type="button" className="secondary-pill" onClick={addDemoChannel}>
              {locale === "fa" ? "افزودن کانال" : "Add channel"}
            </button>
          </div>
          {status ? <div className="status-pill info" style={{ marginTop: 12 }}>{status}</div> : null}

          <div className="blueprint-grid" style={{ marginTop: 18 }}>
            {cards.map((integration) => (
              <article key={integration.integrationKey} className="channel-card">
                <strong>{integration.channel === "TELEGRAM" ? "Telegram Bot" : integration.channel === "BALE" ? "Bale Bot" : integration.integrationKey}</strong>
                <div className="muted-block">{integration.active ? (locale === "fa" ? "متصل" : "Connected") : locale === "fa" ? "در انتظار" : "Pending"}</div>
                <div className="detail-list" style={{ marginTop: 12 }}>
                  <div className="muted-block">{locale === "fa" ? "محیط" : "Environment"}: {integration.active ? "Production" : "Staging"}</div>
                  <div className="muted-block">{locale === "fa" ? "وبهوک" : "Webhook"}: {integration.active ? "Healthy" : "Pending"}</div>
                </div>
                <div className="toolbar-row" style={{ marginTop: 16 }}>
                  <span className="pill">{integration.active ? "Production" : "Staging"}</span>
                  <button type="button" className={integration.channel === "TELEGRAM" ? "primary-pill" : "secondary-pill"} onClick={testSelectedChannel}>{locale === "fa" ? "باز کردن" : "Open"}</button>
                </div>
              </article>
            ))}
            {!cards.find((item) => item.integrationKey === "mini-app") ? (
              <article className="channel-card">
                <strong>{locale === "fa" ? "مینی‌اپ" : "Mini App"}</strong>
                <div className="muted-block">
                  {miniApps.length ? `${miniApps.length} builds` : locale === "fa" ? "آماده راه‌اندازی" : "Provision-ready"}
                </div>
                <div className="toolbar-row" style={{ marginTop: 16 }}>
                  <span className="pill">Staging</span>
                  <button type="button" className="secondary-pill" onClick={provisionMiniApp}>{locale === "fa" ? "راه‌اندازی" : "Provision"}</button>
                </div>
              </article>
            ) : null}
          </div>

          <div className="pill-row" style={{ marginTop: 18 }}>
            <span className="pill status-pill info">{locale === "fa" ? "تنظیمات کانال" : "Channel Settings"}</span>
            <span className="pill">{locale === "fa" ? "نگاشت نشست" : "Session Mapping"}</span>
            <span className="pill">{locale === "fa" ? "تامین" : "Provisioning"}</span>
          </div>

          <div className="data-table-shell" style={{ marginTop: 18 }}>
            <div className="card-title-row">
              <h3>{locale === "fa" ? "ارسال پیام‌های خروجی" : "Outbound message delivery"}</h3>
            </div>
            <table className="data-table" style={{ marginTop: 12 }}>
              <thead>
                <tr>
                  <th>{locale === "fa" ? "کانال" : "Channel"}</th>
                  <th>{locale === "fa" ? "گیرنده" : "Recipient"}</th>
                  <th>{locale === "fa" ? "پیام" : "Message"}</th>
                  <th>{locale === "fa" ? "وضعیت" : "Status"}</th>
                </tr>
              </thead>
              <tbody>
                {(messages.length ? messages : fallbackMessages).slice(0, 5).map((message) => (
                  <tr key={`${message.integrationKey}-${message.externalChatId}-${message.text}`}>
                    <td>{message.channel}</td>
                    <td>{message.externalChatId}</td>
                    <td>{message.text}</td>
                    <td>{message.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="summary-grid" style={{ marginTop: 18 }}>
              <div className="mini-card"><strong>1,248</strong><span className="muted-block">{locale === "fa" ? "ارسال‌شده" : "Sent"}</span></div>
              <div className="mini-card"><strong>1,152</strong><span className="muted-block">{locale === "fa" ? "تحویل‌شده" : "Delivered"}</span></div>
            </div>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{selected.channel === "TELEGRAM" ? "Telegram Bot" : "Bale Bot"}</h3>
            <span className="status-pill success">{locale === "fa" ? "متصل" : "Connected"}</span>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "نام ربات" : "Bot name"}</strong>
              <span className="muted-block">{selected.botUsername ?? "@cyan_assistant_bot"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "آخرین همگام‌سازی" : "Last sync"}</strong>
              <span className="muted-block">{selected.updatedAt ?? (locale === "fa" ? "۱ دقیقه پیش" : "1 minute ago")}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "مینی‌اپ" : "Mini app"}</strong>
              <span className="muted-block">{selected.miniAppUrl ?? "https://preview.cyan.app/mini-app"}</span>
            </div>
          </div>
          <div className="card-title-row" style={{ marginTop: 18 }}>
            <h3>{locale === "fa" ? "اتصال / تست" : "Connect / Test"}</h3>
          </div>
          <div className="mini-card" style={{ marginTop: 12 }}>
            <strong>QR</strong>
            <span className="muted-block">{locale === "fa" ? "برای باز کردن بات اسکن کنید" : "Scan to open chat with this bot"}</span>
          </div>
          <div className="toolbar-row" style={{ marginTop: 16 }}>
            <button type="button" className="primary-pill" onClick={testSelectedChannel}>{locale === "fa" ? "ارسال پیام تست" : "Send Test Message"}</button>
          </div>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <div>
            <strong style={{ display: "block", fontSize: "2rem" }}>{locale === "fa" ? "اپ‌ها / بات‌ها" : "Client Apps / Bots"}</strong>
            <span className="muted-block">{locale === "fa" ? "اتاق کنترل کانال‌ها" : "Channels control room"}</span>
          </div>
          <button type="button" className="secondary-pill">{locale === "fa" ? "افزودن" : "Add"}</button>
        </div>
        <div className="mobile-grid">
          {cards.map((integration) => (
            <div key={integration.integrationKey} className="mobile-card">
              <strong>{integration.channel === "TELEGRAM" ? "Telegram Bot" : "Bale Bot"}</strong>
              <span className="muted-block">{integration.active ? "Connected" : "Pending"}</span>
              <div className="toolbar-row" style={{ marginTop: 12 }}>
                <span className="pill">{integration.active ? "Production" : "Staging"}</span>
                <button type="button" className="primary-pill">{locale === "fa" ? "باز کردن" : "Open"}</button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </PanelShell>
  );
}

const fallbackIntegrations: BotChannelIntegration[] = [
  {
    channel: "TELEGRAM",
    integrationKey: "telegram-main",
    tenantKey: "tenant-demo",
    siteKey: "site-commerce",
    botUsername: "@cyan_assistant_bot",
    active: true
  },
  {
    channel: "BALE",
    integrationKey: "bale-main",
    tenantKey: "tenant-demo",
    siteKey: "site-commerce",
    botUsername: "@cyan_bale_bot",
    active: true
  }
];

const fallbackMessages: BotOutboundMessage[] = [
  {
    channel: "TELEGRAM",
    integrationKey: "telegram-main",
    externalChatId: "@john_doe",
    text: "Order #12345 has been confirmed.",
    status: "Delivered",
    attemptCount: 1
  },
  {
    channel: "BALE",
    integrationKey: "bale-main",
    externalChatId: "user_98432",
    text: "Your appointment is scheduled for tomorrow.",
    status: "Delivered",
    attemptCount: 1
  }
];
