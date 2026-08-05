"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { listBlueprints, listClientDrafts } from "@/lib/platform-api";
import type { AppBlueprint, ClientAppDraft } from "@/lib/types";

export default function ProjectsPage() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const [blueprints, setBlueprints] = useState<AppBlueprint[]>([]);
  const [drafts, setDrafts] = useState<ClientAppDraft[]>([]);
  const [query, setQuery] = useState("");
  const [kind, setKind] = useState("all");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!tenantKey) { setLoading(false); return; }
    setLoading(true);
    setError(null);
    const [blueprintResult, draftResult] = await Promise.allSettled([
      listBlueprints(),
      listClientDrafts({ tenantKey, siteKey: siteKey || undefined }),
    ]);
    if (blueprintResult.status === "fulfilled") setBlueprints(blueprintResult.value.filter((item) => item.active));
    if (draftResult.status === "fulfilled") setDrafts(draftResult.value);
    const failures = [blueprintResult, draftResult].filter((result) => result.status === "rejected");
    if (failures.length) setError(locale === "fa" ? "بخشی از داده‌های پروژه در دسترس نیست." : "Some project data is unavailable.");
    setLoading(false);
  }, [locale, siteKey, tenantKey]);

  useEffect(() => { void load(); }, [load, queryVersion]);
  const appTypes = useMemo(() => Array.from(new Set(blueprints.map((item) => item.appType))).sort(), [blueprints]);
  const match = (title: string, appType: string) => (kind === "all" || appType === kind) && title.toLocaleLowerCase().includes(query.toLocaleLowerCase());
  const visibleDrafts = drafts.filter((draft) => match(draft.title, draft.appType));
  const visibleBlueprints = blueprints.filter((blueprint) => match(blueprint.title, blueprint.appType));

  return <PanelShell activeKey="blueprints" title="Projects" titleFa="پروژه‌ها" subtitle="Open a persisted project or start from an available blueprint." subtitleFa="یک پروژه ذخیره‌شده را باز کنید یا از قالب‌های موجود شروع کنید.">
    <section className="panel-card project-catalog-toolbar">
      <label><span>{locale === "fa" ? "جستجو" : "Search"}</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={locale === "fa" ? "نام پروژه یا قالب" : "Project or blueprint name"} /></label>
      <label><span>{locale === "fa" ? "نوع برنامه" : "App type"}</span><select value={kind} onChange={(event) => setKind(event.target.value)}><option value="all">{locale === "fa" ? "همه" : "All"}</option>{appTypes.map((type) => <option key={type} value={type}>{type}</option>)}</select></label>
      <Link className="primary-pill" href="/ai">{locale === "fa" ? "پروژه جدید" : "New project"}</Link>
    </section>
    {error ? <div className="operational-banner error" role="alert">{error}<button onClick={load}>{locale === "fa" ? "تلاش دوباره" : "Retry"}</button></div> : null}
    {loading ? <div className="dashboard-grid"><Skeleton height={220} /><Skeleton height={220} /></div> : <>
      <section className="panel-card">
        <div className="card-title-row"><div><p className="page-kicker">{locale === "fa" ? "پایدار" : "Persisted"}</p><h2>{locale === "fa" ? "پروژه‌های شما" : "Your projects"}</h2></div><span className="muted">{visibleDrafts.length}</span></div>
        {visibleDrafts.length ? <div className="project-card-grid">{visibleDrafts.map((draft) => <Link className="project-card" href={`/projects/${encodeURIComponent(draft.draftId)}`} key={draft.draftId}><div><strong>{draft.title}</strong><StatusBadge tone={draft.status === "PROVISIONED" ? "success" : draft.status === "FAILED" ? "danger" : "info"}>{draft.status}</StatusBadge></div><p>{draft.latestIntent || (locale === "fa" ? "بدون توضیح" : "No description")}</p><dl><div><dt>{locale === "fa" ? "نوع" : "Type"}</dt><dd>{draft.appType}</dd></div><div><dt>{locale === "fa" ? "نسخه" : "Revision"}</dt><dd>{draft.revision ?? "—"}</dd></div><div><dt>{locale === "fa" ? "به‌روزرسانی" : "Updated"}</dt><dd>{draft.updatedAt ? new Date(draft.updatedAt).toLocaleString(locale) : "—"}</dd></div></dl></Link>)}</div> : <EmptyState title={locale === "fa" ? "پروژه‌ای یافت نشد" : "No projects found"} description={locale === "fa" ? "فیلترها را تغییر دهید یا پروژه‌ای در AI Studio بسازید." : "Change the filters or create a project in AI Studio."} />}
      </section>
      <section className="panel-card">
        <div className="card-title-row"><div><p className="page-kicker">{locale === "fa" ? "شروع سریع" : "Start quickly"}</p><h2>{locale === "fa" ? "قالب‌های فعال" : "Active blueprints"}</h2></div><span className="muted">{visibleBlueprints.length}</span></div>
        {visibleBlueprints.length ? <div className="blueprint-grid">{visibleBlueprints.map((blueprint) => <article className="blueprint-card" key={blueprint.blueprintKey}><div className="blueprint-preview" aria-hidden><div className="blueprint-preview-card" /><div className="blueprint-preview-card small" /></div><div><strong>{blueprint.title}</strong><StatusBadge tone="success">{blueprint.appType}</StatusBadge></div><p>{blueprint.description}</p><div className="pill-row">{blueprint.capabilities.map((capability) => <span className="pill" key={capability}>{capability}</span>)}</div><div className="toolbar-row"><button className="secondary-pill" disabled title={locale === "fa" ? "قرارداد پیش‌نمایش موجود نیست" : "No preview contract is available"}>{locale === "fa" ? "پیش‌نمایش موجود نیست" : "Preview unavailable"}</button><Link className="primary-pill" href={`/ai?blueprintKey=${encodeURIComponent(blueprint.blueprintKey)}`}>{locale === "fa" ? "استفاده از قالب" : "Use blueprint"}</Link></div></article>)}</div> : <EmptyState title={locale === "fa" ? "قالب فعالی نیست" : "No active blueprints"} description={locale === "fa" ? "سرویس AI قالب فعالی برای این محیط برنگرداند." : "The AI service returned no active blueprints for this environment."} />}
      </section>
    </>}
  </PanelShell>;
}
