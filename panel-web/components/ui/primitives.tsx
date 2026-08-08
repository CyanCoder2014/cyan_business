"use client";

import { useId, type ButtonHTMLAttributes, type HTMLAttributes, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes } from "react";

export function Button(props: ButtonHTMLAttributes<HTMLButtonElement>) { return <button {...props} className={`primary-pill ${props.className ?? ""}`} />; }
export function AsyncButton({ pending=false, pendingLabel="Working…", children, disabled, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { pending?:boolean; pendingLabel?:string }) {
  return <button {...props} disabled={disabled||pending} aria-busy={pending} className={`primary-pill async-button ${props.className??""}`}>{pending?<span className="button-spinner" aria-hidden/>:null}<span>{pending?pendingLabel:children}</span></button>;
}
export function IconButton({ label, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { label: string }) { return <button {...props} aria-label={label} className={`header-icon-button ${props.className ?? ""}`} />; }
export function Field({ label, hint, error, ...props }: InputHTMLAttributes<HTMLInputElement> & { label: string; hint?: string; error?: string }) { const id = useId(); return <label className="ui-field" htmlFor={id}><span>{label}</span><input {...props} id={id}/>{error ? <small className="field-error">{error}</small> : hint ? <small>{hint}</small> : null}</label>; }
export function Select({ label, children, ...props }: SelectHTMLAttributes<HTMLSelectElement> & { label: string; children: ReactNode }) { const id = useId(); return <label className="ui-field" htmlFor={id}><span>{label}</span><select {...props} id={id}>{children}</select></label>; }
export function Combobox(props: InputHTMLAttributes<HTMLInputElement> & { label: string; options: string[] }) { const id = useId(); return <label className="ui-field"><span>{props.label}</span><input {...props} list={id}/><datalist id={id}>{props.options.map((option) => <option key={option} value={option}/>)}</datalist></label>; }
export function Tabs({ items, active, onChange }: { items: Array<{ key: string; label: string }>; active: string; onChange: (key: string) => void }) { return <div className="ui-tabs" role="tablist">{items.map((item) => <button role="tab" aria-selected={active === item.key} key={item.key} onClick={() => onChange(item.key)}>{item.label}</button>)}</div>; }
export const SegmentedControl = Tabs;
export function Badge({ children, tone = "neutral" }: { children: ReactNode; tone?: string }) { return <span className={`status-pill ${tone}`}>{children}</span>; }
export const StatusBadge = Badge;
export function Card(props: HTMLAttributes<HTMLElement>) { return <section {...props} className={`panel-card ${props.className ?? ""}`} />; }
export function Dialog({ open, title, children, onClose }: { open: boolean; title: string; children: ReactNode; onClose: () => void }) { return open ? <div className="dialog-backdrop" onMouseDown={onClose}><section role="dialog" aria-modal="true" aria-label={title} className="scope-dialog" onMouseDown={(event) => event.stopPropagation()}><h2>{title}</h2>{children}</section></div> : null; }
export function Drawer(props: Parameters<typeof Dialog>[0]) { return <Dialog {...props}/>; }
export function BottomSheet(props: Parameters<typeof Dialog>[0]) { return <Dialog {...props}/>; }
export function Toast({ children, tone = "info" }: { children: ReactNode; tone?: string }) { return <div role="status" className={`operational-banner ${tone}`}>{children}</div>; }
export function ConfirmDialog({ open, title, body, confirmLabel, onConfirm, onClose }: { open: boolean; title: string; body: ReactNode; confirmLabel: string; onConfirm: () => void; onClose: () => void }) { return <Dialog open={open} title={title} onClose={onClose}><div>{body}</div><div className="dialog-actions"><button className="secondary-pill" onClick={onClose}>Cancel</button><button className="primary-pill" onClick={onConfirm}>{confirmLabel}</button></div></Dialog>; }
export function Skeleton({ width = "100%", height = 16 }: { width?: string; height?: number }) { return <span className="ui-skeleton" style={{ width, height }} aria-hidden/>; }
export function EmptyState({ title, description, action }: { title: string; description: string; action?: ReactNode }) { return <section className="ui-state"><h3>{title}</h3><p>{description}</p>{action}</section>; }
export function ErrorState({ title, description, retry }: { title: string; description: string; retry?: () => void }) { return <section className="ui-state" role="alert"><h3>{title}</h3><p>{description}</p>{retry ? <Button onClick={retry}>Retry</Button> : null}</section>; }
export const PermissionState = EmptyState;
export function PlanGate({ allowed, children, fallback }: { allowed: boolean; children: ReactNode; fallback: ReactNode }) { return <>{allowed ? children : fallback}</>; }
export function OfflineIndicator({ offline, stale }: { offline: boolean; stale?: boolean }) { return offline || stale ? <Badge tone="warning">{offline ? "Offline" : "Stale"}</Badge> : null; }
export function PageHeader({ title, description, actions }: { title: string; description?: string; actions?: ReactNode }) { return <header className="page-intro"><div><h1>{title}</h1>{description ? <p>{description}</p> : null}</div>{actions}</header>; }
export function ResponsiveInspector({ children }: { children: ReactNode }) { return <div className="responsive-inspector">{children}</div>; }
export function CodeViewer({ value }: { value: unknown }) { return <pre className="json-view" dir="ltr">{typeof value === "string" ? value : JSON.stringify(value, null, 2)}</pre>; }
export function DataGrid({ columns, rows }: { columns: Array<{ key: string; label: string }>; rows: Array<Record<string, ReactNode>> }) { return <div className="ui-data-grid" role="table"><div role="row" className="ui-data-grid-row">{columns.map((column) => <strong role="columnheader" key={column.key}>{column.label}</strong>)}</div>{rows.map((row, index) => <div role="row" className="ui-data-grid-row" key={index}>{columns.map((column) => <span role="cell" key={column.key}>{row[column.key]}</span>)}</div>)}</div>; }
