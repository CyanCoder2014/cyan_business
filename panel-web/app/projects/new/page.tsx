"use client";

import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { generatePlatformApp } from "@/lib/platform-api";
import { seedDrafts } from "@/lib/draft-store";
import { listProjects, saveProject } from "@/lib/project-api";
import type { GeneratePlatformAppResponse, PlatformAppType, ProjectDraft } from "@/lib/types";

const capabilityOptions = ["website", "blog", "shop", "crm", "bpm"] as const;

const typeOptions: Array<{ label: string; value: PlatformAppType }> = [
  { label: "Website", value: "WEBSITE" },
  { label: "Blog", value: "BLOG" },
  { label: "Shop", value: "SHOP" },
  { label: "CRM", value: "CRM" },
  { label: "Mixed", value: "MIXED_BUSINESS_APP" }
];

function makeDraftId(title: string) {
  return title.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "");
}

export default function NewProjectPage() {
  const [prompt, setPrompt] = useState("Build a modern ecommerce site with CRM, invoices, and a content blog.");
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-shop-a");
  const [appType, setAppType] = useState<PlatformAppType>("MIXED_BUSINESS_APP");
  const [execute, setExecute] = useState(false);
  const [selectedCapabilities, setSelectedCapabilities] = useState<string[]>(["website", "shop", "crm"]);
  const [answers, setAnswers] = useState(`{\n  "brandName": "Demo Commerce",\n  "preferredDomain": "demo.example.com"\n}`);
  const [response, setResponse] = useState<GeneratePlatformAppResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [drafts, setDrafts] = useState<ProjectDraft[]>(seedDrafts());

  const draftSummary = response
    ? [
        response.dsl.app.title ?? "Untitled app",
        response.dsl.app.type ?? "MIXED_BUSINESS_APP",
        `${response.dsl.entities.length} entities`,
        `${response.dsl.routes.length} routes`
      ]
    : [];

  useEffect(() => {
    listProjects()
      .then(setDrafts)
      .catch(() => setDrafts(seedDrafts()));
  }, []);

  async function handleGenerate() {
    setIsLoading(true);
    setError(null);
    try {
      const parsedAnswers = answers.trim() ? (JSON.parse(answers) as Record<string, unknown>) : {};
      const payload = {
        prompt,
        tenantKey,
        siteKey,
        execute,
        answers: {
          ...parsedAnswers,
          appType,
          capabilities: selectedCapabilities
        }
      };
      const generated = await generatePlatformApp(payload);
      setResponse(generated);

      const draft: ProjectDraft = {
        id: makeDraftId(generated.dsl.app.title ?? prompt),
        title: generated.dsl.app.title ?? "Generated app",
        prompt,
        tenantKey,
        siteKey,
        updatedAt: new Date().toISOString(),
        status: generated.provisioningResult ? "PROVISIONED" : "DRAFT",
        dsl: generated.dsl,
        nextQuestions: generated.nextQuestions,
        provisioningResult: generated.provisioningResult
      };

      await saveProject(draft);
      setDrafts(await listProjects().catch(() => [draft]));
    } catch (ex) {
      const message = ex instanceof Error ? ex.message : "Generation failed";
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <AppShell
      title="App Studio"
      subtitle="Draft apps, regenerate only when needed, and push the result into platform services."
    >
      <div className="studio-grid">
        <section className="panel rail">
          <div className="editor-toolbar">
            <div>
              <p className="section-title">Prompt builder</p>
              <div className="meta">Backed by `/endpoint/ai-orchestrator/generate/app`.</div>
            </div>
            <button className="btn" onClick={handleGenerate} disabled={isLoading}>
              {isLoading ? "Generating..." : "Generate app"}
            </button>
          </div>

          <div className="form-grid">
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

            <div className="field">
              <label>App type</label>
              <div className="chip-row">
                {typeOptions.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    className={`chip ${appType === option.value ? "active" : ""}`}
                    onClick={() => setAppType(option.value)}
                  >
                    {option.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="field">
              <label>Capabilities</label>
              <div className="chip-row">
                {capabilityOptions.map((capability) => (
                  <button
                    key={capability}
                    type="button"
                    className={`chip ${selectedCapabilities.includes(capability) ? "active" : ""}`}
                    onClick={() =>
                      setSelectedCapabilities((current) =>
                        current.includes(capability)
                          ? current.filter((item) => item !== capability)
                          : [...current, capability]
                      )
                    }
                  >
                    {capability}
                  </button>
                ))}
              </div>
            </div>

            <div className="field">
              <label htmlFor="prompt">Prompt</label>
              <textarea id="prompt" value={prompt} onChange={(event) => setPrompt(event.target.value)} />
            </div>

            <div className="field">
              <label htmlFor="answers">Structured answers JSON</label>
              <textarea id="answers" value={answers} onChange={(event) => setAnswers(event.target.value)} />
            </div>

            <div className="field-grid">
              <div className="field">
                <label>Execution</label>
                <button type="button" className={`chip ${execute ? "active" : ""}`} onClick={() => setExecute((value) => !value)}>
                  {execute ? "Execute after generate" : "Review only"}
                </button>
              </div>
              <div className="field">
                <label>Draft cache</label>
                <button
                  type="button"
                  className="chip"
                  onClick={() => listProjects().then(setDrafts).catch(() => setDrafts(seedDrafts()))}
                >
                  Refresh project registry
                </button>
              </div>
            </div>

            {error ? (
              <div className="result-card" style={{ borderColor: "rgba(255, 127, 127, 0.35)" }}>
                <h4>Generation error</h4>
                <p className="muted">{error}</p>
              </div>
            ) : null}
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Generated draft</p>
            {response ? (
              <div className="result-grid">
                <div className="result-card">
                  <h4>{response.dsl.app.title ?? "Untitled app"}</h4>
                  <div className="chip-row" style={{ marginTop: 10 }}>
                    <span className="tag">{response.dsl.app.type ?? "MIXED_BUSINESS_APP"}</span>
                    <span className="tag">{response.dsl.app.tenantKey ?? tenantKey}</span>
                    <span className="tag">{response.dsl.app.siteKey ?? siteKey}</span>
                  </div>
                </div>
                <div className="result-card">
                  <h4>Next questions</h4>
                  {response.nextQuestions.length ? (
                    <ul className="result-list">
                      {response.nextQuestions.map((question) => (
                        <li key={question}>{question}</li>
                      ))}
                    </ul>
                  ) : (
                    <p className="muted">No follow-up questions. Ready for provisioning.</p>
                  )}
                </div>
                <div className="result-card">
                  <h4>Provisioning summary</h4>
                  {response.provisioningResult ? (
                    <ul className="result-list">
                      <li>Status: {response.provisioningResult.status}</li>
                      <li>Definitions: {response.provisioningResult.createdDefinitions.length}</li>
                      <li>Records: {response.provisioningResult.createdRecords.length}</li>
                      <li>Flows: {response.provisioningResult.createdFlows.length}</li>
                      <li>Delivery endpoints: {response.provisioningResult.deliveryEndpoints.length}</li>
                      <li>Manual actions: {response.provisioningResult.manualActions.length}</li>
                    </ul>
                  ) : (
                    <p className="muted">Provisioning is disabled for this run or blocked by outstanding questions.</p>
                  )}
                </div>
              </div>
            ) : (
              <p className="muted">Generate an app draft to see the DSL, follow-up questions, and provisioning trace here.</p>
            )}
          </section>

          <section className="panel rail">
            <p className="section-title">Draft cache</p>
            <div className="draft-list">
              {drafts.map((draft) => (
                <div key={draft.id} className="draft-item">
                  <strong>
                    <span>{draft.title}</span>
                    <span className="muted">{draft.status}</span>
                  </strong>
                  <span className="muted">{draft.tenantKey} / {draft.siteKey}</span>
                  <span className="muted">{draft.dsl.app.type}</span>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>

      <section style={{ padding: "24px" }}>
        <p className="section-title">DSL preview</p>
        <pre className="json-view">
{JSON.stringify(
  response?.dsl ?? {
    app: {
      appKey: "preview-app",
      title: "Preview app",
      type: appType,
      tenantKey,
      siteKey,
      capabilities: selectedCapabilities
    },
    entities: [],
    routes: [],
    flows: [],
    delivery: {
      publicApis: ["/public/storefront/render?path=/"],
      botApis: ["/api/content-service/**"]
    },
    manualActions: []
  },
  null,
  2
)}
        </pre>
        {draftSummary.length ? (
          <div className="kpi">
            {draftSummary.map((item) => (
              <div key={item} className="kpi-card">
                <strong>{item}</strong>
                <span className="muted">Generated from the current prompt and answer set.</span>
              </div>
            ))}
          </div>
        ) : null}
      </section>
    </AppShell>
  );
}
