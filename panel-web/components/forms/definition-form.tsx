"use client";

import { useMemo, useState } from "react";
import { AsyncButton, ValidationSummary } from "@/components/ui/primitives";
import { PlatformApiError, fieldErrorsByPath } from "@/lib/api-error";

type Field = { type?: string; defaultValue?: unknown; validations?: Array<Record<string, unknown>>; itemValidations?: Record<string, Field> };

export function DefinitionForm({ definition, submitLabel, onSubmit }: { definition: Record<string, unknown>; submitLabel: string; onSubmit: (data: Record<string, unknown>) => Promise<void> }) {
  const fields = useMemo(() => ((definition.fields && typeof definition.fields === "object") ? definition.fields : {}) as Record<string, Field>, [definition]);
  const [data, setData] = useState<Record<string, unknown>>(() => defaults(fields));
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<PlatformApiError | null>(null);
  const errors = fieldErrorsByPath(error);
  const submit = async () => { if (pending) return; setPending(true); setError(null); try { await onSubmit(data); setData(defaults(fields)); } catch (cause) { setError(cause instanceof PlatformApiError ? cause : new PlatformApiError("UNKNOWN", cause instanceof Error ? cause.message : "Submission failed", 0)); } finally { setPending(false); } };
  return <form className="published-definition-form" onSubmit={event => { event.preventDefault(); void submit(); }}>
    <ValidationSummary errors={error?.fieldErrors ?? []} correlationId={error?.correlationId}/>
    {error && !error.fieldErrors.length ? <div className="operational-banner error" role="alert">{error.message}</div> : null}
    <div className="published-form-fields">{Object.entries(fields).map(([name, field]) => <GeneratedField key={name} path={name} label={name} field={field} value={data[name]} error={errors[name] ?? errors[`data.${name}`]} onChange={value => { setData(current => ({ ...current, [name]: value })); setError(null); }}/>)}</div>
    <AsyncButton pending={pending} pendingLabel="Submitting…" type="submit">{submitLabel}</AsyncButton>
  </form>;
}

function GeneratedField({ path, label, field, value, error, onChange }: { path: string; label: string; field: Field; value: unknown; error?: string; onChange: (value: unknown) => void }) {
  const type = field.type ?? "string"; const id = `public-${path.replace(/[^a-zA-Z0-9_-]/g, "-")}`;
  const required = field.validations?.some(rule => String(rule.validation).toUpperCase() === "REQUIRED") ?? false;
  if (type === "object" && field.itemValidations) {
    const object = value && typeof value === "object" && !Array.isArray(value) ? value as Record<string, unknown> : {};
    return <fieldset className="published-object-field"><legend>{humanize(label)}</legend>{Object.entries(field.itemValidations).map(([child, childField]) => <GeneratedField key={child} path={`${path}.${child}`} label={child} field={childField} value={object[child]} error={error} onChange={next => onChange({ ...object, [child]: next })}/>)}</fieldset>;
  }
  if (type === "boolean") return <label className="generated-field generated-toggle" htmlFor={id}><input id={id} type="checkbox" checked={Boolean(value)} onChange={event => onChange(event.target.checked)}/><span>{humanize(label)}</span>{error ? <small className="field-error">{error}</small> : null}</label>;
  if (type === "list") return <label className="generated-field" htmlFor={id}><span>{humanize(label)}<em>list</em></span><textarea id={id} dir="auto" required={required} value={Array.isArray(value) ? value.join("\n") : ""} placeholder="One item per line" onChange={event => onChange(event.target.value.split("\n").map(item => item.trim()).filter(Boolean))}/>{error ? <small className="field-error">{error}</small> : null}</label>;
  return <label className="generated-field" htmlFor={id}><span>{humanize(label)}{required ? <b aria-hidden> *</b> : null}<em>{type}</em></span><input id={id} required={required} aria-invalid={Boolean(error)} type={type === "number" || type === "integer" ? "number" : type === "date" ? "date" : type === "email" ? "email" : "text"} value={value == null ? "" : String(value)} onChange={event => onChange(type === "number" || type === "integer" ? (event.target.value === "" ? null : Number(event.target.value)) : event.target.value)}/>{error ? <small className="field-error">{error}</small> : null}</label>;
}
function defaults(fields: Record<string, Field>) { return Object.fromEntries(Object.entries(fields).filter(([, field]) => field.defaultValue !== undefined).map(([key, field]) => [key, structuredClone(field.defaultValue)])); }
function humanize(value: string) { return value.replace(/([a-z])([A-Z])/g, "$1 $2").replaceAll("_", " ").replace(/^./, letter => letter.toUpperCase()); }
