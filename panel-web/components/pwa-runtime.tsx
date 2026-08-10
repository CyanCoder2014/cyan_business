"use client";

import { useEffect, useState } from "react";
import { usePanel } from "@/components/panel-provider";

export function PwaRuntime() {
  const { locale } = usePanel();
  const [offline, setOffline] = useState(false);
  const [waiting, setWaiting] = useState<ServiceWorker | null>(null);
  const [installEvent, setInstallEvent] = useState<Event | null>(null);
  useEffect(() => {
    const update = () => setOffline(!navigator.onLine);
    update();
    window.addEventListener("online", update);
    window.addEventListener("offline", update);
    const beforeInstall = (event: Event) => { event.preventDefault(); setInstallEvent(event); };
    window.addEventListener("beforeinstallprompt", beforeInstall);
    const reload = () => window.location.reload();
    if ("serviceWorker" in navigator && process.env.NODE_ENV === "production") {
      navigator.serviceWorker.register("/sw.js").then((registration) => {
        if (registration.waiting) setWaiting(registration.waiting);
        registration.addEventListener("updatefound", () => {
          const worker = registration.installing;
          worker?.addEventListener("statechange", () => { if (worker.state === "installed" && navigator.serviceWorker.controller) setWaiting(worker); });
        });
      }).catch(() => undefined);
      navigator.serviceWorker.addEventListener("controllerchange", reload);
    }
    return () => { window.removeEventListener("online", update); window.removeEventListener("offline", update); window.removeEventListener("beforeinstallprompt", beforeInstall); navigator.serviceWorker?.removeEventListener("controllerchange", reload); };
  }, []);
  const install = async () => { const prompt = installEvent as Event & { prompt?:()=>Promise<void> }; await prompt.prompt?.(); setInstallEvent(null); };
  return <div className="pwa-notices" aria-live="polite">
    {offline ? <div className="offline-banner" role="status">{locale === "fa" ? "آفلاین — پوسته ذخیره‌شده در دسترس است؛ عملیات زنده و تغییرات متوقف‌اند." : "Offline — cached shell assets remain available; live operations and mutations are paused."}</div> : null}
    {waiting ? <div className="pwa-update-banner" role="status"><span>{locale === "fa" ? "نسخه جدید سیان آماده است." : "A Cyan update is ready."}</span><button onClick={() => waiting.postMessage({ type:"SKIP_WAITING" })}>{locale === "fa" ? "به‌روزرسانی" : "Update now"}</button></div> : null}
    {installEvent ? <button className="pwa-install-button" onClick={install}>{locale === "fa" ? "نصب سیان" : "Install Cyan"}</button> : null}
  </div>;
}
