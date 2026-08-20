"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { CodeViewer, EmptyState, ErrorState, Skeleton, StatusBadge, Tabs } from "@/components/ui/primitives";
import { createProjectRelease, getClientDraft, listConversationSessions, listProjectReleases, listProvisioningRuns, provisionClientDraft, publishProjectRelease, rollbackProjectRelease } from "@/lib/platform-api";
import type { AiConversationSession, ClientAppDraft, ProjectRelease, ProvisioningRun } from "@/lib/types";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";

type WorkspaceTab = "overview" | "ai" | "structure" | "automations" | "bpm" | "data" | "channels" | "site" | "releases" | "provisioning" | "activity";

export default function ProjectPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const { showToast } = useToast();
  const [draft, setDraft] = useState<ClientAppDraft | null>(null);
  const [sessions, setSessions] = useState<AiConversationSession[]>([]);
  const [runs, setRuns] = useState<ProvisioningRun[]>([]);
  const [releases, setReleases] = useState<ProjectRelease[]>([]);
  const [tab, setTab] = useState<WorkspaceTab>("overview");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!tenantKey) return;
    setLoading(true);
    setError(null);
    const results = await Promise.allSettled([
      getClientDraft(projectId),
      listConversationSessions({ draftId: projectId, tenantKey, siteKey: siteKey || undefined }),
      listProvisioningRuns(projectId),
      listProjectReleases(projectId),
    ]);
    if (results[0].status === "fulfilled") setDraft(results[0].value); else setError(describeApiError(results[0].reason, "Project unavailable").message);
    if (results[1].status === "fulfilled") setSessions(results[1].value);
    if (results[2].status === "fulfilled") setRuns(results[2].value);
    if (results[3].status === "fulfilled") setReleases(results[3].value);
    if (results.slice(1).some((result) => result.status === "rejected")) setError((current) => current ?? (locale === "fa" ? "بخشی از فضای پروژه در دسترس نیست." : "Part of the project workspace is unavailable."));
    setLoading(false);
  }, [locale, projectId, siteKey, tenantKey]);

  useEffect(() => { void load(); }, [load, queryVersion]);
  const capabilities = draft?.resolvedDsl?.app?.capabilities ?? [];
  const availableTabs = useMemo(() => {
    const items: Array<{ key: WorkspaceTab; label: string }> = [
      { key: "overview", label: locale === "fa" ? "نمای کلی" : "Overview" },
      { key: "ai", label: "AI" },
      { key: "structure", label: locale === "fa" ? "ساختار" : "Structure" },
      { key: "data", label: locale === "fa" ? "داده" : "Data" },
      { key: "releases", label: locale === "fa" ? "نسخه‌ها" : "Releases" },
      { key: "provisioning", label: locale === "fa" ? "راه‌اندازی" : "Provisioning" },
      { key: "activity", label: locale === "fa" ? "فعالیت" : "Activity" },
    ];
    if (capabilities.some((item) => item.includes("automation"))) items.splice(3, 0, { key: "automations", label: locale === "fa" ? "اتوماسیون" : "Automations" });
    if (capabilities.some((item) => item.includes("bpm") || item.includes("workflow"))) items.splice(4, 0, { key: "bpm", label: "BPM" });
    if (capabilities.some((item) => item.includes("bot") || item.includes("channel"))) items.splice(-3, 0, { key: "channels", label: locale === "fa" ? "کانال‌ها" : "Channels" });
    if (capabilities.some((item) => item.includes("site") || item.includes("storefront"))) items.splice(-3, 0, { key: "site", label: locale === "fa" ? "سایت" : "Site" });
    return items;
  }, [capabilities, locale]);

  const execute = async (mode: "PLAN" | "APPLY") => {
    if (mode === "APPLY" && !window.confirm(locale === "fa" ? "این پروژه در سرویس‌های مالک اعمال شود؟" : "Apply this project to the owning services?")) return;
    setBusy(true); setError(null);
    try {
      const run = await provisionClientDraft(projectId, { mode, triggerType: "PANEL", idempotencyKey: crypto.randomUUID() });
      setRuns((current) => [run, ...current.filter((item) => item.runId !== run.runId)]);
      setTab("provisioning");
    } catch (cause) { const { title, message } = describeApiError(cause, "Provisioning failed"); setError(message); showToast({ tone: "error", title, message }); }
    finally { setBusy(false); }
  };
  const createRelease = async (runId: string) => {
    if (!window.confirm(locale === "fa" ? "از این اجرای موفق یک نسخه تغییرناپذیر ساخته شود؟" : "Create an immutable release from this successful run?")) return;
    setBusy(true); try { const release = await createProjectRelease(projectId, runId); setReleases((current) => [release, ...current]); setTab("releases"); } catch (cause) { const { title, message } = describeApiError(cause, "Release creation failed"); setError(message); showToast({ tone: "error", title, message }); } finally { setBusy(false); }
  };
  const mutateRelease = async (release: ProjectRelease, action: "publish" | "rollback") => {
    if (!window.confirm(action === "publish" ? (locale === "fa" ? "این نسخه فعال شود؟" : "Publish this release?") : (locale === "fa" ? "به این نسخه بازگردانی شود؟" : "Roll back to this release?"))) return;
    setBusy(true); try { const updated = action === "publish" ? await publishProjectRelease(release.releaseId) : await rollbackProjectRelease(release.releaseId); setReleases((current) => current.map((item) => item.releaseId === updated.releaseId ? updated : item.status === "ACTIVE" ? { ...item, status: "SUPERSEDED" } : item)); } catch (cause) { const { title, message } = describeApiError(cause, "Release operation failed"); setError(message); showToast({ tone: "error", title, message }); } finally { setBusy(false); }
  };

  if (loading) return <PanelShell activeKey="blueprints" title="Project workspace" titleFa="فضای پروژه" subtitle="Loading persisted project state." subtitleFa="در حال بارگیری وضعیت پایدار پروژه."><Skeleton height={520} /></PanelShell>;
  if (!draft) return <PanelShell activeKey="blueprints" title="Project workspace" titleFa="فضای پروژه" subtitle="The project could not be loaded." subtitleFa="پروژه بارگیری نشد."><ErrorState title="Project unavailable" description={error ?? "The AI orchestrator did not return this project."} retry={load} /></PanelShell>;

  const dsl = draft.resolvedDsl;
  const activeRelease = releases.find((release) => release.status === "ACTIVE");
  return <PanelShell activeKey="blueprints" title={draft.title} titleFa={draft.title} subtitle={draft.latestIntent} subtitleFa={draft.latestIntent} kicker="Project workspace" kickerFa="فضای پروژه">
    <div className="project-workspace-head"><div className="pill-row"><StatusBadge tone={draft.status === "PROVISIONED" ? "success" : draft.status === "FAILED" ? "danger" : "info"}>{draft.status}</StatusBadge><span className="pill">r{draft.revision ?? "—"}</span>{activeRelease ? <span className="pill">{locale === "fa" ? "نسخه فعال" : "Active release"}: {activeRelease.releaseId}</span> : null}</div><div><button className="secondary-pill" disabled={busy} onClick={() => execute("PLAN")}>{locale === "fa" ? "برنامه‌ریزی" : "Plan"}</button><button className="primary-pill" disabled={busy || draft.status === "WAITING_FOR_ANSWERS"} onClick={() => execute("APPLY")}>{locale === "fa" ? "اعمال" : "Apply"}</button></div></div>
    {error ? <div className="operational-banner error" role="alert">{error}<button onClick={() => setError(null)}>×</button></div> : null}
    <Tabs active={tab} onChange={(key) => setTab(key as WorkspaceTab)} items={availableTabs} />
    <section className="panel-card project-workspace-body">
      {tab === "overview" ? <div className="project-overview-grid"><div><h2>{locale === "fa" ? "وضعیت پروژه" : "Project state"}</h2><dl className="detail-list"><div className="detail-item"><dt>{locale === "fa" ? "محدوده" : "Scope"}</dt><dd>{draft.tenantKey}{draft.siteKey ? ` / ${draft.siteKey}` : ""}</dd></div><div className="detail-item"><dt>{locale === "fa" ? "سوال‌های باز" : "Pending questions"}</dt><dd>{draft.pendingQuestions.length}</dd></div><div className="detail-item"><dt>{locale === "fa" ? "آخرین تغییر" : "Updated"}</dt><dd>{draft.updatedAt ? new Date(draft.updatedAt).toLocaleString(locale) : "—"}</dd></div></dl>{draft.pendingQuestions.length ? <ul>{draft.pendingQuestions.map((question) => <li key={question}>{question}</li>)}</ul> : null}</div><div className="summary-grid"><div className="mini-card"><strong>{dsl.routes.length}</strong><span>Routes</span></div><div className="mini-card"><strong>{dsl.entities.length}</strong><span>Entities</span></div><div className="mini-card"><strong>{dsl.flows.length}</strong><span>Flows</span></div><div className="mini-card"><strong>{capabilities.length}</strong><span>Capabilities</span></div></div></div>
        : tab === "ai" ? sessions.length ? <div className="project-list">{sessions.map((session) => <Link className="project-card" href={`/ai?sessionId=${encodeURIComponent(session.sessionId)}`} key={session.sessionId}><div><strong>{session.latestPrompt || session.latestQuestion || session.sessionId}</strong><StatusBadge tone={session.status === "ACTIVE" ? "success" : "neutral"}>{session.status}</StatusBadge></div><p>{session.sessionId}</p></Link>)}</div> : <EmptyState title={locale === "fa" ? "گفتگویی نیست" : "No linked conversations"} description={locale === "fa" ? "گفتگوی پروژه را در AI Studio شروع کنید." : "Start the project conversation in AI Studio."} />
        : tab === "structure" ? <CodeViewer value={{ routes: dsl.routes, entities: dsl.entities, flows: dsl.flows }} />
        : tab === "automations" ? <Link className="secondary-pill" href="/automation">{locale === "fa" ? "باز کردن اتوماسیون" : "Open Automation"}</Link>
        : tab === "bpm" ? <Link className="secondary-pill" href="/flows">{locale === "fa" ? "باز کردن BPM" : "Open BPM"}</Link>
        : tab === "data" ? <><CodeViewer value={dsl.entities} /><Link className="secondary-pill" href="/data">{locale === "fa" ? "باز کردن داده‌ها" : "Open Data"}</Link></>
        : tab === "channels" ? <><CodeViewer value={dsl.delivery.botApis} /><Link className="secondary-pill" href="/integrations">{locale === "fa" ? "باز کردن کانال‌ها" : "Open channels"}</Link></>
        : tab === "site" ? <><CodeViewer value={dsl.delivery.publicApis} /><Link className="secondary-pill" href="/site-builder">{locale === "fa" ? "باز کردن سایت" : "Open site"}</Link></>
        : tab === "releases" ? releases.length ? <div className="version-list">{releases.map((release) => <div key={release.releaseId}><strong>{release.releaseId}</strong><StatusBadge tone={release.status === "ACTIVE" ? "success" : "info"}>{release.status}</StatusBadge><span>r{release.sourceRevision}</span><time>{new Date(release.createdAt).toLocaleString(locale)}</time><button className="secondary-pill" disabled={busy || release.status === "ACTIVE"} onClick={() => mutateRelease(release, release.status === "SUPERSEDED" ? "rollback" : "publish")}>{release.status === "SUPERSEDED" ? (locale === "fa" ? "بازگردانی" : "Rollback") : (locale === "fa" ? "انتشار" : "Publish")}</button></div>)}</div> : <EmptyState title={locale === "fa" ? "نسخه‌ای نیست" : "No releases"} description={locale === "fa" ? "یک اجرای موفق را به نسخه تبدیل کنید." : "Create a release from a successful provisioning run."} />
        : tab === "provisioning" ? runs.length ? <div className="run-timeline">{runs.map((run) => <article key={run.runId}><div><strong>{run.runId}</strong><StatusBadge tone={run.status === "SUCCESS" ? "success" : run.status === "FAILED" ? "danger" : "info"}>{run.status}</StatusBadge></div><p>{run.stepResults.length} {locale === "fa" ? "گام" : "steps"}</p><CodeViewer value={run.stepResults} />{run.status === "SUCCESS" ? <button className="secondary-pill" disabled={busy || releases.some((release) => release.provisioningRunId === run.runId)} onClick={() => createRelease(run.runId)}>{locale === "fa" ? "ساخت نسخه" : "Create release"}</button> : null}</article>)}</div> : <EmptyState title={locale === "fa" ? "اجرایی نیست" : "No provisioning runs"} description={locale === "fa" ? "ابتدا PLAN را اجرا کنید." : "Run PLAN first."} />
        : <div className="version-list">{[...runs.map((run) => ({ id: run.runId, label: `Provisioning · ${run.status}`, at: run.startedAt })), ...releases.map((release) => ({ id: release.releaseId, label: `Release · ${release.status}`, at: release.createdAt }))].sort((a, b) => String(b.at).localeCompare(String(a.at))).map((item) => <div key={item.id}><strong>{item.label}</strong><span>{item.id}</span><time>{item.at ? new Date(item.at).toLocaleString(locale) : "—"}</time></div>)}</div>}
    </section>
  </PanelShell>;
}
