"use client";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { lookupPublicRelationRecords, lookupRelationRecords, resolveRelationRecords, type RelationLookupItem } from "@/lib/dynamic-api";
import type { DynamicServiceKey } from "@/lib/types";

export type RelationConfig = { serviceKey?: string; entityKey?: string; displayField?: string; publicLookup?: boolean };
/** Where the picker is being rendered, which decides whether it can search at all and over which API. */
export type RelationLookupSource =
  | { kind: "scoped"; tenantKey?: string; siteKey?: string }
  | { kind: "public"; slug: string };

const PAGE_SIZE = 20;

export function RelationField({ name, label, required, relation, value, error, source, onChange }: {
  name: string;
  label: string;
  required?: boolean;
  relation: RelationConfig;
  value: unknown;
  error?: string;
  source?: RelationLookupSource;
  onChange: (value: unknown) => void;
}) {
  const id = `relation-${name.replace(/[^a-zA-Z0-9_-]/g, "-")}`;
  const selectedKey = typeof value === "string" && value ? value : "";
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [items, setItems] = useState<RelationLookupItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [selectedLabel, setSelectedLabel] = useState<string | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);

  const search = useCallback(async (nextQuery: string, nextPage: number) => {
    if (!source) return;
    setLoading(true);
    setLoadError(null);
    try {
      const result = source.kind === "public"
        ? await lookupPublicRelationRecords(source.slug, name, { query: nextQuery, page: nextPage, size: PAGE_SIZE })
        : await lookupRelationRecords(relation.serviceKey as DynamicServiceKey, relation.entityKey ?? "", { tenantKey: source.tenantKey, siteKey: source.siteKey }, { query: nextQuery, displayField: relation.displayField, page: nextPage, size: PAGE_SIZE });
      setItems(current => nextPage === 0 ? result.items : [...current, ...result.items]);
      setTotal(result.total);
      setPage(nextPage);
    } catch (cause) {
      setLoadError(cause instanceof Error ? cause.message : "Lookup failed");
    } finally {
      setLoading(false);
    }
  }, [name, relation.serviceKey, relation.entityKey, relation.displayField, source]);

  // Debounced so typing in a >100-row relation issues one server search, not one per keystroke.
  useEffect(() => {
    if (!open) return;
    const timer = setTimeout(() => void search(query, 0), 220);
    return () => clearTimeout(timer);
  }, [open, query, search]);

  // Resolve an already-stored key to its label so an edit form shows a name, not a raw id.
  useEffect(() => {
    if (!selectedKey || !source || source.kind === "public" || !relation.entityKey) { setSelectedLabel(null); return; }
    let live = true;
    resolveRelationRecords(relation.serviceKey as DynamicServiceKey, relation.entityKey, [selectedKey], { tenantKey: source.tenantKey, siteKey: source.siteKey }, relation.displayField)
      .then(result => { if (live) setSelectedLabel(result[0]?.label ?? null); })
      .catch(() => { if (live) setSelectedLabel(null); });
    return () => { live = false; };
  }, [selectedKey, relation.serviceKey, relation.entityKey, relation.displayField, source]);

  useEffect(() => {
    if (!open) return;
    const onDocumentClick = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", onDocumentClick);
    return () => document.removeEventListener("mousedown", onDocumentClick);
  }, [open]);

  const display = useMemo(() => {
    if (!selectedKey) return "";
    return selectedLabel ?? items.find(item => item.recordKey === selectedKey)?.label ?? selectedKey;
  }, [selectedKey, selectedLabel, items]);

  if (!source) {
    return <div className="generated-field generated-relation-field">
      <span>{label}{required ? <b aria-hidden> *</b> : null}<em>relation</em></span>
      <small className="field-error">This related field is not available on this form.</small>
    </div>;
  }

  return <div className="generated-field generated-relation-field" ref={containerRef}>
    <span id={`${id}-label`}>{label}{required ? <b aria-hidden> *</b> : null}<em>relation</em></span>
    {selectedKey && !open ? <div className="generated-relation-chip">
      <span title={display}>{display}</span>
      <code dir="ltr">{selectedKey}</code>
      <button type="button" className="generated-list-remove" aria-label={`Clear ${label}`} onClick={() => { onChange(undefined); setSelectedLabel(null); }}>×</button>
    </div> : <div className="generated-relation-search">
      <input
        id={id}
        role="combobox"
        aria-expanded={open}
        aria-controls={`${id}-listbox`}
        aria-invalid={Boolean(error)}
        autoComplete="off"
        placeholder={`Search ${label}…`}
        value={query}
        onFocus={() => setOpen(true)}
        onChange={event => { setQuery(event.target.value); setOpen(true); }}
      />
      {open ? <div className="generated-relation-menu" id={`${id}-listbox`} role="listbox">
        {loadError ? <p className="field-error">{loadError}</p>
          : items.length ? <>
            {items.map(item => <button
              type="button"
              key={item.recordKey}
              role="option"
              aria-selected={item.recordKey === selectedKey}
              className="generated-relation-option"
              onClick={() => { onChange(item.recordKey); setSelectedLabel(item.label); setOpen(false); setQuery(""); }}
            >
              <strong>{item.label}</strong>
              <code dir="ltr">{item.recordKey}</code>
            </button>)}
            {items.length < total ? <button type="button" className="generated-relation-more" disabled={loading} onClick={() => void search(query, page + 1)}>
              {loading ? "Loading…" : `Load more (${items.length} of ${total})`}
            </button> : <p className="generated-relation-count">{total} {total === 1 ? "match" : "matches"}</p>}
          </>
          : <p className="generated-relation-count">{loading ? "Searching…" : "No matching records"}</p>}
      </div> : null}
    </div>}
    {error ? <small className="field-error">{error}</small> : null}
  </div>;
}
