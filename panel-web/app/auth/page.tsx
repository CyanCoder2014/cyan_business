"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useCallback, useEffect, useMemo, useState } from "react";
import {
  ArrowRightIcon,
  BuildingIcon,
  DocumentIcon,
  EyeIcon,
  EyeOffIcon,
  GitHubIcon,
  GlobeIcon,
  GoogleIcon,
  LayersIcon,
  LightningIcon,
  LockIcon,
  MailIcon,
  PhoneDeviceIcon,
  PhoneIcon,
  PlaneIcon,
  ShieldIcon,
  ShopIcon,
  SparkleIcon,
  UsersIcon
} from "@/components/auth-icons";
import { usePanel } from "@/components/panel-provider";
import { LogoMark } from "@/components/logo-mark";
import { createCaptchaChallenge, loginWithPassword, registerPanelUser, sendLoginOtp } from "@/lib/platform-auth";

type AuthMode = "signin" | "signup";
type CaptchaState = {
  challengeId: string;
  prompt: string;
};

const FEATURES = [
  {
    key: "website",
    icon: GlobeIcon,
    tone: "blue",
    title: "Website & PWA",
    titleFa: "وب‌سایت و PWA",
    body: "Marketing sites, portals, and progressive web apps.",
    bodyFa: "سایت‌های بازاریابی، پورتال‌ها و اپ‌های پیش‌رونده."
  },
  {
    key: "shop",
    icon: ShopIcon,
    tone: "pink",
    title: "Shop",
    titleFa: "فروشگاه",
    body: "Catalogs, carts, checkout, and payments.",
    bodyFa: "کاتالوگ، سبد خرید، تسویه‌حساب و پرداخت."
  },
  {
    key: "crm",
    icon: UsersIcon,
    tone: "violet",
    title: "CRM",
    titleFa: "CRM",
    body: "Manage leads, contacts, deals, and relationships.",
    bodyFa: "سرنخ‌ها، مخاطبان، معاملات و ارتباطات را مدیریت کنید."
  },
  {
    key: "bpm",
    icon: DocumentIcon,
    tone: "amber",
    title: "BPM / Forms",
    titleFa: "BPM / فرم‌ها",
    body: "Smart forms, approvals, and workflow routing.",
    bodyFa: "فرم‌های هوشمند، تأییدها و مسیریابی گردش‌کار."
  },
  {
    key: "automation",
    icon: LightningIcon,
    tone: "blue",
    title: "Automation",
    titleFa: "اتوماسیون",
    body: "Connect services and remove repetitive busywork.",
    bodyFa: "سرویس‌ها را متصل کنید و کارهای تکراری را حذف کنید."
  },
  {
    key: "bots",
    icon: PlaneIcon,
    tone: "cyan",
    title: "Telegram & Bale Bots",
    titleFa: "ربات تلگرام و بله",
    body: "Run customer support and workflows in messaging channels.",
    bodyFa: "پشتیبانی مشتری و فلوها را در کانال‌های پیام‌رسان اجرا کنید."
  }
] as const;

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
  const [mode, setMode] = useState<AuthMode>(requestedMode === "register" || requestedMode === "signup" ? "signup" : "signin");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [workspace, setWorkspace] = useState("");
  const [phone, setPhone] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [captchaAnswer, setCaptchaAnswer] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [otpLoading, setOtpLoading] = useState(false);
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
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "بارگذاری بررسی امنیتی ناموفق بود." : "Failed to load security check");
    }
  }, [locale]);

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
    return locale === "fa" ? "ادامه با ایمیل" : "Continue with email";
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
        captchaAnswer,
        otpCode: otpCode.trim() || undefined
      });
      router.replace(returnTo);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : mode === "signin" ? "Sign in failed." : "Registration failed.");
      await loadCaptcha().catch(() => null);
    } finally {
      setLoading(false);
    }
  }

  async function handleSendOtp() {
    if (!captcha || !captchaAnswer.trim()) {
      setStatus(locale === "fa" ? "ابتدا پاسخ بررسی امنیتی را وارد کنید." : "Complete the security answer before requesting a login code.");
      return;
    }
    setOtpLoading(true);
    setStatus(null);
    try {
      const response = await sendLoginOtp(email, captcha.challengeId, captchaAnswer.trim());
      await loadCaptcha();
      setStatus(response.devCode
        ? (locale === "fa" ? `کد توسعه: ${response.devCode}` : `Development login code: ${response.devCode}`)
        : (locale === "fa" ? "کد ورود ارسال شد." : "Login code sent."));
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to send login code.");
    } finally {
      setOtpLoading(false);
    }
  }

  function handleSocialLogin(provider: "google" | "github") {
    const labels = {
      google: locale === "fa" ? "گوگل" : "Google",
      github: "GitHub"
    };
    setStatus(
      locale === "fa"
        ? `ورود با ${labels[provider]} برای این محیط هنوز پیکربندی نشده است.`
        : `${labels[provider]} sign-in is not configured in this environment yet.`
    );
  }

  function handleMagicLink() {
    setStatus(
      locale === "fa"
        ? "ورود با لینک جادویی برای این محیط هنوز پیکربندی نشده است."
        : "Magic link sign-in is not configured in this environment yet."
    );
  }

  return (
    <main className="auth-shell">
      <div className="auth-body">
        <section className="auth-marketing">
          <Link href="/" className="brand-lockup auth-brand-lockup">
            <div className="brand-badge"><LogoMark/></div>
            <strong>Cyan</strong>
          </Link>

          <div className="auth-hero">
            <p className="auth-kicker">
              <SparkleIcon size={14} />
              <span>{locale === "fa" ? "پلتفرم هوشمند اپلیکیشن" : "AI-native app platform"}</span>
            </p>
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
            <p className="auth-hero-copy">
              {locale === "fa"
                ? "Cyan به تیم‌ها کمک می‌کند اپ‌های کسب‌وکار مدرن را سریع‌تر از همیشه بسازند، خودکار کنند و مقیاس دهند."
                : "Cyan helps teams build, automate, and scale modern business apps — faster than ever."}
            </p>
          </div>

          <div className="auth-feature-columns">
            {FEATURES.map((feature) => {
              const Icon = feature.icon;
              return (
                <div key={feature.key} className="auth-feature-item">
                  <div className={`auth-feature-icon tone-${feature.tone}`}>
                    <Icon />
                  </div>
                  <div>
                    <strong>{locale === "fa" ? feature.titleFa : feature.title}</strong>
                    <p>{locale === "fa" ? feature.bodyFa : feature.body}</p>
                  </div>
                </div>
              );
            })}
          </div>

          <div className="auth-mini-board">
            <div className="auth-mini-card">
              <strong>{locale === "fa" ? "استودیوی هوش مصنوعی" : "AI Studio"}</strong>
              <div className="auth-mini-chat">
                <span className="auth-mini-chat-label">Cyan AI</span>
                <p>
                  {locale === "fa"
                    ? "سلام! من Cyan AI هستم. امروز چه چیزی می‌خواهید بسازید؟"
                    : "Hi! I'm Cyan AI. What would you like to build today?"}
                </p>
              </div>
            </div>
            <div className="auth-mini-card">
              <strong>{locale === "fa" ? "قالب‌ها" : "Blueprints"}</strong>
              <ul className="auth-mini-list">
                <li>{locale === "fa" ? "شروع فروشگاه" : "Shop Starter"}</li>
                <li>{locale === "fa" ? "شروع CRM" : "CRM Starter"}</li>
                <li>{locale === "fa" ? "میز پشتیبانی" : "Support Desk"}</li>
              </ul>
            </div>
            <div className="auth-mini-card">
              <strong>{locale === "fa" ? "فلوساز" : "Flow Builder"}</strong>
              <div className="auth-flow-steps">
                <span>{locale === "fa" ? "ارسال فرم" : "Form Submitted"}</span>
                <span>{locale === "fa" ? "ارسال ایمیل" : "Send Email"}</span>
                <span>{locale === "fa" ? "ایجاد تسک" : "Create Task"}</span>
              </div>
            </div>
          </div>

          <div className="auth-trust-row">
            <div className="auth-trust-card">
              <div className="auth-trust-icon">
                <ShieldIcon />
              </div>
              <div>
                <strong>{locale === "fa" ? "امن از پایه" : "Secure by design"}</strong>
                <p>{locale === "fa" ? "امنیت سازمانی و داده‌های رمزگذاری‌شده." : "Enterprise-grade security and encrypted data."}</p>
              </div>
            </div>
            <div className="auth-trust-card">
              <div className="auth-trust-icon">
                <LayersIcon />
              </div>
              <div>
                <strong>{locale === "fa" ? "آماده چندمستاجری" : "Multi-tenant ready"}</strong>
                <p>{locale === "fa" ? "فضاهای کاری مجزا برای تیم‌ها و مشتریان." : "Isolated workspaces for teams and clients."}</p>
              </div>
            </div>
            <div className="auth-trust-card">
              <div className="auth-trust-icon">
                <PhoneDeviceIcon />
              </div>
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
              otpCode={otpCode}
              otpLoading={otpLoading}
              password={password}
              phone={phone}
              setCaptchaAnswer={setCaptchaAnswer}
              setEmail={setEmail}
              setOtpCode={setOtpCode}
              setPassword={setPassword}
              setPhone={setPhone}
              setShowPassword={setShowPassword}
              setWorkspace={setWorkspace}
              showPassword={showPassword}
              status={status}
              submitLabel={submitLabel}
              workspace={workspace}
              onMagicLink={handleMagicLink}
              onSendOtp={handleSendOtp}
              onRefreshCaptcha={loadCaptcha}
              onSocialLogin={handleSocialLogin}
              onSubmit={handleSubmit}
            />
          </div>

          <div className="mobile-only mobile-screen">
            <div className="mobile-phone-strip">
              <strong>9:41</strong>
            </div>
            <div className="mobile-card">
              <div className="mobile-brand">
                <div className="brand-badge"><LogoMark/></div>
                <strong style={{ fontSize: "2rem" }}>Cyan</strong>
              </div>
              <h2 className="auth-mobile-title">
                {locale === "fa" ? "کسب‌وکار خود را با هوش مصنوعی بسازید" : "Launch your business app with AI"}
              </h2>
              <p className="muted auth-mobile-copy">
                {locale === "fa"
                  ? "Cyan به تیم‌ها کمک می‌کند اپ‌های کسب‌وکار مدرن را سریع‌تر از همیشه بسازند، خودکار کنند و مقیاس دهند."
                  : "Cyan helps teams build, automate, and scale modern business apps — faster than ever."}
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
                otpCode={otpCode}
                otpLoading={otpLoading}
                password={password}
                phone={phone}
                setCaptchaAnswer={setCaptchaAnswer}
                setEmail={setEmail}
                setOtpCode={setOtpCode}
                setPassword={setPassword}
                setPhone={setPhone}
                setShowPassword={setShowPassword}
                setWorkspace={setWorkspace}
                showPassword={showPassword}
                status={status}
                submitLabel={submitLabel}
                workspace={workspace}
                onMagicLink={handleMagicLink}
                onSendOtp={handleSendOtp}
                onRefreshCaptcha={loadCaptcha}
                onSocialLogin={handleSocialLogin}
                onSubmit={handleSubmit}
              />
            </div>
          </div>
        </section>
      </div>

      <footer className="auth-footer">
        <span>{locale === "fa" ? "© ۲۰۲۴ Cyan Labs, Inc. تمامی حقوق محفوظ است." : "© 2024 Cyan Labs, Inc. All rights reserved."}</span>
        <div className="auth-footer-links">
          <Link href="#">{locale === "fa" ? "شرایط استفاده" : "Terms of Service"}</Link>
          <Link href="#">{locale === "fa" ? "حریم خصوصی" : "Privacy Policy"}</Link>
          <Link href="#">{locale === "fa" ? "مستندات" : "Docs"}</Link>
          <Link href="#">{locale === "fa" ? "تغییرات" : "Changelog"}</Link>
          <Link href="#">{locale === "fa" ? "وضعیت" : "Status"}</Link>
        </div>
      </footer>
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
  otpCode,
  otpLoading,
  password,
  phone,
  setCaptchaAnswer,
  setEmail,
  setOtpCode,
  setPassword,
  setPhone,
  setShowPassword,
  setWorkspace,
  showPassword,
  status,
  submitLabel,
  workspace,
  onMagicLink,
  onSendOtp,
  onRefreshCaptcha,
  onSocialLogin,
  onSubmit
}: {
  captcha: CaptchaState | null;
  captchaAnswer: string;
  compact?: boolean;
  email: string;
  loading: boolean;
  locale: "en" | "fa";
  mode: AuthMode;
  otpCode: string;
  otpLoading: boolean;
  password: string;
  phone: string;
  setCaptchaAnswer: (value: string) => void;
  setEmail: (value: string) => void;
  setOtpCode: (value: string) => void;
  setPassword: (value: string) => void;
  setPhone: (value: string) => void;
  setShowPassword: (value: boolean) => void;
  setWorkspace: (value: string) => void;
  showPassword: boolean;
  status: string | null;
  submitLabel: string;
  workspace: string;
  onMagicLink: () => void;
  onSendOtp: () => Promise<void>;
  onRefreshCaptcha: () => Promise<void>;
  onSocialLogin: (provider: "google" | "github") => void;
  onSubmit: () => Promise<void>;
}) {
  const passwordInputId = compact ? "mobile-auth-password" : "desktop-auth-password";

  return (
    <form
      className="auth-form"
      data-testid={compact ? "mobile-auth-form" : "desktop-auth-form"}
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit().catch(() => null);
      }}
    >
      <label className="auth-field">
        <span>{locale === "fa" ? "ایمیل کاری" : "Work email"}</span>
        <div className="auth-input-shell">
          <MailIcon className="auth-input-icon" />
          <input
            autoComplete="email"
            inputMode="email"
            placeholder="name@company.com"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>
      </label>

      <div className="auth-field">
        <span>
          <label htmlFor={passwordInputId}>{locale === "fa" ? "رمز عبور" : "Password"}</label>
        </span>
        <div className="auth-input-shell">
          <LockIcon className="auth-input-icon" />
          <input
            id={passwordInputId}
            autoComplete={mode === "signin" ? "current-password" : "new-password"}
            placeholder={
              mode === "signin"
                ? locale === "fa"
                  ? "رمز عبور خود را وارد کنید"
                  : "Enter your password"
                : locale === "fa"
                  ? "یک رمز قوی بسازید"
                  : "Create a strong password"
            }
            type={showPassword ? "text" : "password"}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <button
            type="button"
            className="auth-input-action"
            aria-label={showPassword ? (locale === "fa" ? "پنهان کردن رمز" : "Hide password") : locale === "fa" ? "نمایش رمز" : "Show password"}
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? <EyeOffIcon /> : <EyeIcon />}
          </button>
        </div>
      </div>

      {mode === "signup" ? (
        <>
          <label className="auth-field">
            <span>{locale === "fa" ? "نام فضای کاری" : "Workspace name"}</span>
            <div className="auth-input-shell">
              <BuildingIcon className="auth-input-icon" />
              <input
                autoComplete="organization"
                placeholder={locale === "fa" ? "شرکت نمونه" : "Acme Corporation"}
                value={workspace}
                onChange={(event) => setWorkspace(event.target.value)}
              />
            </div>
            <small className="auth-field-hint">
              {locale === "fa" ? "این نام فضای کاری شما در Cyan خواهد بود." : "This will be your workspace name on Cyan."}
            </small>
          </label>

          <label className="auth-field">
            <span>{locale === "fa" ? "تلفن (اختیاری)" : "Phone (optional)"}</span>
            <div className="auth-input-shell auth-phone-shell">
              <PhoneIcon className="auth-input-icon" />
              <span className="auth-phone-prefix" aria-hidden="true">
                +1
              </span>
              <input
                autoComplete="tel"
                placeholder="(555) 123-4567"
                value={phone}
                onChange={(event) => setPhone(event.target.value)}
              />
            </div>
            <small className="auth-field-hint">
              {locale === "fa" ? "برای به‌روزرسانی‌های مهم و بازیابی از آن استفاده می‌کنیم." : "We'll use this for important updates and recovery."}
            </small>
          </label>
        </>
      ) : null}

      <label className="auth-field">
        <span>{locale === "fa" ? "پاسخ امنیتی" : "Security answer"}</span>
        <div className="auth-captcha-row">
          <input
            autoComplete="off"
            inputMode="numeric"
            placeholder={captcha?.prompt ?? (locale === "fa" ? "در حال آماده‌سازی..." : "Loading...")}
            value={captchaAnswer}
            onChange={(event) => setCaptchaAnswer(event.target.value)}
          />
          <button type="button" className="secondary-pill auth-refresh-btn" onClick={() => onRefreshCaptcha()} disabled={loading}>
            {locale === "fa" ? "تازه‌سازی" : "Refresh"}
          </button>
        </div>
      </label>

      {mode === "signin" ? (
        <label className="auth-field">
          <span>{locale === "fa" ? "کد ورود دومرحله‌ای (در صورت فعال بودن)" : "Two-factor login code (if enabled)"}</span>
          <div className="auth-captcha-row">
            <input
              aria-label={locale === "fa" ? "کد ورود دومرحله‌ای" : "Two-factor login code"}
              autoComplete="one-time-code"
              inputMode="numeric"
              value={otpCode}
              onChange={(event) => setOtpCode(event.target.value)}
            />
            <button type="button" className="secondary-pill auth-refresh-btn" onClick={() => onSendOtp()} disabled={loading || otpLoading || !email.trim()}>
              {otpLoading ? (locale === "fa" ? "در حال ارسال…" : "Sending…") : (locale === "fa" ? "ارسال کد" : "Send code")}
            </button>
          </div>
        </label>
      ) : null}

      {status ? <div className="status-pill danger">{status}</div> : null}

      <button type="submit" className="primary-pill auth-submit" disabled={loading || !captcha}>
        <span>{submitLabel}</span>
        {mode === "signup" ? <ArrowRightIcon /> : null}
      </button>

      <div className="auth-divider">
        <span>{locale === "fa" ? "یا ادامه با" : "or continue with"}</span>
      </div>

      <div className="auth-socials">
        <button type="button" className="auth-social-btn" onClick={() => onSocialLogin("google")} disabled={loading}>
          <GoogleIcon />
          <span>{locale === "fa" ? "ادامه با Google" : "Continue with Google"}</span>
        </button>
        <button type="button" className="auth-social-btn" onClick={() => onSocialLogin("github")} disabled={loading}>
          <GitHubIcon />
          <span>{locale === "fa" ? "ادامه با GitHub" : "Continue with GitHub"}</span>
        </button>
      </div>

      {!compact ? (
        <>
          <p className="auth-magic-link">
            {locale === "fa" ? "لینک جادویی را ترجیح می‌دهید؟" : "Prefer a magic link?"}{" "}
            <button type="button" className="text-link" onClick={onMagicLink}>
              {locale === "fa" ? "یک لینک ورود امن برایتان ایمیل می‌کنیم." : "We'll email you a secure sign-in link."}
            </button>
          </p>
          <p className="auth-legal">
            {locale === "fa" ? "با ادامه دادن، " : "By continuing, you agree to our "}
            <Link href="#">{locale === "fa" ? "شرایط سرویس" : "Terms of Service"}</Link>
            {locale === "fa" ? " و " : " and "}
            <Link href="#">{locale === "fa" ? "سیاست حریم خصوصی" : "Privacy Policy"}</Link>
            {locale === "fa" ? " را می‌پذیرید." : "."}
          </p>
        </>
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
