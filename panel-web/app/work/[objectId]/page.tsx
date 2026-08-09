"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { AsyncButton, CodeViewer, Skeleton, StatusBadge } from "@/components/ui/primitives";
import {
  addAttachment, addComment, assignManagedObject, getActiveManagedObjectForm, getManagedObject,
  listAttachments, listComments, listTransitionOptions, setManagedObjectLock, submitManagedObjectForm,
  transitionManagedObject, type ManagedObject, type ManagedObjectActiveFormResponse,
  type ManagedObjectAttachment, type ManagedObjectComment, type TransitionOptionResponse
} from "@/lib/bpm-api";
import { prepareMediaUpload, uploadMediaBytes } from "@/lib/media-api";
import { useScopeAccess } from "@/components/scope-access-provider";
import { usePanel } from "@/components/panel-provider";

type Field = { type?: string; required?: boolean; label?: string };
type AssigneeType = "USER" | "ROLE" | "GROUP";

export default function WorkItem({ params }: { params: { objectId: string } }) {
  const { locale } = usePanel();
  const { tenantKey, siteKey } = useScopeAccess();
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }), [tenantKey, siteKey]);
  const [item, setItem] = useState<ManagedObject | null>(null);
  const [form, setForm] = useState<ManagedObjectActiveFormResponse | null>(null);
  const [options, setOptions] = useState<TransitionOptionResponse[]>([]);
  const [comments, setComments] = useState<ManagedObjectComment[]>([]);
  const [attachments, setAttachments] = useState<ManagedObjectAttachment[]>([]);
  const [values, setValues] = useState<Record<string, unknown>>({});
  const [comment, setComment] = useState("");
  const [assignee, setAssignee] = useState("");
  const [assigneeType, setAssigneeType] = useState<AssigneeType>("USER");
  const [pending, setPending] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [uploadProgress, setUploadProgress] = useState<number | null>(null);

  const load = useCallback(async () => {
    if (!tenantKey) return;
    setError(null);
    try {
      const [object, activeForm, transitions, objectComments, objectAttachments] = await Promise.all([
        getManagedObject(params.objectId, scope), getActiveManagedObjectForm(params.objectId, scope),
        listTransitionOptions(params.objectId, scope), listComments(params.objectId, scope),
        listAttachments(params.objectId, scope)
      ]);
      setItem(object); setForm(activeForm); setOptions(transitions); setComments(objectComments); setAttachments(objectAttachments);
      setValues((object.payload?.currentFormValues as Record<string, unknown>) ?? {});
      setAssigneeType(object.assigneeType ?? "USER");
    } catch (reason) { setError(reason instanceof Error ? reason.message : String(reason)); }
  }, [params.objectId, scope, tenantKey]);

  useEffect(() => { void load(); }, [load]);
  const fields = useMemo(() => {
    const raw = form?.rendererDefinition?.fields;
    return raw && typeof raw === "object" && !Array.isArray(raw) ? raw as Record<string, Field> : {};
  }, [form]);

  const action = async (key: string, operation: () => Promise<unknown>) => {
    if (pending) return;
    setPending(key); setError(null);
    try { await operation(); await load(); }
    catch (reason) { setError(reason instanceof Error ? reason.message : String(reason)); }
    finally { setPending(null); }
  };
  const upload = async (file: File) => {
    if (!tenantKey || pending) return;
    setPending("upload"); setUploadProgress(0); setError(null);
    try {
      const mediaScope = { tenantKey, siteKey: siteKey ?? undefined };
      const prepared = await prepareMediaUpload(file, mediaScope);
      const uploaded = await uploadMediaBytes(file, prepared, mediaScope, setUploadProgress);
      await addAttachment(params.objectId, { assetKey: uploaded.assetKey, fileName: file.name, contentType: file.type || "application/octet-stream", sizeBytes: file.size, downloadUrl: uploaded.deliveryUrl }, scope);
      await load();
    } catch (reason) { setError(reason instanceof Error ? reason.message : String(reason)); }
    finally { setPending(null); setUploadProgress(null); }
  };

  return <PanelShell activeKey="flows" title="Work item" titleFa="مورد کاری" subtitle={params.objectId} subtitleFa={params.objectId}>
    {error ? <div className="operational-banner error" role="alert"><span>{error}</span><button aria-label={locale === "fa" ? "بستن خطا" : "Dismiss error"} onClick={() => setError(null)}>×</button></div> : null}
    {!item || !form ? <Skeleton height={600}/> : <div className="work-item-layout">
      <main>
        <div className="page-action-bar"><div><StatusBadge tone="info">{item.state}</StatusBadge><StatusBadge tone={item.priority === "URGENT" ? "danger" : "neutral"}>{item.priority ?? "NORMAL"}</StatusBadge>{item.locked ? <span>{locale === "fa" ? `قفل‌شده توسط ${item.lockedBy ?? "—"}` : `Locked by ${item.lockedBy ?? "—"}`}</span> : null}</div><div><AsyncButton className="secondary-pill" pending={pending === "lock"} disabled={Boolean(pending && pending !== "lock")} onClick={() => action("lock", () => setManagedObjectLock(item.id, !item.locked, scope))}>{item.locked ? (locale === "fa" ? "بازکردن قفل" : "Unlock") : (locale === "fa" ? "قفل" : "Lock")}</AsyncButton></div></div>
        <section className="active-form"><header><div><h2>{form.formKey ?? (locale === "fa" ? "فرم فعال" : "Active form")}</h2><p>{form.entityService} · {form.entityKey}</p></div></header>{Object.keys(fields).length ? <div className="generated-work-form">{Object.entries(fields).map(([name, field]) => <WorkField key={name} name={name} field={field} value={values[name]} onChange={(value) => setValues(current => ({ ...current, [name]: value }))}/>)}</div> : <CodeViewer value={values}/>}<AsyncButton pending={pending === "submit"} disabled={Boolean(pending && pending !== "submit")} onClick={() => action("submit", () => submitManagedObjectForm(item.id, { formData: values }, scope))}>{locale === "fa" ? "ثبت فرم" : "Submit form"}</AsyncButton></section>
        <section className="transition-actions"><h2>{locale === "fa" ? "اقدام‌های مجاز" : "Available transitions"}</h2>{options.length ? <div>{options.map(option => <AsyncButton key={option.transitionId} pending={pending === `transition-${option.transitionId}`} disabled={Boolean(pending && pending !== `transition-${option.transitionId}`)} className="secondary-pill" onClick={() => action(`transition-${option.transitionId}`, () => transitionManagedObject(item.id, { nextState: option.toState, context: {} }, scope))}>{option.label}</AsyncButton>)}</div> : <p>{locale === "fa" ? "انتقال مجازی نیست." : "No transition is currently allowed."}</p>}</section>
        <section><h2>{locale === "fa" ? "تاریخچه" : "History"}</h2><CodeViewer value={{ audit: item.auditLog, transitions: item.transitionHistory, automation: item.automationBlockRegistry }}/></section>
      </main>
      <aside className="work-collaboration">
        <section><h2>{locale === "fa" ? "مسئول" : "Assignment"}</h2><label><span>{locale === "fa" ? "نوع مسئول" : "Assignee type"}</span><select value={assigneeType} disabled={Boolean(pending)} onChange={event => setAssigneeType(event.target.value as AssigneeType)}><option value="USER">{locale === "fa" ? "کاربر" : "User"}</option><option value="ROLE">{locale === "fa" ? "نقش" : "Role"}</option><option value="GROUP">{locale === "fa" ? "گروه" : "Group"}</option></select></label><label><span>{locale === "fa" ? "کلید مسئول" : "Assignee key"}</span><input dir="ltr" value={assignee} disabled={Boolean(pending)} onChange={event => setAssignee(event.target.value)} placeholder={item.assignee ?? (assigneeType === "USER" ? "username" : assigneeType.toLowerCase())}/></label><small>{locale === "fa" ? "کلید باید دقیقاً با کاربر، نقش یا گروه موجود مطابقت داشته باشد." : "The key must exactly match an existing user, role, or group."}</small><AsyncButton pending={pending === "assign"} disabled={!assignee.trim() || Boolean(pending && pending !== "assign")} onClick={() => action("assign", () => assignManagedObject(item.id, assignee.trim(), assigneeType, scope))}>{locale === "fa" ? "تخصیص" : "Assign"}</AsyncButton></section>
        <section><h2>{locale === "fa" ? "نظرها" : "Comments"}</h2><div className="comment-list">{comments.map(entry => <article key={entry.id}><strong>{entry.authorUserId ?? "—"}</strong><p>{entry.body}</p><time>{entry.createdAt ? new Date(entry.createdAt).toLocaleString(locale === "fa" ? "fa-IR" : "en") : ""}</time></article>)}</div><textarea value={comment} disabled={Boolean(pending)} onChange={event => setComment(event.target.value)} placeholder={locale === "fa" ? "نظر بنویسید" : "Write a comment"}/><AsyncButton pending={pending === "comment"} disabled={!comment.trim() || Boolean(pending && pending !== "comment")} onClick={() => action("comment", async () => { await addComment(item.id, comment, scope); setComment(""); })}>{locale === "fa" ? "ارسال نظر" : "Add comment"}</AsyncButton></section>
        <section><h2>{locale === "fa" ? "پیوست‌ها" : "Attachments"}</h2>{attachments.map(attachment => <article key={attachment.id}><strong>{attachment.fileName ?? attachment.assetKey}</strong><small>{attachment.contentType}</small></article>)}<label className="attachment-picker"><span>{pending === "upload" ? (locale === "fa" ? `بارگذاری ${uploadProgress ?? 0}٪` : `Uploading ${uploadProgress ?? 0}%`) : (locale === "fa" ? "افزودن فایل" : "Add file")}</span><input type="file" disabled={Boolean(pending)} onChange={event => { const file = event.target.files?.[0]; if (file) void upload(file); }}/></label></section>
      </aside>
    </div>}
  </PanelShell>;
}

function WorkField({ name, field, value, onChange }: { name: string; field: Field; value: unknown; onChange: (value: unknown) => void }) {
  if (field.type === "boolean") return <label className="generated-field"><input type="checkbox" checked={Boolean(value)} onChange={event => onChange(event.target.checked)}/><span>{field.label ?? name}</span></label>;
  if (field.type === "object" || field.type === "list") return <label className="generated-field"><span>{field.label ?? name}</span><textarea dir="ltr" value={value == null ? "" : JSON.stringify(value, null, 2)} onChange={event => { try { onChange(JSON.parse(event.target.value)); } catch { /* Preserve invalid draft until it is valid JSON. */ } }}/></label>;
  return <label className="generated-field"><span>{field.label ?? name}{field.required ? " *" : ""}</span><input required={field.required} type={field.type === "number" || field.type === "integer" ? "number" : field.type === "date" ? "date" : "text"} value={value == null ? "" : String(value)} onChange={event => onChange(field.type === "number" || field.type === "integer" ? Number(event.target.value) : event.target.value)}/></label>;
}
