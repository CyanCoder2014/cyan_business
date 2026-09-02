"use client";

import { useCallback, useEffect, useState } from "react";
import { AsyncButton } from "@/components/ui/primitives";
import { confirmPasswordReset, createCaptchaChallenge, requestPasswordReset } from "@/lib/platform-auth";

type Step = "request" | "confirm" | "done";

/**
 * Self-service reset. Kept as a dialog rather than another auth-page mode so it
 * carries its own captcha and code state instead of threading a third variant
 * through the sign-in form.
 */
export function PasswordResetDialog({ locale, initialUsername, onClose }: {
  locale: "en" | "fa";
  initialUsername: string;
  onClose: () => void;
}) {
  const fa = locale === "fa";
  const [step, setStep] = useState<Step>("request");
  const [username, setUsername] = useState(initialUsername);
  const [captcha, setCaptcha] = useState<{ challengeId: string; prompt: string } | null>(null);
  const [captchaAnswer, setCaptchaAnswer] = useState("");
  const [code, setCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadCaptcha = useCallback(async () => {
    try {
      const challenge = await createCaptchaChallenge();
      setCaptcha({ challengeId: challenge.challengeId, prompt: challenge.prompt });
      setCaptchaAnswer("");
    } catch {
      setCaptcha(null);
    }
  }, []);

  useEffect(() => { void loadCaptcha(); }, [loadCaptcha]);

  useEffect(() => {
    const onKey = (event: KeyboardEvent) => { if (event.key === "Escape" && !pending) onClose(); };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [onClose, pending]);

  const submitRequest = async () => {
    if (!captcha || !captchaAnswer.trim() || !username.trim()) return;
    setPending(true); setError(null);
    try {
      await requestPasswordReset(username, captcha.challengeId, captchaAnswer.trim(), locale);
      setStep("confirm");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : String(reason));
      await loadCaptcha();
    } finally { setPending(false); }
  };

  const submitConfirm = async () => {
    if (newPassword !== confirmPassword) {
      setError(fa ? "رمزهای عبور یکسان نیستند." : "The two passwords do not match.");
      return;
    }
    setPending(true); setError(null);
    try {
      await confirmPasswordReset(username, code.trim(), newPassword);
      setStep("done");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : String(reason));
    } finally { setPending(false); }
  };

  return <div className="dialog-backdrop" onMouseDown={() => !pending && onClose()}>
    <section className="access-drawer" role="dialog" aria-modal="true" aria-label={fa ? "بازیابی رمز عبور" : "Reset password"} onMouseDown={event => event.stopPropagation()}>
      <div className="drawer-head">
        <div>
          <p className="page-kicker">{fa ? "بازیابی حساب" : "Account recovery"}</p>
          <h2>{fa ? "بازیابی رمز عبور" : "Reset your password"}</h2>
        </div>
        <button className="header-icon-button" disabled={pending} onClick={onClose} aria-label={fa ? "بستن" : "Close"}>×</button>
      </div>

      {error ? <div className="operational-banner error" role="alert">{error}</div> : null}

      {step === "request" ? <div className="access-form">
        <p>{fa
          ? "کد بازیابی به شماره ثبت‌شده روی این حساب پیامک می‌شود."
          : "We'll text a recovery code to the phone number on file for this account."}</p>
        <label>
          <span>{fa ? "ایمیل یا نام کاربری" : "Email or username"}</span>
          <input dir="ltr" value={username} disabled={pending} onChange={event => setUsername(event.target.value)}/>
        </label>
        <label>
          <span>{fa ? "پاسخ بررسی امنیتی" : "Security answer"}</span>
          <input value={captchaAnswer} disabled={pending || !captcha} placeholder={captcha?.prompt ?? "…"} onChange={event => setCaptchaAnswer(event.target.value)}/>
        </label>
        <AsyncButton pending={pending} disabled={!username.trim() || !captchaAnswer.trim() || !captcha} onClick={submitRequest}>
          {fa ? "ارسال کد بازیابی" : "Send recovery code"}
        </AsyncButton>
      </div> : null}

      {step === "confirm" ? <div className="access-form">
        <p>{fa
          ? "اگر این حساب وجود داشته باشد، کدی برایش ارسال شده است. کد و رمز عبور جدید را وارد کنید."
          : "If that account exists, a code is on its way. Enter it along with your new password."}</p>
        <label>
          <span>{fa ? "کد بازیابی" : "Recovery code"}</span>
          <input dir="ltr" inputMode="numeric" autoComplete="one-time-code" value={code} disabled={pending} onChange={event => setCode(event.target.value)}/>
        </label>
        <label>
          <span>{fa ? "رمز عبور جدید" : "New password"}</span>
          <input type="password" value={newPassword} disabled={pending} onChange={event => setNewPassword(event.target.value)}/>
          <small>{fa ? "حداقل ۸ نویسه" : "At least 8 characters"}</small>
        </label>
        <label>
          <span>{fa ? "تکرار رمز عبور جدید" : "Confirm new password"}</span>
          <input type="password" value={confirmPassword} disabled={pending} onChange={event => setConfirmPassword(event.target.value)}/>
        </label>
        <AsyncButton pending={pending} disabled={!code.trim() || newPassword.length < 8} onClick={submitConfirm}>
          {fa ? "تغییر رمز عبور" : "Change password"}
        </AsyncButton>
      </div> : null}

      {step === "done" ? <div className="access-form">
        <div className="operational-banner success" role="status">
          {fa ? "رمز عبور تغییر کرد. اکنون می‌توانید وارد شوید." : "Your password has been changed. You can sign in now."}
        </div>
        <button className="primary-pill" onClick={onClose}>{fa ? "بازگشت به ورود" : "Back to sign in"}</button>
      </div> : null}
    </section>
  </div>;
}
