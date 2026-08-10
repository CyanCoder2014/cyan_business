"use client";

import type { ReactNode } from "react";
import { PanelProvider } from "@/components/panel-provider";
import { ScopeAccessProvider } from "@/components/scope-access-provider";
import { PwaRuntime } from "@/components/pwa-runtime";

export function AppProviders({ children }: { children: ReactNode }) {
  return <PanelProvider><ScopeAccessProvider><PwaRuntime />{children}</ScopeAccessProvider></PanelProvider>;
}
