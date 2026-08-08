"use client";

import type { ReactNode } from "react";
import { usePathname } from "next/navigation";
import { PanelShell } from "@/components/panel-shell";

type AppShellProps = {
  title: string;
  subtitle: string;
  children: ReactNode;
};

export function AppShell({ title, subtitle, children }: AppShellProps) {
  const pathname = usePathname();
  const activeKey = resolveActiveKey(pathname);

  return (
    <PanelShell activeKey={activeKey} title={title} titleFa={title} subtitle={subtitle} subtitleFa={subtitle}>
      <section className="panel-card">{children}</section>
    </PanelShell>
  );
}

function resolveActiveKey(pathname: string) {
  if (pathname.startsWith("/projects/new")) return "studio";
  if (pathname.startsWith("/projects")) return "blueprints";
  if (pathname.startsWith("/maker")) return "maker";
  if (pathname.startsWith("/data")) return "data";
  if (pathname.startsWith("/flows")) return "flows";
  if (pathname.startsWith("/integrations") || pathname.startsWith("/bot")) return "bots";
  if (pathname.startsWith("/sites") || pathname.startsWith("/site-builder")) return "sites";
  if (pathname.startsWith("/domains")) return "domains";
  if (pathname.startsWith("/search")) return "search";
  if (pathname.startsWith("/automation")) return "automation";
  if (pathname.startsWith("/iam")) return "iam";
  if (pathname.startsWith("/notifications")) return "notifications";
  return "dashboard";
}
