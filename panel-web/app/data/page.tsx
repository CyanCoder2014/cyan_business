"use client";

import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { dynamicServices, listDefinitions, listRecords, submitRecord } from "@/lib/dynamic-api";
import type { DynamicEntityDefinition, DynamicEntityRecord, DynamicServiceKey } from "@/lib/types";

export default function DataPage() {
  const [serviceKey, setServiceKey] = useState<DynamicServiceKey>("content-service");
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [definitions, setDefinitions] = useState<DynamicEntityDefinition[]>([]);
  const [entityKey, setEntityKey] = useState("");
  const [records, setRecords] = useState<DynamicEntityRecord[]>([]);
  const [recordKey, setRecordKey] = useState("starter-record");
  const [recordJson, setRecordJson] = useState(`{\n  "title": "Starter record"\n}`);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function refreshDefinitions() {
    setLoading(true);
    setStatus(null);
    try {
      const nextDefinitions = await listDefinitions(serviceKey, { tenantKey, siteKey });
      setDefinitions(nextDefinitions);
      setEntityKey((current) => current || nextDefinitions[0]?.entityKey || "");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load definitions");
    } finally {
      setLoading(false);
    }
  }

  async function refreshRecords(nextEntityKey = entityKey) {
    if (!nextEntityKey) {
      setRecords([]);
      return;
    }
    setLoading(true);
    setStatus(null);
    try {
      setRecords(await listRecords(serviceKey, nextEntityKey, { tenantKey, siteKey }));
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load records");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refreshDefinitions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceKey]);

  async function createRecord() {
    if (!entityKey || !recordKey.trim()) {
      setStatus("Select an entity and enter a record key.");
      return;
    }
    setLoading(true);
    setStatus(null);
    try {
      const parsed = recordJson.trim() ? (JSON.parse(recordJson) as Record<string, unknown>) : {};
      await submitRecord(serviceKey, entityKey, recordKey.trim(), parsed, { tenantKey, siteKey });
      await refreshRecords();
      setStatus("Record saved with strict service validation.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to save record");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Data Manager" subtitle="Manage tenant/site-scoped records after definitions are provisioned.">
      <div className="studio-grid">
        <section className="panel rail">
          <div className="editor-toolbar">
            <div>
              <p className="section-title">Entity data workbench</p>
              <div className="meta">Uses endpoint entity APIs and preserves strict dynamic validation.</div>
            </div>
            <button type="button" className="btn" onClick={() => refreshRecords()} disabled={loading || !entityKey}>
              {loading ? "Loading..." : "Load records"}
            </button>
          </div>

          <div className="form-grid">
            <div className="field">
              <label htmlFor="dataService">Service</label>
              <select id="dataService" value={serviceKey} onChange={(event) => setServiceKey(event.target.value as DynamicServiceKey)}>
                {dynamicServices.map((service) => (
                  <option key={service} value={service}>
                    {service}
                  </option>
                ))}
              </select>
            </div>

            <div className="field-grid">
              <div className="field">
                <label htmlFor="dataTenant">Tenant key</label>
                <input id="dataTenant" value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="dataSite">Site key</label>
                <input id="dataSite" value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
              </div>
            </div>

            <div className="field-grid">
              <div className="field">
                <label htmlFor="entityKey">Entity</label>
                <select
                  id="entityKey"
                  value={entityKey}
                  onChange={(event) => {
                    setEntityKey(event.target.value);
                    refreshRecords(event.target.value);
                  }}
                >
                  <option value="">Select entity</option>
                  {definitions.map((definition) => (
                    <option key={definition.entityKey} value={definition.entityKey}>
                      {definition.entityKey}
                    </option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label htmlFor="recordKey">Record key</label>
                <input id="recordKey" value={recordKey} onChange={(event) => setRecordKey(event.target.value)} />
              </div>
            </div>

            <div className="field">
              <label htmlFor="recordJson">Record data JSON</label>
              <textarea id="recordJson" value={recordJson} onChange={(event) => setRecordJson(event.target.value)} />
            </div>

            <button type="button" className="btn" onClick={createRecord} disabled={loading}>
              Save record
            </button>

            {status ? (
              <div className="result-card">
                <h4>Data status</h4>
                <p className="muted">{status}</p>
              </div>
            ) : null}
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Definitions</p>
            <div className="draft-list">
              {definitions.map((definition) => (
                <button
                  key={definition.entityKey}
                  type="button"
                  className={`draft-item ${entityKey === definition.entityKey ? "active" : ""}`}
                  onClick={() => {
                    setEntityKey(definition.entityKey);
                    refreshRecords(definition.entityKey);
                  }}
                >
                  <strong>
                    <span>{definition.entityKey}</span>
                    <span className="muted">{definition.serviceKey}</span>
                  </strong>
                  <span className="muted">{definition.tenantKey ?? "global"} / {definition.siteKey ?? "global"}</span>
                </button>
              ))}
            </div>
          </section>

          <section className="panel rail">
            <p className="section-title">Records</p>
            <div className="draft-list">
              {records.map((record) => (
                <div key={record.recordKey} className="draft-item">
                  <strong>
                    <span>{record.recordKey}</span>
                    <span className="muted">{record.entityKey ?? entityKey}</span>
                  </strong>
                  <pre className="json-view">{JSON.stringify(record.data, null, 2)}</pre>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>
    </AppShell>
  );
}
