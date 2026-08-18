"use client";

import type { ReactNode } from "react";
import { PanelProvider } from "@/components/panel-provider";
import { ScopeAccessProvider } from "@/components/scope-access-provider";
import { PwaRuntime } from "@/components/pwa-runtime";
import { ToastProvider } from "@/components/ui/toast-provider";

export function AppProviders({ children }: { children: ReactNode }) {
  return <PanelProvider><ToastProvider><ScopeAccessProvider><PwaRuntime />{children}</ScopeAccessProvider></ToastProvider></PanelProvider>;
}
