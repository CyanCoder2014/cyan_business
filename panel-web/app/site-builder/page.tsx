"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { createDefinitionFromTemplate, listRecords, submitRecord } from "@/lib/dynamic-api";
import { renderStorefrontRoute, resolveStorefrontRoute, type StorefrontRenderedPage, type StorefrontResolvedRoute } from "@/lib/storefront-api";
import type { DynamicEntityRecord } from "@/lib/types";

export default function SiteBuilderPage() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? "", siteKey: siteKey ?? undefined }), [siteKey, tenantKey]);
  const [routes, setRoutes] = useState<DynamicEntityRecord[]>([]);
  const [selectedRouteKey, setSelectedRouteKey] = useState<string>("home");
  const [routePath, setRoutePath] = useState("/");
  const [routeTitle, setRouteTitle] = useState("Home");
  const [status, setStatus] = useState<string | null>(null);
  const [resolved, setResolved] = useState<StorefrontResolvedRoute | null>(null);
  const [rendered, setRendered] = useState<StorefrontRenderedPage | null>(null);

  const refresh = useCallback(async (path: string, preferredRouteKey: string | null) => {
    if (!tenantKey) return;
    const [routeItems, resolveResult, renderResult] = await Promise.all([
      listRecords("storefront-service", "site-route", scope).catch(() => []),
      resolveStorefrontRoute(path, scope).catch(() => null),
      renderStorefrontRoute(path, scope).catch(() => null)
    ]);
    setRoutes(routeItems);
    setResolved(resolveResult);
    setRendered(renderResult);
    if (routeItems.length) {
      const selected = routeItems.find((item) => item.recordKey === preferredRouteKey) ?? routeItems[0];
      setSelectedRouteKey(selected.recordKey);
      setRoutePath(recordPath(selected));
      setRouteTitle(recordLabel(selected));
    }
  }, [scope, tenantKey]);

  useEffect(() => {
    refresh(routePath, selectedRouteKey).catch((error) => {
      setRoutes([]);
      setResolved(null);
      setRendered(null);
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "سایت‌ساز بارگیری نشد." : "Site builder could not be loaded.");
    });
    // Initial route load should not retrigger while the user edits the local route form.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [locale, queryVersion, refresh]);

  const selectedRoute = useMemo(
    () => routes.find((route) => route.recordKey === selectedRouteKey) ?? routes[0] ?? null,
    [routes, selectedRouteKey]
  );

  async function persistRoute(publicationStatus: "DRAFT" | "PUBLISHED") {
    setStatus(locale === "fa" ? "در حال ذخیره..." : "Saving...");
    const recordKey = selectedRouteKey || slugify(routeTitle || routePath || "home");
    const normalizedPath = routePath.startsWith("/") ? routePath : `/${routePath}`;
    try {
      await createDefinitionFromTemplate("storefront-service", "site-route", "site-route", scope).catch(() => null);
      await submitRecord(
        "storefront-service",
        "site-route",
        recordKey,
        {
          routeKey: recordKey,
          path: normalizedPath,
          routeType: recordString(selectedRoute, "routeType") ?? "LANDING",
          entityRef: recordEntityRef(selectedRoute) ?? {
            service: "content-service",
            entityKey: "landing-page",
            recordKey
          },
          navigation: {
            label: routeTitle,
            menuKey: "main",
            sortOrder: 1,
            visible: "true"
          },
          seo: {
            title: `${routeTitle} | Cyan`,
            description: `${routeTitle} page published from Cyan Site Builder.`,
            robots: "index,follow",
            twitterCard: "summary_large_image",
            structuredDataBlocks: []
          },
          rendering: {
            themeKey: "cyan-light",
            templateKey: "landing-v1",
            cacheTtlSeconds: 300,
            preloadAssets: [],
            hydrateTargetEntity: "false"
          },
          indexingEnabled: "true",
          sitemapPriority: normalizedPath === "/" ? "0.8" : "0.5",
          publicationStatus,
          routeLifecycle: {}
        },
        scope
      );
      setSelectedRouteKey(recordKey);
      await refresh(normalizedPath, recordKey);
      setStatus(publicationStatus === "PUBLISHED" ? (locale === "fa" ? "منتشر شد." : "Published.") : locale === "fa" ? "پیش‌نویس ذخیره شد." : "Draft saved.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ذخیره ناموفق بود." : "Save failed.");
    }
  }

  function selectRoute(route: DynamicEntityRecord) {
    setSelectedRouteKey(route.recordKey);
    setRoutePath(recordPath(route));
    setRouteTitle(recordLabel(route));
    Promise.all([
      resolveStorefrontRoute(recordPath(route), scope).catch(() => null),
      renderStorefrontRoute(recordPath(route), scope).catch(() => null)
    ]).then(([resolveResult, renderResult]) => {
      setResolved(resolveResult);
      setRendered(renderResult);
    });
  }

  return (
    <PanelShell
      activeKey="site-builder"
      title="Site Builder"
      titleFa="سایت‌ساز"
      subtitle="Visually build, preview, and publish production-ready routes without dropping into raw JSON."
      subtitleFa="بدون ورود به JSON خام، مسیرهای سایت را به‌صورت بصری بسازید، پیش‌نمایش بگیرید و منتشر کنید."
    >
      <div className="desktop-only site-builder-grid">
        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "صفحه‌ها" : "Pages"}</h3>
            <button type="button" className="secondary-pill" onClick={() => {
              setSelectedRouteKey("new-route");
              setRoutePath("/new-page");
              setRouteTitle("New Page");
            }}>
              {locale === "fa" ? "افزودن صفحه" : "Add page"}
            </button>
          </div>
          <div className="entity-list" style={{ marginTop: 16 }}>
            {routes.map((route) => (
              <button
                key={route.recordKey}
                type="button"
                className={route.recordKey === selectedRouteKey ? "entity-item active" : "entity-item"}
                onClick={() => selectRoute(route)}
                style={{ textAlign: "start" }}
              >
                <strong>{recordLabel(route)}</strong>
                <span className="muted-block">{recordPath(route)}</span>
              </button>
            ))}
            {!routes.length ? (
              <div className="entity-item">
                <strong>{locale === "fa" ? "مسیر ذخیره‌شده‌ای وجود ندارد" : "No stored routes were returned"}</strong>
                <span className="muted-block">{locale === "fa" ? "این صفحه دیگر فهرست صفحه ساختگی ندارد." : "This page no longer shows a fabricated page list."}</span>
              </div>
            ) : null}
          </div>
        </aside>

        <section className="preview-frame">
          <div className="toolbar-row" style={{ marginBottom: 16 }}>
            <div className="pill-row">
              <span className="pill">{recordValue(rendered?.theme, "templateKey") ? String(recordValue(rendered?.theme, "templateKey")) : locale === "fa" ? "قالب backend" : "Backend theme"}</span>
              <span className="pill">{locale === "fa" ? "دسکتاپ" : "Desktop"}</span>
            </div>
            <div className="pill-row">
              <button type="button" className="secondary-pill" onClick={() => refresh(routePath, selectedRouteKey).catch((error) => setStatus(error instanceof Error ? error.message : "Preview refresh failed"))}>
                {locale === "fa" ? "پیش‌نمایش" : "Preview"}
              </button>
              <button type="button" className="secondary-pill" onClick={() => persistRoute("DRAFT")}>
                {locale === "fa" ? "پیش‌نویس" : "Draft"}
              </button>
              <button type="button" className="primary-pill" onClick={() => persistRoute("PUBLISHED")}>
                {locale === "fa" ? "انتشار" : "Publish"}
              </button>
            </div>
          </div>
          {status ? <div className="status-pill info" style={{ marginBottom: 16 }}>{status}</div> : null}

          <div className="toolbar-row" style={{ marginBottom: 16, gap: 12, flexWrap: "wrap" }}>
            <label style={{ display: "grid", gap: 6, minWidth: 200 }}>
              <span className="muted-block">{locale === "fa" ? "عنوان صفحه" : "Page title"}</span>
              <input value={routeTitle} onChange={(event) => setRouteTitle(event.target.value)} />
            </label>
            <label style={{ display: "grid", gap: 6, minWidth: 200 }}>
              <span className="muted-block">{locale === "fa" ? "مسیر" : "Path"}</span>
              <input value={routePath} onChange={(event) => setRoutePath(event.target.value)} />
            </label>
          </div>

          <div className="toolbar-row" style={{ marginBottom: 16 }}>
            <div className="pill-row">
              <span className="pill">{locale === "fa" ? "مسیر" : "Route"}: {resolved?.path ?? routePath}</span>
              <span className="pill">{locale === "fa" ? "دامنه" : "Domain"}: {scope.siteKey}.cyan.app</span>
              <span className={rendered ? "status-pill success" : "status-pill warning"}>{rendered ? (locale === "fa" ? "رندر شد" : "Rendered") : locale === "fa" ? "بدون رندر" : "Not rendered"}</span>
            </div>
          </div>

          <div className="preview-hero">
            <div>
              <span className="status-pill info">{locale === "fa" ? "ساخته‌شده با storefront-service" : "Built by storefront-service"}</span>
              <h2 style={{ fontSize: "3rem", marginBottom: 12 }}>
                {selectedRoute ? recordLabel(selectedRoute) : routeTitle || (locale === "fa" ? "صفحه جدید" : "New page")}
              </h2>
              <p className="muted" style={{ lineHeight: 1.9 }}>
                {rendered?.target ? JSON.stringify(rendered.target) : locale === "fa"
                  ? "برای این مسیر هنوز payload رندرشده‌ای از backend برنگشته است."
                  : "The backend has not returned a rendered payload for this route yet."}
              </p>
              <div className="pill-row" style={{ marginTop: 16 }}>
                <span className="pill">{recordString(selectedRoute, "routeType") ?? "LANDING"}</span>
                <span className="pill">{recordString(selectedRoute, "publicationStatus") ?? "DRAFT"}</span>
                <span className="pill">{resolved?.siteKey ?? scope.siteKey}</span>
              </div>
            </div>

            <div className="preview-phone">
              <strong>{routeTitle || "Cyan"}</strong>
              <h3 style={{ fontSize: "2rem" }}>{resolved?.path ?? routePath}</h3>
              <p className="muted">{rendered?.html ? `${rendered.html.length} HTML chars` : locale === "fa" ? "هنوز HTML رندرشده‌ای موجود نیست" : "No rendered HTML yet"}</p>
              <button type="button" className="primary-pill" style={{ width: "100%", marginTop: 16 }} onClick={() => persistRoute("PUBLISHED")}>
                {locale === "fa" ? "انتشار این مسیر" : "Publish this route"}
              </button>
            </div>
          </div>

          <div className="three-column-grid" style={{ marginTop: 18 }}>
            <article className="mini-card">
              <strong>{locale === "fa" ? "مسیرهای ذخیره‌شده" : "Stored routes"}</strong>
              <span className="muted-block">{routes.length}</span>
            </article>
            <article className="mini-card">
              <strong>{locale === "fa" ? "رندر" : "Rendered HTML"}</strong>
              <span className="muted-block">{rendered?.html ? `${rendered.html.length} chars` : "—"}</span>
            </article>
            <article className="mini-card">
              <strong>{locale === "fa" ? "هدف" : "Target"}</strong>
              <span className="muted-block">{resolved?.target ? "Resolved" : "Pending"}</span>
            </article>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "جزئیات مسیر" : "Route details"}</h3>
            <span className="pill">#{selectedRouteKey}</span>
          </div>
          <div className="pill-row" style={{ marginTop: 14 }}>
            <span className="pill status-pill info">{locale === "fa" ? "محتوا" : "Content"}</span>
            <span className="pill">{locale === "fa" ? "SEO" : "SEO"}</span>
            <span className="pill">{locale === "fa" ? "رندر" : "Rendering"}</span>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "چیدمان" : "Path"}</strong>
              <span className="muted-block">{routePath}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "عنوان" : "Title"}</strong>
              <span className="muted-block">{routeTitle}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "پس‌زمینه" : "Target entity"}</strong>
              <span className="muted-block">{recordEntityRef(selectedRoute) ? JSON.stringify(recordEntityRef(selectedRoute)) : "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "وضعیت انتشار" : "Publish status"}</strong>
              <span className="muted-block">{recordString(selectedRoute, "publicationStatus") ?? "DRAFT"}</span>
            </div>
          </div>
          <div className="toolbar-row" style={{ marginTop: 18 }}>
            <button type="button" className="secondary-pill" onClick={() => persistRoute("DRAFT")}>{locale === "fa" ? "ذخیره پیش‌نویس" : "Save draft"}</button>
            <button type="button" className="primary-pill" onClick={() => persistRoute("PUBLISHED")}>{locale === "fa" ? "انتشار" : "Publish"}</button>
          </div>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <div>
            <strong style={{ display: "block", fontSize: "2rem" }}>{locale === "fa" ? "سایت‌ساز" : "Site Builder"}</strong>
            <span className="muted-block">{scope.siteKey}.cyan.app</span>
          </div>
          <button type="button" className="primary-pill" onClick={() => persistRoute("PUBLISHED")}>{locale === "fa" ? "انتشار" : "Publish"}</button>
        </div>
        <div className="mobile-card">
          <div className="toolbar-row">
            <span className={rendered ? "status-pill success" : "status-pill warning"}>{rendered ? (locale === "fa" ? "منتشرشده" : "Published") : locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
            <span className="pill">{routePath}</span>
          </div>
          <h3 style={{ fontSize: "2rem", marginBottom: 8 }}>{routeTitle}</h3>
          <p className="muted">{rendered?.html ? `${rendered.html.length} HTML chars` : locale === "fa" ? "پیش‌نمایش زنده از backend هنوز موجود نیست." : "A live backend preview is not available yet."}</p>
        </div>
        <div className="mobile-list">
          {routes.map((route) => (
            <button key={route.recordKey} type="button" className="mobile-list-item" onClick={() => selectRoute(route)}>
              <strong>{recordLabel(route)}</strong>
              <span className="muted-block">{recordPath(route)}</span>
            </button>
          ))}
        </div>
      </div>
    </PanelShell>
  );
}

function slugify(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "") || "route";
}

function recordValue(record: Record<string, unknown> | undefined | null, key: string) {
  return record?.[key];
}

function recordString(route: DynamicEntityRecord | null, key: string) {
  const value = route?.data?.[key];
  return typeof value === "string" ? value : null;
}

function recordPath(route: DynamicEntityRecord) {
  return recordString(route, "path") ?? "/";
}

function recordLabel(route: DynamicEntityRecord) {
  const navigation = route.data.navigation;
  if (navigation && typeof navigation === "object" && typeof (navigation as Record<string, unknown>).label === "string") {
    return String((navigation as Record<string, unknown>).label);
  }
  return recordString(route, "routeKey") ?? route.recordKey;
}

function recordEntityRef(route: DynamicEntityRecord | null) {
  const entityRef = route?.data?.entityRef;
  return entityRef && typeof entityRef === "object" ? entityRef : null;
}
