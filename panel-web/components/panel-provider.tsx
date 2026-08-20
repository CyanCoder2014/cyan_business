"use client";

import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

export type PanelLocale = "en" | "fa";
export type PanelTheme = "light" | "dark" | "system";
export type SidebarMode = "open" | "half" | "closed";

type PanelContextValue = {
  locale: PanelLocale;
  theme: PanelTheme;
  sidebarMode: SidebarMode;
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
  setSidebarMode: (value: SidebarMode) => void;
  toggleLocale: () => void;
  toggleTheme: () => void;
  cycleSidebarMode: () => void;
  isRtl: boolean;
};

const STORAGE_KEYS = {
  locale: "cyan.panel.locale",
  theme: "cyan.panel.theme",
  sidebarMode: "cyan.panel.sidebarMode"
} as const;

const PanelContext = createContext<PanelContextValue | null>(null);

export function PanelProvider({ children }: { children: ReactNode }) {
  const [locale, setLocaleState] = useState<PanelLocale>("en");
  const [theme, setThemeState] = useState<PanelTheme>("system");
  const [sidebarMode, setSidebarModeState] = useState<SidebarMode>("open");
  const [preferencesLoaded, setPreferencesLoaded] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    const storedLocale = window.localStorage.getItem(STORAGE_KEYS.locale) as PanelLocale | null;
    const storedTheme = window.localStorage.getItem(STORAGE_KEYS.theme) as PanelTheme | null;
    const storedSidebarMode = window.localStorage.getItem(STORAGE_KEYS.sidebarMode) as SidebarMode | null;

    if (storedLocale === "en" || storedLocale === "fa") {
      setLocaleState(storedLocale);
    }
    if (storedTheme === "light" || storedTheme === "dark" || storedTheme === "system") {
      setThemeState(storedTheme);
    }
    if (storedSidebarMode === "open" || storedSidebarMode === "half" || storedSidebarMode === "closed") {
      setSidebarModeState(storedSidebarMode);
    }
    setPreferencesLoaded(true);
  }, []);

  useEffect(() => {
    if (!preferencesLoaded) return;
    const root = document.documentElement;
    root.lang = locale === "fa" ? "fa" : "en";
    root.dir = locale === "fa" ? "rtl" : "ltr";
    const systemDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
    const resolvedTheme = theme === "system" ? (systemDark ? "dark" : "light") : theme;
    root.dataset.theme = resolvedTheme;
    root.dataset.themePreference = theme;
    root.dataset.locale = locale;
    root.dataset.sidebarMode = sidebarMode;
    window.localStorage.setItem(STORAGE_KEYS.locale, locale);
    window.localStorage.setItem(STORAGE_KEYS.theme, theme);
    window.localStorage.setItem(STORAGE_KEYS.sidebarMode, sidebarMode);
    const themeColors = document.querySelectorAll<HTMLMetaElement>('meta[name="theme-color"]');
    const syncThemeColor = (dark: boolean) => themeColors.forEach((meta) => meta.setAttribute("content", dark ? "#07101e" : "#f7f8fe"));
    syncThemeColor(resolvedTheme === "dark");
    const media = window.matchMedia("(prefers-color-scheme: dark)");
    const syncSystem = () => {
      if (theme !== "system") return;
      root.dataset.theme = media.matches ? "dark" : "light";
      syncThemeColor(media.matches);
    };
    media.addEventListener("change", syncSystem);
    return () => media.removeEventListener("change", syncSystem);
  }, [locale, preferencesLoaded, theme, sidebarMode]);

  const value = useMemo<PanelContextValue>(
    () => ({
      locale,
      theme,
      sidebarMode,
      workspaceName: "",
      siteName: "",
      setWorkspaceName: () => undefined,
      setSiteName: () => undefined,
      setLocale: setLocaleState,
      setTheme: setThemeState,
      setSidebarMode: setSidebarModeState,
      toggleLocale: () => setLocaleState((current) => (current === "en" ? "fa" : "en")),
      toggleTheme: () => setThemeState((current) => (current === "light" ? "dark" : current === "dark" ? "system" : "light")),
      cycleSidebarMode: () => setSidebarModeState((current) => (current === "open" ? "half" : current === "half" ? "closed" : "open")),
      isRtl: locale === "fa"
    }),
    [locale, theme, sidebarMode]
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
