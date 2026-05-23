"use client";

import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

export type PanelLocale = "en" | "fa";
export type PanelTheme = "light" | "dark";

type PanelContextValue = {
  locale: PanelLocale;
  theme: PanelTheme;
  workspaceName: string;
  siteName: string;
  setWorkspaceName: (value: string) => void;
  setSiteName: (value: string) => void;
  setLocale: (value: PanelLocale) => void;
  setTheme: (value: PanelTheme) => void;
  toggleLocale: () => void;
  toggleTheme: () => void;
  isRtl: boolean;
};

const STORAGE_KEYS = {
  locale: "cyan.panel.locale",
  theme: "cyan.panel.theme",
  workspace: "cyan.panel.workspace",
  site: "cyan.panel.site"
} as const;

const PanelContext = createContext<PanelContextValue | null>(null);

export function PanelProvider({ children }: { children: ReactNode }) {
  const [locale, setLocaleState] = useState<PanelLocale>("en");
  const [theme, setThemeState] = useState<PanelTheme>("light");
  const [workspaceName, setWorkspaceNameState] = useState("Acme Corp");
  const [siteName, setSiteNameState] = useState("acme.cyan.app");

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    const storedLocale = window.localStorage.getItem(STORAGE_KEYS.locale) as PanelLocale | null;
    const storedTheme = window.localStorage.getItem(STORAGE_KEYS.theme) as PanelTheme | null;
    const storedWorkspace = window.localStorage.getItem(STORAGE_KEYS.workspace);
    const storedSite = window.localStorage.getItem(STORAGE_KEYS.site);

    if (storedLocale === "en" || storedLocale === "fa") {
      setLocaleState(storedLocale);
    }
    if (storedTheme === "light" || storedTheme === "dark") {
      setThemeState(storedTheme);
    }
    if (storedWorkspace) {
      setWorkspaceNameState(storedWorkspace);
    }
    if (storedSite) {
      setSiteNameState(storedSite);
    }
  }, []);

  useEffect(() => {
    const root = document.documentElement;
    root.lang = locale === "fa" ? "fa" : "en";
    root.dir = locale === "fa" ? "rtl" : "ltr";
    root.dataset.theme = theme;
    root.dataset.locale = locale;
    window.localStorage.setItem(STORAGE_KEYS.locale, locale);
    window.localStorage.setItem(STORAGE_KEYS.theme, theme);
    window.localStorage.setItem(STORAGE_KEYS.workspace, workspaceName);
    window.localStorage.setItem(STORAGE_KEYS.site, siteName);
  }, [locale, theme, workspaceName, siteName]);

  const value = useMemo<PanelContextValue>(
    () => ({
      locale,
      theme,
      workspaceName,
      siteName,
      setWorkspaceName: setWorkspaceNameState,
      setSiteName: setSiteNameState,
      setLocale: setLocaleState,
      setTheme: setThemeState,
      toggleLocale: () => setLocaleState((current) => (current === "en" ? "fa" : "en")),
      toggleTheme: () => setThemeState((current) => (current === "light" ? "dark" : "light")),
      isRtl: locale === "fa"
    }),
    [locale, siteName, theme, workspaceName]
  );

  return <PanelContext.Provider value={value}>{children}</PanelContext.Provider>;
}

export function usePanel() {
  const context = useContext(PanelContext);
  if (!context) {
    throw new Error("usePanel must be used inside PanelProvider");
  }
  return context;
}
