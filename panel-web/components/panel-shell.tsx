"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { usePanel } from "@/components/panel-provider";

type PanelShellProps = {
  title: string;
  titleFa: string;
  subtitle: string;
  subtitleFa: string;
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
  { href: "/site-builder", key: "site-builder", icon: "▣", en: "Site Builder", fa: "سایت‌ساز" },
  { href: "/search", key: "search", icon: "⌕", en: "Search", fa: "جستجو" },
  { href: "/automation", key: "automation", icon: "⚙", en: "Automation", fa: "اتوماسیون" },
  { href: "/iam", key: "iam", icon: "◎", en: "IAM", fa: "دسترسی" },
  { href: "/notifications", key: "notifications", icon: "◌", en: "Notifications", fa: "اعلان‌ها" }
];

export function PanelShell({ title, titleFa, subtitle, subtitleFa, activeKey, children }: PanelShellProps) {
  const pathname = usePathname();
  const { locale, theme, toggleLocale, toggleTheme, workspaceName, siteName, isRtl } = usePanel();

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
          <div className="avatar-chip">AC</div>
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
              <span>{locale === "fa" ? "فضای کاری" : "Workspace"}</span>
              <strong>{workspaceName}</strong>
            </div>
            <div className="workspace-switcher">
              <span>{locale === "fa" ? "سایت" : "Site"}</span>
              <strong>{siteName}</strong>
            </div>
          </div>

          <div className="header-actions">
            <button type="button" className="icon-pill" onClick={toggleLocale}>
              {locale === "fa" ? "EN" : "فا"}
            </button>
            <button type="button" className="icon-pill" onClick={toggleTheme}>
              {theme === "light" ? "☾" : "☀"}
            </button>
            <span className="icon-pill" aria-hidden="true">
              ⍰
            </span>
            <div className="header-profile">
              <div className="header-avatar">AM</div>
              <div>
                <strong>{isRtl ? "علی محمدی" : "Ali Mohammadi"}</strong>
                <span>{isRtl ? "مدیر ارشد" : "Admin"}</span>
              </div>
            </div>
          </div>
        </header>

        <main className="workspace-content">
          <section className="page-intro">
            <div>
              <p className="page-kicker">{locale === "fa" ? "پلتفرم کسب‌وکار هوشمند" : "AI-native business platform"}</p>
              <h1>{locale === "fa" ? titleFa : title}</h1>
              <p>{locale === "fa" ? subtitleFa : subtitle}</p>
            </div>
          </section>
          {children}
          <nav className="mobile-bottom-nav" aria-label="Mobile navigation">
            {navigation.slice(0, 5).map((item) => (
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
