"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import {
  listBotIntegrations,
  listBotMessages,
  listMiniAppBuilds,
  publishMiniAppBuild,
  registerBotWebhook,
  sendBotMessage,
  upsertBotIntegration,
  upsertMiniAppBuild
} from "@/lib/platform-api";
import type { BotChannelIntegration, BotMiniAppBuild, BotOutboundMessage } from "@/lib/types";

const scope = { tenantKey: "tenant-demo", siteKey: "site-commerce" };

export default function IntegrationsPage() {
  const { locale } = usePanel();
  const [integrations, setIntegrations] = useState<BotChannelIntegration[]>([]);
  const [messages, setMessages] = useState<BotOutboundMessage[]>([]);
  const [miniApps, setMiniApps] = useState<BotMiniAppBuild[]>([]);
  const [selectedIntegrationKey, setSelectedIntegrationKey] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  async function refresh() {
    const [integrationItems, messageItems, miniAppItems] = await Promise.all([
      listBotIntegrations(scope),
      listBotMessages(scope),
      listMiniAppBuilds(scope)
    ]);
    setIntegrations(integrationItems);
    setMessages(messageItems);
    setMiniApps(miniAppItems);
    setSelectedIntegrationKey((current) => current ?? integrationItems[0]?.integrationKey ?? null);
  }

  useEffect(() => {
    refresh().catch((error) => {
      setIntegrations([]);
      setMessages([]);
      setMiniApps([]);
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "یکپارچه‌سازی‌ها بارگیری نشدند." : "Integrations could not be loaded.");
    });
  }, [locale]);

  const selected = useMemo(
    () =>
      integrations.find((integration) => integration.integrationKey === selectedIntegrationKey) ??
      integrations[0] ??
      null,
    [integrations, selectedIntegrationKey]
  );
  const selectedMessages = useMemo(
    () => (selected ? messages.filter((message) => message.integrationKey === selected.integrationKey) : []),
    [messages, selected]
  );
  const selectedMiniApps = useMemo(
    () => (selected ? miniApps.filter((item) => item.integrationKey === selected.integrationKey) : miniApps),
    [miniApps, selected]
  );
  const deliveryStats = useMemo(() => {
    const sent = selectedMessages.length;
    const delivered = selectedMessages.filter((message) => String(message.status).toUpperCase().includes("DELIVER")).length;
    const failed = selectedMessages.filter((message) => String(message.status).toUpperCase().includes("FAIL")).length;
    return { sent, delivered, failed };
  }, [selectedMessages]);

  async function addChannel() {
    setStatus(locale === "fa" ? "در حال ذخیره کانال..." : "Saving channel...");
    try {
      const saved = await upsertBotIntegration({
        channel: "TELEGRAM",
        integrationKey: "telegram-main",
        tenantKey: scope.tenantKey,
        siteKey: scope.siteKey,
        appTypeHint: "SHOP",
        botUsername: "@cyan_assistant_bot",
        tokenSecretRef: "vault://bots/retail-demo",
        miniAppUrl: "https://preview.cyan.app/mini-app",
        miniAppEnabled: true,
        active: true
      });
      setIntegrations((current) => [saved, ...current.filter((item) => item.integrationKey !== saved.integrationKey)]);
      setSelectedIntegrationKey(saved.integrationKey);
      setStatus(locale === "fa" ? "کانال ذخیره شد." : "Channel saved.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ذخیره کانال ناموفق بود." : "Channel save failed.");
    }
  }

  async function testSelectedChannel() {
    if (!selected) {
      setStatus(locale === "fa" ? "ابتدا یک کانال بسازید." : "Create a channel first.");
      return;
    }
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
          id: sent.deliveryId,
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
    if (!selected) {
      setStatus(locale === "fa" ? "ابتدا یک کانال بسازید." : "Create a channel first.");
      return;
    }
    setStatus(locale === "fa" ? "در حال تامین مینی‌اپ..." : "Provisioning mini app...");
    try {
      const build = await upsertMiniAppBuild({
        channel: selected.channel,
        integrationKey: selected.integrationKey,
        buildKey: `${selected.integrationKey}-build`,
        title: `${selected.botUsername ?? selected.integrationKey} Mini App`,
        launchUrl: selected.miniAppUrl ?? "https://preview.cyan.app/mini-app",
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

  async function publishSelectedMiniApp() {
    const build = selectedMiniApps[0];
    if (!selected || !build) {
      setStatus(locale === "fa" ? "ابتدا یک build مینی‌اپ بسازید." : "Create a mini app build first.");
      return;
    }
    setStatus(locale === "fa" ? "در حال انتشار مینی‌اپ..." : "Publishing mini app...");
    try {
      const published = await publishMiniAppBuild(selected.channel, selected.integrationKey, build.buildKey);
      setMiniApps((current) => [published, ...current.filter((item) => item.buildKey !== published.buildKey)]);
      setStatus(locale === "fa" ? "مینی‌اپ منتشر شد." : "Mini app published.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "انتشار مینی‌اپ ناموفق بود." : "Mini app publish failed.");
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
              <span className={`status-pill ${integrations.length ? "success" : "warning"}`}>
                {integrations.length
                  ? locale === "fa" ? "اتصال‌های backend بارگیری شد" : "Backend integrations loaded"
                  : locale === "fa" ? "کانالی بارگیری نشد" : "No channel returned"}
              </span>
              <span className={`status-pill ${miniApps.length ? "success" : "warning"}`}>
                {miniApps.length
                  ? locale === "fa" ? "build مینی‌اپ موجود است" : "Mini app build available"
                  : locale === "fa" ? "build مینی‌اپ موجود نیست" : "No mini app build"}
              </span>
              <span className={`status-pill ${messages.length ? "success" : "warning"}`}>
                {messages.length
                  ? locale === "fa" ? "سابقه ارسال موجود است" : "Delivery history available"
                  : locale === "fa" ? "سابقه ارسال خالی است" : "No delivery history"}
              </span>
            </div>
            <div className="pill-row">
              <button type="button" className="secondary-pill" onClick={() => refresh().catch((error) => setStatus(error instanceof Error ? error.message : "Refresh failed"))}>
                {locale === "fa" ? "بازخوانی" : "Refresh"}
              </button>
              <button type="button" className="secondary-pill" onClick={addChannel}>
                {locale === "fa" ? "افزودن کانال" : "Add channel"}
              </button>
            </div>
          </div>
          {status ? <div className="status-pill info" style={{ marginTop: 12 }}>{status}</div> : null}

          <div className="blueprint-grid" style={{ marginTop: 18 }}>
            {integrations.map((integration) => {
              const integrationMessages = messages.filter((message) => message.integrationKey === integration.integrationKey);
              const integrationMiniApps = miniApps.filter((item) => item.integrationKey === integration.integrationKey);
              const isSelected = integration.integrationKey === selected?.integrationKey;
              return (
                <article key={integration.integrationKey} className={isSelected ? "channel-card active" : "channel-card"}>
                  <strong>{integration.channel === "TELEGRAM" ? "Telegram Bot" : integration.channel === "BALE" ? "Bale Bot" : integration.integrationKey}</strong>
                  <div className="muted-block">{integration.botUsername ?? integration.integrationKey}</div>
                  <div className="detail-list" style={{ marginTop: 12 }}>
                    <div className="muted-block">{locale === "fa" ? "وضعیت" : "Status"}: {integration.active ? (locale === "fa" ? "متصل" : "Connected") : locale === "fa" ? "غیرفعال" : "Inactive"}</div>
                    <div className="muted-block">{locale === "fa" ? "ارسال‌ها" : "Messages"}: {integrationMessages.length}</div>
                    <div className="muted-block">{locale === "fa" ? "مینی‌اپ‌ها" : "Mini apps"}: {integrationMiniApps.length}</div>
                  </div>
                  <div className="toolbar-row" style={{ marginTop: 16 }}>
                    <span className="pill">{integration.active ? "Production" : "Draft"}</span>
                    <button type="button" className={integration.channel === "TELEGRAM" ? "primary-pill" : "secondary-pill"} onClick={() => setSelectedIntegrationKey(integration.integrationKey)}>
                      {locale === "fa" ? "انتخاب" : "Select"}
                    </button>
                  </div>
                </article>
              );
            })}
            {!integrations.length ? (
              <article className="channel-card">
                <strong>{locale === "fa" ? "کانالی از backend دریافت نشد" : "No channels returned by backend"}</strong>
                <div className="muted-block">
                  {locale === "fa" ? "این صفحه دیگر از کارت‌های ساختگی استفاده نمی‌کند." : "This page no longer falls back to fabricated channel cards."}
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
                {selectedMessages.slice(0, 5).map((message) => (
                  <tr key={message.id ?? `${message.integrationKey}-${message.externalChatId}-${message.text}`}>
                    <td>{message.channel}</td>
                    <td>{message.externalChatId}</td>
                    <td>{message.text}</td>
                    <td>{message.status}</td>
                  </tr>
                ))}
                {!selectedMessages.length ? (
                  <tr>
                    <td colSpan={4}>{locale === "fa" ? "پیامی برای این کانال ثبت نشده است." : "No outbound messages were returned for this channel."}</td>
                  </tr>
                ) : null}
              </tbody>
            </table>
            <div className="summary-grid" style={{ marginTop: 18 }}>
              <div className="mini-card"><strong>{deliveryStats.sent}</strong><span className="muted-block">{locale === "fa" ? "ارسال‌شده" : "Sent"}</span></div>
              <div className="mini-card"><strong>{deliveryStats.delivered}</strong><span className="muted-block">{locale === "fa" ? "تحویل‌شده" : "Delivered"}</span></div>
              <div className="mini-card"><strong>{deliveryStats.failed}</strong><span className="muted-block">{locale === "fa" ? "ناموفق" : "Failed"}</span></div>
            </div>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{selected ? (selected.channel === "TELEGRAM" ? "Telegram Bot" : "Bale Bot") : locale === "fa" ? "بدون کانال" : "No channel selected"}</h3>
            <span className={selected?.active ? "status-pill success" : "status-pill warning"}>
              {selected ? (selected.active ? (locale === "fa" ? "متصل" : "Connected") : locale === "fa" ? "غیرفعال" : "Inactive") : locale === "fa" ? "خالی" : "Empty"}
            </span>
          </div>
          {selected ? (
            <>
              <div className="detail-list" style={{ marginTop: 16 }}>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "نام ربات" : "Bot name"}</strong>
                  <span className="muted-block">{selected.botUsername ?? selected.integrationKey}</span>
                </div>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "آخرین همگام‌سازی" : "Last sync"}</strong>
                  <span className="muted-block">{selected.updatedAt ?? (locale === "fa" ? "ثبت نشده" : "Not reported")}</span>
                </div>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "مینی‌اپ" : "Mini app"}</strong>
                  <span className="muted-block">{selectedMiniApps[0]?.publishedUrl ?? selectedMiniApps[0]?.launchUrl ?? selected.miniAppUrl ?? "—"}</span>
                </div>
                <div className="detail-item">
                  <strong>{locale === "fa" ? "توکن" : "Token secret"}</strong>
                  <span className="muted-block">{selected.tokenSecretRef ?? "—"}</span>
                </div>
              </div>
              <div className="card-title-row" style={{ marginTop: 18 }}>
                <h3>{locale === "fa" ? "اتصال / تست" : "Connect / Test"}</h3>
              </div>
              <div className="mini-card" style={{ marginTop: 12 }}>
                <strong>{selectedMiniApps[0]?.status ?? (locale === "fa" ? "بدون build" : "No build")}</strong>
                <span className="muted-block">
                  {selectedMiniApps[0]?.publishedUrl ?? selectedMiniApps[0]?.launchUrl ?? (locale === "fa" ? "برای این کانال هنوز URL مینی‌اپ گزارش نشده است." : "No mini app URL has been reported for this channel yet.")}
                </span>
              </div>
              <div className="toolbar-row" style={{ marginTop: 16, flexWrap: "wrap" }}>
                <button type="button" className="primary-pill" onClick={testSelectedChannel}>{locale === "fa" ? "ارسال پیام تست" : "Send test message"}</button>
                <button type="button" className="secondary-pill" onClick={provisionMiniApp}>{locale === "fa" ? "ساخت build" : "Create build"}</button>
                <button type="button" className="secondary-pill" onClick={publishSelectedMiniApp}>{locale === "fa" ? "انتشار مینی‌اپ" : "Publish mini app"}</button>
              </div>
            </>
          ) : (
            <div className="mini-card" style={{ marginTop: 16 }}>
              <strong>{locale === "fa" ? "کانالی برای مدیریت وجود ندارد" : "No channel is available to manage"}</strong>
              <span className="muted-block">{locale === "fa" ? "با Add channel یک اتصال واقعی در backend بسازید." : "Use Add channel to create a real backend integration."}</span>
            </div>
          )}
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <div>
            <strong style={{ display: "block", fontSize: "2rem" }}>{locale === "fa" ? "اپ‌ها / بات‌ها" : "Client Apps / Bots"}</strong>
            <span className="muted-block">{locale === "fa" ? "اتاق کنترل کانال‌ها" : "Channels control room"}</span>
          </div>
          <button type="button" className="secondary-pill" onClick={addChannel}>{locale === "fa" ? "افزودن" : "Add"}</button>
        </div>
        <div className="mobile-grid">
          {integrations.map((integration) => (
            <div key={integration.integrationKey} className="mobile-card">
              <strong>{integration.channel === "TELEGRAM" ? "Telegram Bot" : "Bale Bot"}</strong>
              <span className="muted-block">{integration.active ? "Connected" : "Inactive"}</span>
              <div className="toolbar-row" style={{ marginTop: 12 }}>
                <span className="pill">{messages.filter((message) => message.integrationKey === integration.integrationKey).length} msgs</span>
                <button type="button" className="primary-pill" onClick={() => setSelectedIntegrationKey(integration.integrationKey)}>{locale === "fa" ? "انتخاب" : "Select"}</button>
              </div>
            </div>
          ))}
          {!integrations.length ? (
            <div className="mobile-card">
              <strong>{locale === "fa" ? "کانالی یافت نشد" : "No channels found"}</strong>
              <span className="muted-block">{locale === "fa" ? "backend هنوز کانالی برنگردانده است." : "The backend has not returned any channels yet."}</span>
            </div>
          ) : null}
        </div>
      </div>
    </PanelShell>
  );
}
