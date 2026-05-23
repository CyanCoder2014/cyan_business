"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { AppShell } from "@/components/app-shell";
import { createDefinitionFromTemplate, listRecords, submitRecord } from "@/lib/dynamic-api";
import { prepareMediaUpload } from "@/lib/media-api";
import {
  renderStorefrontRoute,
  resolveStorefrontRoute,
  storefrontPageUrl,
  type StorefrontRenderedPage,
  type StorefrontResolvedRoute
} from "@/lib/storefront-api";
import type { DynamicEntityRecord } from "@/lib/types";

type SiteBlockType = "TEXT" | "FEATURES" | "CTA" | "FAQ";
type PublicationStatus = "DRAFT" | "PUBLISHED";
type PreviewMode = "desktop" | "mobile";
type ThemeTone = "Editorial" | "Commerce" | "Studio";
type StructuredDataBlock = { context: string; type: string; payloadJson: string };
type ManagedPageDraft = {
  slug: string;
  title: string;
  heroTitle: string;
  heroSubtitle: string;
  publicationStatus: PublicationStatus;
};

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
    category: string;
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
    accentClass: "tone-editorial",
    category: "Editorial"
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
    accentClass: "tone-commerce",
    category: "Commerce"
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
    accentClass: "tone-studio",
    category: "Studio"
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

function parseJsonRecord(value: string) {
  return value.trim() ? (JSON.parse(value) as Record<string, unknown>) : {};
}

function prettyJson(value: unknown) {
  return JSON.stringify(value, null, 2);
}

function mediaUrl(record: DynamicEntityRecord | null) {
  const storage = record?.data?.storage;
  if (storage && typeof storage === "object" && "cdnUrl" in storage) {
    return String((storage as { cdnUrl?: unknown }).cdnUrl ?? "");
  }
  return "";
}

export default function SiteBuilderPage() {
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [slug, setSlug] = useState("home");
  const [title, setTitle] = useState("Cyan Commerce");
  const [heroTitle, setHeroTitle] = useState("Launch your business app, storefront, and bot");
  const [heroSubtitle, setHeroSubtitle] = useState("One panel for AI generation, structured data, public pages, and Telegram/Bale experiences.");
  const [themeKey, setThemeKey] = useState("theme-main");
  const [templateKey, setTemplateKey] = useState("theme-template-main");
  const [templateTitle, setTemplateTitle] = useState("Commerce Editorial Blend");
  const [templateDescription, setTemplateDescription] = useState("Balanced storefront theme for operator-led launches.");
  const [brandName, setBrandName] = useState("Cyan Commerce");
  const [themeTone, setThemeTone] = useState<ThemeTone>("Commerce");
  const [previewMode, setPreviewMode] = useState<PreviewMode>("desktop");
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [registryLoading, setRegistryLoading] = useState(false);
  const [selectedBlockId, setSelectedBlockId] = useState("block-1");
  const [resolvedRoute, setResolvedRoute] = useState<StorefrontResolvedRoute | null>(null);
  const [renderedPage, setRenderedPage] = useState<StorefrontRenderedPage | null>(null);
  const [themeTemplates, setThemeTemplates] = useState<DynamicEntityRecord[]>([]);
  const [domainBindings, setDomainBindings] = useState<DynamicEntityRecord[]>([]);
  const [mediaAssets, setMediaAssets] = useState<DynamicEntityRecord[]>([]);
  const [themeVersions, setThemeVersions] = useState<DynamicEntityRecord[]>([]);
  const [routeVersions, setRouteVersions] = useState<DynamicEntityRecord[]>([]);
  const [navigationItems, setNavigationItems] = useState(themePresets.Commerce.navigation);
  const [pages, setPages] = useState<ManagedPageDraft[]>([
    {
      slug: "home",
      title: "Cyan Commerce",
      heroTitle: "Launch your business app, storefront, and bot",
      heroSubtitle: "One panel for AI generation, structured data, public pages, and Telegram/Bale experiences.",
      publicationStatus: "DRAFT"
    }
  ]);
  const [selectedMediaAssetKey, setSelectedMediaAssetKey] = useState("");
  const [assetKey, setAssetKey] = useState("hero-commerce");
  const [assetTitle, setAssetTitle] = useState("Hero storefront image");
  const [assetAltText, setAssetAltText] = useState("Storefront hero image");
  const [assetPath, setAssetPath] = useState("assets/hero-commerce/hero.jpg");
  const [canonicalUrl, setCanonicalUrl] = useState("");
  const [robots, setRobots] = useState("index,follow");
  const [twitterCard, setTwitterCard] = useState("summary_large_image");
  const [organizationJsonLd, setOrganizationJsonLd] = useState("{\n  \"@context\": \"https://schema.org\",\n  \"@type\": \"Organization\",\n  \"name\": \"Cyan Commerce\"\n}");
  const [structuredDataBlocks, setStructuredDataBlocks] = useState<StructuredDataBlock[]>([
    {
      context: "https://schema.org",
      type: "WebPage",
      payloadJson: "{\n  \"@context\": \"https://schema.org\",\n  \"@type\": \"WebPage\",\n  \"name\": \"Cyan Commerce\"\n}"
    }
  ]);
  const [domainHost, setDomainHost] = useState("www.cyan-demo.local");
  const [domainStatus, setDomainStatus] = useState("PENDING");
  const [verificationMethod, setVerificationMethod] = useState("DNS_TXT");
  const [verificationToken, setVerificationToken] = useState("cyan-demo-token");
  const [dnsTarget, setDnsTarget] = useState("cname.platform.local");
  const [domainNotes, setDomainNotes] = useState("Switch to ACTIVE after DNS validation succeeds.");
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
  const selectedMedia = mediaAssets.find((item) => item.recordKey === selectedMediaAssetKey) ?? null;
  const selectedMediaUrl = mediaUrl(selectedMedia);
  const routePath = buildPath(slug);
  const previewUrl = `/public/storefront/render?path=${routePath}`;
  const previewHtmlUrl = storefrontPageUrl(routePath);
  const bindingKey = `${domainHost.replace(/[^a-zA-Z0-9]+/g, "-").toLowerCase()}-binding`;

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
        cacheTtlSeconds: 300,
        preloadAssets: selectedMediaAssetKey ? [selectedMediaAssetKey] : [],
        hydrateTargetEntity: "true"
      },
      seo: {
        title,
        description: heroSubtitle,
        canonicalUrl: canonicalUrl || undefined,
        robots,
        ogImage: selectedMediaUrl || undefined,
        twitterCard,
        structuredDataBlocks: structuredDataBlocks.map((item) => ({
          "@context": item.context,
          "@type": item.type,
          payloadJson: item.payloadJson
        }))
      },
      indexingEnabled: "true",
      sitemapPriority: "0.8",
      publicationStatus: "PUBLISHED"
    }),
    [canonicalUrl, heroSubtitle, robots, routePath, selectedMediaAssetKey, selectedMediaUrl, slug, structuredDataBlocks, themeKey, title, twitterCard]
  );

  const themeRecord = useMemo(
    () => ({
      themeKey,
      templateKey,
      themeCategory: themePresets[themeTone].category,
      previewImage: selectedMediaUrl || undefined,
      brandName,
      tone: themeTone,
      status: "ACTIVE",
      navigation: navigationItems,
      globalSeo: {
        siteName: brandName,
        defaultTitleTemplate: `%s | ${brandName}`,
        defaultDescription: heroSubtitle,
        organizationJsonLd
      },
      blocks: blocks.map((block) => ({
        blockKey: block.id,
        componentType: block.blockType,
        props: {
          title: block.title,
          body: block.body,
          ctaLabel: block.ctaLabel,
          ctaUrl: block.ctaUrl,
          heroMediaUrl: selectedMediaUrl || undefined
        }
      }))
    }),
    [blocks, brandName, heroSubtitle, navigationItems, organizationJsonLd, selectedMediaUrl, templateKey, themeKey, themeTone]
  );

  const pageRecord = useMemo(
    () => (publicationStatus: PublicationStatus) => ({
      slug,
      title,
      heroTitle,
      heroSubtitle,
      publicationStatus,
      media: selectedMediaUrl ? [{ url: selectedMediaUrl, assetKey: selectedMediaAssetKey }] : [],
      sections: blocks.map((block, index) => ({
        order: index + 1,
        blockType: block.blockType,
        title: block.title,
        body: block.body,
        ctaLabel: block.ctaLabel,
        ctaUrl: block.ctaUrl
      }))
    }),
    [blocks, heroSubtitle, heroTitle, selectedMediaAssetKey, selectedMediaUrl, slug, title]
  );

  useEffect(() => {
    refreshRegistries();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantKey, siteKey]);

  async function ensureBuilderDefinitions() {
    await Promise.all([
      createDefinitionFromTemplate("content-service", "landing-page", "landing-page", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("storefront-service", "theme-layout", "theme-layout", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("storefront-service", "site-route", "site-route", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("storefront-service", "theme-template", "theme-template", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("storefront-service", "domain-binding", "domain-binding", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("storefront-service", "theme-layout-version", "theme-layout-version", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("storefront-service", "site-route-version", "site-route-version", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("media-service", "media-asset", "media-asset", { tenantKey, siteKey }).catch(() => null)
    ]);
  }

  async function refreshRegistries() {
    setRegistryLoading(true);
    try {
      await ensureBuilderDefinitions();
      const [templateItems, bindingItems, mediaItems, themeVersionItems, routeVersionItems] = await Promise.all([
        listRecords("storefront-service", "theme-template", { tenantKey, siteKey }).catch(() => []),
        listRecords("storefront-service", "domain-binding", { tenantKey, siteKey }).catch(() => []),
        listRecords("media-service", "media-asset", { tenantKey, siteKey }).catch(() => []),
        listRecords("storefront-service", "theme-layout-version", { tenantKey, siteKey }).catch(() => []),
        listRecords("storefront-service", "site-route-version", { tenantKey, siteKey }).catch(() => [])
      ]);
      setThemeTemplates(templateItems);
      setDomainBindings(bindingItems);
      setMediaAssets(mediaItems);
      setThemeVersions(themeVersionItems);
      setRouteVersions(routeVersionItems);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load theme, domain, or media registry");
    } finally {
      setRegistryLoading(false);
    }
  }

  async function saveRevisionSnapshots(publicationStatus: PublicationStatus) {
    const timestamp = new Date().toISOString();
    const publishedFlag = publicationStatus === "PUBLISHED" ? "true" : "false";
    await submitRecord("storefront-service", "theme-layout-version", `${themeKey}-${Date.now().toString(36)}`, {
      themeKey,
      versionKey: `${themeKey}-${timestamp}`,
      label: `${publicationStatus} ${timestamp}`,
      snapshot: themeRecord,
      published: publishedFlag
    }, { tenantKey, siteKey });
    await submitRecord("storefront-service", "site-route-version", `${routeRecord.routeKey}-${Date.now().toString(36)}`, {
      routeKey: routeRecord.routeKey,
      versionKey: `${routeRecord.routeKey}-${timestamp}`,
      label: `${publicationStatus} ${timestamp}`,
      snapshot: routeRecord,
      published: publishedFlag
    }, { tenantKey, siteKey });
  }

  function setThemePreset(tone: ThemeTone) {
    const preset = themePresets[tone];
    setThemeTone(tone);
    setBrandName(preset.brandName);
    setHeroTitle(preset.heroTitle);
    setHeroSubtitle(preset.heroSubtitle);
    setNavigationItems(preset.navigation);
  }

  function syncCurrentPage(publicationStatus: PublicationStatus = "DRAFT") {
    setPages((current) => {
      const next = current.filter((item) => item.slug !== slug);
      return [
        ...next,
        {
          slug,
          title,
          heroTitle,
          heroSubtitle,
          publicationStatus
        }
      ].sort((a, b) => a.slug.localeCompare(b.slug));
    });
  }

  function loadPage(page: ManagedPageDraft) {
    setSlug(page.slug);
    setTitle(page.title);
    setHeroTitle(page.heroTitle);
    setHeroSubtitle(page.heroSubtitle);
  }

  function addNavigationItem() {
    setNavigationItems((current) => [...current, { label: "New page", path: "/new-page" }]);
  }

  function updateNavigationItem(index: number, patch: Partial<{ label: string; path: string }>) {
    setNavigationItems((current) => current.map((item, itemIndex) => (itemIndex === index ? { ...item, ...patch } : item)));
  }

  function removeNavigationItem(index: number) {
    setNavigationItems((current) => current.filter((_, itemIndex) => itemIndex !== index));
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
      if (index < 0 || nextIndex < 0 || nextIndex >= current.length) return current;
      const next = [...current];
      const [item] = next.splice(index, 1);
      next.splice(nextIndex, 0, item);
      return next;
    });
  }

  function duplicateBlock() {
    if (!activeBlock) return;
    const duplicate = { ...activeBlock, id: `${activeBlock.blockType.toLowerCase()}-${Date.now().toString(36)}` };
    setBlocks((current) => [...current, duplicate]);
    setSelectedBlockId(duplicate.id);
  }

  function removeBlock() {
    if (!activeBlock || blocks.length === 1) return;
    const nextBlocks = blocks.filter((block) => block.id !== activeBlock.id);
    setBlocks(nextBlocks);
    setSelectedBlockId(nextBlocks[0].id);
  }

  function addStructuredDataBlock() {
    setStructuredDataBlocks((current) => [
      ...current,
      {
        context: "https://schema.org",
        type: "WebPage",
        payloadJson: "{\n  \"@context\": \"https://schema.org\",\n  \"@type\": \"WebPage\"\n}"
      }
    ]);
  }

  function updateStructuredDataBlock(index: number, patch: Partial<StructuredDataBlock>) {
    setStructuredDataBlocks((current) => current.map((item, itemIndex) => (itemIndex === index ? { ...item, ...patch } : item)));
  }

  function removeStructuredDataBlock(index: number) {
    setStructuredDataBlocks((current) => current.filter((_, itemIndex) => itemIndex !== index));
  }

  async function persistSite(publicationStatus: PublicationStatus) {
    setLoading(true);
    setStatus(null);
    try {
      await ensureBuilderDefinitions();
      syncCurrentPage(publicationStatus);
      await submitRecord("content-service", "landing-page", slug, pageRecord(publicationStatus), { tenantKey, siteKey });
      await submitRecord("storefront-service", "theme-layout", themeKey, themeRecord, { tenantKey, siteKey });
      await submitRecord("storefront-service", "site-route", routeRecord.routeKey, routeRecord, { tenantKey, siteKey });
      await saveRevisionSnapshots(publicationStatus);
      await refreshRegistries();
      setStatus(publicationStatus === "PUBLISHED" ? `Page published. Public preview route: ${previewUrl}` : `Draft saved. Theme and route records are ready for later publish: ${previewUrl}`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to persist site builder records");
    } finally {
      setLoading(false);
    }
  }

  async function publishAllPages() {
    setLoading(true);
    setStatus(null);
    try {
      await ensureBuilderDefinitions();
      const allPages = pages.some((item) => item.slug === slug)
        ? pages.map((item) => item.slug === slug ? { ...item, title, heroTitle, heroSubtitle, publicationStatus: "PUBLISHED" as const } : item)
        : [...pages, { slug, title, heroTitle, heroSubtitle, publicationStatus: "PUBLISHED" as const }];

      await submitRecord("storefront-service", "theme-layout", themeKey, themeRecord, { tenantKey, siteKey });
      await saveRevisionSnapshots("PUBLISHED");
      for (const page of allPages) {
        const path = buildPath(page.slug);
        await submitRecord("content-service", "landing-page", page.slug, {
          slug: page.slug,
          title: page.title,
          heroTitle: page.heroTitle,
          heroSubtitle: page.heroSubtitle,
          publicationStatus: "PUBLISHED",
          media: selectedMediaUrl ? [{ url: selectedMediaUrl, assetKey: selectedMediaAssetKey }] : [],
          sections: blocks.map((block, index) => ({
            order: index + 1,
            blockType: block.blockType,
            title: block.title,
            body: block.body,
            ctaLabel: block.ctaLabel,
            ctaUrl: block.ctaUrl
          }))
        }, { tenantKey, siteKey });
        await submitRecord("storefront-service", "site-route", `${page.slug}-route`, {
          ...routeRecord,
          routeKey: `${page.slug}-route`,
          path,
          entityRef: {
            service: "content-service",
            entityKey: "landing-page",
            recordKey: page.slug
          },
          seo: {
            ...routeRecord.seo,
            title: page.title,
            description: page.heroSubtitle
          }
        }, { tenantKey, siteKey });
      }
      setPages(allPages);
      await refreshRegistries();
      setStatus(`Published ${allPages.length} pages with shared theme ${themeKey}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to publish all pages");
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

  async function rollbackTheme(record: DynamicEntityRecord) {
    const snapshot = record.data?.snapshot;
    if (!snapshot || typeof snapshot !== "object") return;
    setLoading(true);
    setStatus(null);
    try {
      await submitRecord("storefront-service", "theme-layout", themeKey, snapshot as Record<string, unknown>, { tenantKey, siteKey });
      await refreshRegistries();
      setStatus(`Theme rolled back to ${record.recordKey}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to roll back theme");
    } finally {
      setLoading(false);
    }
  }

  async function rollbackRoute(record: DynamicEntityRecord) {
    const snapshot = record.data?.snapshot;
    if (!snapshot || typeof snapshot !== "object") return;
    setLoading(true);
    setStatus(null);
    try {
      await submitRecord("storefront-service", "site-route", routeRecord.routeKey, snapshot as Record<string, unknown>, { tenantKey, siteKey });
      await refreshRegistries();
      setStatus(`Route rolled back to ${record.recordKey}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to roll back route");
    } finally {
      setLoading(false);
    }
  }

  async function saveThemeTemplate() {
    setLoading(true);
    setStatus(null);
    try {
      await ensureBuilderDefinitions();
      await submitRecord("storefront-service", "theme-template", templateKey, {
        templateKey,
        title: templateTitle,
        category: themePresets[themeTone].category,
        description: templateDescription,
        previewImage: selectedMediaUrl || undefined,
        status: "ACTIVE",
        themeLayout: themeRecord,
        recommendedRoute: routeRecord
      }, { tenantKey, siteKey });
      await refreshRegistries();
      setStatus(`Theme template ${templateKey} saved to storefront-service registry.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to save theme template");
    } finally {
      setLoading(false);
    }
  }

  function applyThemeTemplate(record: DynamicEntityRecord) {
    const data = record.data ?? {};
      const themeLayout = data.themeLayout;
    if (!themeLayout || typeof themeLayout !== "object") return;
    const layout = themeLayout as Record<string, unknown>;
    setTemplateKey(String(data.templateKey ?? record.recordKey));
    setTemplateTitle(String(data.title ?? record.recordKey));
    setTemplateDescription(String(data.description ?? ""));
    setThemeKey(String(layout.themeKey ?? themeKey));
    setBrandName(String(layout.brandName ?? brandName));
    setThemeTone((String(layout.tone ?? themeTone) as ThemeTone) in themePresets ? (String(layout.tone ?? themeTone) as ThemeTone) : themeTone);
    if (Array.isArray(layout.navigation)) {
      setNavigationItems(layout.navigation as Array<{ label: string; path: string }>);
    }
    const seo = layout.globalSeo as Record<string, unknown> | undefined;
    if (seo) {
      setHeroSubtitle(String(seo.defaultDescription ?? heroSubtitle));
      setOrganizationJsonLd(String(seo.organizationJsonLd ?? organizationJsonLd));
    }
    const templateBlocks = Array.isArray(layout.blocks) ? layout.blocks : [];
    const nextBlocks = templateBlocks.map((item, index) => {
      const block = item as Record<string, unknown>;
      const props = (block.props as Record<string, unknown> | undefined) ?? {};
      return {
        id: String(block.blockKey ?? `template-block-${index}`),
        blockType: String(block.componentType ?? "TEXT") as SiteBlockType,
        title: String(props.title ?? `Block ${index + 1}`),
        body: String(props.body ?? ""),
        ctaLabel: props.ctaLabel ? String(props.ctaLabel) : undefined,
        ctaUrl: props.ctaUrl ? String(props.ctaUrl) : undefined
      };
    });
    if (nextBlocks.length) {
      setBlocks(nextBlocks);
      setSelectedBlockId(nextBlocks[0].id);
    }
    if (data.previewImage) {
      const matched = mediaAssets.find((item) => mediaUrl(item) === String(data.previewImage));
      if (matched) setSelectedMediaAssetKey(matched.recordKey);
    }
    setStatus(`Applied theme template ${record.recordKey}.`);
  }

  async function saveDomainBinding() {
    setLoading(true);
    setStatus(null);
    try {
      await ensureBuilderDefinitions();
      await submitRecord("storefront-service", "domain-binding", bindingKey, {
        bindingKey,
        hostname: domainHost,
        routeKey: routeRecord.routeKey,
        themeKey,
        targetPath: routePath,
        verificationMethod,
        verificationToken,
        dnsTarget,
        status: domainStatus,
        sslStatus: domainStatus === "ACTIVE" ? "ACTIVE" : "PENDING",
        canonicalPolicy: "PRIMARY",
        notes: domainNotes
      }, { tenantKey, siteKey });
      await refreshRegistries();
      setCanonicalUrl(`https://${domainHost}${routePath}`);
      setStatus(`Domain binding ${domainHost} saved. Canonical URL updated.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to save domain binding");
    } finally {
      setLoading(false);
    }
  }

  async function createMediaAsset() {
    setLoading(true);
    setStatus(null);
    try {
      await ensureBuilderDefinitions();
      await prepareMediaUpload({
        assetKey,
        assetType: "IMAGE",
        originalFileName: assetPath.split("/").pop() ?? "asset.jpg",
        mimeType: "image/jpeg",
        visibility: "PUBLIC",
        altText: assetAltText,
        caption: heroTitle,
        title: assetTitle,
        license: "internal-demo",
        bucket: "default-public",
        path: assetPath,
        width: 1600,
        height: 900,
        sizeBytes: 120000
      });
      setSelectedMediaAssetKey(assetKey);
      await refreshRegistries();
      setStatus(`Media asset ${assetKey} created and added to the site asset library.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to create media asset");
    } finally {
      setLoading(false);
    }
  }

  const readinessChecks = [
    { label: "Tenant/site scope", value: tenantKey && siteKey ? "Ready" : "Missing" },
    { label: "Public route", value: routePath },
    { label: "Theme templates", value: `${themeTemplates.length}` },
    { label: "Domain bindings", value: `${domainBindings.length}` },
    { label: "Media assets", value: `${mediaAssets.length}` },
    { label: "SEO blocks", value: `${structuredDataBlocks.length}` }
  ];

  return (
    <AppShell
      title="Website Builder"
      subtitle="Compose storefront pages with reusable theme templates, domain workflow, media library integration, and richer SEO/schema controls."
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
            <strong>Registry-backed builder</strong>
            <span>Theme templates, domain bindings, and media assets now live in service-owned records so they can be reused across projects.</span>
          </div>
        </aside>

        <section className="builder-canvas">
          <div className="builder-toolbar">
            <div className="segmented">
              <button type="button" className="active">Page builder</button>
              <button type="button">Theme registry</button>
            </div>
            <div className="hero-actions" style={{ marginTop: 0 }}>
              <button type="button" className="ghost-btn" onClick={() => persistSite("DRAFT")} disabled={loading}>
                {loading ? "Saving..." : "Save draft"}
              </button>
              <button type="button" className="btn" onClick={() => persistSite("PUBLISHED")} disabled={loading}>
                {loading ? "Publishing..." : "Publish live"}
              </button>
              <button type="button" className="ghost-btn" onClick={publishAllPages} disabled={loading}>
                {loading ? "Publishing..." : "Publish all pages"}
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
                <button key={mode} type="button" className={`chip ${previewMode === mode ? "active" : ""}`} onClick={() => setPreviewMode(mode)}>
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
                <label>Theme template key</label>
                <input value={templateKey} onChange={(event) => setTemplateKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Template title</label>
                <input value={templateTitle} onChange={(event) => setTemplateTitle(event.target.value)} />
              </div>
            </div>

            <div className="field">
              <label>Template description</label>
              <textarea value={templateDescription} onChange={(event) => setTemplateDescription(event.target.value)} />
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
                  <button key={tone} type="button" className={`chip ${themeTone === tone ? "active" : ""}`} onClick={() => setThemePreset(tone)}>
                    {tone}
                  </button>
                ))}
                <button type="button" className="chip" onClick={saveThemeTemplate} disabled={loading}>Save template</button>
                <button type="button" className="chip" onClick={refreshRegistries} disabled={registryLoading}>{registryLoading ? "Refreshing..." : "Refresh registries"}</button>
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
                {selectedMediaUrl ? <p className="muted">Hero media: {selectedMediaUrl}</p> : null}
                <div className="hero-actions">
                  <Link href="/projects/new" className="btn">Open AI Studio</Link>
                  <Link href="/flows" className="ghost-btn">Connect workflow</Link>
                </div>
              </div>

              <div className="site-blocks">
                {blocks.map((block) => (
                  <button key={block.id} type="button" className={`site-block-card ${selectedBlockId === block.id ? "active" : ""}`} onClick={() => setSelectedBlockId(block.id)}>
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
                <h4>Theme templates</h4>
                <div className="draft-list">
                  {themeTemplates.map((item) => (
                    <button key={item.recordKey} type="button" className="draft-item" onClick={() => applyThemeTemplate(item)}>
                      <strong>
                        <span>{String(item.data?.title ?? item.recordKey)}</span>
                        <span className="muted">{String(item.data?.category ?? "theme")}</span>
                      </strong>
                      <span className="muted">{String(item.data?.description ?? "")}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="result-card">
                <h4>Version history and rollback</h4>
                <div className="draft-list">
                  {themeVersions
                    .filter((item) => String(item.data?.themeKey ?? "") === themeKey)
                    .slice()
                    .reverse()
                    .map((item) => (
                      <div key={item.recordKey} className="draft-item">
                        <strong>
                          <span>{String(item.data?.label ?? item.recordKey)}</span>
                          <span className="muted">theme</span>
                        </strong>
                        <button type="button" className="chip" onClick={() => rollbackTheme(item)}>Rollback theme</button>
                      </div>
                    ))}
                  {routeVersions
                    .filter((item) => String(item.data?.routeKey ?? "") === routeRecord.routeKey)
                    .slice()
                    .reverse()
                    .map((item) => (
                      <div key={item.recordKey} className="draft-item">
                        <strong>
                          <span>{String(item.data?.label ?? item.recordKey)}</span>
                          <span className="muted">route</span>
                        </strong>
                        <button type="button" className="chip" onClick={() => rollbackRoute(item)}>Rollback route</button>
                      </div>
                    ))}
                </div>
              </div>

              <div className="result-card">
                <h4>Domain binding workflow</h4>
                <div className="field-grid">
                  <div className="field">
                    <label>Hostname</label>
                    <input value={domainHost} onChange={(event) => setDomainHost(event.target.value)} />
                  </div>
                  <div className="field">
                    <label>Status</label>
                    <select value={domainStatus} onChange={(event) => setDomainStatus(event.target.value)}>
                      {["PENDING", "VERIFYING", "VERIFIED", "ACTIVE", "FAILED"].map((item) => <option key={item} value={item}>{item}</option>)}
                    </select>
                  </div>
                </div>
                <div className="field-grid">
                  <div className="field">
                    <label>Verification method</label>
                    <select value={verificationMethod} onChange={(event) => setVerificationMethod(event.target.value)}>
                      {["DNS_TXT", "CNAME", "HTTP_FILE"].map((item) => <option key={item} value={item}>{item}</option>)}
                    </select>
                  </div>
                  <div className="field">
                    <label>Verification token</label>
                    <input value={verificationToken} onChange={(event) => setVerificationToken(event.target.value)} />
                  </div>
                </div>
                <div className="field">
                  <label>DNS target</label>
                  <input value={dnsTarget} onChange={(event) => setDnsTarget(event.target.value)} />
                </div>
                <div className="field">
                  <label>Notes</label>
                  <textarea value={domainNotes} onChange={(event) => setDomainNotes(event.target.value)} />
                </div>
                <div className="hero-actions">
                  <button type="button" className="btn" onClick={saveDomainBinding} disabled={loading}>Save domain binding</button>
                </div>
                <div className="draft-list">
                  {domainBindings.map((item) => (
                    <div key={item.recordKey} className="draft-item">
                      <strong>
                        <span>{String(item.data?.hostname ?? item.recordKey)}</span>
                        <span className="muted">{String(item.data?.status ?? "PENDING")}</span>
                      </strong>
                      <span className="muted">{String(item.data?.verificationMethod ?? "")} / {String(item.data?.dnsTarget ?? "")}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="result-card">
                <h4>Page registry and navigation</h4>
                <div className="hero-actions">
                  <button type="button" className="chip" onClick={() => syncCurrentPage("DRAFT")}>Save current page to registry</button>
                  <button type="button" className="chip" onClick={addNavigationItem}>Add nav item</button>
                </div>
                <div className="draft-list">
                  {pages.map((page) => (
                    <button key={page.slug} type="button" className={`draft-item ${page.slug === slug ? "active" : ""}`} onClick={() => loadPage(page)}>
                      <strong>
                        <span>{page.slug}</span>
                        <span className="muted">{page.publicationStatus}</span>
                      </strong>
                      <span className="muted">{page.title}</span>
                    </button>
                  ))}
                </div>
                <div className="draft-list">
                  {navigationItems.map((item, index) => (
                    <div key={`${item.path}-${index}`} className="draft-item">
                      <div className="field-grid">
                        <div className="field">
                          <label>Label</label>
                          <input value={item.label} onChange={(event) => updateNavigationItem(index, { label: event.target.value })} />
                        </div>
                        <div className="field">
                          <label>Path</label>
                          <input value={item.path} onChange={(event) => updateNavigationItem(index, { path: event.target.value })} />
                        </div>
                      </div>
                      <button type="button" className="chip" onClick={() => removeNavigationItem(index)}>Remove nav item</button>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="result-grid">
              <div className="result-card">
                <h4>Media picker and asset library</h4>
                <div className="field-grid">
                  <div className="field">
                    <label>Asset key</label>
                    <input value={assetKey} onChange={(event) => setAssetKey(event.target.value)} />
                  </div>
                  <div className="field">
                    <label>Asset title</label>
                    <input value={assetTitle} onChange={(event) => setAssetTitle(event.target.value)} />
                  </div>
                </div>
                <div className="field-grid">
                  <div className="field">
                    <label>Alt text</label>
                    <input value={assetAltText} onChange={(event) => setAssetAltText(event.target.value)} />
                  </div>
                  <div className="field">
                    <label>Asset path</label>
                    <input value={assetPath} onChange={(event) => setAssetPath(event.target.value)} />
                  </div>
                </div>
                <div className="hero-actions">
                  <button type="button" className="btn" onClick={createMediaAsset} disabled={loading}>Create asset</button>
                </div>
                <div className="draft-list">
                  {mediaAssets.map((item) => (
                    <button key={item.recordKey} type="button" className={`draft-item ${selectedMediaAssetKey === item.recordKey ? "active" : ""}`} onClick={() => setSelectedMediaAssetKey(item.recordKey)}>
                      <strong>
                        <span>{item.recordKey}</span>
                        <span className="muted">{String(item.data?.assetType ?? "IMAGE")}</span>
                      </strong>
                      <span className="muted">{mediaUrl(item)}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="result-card">
                <h4>SEO and schema editor</h4>
                <div className="field-grid">
                  <div className="field">
                    <label>Canonical URL</label>
                    <input value={canonicalUrl} onChange={(event) => setCanonicalUrl(event.target.value)} />
                  </div>
                  <div className="field">
                    <label>Robots</label>
                    <select value={robots} onChange={(event) => setRobots(event.target.value)}>
                      {["index,follow", "noindex,follow", "index,nofollow", "noindex,nofollow"].map((item) => <option key={item} value={item}>{item}</option>)}
                    </select>
                  </div>
                </div>
                <div className="field-grid">
                  <div className="field">
                    <label>Twitter card</label>
                    <select value={twitterCard} onChange={(event) => setTwitterCard(event.target.value)}>
                      {["summary", "summary_large_image"].map((item) => <option key={item} value={item}>{item}</option>)}
                    </select>
                  </div>
                  <div className="field">
                    <label>Selected OG image</label>
                    <input value={selectedMediaUrl} readOnly />
                  </div>
                </div>
                <div className="field">
                  <label>Organization JSON-LD</label>
                  <textarea value={organizationJsonLd} onChange={(event) => setOrganizationJsonLd(event.target.value)} />
                </div>
                <div className="hero-actions">
                  <button type="button" className="chip" onClick={addStructuredDataBlock}>Add schema block</button>
                </div>
                <div className="draft-list">
                  {structuredDataBlocks.map((item, index) => (
                    <div key={`${item.type}-${index}`} className="draft-item">
                      <div className="field-grid">
                        <div className="field">
                          <label>@context</label>
                          <input value={item.context} onChange={(event) => updateStructuredDataBlock(index, { context: event.target.value })} />
                        </div>
                        <div className="field">
                          <label>@type</label>
                          <input value={item.type} onChange={(event) => updateStructuredDataBlock(index, { type: event.target.value })} />
                        </div>
                      </div>
                      <div className="field">
                        <label>Payload JSON</label>
                        <textarea value={item.payloadJson} onChange={(event) => updateStructuredDataBlock(index, { payloadJson: event.target.value })} />
                      </div>
                      <button type="button" className="chip" onClick={() => removeStructuredDataBlock(index)}>Remove schema block</button>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="result-grid">
              <div className="result-card">
                <h4>Public page endpoint</h4>
                <p className="muted">{previewHtmlUrl}</p>
                <iframe title="Storefront page preview" src={previewHtmlUrl} className="site-preview-frame" />
              </div>
              <div className="result-card">
                <h4>Resolved storefront payload</h4>
                <pre className="json-view">{prettyJson(resolvedRoute)}</pre>
              </div>
              <div className="result-card">
                <h4>Rendered storefront payload</h4>
                <pre className="json-view">{prettyJson(renderedPage)}</pre>
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
                <select value={activeBlock.blockType} onChange={(event) => updateActiveBlock({ blockType: event.target.value as SiteBlockType })}>
                  {blockPalette.map((item) => <option key={item.type} value={item.type}>{item.label}</option>)}
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
              <button key={block.id} type="button" className={`draft-item ${selectedBlockId === block.id ? "active" : ""}`} onClick={() => setSelectedBlockId(block.id)}>
                <strong>
                  <span>{index + 1}. {block.title}</span>
                  <span className="muted">{block.blockType}</span>
                </strong>
                <span className="muted">{block.ctaLabel ? `CTA: ${block.ctaLabel}` : "Content section"}</span>
              </button>
            ))}
          </div>

          <p className="section-title" style={{ marginTop: 20 }}>Theme record</p>
          <pre className="json-view">{prettyJson(themeRecord)}</pre>
          <p className="section-title" style={{ marginTop: 20 }}>Route record</p>
          <pre className="json-view">{prettyJson(routeRecord)}</pre>
        </aside>
      </div>
    </AppShell>
  );
}
