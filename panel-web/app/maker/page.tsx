"use client";

import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import {
  createDefinitionFromTemplate,
  dynamicServices,
  listDefinitions,
  listTemplates
} from "@/lib/dynamic-api";
import type { DynamicEntityDefinition, DynamicEntityTemplate, DynamicServiceKey } from "@/lib/types";

export default function MakerPage() {
  const [serviceKey, setServiceKey] = useState<DynamicServiceKey>("content-service");
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [templates, setTemplates] = useState<DynamicEntityTemplate[]>([]);
  const [definitions, setDefinitions] = useState<DynamicEntityDefinition[]>([]);
  const [selectedTemplate, setSelectedTemplate] = useState("");
  const [entityKey, setEntityKey] = useState("");
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function refresh() {
    setLoading(true);
    setStatus(null);
    try {
      const [nextTemplates, nextDefinitions] = await Promise.all([
        listTemplates(serviceKey),
        listDefinitions(serviceKey, { tenantKey, siteKey })
      ]);
      setTemplates(nextTemplates);
      setDefinitions(nextDefinitions);
      setSelectedTemplate((current) => current || nextTemplates[0]?.templateKey || "");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load maker data");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceKey]);

  async function createDefinition() {
    if (!selectedTemplate || !entityKey.trim()) {
      setStatus("Select a template and enter an entity key.");
      return;
    }
    setLoading(true);
    setStatus(null);
    try {
      await createDefinitionFromTemplate(serviceKey, selectedTemplate, entityKey.trim(), { tenantKey, siteKey });
      setEntityKey("");
      await refresh();
      setStatus("Definition created from service-owned template.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to create definition");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell
      title="Maker Panel"
      subtitle="Mobile-first workspace for manually or AI-assisted editing of app structure."
    >
      <div className="studio-grid">
        <section className="panel rail">
          <div className="editor-toolbar">
            <div>
              <p className="section-title">Definition maker</p>
              <div className="meta">Creates definitions from existing service-owned templates.</div>
            </div>
            <button type="button" className="btn" onClick={refresh} disabled={loading}>
              {loading ? "Loading..." : "Refresh"}
            </button>
          </div>

          <div className="form-grid">
            <div className="field">
              <label htmlFor="serviceKey">Service</label>
              <select id="serviceKey" value={serviceKey} onChange={(event) => setServiceKey(event.target.value as DynamicServiceKey)}>
                {dynamicServices.map((service) => (
                  <option key={service} value={service}>
                    {service}
                  </option>
                ))}
              </select>
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
                <label htmlFor="templateKey">Template</label>
                <select id="templateKey" value={selectedTemplate} onChange={(event) => setSelectedTemplate(event.target.value)}>
                  {templates.map((template) => (
                    <option key={template.templateKey} value={template.templateKey}>
                      {template.templateKey}
                    </option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label htmlFor="entityKey">New entity key</label>
                <input id="entityKey" value={entityKey} onChange={(event) => setEntityKey(event.target.value)} placeholder="landing-page" />
              </div>
            </div>

            <button type="button" className="btn" onClick={createDefinition} disabled={loading}>
              Create definition
            </button>

            {status ? (
              <div className="result-card">
                <h4>Maker status</h4>
                <p className="muted">{status}</p>
              </div>
            ) : null}
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Available templates</p>
            <div className="draft-list">
              {templates.map((template) => (
                <button
                  key={template.templateKey}
                  type="button"
                  className={`draft-item ${selectedTemplate === template.templateKey ? "active" : ""}`}
                  onClick={() => {
                    setSelectedTemplate(template.templateKey);
                    setEntityKey(template.templateKey);
                  }}
                >
                  <strong>
                    <span>{template.title ?? template.templateKey}</span>
                    <span className="muted">{template.entityType ?? "template"}</span>
                  </strong>
                  <span className="muted">{template.description ?? template.templateKey}</span>
                </button>
              ))}
            </div>
          </section>

          <section className="panel rail">
            <p className="section-title">Current definitions</p>
            <div className="draft-list">
              {definitions.map((definition) => (
                <div key={definition.entityKey} className="draft-item">
                  <strong>
                    <span>{definition.entityKey}</span>
                    <span className="muted">{definition.serviceKey}</span>
                  </strong>
                  <span className="muted">{definition.tenantKey ?? "global"} / {definition.siteKey ?? "global"}</span>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>
    </AppShell>
  );
}
