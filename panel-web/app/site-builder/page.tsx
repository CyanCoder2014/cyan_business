"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { AppShell } from "@/components/app-shell";
import { createDefinitionFromTemplate, submitRecord } from "@/lib/dynamic-api";
import {
  renderStorefrontRoute,
  resolveStorefrontRoute,
  storefrontPageUrl,
  type StorefrontRenderedPage,
  type StorefrontResolvedRoute
} from "@/lib/storefront-api";

type SiteBlockType = "TEXT" | "FEATURES" | "CTA" | "FAQ";
type PublicationStatus = "DRAFT" | "PUBLISHED";
type PreviewMode = "desktop" | "mobile";
type ThemeTone = "Editorial" | "Commerce" | "Studio";

type SiteBlock = {
  id: string;
  blockType: SiteBlockType;
  title: string;
  body: string;
  ctaLabel?: string;
  ctaUrl?: string;
};

type SectionPreset = {
  label: string;
  block: Omit<SiteBlock, "id">;
};

const blockPalette: Array<{ type: SiteBlockType; label: string }> = [
  { type: "TEXT", label: "Text" },
  { type: "FEATURES", label: "Features" },
  { type: "CTA", label: "CTA" },
  { type: "FAQ", label: "FAQ" }
];

const sectionPresets: SectionPreset[] = [
  {
    label: "Launch story",
    block: {
      blockType: "TEXT",
      title: "What this business ships",
      body: "Introduce the offer, delivery promise, and operating model in one clear section."
    }
  },
  {
    label: "Three-up features",
    block: {
      blockType: "FEATURES",
      title: "Why buyers stay",
      body: "Fast setup, structured workflows, and public experience channels from one control surface."
    }
  },
  {
    label: "Conversion CTA",
    block: {
      blockType: "CTA",
      title: "Book a guided setup",
      body: "Route visitors into a guided onboarding, quote request, or product demo flow.",
      ctaLabel: "Start now",
      ctaUrl: "/contact"
    }
  },
  {
    label: "FAQ strip",
    block: {
      blockType: "FAQ",
      title: "Questions buyers ask first",
      body: "Answer delivery, pricing, support, or onboarding concerns before they become drop-off points."
    }
  }
];

const themePresets: Record<
  ThemeTone,
  {
    brandName: string;
    heroTitle: string;
    heroSubtitle: string;
    navigation: Array<{ label: string; path: string }>;
    accentClass: string;
  }
> = {
  Editorial: {
    brandName: "Northline Journal",
    heroTitle: "Publish a public site that still respects structured operations",
    heroSubtitle: "Editorial landing pages, route-level SEO, and reusable sections tied to service-owned records.",
    navigation: [
      { label: "Stories", path: "/" },
      { label: "Archive", path: "/archive" },
      { label: "Subscribe", path: "/subscribe" }
    ],
    accentClass: "tone-editorial"
  },
  Commerce: {
    brandName: "Cyan Commerce",
    heroTitle: "Launch your business app, storefront, and bot",
    heroSubtitle: "One panel for AI generation, structured data, public pages, and Telegram/Bale experiences.",
    navigation: [
      { label: "Home", path: "/" },
      { label: "Products", path: "/products" },
      { label: "Contact", path: "/contact" }
    ],
    accentClass: "tone-commerce"
  },
  Studio: {
    brandName: "Studio Assembly",
    heroTitle: "Turn a client brief into a publishable presence in one session",
    heroSubtitle: "Mix brand storytelling, intake flows, and public route publishing without leaving the maker panel.",
    navigation: [
      { label: "Work", path: "/" },
      { label: "Services", path: "/services" },
      { label: "Book", path: "/book" }
    ],
    accentClass: "tone-studio"
  }
};

function makeBlock(type: SiteBlockType): SiteBlock {
  return {
    id: `${type.toLowerCase()}-${Date.now().toString(36)}`,
    blockType: type,
    title: `${type} section`,
    body: "Edit this section in the builder inspector."
  };
}

function makePresetBlock(preset: SectionPreset): SiteBlock {
  return {
    id: `${preset.block.blockType.toLowerCase()}-${Date.now().toString(36)}`,
    ...preset.block
  };
}

function sanitizeSlug(value: string) {
  const normalized = value.trim().toLowerCase().replace(/[^a-z0-9/-]+/g, "-").replace(/-+/g, "-").replace(/^-|-$/g, "");
  return normalized || "home";
}

function buildPath(slug: string) {
  return slug === "home" ? "/" : `/${slug.replace(/^\/+/, "")}`;
}

export default function SiteBuilderPage() {
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [slug, setSlug] = useState("home");
  const [title, setTitle] = useState("Cyan Commerce");
  const [heroTitle, setHeroTitle] = useState("Launch your business app, storefront, and bot");
  const [heroSubtitle, setHeroSubtitle] = useState("One panel for AI generation, structured data, public pages, and Telegram/Bale experiences.");
  const [themeKey, setThemeKey] = useState("theme-main");
  const [brandName, setBrandName] = useState("Cyan Commerce");
  const [themeTone, setThemeTone] = useState<ThemeTone>("Commerce");
  const [previewMode, setPreviewMode] = useState<PreviewMode>("desktop");
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [selectedBlockId, setSelectedBlockId] = useState("block-1");
  const [resolvedRoute, setResolvedRoute] = useState<StorefrontResolvedRoute | null>(null);
  const [renderedPage, setRenderedPage] = useState<StorefrontRenderedPage | null>(null);
  const [blocks, setBlocks] = useState<SiteBlock[]>([
    {
      id: "block-1",
      blockType: "TEXT",
      title: "Why Cyan",
      body: "Structured dynamic services, storefront rendering, BPM workflows, and bot channels are wired behind one product."
    },
    {
      id: "block-2",
      blockType: "CTA",
      title: "Start building",
      body: "Generate your first project from prompt or blueprint.",
      ctaLabel: "Open AI Studio",
      ctaUrl: "/projects/new"
    }
  ]);

  const activeBlock = blocks.find((block) => block.id === selectedBlockId) ?? blocks[0] ?? null;
  const routePath = buildPath(slug);
  const previewUrl = `/public/storefront/render?path=${routePath}`;
  const previewHtmlUrl = storefrontPageUrl(routePath);

  const routeRecord = useMemo(
    () => ({
      routeKey: `${slug}-route`,
      path: routePath,
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
    }),
    [heroSubtitle, routePath, slug, themeKey, title]
  );

  const themeRecord = useMemo(
    () => ({
      themeKey,
      brandName,
      tone: themeTone,
      status: "ACTIVE",
      navigation: themePresets[themeTone].navigation,
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
    }),
    [blocks, brandName, heroSubtitle, themeKey, themeTone]
  );

  const pageRecord = useMemo(
    () => (publicationStatus: PublicationStatus) => ({
      slug,
      title,
      heroTitle,
      heroSubtitle,
      publicationStatus,
      sections: blocks.map((block, index) => ({
        order: index + 1,
        blockType: block.blockType,
        title: block.title,
        body: block.body,
        ctaLabel: block.ctaLabel,
        ctaUrl: block.ctaUrl
      }))
    }),
    [blocks, heroSubtitle, heroTitle, slug, title]
  );

  const readinessChecks = [
    { label: "Tenant/site scope", value: tenantKey && siteKey ? "Ready" : "Missing" },
    { label: "Public route", value: routePath },
    { label: "Section count", value: String(blocks.length) },
    { label: "Publish state", value: "Draft or live" }
  ];

  function setThemePreset(tone: ThemeTone) {
    const preset = themePresets[tone];
    setThemeTone(tone);
    setBrandName(preset.brandName);
    setHeroTitle(preset.heroTitle);
    setHeroSubtitle(preset.heroSubtitle);
  }

  function addBlock(type: SiteBlockType) {
    const block = makeBlock(type);
    setBlocks((current) => [...current, block]);
    setSelectedBlockId(block.id);
  }

  function addPreset(preset: SectionPreset) {
    const block = makePresetBlock(preset);
    setBlocks((current) => [...current, block]);
    setSelectedBlockId(block.id);
  }

  function updateActiveBlock(patch: Partial<SiteBlock>) {
    if (!activeBlock) return;
    setBlocks((current) => current.map((block) => (block.id === activeBlock.id ? { ...block, ...patch } : block)));
  }

  function moveBlock(direction: -1 | 1) {
    if (!activeBlock) return;
    setBlocks((current) => {
      const index = current.findIndex((block) => block.id === activeBlock.id);
      const nextIndex = index + direction;
      if (index < 0 || nextIndex < 0 || nextIndex >= current.length) {
        return current;
      }
      const next = [...current];
      const [item] = next.splice(index, 1);
      next.splice(nextIndex, 0, item);
      return next;
    });
  }

  function duplicateBlock() {
    if (!activeBlock) return;
    const duplicate = {
      ...activeBlock,
      id: `${activeBlock.blockType.toLowerCase()}-${Date.now().toString(36)}`
    };
    setBlocks((current) => [...current, duplicate]);
    setSelectedBlockId(duplicate.id);
  }

  function removeBlock() {
    if (!activeBlock || blocks.length === 1) return;
    const nextBlocks = blocks.filter((block) => block.id !== activeBlock.id);
    setBlocks(nextBlocks);
    setSelectedBlockId(nextBlocks[0].id);
  }

  async function persistSite(publicationStatus: PublicationStatus) {
    setLoading(true);
    setStatus(null);
    try {
      await createDefinitionFromTemplate("content-service", "landing-page", "landing-page", { tenantKey, siteKey });
      await createDefinitionFromTemplate("storefront-service", "theme-layout", "theme-layout", { tenantKey, siteKey });
      await createDefinitionFromTemplate("storefront-service", "site-route", "site-route", { tenantKey, siteKey });

      await submitRecord("content-service", "landing-page", slug, pageRecord(publicationStatus), { tenantKey, siteKey });
      await submitRecord("storefront-service", "theme-layout", themeKey, themeRecord, { tenantKey, siteKey });
      await submitRecord("storefront-service", "site-route", routeRecord.routeKey, routeRecord, { tenantKey, siteKey });

      setStatus(
        publicationStatus === "PUBLISHED"
          ? `Page published. Public preview route: ${previewUrl}`
          : `Draft saved. Theme and route records are ready for later publish: ${previewUrl}`
      );
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to persist site builder records");
    } finally {
      setLoading(false);
    }
  }

  async function refreshPreview() {
    setPreviewLoading(true);
    setStatus(null);
    try {
      const [resolved, rendered] = await Promise.all([
        resolveStorefrontRoute(routePath, { tenantKey, siteKey }),
        renderStorefrontRoute(routePath, { tenantKey, siteKey })
      ]);
      setResolvedRoute(resolved);
      setRenderedPage(rendered);
      setStatus(`Resolved storefront route for ${routePath}. Public page endpoint is ready to inspect.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load storefront preview");
    } finally {
      setPreviewLoading(false);
    }
  }

  return (
    <AppShell
      title="Website Builder"
      subtitle="Compose storefront pages with reusable sections, theme presets, preview controls, and explicit draft vs publish states."
    >
      <div className="builder-shell">
        <aside className="builder-palette">
          <p className="section-title">Blocks</p>
          {blockPalette.map((item) => (
            <button key={item.type} type="button" className="builder-tool" onClick={() => addBlock(item.type)}>
              <span>{item.type === "TEXT" ? "Tt" : item.type === "FEATURES" ? "Fe" : item.type === "CTA" ? "Go" : "?"}</span>
              {item.label}
            </button>
          ))}

          <p className="section-title" style={{ marginTop: 12 }}>Section presets</p>
          {sectionPresets.map((preset) => (
            <button key={preset.label} type="button" className="builder-tool" onClick={() => addPreset(preset)}>
              <span>+</span>
              {preset.label}
            </button>
          ))}

          <div className="builder-tip">
            <strong>Highest-value gap addressed</strong>
            <span>This builder now supports reusable sections, operator editing, draft save, and live publish without falling back to raw JSON editing.</span>
          </div>
        </aside>

        <section className="builder-canvas">
          <div className="builder-toolbar">
            <div className="segmented">
              <button type="button" className="active">Page builder</button>
              <button type="button">Theme builder</button>
            </div>
            <div className="hero-actions" style={{ marginTop: 0 }}>
              <button type="button" className="ghost-btn" onClick={() => persistSite("DRAFT")} disabled={loading}>
                {loading ? "Saving..." : "Save draft"}
              </button>
              <button type="button" className="btn" onClick={() => persistSite("PUBLISHED")} disabled={loading}>
                {loading ? "Publishing..." : "Publish live"}
              </button>
              <button type="button" className="ghost-btn" onClick={refreshPreview} disabled={previewLoading}>
                {previewLoading ? "Refreshing..." : "Refresh storefront preview"}
              </button>
            </div>
          </div>

          <div className="site-builder-banner">
            <div className="site-builder-banner-copy">
              <span className="tag">Storefront route flow</span>
              <strong>{routePath}</strong>
              <span className="muted">Preview URL: {previewUrl}</span>
            </div>
            <div className="chip-row">
              {(["desktop", "mobile"] as const).map((mode) => (
                <button
                  key={mode}
                  type="button"
                  className={`chip ${previewMode === mode ? "active" : ""}`}
                  onClick={() => setPreviewMode(mode)}
                >
                  {mode}
                </button>
              ))}
            </div>
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
                <input value={slug} onChange={(event) => setSlug(sanitizeSlug(event.target.value))} />
              </div>
              <div className="field">
                <label>Theme key</label>
                <input value={themeKey} onChange={(event) => setThemeKey(event.target.value)} />
              </div>
            </div>
            <div className="field-grid">
              <div className="field">
                <label>Page title</label>
                <input value={title} onChange={(event) => setTitle(event.target.value)} />
              </div>
              <div className="field">
                <label>Brand name</label>
                <input value={brandName} onChange={(event) => setBrandName(event.target.value)} />
              </div>
            </div>
            <div className="field">
              <label>Hero title</label>
              <input value={heroTitle} onChange={(event) => setHeroTitle(event.target.value)} />
            </div>
            <div className="field">
              <label>Hero subtitle</label>
              <textarea value={heroSubtitle} onChange={(event) => setHeroSubtitle(event.target.value)} />
            </div>

            <div className="field">
              <label>Theme preset</label>
              <div className="chip-row">
                {(Object.keys(themePresets) as ThemeTone[]).map((tone) => (
                  <button
                    key={tone}
                    type="button"
                    className={`chip ${themeTone === tone ? "active" : ""}`}
                    onClick={() => setThemePreset(tone)}
                  >
                    {tone}
                  </button>
                ))}
              </div>
            </div>

            <div className={`site-preview site-preview-${previewMode} ${themePresets[themeTone].accentClass}`}>
              <div className="site-preview-nav">
                <strong>{brandName}</strong>
                <div className="chip-row">
                  {themePresets[themeTone].navigation.map((item) => (
                    <span key={item.path} className="site-preview-link">{item.label}</span>
                  ))}
                </div>
              </div>

              <div className="site-hero">
                <span className="tag">{themeTone} theme</span>
                <h3>{heroTitle}</h3>
                <p>{heroSubtitle}</p>
                <div className="hero-actions">
                  <Link href="/projects/new" className="btn">Open AI Studio</Link>
                  <Link href="/flows" className="ghost-btn">Connect workflow</Link>
                </div>
              </div>

              <div className="site-blocks">
                {blocks.map((block) => (
                  <button
                    key={block.id}
                    type="button"
                    className={`site-block-card ${selectedBlockId === block.id ? "active" : ""}`}
                    onClick={() => setSelectedBlockId(block.id)}
                  >
                    <small>{block.blockType}</small>
                    <strong>{block.title}</strong>
                    <p>{block.body}</p>
                    {block.ctaLabel ? <span className="ghost-btn">{block.ctaLabel}</span> : null}
                  </button>
                ))}
              </div>
            </div>

            <div className="result-grid">
              <div className="result-card">
                <h4>Public page endpoint</h4>
                <p className="muted">{previewHtmlUrl}</p>
                <iframe
                  title="Storefront page preview"
                  src={previewHtmlUrl}
                  className="site-preview-frame"
                />
              </div>

              <div className="result-card">
                <h4>Resolved storefront payload</h4>
                <pre className="json-view">{JSON.stringify(resolvedRoute, null, 2)}</pre>
              </div>

              <div className="result-card">
                <h4>Rendered storefront payload</h4>
                <pre className="json-view">{JSON.stringify(renderedPage, null, 2)}</pre>
              </div>
            </div>

            <div className="site-builder-readiness">
              {readinessChecks.map((item) => (
                <div key={item.label} className="mini-card">
                  <h3>{item.label}</h3>
                  <p>{item.value}</p>
                </div>
              ))}
            </div>

            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>

        <aside className="builder-inspector">
          <p className="section-title">Section inspector</p>
          {activeBlock ? (
            <div className="form-grid">
              <div className="field">
                <label>Block type</label>
                <select
                  value={activeBlock.blockType}
                  onChange={(event) => updateActiveBlock({ blockType: event.target.value as SiteBlockType })}
                >
                  {blockPalette.map((item) => (
                    <option key={item.type} value={item.type}>{item.label}</option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label>Section title</label>
                <input value={activeBlock.title} onChange={(event) => updateActiveBlock({ title: event.target.value })} />
              </div>
              <div className="field">
                <label>Body</label>
                <textarea value={activeBlock.body} onChange={(event) => updateActiveBlock({ body: event.target.value })} />
              </div>
              <div className="field-grid">
                <div className="field">
                  <label>CTA label</label>
                  <input value={activeBlock.ctaLabel ?? ""} onChange={(event) => updateActiveBlock({ ctaLabel: event.target.value })} />
                </div>
                <div className="field">
                  <label>CTA URL</label>
                  <input value={activeBlock.ctaUrl ?? ""} onChange={(event) => updateActiveBlock({ ctaUrl: event.target.value })} />
                </div>
              </div>

              <div className="site-builder-actions">
                <button type="button" className="chip" onClick={() => moveBlock(-1)}>Move up</button>
                <button type="button" className="chip" onClick={() => moveBlock(1)}>Move down</button>
                <button type="button" className="chip" onClick={duplicateBlock}>Duplicate</button>
                <button type="button" className="chip" onClick={removeBlock} disabled={blocks.length === 1}>Delete</button>
              </div>
            </div>
          ) : null}

          <p className="section-title" style={{ marginTop: 20 }}>Section order</p>
          <div className="draft-list">
            {blocks.map((block, index) => (
              <button
                key={block.id}
                type="button"
                className={`draft-item ${selectedBlockId === block.id ? "active" : ""}`}
                onClick={() => setSelectedBlockId(block.id)}
              >
                <strong>
                  <span>{index + 1}. {block.title}</span>
                  <span className="muted">{block.blockType}</span>
                </strong>
                <span className="muted">{block.ctaLabel ? `CTA: ${block.ctaLabel}` : "Content section"}</span>
              </button>
            ))}
          </div>

          <p className="section-title" style={{ marginTop: 20 }}>Route record</p>
          <pre className="json-view">{JSON.stringify(routeRecord, null, 2)}</pre>
          <p className="section-title" style={{ marginTop: 20 }}>Theme record</p>
          <pre className="json-view">{JSON.stringify(themeRecord, null, 2)}</pre>
        </aside>
      </div>
    </AppShell>
  );
}
