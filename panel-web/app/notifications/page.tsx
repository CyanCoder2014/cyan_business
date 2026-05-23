"use client";

import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { createDefinitionFromTemplate, listRecords, submitRecord } from "@/lib/dynamic-api";
import { getNotificationMessage, sendNotification } from "@/lib/service-api";
import type { DynamicEntityRecord } from "@/lib/types";

export default function NotificationsPage() {
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [templateKey, setTemplateKey] = useState("welcome-webhook");
  const [channel, setChannel] = useState("WEBHOOK");
  const [provider, setProvider] = useState("rest-webhook");
  const [recipient, setRecipient] = useState("https://example.com/hooks/customer");
  const [subject, setSubject] = useState("Welcome");
  const [body, setBody] = useState("Hello {{name}}, your storefront flow is live.");
  const [modelJson, setModelJson] = useState('{\n  "name": "Retail Demo"\n}');
  const [templates, setTemplates] = useState<DynamicEntityRecord[]>([]);
  const [messageRecord, setMessageRecord] = useState<Record<string, unknown> | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function refresh() {
    await createDefinitionFromTemplate("notification-service", "notification-template", "notification-template", { tenantKey, siteKey }).catch(() => null);
    await createDefinitionFromTemplate("notification-service", "notification-message", "notification-message", { tenantKey, siteKey }).catch(() => null);
    const items = await listRecords("notification-service", "notification-template", { tenantKey, siteKey }).catch(() => []);
    setTemplates(items);
  }

  useEffect(() => {
    refresh().catch((error) => setStatus(error instanceof Error ? error.message : "Failed to load notification templates"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantKey, siteKey]);

  async function saveTemplate() {
    setLoading(true);
    setStatus(null);
    try {
      await submitRecord("notification-service", "notification-template", templateKey, {
        templateKey,
        channel,
        provider,
        subjectTemplate: subject,
        bodyTemplate: body,
        active: "true"
      }, { tenantKey, siteKey });
      await refresh();
      setStatus(`Notification template ${templateKey} saved.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to save notification template");
    } finally {
      setLoading(false);
    }
  }

  async function dispatch() {
    setLoading(true);
    setStatus(null);
    try {
      const messageKey = `message-${Date.now().toString(36)}`;
      const result = await sendNotification({
        messageKey,
        channel,
        templateKey,
        provider,
        dispatchMode: "SYNC",
        recipient,
        subject,
        body,
        model: modelJson.trim() ? JSON.parse(modelJson) : {},
        relatedRef: {
          tenantKey,
          siteKey
        }
      });
      const message = await getNotificationMessage(messageKey).catch(() => null);
      setMessageRecord(message);
      setStatus(`Notification ${result.status ?? "sent"} as ${messageKey}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to dispatch notification");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Notification Builder" subtitle="Author service-owned templates and dispatch live test messages from the same operator surface.">
      <div className="studio-grid">
        <section className="panel rail">
          <div className="form-grid">
            <div className="field-grid">
              <div className="field">
                <label>Tenant key</label>
                <input value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Site key</label>
                <input value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
              </div>
            </div>
            <div className="field-grid">
              <div className="field">
                <label>Template key</label>
                <input value={templateKey} onChange={(event) => setTemplateKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Channel</label>
                <input value={channel} onChange={(event) => setChannel(event.target.value)} />
              </div>
            </div>
            <div className="field">
              <label>Provider</label>
              <input value={provider} onChange={(event) => setProvider(event.target.value)} />
            </div>
            <div className="field">
              <label>Recipient</label>
              <input value={recipient} onChange={(event) => setRecipient(event.target.value)} />
            </div>
            <div className="field">
              <label>Subject</label>
              <input value={subject} onChange={(event) => setSubject(event.target.value)} />
            </div>
            <div className="field">
              <label>Body</label>
              <textarea value={body} onChange={(event) => setBody(event.target.value)} />
            </div>
            <div className="field">
              <label>Model JSON</label>
              <textarea value={modelJson} onChange={(event) => setModelJson(event.target.value)} />
            </div>
            <div className="hero-actions">
              <button type="button" className="btn" onClick={saveTemplate} disabled={loading}>Save template</button>
              <button type="button" className="ghost-btn" onClick={dispatch} disabled={loading}>Send test message</button>
            </div>
            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>
        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Saved templates</p>
            <div className="draft-list">
              {templates.map((item) => (
                <button
                  key={item.recordKey}
                  type="button"
                  className="draft-item"
                  onClick={() => {
                    setTemplateKey(item.recordKey);
                    setChannel(String(item.data?.channel ?? "WEBHOOK"));
                    setProvider(String(item.data?.provider ?? "rest-webhook"));
                    setSubject(String(item.data?.subjectTemplate ?? ""));
                    setBody(String(item.data?.bodyTemplate ?? ""));
                  }}
                >
                  <strong><span>{item.recordKey}</span><span className="muted">{String(item.data?.channel ?? "")}</span></strong>
                  <span className="muted">{String(item.data?.provider ?? "")}</span>
                </button>
              ))}
            </div>
          </section>
          <section className="panel rail">
            <p className="section-title">Latest message record</p>
            <pre className="json-view">{JSON.stringify(messageRecord, null, 2)}</pre>
          </section>
        </aside>
      </div>
    </AppShell>
  );
}
