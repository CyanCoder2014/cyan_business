"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { generatePlatformApp } from "@/lib/platform-api";
import { appendBotMessage, createBotSession, listBotSessions, updateBotSession } from "@/lib/bot-session-api";
import type { BotChannel, BotConversationSession, GeneratePlatformAppResponse, PlatformAppType } from "@/lib/types";

const channelPresets: Record<BotChannel, { label: string; command: string; help: string }> = {
  telegram: {
    label: "Telegram",
    command: "/newapp",
    help: "Start a structured app generation conversation inside Telegram."
  },
  bale: {
    label: "Bale",
    command: "/create",
    help: "Use the same orchestrator flow for Bale-based operator onboarding."
  }
};

function parseAnswersJson(value: string): Record<string, unknown> {
  return value.trim() ? (JSON.parse(value) as Record<string, unknown>) : {};
}

export default function BotStudioPage() {
  const [channel, setChannel] = useState<BotChannel>("telegram");
  const [prompt, setPrompt] = useState("Build a CRM and storefront app for a local retailer.");
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-retail");
  const [appType, setAppType] = useState<PlatformAppType>("MIXED_BUSINESS_APP");
  const [answers, setAnswers] = useState(`{\n  "businessName": "Retail Demo"\n}`);
  const [response, setResponse] = useState<GeneratePlatformAppResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sessions, setSessions] = useState<BotConversationSession[]>([]);
  const [activeSessionId, setActiveSessionId] = useState<string | null>(null);
  const [messageText, setMessageText] = useState("Need a storefront with CRM and invoice workflow.");

  const currentPreset = useMemo(() => channelPresets[channel], [channel]);

  const activeSession = sessions.find((session) => session.id === activeSessionId) ?? null;

  useEffect(() => {
    listBotSessions().then((items) => {
      setSessions(items);
      setActiveSessionId((current) => current ?? items[0]?.id ?? null);
      if (items[0]) {
        setChannel(items[0].channel);
        setTenantKey(items[0].tenantKey);
        setSiteKey(items[0].siteKey);
      }
    });
  }, []);

  async function refreshSessions() {
    const items = await listBotSessions();
    setSessions(items);
  }

  async function createThread() {
    try {
      const session = await createBotSession({
        channel,
        title: `${currentPreset.label} thread`,
        tenantKey,
        siteKey,
        draftId: null,
        status: "OPEN",
        appType,
        lastPrompt: prompt,
        answers: parseAnswersJson(answers),
        messages: []
      });
      await appendBotMessage(session.id, {
        role: "system",
        content: `Session started for ${currentPreset.label} with command ${currentPreset.command}.`
      });
      await appendBotMessage(session.id, {
        role: "user",
        content: prompt
      });
      await refreshSessions();
      setActiveSessionId(session.id);
    } catch (ex) {
      setError(ex instanceof Error ? ex.message : "Failed to create session");
    }
  }

  async function generateForBot() {
    setLoading(true);
    setError(null);
    try {
      const parsedAnswers = parseAnswersJson(answers);
      const generated = await generatePlatformApp({
        prompt,
        tenantKey,
        siteKey,
        execute: false,
        answers: {
          ...parsedAnswers,
          channel,
          appType
        }
      });
      setResponse(generated);

      if (activeSession) {
        const session = await updateBotSession(activeSession.id, {
          channel,
          tenantKey,
          siteKey,
          appType,
          title: generated.dsl.app.title ?? activeSession.title,
          draftId: generated.dsl.app.appKey ?? activeSession.draftId,
          status: generated.nextQuestions.length ? "WAITING_FOR_ANSWERS" : "RESOLVED",
          lastPrompt: prompt,
          answers: parsedAnswers,
        });
        if (session) {
          await appendBotMessage(session.id, {
            role: "assistant",
            content: generated.nextQuestions.length
              ? `I need one more thing: ${generated.nextQuestions[0]}`
              : `Generated ${generated.dsl.app.title ?? "the app"} and it is ready for review.`
          });
          await refreshSessions();
          setActiveSessionId(session.id);
        }
      }
    } catch (ex) {
      setError(ex instanceof Error ? ex.message : "Bot generation failed");
    } finally {
      setLoading(false);
    }
  }

  async function sendMessage() {
    if (!activeSession || !messageText.trim()) {
      return;
    }
    await appendBotMessage(activeSession.id, {
      role: "user",
      content: messageText.trim()
    });
    await updateBotSession(activeSession.id, {
      lastPrompt: messageText.trim(),
      status: "OPEN"
    });
    setMessageText("");
    await refreshSessions();
  }

  return (
    <AppShell
      title="Bot Adapter Flow"
      subtitle="Use the same orchestration endpoint from Telegram or Bale conversations."
    >
      <div className="studio-grid">
        <section className="panel rail">
          <div className="editor-toolbar">
            <div>
              <p className="section-title">Sessions</p>
              <div className="meta">Resume bot threads across Telegram and Bale.</div>
            </div>
            <button type="button" className="btn" onClick={createThread}>
              Start thread
            </button>
          </div>

          <div className="draft-list" style={{ marginBottom: 20 }}>
            {sessions.map((session) => (
              <button
                key={session.id}
                type="button"
                className={`draft-item ${activeSessionId === session.id ? "active" : ""}`}
                onClick={() => {
                  setActiveSessionId(session.id);
                  setChannel(session.channel);
                  setTenantKey(session.tenantKey);
                  setSiteKey(session.siteKey);
                  setPrompt(session.lastPrompt);
                  setAnswers(JSON.stringify(session.answers, null, 2));
                }}
              >
                <strong>
                  <span>{session.title}</span>
                  <span className="muted">{session.channel}</span>
                </strong>
                <span className="muted">{session.status}</span>
                <span className="muted">{session.tenantKey} / {session.siteKey}</span>
              </button>
            ))}
          </div>

          <p className="section-title">Conversation builder</p>
          <div className="form-grid">
            <div className="field">
              <label>Channel</label>
              <div className="chip-row">
                {(["telegram", "bale"] as const).map((item) => (
                  <button
                    key={item}
                    type="button"
                    className={`chip ${channel === item ? "active" : ""}`}
                    onClick={() => setChannel(item)}
                  >
                    {channelPresets[item].label}
                  </button>
                ))}
              </div>
            </div>

            <div className="field-grid">
              <div className="field">
                <label htmlFor="botTenant">Tenant key</label>
                <input id="botTenant" value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="botSite">Site key</label>
                <input id="botSite" value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
              </div>
            </div>

            <div className="field">
              <label htmlFor="botPrompt">User message</label>
              <textarea id="botPrompt" value={prompt} onChange={(event) => setPrompt(event.target.value)} />
            </div>

            <div className="field">
              <label htmlFor="botAnswers">Structured answers JSON</label>
              <textarea id="botAnswers" value={answers} onChange={(event) => setAnswers(event.target.value)} />
            </div>

            <div className="hero-actions">
              <button type="button" className="btn" onClick={generateForBot} disabled={loading}>
                {loading ? "Generating bot payload..." : `Generate ${currentPreset.label} payload`}
              </button>
              <button type="button" className="ghost-btn" onClick={sendMessage} disabled={!activeSession}>
                Send message to thread
              </button>
            </div>

            <div className="field">
              <label htmlFor="botMessage">Thread message</label>
              <textarea id="botMessage" value={messageText} onChange={(event) => setMessageText(event.target.value)} />
            </div>

            {error ? (
              <div className="result-card" style={{ borderColor: "rgba(255, 127, 127, 0.35)" }}>
                <h4>Bot flow error</h4>
                <p className="muted">{error}</p>
              </div>
            ) : null}
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Adapter hints</p>
            <div className="timeline">
              <div className="timeline-step">
                <strong>Command</strong>
                <span>{currentPreset.command}</span>
              </div>
              <div className="timeline-step">
                <strong>Flow</strong>
                <span>{currentPreset.help}</span>
              </div>
              <div className="timeline-step">
                <strong>Backend</strong>
                <span>Both Telegram and Bale should call the same orchestrator endpoint.</span>
              </div>
              <div className="timeline-step">
                <strong>Active thread</strong>
                <span>{activeSession?.title ?? "none"}</span>
              </div>
            </div>
          </section>

          <section className="panel rail">
            <p className="section-title">Generated bot payload</p>
            {activeSession ? (
              <div className="editor-toolbar" style={{ marginBottom: 16 }}>
                <div className="meta">Thread id: {activeSession.id}</div>
                <Link href={`/bot/${activeSession.id}`} className="ghost-btn">
                  Open thread
                </Link>
              </div>
            ) : null}
            {response ? (
              <div className="result-grid">
                <div className="result-card">
                  <h4>{response.dsl.app.title ?? "Generated app"}</h4>
                  <p className="muted">
                    {response.dsl.delivery.botApis.length} bot endpoints are available for chat adapter wiring.
                  </p>
                </div>
                <div className="result-card">
                  <h4>Chat response</h4>
                  {response.nextQuestions.length ? (
                    <ul className="result-list">
                      {response.nextQuestions.map((question) => (
                        <li key={question}>{question}</li>
                      ))}
                    </ul>
                  ) : (
                    <p className="muted">The bot can proceed directly to the next step.</p>
                  )}
                </div>
                <div className="result-card">
                  <h4>Delivery endpoints</h4>
                  <ul className="result-list">
                    {response.dsl.delivery.botApis.map((api) => (
                      <li key={api}>{api}</li>
                    ))}
                  </ul>
                </div>
              </div>
            ) : (
              <p className="muted">Generate a bot payload to preview how the orchestrator should answer chat-based app requests.</p>
            )}
          </section>
        </aside>
      </div>

      <section style={{ padding: "24px" }}>
        <p className="section-title">Bot DSL preview</p>
        <pre className="json-view">
{JSON.stringify(
  response?.dsl ?? {
    app: {
      appKey: "bot-preview",
      title: "Bot Preview",
      type: appType,
      tenantKey,
      siteKey,
      capabilities: ["website", "shop"]
    },
    entities: [],
    routes: [],
    flows: [],
    delivery: {
      publicApis: ["/public/storefront/render?path=/"],
      botApis: ["/api/content-service/**", "/api/catalog-service/**"]
    },
    manualActions: []
  },
  null,
  2
)}
        </pre>
      </section>
    </AppShell>
  );
}
