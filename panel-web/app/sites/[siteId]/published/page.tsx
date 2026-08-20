"use client";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { listRecords } from "@/lib/dynamic-api";
import { publicSitePath } from "@/lib/forms-api";
import { LayoutIcon } from "@/components/nav-icons";
import type { DynamicEntityRecord } from "@/lib/types";

export default function PublishedSitePage() {
  const { siteId } = useParams<{siteId:string}>(); const decodedSite = decodeURIComponent(siteId); const { locale } = usePanel(); const { tenantKey } = useScopeAccess();
  const scope = useMemo(() => ({ tenantKey: tenantKey || undefined, siteKey: decodedSite }), [tenantKey, decodedSite]); const [routes, setRoutes] = useState<DynamicEntityRecord[]>([]); const [loading, setLoading] = useState(true); const [error, setError] = useState<string | null>(null);
  const load = useCallback(async () => { if (!tenantKey) { setLoading(false); return; } setLoading(true); setError(null); try { setRoutes((await listRecords("storefront-service", "site-route", scope)).filter(record => String(record.data.publicationStatus).toUpperCase() === "PUBLISHED")); } catch (cause) { setError(cause instanceof Error ? cause.message : "Published pages unavailable"); } finally { setLoading(false); } }, [tenantKey, scope]);
  useEffect(() => { void load(); }, [load]);
  return <PanelShell activeKey="sites" title={`${decodedSite} website`} titleFa={`وب‌سایت ${decodedSite}`} subtitle="Open Cyan-hosted pages before connecting a custom domain." subtitleFa="پیش از اتصال دامنه اختصاصی، صفحه‌های میزبانی‌شده سیان را باز کنید."><div className="page-action-bar"><Link className="secondary-pill" href="/sites">{locale === "fa" ? "سایت‌ها" : "Sites"}</Link><Link className="primary-pill" href={`/sites/${encodeURIComponent(decodedSite)}/builder`}>{locale === "fa" ? "باز کردن سازنده" : "Open builder"}</Link></div>{loading ? <Skeleton height={340}/> : error ? <ErrorState title="Website unavailable" description={error} retry={load}/> : routes.length && tenantKey ? <div className="result-card-grid">{routes.map(route => { const path = String(route.data.path || "/"); const title = String((route.data.seo as Record<string,unknown> | undefined)?.title || route.data.routeKey || route.recordKey); const href = publicSitePath(tenantKey, decodedSite, path); return <article className="result-card" key={route.recordKey}><header><span className="result-card-icon"><LayoutIcon size={18}/></span><StatusBadge tone="success">PUBLISHED</StatusBadge></header><div><h2>{title}</h2><p>{path}</p><code dir="ltr">{href}</code></div><footer><Link className="primary-pill" target="_blank" href={href}>{locale === "fa" ? "مشاهده صفحه" : "View page"}</Link><button className="secondary-pill" onClick={() => navigator.clipboard.writeText(`${location.origin}${href}`)}>{locale === "fa" ? "کپی پیوند" : "Copy link"}</button></footer></article>; })}</div> : <EmptyState title={locale === "fa" ? "صفحه منتشرشده‌ای نیست" : "No published pages"} description={locale === "fa" ? "یک صفحه را در سازنده سایت منتشر کنید تا پیوند عمومی آن اینجا نمایش داده شود." : "Publish a page in Site Builder and its public Cyan link will appear here."} action={<Link className="primary-pill" href={`/sites/${encodeURIComponent(decodedSite)}/builder`}>{locale === "fa" ? "ساخت صفحه" : "Build a page"}</Link>}/>}</PanelShell>;
}
