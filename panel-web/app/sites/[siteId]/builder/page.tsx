"use client";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { AsyncButton, EmptyState, ErrorState, Field, Select, Skeleton, StatusBadge, Tabs } from "@/components/ui/primitives";
import { createDefinitionFromTemplate, getDefinition, listRecords, saveDefinition, submitRecord } from "@/lib/dynamic-api";
import { renderStorefrontRoute, type StorefrontRenderedPage } from "@/lib/storefront-api";
import type { DynamicEntityRecord } from "@/lib/types";
import { SectionListEditor } from "@/components/sites/section-list-editor";
import { SectionFields } from "@/components/sites/section-fields";
import { SECTIONS_FIELD_SCHEMA, newSection, normalizeSections, reorderSections, type SiteSection } from "@/lib/site-sections";
import type { SectionType } from "@/components/nav-icons";
import type { SchemaField } from "@/components/definitions/schema-tree-editor";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";

type Draft = {
  recordKey: string; path: string; title: string; routeType: string;
  service: string; entityKey: string; targetRecordKey: string;
  themeKey: string; templateKey: string; description: string; robots: string; assets: string;
  publicationStatus: "DRAFT" | "PUBLISHED";
  sections: SiteSection[];
};
const empty: Draft = { recordKey: "", path: "", title: "", routeType: "LANDING", service: "", entityKey: "", targetRecordKey: "", themeKey: "", templateKey: "", description: "", robots: "index,follow", assets: "", publicationStatus: "DRAFT", sections: [] };

export default function Builder({ params }: { params: { siteId: string } }) {
  const siteId = decodeURIComponent(params.siteId);
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const { showToast } = useToast();
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? "", siteKey: siteId }), [tenantKey, siteId]);
  const [routes, setRoutes] = useState<DynamicEntityRecord[]>([]);
  const [themes, setThemes] = useState<DynamicEntityRecord[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [draft, setDraft] = useState<Draft>(empty);
  const [selectedSectionKey, setSelectedSectionKey] = useState<string | null>(null);
  const [tab, setTab] = useState("page");
  const [preview, setPreview] = useState<StorefrontRenderedPage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState<string | null>(null);

  const hydrate = (r: DynamicEntityRecord): Draft => {
    const d = r.data as Record<string, unknown>, ref = (d.entityRef ?? {}) as Record<string, unknown>, seo = (d.seo ?? {}) as Record<string, unknown>, rendering = (d.rendering ?? {}) as Record<string, unknown>;
    return { recordKey: r.recordKey, path: String(d.path ?? ""), title: String(seo.title ?? d.routeKey ?? r.recordKey), routeType: String(d.routeType ?? "LANDING"), service: String(ref.service ?? ""), entityKey: String(ref.entityKey ?? ""), targetRecordKey: String(ref.recordKey ?? ""), themeKey: String(rendering.themeKey ?? ""), templateKey: String(rendering.templateKey ?? ""), description: String(seo.description ?? ""), robots: String(seo.robots ?? "index,follow"), assets: Array.isArray(rendering.preloadAssets) ? rendering.preloadAssets.join("\n") : "", publicationStatus: String(d.publicationStatus) === "PUBLISHED" ? "PUBLISHED" : "DRAFT", sections: normalizeSections(d.sections) };
  };

  const load = useCallback(async () => {
    if (!tenantKey) { setLoading(false); return; }
    setLoading(true); setError(null);
    try {
      const [r, t] = await Promise.all([listRecords("storefront-service", "site-route", scope).catch(() => []), listRecords("storefront-service", "theme-layout", scope).catch(() => [])]);
      setRoutes(r); setThemes(t);
      if (selected) { const found = r.find(x => x.recordKey === selected); if (found) setDraft(hydrate(found)); }
    } catch (e) { const { title, message } = describeApiError(e, "Site pages unavailable"); setError(message); showToast({ tone: "error", title, message }); }
    finally { setLoading(false); }
  }, [scope, selected, tenantKey]);
  useEffect(() => { void load(); }, [queryVersion, tenantKey, siteId]);

  useEffect(() => {
    if (!tenantKey) return;
    let live = true;
    (async () => {
      try {
        const definition = await getDefinition("storefront-service", "site-route", scope);
        const fields = (definition.definition?.fields ?? {}) as Record<string, SchemaField>;
        if (!fields.sections) {
          const patched = { ...definition.definition, fields: { ...fields, sections: SECTIONS_FIELD_SCHEMA } };
          await saveDefinition("storefront-service", "site-route", JSON.stringify(patched), scope);
        }
      } catch {
        // No definition yet for this site: the next save creates one from the current template, which already includes sections.
      }
    })();
    return () => { live = false; };
  }, [tenantKey, siteId]);

  const choose = (r: DynamicEntityRecord) => { setSelected(r.recordKey); setDraft(hydrate(r)); setSelectedSectionKey(null); setPreview(null); };

  const patchSections = (sections: SiteSection[]) => setDraft(v => ({ ...v, sections }));
  const addSection = (type: SectionType) => {
    const section = newSection(type, draft.sections.length);
    patchSections([...draft.sections, section]);
    setSelectedSectionKey(section.sectionKey);
  };
  const updateSection = (next: SiteSection) => patchSections(draft.sections.map(s => s.sectionKey === next.sectionKey ? next : s));
  const toggleSectionVisible = (sectionKey: string) => patchSections(draft.sections.map(s => s.sectionKey === sectionKey ? { ...s, visible: s.visible === "false" ? "true" : "false" } : s));
  const removeSection = (sectionKey: string) => { patchSections(draft.sections.filter(s => s.sectionKey !== sectionKey)); if (selectedSectionKey === sectionKey) setSelectedSectionKey(null); };
  const reorderSectionsBy = (fromKey: string, toKey: string) => patchSections(reorderSections(draft.sections, fromKey, toKey));
  const selectedSection = draft.sections.find(s => s.sectionKey === selectedSectionKey) ?? null;

  const save = async (status: "DRAFT" | "PUBLISHED") => {
    if (pending) return;
    const normalized = draft.path.startsWith("/") ? draft.path : `/${draft.path}`;
    if (!/^\/[a-z0-9/-]*$/.test(normalized)) { setError(locale === "fa" ? "مسیر نامعتبر است." : "Path must contain lowercase latin letters, numbers, slashes, or hyphens."); return; }
    if (routes.some(r => r.recordKey !== draft.recordKey && String((r.data as Record<string, unknown>).path) === normalized)) { setError(locale === "fa" ? "این مسیر قبلاً استفاده شده است." : "This path is already used by another page."); return; }
    setPending(status); setError(null);
    try {
      await createDefinitionFromTemplate("storefront-service", "site-route", "site-route", scope).catch(() => null);
      await submitRecord("storefront-service", "site-route", draft.recordKey, {
        routeKey: draft.recordKey, path: normalized, routeType: draft.routeType,
        entityRef: { service: draft.service, entityKey: draft.entityKey, recordKey: draft.targetRecordKey },
        navigation: { label: draft.title, menuKey: "main", sortOrder: routes.length + 1, visible: "true" },
        seo: { title: draft.title, description: draft.description, robots: draft.robots, twitterCard: "summary_large_image", structuredDataBlocks: [] },
        rendering: { themeKey: draft.themeKey, templateKey: draft.templateKey, cacheTtlSeconds: 300, preloadAssets: draft.assets.split("\n").map(x => x.trim()).filter(Boolean), hydrateTargetEntity: "true" },
        sections: draft.sections.map((section, index) => ({ ...section, order: index })),
        indexingEnabled: "true", sitemapPriority: normalized === "/" ? "0.8" : "0.5", publicationStatus: status, routeLifecycle: {}
      }, scope);
      setSelected(draft.recordKey);
      setDraft(v => ({ ...v, path: normalized, publicationStatus: status }));
      await load();
      if (status === "PUBLISHED") setPreview(await renderStorefrontRoute(normalized, scope));
    } catch (e) { const { title, message } = describeApiError(e, "Save failed"); setError(message); showToast({ tone: "error", title, message }); }
    finally { setPending(null); }
  };
  const canSave = draft.recordKey.length > 1 && draft.path.length > 0 && draft.title.length > 0 && draft.service && draft.entityKey && draft.targetRecordKey && draft.themeKey && draft.templateKey;

  return <PanelShell activeKey="sites" title={`${siteId} builder`} titleFa={`سایت‌ساز ${siteId}`} subtitle="Build real published pages from content sections and preview the server render." subtitleFa="صفحات واقعی را با بخش‌های محتوا بسازید و رندر سرور را ببینید.">
    <div className="page-action-bar"><Link className="secondary-pill" href="/sites">{locale === "fa" ? "سایت‌ها" : "Sites"}</Link>{siteKey !== siteId ? <StatusBadge tone="warning">{locale === "fa" ? "سایت ساز با مسیر انتخاب‌شده باز شده است" : "Builder is scoped to the URL site"}</StatusBadge> : null}<button className="primary-pill" onClick={() => { setSelected(null); setDraft(empty); setSelectedSectionKey(null); setPreview(null); }}>{locale === "fa" ? "صفحه جدید" : "New page"}</button></div>
    {error ? <div className="operational-banner error" role="alert">{error}</div> : null}
    {loading ? <Skeleton height={620} /> : <div className="site-builder-v2">
      <aside className="panel-card phase9-builder-sidebar">
        <h2>{locale === "fa" ? "صفحه‌ها" : "Pages"}</h2>
        {routes.length ? routes.map(r => <button key={r.recordKey} className={selected === r.recordKey ? "entity-item active" : "entity-item"} onClick={() => choose(r)}><strong>{String(((r.data as Record<string, unknown>).seo as Record<string, unknown> | undefined)?.title ?? r.recordKey)}</strong><span>{String((r.data as Record<string, unknown>).path ?? "")}</span></button>) : <EmptyState title={locale === "fa" ? "صفحه‌ای نیست" : "No pages"} description={locale === "fa" ? "فرم خالی صفحه جدید را تکمیل کنید." : "Complete the blank new-page form."} />}
      </aside>
      <section className="phase9-builder-main">
        <Tabs active={tab} onChange={setTab} items={[{ key: "page", label: locale === "fa" ? "صفحه" : "Page" }, { key: "sections", label: locale === "fa" ? "بخش‌ها" : "Sections" }, { key: "seo", label: "SEO" }, { key: "assets", label: locale === "fa" ? "دارایی‌ها" : "Assets" }, { key: "preview", label: locale === "fa" ? "پیش‌نمایش" : "Preview" }]} />
        <div className="panel-card phase9-builder-editor">
          {tab === "page" ? <div className="phase9-form-grid">
            <Field label="Route key" value={draft.recordKey} disabled={Boolean(selected)} onChange={e => setDraft(v => ({ ...v, recordKey: e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, "-") }))} />
            <Field label={locale === "fa" ? "مسیر" : "Path"} placeholder="/" value={draft.path} onChange={e => setDraft(v => ({ ...v, path: e.target.value.toLowerCase() }))} />
            <Field label={locale === "fa" ? "عنوان" : "Title"} value={draft.title} onChange={e => setDraft(v => ({ ...v, title: e.target.value }))} />
            <Select label={locale === "fa" ? "نوع مسیر" : "Route type"} value={draft.routeType} onChange={e => setDraft(v => ({ ...v, routeType: e.target.value }))}>{["LANDING", "BLOG", "PRODUCT", "CATEGORY", "SEARCH", "CHECKOUT", "CUSTOM"].map(x => <option key={x}>{x}</option>)}</Select>
            <Field label="Content service" value={draft.service} onChange={e => setDraft(v => ({ ...v, service: e.target.value }))} />
            <Field label="Entity key" value={draft.entityKey} onChange={e => setDraft(v => ({ ...v, entityKey: e.target.value }))} />
            <Field label="Record key" value={draft.targetRecordKey} onChange={e => setDraft(v => ({ ...v, targetRecordKey: e.target.value }))} />
            <Select label={locale === "fa" ? "قالب ذخیره‌شده" : "Stored theme"} value={draft.themeKey} onChange={e => setDraft(v => ({ ...v, themeKey: e.target.value }))}><option value="">Select</option>{themes.map(t => <option key={t.recordKey} value={t.recordKey}>{String((t.data as Record<string, unknown>).brandName ?? t.recordKey)}</option>)}</Select>
            <Field label="Template key" value={draft.templateKey} onChange={e => setDraft(v => ({ ...v, templateKey: e.target.value }))} />
          </div> : null}
          {tab === "sections" ? <div className="section-tab-layout">
            <SectionListEditor sections={draft.sections} selectedKey={selectedSectionKey} locale={locale} onSelect={setSelectedSectionKey} onReorder={reorderSectionsBy} onToggleVisible={toggleSectionVisible} onRemove={removeSection} onAdd={addSection} />
            {selectedSection ? <SectionFields section={selectedSection} locale={locale} onChange={updateSection} /> : <EmptyState title={locale === "fa" ? "بخشی انتخاب نشده" : "No section selected"} description={locale === "fa" ? "یک بخش را انتخاب یا اضافه کنید تا محتوای آن را ویرایش کنید." : "Select or add a section to edit its content."} />}
          </div> : null}
          {tab === "seo" ? <div className="phase9-form">
            <Field label="SEO title" maxLength={70} value={draft.title} onChange={e => setDraft(v => ({ ...v, title: e.target.value }))} />
            <label className="ui-field"><span>Description</span><textarea maxLength={160} value={draft.description} onChange={e => setDraft(v => ({ ...v, description: e.target.value }))} /><small>{draft.description.length}/160</small></label>
            <Select label="Robots" value={draft.robots} onChange={e => setDraft(v => ({ ...v, robots: e.target.value }))}>{["index,follow", "noindex,follow", "index,nofollow", "noindex,nofollow"].map(x => <option key={x}>{x}</option>)}</Select>
          </div> : null}
          {tab === "assets" ? <label className="ui-field"><span>{locale === "fa" ? "نشانی دارایی‌های preload، هر خط یکی" : "Preload asset URLs, one per line"}</span><textarea dir="ltr" rows={8} value={draft.assets} onChange={e => setDraft(v => ({ ...v, assets: e.target.value }))} /><small>{locale === "fa" ? "فقط ارجاع‌های واقعی رسانه ذخیره می‌شوند؛ آپلود ساختگی وجود ندارد." : "Only real media references are stored; this does not pretend to upload files."}</small></label> : null}
          {tab === "preview" ? <div className="phase9-preview">{preview?.html ? <iframe title={locale === "fa" ? "پیش‌نمایش سایت" : "Site preview"} sandbox="" srcDoc={preview.html} /> : <EmptyState title={locale === "fa" ? "پیش‌نمایش آماده نیست" : "Preview not ready"} description={locale === "fa" ? "صفحه منتشرشده را از storefront-service رندر کنید." : "Render a published page from storefront-service."} action={<AsyncButton pending={pending === "preview"} disabled={draft.publicationStatus !== "PUBLISHED"} onClick={() => { setPending("preview"); renderStorefrontRoute(draft.path, scope).then(setPreview).catch(e => { const { title, message } = describeApiError(e, "Preview render failed"); setError(message); showToast({ tone: "error", title, message }); }).finally(() => setPending(null)); }}>{locale === "fa" ? "رندر پیش‌نمایش" : "Render preview"}</AsyncButton>} />}</div> : null}
        </div>
        <div className="phase9-sticky-actions">
          <span><StatusBadge tone={draft.publicationStatus === "PUBLISHED" ? "success" : "warning"}>{draft.publicationStatus}</StatusBadge></span>
          <div>
            <AsyncButton className="secondary-pill" pending={pending === "DRAFT"} disabled={!canSave || Boolean(pending)} onClick={() => save("DRAFT")}>{locale === "fa" ? "ذخیره پیش‌نویس" : "Save draft"}</AsyncButton>
            <AsyncButton pending={pending === "PUBLISHED"} disabled={!canSave || Boolean(pending)} onClick={() => confirm(locale === "fa" ? "این صفحه منتشر شود؟" : "Publish this page?") && save("PUBLISHED")}>{locale === "fa" ? "انتشار" : "Publish"}</AsyncButton>
          </div>
        </div>
      </section>
    </div>}
  </PanelShell>;
}
