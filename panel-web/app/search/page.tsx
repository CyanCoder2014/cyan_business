"use client";

import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { createDefinitionFromTemplate, listRecords, submitRecord } from "@/lib/dynamic-api";
import { searchIndex, suggestIndex, syncSearchIndex } from "@/lib/service-api";
import type { DynamicEntityRecord, SearchQueryResponse, SearchSuggestionResponse } from "@/lib/types";

export default function SearchPage() {
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [indexKey, setIndexKey] = useState("content-index");
  const [query, setQuery] = useState("cyan");
  const [sourceServiceKey, setSourceServiceKey] = useState("content-service");
  const [sourceEntityKey, setSourceEntityKey] = useState("landing-page");
  const [definitions, setDefinitions] = useState<DynamicEntityRecord[]>([]);
  const [results, setResults] = useState<SearchQueryResponse | null>(null);
  const [suggestions, setSuggestions] = useState<SearchSuggestionResponse | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function refreshDefinitions() {
    await createDefinitionFromTemplate("search-index-service", "index-definition", "index-definition", { tenantKey, siteKey }).catch(() => null);
    await createDefinitionFromTemplate("search-index-service", "search-document", "search-document", { tenantKey, siteKey }).catch(() => null);
    const items = await listRecords("search-index-service", "index-definition", { tenantKey, siteKey }).catch(() => []);
    setDefinitions(items);
  }

  useEffect(() => {
    refreshDefinitions().catch((error) => setStatus(error instanceof Error ? error.message : "Failed to load search registry"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantKey, siteKey]);

  async function saveIndexDefinition() {
    setLoading(true);
    setStatus(null);
    try {
      await submitRecord("search-index-service", "index-definition", indexKey, {
        indexKey,
        source: {
          serviceKey: sourceServiceKey,
          entityKey: sourceEntityKey,
          entityType: "LANDING"
        },
        engine: "MONGO_PROJECTION",
        searchableFields: [
          { fieldPath: "title", analyzer: "plain", boost: 2 },
          { fieldPath: "summary", analyzer: "plain", boost: 1 }
        ],
        filterableFields: [{ fieldPath: "entityType", filterType: "TERM", label: "Entity type" }],
        sortableFields: [{ fieldPath: "title", sortType: "TEXT" }],
        suggestFields: [{ fieldPath: "title", weight: 10 }],
        status: "ACTIVE"
      }, { tenantKey, siteKey });
      await refreshDefinitions();
      setStatus(`Search definition ${indexKey} saved.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to save search definition");
    } finally {
      setLoading(false);
    }
  }

  async function runSync() {
    setLoading(true);
    setStatus(null);
    try {
      await syncSearchIndex(sourceServiceKey, sourceEntityKey);
      setStatus(`Search sync started for ${sourceServiceKey}/${sourceEntityKey}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to sync search index");
    } finally {
      setLoading(false);
    }
  }

  async function runSearch() {
    setLoading(true);
    setStatus(null);
    try {
      const [searchResponse, suggestResponse] = await Promise.all([
        searchIndex({ q: query, tenantKey, siteKey }),
        suggestIndex({ q: query, tenantKey, siteKey })
      ]);
      setResults(searchResponse);
      setSuggestions(suggestResponse);
      setStatus(`Search returned ${searchResponse.total ?? 0} items.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to query search index");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Search Builder" subtitle="Manage index metadata, trigger syncs, and verify public search behavior from the panel.">
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
                <label>Index key</label>
                <input value={indexKey} onChange={(event) => setIndexKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Query</label>
                <input value={query} onChange={(event) => setQuery(event.target.value)} />
              </div>
            </div>
            <div className="field-grid">
              <div className="field">
                <label>Source service</label>
                <input value={sourceServiceKey} onChange={(event) => setSourceServiceKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Source entity</label>
                <input value={sourceEntityKey} onChange={(event) => setSourceEntityKey(event.target.value)} />
              </div>
            </div>
            <div className="hero-actions">
              <button type="button" className="btn" onClick={saveIndexDefinition} disabled={loading}>Save definition</button>
              <button type="button" className="ghost-btn" onClick={runSync} disabled={loading}>Sync source</button>
              <button type="button" className="ghost-btn" onClick={runSearch} disabled={loading}>Search</button>
            </div>
            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>
        <aside className="sidebar">
          <section className="panel rail">
            <p className="section-title">Index definitions</p>
            <div className="draft-list">
              {definitions.map((item) => (
                <button key={item.recordKey} type="button" className="draft-item" onClick={() => setIndexKey(item.recordKey)}>
                  <strong><span>{item.recordKey}</span><span className="muted">{String(item.data?.sourceServiceKey ?? "")}</span></strong>
                  <span className="muted">{String(item.data?.sourceEntityKey ?? "")}</span>
                </button>
              ))}
            </div>
          </section>
          <section className="panel rail">
            <p className="section-title">Suggestions</p>
            <pre className="json-view">{JSON.stringify(suggestions, null, 2)}</pre>
          </section>
          <section className="panel rail">
            <p className="section-title">Search results</p>
            <pre className="json-view">{JSON.stringify(results, null, 2)}</pre>
          </section>
        </aside>
      </div>
    </AppShell>
  );
}
