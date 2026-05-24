"use client";

import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { createDefinitionFromTemplate, submitRecord } from "@/lib/dynamic-api";
import { renderStorefrontRoute, resolveStorefrontRoute, type StorefrontRenderedPage, type StorefrontResolvedRoute } from "@/lib/storefront-api";

export default function SiteBuilderPage() {
  const { locale } = usePanel();
  const [resolved, setResolved] = useState<StorefrontResolvedRoute | null>(null);
  const [rendered, setRendered] = useState<StorefrontRenderedPage | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      resolveStorefrontRoute("/", { tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => null),
      renderStorefrontRoute("/", { tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => null)
    ]).then(([resolveResult, renderResult]) => {
      setResolved(resolveResult);
      setRendered(renderResult);
    });
  }, []);

  async function persistRoute(publicationStatus: "DRAFT" | "PUBLISHED") {
    setStatus(locale === "fa" ? "در حال ذخیره..." : "Saving...");
    const scope = { tenantKey: "tenant-demo", siteKey: "site-commerce" };
    try {
      await createDefinitionFromTemplate("storefront-service", "site-route", "site-route", scope).catch(() => null);
      await submitRecord(
        "storefront-service",
        "site-route",
        "home",
        {
          routeKey: "home",
          path: "/",
          routeType: "LANDING",
          entityRef: {
            service: "content-service",
            entityKey: "landing-page",
            recordKey: "home"
          },
          navigation: { label: "Home", menuKey: "main", sortOrder: 1, visible: "true" },
          seo: {
            title: "Cyan - AI-native business platform",
            description: "Build, automate, and launch production-ready business apps with Cyan.",
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
          sitemapPriority: "0.8",
          publicationStatus,
          routeLifecycle: {}
        },
        scope
      );
      const [resolveResult, renderResult] = await Promise.all([
        resolveStorefrontRoute("/", scope).catch(() => null),
        renderStorefrontRoute("/", scope).catch(() => null)
      ]);
      setResolved(resolveResult);
      setRendered(renderResult);
      setStatus(publicationStatus === "PUBLISHED" ? (locale === "fa" ? "منتشر شد." : "Published.") : locale === "fa" ? "پیش‌نویس ذخیره شد." : "Draft saved.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ذخیره ناموفق بود." : "Save failed.");
    }
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
            <button type="button" className="secondary-pill">
              {locale === "fa" ? "افزودن صفحه" : "Add page"}
            </button>
          </div>
          <div className="entity-list" style={{ marginTop: 16 }}>
            {["Home", "About", "Shop", "Product", "Contact", "FAQ"].map((page) => (
              <div key={page} className={page === "Home" ? "entity-item active" : "entity-item"}>
                <strong>{page}</strong>
                <span className="muted-block">{page === "Product" ? "/product/:id" : `/${page.toLowerCase() === "home" ? "" : page.toLowerCase()}`}</span>
              </div>
            ))}
          </div>
          <div className="card-title-row" style={{ marginTop: 18 }}>
            <h3>{locale === "fa" ? "افزودن بخش" : "Add Section"}</h3>
          </div>
          <div className="entity-list" style={{ marginTop: 12 }}>
            {["Hero", "Features", "Product Grid", "Testimonials", "FAQ", "CTA", "Footer"].map((item) => (
              <div key={item} className="entity-item">
                <strong>{item}</strong>
              </div>
            ))}
          </div>
        </aside>

        <section className="preview-frame">
          <div className="toolbar-row" style={{ marginBottom: 16 }}>
            <div className="pill-row">
              <span className="pill">{locale === "fa" ? "قالب روشن" : "Cyan Light"}</span>
              <span className="pill">{locale === "fa" ? "دسکتاپ" : "Desktop"}</span>
            </div>
            <div className="pill-row">
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "پیش‌نمایش" : "Preview"}
              </button>
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "پیش‌نویس" : "Draft"}
              </button>
              <button type="button" className="primary-pill" onClick={() => persistRoute("PUBLISHED")}>
                {locale === "fa" ? "انتشار" : "Publish"}
              </button>
            </div>
          </div>
          {status ? <div className="status-pill info" style={{ marginBottom: 16 }}>{status}</div> : null}

          <div className="toolbar-row" style={{ marginBottom: 16 }}>
            <div className="pill-row">
              <span className="pill">{locale === "fa" ? "مسیر" : "Route"}: {resolved?.path ?? "/"}</span>
              <span className="pill">{locale === "fa" ? "دامنه" : "Domain"}: acme.cyan.app</span>
              <span className="status-pill success">{locale === "fa" ? "تاییدشده" : "Verified"}</span>
            </div>
          </div>

          <div className="preview-hero">
            <div>
              <span className="status-pill info">{locale === "fa" ? "ساخته‌شده با Cyan" : "Built on Cyan"}</span>
              <h2 style={{ fontSize: "3rem", marginBottom: 12 }}>
                {locale === "fa" ? (
                  <>
                    بسازید، خودکار کنید و با <span className="gradient-text">اعتماد</span> منتشر کنید
                  </>
                ) : (
                  <>
                    Build, automate, and launch with <span className="gradient-text">confidence</span>
                  </>
                )}
              </h2>
              <p className="muted" style={{ lineHeight: 1.9 }}>
                {locale === "fa"
                  ? "وب‌سایت‌ها، فروشگاه‌ها، بات‌ها و مسیرهای عملیاتی را از یک پلتفرم واحد و ساختاریافته منتشر کنید."
                  : "Ship websites, stores, bots, and operational experiences from one structured business platform."}
              </p>
              <div className="pill-row" style={{ marginTop: 16 }}>
                <span className="pill">{locale === "fa" ? "بدون کدنویسی" : "No code"}</span>
                <span className="pill">{locale === "fa" ? "امن و سازمانی" : "Enterprise-ready"}</span>
                <span className="pill">{locale === "fa" ? "چندزبانه" : "Multi-language"}</span>
              </div>
            </div>

            <div className="preview-phone">
              <strong>Cyan</strong>
              <h3 style={{ fontSize: "2rem" }}>{locale === "fa" ? "کسب‌وکارتان را آنلاین کنید" : "Launch your business digitally"}</h3>
              <p className="muted">{locale === "fa" ? "از وب تا ربات و PWA" : "From web to bots and PWA"}</p>
              <button type="button" className="primary-pill" style={{ width: "100%", marginTop: 16 }}>
                {locale === "fa" ? "شروع کنید" : "Get Started"}
              </button>
            </div>
          </div>

          <div className="three-column-grid" style={{ marginTop: 18 }}>
            {["Visual App Builder", "Workflow Automation", "AI Studio"].map((item) => (
              <article key={item} className="mini-card">
                <strong>{item}</strong>
                <span className="muted-block">{locale === "fa" ? "بخش فعال در صفحه" : "Visible in the current section"}</span>
              </article>
            ))}
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "بخش" : "Section"}</h3>
            <span className="pill">#hero-1</span>
          </div>
          <div className="pill-row" style={{ marginTop: 14 }}>
            <span className="pill status-pill info">{locale === "fa" ? "محتوا" : "Content"}</span>
            <span className="pill">{locale === "fa" ? "استایل" : "Style"}</span>
            <span className="pill">{locale === "fa" ? "پیشرفته" : "Advanced"}</span>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "چیدمان" : "Layout"}</strong>
              <span className="muted-block">{locale === "fa" ? "تمام‌عرض" : "Full width"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "ارتفاع" : "Height"}</strong>
              <span className="muted-block">{locale === "fa" ? "بزرگ" : "Large"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "پس‌زمینه" : "Background"}</strong>
              <span className="muted-block">#FFFFFF</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "وضعیت انتشار" : "Publish status"}</strong>
              <span className="muted-block">{locale === "fa" ? "منتشر شده" : "Published"}</span>
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
            <span className="muted-block">acme.cyan.app</span>
          </div>
          <button type="button" className="primary-pill">{locale === "fa" ? "انتشار" : "Publish"}</button>
        </div>
        <div className="mobile-card">
          <div className="toolbar-row">
            <span className="status-pill success">{locale === "fa" ? "منتشرشده" : "Published"}</span>
            <span className="pill">/</span>
          </div>
          <h3 style={{ fontSize: "2rem", marginBottom: 8 }}>{locale === "fa" ? "بسازید، خودکار کنید و منتشر کنید" : "Build, automate, and launch with confidence"}</h3>
          <p className="muted">{locale === "fa" ? "پیش‌نمایش زنده از صفحه خانه" : "Live preview of the homepage section."}</p>
        </div>
        <div className="mobile-list">
          {["Home", "About", "Shop", "Product", "Contact"].map((page) => (
            <div key={page} className="mobile-list-item">
              <strong>{page}</strong>
              <span className="muted-block">{page === "Home" ? "/" : `/${page.toLowerCase()}`}</span>
            </div>
          ))}
        </div>
      </div>
    </PanelShell>
  );
}
