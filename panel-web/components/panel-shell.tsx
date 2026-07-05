"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { BuildingIcon, GlobeIcon } from "@/components/auth-icons";
import { usePanel } from "@/components/panel-provider";
import { getPlatformAuthToken, getPlatformUsername, logoutPlatformSession, redirectToAuth } from "@/lib/platform-auth";

type PanelShellProps = {
  title: string;
  titleFa: string;
  subtitle: string;
  subtitleFa: string;
  kicker?: string;
  kickerFa?: string;
  activeKey: string;
  children: ReactNode;
};

const navigation = [
  { href: "/", key: "dashboard", icon: "⌘", en: "Dashboard", fa: "داشبورد" },
  { href: "/projects/new", key: "studio", icon: "✦", en: "AI Studio", fa: "استودیوی هوش مصنوعی" },
  { href: "/projects", key: "blueprints", icon: "▤", en: "Blueprints", fa: "قالب‌ها" },
  { href: "/maker", key: "maker", icon: "✎", en: "Maker", fa: "سازنده" },
  { href: "/data", key: "data", icon: "◍", en: "Data", fa: "داده‌ها" },
  { href: "/flows", key: "flows", icon: "⌇", en: "Flow Builder", fa: "فلوساز" },
  { href: "/integrations", key: "integrations", icon: "⬡", en: "Client Apps/Bots", fa: "اپ‌ها / بات‌ها" },
  { href: "/bot", key: "bot", icon: "☻", en: "Bot Experience", fa: "تجربه بات" },
  { href: "/site-builder", key: "site-builder", icon: "▣", en: "Site Builder", fa: "سایت‌ساز" },
  { href: "/search", key: "search", icon: "⌕", en: "Media", fa: "مدیا" },
  { href: "/automation", key: "automation", icon: "◔", en: "Analytics", fa: "آنالیتیکس" },
  { href: "/iam", key: "iam", icon: "⚙", en: "Settings", fa: "تنظیمات" }
];

export function PanelShell({ title, titleFa, subtitle, subtitleFa, kicker, kickerFa, activeKey, children }: PanelShellProps) {
  const pathname = usePathname();
  const { locale, theme, toggleLocale, toggleTheme, workspaceName, siteName, isRtl } = usePanel();
  const [authChecked, setAuthChecked] = useState(false);
  const [profileName, setProfileName] = useState("");
  const [loggingOut, setLoggingOut] = useState(false);

  useEffect(() => {
    if (!getPlatformAuthToken()) {
      redirectToAuth(pathname || "/");
      return;
    }
    setProfileName(getPlatformUsername());
    setAuthChecked(true);
  }, [pathname]);

  const avatarLabel = useMemo(() => {
    const source = profileName || workspaceName || "Cyan";
    return source
      .split(/[\s@._-]+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join("") || "CY";
  }, [profileName, workspaceName]);

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await logoutPlatformSession();
      redirectToAuth("/");
    } finally {
      setLoggingOut(false);
    }
  }

  if (!authChecked) {
    return (
      <main className="auth-check-shell">
        <div className="brand-badge">C</div>
      </main>
    );
  }

  return (
    <div className="panel-app-shell">
      <aside className="workspace-sidebar">
        <Link href="/" className="brand-lockup">
          <div className="brand-badge">C</div>
          <div>
            <strong>Cyan</strong>
            <span>{locale === "fa" ? "پلتفرم کسب‌وکار هوشمند" : "AI-native app platform"}</span>
          </div>
        </Link>

        <nav className="workspace-nav" aria-label="Primary navigation">
          {navigation.map((item) => {
            const isActive = item.key === activeKey || pathname === item.href;
            return (
              <Link key={item.href} href={item.href} className={isActive ? "workspace-link active" : "workspace-link"}>
                <span className="workspace-link-icon" aria-hidden="true">
                  {item.icon}
                </span>
                <span>{locale === "fa" ? item.fa : item.en}</span>
              </Link>
            );
          })}
        </nav>

        <div className="sidebar-plan-card">
          <p>{locale === "fa" ? "پلن حرفه‌ای" : "Pro plan"}</p>
          <span>
            {locale === "fa"
              ? "پروژه‌های نامحدود، امکانات حرفه‌ای و پشتیبانی سریع‌تر."
              : "Unlimited projects, premium modules, and priority support."}
          </span>
          <button type="button" className="secondary-pill">
            {locale === "fa" ? "مدیریت پلن" : "Manage plan"}
          </button>
        </div>

        <div className="sidebar-workspace-badge">
          <div className="avatar-chip">{avatarLabel}</div>
          <div>
            <strong>{workspaceName}</strong>
            <span>{locale === "fa" ? "فضای کاری" : "Workspace"}</span>
          </div>
        </div>
      </aside>

      <div className="workspace-main">
        <header className="workspace-header">
          <div className="workspace-switchers">
            <div className="workspace-switcher">
              <span className="workspace-switcher-icon" aria-hidden="true">
                <BuildingIcon size={20} />
              </span>
              <div>
                <span>{locale === "fa" ? "فضای کاری" : "Workspace"}</span>
                <strong>{workspaceName}</strong>
              </div>
              <span className="workspace-switcher-caret" aria-hidden="true">⌄</span>
            </div>
            <div className="workspace-switcher">
              <span className="workspace-switcher-icon" aria-hidden="true">
                <GlobeIcon size={20} />
              </span>
              <div>
                <span>{locale === "fa" ? "سایت" : "Site"}</span>
                <strong>{siteName}</strong>
              </div>
              <span className="workspace-switcher-caret" aria-hidden="true">⌄</span>
            </div>
          </div>

          <div className="header-actions">
            <button type="button" className="header-icon-button notification-button" aria-label={isRtl ? "اعلان‌ها" : "Notifications"}>
              <span className="header-bell" aria-hidden="true" />
            </button>
            <Link href="/iam" className="header-icon-button" aria-label={isRtl ? "راهنما و تنظیمات" : "Help and settings"}>
              ?
            </Link>
            <details className="header-account-menu">
              <summary aria-label={isRtl ? "منوی حساب" : "Account menu"}>
                <div className="header-avatar">{avatarLabel}</div>
                <span className="header-profile-state" aria-hidden="true" />
                <span className="workspace-switcher-caret" aria-hidden="true">⌄</span>
              </summary>
              <div className="header-account-popover">
                <div className="header-profile">
                  <div className="header-avatar small">{avatarLabel}</div>
                  <div>
                    <strong>{profileName || (isRtl ? "کاربر پنل" : "Panel user")}</strong>
                    <span>{isRtl ? "حساب فعال" : "Signed in"}</span>
                  </div>
                </div>
                <Link href="/iam" className="account-menu-item">
                  {isRtl ? "پروفایل" : "Profile"}
                </Link>
                <button type="button" className="account-menu-item" onClick={toggleLocale}>
                  {locale === "fa" ? "English" : "فارسی"}
                </button>
                <button type="button" className="account-menu-item" onClick={toggleTheme}>
                  {theme === "light" ? (isRtl ? "حالت تاریک" : "Dark mode") : isRtl ? "حالت روشن" : "Light mode"}
                </button>
                <button type="button" className="account-menu-item danger" onClick={() => handleLogout().catch(() => null)} disabled={loggingOut}>
                  {loggingOut ? (isRtl ? "خروج..." : "Signing out...") : isRtl ? "خروج" : "Logout"}
                </button>
              </div>
            </details>
          </div>
        </header>

        <main className="workspace-content">
          <section className="page-intro">
            <div>
              <p className="page-kicker">{locale === "fa" ? kickerFa ?? "پلتفرم کسب‌وکار هوشمند" : kicker ?? "AI-native business platform"}</p>
              <h1>{locale === "fa" ? titleFa : title}</h1>
              <p>{locale === "fa" ? subtitleFa : subtitle}</p>
            </div>
          </section>
          {children}
          <nav className="mobile-bottom-nav" aria-label="Mobile navigation">
            {[
              navigation[0],
              navigation[1],
              navigation[4],
              navigation[5],
              navigation[6]
            ].map((item) => (
              <Link key={item.href} href={item.href}>
                <span>{item.icon}</span>
                <span>{locale === "fa" ? item.fa : item.en}</span>
              </Link>
            ))}
          </nav>
        </main>
      </div>
    </div>
  );
}
