"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { FormIcon } from "@/components/nav-icons";
import {
  AsyncButton,
  ConfirmDialog,
  Dialog,
  EmptyState,
  ErrorState,
  Field,
  Select,
  Skeleton,
  StatusBadge,
} from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";
import { dynamicServices, listDefinitions } from "@/lib/dynamic-api";
import {
  archiveForm,
  listPublishedForms,
  memberFormPath,
  publicFormPath,
  publishForm,
  type FormVisibility,
  type PublishedFormSummary,
} from "@/lib/forms-api";
import type { DynamicEntityDefinition, DynamicServiceKey } from "@/lib/types";

export default function FormsPage() {
  const { locale } = usePanel();
  const { showToast } = useToast();
  const { tenantKey, siteKey, can } = useScopeAccess();
  const [forms, setForms] = useState<PublishedFormSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialog, setDialog] = useState(false);
  const [pending, setPending] = useState(false);
  const [remove, setRemove] = useState<PublishedFormSummary | null>(null);
  const [service, setService] = useState<DynamicServiceKey>("content-service");
  const [definitions, setDefinitions] = useState<DynamicEntityDefinition[]>([]);
  const [definitionsLoading, setDefinitionsLoading] = useState(false);
  const [entityKey, setEntityKey] = useState("");
  const [slug, setSlug] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [visibility, setVisibility] = useState<FormVisibility>("AUTHENTICATED");
  const scope = useMemo(
    () => tenantKey ? { tenantKey, siteKey: siteKey || undefined } : null,
    [tenantKey, siteKey],
  );
  const canBuild = can("definition.read") && can("definition.manage");
  const activeForms = forms.filter((item) => item.status === "PUBLISHED");

  useEffect(() => {
    const searchParams = new URLSearchParams(window.location.search);
    const requestedService = searchParams.get("serviceKey");
    const requestedEntity = searchParams.get("entityKey");
    if (requestedService && dynamicServices.includes(requestedService as DynamicServiceKey)) setService(requestedService as DynamicServiceKey);
    if (requestedEntity) setEntityKey(requestedEntity);
    if (requestedService || requestedEntity) setDialog(true);
  }, []);

  const load = useCallback(async () => {
    if (!scope) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setForms(await listPublishedForms(scope));
    } catch (cause) {
      const { title, message } = describeApiError(cause, "Forms unavailable"); setError(message); showToast({ tone: "error", title, message });
    } finally {
      setLoading(false);
    }
  }, [scope]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    if (!dialog || !canBuild || !scope) return;
    setDefinitionsLoading(true);
    listDefinitions(service, scope)
      .then(setDefinitions)
      .catch((cause) => { const { message } = describeApiError(cause, "Definitions unavailable"); setError(message); })
      .finally(() => setDefinitionsLoading(false));
  }, [dialog, service, canBuild, scope]);

  const chooseDefinition = (key: string) => {
    setEntityKey(key);
    const found = definitions.find((item) => item.entityKey === key);
    if (found) {
      setTitle(found.title || found.entityKey);
      setSlug(
        found.entityKey
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, "-")
          .replace(/^-|-$/g, ""),
      );
    }
  };

  const save = async () => {
    if (!scope || pending) return;
    setPending(true);
    setError(null);
    try {
      await publishForm(scope, { slug, serviceKey: service, entityKey, title, description, visibility });
      setDialog(false);
      await load();
      showToast({
        tone: "success",
        title: locale === "fa" ? "فرم منتشر شد" : "Form published",
        message: visibility === "PUBLIC" ? publicFormPath(slug) : memberFormPath(slug),
      });
    } catch (cause) {
      const { title, message } = describeApiError(cause, "Publish failed"); setError(message); showToast({ tone: "error", title, message });
    } finally {
      setPending(false);
    }
  };

  const confirmArchive = async () => {
    if (!scope || !remove || pending) return;
    setPending(true);
    try {
      await archiveForm(remove.slug, scope);
      setRemove(null);
      await load();
      showToast({ tone: "success", title: locale === "fa" ? "فرم بایگانی شد" : "Form archived" });
    } catch (cause) {
      const { title, message } = describeApiError(cause, "Archive failed"); setError(message); showToast({ tone: "error", title, message });
    } finally {
      setPending(false);
    }
  };

  return (
    <PanelShell
      activeKey="forms"
      title="Forms"
      titleFa="فرم‌های من"
      subtitle="Open private workspace forms or publish a safe public link."
      subtitleFa="فرم‌های خصوصی فضای کاری را باز کنید یا پیوند عمومی امن منتشر کنید."
    >
      <div className="page-action-bar">
        <StatusBadge tone="info">
          {activeForms.length} {locale === "fa" ? "فرم فعال" : "active forms"}
        </StatusBadge>
        {canBuild ? (
          <button className="primary-pill" onClick={() => setDialog(true)}>
            {locale === "fa" ? "انتشار فرم" : "Publish form"}
          </button>
        ) : null}
      </div>

      {loading ? (
        <div className="result-card-grid"><Skeleton height={180} /><Skeleton height={180} /></div>
      ) : error && !forms.length ? (
        <ErrorState
          title={locale === "fa" ? "فرم‌ها در دسترس نیستند" : "Forms unavailable"}
          description={error}
          retry={load}
        />
      ) : activeForms.length ? (
        <div className="result-card-grid">
          {activeForms.map((form) => {
            const href = form.visibility === "PUBLIC" ? publicFormPath(form.slug) : memberFormPath(form.slug);
            return (
              <article className="result-card" key={form.slug}>
                <header>
                  <span className="result-card-icon" aria-hidden><FormIcon size={18}/></span>
                  <StatusBadge tone={form.visibility === "PUBLIC" ? "success" : "info"}>{form.visibility}</StatusBadge>
                </header>
                <div>
                  <h2>{form.title}</h2>
                  <p>{form.description || `${form.serviceKey} · ${form.entityKey}`}</p>
                  <code dir="ltr">{href}</code>
                </div>
                <footer>
                  <Link className="primary-pill" href={href} target={form.visibility === "PUBLIC" ? "_blank" : undefined}>
                    {locale === "fa" ? "باز کردن فرم" : "Open form"}
                  </Link>
                  <Link className="secondary-pill" href={`/data/${encodeURIComponent(form.serviceKey)}/${encodeURIComponent(form.entityKey)}`}>
                    {locale === "fa" ? "داده" : "Data"}
                  </Link>
                  <Link className="secondary-pill" href={`/bpm/new?entityService=${encodeURIComponent(form.serviceKey)}&entityKey=${encodeURIComponent(form.entityKey)}`}>
                    {locale === "fa" ? "استفاده در BPM" : "Use in BPM"}
                  </Link>
                  <button
                    className="secondary-pill"
                    onClick={() => navigator.clipboard.writeText(`${location.origin}${href}`).then(() => showToast({
                      tone: "success",
                      title: locale === "fa" ? "پیوند کپی شد" : "Link copied",
                    }))}
                  >
                    {locale === "fa" ? "کپی پیوند" : "Copy link"}
                  </button>
                  {canBuild ? (
                    <button className="text-danger-button" onClick={() => setRemove(form)}>
                      {locale === "fa" ? "بایگانی" : "Archive"}
                    </button>
                  ) : null}
                </footer>
              </article>
            );
          })}
        </div>
      ) : (
        <EmptyState
          title={locale === "fa" ? "فرمی برای شما منتشر نشده است" : "No forms are available to you"}
          description={locale === "fa"
            ? "مدیر فضای کاری می‌تواند یک تعریف منتشرشده را به فرم خصوصی یا عمومی تبدیل کند."
            : "A workspace builder can expose a published definition as a private or public form."}
          action={canBuild ? <button className="primary-pill" onClick={() => setDialog(true)}>Publish first form</button> : undefined}
        />
      )}

      {error && forms.length ? <div className="operational-banner error" role="alert">{error}</div> : null}

      <Dialog
        open={dialog}
        title={locale === "fa" ? "انتشار فرم" : "Publish a form"}
        description={locale === "fa"
          ? "فرم خصوصی فقط برای اعضای واردشده قابل دسترسی است. فرم عمومی بدون ورود باز می‌شود."
          : "Private forms require workspace sign-in. Public forms can be opened without an account."}
        onClose={() => !pending && setDialog(false)}
      >
        <div className="phase9-form">
          <Select
            label="Service"
            value={service}
            disabled={pending}
            onChange={(event) => {
              setService(event.target.value as DynamicServiceKey);
              setEntityKey("");
            }}
          >
            {dynamicServices.map((item) => <option key={item}>{item}</option>)}
          </Select>
          <Select
            label={locale === "fa" ? "تعریف منتشرشده" : "Published definition"}
            value={entityKey}
            disabled={pending || definitionsLoading}
            onChange={(event) => chooseDefinition(event.target.value)}
          >
            <option value="">{definitionsLoading ? "Loading…" : "Select"}</option>
            {definitions.filter((item) => item.active).map((item) => (
              <option value={item.entityKey} key={item.entityKey}>{item.title || item.entityKey}</option>
            ))}
          </Select>
          <Field label={locale === "fa" ? "عنوان فرم" : "Form title"} value={title} disabled={pending} onChange={(event) => setTitle(event.target.value)} />
          <Field label={locale === "fa" ? "پیوند" : "Link slug"} dir="ltr" value={slug} disabled={pending} onChange={(event) => setSlug(event.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ""))} />
          <label className="ui-field">
            <span>{locale === "fa" ? "توضیح" : "Description"}</span>
            <textarea value={description} disabled={pending} onChange={(event) => setDescription(event.target.value)} />
          </label>
          <Select label={locale === "fa" ? "دسترسی" : "Access"} value={visibility} disabled={pending} onChange={(event) => setVisibility(event.target.value as FormVisibility)}>
            <option value="AUTHENTICATED">{locale === "fa" ? "خصوصی — اعضای واردشده" : "Private — signed-in members"}</option>
            <option value="PUBLIC">{locale === "fa" ? "عمومی — هرکس با پیوند" : "Public — anyone with the link"}</option>
          </Select>
          {error ? <p className="field-error" role="alert">{error}</p> : null}
          <div className="dialog-actions">
            <button className="secondary-pill" disabled={pending} onClick={() => setDialog(false)}>
              {locale === "fa" ? "لغو" : "Cancel"}
            </button>
            <AsyncButton pending={pending} disabled={!entityKey || title.trim().length < 2 || slug.length < 3} onClick={save}>
              {locale === "fa" ? "انتشار" : "Publish"}
            </AsyncButton>
          </div>
        </div>
      </Dialog>

      <ConfirmDialog
        open={Boolean(remove)}
        title={locale === "fa" ? "فرم بایگانی شود؟" : "Archive this form?"}
        body={locale === "fa"
          ? "پیوند بلافاصله غیرفعال می‌شود؛ داده‌های قبلی حذف نمی‌شوند."
          : "The link stops working immediately. Existing submitted records are not deleted."}
        confirmLabel={locale === "fa" ? "بایگانی" : "Archive"}
        pending={pending}
        onClose={() => !pending && setRemove(null)}
        onConfirm={confirmArchive}
      />
    </PanelShell>
  );
}
