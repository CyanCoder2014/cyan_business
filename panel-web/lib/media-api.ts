import { getPlatformAuthToken, platformFetch } from "@/lib/platform-auth";

export type PreparedUpload = { uploadId: string; assetKey: string; uploadUrl: string; method: "PUT"; status: string; expectedSizeBytes: number; uploadedSizeBytes: number; expiresAt: string; deliveryUrl?: string | null };
export type MediaScope = { tenantKey: string; siteKey?: string };
export type MediaAsset={assetKey:string;originalFileName:string;mimeType:string;assetType:string;visibility:string;sizeBytes:number;status:string;deliveryUrl?:string;createdAt:string;completedAt?:string;metadata:Record<string,unknown>};export type MediaPage={items:MediaAsset[];page:number;size:number;total:number};export type MediaUsage={assetKey:string;status:string;referenceCount:number;references:Array<Record<string,unknown>>;reason?:string};

async function responseJson<T>(response: Response): Promise<T> {
  if (!response.ok) throw new Error(await response.text().catch(() => `Media request failed (${response.status})`));
  return response.json() as Promise<T>;
}

export async function prepareMediaUpload(file: File, scope: MediaScope): Promise<PreparedUpload> {
  return responseJson(await platformFetch("/api/platform/dynamic/media-service/endpoint/media/uploads/prepare", {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-Tenant-Key": scope.tenantKey, ...(scope.siteKey ? { "X-Site-Key": scope.siteKey } : {}) },
    body: JSON.stringify({ originalFileName: file.name, mimeType: file.type || "application/octet-stream", sizeBytes: file.size, visibility: "PRIVATE" }),
  }));
}

export function uploadMediaBytes(file: File, prepared: PreparedUpload, scope: MediaScope, onProgress: (percent: number) => void, signal?: AbortSignal): Promise<PreparedUpload> {
  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open("PUT", `/api/platform/dynamic/media-service${prepared.uploadUrl}`);
    const token = getPlatformAuthToken();
    if (token) request.setRequestHeader("Authorization", `Bearer ${token}`);
    request.setRequestHeader("Content-Type", file.type || "application/octet-stream");
    request.setRequestHeader("X-Tenant-Key", scope.tenantKey);
    if (scope.siteKey) request.setRequestHeader("X-Site-Key", scope.siteKey);
    request.upload.onprogress = (event) => { if (event.lengthComputable) onProgress(Math.round((event.loaded / event.total) * 100)); };
    request.onerror = () => reject(new Error("Media byte upload failed"));
    request.onabort = () => reject(new DOMException("Upload cancelled", "AbortError"));
    request.onload = () => request.status >= 200 && request.status < 300 ? resolve(JSON.parse(request.responseText) as PreparedUpload) : reject(new Error(request.responseText || `Media upload failed (${request.status})`));
    signal?.addEventListener("abort", () => request.abort(), { once: true });
    request.send(file);
  });
}

export async function cancelMediaUpload(prepared: PreparedUpload, scope: MediaScope): Promise<void> {
  const response = await platformFetch(`/api/platform/dynamic/media-service${prepared.uploadUrl}`, { method: "DELETE", headers: { "X-Tenant-Key": scope.tenantKey, ...(scope.siteKey ? { "X-Site-Key": scope.siteKey } : {}) } });
  if (!response.ok && response.status !== 204) throw new Error(await response.text());
}
const assetBase="/api/platform/service/media-service/endpoint/media/assets";const scopeHeaders=(s:MediaScope)=>({"X-Tenant-Key":s.tenantKey,...(s.siteKey?{"X-Site-Key":s.siteKey}:{})});async function assetJson<T>(path:string,s:MediaScope,init:RequestInit={}){const r=await platformFetch(`${assetBase}${path}`,{...init,headers:{"Content-Type":"application/json",...scopeHeaders(s),...(init.headers??{})},cache:"no-store"});if(!r.ok){const b=await r.json().catch(()=>null);throw new Error(b?.message??b?.detail??`Media request failed (${r.status})`)}return r.json()as Promise<T>}export const listMediaAssets=(s:MediaScope,q="",type="")=>assetJson<MediaPage>(`?q=${encodeURIComponent(q)}&type=${encodeURIComponent(type)}`,s);export const getMediaAsset=(s:MediaScope,k:string)=>assetJson<MediaAsset>(`/${encodeURIComponent(k)}`,s);export const getMediaUsage=(s:MediaScope,k:string)=>assetJson<MediaUsage>(`/${encodeURIComponent(k)}/usage`,s);export const mediaPreviewUrl=(a:MediaAsset)=>a.visibility==="PUBLIC"?`/api/platform/service/media-service/public/media/content/${encodeURIComponent(a.assetKey)}`:null;
