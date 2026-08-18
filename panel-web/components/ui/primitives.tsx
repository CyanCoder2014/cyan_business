"use client";

import { useCallback, useEffect, useId, useRef, type ButtonHTMLAttributes, type HTMLAttributes, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes } from "react";
import type { PlatformFieldError } from "@/lib/api-error";

export function Button(props: ButtonHTMLAttributes<HTMLButtonElement>) { return <button {...props} className={`primary-pill ${props.className ?? ""}`} />; }
export function AsyncButton({ pending=false, pendingLabel="Working…", children, disabled, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { pending?:boolean; pendingLabel?:string }) {
  return <button {...props} disabled={disabled||pending} aria-disabled={disabled||pending} aria-busy={pending} className={`primary-pill async-button ${props.className??""}`}>{pending?<span className="button-spinner" aria-hidden/>:null}<span>{pending?pendingLabel:children}</span></button>;
}
export function IconButton({ label, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { label: string }) { return <button {...props} aria-label={label} className={`header-icon-button ${props.className ?? ""}`} />; }
export function Field({ label, hint, error, ...props }: InputHTMLAttributes<HTMLInputElement> & { label: string; hint?: string; error?: string }) { const id = useId(); const helpId = `${id}-help`; return <label className="ui-field" htmlFor={id}><span>{label}</span><input {...props} id={id} aria-invalid={Boolean(error)} aria-describedby={error||hint?helpId:undefined}/>{error ? <small id={helpId} className="field-error">{error}</small> : hint ? <small id={helpId}>{hint}</small> : null}</label>; }
export function Select({ label, children, ...props }: SelectHTMLAttributes<HTMLSelectElement> & { label: string; children: ReactNode }) { const id = useId(); return <label className="ui-field" htmlFor={id}><span>{label}</span><select {...props} id={id}>{children}</select></label>; }
export function Combobox(props: InputHTMLAttributes<HTMLInputElement> & { label: string; options: string[] }) { const id = useId(); return <label className="ui-field"><span>{props.label}</span><input {...props} list={id}/><datalist id={id}>{props.options.map((option) => <option key={option} value={option}/>)}</datalist></label>; }
export function Tabs({ items, active, onChange }: { items: Array<{ key: string; label: string }>; active: string; onChange: (key: string) => void }) { return <div className="ui-tabs" role="tablist">{items.map((item) => <button role="tab" aria-selected={active === item.key} key={item.key} onClick={() => onChange(item.key)}>{item.label}</button>)}</div>; }
export const SegmentedControl = Tabs;
export function Badge({ children, tone = "neutral" }: { children: ReactNode; tone?: string }) { return <span className={`status-pill ${tone}`}>{children}</span>; }
export const StatusBadge = Badge;
export function Card(props: HTMLAttributes<HTMLElement>) { return <section {...props} className={`panel-card ${props.className ?? ""}`} />; }
export function Dialog({ open, title, description, children, onClose, closeLabel="Close dialog", size="medium", dismissible=true }: { open: boolean; title: string; description?:string; children: ReactNode; onClose: () => void; closeLabel?:string; size?:"small"|"medium"|"large"; dismissible?:boolean }) {
  const dialogRef = useRef<HTMLElement>(null);
  const returnFocusRef = useRef<HTMLElement | null>(null);
  const titleId = useId();
  const descriptionId = useId();
  const close = useCallback(() => {
    const target = returnFocusRef.current;
    onClose();
    window.setTimeout(() => target?.focus(), 0);
  }, [onClose]);
  useEffect(() => {
    if (!open) return;
    returnFocusRef.current = document.activeElement as HTMLElement | null;
    const dialog = dialogRef.current;
    const focusable = () => Array.from(dialog?.querySelectorAll<HTMLElement>('button:not([disabled]),a[href],input:not([disabled]),select:not([disabled]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"])') ?? []);
    (focusable()[0] ?? dialog)?.focus();
    const keydown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && dismissible) { event.preventDefault(); close(); return; }
      if (event.key !== "Tab") return;
      const items = focusable(); if (!items.length) { event.preventDefault(); dialog?.focus(); return; }
      const first = items[0], last = items[items.length - 1];
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
      else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
    };
    document.addEventListener("keydown", keydown);
    return () => { document.removeEventListener("keydown", keydown); returnFocusRef.current?.focus(); };
  }, [open, close, dismissible]);
  return open ? <div className="dialog-backdrop" onMouseDown={dismissible?close:undefined}>
    <section ref={dialogRef} tabIndex={-1} role="dialog" aria-modal="true" aria-labelledby={titleId} aria-describedby={description?descriptionId:undefined} className={`scope-dialog dialog-${size}`} onMouseDown={(event) => event.stopPropagation()}>
      <header className="dialog-header"><div><h2 id={titleId}>{title}</h2>{description?<p id={descriptionId}>{description}</p>:null}</div>{dismissible?<button type="button" className="dialog-close" aria-label={closeLabel} onClick={close}>×</button>:null}</header>
      <div className="dialog-content">{children}</div>
    </section>
  </div> : null;
}
export function Drawer(props: Parameters<typeof Dialog>[0]) { return <Dialog {...props}/>; }
export function BottomSheet(props: Parameters<typeof Dialog>[0]) { return <Dialog {...props}/>; }
export function Toast({ children, tone = "info" }: { children: ReactNode; tone?: string }) { return <div role="status" className={`operational-banner ${tone}`}>{children}</div>; }
export function ConfirmDialog({ open, title, body, confirmLabel, cancelLabel="Cancel", pending=false, onConfirm, onClose }: { open: boolean; title: string; body: ReactNode; confirmLabel: string; cancelLabel?:string; pending?:boolean; onConfirm: () => void; onClose: () => void }) { return <Dialog open={open} title={title} onClose={onClose}><div>{body}</div><div className="dialog-actions"><button className="secondary-pill" disabled={pending} onClick={onClose}>{cancelLabel}</button><AsyncButton pending={pending} onClick={onConfirm}>{confirmLabel}</AsyncButton></div></Dialog>; }
export function ValidationSummary({ title="Please correct the following fields", errors, correlationId }: { title?:string; errors:PlatformFieldError[]; correlationId?:string }) { if (!errors.length) return null; return <section className="validation-summary" role="alert" aria-live="assertive"><div className="validation-summary-icon" aria-hidden>!</div><div><strong>{title}</strong><ul>{errors.map((error,index)=><li key={`${error.field}-${index}`}><a href={`#field-${error.field.replace(/[^a-zA-Z0-9_-]/g,"-")}`}><span>{error.field}</span> — {error.message}</a></li>)}</ul>{correlationId?<small className="correlation-id" dir="ltr">Reference: {correlationId}</small>:null}</div></section>; }
export function Skeleton({ width = "100%", height = 16 }: { width?: string; height?: number }) { return <span className="ui-skeleton" style={{ width, height }} aria-hidden/>; }
export function EmptyState({ title, description, action }: { title: string; description: string; action?: ReactNode }) { return <section className="ui-state"><h3>{title}</h3><p>{description}</p>{action}</section>; }
export function ErrorState({ title, description, retry, retryLabel="Retry", correlationId }: { title: string; description: string; retry?: () => void; retryLabel?:string; correlationId?:string }) { return <section className="ui-state" role="alert"><h3>{title}</h3><p>{description}</p>{correlationId?<small className="correlation-id" dir="ltr">{correlationId}</small>:null}{retry ? <Button onClick={retry}>{retryLabel}</Button> : null}</section>; }
export const PermissionState = EmptyState;
export function PlanGate({ allowed, children, fallback }: { allowed: boolean; children: ReactNode; fallback: ReactNode }) { return <>{allowed ? children : fallback}</>; }
export function OfflineIndicator({ offline, stale }: { offline: boolean; stale?: boolean }) { return offline || stale ? <Badge tone="warning">{offline ? "Offline" : "Stale"}</Badge> : null; }
export function PageHeader({ title, description, actions }: { title: string; description?: string; actions?: ReactNode }) { return <header className="page-intro"><div><h1>{title}</h1>{description ? <p>{description}</p> : null}</div>{actions}</header>; }
export function ResponsiveInspector({ children }: { children: ReactNode }) { return <div className="responsive-inspector">{children}</div>; }
export function CodeViewer({ value }: { value: unknown }) { return <pre className="json-view" dir="ltr">{typeof value === "string" ? value : JSON.stringify(value, null, 2)}</pre>; }
export function DataGrid({ columns, rows }: { columns: Array<{ key: string; label: string }>; rows: Array<Record<string, ReactNode>> }) { return <div className="ui-data-grid" role="table"><div role="row" className="ui-data-grid-row">{columns.map((column) => <strong role="columnheader" key={column.key}>{column.label}</strong>)}</div>{rows.map((row, index) => <div role="row" className="ui-data-grid-row" key={index}>{columns.map((column) => <span role="cell" key={column.key}>{row[column.key]}</span>)}</div>)}</div>; }
