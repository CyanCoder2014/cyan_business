"use client";

import { useMemo, useState } from "react";
import { AsyncButton, ValidationSummary } from "@/components/ui/primitives";
import { PlatformApiError, fieldErrorsByPath } from "@/lib/api-error";
import { fieldDefaults, GeneratedField, type Field } from "@/components/forms/generated-field";
import type { MediaScope } from "@/lib/media-api";
import type { RelationLookupSource } from "@/components/forms/relation-field";

export function DefinitionForm({ definition, submitLabel, scope, relationSource, onSubmit }: { definition: Record<string, unknown>; submitLabel: string; scope?: MediaScope; relationSource?: RelationLookupSource; onSubmit: (data: Record<string, unknown>) => Promise<void> }) {
  const fields = useMemo(() => ((definition.fields && typeof definition.fields === "object") ? definition.fields : {}) as Record<string, Field>, [definition]);
  const [data, setData] = useState<Record<string, unknown>>(() => fieldDefaults(fields));
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<PlatformApiError | null>(null);
  const errors = fieldErrorsByPath(error);
  const submit = async () => { if (pending) return; setPending(true); setError(null); try { await onSubmit(data); setData(fieldDefaults(fields)); } catch (cause) { setError(cause instanceof PlatformApiError ? cause : new PlatformApiError("UNKNOWN", cause instanceof Error ? cause.message : "Submission failed", 0)); } finally { setPending(false); } };
  return <form className="published-definition-form" onSubmit={event => { event.preventDefault(); void submit(); }}>
    <ValidationSummary errors={error?.fieldErrors ?? []} correlationId={error?.correlationId}/>
    {error && !error.fieldErrors.length ? <div className="operational-banner error" role="alert">{error.message}</div> : null}
    <div className="published-form-fields">{Object.entries(fields).map(([name, field]) => <GeneratedField key={name} path={name} name={name} field={field} value={data[name]} errors={errors} scope={scope} relationSource={relationSource} onChange={value => { setData(current => ({ ...current, [name]: value })); setError(null); }}/>)}</div>
    <AsyncButton pending={pending} pendingLabel="Submitting…" type="submit">{submitLabel}</AsyncButton>
  </form>;
}
