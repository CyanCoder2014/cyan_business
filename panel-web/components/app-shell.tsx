import Link from "next/link";
import type { ReactNode } from "react";
import { WorkspaceControls } from "@/components/workspace-controls";

type AppShellProps = {
  title: string;
  subtitle: string;
  children: ReactNode;
};

export function AppShell({ title, subtitle, children }: AppShellProps) {
  const navItems = [
    { href: "/", label: "Dashboard", fa: "داشبورد" },
    { href: "/roadmap", label: "Roadmap", fa: "نقشه راه" },
    { href: "/projects", label: "Projects", fa: "پروژه ها" },
    { href: "/projects/new", label: "AI Studio", fa: "استودیو AI" },
    { href: "/maker", label: "Maker", fa: "سازنده" },
    { href: "/site-builder", label: "Site", fa: "سایت" },
    { href: "/qa", label: "QA", fa: "تست" },
    { href: "/data", label: "Data", fa: "داده" },
    { href: "/flows", label: "Flows", fa: "فرآیند" },
    { href: "/integrations", label: "Apps/Bots", fa: "اپ/بات" },
    { href: "/bot", label: "Bot Flow", fa: "بات" }
  ];

  return (
    <div className="page-shell">
      <div className="container">
        <header className="topbar">
          <div className="brand">
            <div className="brand-mark" />
            <div>
              <h1>Cyan Panel</h1>
              <p>{subtitle}</p>
            </div>
          </div>
          <nav className="nav" aria-label="Primary">
            {navItems.map((item) => (
              <Link key={item.href} href={item.href}>
                <span>{item.label}</span>
                <small>{item.fa}</small>
              </Link>
            ))}
          </nav>
          <WorkspaceControls />
        </header>
        <main style={{ paddingTop: 24 }}>
          <section className="panel">
            <div className="panel-header">
              <div>
                <p className="eyebrow">Structured app maker</p>
                <h2 className="headline">{title}</h2>
              </div>
              <div className="tag">Panel workspace</div>
            </div>
            {children}
          </section>
        </main>
      </div>
    </div>
  );
}
