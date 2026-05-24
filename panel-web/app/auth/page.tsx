"use client";

import Link from "next/link";
import { useState } from "react";
import { usePanel } from "@/components/panel-provider";

export default function AuthPage() {
  const { locale } = usePanel();
  const [mode, setMode] = useState<"signin" | "signup">("signup");

  return (
    <main className="auth-shell">
      <section className="auth-marketing">
        <Link href="/" className="brand-lockup" style={{ paddingInline: 0 }}>
          <div className="brand-badge">C</div>
          <div>
            <strong>Cyan</strong>
            <span>{locale === "fa" ? "پلتفرم هوشمند ساخت اپ" : "AI-native app platform"}</span>
          </div>
        </Link>

        <div>
          <p className="page-kicker">{locale === "fa" ? "پلتفرم هوشمند اپلیکیشن" : "AI-native app platform"}</p>
          <h1>
            {locale === "fa" ? (
              <>
                کسب‌وکار خود را با <span className="gradient-text">هوش مصنوعی</span> بسازید
              </>
            ) : (
              <>
                Launch your business app with <span className="gradient-text">AI</span>
              </>
            )}
          </h1>
          <p>
            {locale === "fa"
              ? "وب‌سایت، PWA، فروشگاه، CRM، فرم‌ها، اتوماسیون و ربات‌ها را سریع‌تر از همیشه راه‌اندازی کنید."
              : "Build websites, PWAs, commerce, CRM, forms, automation, and bots from one workspace."}
          </p>
        </div>

        <div className="auth-feature-columns">
          {[
            ["Website & PWA", "Marketing sites, portals, and progressive web apps."],
            ["Automation", "Connect services and remove repetitive busywork."],
            ["CRM", "Manage leads, contacts, deals, and relationships."],
            ["Telegram & Bale", "Run customer support and workflows in messaging channels."]
          ].map(([title, body]) => (
            <div key={title} className="auth-feature-item">
              <div className="auth-feature-icon">✦</div>
              <div>
                <strong>{locale === "fa" ? translateAuth(title) : title}</strong>
                <p>{locale === "fa" ? translateAuth(body) : body}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="auth-trust-row">
          <div className="auth-trust-card">
            <div className="auth-feature-icon">◈</div>
            <div>
              <strong>{locale === "fa" ? "امن" : "Secure by design"}</strong>
              <p>{locale === "fa" ? "داده‌های رمزگذاری‌شده و استاندارد سازمانی." : "Enterprise-grade security and encrypted data."}</p>
            </div>
          </div>
          <div className="auth-trust-card">
            <div className="auth-feature-icon">◎</div>
            <div>
              <strong>{locale === "fa" ? "چندمستاجری" : "Multi-tenant ready"}</strong>
              <p>{locale === "fa" ? "فضاهای کاری مجزا برای تیم‌ها و مشتریان." : "Isolated workspaces for teams and clients."}</p>
            </div>
          </div>
          <div className="auth-trust-card">
            <div className="auth-feature-icon">▣</div>
            <div>
              <strong>{locale === "fa" ? "سازگار با موبایل" : "Mobile-friendly"}</strong>
              <p>{locale === "fa" ? "تجربه PWA برای هر دستگاه." : "PWA-ready experience on every device."}</p>
            </div>
          </div>
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-panel-head">
          <span>{locale === "fa" ? "جدید در Cyan؟" : "New to Cyan?"}</span>
          <Link href="/auth">{locale === "fa" ? "ساخت حساب" : "Create account"}</Link>
        </div>

        <div className="auth-card desktop-only">
          <div className="auth-tabs">
            <button type="button" className={mode === "signin" ? "active" : ""} onClick={() => setMode("signin")}>
              {locale === "fa" ? "ورود" : "Sign in"}
            </button>
            <button type="button" className={mode === "signup" ? "active" : ""} onClick={() => setMode("signup")}>
              {locale === "fa" ? "ساخت حساب" : "Create account"}
            </button>
          </div>
          <AuthForm locale={locale} />
        </div>

        <div className="mobile-only mobile-screen">
          <div className="mobile-phone-strip">
            <strong>9:41</strong>
          </div>
          <div className="mobile-card">
            <div className="mobile-brand">
              <div className="brand-badge">C</div>
              <strong style={{ fontSize: "2rem" }}>Cyan</strong>
            </div>
            <h2 style={{ textAlign: "center", marginTop: 18 }}>
              {locale === "fa" ? "کسب‌وکار خود را با هوش مصنوعی بسازید" : "Launch your business app with AI"}
            </h2>
            <p className="muted" style={{ textAlign: "center" }}>
              {locale === "fa" ? "در چند دقیقه بسازید، خودکار کنید و رشد دهید." : "Create, automate, and scale in minutes."}
            </p>
            <div className="auth-tabs" style={{ marginTop: 18 }}>
              <button type="button" className={mode === "signin" ? "active" : ""} onClick={() => setMode("signin")}>
                {locale === "fa" ? "ورود" : "Sign in"}
              </button>
              <button type="button" className={mode === "signup" ? "active" : ""} onClick={() => setMode("signup")}>
                {locale === "fa" ? "ساخت حساب" : "Create account"}
              </button>
            </div>
            <AuthForm locale={locale} compact />
          </div>
        </div>
      </section>
    </main>
  );
}

function AuthForm({ locale, compact = false }: { locale: "en" | "fa"; compact?: boolean }) {
  return (
    <div className="auth-form">
      <label>
        <span>{locale === "fa" ? "ایمیل کاری" : "Work email"}</span>
        <input placeholder={locale === "fa" ? "name@company.com" : "name@company.com"} />
      </label>
      <label>
        <span>{locale === "fa" ? "رمز عبور" : "Password"}</span>
        <input placeholder={locale === "fa" ? "یک رمز قوی بسازید" : "Create a strong password"} />
      </label>
      <label>
        <span>{locale === "fa" ? "نام فضای کاری" : "Workspace name"}</span>
        <input placeholder={locale === "fa" ? "شرکت آکمان" : "Acme Corp"} />
      </label>
      <label>
        <span>{locale === "fa" ? "تلفن (اختیاری)" : "Phone number (optional)"}</span>
        <input placeholder="+1 (555) 123-4567" />
      </label>
      <button type="button" className="primary-pill auth-submit">
        {locale === "fa" ? "ادامه با ایمیل" : "Continue with email"}
      </button>
      {!compact ? (
        <>
          <div className="auth-divider">
            <span>{locale === "fa" ? "یا" : "or continue with"}</span>
          </div>
          <div className="auth-socials">
            <button type="button" className="secondary-pill">
              Google
            </button>
            <button type="button" className="secondary-pill">
              GitHub
            </button>
          </div>
        </>
      ) : null}
      <p className="auth-magic-link">
        {locale === "fa" ? "لینک جادویی می‌خواهید؟ با ایمیل وارد شوید." : "Prefer a magic link? Sign in with email."}
      </p>
    </div>
  );
}

function translateAuth(value: string) {
  const dict: Record<string, string> = {
    "Website & PWA": "وب‌سایت و PWA",
    "Marketing sites, portals, and progressive web apps.": "سایت‌های بازاریابی، پورتال‌ها و اپ‌های پیش‌رونده.",
    Automation: "اتوماسیون",
    "Connect services and remove repetitive busywork.": "سرویس‌ها را متصل کنید و کارهای تکراری را حذف کنید.",
    CRM: "CRM",
    "Manage leads, contacts, deals, and relationships.": "سرنخ‌ها، مخاطبان، معاملات و ارتباطات را مدیریت کنید.",
    "Telegram & Bale": "تلگرام و بله",
    "Run customer support and workflows in messaging channels.": "پشتیبانی مشتری و فلوها را در کانال‌های پیام‌رسان اجرا کنید."
  };
  return dict[value] ?? value;
}
