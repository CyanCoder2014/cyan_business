"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { listBotIntegrations, listBotMessages, listMiniAppBuilds, publishMiniAppBuild, registerBotWebhook, retryBotMessage, sendBotMessage, upsertBotIntegration, upsertMiniAppBuild } from "@/lib/platform-api";
import type { BotChannelIntegration, BotMiniAppBuild, BotOutboundMessage } from "@/lib/types";

const platformBaseUrl = process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8001";

export default function IntegrationsPage() {
  const [channel, setChannel] = useState<"TELEGRAM" | "BALE">("TELEGRAM");
  const [integrationKey, setIntegrationKey] = useState("retail-bot");
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [clientKey, setClientKey] = useState("client-demo");
  const [appTypeHint, setAppTypeHint] = useState("MIXED_BUSINESS_APP");
  const [botId, setBotId] = useState("");
  const [botUsername, setBotUsername] = useState("retail_demo_bot");
  const [botToken, setBotToken] = useState("");
  const [tokenSecretRef, setTokenSecretRef] = useState("vault://bots/retail-demo");
  const [webhookSecret, setWebhookSecret] = useState("");
  const [miniAppEnabled, setMiniAppEnabled] = useState(false);
  const [miniAppUrl, setMiniAppUrl] = useState("");
  const [miniAppStartParam, setMiniAppStartParam] = useState("");
  const [testChatId, setTestChatId] = useState("");
  const [testMessage, setTestMessage] = useState("Panel test message from bot-adapter-service.");
  const [integrations, setIntegrations] = useState<BotChannelIntegration[]>([]);
  const [messages, setMessages] = useState<BotOutboundMessage[]>([]);
  const [miniAppBuilds, setMiniAppBuilds] = useState<BotMiniAppBuild[]>([]);
  const [miniAppBuildKey, setMiniAppBuildKey] = useState("retail-mini-app");
  const [miniAppTitle, setMiniAppTitle] = useState("Retail Demo Mini App");
  const [miniAppManifestJson, setMiniAppManifestJson] = useState('{\n  "entry": "/start",\n  "pages": ["/", "/products", "/checkout"]\n}');
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const webhookUrl = useMemo(
    () => `${platformBaseUrl}/public/bot-adapter/${channel.toLowerCase()}/${integrationKey}/webhook`,
    [channel, integrationKey]
  );
  const miniAppLaunchUrl = useMemo(() => {
    if (!miniAppEnabled || !miniAppUrl.trim()) {
      return null;
    }
    try {
      const url = new URL(miniAppUrl);
      if (miniAppStartParam.trim()) {
        url.searchParams.set("startapp", miniAppStartParam.trim());
      }
      return url.toString();
    } catch {
      return "Invalid mini app URL";
    }
  }, [miniAppEnabled, miniAppStartParam, miniAppUrl]);

  async function refresh() {
    setLoading(true);
    setStatus(null);
    try {
      setIntegrations(await listBotIntegrations({ tenantKey, siteKey }));
      setMessages(await listBotMessages({ tenantKey, siteKey, integrationKey }));
      setMiniAppBuilds(await listMiniAppBuilds({ tenantKey, siteKey }));
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load bot integrations");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function saveIntegration() {
    setLoading(true);
    setStatus(null);
    try {
      await upsertBotIntegration({
        channel,
        integrationKey,
        tenantKey,
        siteKey,
        clientKey,
        appTypeHint,
        botId,
        botUsername,
        botToken,
        tokenSecretRef,
        webhookSecret,
        miniAppEnabled,
        miniAppUrl,
        miniAppStartParam,
        active: true
      });
      setBotToken("");
      await refresh();
      setStatus("Bot integration saved. Register the generated webhook URL with the channel provider.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to save bot integration");
    } finally {
      setLoading(false);
    }
  }

  async function registerWebhookNow() {
    setLoading(true);
    setStatus(null);
    try {
      const result = await registerBotWebhook(channel, integrationKey);
      setStatus(`Webhook registration requested for ${result.channel}. Target URL: ${result.webhookUrl}`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to register webhook");
    } finally {
      setLoading(false);
    }
  }

  async function sendTestMessage() {
    setLoading(true);
    setStatus(null);
    try {
      const result = await sendBotMessage({
        channel,
        integrationKey,
        externalChatId: testChatId,
        text: testMessage
      });
      await refresh();
      setStatus(`Outbound message ${result.status.toLowerCase()} via ${result.provider} to chat ${result.externalChatId}. Delivery ${result.deliveryId}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to send outbound bot message");
    } finally {
      setLoading(false);
    }
  }

  async function retryMessage(messageId: string) {
    setLoading(true);
    setStatus(null);
    try {
      const result = await retryBotMessage(messageId);
      await refresh();
      setStatus(`Retry ${result.status.toLowerCase()} for delivery ${result.deliveryId}. Attempt ${result.attemptCount}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to retry outbound message");
    } finally {
      setLoading(false);
    }
  }

  async function saveMiniAppBuild() {
    setLoading(true);
    setStatus(null);
    try {
      await upsertMiniAppBuild({
        channel,
        integrationKey,
        buildKey: miniAppBuildKey,
        title: miniAppTitle,
        launchUrl: miniAppLaunchUrl ?? miniAppUrl,
        manifest: miniAppManifestJson.trim() ? JSON.parse(miniAppManifestJson) : {}
      });
      await refresh();
      setStatus(`Mini app build ${miniAppBuildKey} saved.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to save mini app build");
    } finally {
      setLoading(false);
    }
  }

  async function publishMiniApp() {
    setLoading(true);
    setStatus(null);
    try {
      const build = await publishMiniAppBuild(channel, integrationKey, miniAppBuildKey);
      await refresh();
      setStatus(`Mini app ${build.buildKey} published to ${build.publishedUrl ?? build.launchUrl ?? "runtime URL"}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to publish mini app build");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Client Apps And Bots" subtitle="Manage every presentation channel attached to a generated business app.">
      <div className="studio-grid">
        <section className="panel rail">
          <div className="editor-toolbar">
            <div>
              <p className="section-title">Telegram/Bale integration</p>
              <div className="meta">Stores token references only. Webhooks are idempotently forwarded to AI sessions.</div>
            </div>
            <Link className="ghost-btn" href="/bot">
              Open bot flow
            </Link>
          </div>

          <div className="form-grid">
            <div className="field">
              <label>Channel</label>
              <div className="chip-row">
                {(["TELEGRAM", "BALE"] as const).map((item) => (
                  <button key={item} type="button" className={`chip ${channel === item ? "active" : ""}`} onClick={() => setChannel(item)}>
                    {item}
                  </button>
                ))}
              </div>
            </div>

            <div className="field-grid">
              <div className="field">
                <label htmlFor="integrationKey">Integration key</label>
                <input id="integrationKey" value={integrationKey} onChange={(event) => setIntegrationKey(event.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="botId">Bot id</label>
                <input id="botId" value={botId} onChange={(event) => setBotId(event.target.value)} placeholder="123456789" />
              </div>
            </div>

            <div className="field-grid">
              <div className="field">
                <label htmlFor="botUsername">Bot username</label>
                <input id="botUsername" value={botUsername} onChange={(event) => setBotUsername(event.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="botToken">Bot token/key</label>
                <input
                  id="botToken"
                  value={botToken}
                  onChange={(event) => setBotToken(event.target.value)}
                  placeholder="Write-only; not returned by API"
                />
              </div>
            </div>

            <div className="field-grid">
              <div className="field">
                <label htmlFor="tenantKey">Tenant key</label>
                <input id="tenantKey" value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="siteKey">Site key</label>
                <input id="siteKey" value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
              </div>
            </div>

            <div className="field-grid">
              <div className="field">
                <label htmlFor="clientKey">Client key</label>
                <input id="clientKey" value={clientKey} onChange={(event) => setClientKey(event.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="appTypeHint">App type hint</label>
                <input id="appTypeHint" value={appTypeHint} onChange={(event) => setAppTypeHint(event.target.value)} />
              </div>
            </div>

            <div className="field">
              <label htmlFor="tokenSecretRef">Token secret reference</label>
              <input id="tokenSecretRef" value={tokenSecretRef} onChange={(event) => setTokenSecretRef(event.target.value)} />
            </div>

            <div className="field">
              <label htmlFor="webhookSecret">Webhook secret</label>
              <input id="webhookSecret" value={webhookSecret} onChange={(event) => setWebhookSecret(event.target.value)} />
            </div>

            <div className="field">
              <label>Mini app</label>
              <button type="button" className={`chip ${miniAppEnabled ? "active" : ""}`} onClick={() => setMiniAppEnabled((value) => !value)}>
                {miniAppEnabled ? "Mini app enabled" : "Mini app disabled"}
              </button>
            </div>

            <div className="field-grid">
              <div className="field">
                <label htmlFor="miniAppUrl">Mini app URL</label>
                <input id="miniAppUrl" value={miniAppUrl} onChange={(event) => setMiniAppUrl(event.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="miniAppStartParam">Mini app start param</label>
                <input id="miniAppStartParam" value={miniAppStartParam} onChange={(event) => setMiniAppStartParam(event.target.value)} />
              </div>
            </div>

            <div className="result-card">
              <h4>Webhook URL</h4>
              <p className="muted">{webhookUrl}</p>
            </div>

            <div className="result-card">
              <h4>Mini app launch URL</h4>
              <p className="muted">{miniAppLaunchUrl ?? "Enable mini app and provide a URL to generate a launch link."}</p>
            </div>

            <div className="result-card">
              <h4>Mini app runtime build</h4>
              <div className="form-grid">
                <div className="field-grid">
                  <div className="field">
                    <label>Build key</label>
                    <input value={miniAppBuildKey} onChange={(event) => setMiniAppBuildKey(event.target.value)} />
                  </div>
                  <div className="field">
                    <label>Title</label>
                    <input value={miniAppTitle} onChange={(event) => setMiniAppTitle(event.target.value)} />
                  </div>
                </div>
                <div className="field">
                  <label>Manifest JSON</label>
                  <textarea value={miniAppManifestJson} onChange={(event) => setMiniAppManifestJson(event.target.value)} />
                </div>
                <div className="hero-actions">
                  <button type="button" className="btn" onClick={saveMiniAppBuild} disabled={loading || !miniAppEnabled}>Save mini app build</button>
                  <button type="button" className="ghost-btn" onClick={publishMiniApp} disabled={loading || !miniAppEnabled}>Publish mini app</button>
                </div>
              </div>
            </div>

            <div className="hero-actions">
              <button type="button" className="btn" onClick={saveIntegration} disabled={loading}>
                {loading ? "Saving..." : "Save integration"}
              </button>
              <button type="button" className="ghost-btn" onClick={registerWebhookNow} disabled={loading}>
                Register webhook
              </button>
              <button type="button" className="ghost-btn" onClick={refresh} disabled={loading}>
                Refresh
              </button>
            </div>

            <div className="result-card">
              <h4>Outbound test message</h4>
              <div className="form-grid">
                <div className="field">
                  <label htmlFor="testChatId">External chat id</label>
                  <input id="testChatId" value={testChatId} onChange={(event) => setTestChatId(event.target.value)} placeholder="Telegram/Bale chat id" />
                </div>
                <div className="field">
                  <label htmlFor="testMessage">Message text</label>
                  <textarea id="testMessage" value={testMessage} onChange={(event) => setTestMessage(event.target.value)} />
                </div>
                <button type="button" className="btn" onClick={sendTestMessage} disabled={loading || !testChatId.trim()}>
                  Send test message
                </button>
              </div>
            </div>

            {status ? (
              <div className="result-card">
                <h4>Status</h4>
                <p className="muted">{status}</p>
              </div>
            ) : null}
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Saved bot integrations</p>
            <div className="draft-list">
              {integrations.map((integration) => (
                <button
                  key={`${integration.channel}-${integration.integrationKey}`}
                  type="button"
                  className="draft-item"
                  onClick={() => {
                    setChannel(integration.channel);
                    setIntegrationKey(integration.integrationKey);
                    setTenantKey(integration.tenantKey);
                    setSiteKey(integration.siteKey);
                    setClientKey(integration.clientKey ?? "");
                    setAppTypeHint(integration.appTypeHint ?? "");
                    setBotId(integration.botId ?? "");
                    setBotUsername(integration.botUsername ?? "");
                    setBotToken("");
                    setTokenSecretRef(integration.tokenSecretRef ?? "");
                    setWebhookSecret(integration.webhookSecret ?? "");
                    setMiniAppEnabled(Boolean(integration.miniAppEnabled));
                    setMiniAppUrl(integration.miniAppUrl ?? "");
                    setMiniAppStartParam(integration.miniAppStartParam ?? "");
                  }}
                >
                  <strong>
                    <span>{integration.integrationKey}</span>
                    <span className="muted">{integration.channel}</span>
                  </strong>
                  <span className="muted">{integration.tenantKey} / {integration.siteKey}</span>
                  <span className="muted">
                    {integration.active ? "active" : "inactive"} / {integration.miniAppEnabled ? "mini app" : "bot only"}
                  </span>
                  <span className="muted">{integration.tokenFingerprint ? `token ${integration.tokenFingerprint}` : "token ref only"}</span>
                  <span className="muted">{integration.miniAppUrl ? `mini app ${integration.miniAppUrl}` : "mini app not set"}</span>
                </button>
              ))}
            </div>
          </section>

          <section className="panel rail">
            <p className="section-title">Outbound deliveries</p>
            <div className="draft-list">
              {messages.map((message) => (
                <div key={message.id} className="draft-item">
                  <strong>
                    <span>{message.integrationKey}</span>
                    <span className="muted">{message.status}</span>
                  </strong>
                  <span className="muted">chat {message.externalChatId} / attempts {message.attemptCount}</span>
                  <span className="muted">{message.text}</span>
                  <span className="muted">{message.errorMessage ?? "Delivered or pending without provider error."}</span>
                  <button
                    type="button"
                    className="chip"
                    onClick={() => message.id && retryMessage(message.id)}
                    disabled={loading || !message.id}
                  >
                    Retry
                  </button>
                </div>
              ))}
            </div>
          </section>

          <section className="panel rail">
            <p className="section-title">Mini app builds</p>
            <div className="draft-list">
              {miniAppBuilds.map((build) => (
                <button
                  key={build.id ?? `${build.channel}-${build.integrationKey}-${build.buildKey}`}
                  type="button"
                  className="draft-item"
                  onClick={() => {
                    setChannel(build.channel);
                    setIntegrationKey(build.integrationKey);
                    setMiniAppBuildKey(build.buildKey);
                    setMiniAppTitle(build.title ?? build.buildKey);
                    setMiniAppManifestJson(JSON.stringify(build.manifest ?? {}, null, 2));
                  }}
                >
                  <strong>
                    <span>{build.buildKey}</span>
                    <span className="muted">{build.status ?? "DRAFT"}</span>
                  </strong>
                  <span className="muted">{build.launchUrl ?? "launch URL missing"}</span>
                  <span className="muted">{build.publishedUrl ?? "not published"}</span>
                </button>
              ))}
            </div>
          </section>

          <section className="panel rail">
            <p className="section-title">Next channels</p>
            <div className="mini-grid" style={{ gridTemplateColumns: "1fr" }}>
              {["Website/PWA publish", "Mobile app shell"].map((title) => (
                <div key={title} className="mini-card">
                  <h3>{title}</h3>
                  <p>Planned after bot, mini app runtime, tenant mapping, and public app contracts are stable.</p>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>
    </AppShell>
  );
}
