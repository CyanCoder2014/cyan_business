"use client";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { AsyncButton, EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";
import { cancelAiArtifactJob, listAiArtifactJobs, listAiProviderProfiles, saveAiProviderProfile, startAiArtifactJob, type AiArtifactJob, type AiProviderProfile } from "@/lib/ai-operations-api";

const emptyProfile = { profileKey: "", displayName: "", baseUrl: "", operationPath: "/v1/chat/completions", model: "", secretRef: "", modalities: ["TEXT"], enabled: true };
export default function AiProviders() {
  const { locale } = usePanel(); const { tenantKey, siteKey, queryVersion } = useScopeAccess(); const { showToast } = useToast();
  const scope = useMemo(() => tenantKey ? { tenantKey, siteKey: siteKey ?? undefined } : null, [tenantKey, siteKey]);
  const [profiles, setProfiles] = useState<AiProviderProfile[]>([]); const [jobs, setJobs] = useState<AiArtifactJob[]>([]);
  const [profile, setProfile] = useState(emptyProfile); const [job, setJob] = useState({ operation: "GENERATE_IMAGE", providerProfileKey: "", instructions: "", mimeType: "image/png", fileName: "generated.png", retentionDays: 30 });
  const [loading, setLoading] = useState(true); const [pending, setPending] = useState<string | null>(null); const [error, setError] = useState<string | null>(null);
  const load = useCallback(async () => {
    if (!scope) { setLoading(false); return; } setLoading(true); setError(null);
    try { const [profileValues, jobValues] = await Promise.all([listAiProviderProfiles(scope), listAiArtifactJobs(scope)]); setProfiles(profileValues); setJobs(jobValues); setJob(current => ({ ...current, providerProfileKey: current.providerProfileKey || profileValues.find(value => value.enabled)?.profileKey || "" })); }
    catch (reason) { const { title, message } = describeApiError(reason, "AI operation failed"); setError(message); showToast({ tone: "error", title, message }); } finally { setLoading(false); }
  }, [scope, queryVersion, showToast]);
  useEffect(() => { void load(); }, [load]);
  const perform = async (key: string, operation: () => Promise<unknown>) => { if (pending) return; setPending(key); setError(null); try { await operation(); await load(); } catch (reason) { const { title, message } = describeApiError(reason, "Action failed"); setError(message); showToast({ tone: "error", title, message }); } finally { setPending(null); } };
  const shell = (children: React.ReactNode) => <PanelShell activeKey="studio" title="AI providers" titleFa="ارائه‌دهندگان هوش مصنوعی" subtitle="Credentials use secret references, never stored values." subtitleFa="اعتبارنامه فقط با ارجاع راز استفاده می‌شود.">{children}</PanelShell>;
  if (loading) return shell(<Skeleton height={600}/>); if (!scope) return shell(<EmptyState title="Select a workspace" description="Provider profiles are tenant scoped."/>);
  return <PanelShell activeKey="studio" kicker="AI operations" kickerFa="عملیات هوش مصنوعی" title="Providers and generated media" titleFa="ارائه‌دهندگان و رسانه تولیدشده" subtitle="Credentials remain behind environment or Kubernetes secret references." subtitleFa="اطلاعات محرمانه فقط با ارجاع راز محیط یا کوبرنتیز استفاده می‌شود.">
    <Link className="back-link" href="/ai">← {locale === "fa" ? "استودیوی هوش مصنوعی" : "AI Studio"}</Link>{error ? <ErrorState title="AI operation failed" description={error} retry={load}/> : null}
    <div className="settings-grid"><section className="panel-card access-form"><h2>{locale === "fa" ? "پروفایل API سفارشی" : "Custom API profile"}</h2>
      <label><span>Profile key</span><input dir="ltr" disabled={!!pending} value={profile.profileKey} onChange={event => setProfile({ ...profile, profileKey: event.target.value.toLowerCase().replace(/[^a-z0-9-]/g, "") })}/></label>
      <label><span>{locale === "fa" ? "نام نمایشی" : "Display name"}</span><input disabled={!!pending} value={profile.displayName} onChange={event => setProfile({ ...profile, displayName: event.target.value })}/></label>
      <label><span>HTTPS base URL</span><input dir="ltr" disabled={!!pending} value={profile.baseUrl} onChange={event => setProfile({ ...profile, baseUrl: event.target.value })}/></label>
      <label><span>Operation path</span><input dir="ltr" disabled={!!pending} value={profile.operationPath} onChange={event => setProfile({ ...profile, operationPath: event.target.value })}/></label>
      <label><span>Model</span><input dir="ltr" disabled={!!pending} value={profile.model} onChange={event => setProfile({ ...profile, model: event.target.value })}/></label>
      <label><span>Secret reference</span><input dir="ltr" placeholder="env:PROVIDER_API_KEY" disabled={!!pending} value={profile.secretRef} onChange={event => setProfile({ ...profile, secretRef: event.target.value })}/></label>
      <fieldset><legend>Modalities</legend>{["TEXT", "IMAGE", "AUDIO", "VIDEO", "FILE"].map(modality => <label className="check-row" key={modality}><input type="checkbox" disabled={!!pending} checked={profile.modalities.includes(modality)} onChange={event => setProfile({ ...profile, modalities: event.target.checked ? [...profile.modalities, modality] : profile.modalities.filter(value => value !== modality) })}/><span>{modality}</span></label>)}</fieldset>
      <AsyncButton pending={pending === "profile"} disabled={!profile.profileKey || !profile.displayName || !profile.baseUrl || !profile.model || !profile.secretRef || !profile.modalities.length || !!(pending && pending !== "profile")} onClick={() => perform("profile", async () => { await saveAiProviderProfile(scope, profile); setProfile(emptyProfile); })}>{locale === "fa" ? "ذخیره پروفایل" : "Save profile"}</AsyncButton>
    </section><section className="panel-card"><h2>{locale === "fa" ? "پروفایل‌ها" : "Profiles"}</h2>{profiles.length ? <div className="run-history">{profiles.map(value => <button key={value.profileKey} onClick={() => setProfile({ profileKey: value.profileKey, displayName: value.displayName, baseUrl: value.baseUrl, operationPath: value.operationPath, model: value.model, secretRef: value.secretRef, modalities: value.modalities, enabled: value.enabled })}><span><strong>{value.displayName}</strong><small>{value.model} · {value.modalities.join(", ")}</small></span><StatusBadge tone={value.configurationStatus === "CONFIGURED" ? "success" : "warning"}>{value.configurationStatus}</StatusBadge></button>)}</div> : <EmptyState title="No provider profiles" description="Configure a real custom API before starting a job."/>}</section>
    <section className="panel-card access-form"><h2>{locale === "fa" ? "تولید رسانه" : "Generate media"}</h2>
      <label><span>Operation</span><select disabled={!!pending} value={job.operation} onChange={event => setJob({ ...job, operation: event.target.value })}><option>GENERATE_IMAGE</option><option>GENERATE_AUDIO</option><option>GENERATE_VIDEO</option></select></label>
      <label><span>Provider</span><select disabled={!!pending} value={job.providerProfileKey} onChange={event => setJob({ ...job, providerProfileKey: event.target.value })}><option value="">Select provider</option>{profiles.map(value => <option key={value.profileKey} value={value.profileKey}>{value.displayName} · {value.configurationStatus}</option>)}</select></label>
      <label><span>{locale === "fa" ? "دستور" : "Instructions"}</span><textarea disabled={!!pending} value={job.instructions} onChange={event => setJob({ ...job, instructions: event.target.value })}/></label>
      <label><span>MIME type</span><input dir="ltr" disabled={!!pending} value={job.mimeType} onChange={event => setJob({ ...job, mimeType: event.target.value })}/></label>
      <label><span>{locale === "fa" ? "نام فایل" : "File name"}</span><input disabled={!!pending} value={job.fileName} onChange={event => setJob({ ...job, fileName: event.target.value })}/></label>
      <AsyncButton pending={pending === "job"} disabled={!job.providerProfileKey || !job.instructions.trim() || !!(pending && pending !== "job")} onClick={() => perform("job", () => startAiArtifactJob(scope, { ...job, input: {}, assets: [] }))}>{locale === "fa" ? "شروع تولید" : "Start generation"}</AsyncButton>
    </section><section className="panel-card"><h2>{locale === "fa" ? "کارهای تولید" : "Generation jobs"}</h2>{jobs.length ? <div className="run-history">{jobs.map(value => <article key={value.jobId}><div><strong>{value.operation}</strong><small>{value.providerProfileKey} · {new Date(value.createdAt).toLocaleString(locale)}</small></div><StatusBadge tone={value.status === "SUCCEEDED" ? "success" : value.status === "FAILED" ? "danger" : "warning"}>{value.status}</StatusBadge>{["QUEUED", "RUNNING"].includes(value.status) ? <AsyncButton className="secondary-pill" pending={pending === value.jobId} disabled={!!(pending && pending !== value.jobId)} onClick={() => perform(value.jobId, () => cancelAiArtifactJob(scope, value.jobId))}>{locale === "fa" ? "لغو" : "Cancel"}</AsyncButton> : null}{value.assetKey ? <code dir="ltr">{value.assetKey}</code> : null}{value.errorMessage ? <small>{value.errorMessage}</small> : null}</article>)}</div> : <EmptyState title="No generation jobs" description="Persisted jobs will appear here."/>}</section></div>
  </PanelShell>;
}
