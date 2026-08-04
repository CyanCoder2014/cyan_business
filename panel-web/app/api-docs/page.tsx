"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import {
  getApiDocsService,
  listApiDocsServices,
  type ApiDocsServiceSummary,
  type OpenApiDocument
} from "@/lib/service-api";

const httpMethods = new Set(["get", "post", "put", "patch", "delete", "head", "options", "trace"]);

type OperationRow = {
  method: string;
  path: string;
  summary: string;
  auth: string;
};

export default function ApiDocsPage() {
  const { locale } = usePanel();
  const [services, setServices] = useState<ApiDocsServiceSummary[]>([]);
  const [selected, setSelected] = useState("");
  const [document, setDocument] = useState<OpenApiDocument | null>(null);
  const [status, setStatus] = useState(locale === "fa" ? "در حال بارگیری..." : "Loading live controller APIs...");

  useEffect(() => {
    listApiDocsServices()
      .then((items) => {
        setServices(items);
        const first = items.find((item) => item.status === "AVAILABLE");
        if (first) setSelected(first.serviceKey);
        setStatus("");
      })
      .catch((error) => setStatus(error instanceof Error ? error.message : "API catalog could not be loaded."));
  }, []);

  useEffect(() => {
    if (!selected) return;
    setStatus(locale === "fa" ? "در حال بارگیری مشخصات..." : "Loading specification...");
    getApiDocsService(selected)
      .then((value) => {
        setDocument(value);
        setStatus("");
      })
      .catch((error) => {
        setDocument(null);
        setStatus(error instanceof Error ? error.message : "Specification could not be loaded.");
      });
  }, [locale, selected]);

  const operations = useMemo<OperationRow[]>(() => {
    const rows: OperationRow[] = [];
    for (const [path, pathItem] of Object.entries(document?.paths ?? {})) {
      for (const [method, operation] of Object.entries(pathItem)) {
        if (!httpMethods.has(method.toLowerCase()) || !operation || typeof operation !== "object") continue;
        const auth = operation["x-platform-auth"]
          ?? inferAuth(operation.security);
        rows.push({
          method: method.toUpperCase(),
          path,
          summary: operation.summary ?? operation.operationId ?? "",
          auth
        });
      }
    }
    return rows.sort((left, right) => left.path.localeCompare(right.path) || left.method.localeCompare(right.method));
  }, [document]);

  const authCounts = useMemo(() => operations.reduce<Record<string, number>>((counts, operation) => {
    counts[operation.auth] = (counts[operation.auth] ?? 0) + 1;
    return counts;
  }, {}), [operations]);

  async function refresh() {
    if (!selected) return;
    setStatus(locale === "fa" ? "در حال به‌روزرسانی..." : "Refreshing from controller...");
    try {
      setDocument(await getApiDocsService(selected, true));
      setStatus(locale === "fa" ? "مشخصات به‌روز شد." : "Specification refreshed.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Refresh failed.");
    }
  }

  function download() {
    if (!document || !selected) return;
    const blob = new Blob([JSON.stringify(document, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const anchor = window.document.createElement("a");
    anchor.href = url;
    anchor.download = `${selected}.openapi.json`;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  return (
    <PanelShell
      activeKey="api-docs"
      title="Live API Documentation"
      titleFa="مستندات زنده API"
      subtitle="Controller-derived OpenAPI, dynamic authentication contracts, and downloadable service specifications."
      subtitleFa="OpenAPI تولیدشده از کنترلرها، قراردادهای احراز هویت و مشخصات قابل دریافت هر سرویس."
    >
      <section className="metric-grid">
        <article className="stat-card">
          <span className="muted">{locale === "fa" ? "سرویس‌های فعال" : "Available services"}</span>
          <strong>{services.filter((item) => item.status === "AVAILABLE").length}</strong>
        </article>
        <article className="stat-card">
          <span className="muted">{locale === "fa" ? "عملیات" : "Operations"}</span>
          <strong>{operations.length}</strong>
        </article>
        <article className="stat-card">
          <span className="muted">Bearer</span>
          <strong>{authCounts.BEARER ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span className="muted">Basic</span>
          <strong>{authCounts.BASIC ?? 0}</strong>
        </article>
      </section>

      <div className="api-docs-grid">
        <section className="panel-card api-service-list">
          <div className="toolbar-row">
            <div>
              <strong>{locale === "fa" ? "سرویس‌ها" : "Services"}</strong>
              <span className="muted-block">{locale === "fa" ? "مشخصات زنده Kubernetes" : "Live Kubernetes catalog"}</span>
            </div>
          </div>
          {services.map((service) => (
            <button
              type="button"
              key={service.serviceKey}
              className={selected === service.serviceKey ? "entity-item active" : "entity-item"}
              onClick={() => service.status === "AVAILABLE" && setSelected(service.serviceKey)}
              disabled={service.status !== "AVAILABLE"}
            >
              <strong>{service.serviceKey}</strong>
              <span className="muted-block">
                {service.status === "AVAILABLE" ? `${service.pathCount} paths · v${service.version ?? "?"}` : service.error ?? "Unavailable"}
              </span>
            </button>
          ))}
        </section>

        <section className="data-table-shell">
          <div className="toolbar-row">
            <div>
              <strong>{document?.info?.title ?? selected}</strong>
              <span className="muted-block">{document?.info?.description ?? "OpenAPI 3"}</span>
            </div>
            <div className="pill-row">
              <button type="button" className="secondary-pill" onClick={() => refresh().catch(() => null)} disabled={!selected}>
                {locale === "fa" ? "به‌روزرسانی" : "Refresh"}
              </button>
              <button type="button" className="primary-pill" onClick={download} disabled={!document}>
                {locale === "fa" ? "دریافت OpenAPI" : "Download OpenAPI"}
              </button>
            </div>
          </div>
          {status ? <p className="status-pill info">{status}</p> : null}
          <div className="api-operation-list">
            {operations.map((operation) => (
              <article key={`${operation.method}:${operation.path}`} className="api-operation-row">
                <span className={`api-method api-method-${operation.method.toLowerCase()}`}>{operation.method}</span>
                <code>{operation.path}</code>
                <span>{operation.summary}</span>
                <span className={`status-pill ${operation.auth === "BASIC" ? "warning" : operation.auth === "NONE" ? "success" : "info"}`}>
                  {operation.auth}
                </span>
              </article>
            ))}
            {!status && operations.length === 0 ? (
              <p className="muted">{locale === "fa" ? "عملیاتی یافت نشد." : "No operations found."}</p>
            ) : null}
          </div>
        </section>
      </div>
    </PanelShell>
  );
}

function inferAuth(security: Array<Record<string, unknown>> | undefined) {
  if (security?.length === 0) return "NONE";
  const names = new Set((security ?? []).flatMap((requirement) => Object.keys(requirement)));
  if (names.has("basicAuth")) return "BASIC";
  return "BEARER";
}
