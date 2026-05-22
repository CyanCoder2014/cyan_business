"use client";

import { useMemo, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { createDefinitionFromTemplate, submitRecord } from "@/lib/dynamic-api";

type SiteBlock = {
  id: string;
  blockType: "TEXT" | "FEATURES" | "CTA" | "FAQ";
  title: string;
  body: string;
  ctaLabel?: string;
  ctaUrl?: string;
};

const blockPalette: Array<{ type: SiteBlock["blockType"]; label: string }> = [
  { type: "TEXT", label: "Text" },
  { type: "FEATURES", label: "Features" },
  { type: "CTA", label: "CTA" },
  { type: "FAQ", label: "FAQ" }
];

export default function SiteBuilderPage() {
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [slug, setSlug] = useState("home");
  const [title, setTitle] = useState("Cyan Commerce");
  const [heroTitle, setHeroTitle] = useState("Launch your business app, storefront, and bot");
  const [heroSubtitle, setHeroSubtitle] = useState("One panel for AI generation, structured data, public pages, and Telegram/Bale experiences.");
  const [themeKey, setThemeKey] = useState("theme-main");
  const [brandName, setBrandName] = useState("Cyan Commerce");
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [blocks, setBlocks] = useState<SiteBlock[]>([
    { id: "block-1", blockType: "TEXT", title: "Why Cyan", body: "Structured dynamic services, storefront rendering, BPM workflows, and bot channels are wired behind one product." },
    { id: "block-2", blockType: "CTA", title: "Start building", body: "Generate your first project from prompt or blueprint.", ctaLabel: "Open AI Studio", ctaUrl: "/projects/new" }
  ]);

  const routeRecord = useMemo(() => ({
    routeKey: `${slug}-route`,
    path: slug === "home" ? "/" : `/${slug}`,
    routeType: "LANDING",
    entityRef: {
      service: "content-service",
      entityKey: "landing-page",
      recordKey: slug
    },
    rendering: {
      themeKey,
      templateKey: "landing-page",
      hydrateTargetEntity: "true"
    },
    seo: {
      title,
      description: heroSubtitle,
      robots: "index,follow",
      twitterCard: "summary_large_image",
      structuredDataBlocks: []
    },
    indexingEnabled: "true",
    publicationStatus: "PUBLISHED"
  }), [heroSubtitle, slug, themeKey, title]);

  const themeRecord = useMemo(() => ({
    themeKey,
    brandName,
    status: "ACTIVE",
    navigation: [
      { label: "Home", path: "/" },
      { label: "Products", path: "/products" },
      { label: "Contact", path: "/contact" }
    ],
    globalSeo: {
      siteName: brandName,
      defaultTitleTemplate: `%s | ${brandName}`,
      defaultDescription: heroSubtitle
    },
    blocks: blocks.map((block) => ({
      blockKey: block.id,
      componentType: block.blockType,
      props: {
        title: block.title,
        body: block.body,
        ctaLabel: block.ctaLabel,
        ctaUrl: block.ctaUrl
      }
    }))
  }), [blocks, brandName, heroSubtitle, themeKey]);

  const pageRecord = useMemo(() => ({
    slug,
    title,
    heroTitle,
    heroSubtitle,
    publicationStatus: "PUBLISHED",
    sections: blocks.map((block) => ({
      blockType: block.blockType,
      title: block.title,
      body: block.body,
      ctaLabel: block.ctaLabel,
      ctaUrl: block.ctaUrl
    }))
  }), [blocks, heroSubtitle, heroTitle, slug, title]);

  function addBlock(type: SiteBlock["blockType"]) {
    setBlocks((current) => [
      ...current,
      {
        id: `${type.toLowerCase()}-${Date.now().toString(36)}`,
        blockType: type,
        title: `${type} section`,
        body: "Edit this section in the builder inspector."
      }
    ]);
  }

  async function publishSite() {
    setLoading(true);
    setStatus(null);
    try {
      await createDefinitionFromTemplate("content-service", "landing-page", "landing-page", { tenantKey, siteKey });
      await createDefinitionFromTemplate("storefront-service", "theme-layout", "theme-layout", { tenantKey, siteKey });
      await createDefinitionFromTemplate("storefront-service", "site-route", "site-route", { tenantKey, siteKey });

      await submitRecord("content-service", "landing-page", slug, pageRecord, { tenantKey, siteKey });
      await submitRecord("storefront-service", "theme-layout", themeKey, themeRecord, { tenantKey, siteKey });
      await submitRecord("storefront-service", "site-route", routeRecord.routeKey, routeRecord, { tenantKey, siteKey });
      setStatus(`Website records published. Preview route: /public/storefront/render?path=${routeRecord.path}`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to publish site builder records");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Website Builder" subtitle="Compose storefront pages with reusable blocks, theme settings, and live route records.">
      <div className="builder-shell">
        <aside className="builder-palette">
          <p className="section-title">Blocks</p>
          {blockPalette.map((item) => (
            <button key={item.type} type="button" className="builder-tool" onClick={() => addBlock(item.type)}>
              <span>{item.type === "TEXT" ? "Tt" : item.type === "FEATURES" ? "Fe" : item.type === "CTA" ? "Go" : "?"}</span>
              {item.label}
            </button>
          ))}
          <div className="builder-tip">
            <strong>VvvebJs-style slice</strong>
            <span>Palette on the left, content canvas in the middle, inspector on the right, then publish to content and storefront records.</span>
          </div>
        </aside>

        <section className="builder-canvas">
          <div className="builder-toolbar">
            <div className="segmented">
              <button type="button" className="active">Page builder</button>
              <button type="button">Theme builder</button>
            </div>
            <button type="button" className="btn" onClick={publishSite} disabled={loading}>
              {loading ? "Publishing..." : "Publish site"}
            </button>
          </div>

          <div className="form-preview-card">
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
                <label>Slug</label>
                <input value={slug} onChange={(event) => setSlug(event.target.value)} />
              </div>
              <div className="field">
                <label>Theme key</label>
                <input value={themeKey} onChange={(event) => setThemeKey(event.target.value)} />
              </div>
            </div>
            <div className="field">
              <label>Page title</label>
              <input value={title} onChange={(event) => setTitle(event.target.value)} />
            </div>
            <div className="field">
              <label>Hero title</label>
              <input value={heroTitle} onChange={(event) => setHeroTitle(event.target.value)} />
            </div>
            <div className="field">
              <label>Hero subtitle</label>
              <textarea value={heroSubtitle} onChange={(event) => setHeroSubtitle(event.target.value)} />
            </div>

            <div className="site-preview">
              <div className="site-hero">
                <span className="tag">Live preview</span>
                <h3>{heroTitle}</h3>
                <p>{heroSubtitle}</p>
              </div>
              <div className="site-blocks">
                {blocks.map((block) => (
                  <article key={block.id} className="site-block-card">
                    <small>{block.blockType}</small>
                    <strong>{block.title}</strong>
                    <p>{block.body}</p>
                    {block.ctaLabel ? <span className="ghost-btn">{block.ctaLabel}</span> : null}
                  </article>
                ))}
              </div>
            </div>

            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>

        <aside className="builder-inspector">
          <p className="section-title">Theme and route</p>
          <div className="form-grid">
            <div className="field">
              <label>Brand name</label>
              <input value={brandName} onChange={(event) => setBrandName(event.target.value)} />
            </div>
          </div>
          <p className="section-title" style={{ marginTop: 20 }}>Theme record</p>
          <pre className="json-view">{JSON.stringify(themeRecord, null, 2)}</pre>
          <p className="section-title" style={{ marginTop: 20 }}>Route record</p>
          <pre className="json-view">{JSON.stringify(routeRecord, null, 2)}</pre>
        </aside>
      </div>
    </AppShell>
  );
}
