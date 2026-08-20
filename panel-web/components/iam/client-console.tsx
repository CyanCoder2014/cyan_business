"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePanel } from "@/components/panel-provider";
import { AsyncButton, Dialog, EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError, platformErrorFromResponse } from "@/lib/api-error";
import { platformFetch } from "@/lib/platform-auth";
import { SearchIcon } from "@/components/nav-icons";

type Client = { tenantKey: string; displayName: string; status: string; createdAt: string };
type Plan = { planKey: string; displayName: string; billingMode: string; active: boolean };
type EffectiveCapability = { key: string; enabled: boolean; source: string; status: string };
type Draft = { tenantKey: string; displayName: string; username: string; email: string; phoneNumber: string; initialPassword: string; mfaRequired: boolean; planKey: string; capabilityKeys: string[] };
const emptyDraft: Draft = { tenantKey: "", displayName: "", username: "", email: "", phoneNumber: "", initialPassword: "", mfaRequired: true, planKey: "", capabilityKeys: [] };

async function json<T>(path: string, init?: RequestInit) {
  const response = await platformFetch(path, init);
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<T>;
}

export function ClientConsole() {
  const { locale } = usePanel();
  const { showToast } = useToast();
  const [clients, setClients] = useState<Client[]>([]);
  const [plans, setPlans] = useState<Plan[]>([]);
  const [capabilities, setCapabilities] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Client | null>(null);
  const [editCapabilities, setEditCapabilities] = useState<string[]>([]);
  const [step, setStep] = useState(0);
  const [query, setQuery] = useState("");
  const [draft, setDraft] = useState<Draft>(emptyDraft);
  const openerRef = useRef<HTMLButtonElement>(null);

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try {
      const [clientList, planList, capabilityList] = await Promise.all([
        json<Client[]>("/api/platform/service/tenant-service/endpoint/clients"),
        json<Plan[]>("/api/platform/service/billing-service/endpoint/billing/plans"),
        json<string[]>("/api/platform/service/tenant-service/endpoint/clients/capabilities/catalog")
      ]);
      const activePlans = planList.filter(plan => plan.active);
      setClients(clientList); setPlans(activePlans); setCapabilities(capabilityList);
      setDraft(current => ({ ...current, planKey: current.planKey || activePlans.find(plan => plan.billingMode === "FREE")?.planKey || "" }));
    } catch (reason) { setError(describeApiError(reason, locale === "fa" ? "بارگذاری مشتریان ناموفق بود" : "Clients could not be loaded").message); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);

  const create = async () => {
    if (pending) return;
    setPending(true); setError(null);
    try {
      await json("/api/platform/service/tenant-service/endpoint/clients", {
        method: "POST", headers: { "Content-Type": "application/json", "Idempotency-Key": crypto.randomUUID() },
        body: JSON.stringify({ tenantKey: draft.tenantKey, displayName: draft.displayName, headUser: { username: draft.username, email: draft.email, phoneNumber: draft.phoneNumber, initialPassword: draft.initialPassword, mfaRequired: draft.mfaRequired }, planKey: draft.planKey, capabilityKeys: draft.capabilityKeys })
      });
      setOpen(false); setStep(0); setDraft(emptyDraft); await load();
    } catch (reason) { const described = describeApiError(reason, locale === "fa" ? "ایجاد مشتری ناموفق بود" : "Could not create client"); setError(described.message); showToast({ tone: "error", title: described.title, message: described.message }); }
    finally { setPending(false); }
  };
  const openCapabilities = async (client: Client) => {
    if (pending) return; setPending(true); setError(null);
    try { const values = await json<EffectiveCapability[]>(`/api/platform/service/tenant-service/endpoint/tenants/${encodeURIComponent(client.tenantKey)}/capabilities`); setEditCapabilities(values.filter(value => value.enabled).map(value => value.key)); setEditing(client); }
    catch (reason) { const described = describeApiError(reason, locale === "fa" ? "بارگذاری دسترسی‌ها ناموفق بود" : "Could not load service access"); setError(described.message); showToast({ tone: "error", title: described.title, message: described.message }); }
    finally { setPending(false); }
  };
  const saveCapabilities = async () => {
    if (!editing || pending) return; setPending(true); setError(null);
    try { await json(`/api/platform/service/tenant-service/endpoint/clients/${encodeURIComponent(editing.tenantKey)}/capabilities`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ capabilityKeys: editCapabilities }) }); setEditing(null); await load(); }
    catch (reason) { const described = describeApiError(reason, locale === "fa" ? "ذخیره دسترسی ناموفق بود" : "Could not save service access"); setError(described.message); showToast({ tone: "error", title: described.title, message: described.message }); }
    finally { setPending(false); }
  };
  const freePlans = useMemo(() => plans.filter(plan => plan.billingMode === "FREE"), [plans]);
  const filteredClients = useMemo(() => { const value = query.trim().toLocaleLowerCase(locale === "fa" ? "fa" : "en"); return value ? clients.filter(client => `${client.displayName} ${client.tenantKey} ${client.status}`.toLocaleLowerCase(locale === "fa" ? "fa" : "en").includes(value)) : clients; }, [clients, locale, query]);
  const close = useCallback(() => { if (!pending) { setOpen(false); setStep(0); window.setTimeout(() => openerRef.current?.focus(), 0); } }, [pending]);

  if (loading) return <div className="access-loading"><Skeleton height={80}/><Skeleton height={260}/></div>;
  if (error && !clients.length) return <ErrorState title={locale === "fa" ? "مشتریان بارگذاری نشدند" : "Clients could not be loaded"} description={error} retry={load}/>;
  return <div className="access-console">
    <div className="access-toolbar"><div className="access-search"><span aria-hidden><SearchIcon size={15}/></span><input aria-label={locale === "fa" ? "جستجوی مشتریان" : "Search clients"} value={query} onChange={event => setQuery(event.target.value)} placeholder={locale === "fa" ? "جستجوی مشتریان" : "Search clients"}/></div><button ref={openerRef} className="primary-pill" onClick={() => setOpen(true)}>＋ {locale === "fa" ? "مشتری جدید" : "New client"}</button></div>
    {error ? <div className="operational-banner error" role="alert">{error}</div> : null}
    {filteredClients.length ? <div className="client-grid">{filteredClients.map(client => <article className="client-card panel-card" key={client.tenantKey}><div className="card-title-row"><span className="client-mark">{client.displayName.slice(0, 2).toUpperCase()}</span><StatusBadge tone={client.status === "ACTIVE" ? "success" : "warning"}>{client.status}</StatusBadge></div><h3>{client.displayName}</h3><code>{client.tenantKey}</code><p>{locale === "fa" ? "مرز مستقل داده و دسترسی" : "Independent data and access boundary"}</p><AsyncButton className="secondary-pill" pending={pending && editing?.tenantKey === client.tenantKey} disabled={pending} onClick={() => openCapabilities(client)}>{locale === "fa" ? "خدمات مجاز" : "Service access"}</AsyncButton></article>)}</div> : <EmptyState title={query ? (locale === "fa" ? "موردی پیدا نشد" : "No matching clients") : (locale === "fa" ? "هنوز مشتری ندارید" : "No clients yet")} description={query ? (locale === "fa" ? "عبارت جستجو را تغییر دهید." : "Try a different search term.") : (locale === "fa" ? "یک فضای کسب‌وکار با کاربر ارشد، خدمات و پلن واقعی بسازید." : "Provision a business tenant with its head user, services, and a real plan.")} action={!query ? <button className="primary-pill" onClick={() => setOpen(true)}>{locale === "fa" ? "ساخت مشتری" : "Create client"}</button> : undefined}/>}
    <Dialog open={open} title={locale === "fa" ? "راه‌اندازی مشتری" : "Provision client"} onClose={close}>
      <div className="drawer-head"><div><p className="page-kicker">{locale === "fa" ? `مرحله ${step + 1} از ۳` : `Step ${step + 1} of 3`}</p></div><button className="header-icon-button" aria-label={locale === "fa" ? "بستن" : "Close"} disabled={pending} onClick={close}>×</button></div>
      <div className="wizard-progress" aria-hidden><i style={{ width: `${(step + 1) / 3 * 100}%` }}/></div>
      {step === 0 ? <div className="access-form"><label><span>{locale === "fa" ? "نام کسب‌وکار" : "Business name"}</span><input autoFocus value={draft.displayName} onChange={event => setDraft(current => ({ ...current, displayName: event.target.value, tenantKey: current.tenantKey || event.target.value.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "") }))}/></label><label><span>{locale === "fa" ? "کلید مشتری" : "Client key"}</span><input dir="ltr" value={draft.tenantKey} onChange={event => setDraft(current => ({ ...current, tenantKey: event.target.value.toLowerCase().replace(/[^a-z0-9-]/g, "") }))}/></label></div> : step === 1 ? <div className="access-form"><label><span>{locale === "fa" ? "نام کاربری مدیر مشتری" : "Head username"}</span><input value={draft.username} onChange={event => setDraft(current => ({ ...current, username: event.target.value }))}/></label><label><span>{locale === "fa" ? "ایمیل" : "Email"}</span><input type="email" value={draft.email} onChange={event => setDraft(current => ({ ...current, email: event.target.value }))}/></label><label><span>{locale === "fa" ? "تلفن" : "Phone"}</span><input dir="ltr" value={draft.phoneNumber} onChange={event => setDraft(current => ({ ...current, phoneNumber: event.target.value }))}/></label><label><span>{locale === "fa" ? "رمز اولیه" : "Initial password"}</span><input type="password" value={draft.initialPassword} onChange={event => setDraft(current => ({ ...current, initialPassword: event.target.value }))}/></label></div> : <div className="access-form"><label><span>{locale === "fa" ? "پلن رایگان قابل فعال‌سازی" : "Activatable free plan"}</span><select value={draft.planKey} onChange={event => setDraft(current => ({ ...current, planKey: event.target.value }))}><option value="">{locale === "fa" ? "انتخاب پلن" : "Select plan"}</option>{freePlans.map(plan => <option key={plan.planKey} value={plan.planKey}>{plan.displayName}</option>)}</select></label><fieldset><legend>{locale === "fa" ? "خدمات مجاز" : "Enabled services"}</legend>{capabilities.map(key => <label className="check-row" key={key}><input type="checkbox" checked={draft.capabilityKeys.includes(key)} onChange={event => setDraft(current => ({ ...current, capabilityKeys: event.target.checked ? [...current.capabilityKeys, key] : current.capabilityKeys.filter(item => item !== key) }))}/><span>{key}</span></label>)}</fieldset>{!freePlans.length ? <div className="operational-banner warning">{locale === "fa" ? "پلن رایگان فعالی وجود ندارد؛ مدیر پلتفرم باید ابتدا یک پلن منتشر کند." : "No active free plan exists. Publish one before provisioning a client."}</div> : null}</div>}
      <div className="dialog-actions"><button className="secondary-pill" disabled={pending || step === 0} onClick={() => setStep(current => current - 1)}>{locale === "fa" ? "قبلی" : "Back"}</button>{step < 2 ? <button className="primary-pill" disabled={(step === 0 && (!draft.displayName || draft.tenantKey.length < 3)) || (step === 1 && (!draft.username || !draft.email || draft.initialPassword.length < 8))} onClick={() => setStep(current => current + 1)}>{locale === "fa" ? "ادامه" : "Continue"}</button> : <AsyncButton pending={pending} disabled={!draft.planKey || !draft.capabilityKeys.length} onClick={create}>{locale === "fa" ? "ایجاد مشتری" : "Create client"}</AsyncButton>}</div>
    </Dialog>
    <Dialog open={Boolean(editing)} title={locale === "fa" ? "دسترسی خدمات مشتری" : "Client service access"} onClose={() => { if (!pending) setEditing(null); }}>
      <div className="access-form"><p><strong>{editing?.displayName}</strong><br/><code>{editing?.tenantKey}</code></p><fieldset><legend>{locale === "fa" ? "قابلیت‌های فعال" : "Enabled capabilities"}</legend>{capabilities.map(key => <label className="check-row" key={key}><input type="checkbox" disabled={pending} checked={editCapabilities.includes(key)} onChange={event => setEditCapabilities(current => event.target.checked ? [...current, key] : current.filter(item => item !== key))}/><span>{key}</span></label>)}</fieldset><div className="dialog-actions"><button className="secondary-pill" disabled={pending} onClick={() => setEditing(null)}>{locale === "fa" ? "لغو" : "Cancel"}</button><AsyncButton pending={pending} disabled={!editCapabilities.length} onClick={saveCapabilities}>{locale === "fa" ? "ذخیره دسترسی" : "Save access"}</AsyncButton></div></div>
    </Dialog>
  </div>;
}
