"use client";
import { useState } from "react";
import { prepareMediaUpload, uploadMediaBytes, type MediaScope } from "@/lib/media-api";

export type Field = { type?: string; defaultValue?: unknown; validations?: Array<Record<string, unknown>>; itemValidations?: Record<string, Field> };
export type FileFieldValue = { assetKey: string; fileName: string; mimeType: string; sizeBytes: number };

export function fieldDefaults(fields: Record<string, Field>): Record<string, unknown> {
  return Object.fromEntries(
    Object.entries(fields)
      .filter(([, field]) => field.defaultValue !== undefined)
      .map(([key, field]) => [key, structuredClone(field.defaultValue)]),
  );
}

function humanize(value: string) {
  return value.replace(/([a-z])([A-Z])/g, "$1 $2").replaceAll("_", " ").replace(/^./, (letter) => letter.toUpperCase());
}

function ruleParams(field: Field, kind: string): Record<string, unknown> {
  const rule = field.validations?.find((item) => String(item.validation).toUpperCase() === kind);
  return (rule?.validationParams as Record<string, unknown> | undefined) ?? {};
}

function formatBytes(value: number) {
  if (value < 1024) return `${value} B`;
  if (value < 1048576) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / 1048576).toFixed(1)} MB`;
}

function FileUploadButton({ field, scope, disabled, onUploaded, label }: { field: Field; scope: MediaScope; disabled?: boolean; onUploaded: (value: FileFieldValue) => void; label: string }) {
  const [pending, setPending] = useState(false);
  const [progress, setProgress] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const accept = (ruleParams(field, "ALLOWED_MIME_TYPES").types as string[] | undefined)?.join(",");
  const upload = async (file: File) => {
    setPending(true);
    setProgress(0);
    setError(null);
    try {
      const prepared = await prepareMediaUpload(file, scope);
      await uploadMediaBytes(file, prepared, scope, setProgress);
      onUploaded({ assetKey: prepared.assetKey, fileName: file.name, mimeType: file.type || "application/octet-stream", sizeBytes: file.size });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Upload failed");
    } finally {
      setPending(false);
      setProgress(null);
    }
  };
  return <div className="generated-file-upload">
    <label className="attachment-picker">
      <span>{pending ? `Uploading ${progress ?? 0}%` : label}</span>
      <input type="file" accept={accept} disabled={disabled || pending} onChange={(event) => { const file = event.target.files?.[0]; if (file) void upload(file); event.target.value = ""; }}/>
    </label>
    {error ? <small className="field-error">{error}</small> : null}
  </div>;
}

export function GeneratedField({ path, name, field, value, errors, scope, onChange }: { path: string; name: string; field: Field; value: unknown; errors?: Record<string, string>; scope?: MediaScope; onChange: (value: unknown) => void }) {
  const type = field.type || "string";
  const id = `field-${path.replace(/[^a-zA-Z0-9_-]/g, "-")}`;
  const error = errors?.[path];
  const describedBy = error ? `${id}-error` : undefined;
  const label = humanize(name);
  const required = field.validations?.some((rule) => String(rule.validation).toUpperCase() === "REQUIRED") ?? false;
  const itemFields = field.itemValidations;

  if (type === "boolean") {
    return <label className="generated-field generated-toggle" htmlFor={id}><input id={id} type="checkbox" checked={Boolean(value)} onChange={(event) => onChange(event.target.checked)}/><span>{label}</span>{error ? <small id={describedBy} className="field-error">{error}</small> : null}</label>;
  }

  if (type === "file") {
    const file = value as FileFieldValue | undefined;
    return <div className="generated-field generated-file-field"><span>{label}{required ? <b aria-hidden> *</b> : null}<em>file</em></span>
      {file ? <div className="generated-file-chip"><span title={file.fileName}>{file.fileName}</span><small>{formatBytes(file.sizeBytes)}</small><button type="button" className="generated-list-remove" aria-label={`Remove ${label}`} onClick={() => onChange(undefined)}>×</button></div>
        : scope ? <FileUploadButton field={field} scope={scope} onUploaded={onChange} label={`Upload ${label}`}/> : <small className="field-error">File upload is not available on this form.</small>}
      {error ? <small id={describedBy} className="field-error">{error}</small> : null}
    </div>;
  }

  if (type === "file-list") {
    const files = Array.isArray(value) ? (value as FileFieldValue[]) : [];
    return <fieldset className="generated-field-group generated-list"><legend>{label}</legend>
      {files.map((file, index) => <div className="generated-list-row" key={file.assetKey || index}>
        <div className="generated-file-chip"><span title={file.fileName}>{file.fileName}</span><small>{formatBytes(file.sizeBytes)}</small></div>
        <button type="button" className="generated-list-remove" aria-label={`Remove ${label} item ${index + 1}`} onClick={() => onChange(files.filter((_, i) => i !== index))}>×</button>
      </div>)}
      {scope ? <FileUploadButton field={field} scope={scope} onUploaded={(next) => onChange([...files, next])} label={`+ Add ${label}`}/> : <small className="field-error">File upload is not available on this form.</small>}
      {error ? <small className="field-error">{error}</small> : null}
    </fieldset>;
  }

  if (type === "object" && itemFields && Object.keys(itemFields).length) {
    const objectValue = (value && typeof value === "object" && !Array.isArray(value) ? value : {}) as Record<string, unknown>;
    return <fieldset className="generated-field-group"><legend>{label}</legend>
      {Object.entries(itemFields).map(([key, subField]) => <GeneratedField key={key} path={`${path}.${key}`} name={key} field={subField} value={objectValue[key]} errors={errors} scope={scope} onChange={(next) => onChange({ ...objectValue, [key]: next })}/>)}
      {error ? <small className="field-error">{error}</small> : null}
    </fieldset>;
  }

  if (type === "list") {
    const items = Array.isArray(value) ? value : [];
    const structured = Boolean(itemFields && Object.keys(itemFields).length);
    const rowBlank = structured ? {} : "";
    return <fieldset className="generated-field-group generated-list"><legend>{label}</legend>
      {items.map((item, index) => <div className="generated-list-row" key={index}>
        {structured
          ? <div className="generated-list-item">{Object.entries(itemFields!).map(([key, subField]) => <GeneratedField key={key} path={`${path}[${index}].${key}`} name={key} field={subField} value={(item as Record<string, unknown> | undefined)?.[key]} errors={errors} scope={scope} onChange={(next) => onChange(items.map((row, i) => i === index ? { ...(row as Record<string, unknown>), [key]: next } : row))}/>)}</div>
          : <input aria-invalid={Boolean(errors?.[`${path}[${index}]`])} value={item == null ? "" : String(item)} onChange={(event) => onChange(items.map((row, i) => i === index ? event.target.value : row))}/>}
        <button type="button" className="generated-list-remove" aria-label={`Remove ${label} item ${index + 1}`} onClick={() => onChange(items.filter((_, i) => i !== index))}>×</button>
        {!structured && errors?.[`${path}[${index}]`] ? <small className="field-error">{errors[`${path}[${index}]`]}</small> : null}
      </div>)}
      <button type="button" className="secondary-pill" onClick={() => onChange([...items, rowBlank])}>+ Add {label}</button>
      {error ? <small className="field-error">{error}</small> : null}
    </fieldset>;
  }

  if (type === "object") {
    return <label className="generated-field" htmlFor={id}><span>{label}<em>{type}</em></span><textarea id={id} dir="ltr" aria-invalid={Boolean(error)} aria-describedby={describedBy} value={value === undefined ? "" : JSON.stringify(value, null, 2)} onChange={(event) => { try { onChange(JSON.parse(event.target.value)); } catch { /* Keep the last valid structured value until parsing succeeds. */ } }}/>{error ? <small id={describedBy} className="field-error">{error}</small> : null}</label>;
  }

  return <label className="generated-field" htmlFor={id}><span>{label}{required ? <b aria-hidden> *</b> : null}<em>{type}</em></span><input id={id} required={required} aria-invalid={Boolean(error)} aria-describedby={describedBy} type={type === "number" || type === "integer" ? "number" : type === "date" ? "date" : type === "email" ? "email" : "text"} value={value == null ? "" : String(value)} onChange={(event) => onChange(type === "number" || type === "integer" ? (event.target.value === "" ? null : Number(event.target.value)) : event.target.value)}/>{error ? <small id={describedBy} className="field-error">{error}</small> : null}</label>;
}
