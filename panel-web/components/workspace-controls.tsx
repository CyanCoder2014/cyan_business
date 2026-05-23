"use client";

import { useEffect, useState } from "react";
import { getPlatformAuthToken, setPlatformAuthToken } from "@/lib/platform-auth";

type ThemeMode = "dark" | "light";
type LocaleMode = "en" | "fa";

export function WorkspaceControls() {
  const [theme, setTheme] = useState<ThemeMode>("light");
  const [locale, setLocale] = useState<LocaleMode>("en");
  const [authToken, setAuthToken] = useState("");

  useEffect(() => {
    setAuthToken(getPlatformAuthToken());
  }, []);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.dataset.locale = locale;
    document.documentElement.dir = locale === "fa" ? "rtl" : "ltr";
    document.documentElement.lang = locale;
  }, [theme, locale]);

  return (
    <div className="workspace-controls" aria-label="Workspace preferences">
      <input
        aria-label="Bearer token"
        placeholder="Bearer token"
        value={authToken}
        onChange={(event) => {
          const next = event.target.value;
          setAuthToken(next);
          setPlatformAuthToken(next);
        }}
        style={{ minWidth: 220 }}
      />
      <button type="button" className="chip" onClick={() => setTheme((value) => (value === "dark" ? "light" : "dark"))}>
        {theme === "dark" ? "Dark" : "Light"}
      </button>
      <button type="button" className="chip" onClick={() => setLocale((value) => (value === "en" ? "fa" : "en"))}>
        {locale === "en" ? "EN" : "FA"}
      </button>
    </div>
  );
}
