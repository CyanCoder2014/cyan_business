"use client";

import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { renderStorefrontRoute, resolveStorefrontRoute, type StorefrontRenderedPage, type StorefrontResolvedRoute } from "@/lib/storefront-api";

export default function SiteBuilderPage() {
  const { locale } = usePanel();
  const [resolved, setResolved] = useState<StorefrontResolvedRoute | null>(null);
  const [rendered, setRendered] = useState<StorefrontRenderedPage | null>(null);

  useEffect(() => {
    Promise.all([
      resolveStorefrontRoute("/", { tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => null),
      renderStorefrontRoute("/", { tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => null)
    ]).then(([resolveResult, renderResult]) => {
      setResolved(resolveResult);
      setRendered(renderResult);
    });
  }, []);

  return (
    <PanelShell
      activeKey="site-builder"
      title="Site Builder"
      titleFa="سایت‌ساز"
      subtitle="Visually build, preview, and publish production-ready routes without dropping into raw JSON."
      subtitleFa="بدون ورود به JSON خام، مسیرهای سایت را به‌صورت بصری بسازید، پیش‌نمایش بگیرید و منتشر کنید."
    >
      <div className="site-builder-grid">
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
              <button type="button" className="primary-pill">
                {locale === "fa" ? "انتشار" : "Publish"}
              </button>
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
            </div>
          </div>

          <div className="two-column-grid" style={{ marginTop: 18 }}>
            <article className="mini-card">
              <strong>{locale === "fa" ? "وضعیت مسیر" : "Route status"}</strong>
              <span className="muted-block">{resolved?.path ?? "/"}</span>
            </article>
            <article className="mini-card">
              <strong>{locale === "fa" ? "پیش‌نمایش رندر" : "Render preview"}</strong>
              <span className="muted-block">{rendered ? "HTML ready" : locale === "fa" ? "حالت نمونه" : "Fallback mode"}</span>
            </article>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "بخش" : "Section"}</h3>
            <span className="pill">#hero-1</span>
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
        </aside>
      </div>
    </PanelShell>
  );
}
