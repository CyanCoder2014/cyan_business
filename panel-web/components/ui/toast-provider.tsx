"use client";

import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";

export type ToastTone = "success" | "error" | "warning" | "info";
type ToastItem = { id: string; title: string; message?: string; tone: ToastTone };
type ToastInput = Omit<ToastItem, "id"> & { duration?: number };

const ToastContext = createContext<{ showToast: (toast: ToastInput) => void } | null>(null);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([]);
  const dismiss = useCallback((id: string) => setItems((current) => current.filter((item) => item.id !== id)), []);
  const showToast = useCallback((toast: ToastInput) => {
    const id = crypto.randomUUID();
    setItems((current) => [...current.slice(-3), { id, title: toast.title, message: toast.message, tone: toast.tone }]);
    window.setTimeout(() => dismiss(id), toast.duration ?? (toast.tone === "error" ? 8000 : 4500));
  }, [dismiss]);
  const value = useMemo(() => ({ showToast }), [showToast]);

  return <ToastContext.Provider value={value}>
    {children}
    <div className="toast-viewport" aria-live="polite" aria-relevant="additions">
      {items.map((item) => <article className={`app-toast ${item.tone}`} key={item.id} role={item.tone === "error" ? "alert" : "status"}>
        <span className="app-toast-icon" aria-hidden>{item.tone === "success" ? "✓" : item.tone === "error" ? "!" : item.tone === "warning" ? "△" : "i"}</span>
        <div><strong>{item.title}</strong>{item.message ? <p>{item.message}</p> : null}</div>
        <button type="button" onClick={() => dismiss(item.id)} aria-label="Dismiss notification">×</button>
      </article>)}
    </div>
  </ToastContext.Provider>;
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) throw new Error("useToast must be used inside ToastProvider");
  return context;
}
