"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { getPlatformAuthToken, logoutPlatformSession, redirectToAuth } from "@/lib/platform-auth";

type PanelShellProps = { title: string; titleFa: string; subtitle: string; subtitleFa: string; kicker?: string; kickerFa?: string; activeKey: string; children: ReactNode };
type NavItem = { href: string; key: string; icon: string; en: string; fa: string; capability?: string; permission?: string };

const groups: Array<{ en: string; fa: string; items: NavItem[] }> = [
  { en: "Workspace", fa: "فضای کار", items: [
    { href: "/", key: "dashboard", icon: "⌂", en: "Home", fa: "خانه" },
    { href: "/projects/new", key: "studio", icon: "✦", en: "AI Studio", fa: "استودیوی هوش", capability: "ai-orchestrator", permission: "project.create" },
    { href: "/projects", key: "blueprints", icon: "▦", en: "Projects", fa: "پروژه‌ها", capability: "ai-orchestrator", permission: "project.read" }
  ]},
  { en: "Build", fa: "ساخت", items: [
    { href: "/maker", key: "maker", icon: "✎", en: "Maker", fa: "سازنده", capability: "dynamic-entities", permission: "definition.read" },
    { href: "/data", key: "data", icon: "◫", en: "Data", fa: "داده‌ها", capability: "dynamic-entities", permission: "record.read" },
    { href: "/flows", key: "flows", icon: "⌁", en: "Flows", fa: "فلوها", capability: "bpm", permission: "bpm.read" },
    { href: "/automation", key: "automation", icon: "↯", en: "Automation", fa: "اتوماسیون", capability: "automation", permission: "automation.read" },
    { href: "/integrations", key: "integrations", icon: "⬡", en: "Integrations", fa: "یکپارچه‌سازی", capability: "bot-adapter", permission: "bot.read" },
    { href: "/site-builder", key: "site-builder", icon: "▣", en: "Site builder", fa: "سایت‌ساز", capability: "site-builder", permission: "site.read" }
  ]},
  { en: "Operate", fa: "عملیات", items: [
    { href: "/search", key: "search", icon: "⌕", en: "Search & media", fa: "جستجو و رسانه", capability: "search", permission: "search.read" },
    { href: "/api-docs", key: "api-docs", icon: "{·}", en: "API docs", fa: "مستندات API" },
    { href: "/iam", key: "iam", icon: "⚙", en: "Settings", fa: "تنظیمات", permission: "settings.read" }
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
  const navEnabled = (item: NavItem) => (!item.permission || can(item.permission)) && (!item.capability || Boolean(bootstrap?.capabilities.some((capability) => capability.key === item.capability && capability.enabled && capability.status === "AVAILABLE")));

  if (!authChecked) return <main className="auth-check-shell" aria-label="Checking authentication"><div className="brand-badge">C</div></main>;
  return (
    <div className="panel-app-shell">
      <aside className="workspace-sidebar">
        <Link href="/" className="brand-lockup" aria-label="Cyan home"><div className="brand-badge">C</div><div><strong>Cyan</strong><span>{locale === "fa" ? "فضای کار هوشمند" : "Business workspace"}</span></div></Link>
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
            <button type="button" className="header-icon-button" disabled title={locale === "fa" ? "صندوق اعلان هنوز ارائه نشده است" : "Notification inbox is unavailable"} aria-label={locale === "fa" ? "اعلان‌ها در دسترس نیست" : "Notifications unavailable"}>♢</button>
            <details className="header-account-menu"><summary aria-label={locale === "fa" ? "منوی حساب" : "Account menu"}><div className="header-avatar">{avatarLabel}</div><span className="workspace-switcher-caret">⌄</span></summary><div className="header-account-popover"><div className="header-profile"><div className="header-avatar small">{avatarLabel}</div><div><strong>{profileName || "—"}</strong><span>{locale === "fa" ? "حساب فعال" : "Signed in"}</span></div></div><Link href="/iam" className="account-menu-item">{locale === "fa" ? "پروفایل" : "Profile"}</Link><button className="account-menu-item" onClick={toggleLocale}>{locale === "fa" ? "English" : "فارسی"}</button><label className="menu-select"><span>{locale === "fa" ? "پوسته" : "Theme"}</span><select value={theme} onChange={(event) => setTheme(event.target.value as "light" | "dark" | "system")}><option value="system">{locale === "fa" ? "سیستم" : "System"}</option><option value="light">{locale === "fa" ? "روشن" : "Light"}</option><option value="dark">{locale === "fa" ? "تاریک" : "Dark"}</option></select></label><button className="account-menu-item danger" onClick={() => logoutPlatformSession().then(() => redirectToAuth("/"))}>{locale === "fa" ? "خروج" : "Logout"}</button></div></details>
          </div>
        </header>
        <main className="workspace-content">
          {error ? <div className="operational-banner error" role="alert"><span>{error}</span><button onClick={() => refresh()}>{locale === "fa" ? "تلاش دوباره" : "Retry"}</button></div> : null}
          {bootstrap?.warnings.length ? <div className="operational-banner" role="status">{bootstrap.warnings.join(" ")}</div> : null}
          {actionError ? <div className="operational-banner error" role="alert">{actionError}<button onClick={() => setActionError(null)}>×</button></div> : null}
          <section className="page-intro"><div><p className="page-kicker">{locale === "fa" ? props.kickerFa ?? "فضای کار" : props.kicker ?? "Workspace"}</p><h1>{locale === "fa" ? props.titleFa : props.title}</h1><p>{locale === "fa" ? props.subtitleFa : props.subtitle}</p></div></section>
          {props.children}
        </main>
      </div>
      <nav className="mobile-bottom-nav" aria-label={locale === "fa" ? "ناوبری موبایل" : "Mobile navigation"}><Link href="/"><span>⌂</span><span>{locale === "fa" ? "خانه" : "Home"}</span></Link><Link href="/projects/new"><span>✦</span><span>{locale === "fa" ? "هوش" : "AI"}</span></Link><button onClick={() => setSheet("build")}><span>＋</span><span>{locale === "fa" ? "ساخت" : "Build"}</span></button><Link href="/flows"><span>⌁</span><span>{locale === "fa" ? "کار" : "Work"}</span></Link><button onClick={() => setSheet("more")}><span>•••</span><span>{locale === "fa" ? "بیشتر" : "More"}</span></button></nav>
      {sheet ? <div className="sheet-backdrop" onClick={() => setSheet(null)}><section className="bottom-sheet" role="dialog" aria-modal="true" aria-label={sheet === "build" ? "Build navigation" : "More navigation"} onClick={(event) => event.stopPropagation()}><div className="sheet-handle"/><div className="sheet-grid">{groups.slice(sheet === "build" ? 1 : 2, sheet === "build" ? 2 : 3).flatMap((group) => group.items).map((item) => navEnabled(item) ? <Link key={item.key} href={item.href} onClick={() => setSheet(null)}><span>{item.icon}</span>{locale === "fa" ? item.fa : item.en}</Link> : <span key={item.key} aria-disabled="true"><span>{item.icon}</span>{locale === "fa" ? item.fa : item.en}</span>)}</div><button className="secondary-pill" onClick={() => setSheet(null)}>{locale === "fa" ? "بستن" : "Close"}</button></section></div> : null}
    </div>
  );
}
