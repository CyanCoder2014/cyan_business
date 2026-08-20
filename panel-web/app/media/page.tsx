"use client";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { AsyncButton, EmptyState, ErrorState, Field, Select, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { useToast } from "@/components/ui/toast-provider";
import { describeApiError } from "@/lib/api-error";
import { platformFetch } from "@/lib/platform-auth";
import {
  deleteMediaAsset,
  getMediaUsage,
  listMediaAssets,
  mediaContentPath,
  prepareMediaUpload,
  updateMediaAsset,
  uploadMediaBytes,
  type MediaAsset,
  type MediaScope,
  type MediaUsage,
} from "@/lib/media-api";

type EditForm = { title: string; altText: string; caption: string; license: string; tags: string; folderKey: string; visibility: string };

function editFormFrom(asset: MediaAsset): EditForm {
  const seo = (asset.metadata.seo as Record<string, unknown> | undefined) ?? {};
  const tags = Array.isArray(asset.metadata.tags) ? (asset.metadata.tags as unknown[]).map(String) : [];
  return {
    title: String(seo.title ?? asset.originalFileName),
    altText: String(seo.altText ?? ""),
    caption: String(seo.caption ?? ""),
    license: String(seo.license ?? ""),
    tags: tags.join(", "),
    folderKey: String(asset.metadata.folderKey ?? ""),
    visibility: asset.visibility,
  };
}

export default function Media() {
  const { locale } = usePanel();
  const { tenantKey, siteKey, queryVersion } = useScopeAccess();
  const { showToast } = useToast();
  const scope = useMemo<MediaScope | null>(() => (tenantKey ? { tenantKey, siteKey: siteKey ?? undefined } : null), [tenantKey, siteKey]);
  const [items, setItems] = useState<MediaAsset[]>([]);
  const [query, setQuery] = useState("");
  const [type, setType] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState<string | null>(null);
  const [progress, setProgress] = useState<number | null>(null);
  const [selected, setSelected] = useState<MediaAsset | null>(null);
  const [usage, setUsage] = useState<MediaUsage | null>(null);
  const [editForm, setEditForm] = useState<EditForm | null>(null);
  const [editPending, setEditPending] = useState(false);
  const input = useRef<HTMLInputElement>(null);

  const load = useCallback(async () => {
    if (!scope) { setLoading(false); return; }
    setLoading(true);
    setError(null);
    try {
      const page = await listMediaAssets(scope, query, type);
      const values = Array.isArray(page.items) ? page.items : [];
      setItems(values);
      setSelected((current) => values.find((value) => value.assetKey === current?.assetKey) ?? values[0] ?? null);
    } catch (reason) {
      const { title, message } = describeApiError(reason, locale === "fa" ? "رسانه در دسترس نیست" : "Media unavailable");
      setError(message);
      showToast({ tone: "error", title, message });
    } finally {
      setLoading(false);
    }
  }, [scope, query, type, queryVersion, locale, showToast]);
  useEffect(() => { const timer = setTimeout(() => void load(), 200); return () => clearTimeout(timer); }, [load]);
  useEffect(() => {
    if (!scope || !selected) { setUsage(null); return; }
    void getMediaUsage(scope, selected.assetKey).then((value) => setUsage({ ...value, references: Array.isArray(value.references) ? value.references : [] })).catch(() => setUsage(null));
  }, [scope, selected]);
  useEffect(() => { setEditForm(selected ? editFormFrom(selected) : null); }, [selected]);

  const upload = async (file: File) => {
    if (!scope || pending) return;
    setPending("upload");
    setProgress(0);
    setError(null);
    try {
      const prepared = await prepareMediaUpload(file, scope);
      await uploadMediaBytes(file, prepared, scope, setProgress);
      showToast({ tone: "success", title: locale === "fa" ? "بارگذاری شد" : "Upload complete" });
      await load();
    } catch (reason) {
      const { title, message } = describeApiError(reason, locale === "fa" ? "بارگذاری ناموفق بود" : "Upload failed");
      setError(message);
      showToast({ tone: "error", title, message });
    } finally {
      setPending(null);
      setProgress(null);
      if (input.current) input.current.value = "";
    }
  };
  const remove = async () => {
    if (!scope || !selected || pending || !usage || usage.referenceCount > 0) return;
    if (!window.confirm(locale === "fa" ? "این فایل بدون ارجاع برای همیشه حذف شود؟" : "Permanently delete this unreferenced asset?")) return;
    setPending("delete");
    setError(null);
    try {
      await deleteMediaAsset(scope, selected.assetKey);
      setSelected(null);
      setUsage(null);
      showToast({ tone: "success", title: locale === "fa" ? "فایل حذف شد" : "Asset deleted" });
      await load();
    } catch (reason) {
      const { title, message } = describeApiError(reason, locale === "fa" ? "حذف ناموفق بود" : "Delete failed");
      setError(message);
      showToast({ tone: "error", title, message });
    } finally {
      setPending(null);
    }
  };
  const saveEdit = async () => {
    if (!scope || !selected || !editForm || editPending) return;
    setEditPending(true);
    try {
      const updated = await updateMediaAsset(scope, selected.assetKey, {
        title: editForm.title,
        altText: editForm.altText,
        caption: editForm.caption,
        license: editForm.license,
        tags: editForm.tags.split(",").map((tag) => tag.trim()).filter(Boolean),
        folderKey: editForm.folderKey,
        visibility: editForm.visibility,
      });
      setItems((current) => current.map((item) => (item.assetKey === updated.assetKey ? updated : item)));
      setSelected(updated);
      showToast({ tone: "success", title: locale === "fa" ? "ذخیره شد" : "Asset updated" });
    } catch (reason) {
      const { title, message } = describeApiError(reason, locale === "fa" ? "ذخیره ناموفق بود" : "Update failed");
      showToast({ tone: "error", title, message });
    } finally {
      setEditPending(false);
    }
  };

  return (
    <PanelShell activeKey="media" kicker="Operate" kickerFa="عملیات" title="Media library" titleFa="کتابخانه رسانه" subtitle="Upload, inspect usage, and safely delete tenant-scoped assets." subtitleFa="بارگذاری، بررسی ارجاع و حذف امن فایل‌های فضای کاری.">
      <div className="operational-toolbar">
        <input aria-label="Search media" value={query} disabled={!!pending} onChange={(event) => setQuery(event.target.value)} placeholder={locale === "fa" ? "جستجوی فایل" : "Search assets"}/>
        <select aria-label="Asset type" value={type} disabled={!!pending} onChange={(event) => setType(event.target.value)}>
          <option value="">{locale === "fa" ? "همه نوع‌ها" : "All types"}</option>
          <option>IMAGE</option><option>DOCUMENT</option><option>VIDEO</option><option>AUDIO</option>
        </select>
        <AsyncButton pending={pending === "upload"} disabled={!!(pending && pending !== "upload")} pendingLabel={locale === "fa" ? `بارگذاری ${progress ?? 0}٪` : `Uploading ${progress ?? 0}%`} onClick={() => input.current?.click()}>{locale === "fa" ? "بارگذاری فایل" : "Upload asset"}</AsyncButton>
        <input ref={input} className="visually-hidden" type="file" disabled={!!pending} onChange={(event) => { const file = event.target.files?.[0]; if (file) void upload(file); }}/>
      </div>
      {progress !== null ? <div className="upload-progress" role="progressbar" aria-valuenow={progress}><span style={{ inlineSize: `${progress}%` }}/></div> : null}
      {loading ? (
        <div className="media-grid"><Skeleton height={220}/><Skeleton height={220}/><Skeleton height={220}/></div>
      ) : error && !items.length ? (
        <ErrorState title={locale === "fa" ? "رسانه در دسترس نیست" : "Media unavailable"} description={error} retry={load}/>
      ) : items.length ? (
        <div className="media-layout">
          <div className="media-grid">{items.map((asset) => <MediaCard key={asset.assetKey} asset={asset} selected={selected?.assetKey === asset.assetKey} pending={!!pending} onSelect={() => setSelected(asset)}/>)}</div>
          {selected && editForm ? (
            <aside className="panel-card media-detail">
              <div className="section-heading">
                <div><h2>{selected.originalFileName}</h2><code dir="ltr">{selected.assetKey}</code></div>
                <StatusBadge tone={selected.visibility === "PUBLIC" ? "success" : "neutral"}>{selected.visibility}</StatusBadge>
              </div>
              <dl>
                <dt>Type</dt><dd>{selected.mimeType}</dd>
                <dt>Size</dt><dd>{formatBytes(selected.sizeBytes)}</dd>
                <dt>Status</dt><dd>{selected.status}</dd>
                <dt>{locale === "fa" ? "تعداد ارجاع" : "References"}</dt><dd>{usage?.referenceCount ?? "…"}</dd>
              </dl>
              {(usage?.references ?? []).map((reference, index) => <code dir="ltr" key={index}>{String(reference.sourceService ?? reference.serviceKey ?? "")} · {String(reference.sourceKey ?? reference.referenceKey ?? "")}</code>)}
              <div className="phase9-form media-edit-form">
                <Field label={locale === "fa" ? "عنوان" : "Title"} value={editForm.title} disabled={editPending} onChange={(event) => setEditForm((v) => v && { ...v, title: event.target.value })}/>
                <Field label={locale === "fa" ? "متن جایگزین تصویر" : "Alt text"} value={editForm.altText} disabled={editPending} onChange={(event) => setEditForm((v) => v && { ...v, altText: event.target.value })}/>
                <Field label={locale === "fa" ? "توضیح" : "Caption"} value={editForm.caption} disabled={editPending} onChange={(event) => setEditForm((v) => v && { ...v, caption: event.target.value })}/>
                <Field label={locale === "fa" ? "مجوز استفاده" : "License"} value={editForm.license} disabled={editPending} onChange={(event) => setEditForm((v) => v && { ...v, license: event.target.value })}/>
                <Field label={locale === "fa" ? "برچسب‌ها (با کاما جدا کنید)" : "Tags (comma-separated)"} value={editForm.tags} disabled={editPending} onChange={(event) => setEditForm((v) => v && { ...v, tags: event.target.value })}/>
                <Field label={locale === "fa" ? "کلید پوشه" : "Folder key"} value={editForm.folderKey} disabled={editPending} onChange={(event) => setEditForm((v) => v && { ...v, folderKey: event.target.value })}/>
                <Select label={locale === "fa" ? "قابلیت مشاهده" : "Visibility"} value={editForm.visibility} disabled={editPending} onChange={(event) => setEditForm((v) => v && { ...v, visibility: event.target.value })}>
                  <option value="PRIVATE">PRIVATE</option>
                  <option value="PUBLIC">PUBLIC</option>
                </Select>
                <AsyncButton pending={editPending} onClick={saveEdit}>{locale === "fa" ? "ذخیره تغییرات" : "Save changes"}</AsyncButton>
              </div>
              <AsyncButton className="danger-pill" pending={pending === "delete"} disabled={!usage || usage.referenceCount > 0 || !!(pending && pending !== "delete")} onClick={remove}>{usage?.referenceCount ? (locale === "fa" ? "فایل در حال استفاده است" : "Asset is in use") : (locale === "fa" ? "حذف فایل" : "Delete asset")}</AsyncButton>
            </aside>
          ) : null}
        </div>
      ) : (
        <EmptyState title={locale === "fa" ? "فایلی نیست" : "No media assets"} description={locale === "fa" ? "یک فایل واقعی بارگذاری کنید؛ داده نمونه نمایش داده نمی‌شود." : "Upload a real file. No sample assets are shown."} action={<AsyncButton onClick={() => input.current?.click()}>{locale === "fa" ? "بارگذاری" : "Upload asset"}</AsyncButton>}/>
      )}
    </PanelShell>
  );
}

function MediaCard({ asset, selected, pending, onSelect }: { asset: MediaAsset; selected: boolean; pending: boolean; onSelect: () => void }) {
  const isImage = asset.mimeType.startsWith("image/");
  const altText = String((asset.metadata.seo as Record<string, unknown> | undefined)?.altText ?? asset.originalFileName);
  return (
    <button className="media-card" disabled={pending} onClick={onSelect} aria-pressed={selected}>
      <div className="media-preview">{isImage ? <MediaThumbnail asset={asset} alt={altText}/> : <span>{asset.assetType.slice(0, 3)}</span>}</div>
      <div><strong title={asset.originalFileName}>{asset.originalFileName}</strong><small>{formatBytes(asset.sizeBytes)} · {asset.mimeType}</small><StatusBadge tone={asset.status === "UPLOADED" ? "success" : "warning"}>{asset.status}</StatusBadge></div>
    </button>
  );
}

function MediaThumbnail({ asset, alt }: { asset: MediaAsset; alt: string }) {
  const path = mediaContentPath(asset);
  const [src, setSrc] = useState<string | null>(asset.visibility === "PUBLIC" ? path : null);
  useEffect(() => {
    if (asset.visibility === "PUBLIC" || !path) return;
    let objectUrl: string | null = null;
    let cancelled = false;
    void platformFetch(path).then((response) => (response.ok ? response.blob() : null)).then((blob) => {
      if (cancelled || !blob) return;
      objectUrl = URL.createObjectURL(blob);
      setSrc(objectUrl);
    }).catch(() => undefined);
    return () => { cancelled = true; if (objectUrl) URL.revokeObjectURL(objectUrl); };
  }, [asset.assetKey, asset.visibility, path]);
  if (!src) return <span>IMG</span>;
  return <img src={src} alt={alt}/>;
}

function formatBytes(value: number) {
  if (value < 1024) return `${value} B`;
  if (value < 1048576) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / 1048576).toFixed(1)} MB`;
}
