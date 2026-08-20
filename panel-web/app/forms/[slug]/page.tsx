"use client";
import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { DefinitionForm } from "@/components/forms/definition-form";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";
import { getMemberForm, submitMemberForm, type PublishedFormView } from "@/lib/forms-api";

export default function MemberFormPage() {
  const { slug } = useParams<{slug:string}>(); const { locale } = usePanel(); const { showToast } = useToast(); const { tenantKey, siteKey } = useScopeAccess();
  const [form, setForm] = useState<PublishedFormView | null>(null); const [loading, setLoading] = useState(true); const [error, setError] = useState<string | null>(null);
  const load = useCallback(async () => { if (!tenantKey) { setLoading(false); return; } setLoading(true); setError(null); try { setForm(await getMemberForm(slug, { tenantKey, siteKey })); } catch (cause) { const { message } = describeApiError(cause, "Form unavailable"); setError(message); } finally { setLoading(false); } }, [slug, tenantKey, siteKey]);
  useEffect(() => { void load(); }, [load]);
  return <PanelShell activeKey="forms" title={form?.title || (locale === "fa" ? "فرم" : "Form")} titleFa={form?.title || "فرم"} subtitle={form?.description || "Private workspace form"} subtitleFa={form?.description || "فرم خصوصی فضای کاری"}>
    {loading ? <Skeleton height={440}/> : error ? <ErrorState title={locale === "fa" ? "فرم در دسترس نیست" : "Form unavailable"} description={error} retry={load}/> : form && tenantKey ? <section className="published-form-card panel-card"><header><div><p className="page-kicker">{form.serviceKey} · {form.entityKey}</p><h2>{form.title}</h2></div><StatusBadge tone="info">{form.visibility}</StatusBadge></header><DefinitionForm definition={form.definition} submitLabel={locale === "fa" ? "ارسال فرم" : "Submit form"} scope={{ tenantKey, siteKey: siteKey ?? undefined }} onSubmit={async data => { const result = await submitMemberForm(form.slug, data, { tenantKey, siteKey }, crypto.randomUUID()); showToast({ tone: "success", title: locale === "fa" ? "فرم ارسال شد" : "Form submitted", message: result.submissionKey }); }}/></section> : <EmptyState title="Select a workspace" description="This private form belongs to a workspace."/>}
  </PanelShell>;
}
