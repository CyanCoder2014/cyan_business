"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useCallback, useEffect, useMemo, useState } from "react";
import { usePanel } from "@/components/panel-provider";
import { createCaptchaChallenge, loginWithPassword, registerPanelUser } from "@/lib/platform-auth";

type AuthMode = "signin" | "signup";
type CaptchaState = {
  challengeId: string;
  prompt: string;
};

export default function AuthPage() {
  return (
    <Suspense fallback={<main className="auth-shell" />}>
      <AuthScreen />
    </Suspense>
  );
}

function AuthScreen() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { locale, setWorkspaceName } = usePanel();
  const returnTo = safeReturnTo(searchParams.get("returnTo"));
  const requestedMode = searchParams.get("mode");
  const [mode, setMode] = useState<AuthMode>(requestedMode === "register" || requestedMode === "signup" ? "signup" : returnTo === "/" ? "signup" : "signin");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [workspace, setWorkspace] = useState("");
  const [phone, setPhone] = useState("");
  const [captchaAnswer, setCaptchaAnswer] = useState("");
  const [captcha, setCaptcha] = useState<CaptchaState | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (requestedMode === "signin") {
      setMode("signin");
    } else if (requestedMode === "register" || requestedMode === "signup") {
      setMode("signup");
    }
  }, [requestedMode]);

  const loadCaptcha = useCallback(async () => {
    try {
      const challenge = await createCaptchaChallenge();
      setCaptcha({ challengeId: challenge.challengeId, prompt: challenge.prompt });
      setCaptchaAnswer("");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to load security check");
    }
  }, []);

  useEffect(() => {
    loadCaptcha().catch(() => null);
  }, [loadCaptcha]);

  const submitLabel = useMemo(() => {
    if (loading) {
      return locale === "fa" ? "در حال ارسال..." : "Working...";
    }
    if (mode === "signin") {
      return locale === "fa" ? "ورود" : "Sign in";
    }
    return locale === "fa" ? "ساخت حساب" : "Create account";
  }, [loading, locale, mode]);

  async function handleSubmit() {
    if (!captcha) {
      setStatus(locale === "fa" ? "بررسی امنیتی آماده نیست." : "Security check is not ready.");
      return;
    }
    if (!email.trim() || !password) {
      setStatus(locale === "fa" ? "ایمیل و رمز عبور لازم است." : "Email and password are required.");
      return;
    }

    setLoading(true);
    setStatus(null);
    try {
      if (mode === "signup") {
        await registerPanelUser({
          email,
          password,
          phoneNumber: phone
        });
        if (workspace.trim()) {
          setWorkspaceName(workspace.trim());
        }
      }

      await loginWithPassword({
        username: email,
        password,
        captchaChallengeId: captcha.challengeId,
        captchaAnswer
      });
      router.replace(returnTo);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : mode === "signin" ? "Sign in failed." : "Registration failed.");
      await loadCaptcha().catch(() => null);
    } finally {
      setLoading(false);
    }
  }

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
          <span>{mode === "signin" ? (locale === "fa" ? "جدید در Cyan؟" : "New to Cyan?") : locale === "fa" ? "حساب دارید؟" : "Already have an account?"}</span>
          <button type="button" className="text-link" onClick={() => setMode(mode === "signin" ? "signup" : "signin")}>
            {mode === "signin" ? (locale === "fa" ? "ساخت حساب" : "Create account") : locale === "fa" ? "ورود" : "Sign in"}
          </button>
        </div>

        <div className="auth-card desktop-only">
          <AuthTabs locale={locale} mode={mode} setMode={setMode} />
          <AuthForm
            captcha={captcha}
            captchaAnswer={captchaAnswer}
            email={email}
            loading={loading}
            locale={locale}
            mode={mode}
            password={password}
            phone={phone}
            setCaptchaAnswer={setCaptchaAnswer}
            setEmail={setEmail}
            setPassword={setPassword}
            setPhone={setPhone}
            setWorkspace={setWorkspace}
            status={status}
            submitLabel={submitLabel}
            workspace={workspace}
            onRefreshCaptcha={loadCaptcha}
            onSubmit={handleSubmit}
          />
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
            <AuthTabs locale={locale} mode={mode} setMode={setMode} />
            <AuthForm
              compact
              captcha={captcha}
              captchaAnswer={captchaAnswer}
              email={email}
              loading={loading}
              locale={locale}
              mode={mode}
              password={password}
              phone={phone}
              setCaptchaAnswer={setCaptchaAnswer}
              setEmail={setEmail}
              setPassword={setPassword}
              setPhone={setPhone}
              setWorkspace={setWorkspace}
              status={status}
              submitLabel={submitLabel}
              workspace={workspace}
              onRefreshCaptcha={loadCaptcha}
              onSubmit={handleSubmit}
            />
          </div>
        </div>
      </section>
    </main>
  );
}

function AuthTabs({
  locale,
  mode,
  setMode
}: {
  locale: "en" | "fa";
  mode: AuthMode;
  setMode: (mode: AuthMode) => void;
}) {
  return (
    <div className="auth-tabs">
      <button type="button" className={mode === "signin" ? "active" : ""} onClick={() => setMode("signin")}>
        {locale === "fa" ? "ورود" : "Sign in"}
      </button>
      <button type="button" className={mode === "signup" ? "active" : ""} onClick={() => setMode("signup")}>
        {locale === "fa" ? "ساخت حساب" : "Create account"}
      </button>
    </div>
  );
}

function AuthForm({
  captcha,
  captchaAnswer,
  compact = false,
  email,
  loading,
  locale,
  mode,
  password,
  phone,
  setCaptchaAnswer,
  setEmail,
  setPassword,
  setPhone,
  setWorkspace,
  status,
  submitLabel,
  workspace,
  onRefreshCaptcha,
  onSubmit
}: {
  captcha: CaptchaState | null;
  captchaAnswer: string;
  compact?: boolean;
  email: string;
  loading: boolean;
  locale: "en" | "fa";
  mode: AuthMode;
  password: string;
  phone: string;
  setCaptchaAnswer: (value: string) => void;
  setEmail: (value: string) => void;
  setPassword: (value: string) => void;
  setPhone: (value: string) => void;
  setWorkspace: (value: string) => void;
  status: string | null;
  submitLabel: string;
  workspace: string;
  onRefreshCaptcha: () => Promise<void>;
  onSubmit: () => Promise<void>;
}) {
  return (
    <form
      className="auth-form"
      data-testid={compact ? "mobile-auth-form" : "desktop-auth-form"}
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit().catch(() => null);
      }}
    >
      <label>
        <span>{locale === "fa" ? "ایمیل کاری" : "Work email"}</span>
        <input
          autoComplete="email"
          inputMode="email"
          placeholder="name@company.com"
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
        />
      </label>
      <label>
        <span>{locale === "fa" ? "رمز عبور" : "Password"}</span>
        <input
          autoComplete={mode === "signin" ? "current-password" : "new-password"}
          placeholder={mode === "signin" ? (locale === "fa" ? "رمز عبور" : "Enter your password") : locale === "fa" ? "یک رمز قوی بسازید" : "Create a strong password"}
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />
      </label>
      {mode === "signup" ? (
        <>
          <label>
            <span>{locale === "fa" ? "نام فضای کاری" : "Workspace name"}</span>
            <input
              autoComplete="organization"
              placeholder={locale === "fa" ? "شرکت آکمان" : "Acme Corp"}
              value={workspace}
              onChange={(event) => setWorkspace(event.target.value)}
            />
          </label>
          <label>
            <span>{locale === "fa" ? "تلفن (اختیاری)" : "Phone number (optional)"}</span>
            <input
              autoComplete="tel"
              placeholder="+1 (555) 123-4567"
              value={phone}
              onChange={(event) => setPhone(event.target.value)}
            />
          </label>
        </>
      ) : null}
      <label>
        <span>{locale === "fa" ? "پاسخ امنیتی" : "Security answer"}</span>
        <div className="auth-captcha-row">
          <input
            autoComplete="off"
            inputMode="numeric"
            placeholder={captcha?.prompt ?? (locale === "fa" ? "در حال آماده‌سازی..." : "Loading...")}
            value={captchaAnswer}
            onChange={(event) => setCaptchaAnswer(event.target.value)}
          />
          <button type="button" className="secondary-pill" onClick={() => onRefreshCaptcha()} disabled={loading}>
            {locale === "fa" ? "تازه‌سازی" : "Refresh"}
          </button>
        </div>
      </label>
      {status ? <div className="status-pill danger">{status}</div> : null}
      <button type="submit" className="primary-pill auth-submit" disabled={loading || !captcha}>
        {submitLabel}
      </button>
      {!compact ? (
        <p className="auth-legal">
          {locale === "fa" ? "با ادامه دادن، شرایط سرویس و سیاست حریم خصوصی را می‌پذیرید." : "By continuing, you agree to our Terms of Service and Privacy Policy."}
        </p>
      ) : null}
    </form>
  );
}

function safeReturnTo(value: string | null) {
  if (!value || !value.startsWith("/") || value.startsWith("//")) {
    return "/";
  }
  return value;
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
