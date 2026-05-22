"use client";

import { useMemo, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { dynamicServices, saveDefinition } from "@/lib/dynamic-api";
import type { DynamicServiceKey } from "@/lib/types";

type FieldType = "string" | "number" | "decimal" | "boolean" | "object" | "list";
type ValidationName = "REQUIRED" | "MIN_LENGTH" | "MAX_LENGTH" | "REGEX" | "ENUM" | "DECIMAL_MIN" | "DECIMAL_MAX";

type MakerField = {
  id: string;
  label: string;
  type: FieldType;
  required: boolean;
  min?: string;
  max?: string;
  regex?: string;
  enumValues?: string;
  defaultValue?: string;
  relationService?: string;
  relationEntity?: string;
};

const palette: Array<{ label: string; type: FieldType; icon: string }> = [
  { label: "Text", type: "string", icon: "Tt" },
  { label: "Number", type: "number", icon: "123" },
  { label: "Decimal", type: "decimal", icon: "0.0" },
  { label: "Switch", type: "boolean", icon: "On" },
  { label: "Group", type: "object", icon: "{}" },
  { label: "Repeater", type: "list", icon: "[]" }
];

function makeField(type: FieldType): MakerField {
  const id = `${type}_${Date.now().toString(36)}`;
  return {
    id,
    label: type === "string" ? "Text field" : `${type[0].toUpperCase()}${type.slice(1)} field`,
    type,
    required: type === "string"
  };
}

function slug(value: string) {
  return value.trim().toLowerCase().replace(/[^a-z0-9]+/g, "_").replace(/^_|_$/g, "") || "field";
}

function validation(field: MakerField, name: ValidationName, order: number, params: Record<string, unknown> = {}) {
  return {
    id: `${field.id}_${name.toLowerCase()}`,
    order,
    validation: name,
    validationParams: params,
    validationMessage: `${field.label} failed ${name.toLowerCase().replace("_", " ")} validation`
  };
}

export default function MakerPage() {
  const [serviceKey, setServiceKey] = useState<DynamicServiceKey>("content-service");
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [entityKey, setEntityKey] = useState("custom-form");
  const [title, setTitle] = useState("Custom Form");
  const [entityType, setEntityType] = useState("FORM");
  const [fields, setFields] = useState<MakerField[]>([
    { id: "fullName", label: "Full name", type: "string", required: true, min: "2" },
    { id: "mobile", label: "Mobile", type: "string", required: true, regex: "^09[0-9]{9}$" }
  ]);
  const [selectedFieldId, setSelectedFieldId] = useState("fullName");
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const selectedField = fields.find((field) => field.id === selectedFieldId) ?? fields[0] ?? null;

  const definition = useMemo(() => {
    const fieldMap = Object.fromEntries(
      fields.map((field) => {
        const validations = [];
        let order = 1;
        if (field.required) validations.push(validation(field, "REQUIRED", order++));
        if (field.min) validations.push(validation(field, field.type === "decimal" || field.type === "number" ? "DECIMAL_MIN" : "MIN_LENGTH", order++, { min: field.min }));
        if (field.max) validations.push(validation(field, field.type === "decimal" || field.type === "number" ? "DECIMAL_MAX" : "MAX_LENGTH", order++, { max: field.max }));
        if (field.regex) validations.push(validation(field, "REGEX", order++, { pattern: field.regex }));
        if (field.enumValues) validations.push(validation(field, "ENUM", order++, { values: field.enumValues.split(",").map((item) => item.trim()).filter(Boolean) }));
        return [
          field.id,
          {
            id: field.id,
            type: field.type === "number" ? "decimal" : field.type,
            defaultValue: field.defaultValue || undefined,
            validations
          }
        ];
      })
    );
    const relations = Object.fromEntries(
      fields
        .filter((field) => field.relationService && field.relationEntity)
        .map((field) => [field.id, { serviceKey: field.relationService, entityKey: field.relationEntity }])
    );
    return {
      serviceKey,
      entityKey,
      entityType,
      title,
      fields: fieldMap,
      validations: [],
      operations: [],
      defaultValues: {},
      relationDefinitions: relations
    };
  }, [entityKey, entityType, fields, serviceKey, title]);

  function addField(type: FieldType) {
    const field = makeField(type);
    setFields((current) => [...current, field]);
    setSelectedFieldId(field.id);
  }

  function updateSelected(patch: Partial<MakerField>) {
    if (!selectedField) return;
    setFields((current) => current.map((field) => (field.id === selectedField.id ? { ...field, ...patch } : field)));
  }

  async function publishDefinition() {
    setLoading(true);
    setStatus(null);
    try {
      await saveDefinition(serviceKey, entityKey, JSON.stringify(definition), { tenantKey, siteKey });
      setStatus("Definition published. It is now available for records, forms, bots, and BPM states.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to publish definition");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Form Maker" subtitle="Edit definitions, fields, validations, relations, and tenant/site scope visually.">
      <div className="builder-shell">
        <aside className="builder-palette">
          <p className="section-title">Bubbles</p>
          {palette.slice(0, 3).map((item) => (
            <button key={item.type} type="button" className="builder-tool" onClick={() => addField(item.type)}>
              <span>{item.icon}</span>
              {item.label}
            </button>
          ))}
          <p className="section-title">Inputs</p>
          {palette.slice(3).map((item) => (
            <button key={item.type} type="button" className="builder-tool" onClick={() => addField(item.type)}>
              <span>{item.icon}</span>
              {item.label}
            </button>
          ))}
          <div className="builder-tip">
            <strong>Tips</strong>
            <span>Use fields as form controls, entity columns, bot inputs, or BPM active forms.</span>
          </div>
        </aside>

        <section className="builder-canvas">
          <div className="builder-toolbar">
            <div className="segmented">
              <button type="button" className="active">Form editor</button>
              <button type="button">Schema JSON</button>
            </div>
            <button type="button" className="btn" onClick={publishDefinition} disabled={loading}>
              {loading ? "Publishing..." : "Publish"}
            </button>
          </div>

          <div className="ai-banner">AI Applied - The form can be generated or refined from chat, then edited manually.</div>

          <div className="form-preview-card">
            <div className="field-grid">
              <div className="field">
                <label>Service</label>
                <select value={serviceKey} onChange={(event) => setServiceKey(event.target.value as DynamicServiceKey)}>
                  {dynamicServices.map((service) => (
                    <option key={service} value={service}>{service}</option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label>Entity key</label>
                <input value={entityKey} onChange={(event) => setEntityKey(slug(event.target.value))} />
              </div>
            </div>
            <div className="field-grid">
              <div className="field">
                <label>Tenant key</label>
                <input value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Site key</label>
                <input value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
              </div>
            </div>
            <div className="field-grid">
              <div className="field">
                <label>Form title</label>
                <input value={title} onChange={(event) => setTitle(event.target.value)} />
              </div>
              <div className="field">
                <label>Entity type</label>
                <input value={entityType} onChange={(event) => setEntityType(event.target.value)} />
              </div>
            </div>

            <div className="canvas-form">
              {fields.map((field) => (
                <button
                  key={field.id}
                  type="button"
                  className={`canvas-field ${selectedFieldId === field.id ? "active" : ""}`}
                  onClick={() => setSelectedFieldId(field.id)}
                >
                  <span className="field-icon">{field.type === "string" ? "Tt" : field.type === "boolean" ? "On" : field.type === "list" ? "[]" : "123"}</span>
                  <span>
                    <strong>{field.label}</strong>
                    <small>{field.id} - {field.type} {field.required ? "· required" : ""}</small>
                  </span>
                </button>
              ))}
            </div>

            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>

        <aside className="builder-inspector">
          <p className="section-title">Inspector</p>
          {selectedField ? (
            <div className="form-grid">
              <div className="field">
                <label>Field id</label>
                <input value={selectedField.id} onChange={(event) => updateSelected({ id: slug(event.target.value) })} />
              </div>
              <div className="field">
                <label>Label</label>
                <input value={selectedField.label} onChange={(event) => updateSelected({ label: event.target.value })} />
              </div>
              <div className="field">
                <label>Type</label>
                <select value={selectedField.type} onChange={(event) => updateSelected({ type: event.target.value as FieldType })}>
                  {palette.map((item) => <option key={item.type} value={item.type}>{item.label}</option>)}
                </select>
              </div>
              <button type="button" className={`chip ${selectedField.required ? "active" : ""}`} onClick={() => updateSelected({ required: !selectedField.required })}>
                Required
              </button>
              <div className="field-grid">
                <div className="field">
                  <label>Min</label>
                  <input value={selectedField.min ?? ""} onChange={(event) => updateSelected({ min: event.target.value })} />
                </div>
                <div className="field">
                  <label>Max</label>
                  <input value={selectedField.max ?? ""} onChange={(event) => updateSelected({ max: event.target.value })} />
                </div>
              </div>
              <div className="field">
                <label>Regex</label>
                <input value={selectedField.regex ?? ""} onChange={(event) => updateSelected({ regex: event.target.value })} />
              </div>
              <div className="field">
                <label>Enum values</label>
                <input value={selectedField.enumValues ?? ""} onChange={(event) => updateSelected({ enumValues: event.target.value })} placeholder="NEW,ACTIVE,CLOSED" />
              </div>
              <div className="field-grid">
                <div className="field">
                  <label>Relation service</label>
                  <input value={selectedField.relationService ?? ""} onChange={(event) => updateSelected({ relationService: event.target.value })} />
                </div>
                <div className="field">
                  <label>Relation entity</label>
                  <input value={selectedField.relationEntity ?? ""} onChange={(event) => updateSelected({ relationEntity: event.target.value })} />
                </div>
              </div>
            </div>
          ) : <p className="muted">Select or add a field.</p>}

          <p className="section-title" style={{ marginTop: 20 }}>Definition JSON</p>
          <pre className="json-view">{JSON.stringify(definition, null, 2)}</pre>
        </aside>
      </div>
    </AppShell>
  );
}
