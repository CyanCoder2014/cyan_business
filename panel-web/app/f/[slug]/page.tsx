"use client";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { DefinitionForm } from "@/components/forms/definition-form";
import { ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";
import { getPublicForm, submitPublicForm, type PublishedFormView } from "@/lib/forms-api";

export default function PublicFormPage() {
  const { slug } = useParams<{slug:string}>(); const { showToast } = useToast(); const [form, setForm] = useState<PublishedFormView | null>(null); const [loading, setLoading] = useState(true); const [error, setError] = useState<string | null>(null);
  const load = useCallback(async () => { setLoading(true); setError(null); try { setForm(await getPublicForm(slug)); } catch (cause) { const { message } = describeApiError(cause, "Form unavailable"); setError(message); } finally { setLoading(false); } }, [slug]);
  useEffect(() => { void load(); }, [load]);
  return <main className="public-experience-shell"><header className="public-experience-header"><Link href="/" className="brand-lockup"><span className="brand-badge">C</span><span><strong>Cyan Coder</strong><small>Secure form</small></span></Link><StatusBadge tone="success">Public</StatusBadge></header><section className="public-form-surface">{loading ? <Skeleton height={480}/> : error ? <ErrorState title="Form unavailable" description={error} retry={load}/> : form ? <><header><p className="page-kicker">Online form</p><h1>{form.title}</h1>{form.description ? <p>{form.description}</p> : null}</header><DefinitionForm definition={form.definition} submitLabel="Submit form" relationSource={{ kind: "public", slug: form.slug }} onSubmit={async data => { const result = await submitPublicForm(form.slug, data, crypto.randomUUID()); showToast({ tone: "success", title: "Form submitted", message: `Reference ${result.submissionKey}` }); }}/><footer>Your submission is sent directly to the form owner&apos;s workspace.</footer></> : null}</section></main>;
}
