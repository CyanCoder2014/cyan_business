"use client";

import { useEffect, useState } from "react";

type ThemeMode = "dark" | "light";
type LocaleMode = "en" | "fa";

export function WorkspaceControls() {
  const [theme, setTheme] = useState<ThemeMode>("dark");
  const [locale, setLocale] = useState<LocaleMode>("en");

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.dataset.locale = locale;
    document.documentElement.dir = locale === "fa" ? "rtl" : "ltr";
    document.documentElement.lang = locale;
  }, [theme, locale]);

  return (
    <div className="workspace-controls" aria-label="Workspace preferences">
      <button type="button" className="chip" onClick={() => setTheme((value) => (value === "dark" ? "light" : "dark"))}>
        {theme === "dark" ? "Dark" : "Light"}
      </button>
      <button type="button" className="chip" onClick={() => setLocale((value) => (value === "en" ? "fa" : "en"))}>
        {locale === "en" ? "EN" : "FA"}
      </button>
    </div>
  );
}
