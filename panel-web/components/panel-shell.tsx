"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { getPlatformAuthToken, logoutPlatformSession, platformFetch, redirectToAuth } from "@/lib/platform-auth";
import { NotificationCenter } from "@/components/notifications/notification-center";
import { AsyncButton } from "@/components/ui/primitives";

type PanelShellProps = { title: string; titleFa: string; subtitle: string; subtitleFa: string; kicker?: string; kickerFa?: string; activeKey: string; children: ReactNode };
type NavItem = { href: string; key: string; icon: string; en: string; fa: string; capability?: string; permission?: string; platformOnly?: boolean };

const groups: Array<{ en: string; fa: string; items: NavItem[] }> = [
  { en: "Workspace", fa: "فضای کار", items: [
    { href: "/dashboard", key: "dashboard", icon: "⌂", en: "Home", fa: "خانه" },
    { href: "/ai", key: "studio", icon: "✦", en: "AI Studio", fa: "استودیوی هوش", capability: "ai-orchestrator", permission: "project.create" },
    { href: "/projects", key: "blueprints", icon: "▦", en: "Projects", fa: "پروژه‌ها", capability: "ai-orchestrator", permission: "project.read" }
  ]},
  { en: "Build", fa: "ساخت", items: [
    { href: "/definitions", key: "maker", icon: "✎", en: "Definitions", fa: "تعریف‌ها", capability: "dynamic-entities", permission: "definition.read" },
    { href: "/data", key: "data", icon: "◫", en: "Data", fa: "داده‌ها", capability: "dynamic-entities", permission: "record.read" },
    { href: "/bpm", key: "flows", icon: "⌁", en: "BPM", fa: "فرایندها", capability: "bpm", permission: "bpm.read" },
    { href: "/automations", key: "automation", icon: "↯", en: "Automation", fa: "اتوماسیون", capability: "automation", permission: "automation.read" },
    { href: "/bots", key: "bots", icon: "⬡", en: "Bots", fa: "ربات‌ها", capability: "bot-adapter", permission: "bot.read" },
    { href: "/sites", key: "sites", icon: "▣", en: "Sites", fa: "سایت‌ها", capability: "site-builder", permission: "site.read" },
    { href: "/domains", key: "domains", icon: "◎", en: "Domains", fa: "دامنه‌ها", capability: "site-builder", permission: "site.read" }
  ]},
  { en: "Operate", fa: "عملیات", items: [
    { href: "/notifications", key: "notifications", icon: "◉", en: "Notifications", fa: "اعلان‌ها" },
    { href: "/reports", key: "reports", icon: "▥", en: "Reports", fa: "گزارش‌ها", capability: "report", permission: "report.read" },
    { href: "/media", key: "media", icon: "▧", en: "Media", fa: "رسانه", capability: "media", permission: "media.read" },
    { href: "/search", key: "search", icon: "⌕", en: "Search", fa: "جستجو", capability: "search", permission: "search.read" },
    { href: "/api-docs", key: "api-docs", icon: "{·}", en: "API docs", fa: "مستندات API" },
  ]},
  { en: "Manage", fa: "مدیریت", items: [
    { href: "/team/users", key: "team-users", icon: "♙", en: "Team members", fa: "اعضای تیم", permission: "team.read" },
    { href: "/team/roles", key: "team-roles", icon: "◇", en: "Roles & permissions", fa: "نقش‌ها و مجوزها", permission: "team.read" },
    { href: "/clients", key: "clients", icon: "▤", en: "Clients", fa: "مشتریان", permission: "realm:manage" },
    { href: "/billing", key: "billing", icon: "◈", en: "Billing", fa: "صورتحساب", permission: "billing.read" },
    { href: "/settings", key: "settings", icon: "⚙", en: "Settings", fa: "تنظیمات", permission: "settings.read" }
  ]},
  { en: "Platform", fa: "پلتفرم", items: [
    { href: "/platform/health", key: "platform-health", icon: "✣", en: "Health checks", fa: "بررسی سلامت", platformOnly: true }
  ]}
];

export function PanelShell(props: PanelShellProps) {
  const pathname = usePathname();
  const { locale, theme, setTheme, toggleLocale, isRtl } = usePanel();
  const { bootstrap, loading, error, tenantKey, siteKey, selectScope, refresh, can } = useScopeAccess();
  const [authChecked, setAuthChecked] = useState(false);
  const [sheet, setSheet] = useState<"build" | "more" | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => { if (!getPlatformAuthToken()) redirectToAuth(pathname || "/"); else setAuthChecked(true); }, [pathname]);
  useEffect(() => {
    if (!sheet) return;
    const previous = document.activeElement as HTMLElement | null;
    const modal = document.querySelector<HTMLElement>(".bottom-sheet");
    const focusable = () => Array.from(modal?.querySelectorAll<HTMLElement>('a[href],button:not([disabled]),select,input,[tabindex]:not([tabindex="-1"])') ?? []);
    focusable()[0]?.focus();
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") { setSheet(null); return; }
      if (event.key !== "Tab") return;
      const items = focusable(); if (!items.length) return;
      const first = items[0], last = items[items.length - 1];
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
      else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
    };
    document.addEventListener("keydown", handleKey);
    return () => { document.removeEventListener("keydown", handleKey); previous?.focus(); };
  }, [sheet]);
  const tenant = bootstrap?.tenants.find((item) => item.tenantKey === tenantKey);
  const site = bootstrap?.sites.find((item) => item.siteKey === siteKey);
  const profileName = bootstrap?.identity.username ?? "";
  const avatarLabel = useMemo(() => (profileName || "Cyan").split(/[\s@._-]+/).filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join("") || "CY", [profileName]);
  const isPlatformAdmin = Boolean(bootstrap?.access.realmRoles.some((role) => ["platform-admin","realm-admin","admin"].includes(role.toLowerCase())));
  const navEnabled = (item: NavItem) => (!item.platformOnly || isPlatformAdmin) && (!item.permission || can(item.permission)) && (!item.capability || Boolean(bootstrap?.capabilities.some((capability) => capability.key === item.capability && capability.enabled && capability.status === "AVAILABLE")));

  if (!authChecked) return <main className="auth-check-shell" aria-label="Checking authentication"><div className="brand-badge">C</div></main>;
  return (
    <div className="panel-app-shell">
      <aside className="workspace-sidebar">
        <Link href="/dashboard" className="brand-lockup" aria-label="Cyan home"><div className="brand-badge">C</div><div><strong>Cyan</strong><span>{locale === "fa" ? "فضای کار هوشمند" : "Business workspace"}</span></div></Link>
        <nav className="workspace-nav" aria-label={locale === "fa" ? "ناوبری اصلی" : "Primary navigation"}>
          {groups.map((group) => <section className="nav-group" key={group.en}><p>{locale === "fa" ? group.fa : group.en}</p>{group.items.map((item) => {
            const active = item.key === props.activeKey || pathname === item.href;
            return navEnabled(item) ? <Link key={item.key} href={item.href} className={active ? "workspace-link active" : "workspace-link"}><span className="workspace-link-icon" aria-hidden>{item.icon}</span><span>{locale === "fa" ? item.fa : item.en}</span></Link>
              : <span key={item.key} className="workspace-link disabled" aria-disabled="true" title={locale === "fa" ? "در پلن یا محیط فعلی در دسترس نیست" : "Unavailable in the current plan or environment"}><span className="workspace-link-icon" aria-hidden>{item.icon}</span><span>{locale === "fa" ? item.fa : item.en}</span><span className="nav-lock" aria-hidden>·</span></span>;
          })}</section>)}
        </nav>
        {bootstrap?.subscription ? <div className="sidebar-plan-card"><p>{bootstrap.subscription.planKey ?? (locale === "fa" ? "بدون پلن" : "No plan")}</p><span>{bootstrap.subscription.status === "NONE" ? (locale === "fa" ? "صورتحساب پیکربندی نشده است." : "Billing is not configured.") : bootstrap.subscription.status}</span></div> : null}
        <div className="sidebar-workspace-badge"><div className="avatar-chip">{avatarLabel}</div><div><strong>{profileName || "—"}</strong><span>{tenant?.displayName ?? (locale === "fa" ? "بدون فضای کار" : "No workspace")}</span></div></div>
      </aside>
      <div className="workspace-main">
        <header className="workspace-header">
          <div className="workspace-switchers">
            <label className="scope-control"><span>{locale === "fa" ? "فضای کار" : "Workspace"}</span><select aria-label={locale === "fa" ? "انتخاب فضای کار" : "Select workspace"} value={tenantKey ?? ""} disabled={loading || !bootstrap?.tenants.length} onChange={(event) => selectScope(event.target.value, null).catch((reason) => setActionError(String(reason)))}><option value="">{locale === "fa" ? "انتخاب کنید" : "Select"}</option>{bootstrap?.tenants.map((item) => <option key={item.tenantKey} value={item.tenantKey}>{item.displayName}</option>)}</select></label>
            <label className="scope-control"><span>{locale === "fa" ? "سایت" : "Site"}</span><select aria-label={locale === "fa" ? "انتخاب سایت" : "Select site"} value={siteKey ?? ""} disabled={loading || !tenantKey || !bootstrap?.sites.length} onChange={(event) => selectScope(tenantKey!, event.target.value || null).catch((reason) => setActionError(String(reason)))}><option value="">{locale === "fa" ? "بدون سایت" : "No site"}</option>{bootstrap?.sites.map((item) => <option key={item.siteKey} value={item.siteKey}>{item.name}</option>)}</select></label>
            {!loading && bootstrap && !bootstrap.tenants.length ? <span className="scope-unavailable">{locale === "fa" ? "فضای کاری در دسترس نیست" : "No workspace available"}</span> : null}
          </div>
          <div className="header-actions">
            <NotificationCenter />
            <details className="header-account-menu"><summary aria-label={locale === "fa" ? "منوی حساب" : "Account menu"}><div className="header-avatar">{avatarLabel}</div><span className="workspace-switcher-caret">⌄</span></summary><div className="header-account-popover"><div className="header-profile"><div className="header-avatar small">{avatarLabel}</div><div><strong>{profileName || "—"}</strong><span>{locale === "fa" ? "حساب فعال" : "Signed in"}</span></div></div><Link href="/profile" className="account-menu-item">{locale === "fa" ? "پروفایل" : "Profile"}</Link><button className="account-menu-item" onClick={toggleLocale}>{locale === "fa" ? "English" : "فارسی"}</button><label className="menu-select"><span>{locale === "fa" ? "پوسته" : "Theme"}</span><select value={theme} onChange={(event) => setTheme(event.target.value as "light" | "dark" | "system")}><option value="system">{locale === "fa" ? "سیستم" : "System"}</option><option value="light">{locale === "fa" ? "روشن" : "Light"}</option><option value="dark">{locale === "fa" ? "تاریک" : "Dark"}</option></select></label><button className="account-menu-item danger" onClick={() => logoutPlatformSession().then(() => redirectToAuth("/"))}>{locale === "fa" ? "خروج" : "Logout"}</button></div></details>
          </div>
        </header>
        <main className="workspace-content">
          {error ? <div className="operational-banner error" role="alert"><span>{error}</span><button onClick={() => refresh()}>{locale === "fa" ? "تلاش دوباره" : "Retry"}</button></div> : null}
          {bootstrap?.warnings.length ? <div className="operational-banner" role="status">{bootstrap.warnings.join(" ")}</div> : null}
          {actionError ? <div className="operational-banner error" role="alert">{actionError}<button onClick={() => setActionError(null)}>×</button></div> : null}
          <section className="page-intro"><div><p className="page-kicker">{locale === "fa" ? props.kickerFa ?? "فضای کار" : props.kicker ?? "Workspace"}</p><h1>{locale === "fa" ? props.titleFa : props.title}</h1><p>{locale === "fa" ? props.subtitleFa : props.subtitle}</p></div></section>
          {bootstrap && !bootstrap.tenants.length
            ? <WorkspaceOnboarding locale={locale} refresh={refresh} selectScope={selectScope} />
            : bootstrap?.activeTenantKey && bootstrap.subscription?.status === "NONE"
              ? <PlanOnboarding locale={locale} tenantKey={bootstrap.activeTenantKey} refresh={refresh} />
            : props.children}
        </main>
      </div>
      <nav className="mobile-bottom-nav" aria-label={locale === "fa" ? "ناوبری موبایل" : "Mobile navigation"}><Link href="/dashboard"><span>⌂</span><span>{locale === "fa" ? "خانه" : "Home"}</span></Link><Link href="/ai"><span>✦</span><span>{locale === "fa" ? "هوش" : "AI"}</span></Link><button onClick={() => setSheet("build")}><span>＋</span><span>{locale === "fa" ? "ساخت" : "Build"}</span></button><Link href="/work"><span>⌁</span><span>{locale === "fa" ? "کار" : "Work"}</span></Link><button onClick={() => setSheet("more")}><span>•••</span><span>{locale === "fa" ? "بیشتر" : "More"}</span></button></nav>
      {sheet ? <div className="sheet-backdrop" onClick={() => setSheet(null)}><section className="bottom-sheet" role="dialog" aria-modal="true" aria-label={sheet === "build" ? "Build navigation" : "More navigation"} onClick={(event) => event.stopPropagation()}><div className="sheet-handle"/><div className="sheet-grid">{groups.slice(sheet === "build" ? 1 : 2, sheet === "build" ? 2 : groups.length).flatMap((group) => group.items).filter(item=>!item.platformOnly||isPlatformAdmin).map((item) => navEnabled(item) ? <Link key={item.key} href={item.href} onClick={() => setSheet(null)}><span>{item.icon}</span>{locale === "fa" ? item.fa : item.en}</Link> : <span key={item.key} aria-disabled="true"><span>{item.icon}</span>{locale === "fa" ? item.fa : item.en}</span>)}</div><button className="secondary-pill" onClick={() => setSheet(null)}>{locale === "fa" ? "بستن" : "Close"}</button></section></div> : null}
    </div>
  );
}

function WorkspaceOnboarding({locale,refresh,selectScope}:{locale:"en"|"fa";refresh:()=>Promise<void>;selectScope:(tenantKey:string,siteKey?:string|null)=>Promise<void>}) {
  const [name,setName]=useState("");
  const [key,setKey]=useState("");
  const [pending,setPending]=useState(false);
  const [error,setError]=useState<string|null>(null);
  const updateName=(value:string)=>{
    setName(value);
    setKey((current)=>current ? current : value.toLowerCase().trim().replace(/[^a-z0-9]+/g,"-").replace(/^-|-$/g,"").slice(0,48));
  };
  const create=async()=>{
    if(pending)return;
    setPending(true);setError(null);
    try{
      const response=await platformFetch("/api/platform/service/tenant-service/endpoint/tenants",{method:"POST",headers:{"Content-Type":"application/json","Idempotency-Key":crypto.randomUUID()},body:JSON.stringify({tenantKey:key,displayName:name})});
      if(!response.ok)throw new Error((await response.json().catch(()=>null))?.message??`Workspace creation failed (${response.status})`);
      await refresh();
      await selectScope(key,null);
    }catch(reason){setError(reason instanceof Error?reason.message:String(reason))}
    finally{setPending(false)}
  };
  return <section className="workspace-onboarding panel-card">
    <div><p className="page-kicker">{locale==="fa"?"شروع کار":"Get started"}</p><h2>{locale==="fa"?"اولین فضای کاری را بسازید":"Create your first workspace"}</h2><p>{locale==="fa"?"فضای کاری محدوده واقعی داده‌ها، سایت‌ها و دسترسی تیم شماست.":"A workspace is the real tenant boundary for your data, sites, and team access."}</p></div>
    <label><span>{locale==="fa"?"نام فضای کاری":"Workspace name"}</span><input value={name} onChange={event=>updateName(event.target.value)} /></label>
    <label><span>{locale==="fa"?"کلید فضای کاری":"Workspace key"}</span><input dir="ltr" value={key} onChange={event=>setKey(event.target.value.toLowerCase().replace(/[^a-z0-9-]/g,""))} /></label>
    {error?<p className="field-error" role="alert">{error}</p>:null}
    <AsyncButton pending={pending} pendingLabel={locale==="fa"?"در حال ساخت…":"Creating…"} disabled={name.trim().length<2||!/^[a-z0-9][a-z0-9-]{2,79}$/.test(key)} onClick={create}>{locale==="fa"?"ساخت فضای کاری":"Create workspace"}</AsyncButton>
  </section>;
}

type AvailablePlan={planKey:string;displayName:string;description?:string;billingMode:"FREE"|"EXTERNAL";active:boolean;features:string[];limits:Record<string,unknown>};
function PlanOnboarding({locale,tenantKey,refresh}:{locale:"en"|"fa";tenantKey:string;refresh:()=>Promise<void>}){
  const [plans,setPlans]=useState<AvailablePlan[]>([]);const [loading,setLoading]=useState(true);const [pending,setPending]=useState<string|null>(null);const [error,setError]=useState<string|null>(null);
  useEffect(()=>{platformFetch("/api/platform/service/billing-service/endpoint/billing/plans").then(async response=>{if(!response.ok)throw new Error(`Plans could not be loaded (${response.status})`);setPlans(await response.json())}).catch(reason=>setError(reason instanceof Error?reason.message:String(reason))).finally(()=>setLoading(false))},[]);
  const choose=async(plan:AvailablePlan)=>{if(pending||plan.billingMode!=="FREE")return;setPending(plan.planKey);setError(null);try{const response=await platformFetch(`/api/platform/service/billing-service/endpoint/billing/tenants/${encodeURIComponent(tenantKey)}/subscription/change`,{method:"POST",headers:{"Content-Type":"application/json","Idempotency-Key":crypto.randomUUID()},body:JSON.stringify({planKey:plan.planKey})});if(!response.ok)throw new Error((await response.json().catch(()=>null))?.message??`Plan activation failed (${response.status})`);await refresh()}catch(reason){setError(reason instanceof Error?reason.message:String(reason))}finally{setPending(null)}};
  return <section className="plan-onboarding"><div><p className="page-kicker">{locale==="fa"?"دسترسی":"Workspace access"}</p><h2>{locale==="fa"?"یک پلن واقعی انتخاب کنید":"Choose an available plan"}</h2><p>{locale==="fa"?"پلن‌های نیازمند ارائه‌دهنده پرداخت تا زمان پیکربندی قفل می‌مانند.":"Plans requiring an external billing provider remain locked until configured."}</p></div>{loading?<p>{locale==="fa"?"در حال بارگذاری…":"Loading plans…"}</p>:plans.length?<div className="plan-onboarding-grid">{plans.map(plan=><article className="panel-card" key={plan.planKey}><h3>{plan.displayName}</h3><p>{plan.description}</p><small>{plan.features.join(" · ")}</small><AsyncButton pending={pending===plan.planKey} disabled={plan.billingMode!=="FREE"||Boolean(pending)} onClick={()=>choose(plan)}>{plan.billingMode==="FREE"?(locale==="fa"?"فعال‌سازی":"Activate"):(locale==="fa"?"پیکربندی نشده":"Not configured")}</AsyncButton></article>)}</div>:<p>{locale==="fa"?"مدیر پلتفرم هنوز پلنی منتشر نکرده است.":"No plan has been published by the platform administrator."}</p>}{error?<p className="field-error" role="alert">{error}</p>:null}</section>
}
