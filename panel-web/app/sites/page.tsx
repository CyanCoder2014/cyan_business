"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import {
  AsyncButton,
  Dialog,
  EmptyState,
  ErrorState,
  Field,
  Skeleton,
  StatusBadge,
} from "@/components/ui/primitives";
import { createSite, listSites, type SiteSummary } from "@/lib/site-admin-api";

export default function Sites() {
  const { locale } = usePanel();
  const { tenantKey } = useScopeAccess();
  const [items, setItems] = useState<SiteSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState(false);
  const [pending, setPending] = useState(false);
  const [name, setName] = useState("");
  const [key, setKey] = useState("");

  const load = useCallback(async () => {
    if (!tenantKey) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setItems(await listSites(tenantKey));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause));
    } finally {
      setLoading(false);
    }
  }, [tenantKey]);

  useEffect(() => {
    void load();
  }, [load]);

  const save = async () => {
    if (!tenantKey || pending) return;
    setPending(true);
    setError(null);
    try {
      await createSite(tenantKey, { name, siteKey: key || undefined });
      setOpen(false);
      setName("");
      setKey("");
      await load();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause));
    } finally {
      setPending(false);
    }
  };

  return (
    <PanelShell
      activeKey="sites"
      title="Sites"
      titleFa="سایت‌ها"
      subtitle="Build pages, open the published website, or enter its role-scoped cartable."
      subtitleFa="صفحه بسازید، سایت منتشرشده را ببینید یا وارد کارتابل نقش‌محور آن شوید."
    >
      <div className="page-action-bar">
        <StatusBadge tone="info">
          {items.length} {locale === "fa" ? "سایت" : "sites"}
        </StatusBadge>
        <button
          className="primary-pill"
          disabled={!tenantKey || pending}
          onClick={() => setOpen(true)}
        >
          {locale === "fa" ? "سایت جدید" : "New site"}
        </button>
      </div>

      {loading ? (
        <Skeleton height={340} />
      ) : error ? (
        <ErrorState title="Sites unavailable" description={error} retry={load} />
      ) : items.length ? (
        <div className="phase9-card-grid">
          {items.map((site) => (
            <article className="panel-card phase9-resource-card" key={site.siteKey}>
              <div className="phase9-provider-icon">▣</div>
              <div>
                <p className="page-kicker">{site.siteKey}</p>
                <h2>{site.name}</h2>
                <p>{new Date(site.createdAt).toLocaleDateString(locale)}</p>
                <div className="card-actions">
                  <Link className="secondary-pill" href={`/sites/${encodeURIComponent(site.siteKey)}/portal`}>
                    {locale === "fa" ? "کارتابل" : "Work portal"}
                  </Link>
                  <Link className="secondary-pill" href={`/sites/${encodeURIComponent(site.siteKey)}/published`}>
                    {locale === "fa" ? "مشاهده سایت" : "View website"}
                  </Link>
                  <Link className="primary-pill" href={`/sites/${encodeURIComponent(site.siteKey)}/builder`}>
                    {locale === "fa" ? "سازنده" : "Builder"}
                  </Link>
                </div>
              </div>
              <StatusBadge tone="success">{site.status}</StatusBadge>
            </article>
          ))}
        </div>
      ) : (
        <EmptyState
          title={locale === "fa" ? "سایتی نیست" : "No sites"}
          description={locale === "fa" ? "اولین سایت این فضای کار را ایجاد کنید." : "Create the first site in this workspace."}
        />
      )}

      <Dialog
        open={open}
        title={locale === "fa" ? "سایت جدید" : "New site"}
        onClose={() => !pending && setOpen(false)}
      >
        <div className="phase9-form">
          <Field
            label={locale === "fa" ? "نام سایت" : "Site name"}
            value={name}
            disabled={pending}
            onChange={(event) => {
              setName(event.target.value);
              if (!key) {
                setKey(
                  event.target.value
                    .toLowerCase()
                    .replace(/[^a-z0-9]+/g, "-")
                    .replace(/^-|-$/g, ""),
                );
              }
            }}
          />
          <Field
            label={locale === "fa" ? "کلید سایت" : "Site key"}
            value={key}
            disabled={pending}
            onChange={(event) => setKey(event.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ""))}
          />
          {error ? <p className="field-error">{error}</p> : null}
          <div className="dialog-actions">
            <button className="secondary-pill" disabled={pending} onClick={() => setOpen(false)}>
              {locale === "fa" ? "لغو" : "Cancel"}
            </button>
            <AsyncButton
              pending={pending}
              disabled={name.trim().length < 2 || key.length < 2}
              onClick={save}
            >
              {locale === "fa" ? "ایجاد" : "Create"}
            </AsyncButton>
          </div>
        </div>
      </Dialog>
    </PanelShell>
  );
}
