"use client";
import { useState } from "react";

function TreeNode({ label, value, depth }: { label: string; value: unknown; depth: number }) {
  const isArray = Array.isArray(value);
  const isObject = value !== null && typeof value === "object" && !isArray;
  const isExpandable = isArray || isObject;
  const [open, setOpen] = useState(depth < 1);

  if (!isExpandable) {
    return <div className="json-tree-row" style={{ paddingInlineStart: depth * 16 }}>
      <span className="json-tree-key">{label}</span>
      <span className={`json-tree-value json-tree-${value === null || value === undefined ? "null" : typeof value}`}>{value === null || value === undefined ? "null" : typeof value === "boolean" ? String(value) : typeof value === "string" ? `"${value}"` : String(value)}</span>
    </div>;
  }

  const entries = isArray ? (value as unknown[]).map((item, index) => [String(index), item] as const) : Object.entries(value as Record<string, unknown>);
  return <div className="json-tree-branch">
    <button type="button" className="json-tree-row json-tree-toggle" style={{ paddingInlineStart: depth * 16 }} onClick={() => setOpen((current) => !current)} aria-expanded={open}>
      <span className="json-tree-caret" aria-hidden>{open ? "▾" : "▸"}</span>
      <span className="json-tree-key">{label}</span>
      <span className="json-tree-count">{isArray ? `[${entries.length}]` : `{${entries.length}}`}</span>
    </button>
    {open ? (entries.length ? entries.map(([key, child]) => <TreeNode key={key} label={key} value={child} depth={depth + 1}/>) : <div className="json-tree-row json-tree-empty" style={{ paddingInlineStart: (depth + 1) * 16 }}>empty</div>) : null}
  </div>;
}

export function JsonTreeView({ value, emptyLabel }: { value: unknown; emptyLabel?: string }) {
  if (value === null || value === undefined || (typeof value === "object" && Object.keys(value).length === 0)) {
    return <p className="muted">{emptyLabel ?? "No data."}</p>;
  }
  return <div className="json-tree-view">
    {Object.entries(value as Record<string, unknown>).map(([key, child]) => <TreeNode key={key} label={key} value={child} depth={0}/>)}
  </div>;
}
