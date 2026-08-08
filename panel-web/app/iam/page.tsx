"use client";

import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import {
  getIamUser,
  resolveIamAccess,
} from "@/lib/service-api";
import { getPlatformSessionId, getPlatformUsername, logoutPlatformSession } from "@/lib/platform-auth";
import type { IamUserAccessSummary, UserSummary } from "@/lib/types";

export default function IamPage() {
  const { locale, workspaceName, siteName, setWorkspaceName, setSiteName } = usePanel();
  const [username, setUsername] = useState("");
  const [profile, setProfile] = useState<UserSummary | null>(null);
  const [access, setAccess] = useState<IamUserAccessSummary | null>(null);
  const [workspaceDraft, setWorkspaceDraft] = useState(workspaceName);
  const [siteDraft, setSiteDraft] = useState(siteName);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const currentUsername = getPlatformUsername();
    setUsername(currentUsername);
    if (!currentUsername) {
      return;
    }
    Promise.all([
      getIamUser(currentUsername).catch(() => null),
      resolveIamAccess(currentUsername, "cyan-panel").catch(() => null)
    ]).then(([user, resolvedAccess]) => {
      setProfile(user);
      setAccess(resolvedAccess);
    }).catch((error) => {
      setStatus(error instanceof Error ? error.message : "Failed to load account details");
    });
  }, []);

  useEffect(() => {
    setWorkspaceDraft(workspaceName);
    setSiteDraft(siteName);
  }, [workspaceName, siteName]);

  async function handleLogout() {
    setLoading(true);
    setStatus(null);
    try {
      await logoutPlatformSession();
      window.location.assign("/auth");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Logout failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <PanelShell
      activeKey="iam"
      title="Profile & Settings"
      titleFa="پروفایل و تنظیمات"
      subtitle="Review the active account, panel preferences, and current access scopes."
      subtitleFa="حساب فعال، تنظیمات پنل و سطح دسترسی جاری را بررسی کنید."
    >
      <div className="desktop-only two-column-grid">
        <section className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "حساب کاربری" : "Account"}</h3>
            <span className="status-pill info">{locale === "fa" ? "زنده" : "Live"}</span>
          </div>
          <div className="summary-grid" style={{ marginTop: 18 }}>
            <div className="mini-card"><strong>{profile?.username ?? username ?? "—"}</strong><span className="muted-block">{locale === "fa" ? "نام کاربری" : "Username"}</span></div>
            <div className="mini-card"><strong>{profile?.email ?? "—"}</strong><span className="muted-block">{locale === "fa" ? "ایمیل" : "Email"}</span></div>
            <div className="mini-card"><strong>{profile?.phoneNumber ?? "—"}</strong><span className="muted-block">{locale === "fa" ? "تلفن" : "Phone"}</span></div>
            <div className="mini-card"><strong>{profile?.mfaEnabled ? (locale === "fa" ? "فعال" : "Enabled") : locale === "fa" ? "غیرفعال" : "Disabled"}</strong><span className="muted-block">{locale === "fa" ? "ورود دومرحله‌ای" : "MFA"}</span></div>
          </div>
          <div className="panel-card" style={{ marginTop: 18 }}>
            <strong>{locale === "fa" ? "نشست فعال" : "Active session"}</strong>
            <div className="muted-block" style={{ marginTop: 8, overflowWrap: "anywhere" }}>{getPlatformSessionId() || "—"}</div>
            <div className="pill-row" style={{ marginTop: 16 }}>
              <button type="button" className="primary-pill" onClick={() => handleLogout().catch(() => null)} disabled={loading}>
                {loading ? (locale === "fa" ? "در حال خروج..." : "Signing out...") : locale === "fa" ? "خروج از حساب" : "Sign out"}
              </button>
            </div>
          </div>
        </section>

        <section className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "ترجیحات پنل" : "Panel preferences"}</h3>
            <span className="muted">{locale === "fa" ? "ذخیره محلی" : "Stored locally"}</span>
          </div>
          <div className="form-grid" style={{ marginTop: 18 }}>
            <label style={{ display: "grid", gap: 8 }}>
              <span>{locale === "fa" ? "نام فضای کاری" : "Workspace name"}</span>
              <input value={workspaceDraft} onChange={(event) => setWorkspaceDraft(event.target.value)} />
            </label>
            <label style={{ display: "grid", gap: 8 }}>
              <span>{locale === "fa" ? "نام سایت" : "Site name"}</span>
              <input value={siteDraft} onChange={(event) => setSiteDraft(event.target.value)} />
            </label>
            <div className="pill-row">
              <button
                type="button"
                className="primary-pill"
                onClick={() => {
                  setWorkspaceName(workspaceDraft.trim());
                  setSiteName(siteDraft.trim());
                  setStatus(locale === "fa" ? "تنظیمات پنل ذخیره شد." : "Panel settings saved.");
                }}
              >
                {locale === "fa" ? "ذخیره تنظیمات" : "Save settings"}
              </button>
            </div>
          </div>

          <div className="card-title-row" style={{ marginTop: 24 }}>
            <h3>{locale === "fa" ? "دسترسی جاری" : "Current access"}</h3>
          </div>
          <pre className="json-view" style={{ marginTop: 14 }}>{JSON.stringify(access, null, 2)}</pre>
        </section>
      </div>

      {status ? <div className="status-pill info">{status}</div> : null}
    </PanelShell>
  );
}
