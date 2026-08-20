"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { Dialog, EmptyState, ErrorState, Field, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";
import { createDefinition, createDefinitionFromTemplate, listDefinitions, listTemplates } from "@/lib/dynamic-api";
import { useAvailableDynamicServices } from "@/lib/use-available-services";
import type { DynamicEntityDefinition, DynamicEntityTemplate, DynamicServiceKey } from "@/lib/types";

export default function Definitions() {
  const { locale } = usePanel();
  const router = useRouter();
  const { showToast } = useToast();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const dynamicServices = useAvailableDynamicServices({ tenantKey: tenantKey || undefined, siteKey: siteKey || undefined });
  const [service, setService] = useState<DynamicServiceKey>("content-service");
  const [definitions, setDefinitions] = useState<DynamicEntityDefinition[]>([]);
  const [templates, setTemplates] = useState<DynamicEntityTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [creating, setCreating] = useState<string | null>(null);
  const [blankOpen, setBlankOpen] = useState(false);
  const [blankKey, setBlankKey] = useState("");
  const [blankTitle, setBlankTitle] = useState("");
  const [blankPending, setBlankPending] = useState(false);
  const scope = { tenantKey: tenantKey || undefined, siteKey: siteKey || undefined };

  async function load() {
    if (!tenantKey) return;
    setLoading(true); setError(null);
    try {
      const [d, t] = await Promise.all([listDefinitions(service, scope), listTemplates(service)]);
      setDefinitions(d); setTemplates(t);
    } catch (e) { setError(e instanceof Error ? e.message : "Definitions unavailable"); }
    finally { setLoading(false); }
  }
  useEffect(() => { void load(); }, [service, tenantKey, siteKey, queryVersion]);

  const visible = definitions.filter(x => `${x.entityKey} ${x.title || ""}`.toLowerCase().includes(search.toLowerCase()));
  const keyTaken = (key: string) => definitions.some(x => x.entityKey === key);

  async function fromTemplate(t: DynamicEntityTemplate) {
    const key = window.prompt(locale === "fa" ? "کلید یکتای موجودیت" : "Unique entity key", t.templateKey);
    if (!key) return;
    setCreating(t.templateKey);
    try {
      const value = await createDefinitionFromTemplate(service, t.templateKey, key, scope);
      setDefinitions(v => [value, ...v.filter(x => x.entityKey !== value.entityKey)]);
      showToast({ tone: "success", title: locale === "fa" ? "تعریف ساخته شد" : "Definition created" });
    } catch (e) {
      const described = describeApiError(e, locale === "fa" ? "ساخت ناموفق بود" : "Creation failed");
      setError(described.message);
      showToast({ tone: "error", title: described.title, message: described.message });
    } finally { setCreating(null); }
  }

  async function createBlank() {
    const key = blankKey.trim();
    if (!key || blankPending) return;
    setBlankPending(true);
    try {
      await createDefinition(service, key, { entityKey: key, title: blankTitle.trim() || key, fields: {} }, scope);
      showToast({ tone: "success", title: locale === "fa" ? "تعریف خالی ساخته شد" : "Blank definition created" });
      setBlankOpen(false); setBlankKey(""); setBlankTitle("");
      router.push(`/definitions/${service}/${encodeURIComponent(key)}`);
    } catch (e) {
      const described = describeApiError(e, locale === "fa" ? "ساخت ناموفق بود" : "Creation failed");
      showToast({ tone: "error", title: described.title, message: described.message });
    } finally { setBlankPending(false); }
  }

  return <PanelShell activeKey="maker" title="Definitions & Forms" titleFa="تعریف‌ها و فرم‌ها" subtitle="Browse service-owned schemas and templates." subtitleFa="ساختارها و قالب‌های متعلق به هر سرویس را مدیریت کنید.">
    <div className="definition-catalog">
      <aside>
        <label>Service<select value={service} onChange={e => setService(e.target.value as DynamicServiceKey)}>{dynamicServices.map(s => <option key={s}>{s}</option>)}</select></label>
        <input value={search} onChange={e => setSearch(e.target.value)} placeholder={locale === "fa" ? "جستجوی تعریف" : "Search definitions"}/>
      </aside>
      <section>
        <header><div><h2>{service}</h2><span>{definitions.length} definitions</span></div><button className="primary-pill" onClick={() => setBlankOpen(true)}>＋ {locale === "fa" ? "تعریف خالی" : "Blank definition"}</button></header>
        {loading ? <Skeleton height={240}/> : error ? <ErrorState title="Definitions unavailable" description={error} retry={load}/> : visible.length ? <div className="definition-list">{visible.map(x => <Link key={x.entityKey} href={`/definitions/${service}/${encodeURIComponent(x.entityKey)}`}><div><strong>{x.title || x.entityKey}</strong><span>{x.entityKey}</span></div><StatusBadge tone={x.active ? "success" : "neutral"}>{x.active ? "Active" : "Inactive"}</StatusBadge><span>r{x.revision ?? 0}</span></Link>)}</div> : <EmptyState title={locale === "fa" ? "تعریفی نیست" : "No definitions"} description={locale === "fa" ? "این سرویس در محدوده فعال تعریفی ندارد. یک تعریف خالی بسازید یا از قالب شروع کنید." : "This service returned no scoped definitions. Start blank or from a template."} action={<button className="primary-pill" onClick={() => setBlankOpen(true)}>＋ {locale === "fa" ? "تعریف خالی" : "Blank definition"}</button>}/>}
      </section>
      <aside>
        <h2>{locale === "fa" ? "قالب‌ها" : "Templates"}</h2>
        {templates.map(t => <article key={t.templateKey}><strong>{t.title || t.templateKey}</strong><p>{t.description}</p><button className="secondary-pill" disabled={creating === t.templateKey} onClick={() => fromTemplate(t)}>{locale === "fa" ? "ساخت از قالب" : "Create from template"}</button></article>)}
      </aside>
    </div>
    {blankOpen ? <Dialog open title={locale === "fa" ? "تعریف خالی جدید" : "New blank definition"} description={locale === "fa" ? "بدون قالب شروع کنید و فیلدها را خودتان اضافه کنید." : "Start with no template and add fields yourself in the schema editor."} onClose={() => setBlankOpen(false)}>
      <div className="dialog-form">
        <Field label={locale === "fa" ? "کلید موجودیت" : "Entity key"} dir="ltr" placeholder="my-entity" value={blankKey} error={blankKey.trim() && keyTaken(blankKey.trim()) ? (locale === "fa" ? "این کلید قبلاً استفاده شده است." : "This key already exists.") : undefined} onChange={e => setBlankKey(e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, "-"))}/>
        <Field label={locale === "fa" ? "عنوان" : "Title"} value={blankTitle} onChange={e => setBlankTitle(e.target.value)}/>
        <div className="dialog-actions">
          <button className="secondary-pill" onClick={() => setBlankOpen(false)}>{locale === "fa" ? "لغو" : "Cancel"}</button>
          <button className="primary-pill" disabled={!blankKey.trim() || keyTaken(blankKey.trim()) || blankPending} onClick={createBlank}>{blankPending ? (locale === "fa" ? "در حال ساخت…" : "Creating…") : (locale === "fa" ? "ساخت" : "Create")}</button>
        </div>
      </div>
    </Dialog> : null}
  </PanelShell>;
}
