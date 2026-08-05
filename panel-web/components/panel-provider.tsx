"use client";

import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

export type PanelLocale = "en" | "fa";
export type PanelTheme = "light" | "dark" | "system";

type PanelContextValue = {
  locale: PanelLocale;
  theme: PanelTheme;
  /** @deprecated Business scope is supplied by ScopeAccessProvider. */
  workspaceName: string;
  /** @deprecated Business scope is supplied by ScopeAccessProvider. */
  siteName: string;
  /** @deprecated Use the persisted scope selector. */
  setWorkspaceName: (value: string) => void;
  /** @deprecated Use the persisted scope selector. */
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
} as const;

const PanelContext = createContext<PanelContextValue | null>(null);

export function PanelProvider({ children }: { children: ReactNode }) {
  const [locale, setLocaleState] = useState<PanelLocale>("en");
  const [theme, setThemeState] = useState<PanelTheme>("system");
  const [preferencesLoaded, setPreferencesLoaded] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    const storedLocale = window.localStorage.getItem(STORAGE_KEYS.locale) as PanelLocale | null;
    const storedTheme = window.localStorage.getItem(STORAGE_KEYS.theme) as PanelTheme | null;

    if (storedLocale === "en" || storedLocale === "fa") {
      setLocaleState(storedLocale);
    }
    if (storedTheme === "light" || storedTheme === "dark" || storedTheme === "system") {
      setThemeState(storedTheme);
    }
    setPreferencesLoaded(true);
  }, []);

  useEffect(() => {
    if (!preferencesLoaded) return;
    const root = document.documentElement;
    root.lang = locale === "fa" ? "fa" : "en";
    root.dir = locale === "fa" ? "rtl" : "ltr";
    const systemDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
    root.dataset.theme = theme === "system" ? (systemDark ? "dark" : "light") : theme;
    root.dataset.themePreference = theme;
    root.dataset.locale = locale;
    window.localStorage.setItem(STORAGE_KEYS.locale, locale);
    window.localStorage.setItem(STORAGE_KEYS.theme, theme);
    const media = window.matchMedia("(prefers-color-scheme: dark)");
    const syncSystem = () => { if (theme === "system") root.dataset.theme = media.matches ? "dark" : "light"; };
    media.addEventListener("change", syncSystem);
    return () => media.removeEventListener("change", syncSystem);
  }, [locale, preferencesLoaded, theme]);

  const value = useMemo<PanelContextValue>(
    () => ({
      locale,
      theme,
      workspaceName: "",
      siteName: "",
      setWorkspaceName: () => undefined,
      setSiteName: () => undefined,
      setLocale: setLocaleState,
      setTheme: setThemeState,
      toggleLocale: () => setLocaleState((current) => (current === "en" ? "fa" : "en")),
      toggleTheme: () => setThemeState((current) => (current === "light" ? "dark" : current === "dark" ? "system" : "light")),
      isRtl: locale === "fa"
    }),
    [locale, theme]
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
