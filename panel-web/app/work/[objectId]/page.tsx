"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { AsyncButton, Skeleton, StatusBadge } from "@/components/ui/primitives";
import {
  addAttachment, addComment, assignManagedObject, getActiveManagedObjectForm, getManagedObject,
  listAssignmentTargets,
  listAttachments, listComments, listTransitionOptions, renameManagedObject, setManagedObjectLock, submitManagedObjectForm,
  transitionManagedObject, type ManagedObject, type ManagedObjectActiveFormResponse,
  type AssignmentTarget, type ManagedObjectAttachment, type ManagedObjectComment, type TransitionOptionResponse
} from "@/lib/bpm-api";
import { prepareMediaUpload, uploadMediaBytes } from "@/lib/media-api";
import { useScopeAccess } from "@/components/scope-access-provider";
import { usePanel } from "@/components/panel-provider";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError, fieldErrorsByPath, PlatformApiError } from "@/lib/api-error";
import { GeneratedField, fieldDefaults, type Field } from "@/components/forms/generated-field";
import { JsonTreeView } from "@/components/bpm/json-tree-view";

type AssigneeType = "USER" | "ROLE" | "GROUP";
type TransitionHistoryEntry = {
  transitionId?: string;
  label?: string;
  fromState?: string;
  toState?: string;
  actorUserId?: string;
  timestamp?: string;
  decision?: string | null;
  note?: string | null;
  formKey?: string | null;
  processorKey?: string | null;
  submittedFormId?: string | null;
  submittedFormData?: Record<string, unknown> | null;
};

export default function WorkItem({ params }: { params: { objectId: string } }) {
  const { locale } = usePanel();
  const { tenantKey, siteKey } = useScopeAccess();
  const { showToast } = useToast();
  const scope = useMemo(() => ({ tenantKey: tenantKey ?? undefined, siteKey: siteKey ?? undefined }), [tenantKey, siteKey]);
  const [item, setItem] = useState<ManagedObject | null>(null);
  const [form, setForm] = useState<ManagedObjectActiveFormResponse | null>(null);
  const [options, setOptions] = useState<TransitionOptionResponse[]>([]);
  const [comments, setComments] = useState<ManagedObjectComment[]>([]);
  const [attachments, setAttachments] = useState<ManagedObjectAttachment[]>([]);
  const [values, setValues] = useState<Record<string, unknown>>({});
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [comment, setComment] = useState("");
  const [assignee, setAssignee] = useState("");
  const [assigneeType, setAssigneeType] = useState<AssigneeType>("USER");
  const [targets, setTargets] = useState<AssignmentTarget[]>([]);
  const [pending, setPending] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [uploadProgress, setUploadProgress] = useState<number | null>(null);
  const [titleEditing, setTitleEditing] = useState(false);
  const [titleDraft, setTitleDraft] = useState("");

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
    } catch (reason) { const { title, message } = describeApiError(reason, "Work item unavailable"); setError(message); showToast({ tone: "error", title, message }); }
  }, [params.objectId, scope, tenantKey, showToast]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => {
    if (!tenantKey) return;
    const timer = setTimeout(() => {
      void listAssignmentTargets(assigneeType, assignee, scope).then(setTargets).catch(() => setTargets([]));
    }, 180);
    return () => clearTimeout(timer);
  }, [assignee, assigneeType, scope, tenantKey]);
  const fields = useMemo(() => {
    const definition = form?.rendererDefinition?.definition as Record<string, unknown> | undefined;
    const raw = definition?.fields;
    return raw && typeof raw === "object" && !Array.isArray(raw) ? raw as Record<string, Field> : {};
  }, [form]);

  const action = async (key: string, operation: () => Promise<unknown>) => {
    if (pending) return;
    setPending(key); setError(null);
    try { await operation(); await load(); }
    catch (reason) { const { title, message } = describeApiError(reason, "Action failed"); setError(message); showToast({ tone: "error", title, message }); }
    finally { setPending(null); }
  };
  const submit = async (nextState?: string) => {
    if (!item || pending) return;
    const key = nextState ? `submit-transition-${nextState}` : "submit";
    setPending(key); setError(null); setFormErrors({});
    try {
      await submitManagedObjectForm(item.id, { formData: values, nextState }, scope);
      showToast({ tone: "success", title: locale === "fa" ? "ثبت شد" : "Submitted" });
      await load();
    } catch (reason) {
      setFormErrors(fieldErrorsByPath(reason));
      const { title, message } = describeApiError(reason, "Submit failed");
      setError(message);
      showToast({ tone: "error", title, message });
    } finally { setPending(null); }
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
    } catch (reason) { const { title, message } = describeApiError(reason, "Upload failed"); setError(message); showToast({ tone: "error", title, message }); }
    finally { setPending(null); setUploadProgress(null); }
  };
  const saveTitle = async () => {
    if (!item || pending) return;
    setPending("rename"); setError(null);
    try {
      await renameManagedObject(item.id, titleDraft, scope);
      setTitleEditing(false);
      await load();
    } catch (reason) { const { title, message } = describeApiError(reason, "Rename failed"); setError(message); showToast({ tone: "error", title, message }); }
    finally { setPending(null); }
  };

  return <PanelShell activeKey="work" title={item?.title || "Work item"} titleFa={item?.title || "مورد کاری"} subtitle={params.objectId} subtitleFa={params.objectId}>
    {error ? <div className="operational-banner error" role="alert"><span>{error}</span><button aria-label={locale === "fa" ? "بستن خطا" : "Dismiss error"} onClick={() => setError(null)}>×</button></div> : null}
    {!item || !form ? <Skeleton height={600}/> : <div className="work-item-layout">
      <main>
        <div className="page-action-bar">
          <div>
            <StatusBadge tone="info">{item.state}</StatusBadge>
            <StatusBadge tone={item.priority === "URGENT" ? "danger" : "neutral"}>{item.priority ?? "NORMAL"}</StatusBadge>
            {item.locked ? <span>{locale === "fa" ? `قفل‌شده توسط ${item.lockedBy ?? "—"}` : `Locked by ${item.lockedBy ?? "—"}`}</span> : null}
          </div>
          <div><AsyncButton className="secondary-pill" pending={pending === "lock"} disabled={Boolean(pending && pending !== "lock")} onClick={() => action("lock", () => setManagedObjectLock(item.id, !item.locked, scope))}>{item.locked ? (locale === "fa" ? "بازکردن قفل" : "Unlock") : (locale === "fa" ? "قفل" : "Lock")}</AsyncButton></div>
        </div>
        <section className="work-item-title-card">
          {titleEditing ? <div className="work-item-title-edit">
            <input dir="auto" autoFocus value={titleDraft} placeholder={locale === "fa" ? "عنوان مورد کاری" : "Work item title"} onChange={event => setTitleDraft(event.target.value)}/>
            <AsyncButton pending={pending === "rename"} onClick={saveTitle}>{locale === "fa" ? "ذخیره" : "Save"}</AsyncButton>
            <button className="secondary-pill" disabled={Boolean(pending)} onClick={() => setTitleEditing(false)}>{locale === "fa" ? "لغو" : "Cancel"}</button>
          </div> : <div className="work-item-title-view">
            <h2>{item.title || (locale === "fa" ? "بدون عنوان" : "Untitled")}<small>{item.objectType}</small></h2>
            <button className="secondary-pill" onClick={() => { setTitleDraft(item.title ?? ""); setTitleEditing(true); }}>{locale === "fa" ? "ویرایش عنوان" : "Edit title"}</button>
          </div>}
        </section>
        <section className="active-form">
          <header><div><h2>{form.formKey ?? (locale === "fa" ? "فرم فعال" : "Active form")}</h2><p>{form.entityService} · {form.entityKey}</p></div></header>
          {Object.keys(fields).length
            ? <div className="generated-work-form">{Object.entries(fields).map(([name, field]) => <GeneratedField key={name} path={name} name={name} field={field} value={values[name]} errors={formErrors} scope={tenantKey ? { tenantKey, siteKey: siteKey ?? undefined } : undefined} onChange={(value) => setValues(current => ({ ...current, [name]: value }))}/>)}</div>
            : <JsonTreeView value={values} emptyLabel={locale === "fa" ? "این فرم فیلدی ندارد." : "This form has no fields."}/>}
          <div className="work-item-submit-row">
            <AsyncButton pending={pending === "submit"} disabled={Boolean(pending && pending !== "submit")} onClick={() => submit()}>{locale === "fa" ? "ثبت فرم" : "Submit form"}</AsyncButton>
            {options.map(option => <AsyncButton key={option.transitionId} className="secondary-pill" pending={pending === `submit-transition-${option.toState}`} disabled={Boolean(pending && pending !== `submit-transition-${option.toState}`)} onClick={() => submit(option.toState)}>{locale === "fa" ? `ثبت و انتقال به ${option.label}` : `Submit & ${option.label}`}</AsyncButton>)}
          </div>
        </section>
        <section className="transition-actions"><h2>{locale === "fa" ? "اقدام‌های مجاز" : "Available transitions"}</h2>{options.length ? <div>{options.map(option => <AsyncButton key={option.transitionId} pending={pending === `transition-${option.transitionId}`} disabled={Boolean(pending && pending !== `transition-${option.transitionId}`)} className="secondary-pill" onClick={() => action(`transition-${option.transitionId}`, () => transitionManagedObject(item.id, { nextState: option.toState, context: {} }, scope))}>{option.label}</AsyncButton>)}</div> : <p>{locale === "fa" ? "انتقال مجازی نیست." : "No transition is currently allowed."}</p>}</section>
        <section><h2>{locale === "fa" ? "بار داده" : "Payload"}</h2><JsonTreeView value={item.payload} emptyLabel={locale === "fa" ? "داده‌ای نیست." : "No payload data."}/></section>
        <section><h2>{locale === "fa" ? "تاریخچه" : "History"}</h2><TransitionHistory transitions={(item.transitionHistory ?? []) as TransitionHistoryEntry[]} auditLog={item.auditLog ?? []} automation={item.automationBlockRegistry ?? []} locale={locale}/></section>
      </main>
      <aside className="work-collaboration">
        <section><h2>{locale === "fa" ? "مسئول" : "Assignment"}</h2><label><span>{locale === "fa" ? "نوع مسئول" : "Assignee type"}</span><select value={assigneeType} disabled={Boolean(pending)} onChange={event => { setAssigneeType(event.target.value as AssigneeType); setAssignee(""); }}><option value="USER">{locale === "fa" ? "کاربر" : "User"}</option><option value="ROLE">{locale === "fa" ? "نقش" : "Role"}</option><option value="GROUP">{locale === "fa" ? "گروه" : "Group"}</option></select></label><label><span>{locale === "fa" ? "جستجو و انتخاب مسئول" : "Find and select assignee"}</span><input dir="ltr" list="assignment-targets" value={assignee} disabled={Boolean(pending)} onChange={event => setAssignee(event.target.value)} placeholder={item.assignee ?? (assigneeType === "USER" ? "username" : assigneeType.toLowerCase())}/><datalist id="assignment-targets">{targets.filter(target => target.active).map(target => <option key={`${target.type}-${target.key}`} value={target.key}>{target.displayName}</option>)}</datalist></label><small>{locale === "fa" ? "کاربران و نقش‌ها از فهرست معتبر همین مشتری خوانده می‌شوند." : "Users and roles are resolved from this tenant’s authoritative directory."}</small><AsyncButton pending={pending === "assign"} disabled={!targets.some(target => target.active && target.key === assignee.trim()) || Boolean(pending && pending !== "assign")} onClick={() => action("assign", () => assignManagedObject(item.id, assignee.trim(), assigneeType, scope))}>{locale === "fa" ? "تخصیص" : "Assign"}</AsyncButton></section>
        <section><h2>{locale === "fa" ? "نظرها" : "Comments"}</h2><div className="comment-list">{comments.map(entry => <article key={entry.id}><strong>{entry.authorUserId ?? "—"}</strong><p>{entry.body}</p><time>{entry.createdAt ? new Date(entry.createdAt).toLocaleString(locale === "fa" ? "fa-IR" : "en") : ""}</time></article>)}</div><textarea value={comment} disabled={Boolean(pending)} onChange={event => setComment(event.target.value)} placeholder={locale === "fa" ? "نظر بنویسید" : "Write a comment"}/><AsyncButton pending={pending === "comment"} disabled={!comment.trim() || Boolean(pending && pending !== "comment")} onClick={() => action("comment", async () => { await addComment(item.id, comment, scope); setComment(""); })}>{locale === "fa" ? "ارسال نظر" : "Add comment"}</AsyncButton></section>
        <section><h2>{locale === "fa" ? "پیوست‌ها" : "Attachments"}</h2>{attachments.map(attachment => <article key={attachment.id}><strong>{attachment.fileName ?? attachment.assetKey}</strong><small>{attachment.contentType}</small></article>)}<label className="attachment-picker"><span>{pending === "upload" ? (locale === "fa" ? `بارگذاری ${uploadProgress ?? 0}٪` : `Uploading ${uploadProgress ?? 0}%`) : (locale === "fa" ? "افزودن فایل" : "Add file")}</span><input type="file" disabled={Boolean(pending)} onChange={event => { const file = event.target.files?.[0]; if (file) void upload(file); }}/></label></section>
      </aside>
    </div>}
  </PanelShell>;
}

function TransitionHistory({ transitions, auditLog, automation, locale }: { transitions: TransitionHistoryEntry[]; auditLog: string[]; automation: Array<Record<string, unknown>>; locale: string }) {
  const [openId, setOpenId] = useState<string | null>(null);
  if (!transitions.length && !auditLog.length && !automation.length) return <p className="muted">{locale === "fa" ? "تاریخچه‌ای نیست." : "No history yet."}</p>;
  return <div className="transition-history">
    {transitions.length ? <div className="transition-history-timeline">
      {[...transitions].reverse().map((entry, index) => {
        const id = entry.transitionId ?? String(index);
        const open = openId === id;
        const hasDetail = Boolean(entry.decision || entry.note || entry.submittedFormId || entry.submittedFormData || entry.formKey || entry.processorKey);
        return <article key={id} className="transition-history-entry">
          <button type="button" className="transition-history-head" aria-expanded={open} disabled={!hasDetail} onClick={() => setOpenId(open ? null : id)}>
            <span className="transition-history-dot" aria-hidden/>
            <div>
              <strong>{entry.label || (locale === "fa" ? "انتقال" : "Transition")}</strong>
              <span>{entry.fromState} → {entry.toState}</span>
            </div>
            <div className="transition-history-meta">
              <span>{entry.actorUserId ?? "—"}</span>
              <time>{entry.timestamp ? new Date(entry.timestamp).toLocaleString(locale === "fa" ? "fa-IR" : "en") : ""}</time>
            </div>
            {hasDetail ? <span className="transition-history-caret" aria-hidden>{open ? "▾" : "▸"}</span> : null}
          </button>
          {open && hasDetail ? <div className="transition-history-detail">
            {entry.decision ? <div><span>{locale === "fa" ? "تصمیم" : "Decision"}</span><strong>{entry.decision}</strong></div> : null}
            {entry.note ? <div><span>{locale === "fa" ? "یادداشت" : "Note"}</span><strong>{entry.note}</strong></div> : null}
            {entry.formKey ? <div><span>{locale === "fa" ? "کلید فرم" : "Form key"}</span><strong>{entry.formKey}</strong></div> : null}
            {entry.processorKey ? <div><span>{locale === "fa" ? "پردازشگر" : "Processor"}</span><strong>{entry.processorKey}</strong></div> : null}
            {entry.submittedFormId ? <div><span>{locale === "fa" ? "شناسه ثبت" : "Submission id"}</span><strong dir="ltr">{entry.submittedFormId}</strong></div> : null}
            {entry.submittedFormData ? <div className="transition-history-payload"><span>{locale === "fa" ? "داده فرم ارسالی" : "Submitted form data"}</span><JsonTreeView value={entry.submittedFormData}/></div> : null}
          </div> : null}
        </article>;
      })}
    </div> : null}
    {automation.length ? <details className="transition-history-section"><summary>{locale === "fa" ? "اجراهای اتوماسیون" : "Automation runs"} ({automation.length})</summary><JsonTreeView value={automation}/></details> : null}
    {auditLog.length ? <details className="transition-history-section"><summary>{locale === "fa" ? "گزارش رویداد خام" : "Raw audit log"} ({auditLog.length})</summary><ul className="transition-history-audit">{auditLog.map((line, index) => <li key={index}>{line}</li>)}</ul></details> : null}
  </div>;
}
