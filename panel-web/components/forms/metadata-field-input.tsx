export type MetadataFieldDescriptor = { key: string; type: string; required: boolean; description: string; example?: unknown };

/**
 * Renders one input for a backend-described config parameter (BPM action params,
 * automation node config fields) — typed off the descriptor's declared type, with
 * the description shown as a hint and the example as a placeholder.
 */
export function MetadataFieldInput({ field, value, onChange }: { field: MetadataFieldDescriptor; value: unknown; onChange: (value: unknown) => void }) {
  const id = `metadata-field-${field.key}`;
  if (field.type === "boolean") return <label className="toggle-row" htmlFor={id}><input id={id} type="checkbox" checked={Boolean(value)} onChange={(event) => onChange(event.target.checked)}/><span>{field.key}{field.required ? " *" : ""}</span></label>;
  if (field.type === "integer" || field.type === "number") return <label htmlFor={id}><span>{field.key}{field.required ? " *" : ""}</span><input id={id} type="number" value={value == null ? "" : String(value)} placeholder={field.example != null ? String(field.example) : undefined} onChange={(event) => onChange(event.target.value === "" ? undefined : Number(event.target.value))}/>{field.description ? <small className="bpm-field-hint">{field.description}</small> : null}</label>;
  if (field.type.startsWith("array")) {
    const items = Array.isArray(value) ? value : [];
    return <label htmlFor={id}><span>{field.key}{field.required ? " *" : ""}</span><input id={id} dir="ltr" value={items.join(", ")} placeholder={field.example != null ? String(field.example) : "value1, value2"} onChange={(event) => onChange(event.target.value.split(",").map((item) => item.trim()).filter(Boolean))}/>{field.description ? <small className="bpm-field-hint">{field.description}</small> : null}</label>;
  }
  if (field.type === "object" || field.type === "any" || field.type.includes("object")) return <label htmlFor={id}><span>{field.key}{field.required ? " *" : ""}<em>{field.type}</em></span><textarea id={id} dir="ltr" value={value === undefined ? "" : JSON.stringify(value, null, 2)} placeholder={field.example != null ? JSON.stringify(field.example, null, 2) : undefined} onChange={(event) => { try { onChange(event.target.value.trim() === "" ? undefined : JSON.parse(event.target.value)); } catch { /* Keep the last valid structured value until parsing succeeds. */ } }}/>{field.description ? <small className="bpm-field-hint">{field.description}</small> : null}</label>;
  return <label htmlFor={id}><span>{field.key}{field.required ? " *" : ""}</span><input id={id} dir="ltr" value={value == null ? "" : String(value)} placeholder={field.example != null ? String(field.example) : undefined} onChange={(event) => onChange(event.target.value)}/>{field.description ? <small className="bpm-field-hint">{field.description}</small> : null}</label>;
}
