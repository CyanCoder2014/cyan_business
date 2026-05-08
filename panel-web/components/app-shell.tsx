import Link from "next/link";
import type { ReactNode } from "react";

type AppShellProps = {
  title: string;
  subtitle: string;
  children: ReactNode;
};

export function AppShell({ title, subtitle, children }: AppShellProps) {
  return (
    <div className="page-shell">
      <div className="container">
        <header className="topbar">
          <div className="brand">
            <div className="brand-mark" />
            <div>
              <h1>Naviya Panel</h1>
              <p>{subtitle}</p>
            </div>
          </div>
          <nav className="nav" aria-label="Primary">
            <Link href="/">Dashboard</Link>
            <Link href="/projects">Projects</Link>
            <Link href="/projects/new">Build App</Link>
            <Link href="/bot">Bot Flow</Link>
          </nav>
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
