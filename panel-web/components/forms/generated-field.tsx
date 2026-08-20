export type Field = { type?: string; defaultValue?: unknown; validations?: Array<Record<string, unknown>>; itemValidations?: Record<string, Field> };

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

export function GeneratedField({ path, name, field, value, error, onChange }: { path: string; name: string; field: Field; value: unknown; error?: string; onChange: (value: unknown) => void }) {
  const type = field.type || "string";
  const id = `field-${path.replace(/[^a-zA-Z0-9_-]/g, "-")}`;
  const describedBy = error ? `${id}-error` : undefined;
  const label = humanize(name);
  const required = field.validations?.some((rule) => String(rule.validation).toUpperCase() === "REQUIRED") ?? false;
  const itemFields = field.itemValidations;

  if (type === "boolean") {
    return <label className="generated-field generated-toggle" htmlFor={id}><input id={id} type="checkbox" checked={Boolean(value)} onChange={(event) => onChange(event.target.checked)}/><span>{label}</span>{error ? <small id={describedBy} className="field-error">{error}</small> : null}</label>;
  }

  if (type === "object" && itemFields && Object.keys(itemFields).length) {
    const objectValue = (value && typeof value === "object" && !Array.isArray(value) ? value : {}) as Record<string, unknown>;
    return <fieldset className="generated-field-group"><legend>{label}</legend>
      {Object.entries(itemFields).map(([key, subField]) => <GeneratedField key={key} path={`${path}.${key}`} name={key} field={subField} value={objectValue[key]} onChange={(next) => onChange({ ...objectValue, [key]: next })}/>)}
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
          ? <div className="generated-list-item">{Object.entries(itemFields!).map(([key, subField]) => <GeneratedField key={key} path={`${path}[${index}].${key}`} name={key} field={subField} value={(item as Record<string, unknown> | undefined)?.[key]} onChange={(next) => onChange(items.map((row, i) => i === index ? { ...(row as Record<string, unknown>), [key]: next } : row))}/>)}</div>
          : <input value={item == null ? "" : String(item)} onChange={(event) => onChange(items.map((row, i) => i === index ? event.target.value : row))}/>}
        <button type="button" className="generated-list-remove" aria-label={`Remove ${label} item ${index + 1}`} onClick={() => onChange(items.filter((_, i) => i !== index))}>×</button>
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
