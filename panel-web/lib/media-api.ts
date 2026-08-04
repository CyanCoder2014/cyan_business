import { platformFetch } from "@/lib/platform-auth";

export type MediaPrepareUploadRequest = {
  assetKey: string;
  assetType?: string;
  originalFileName: string;
  mimeType?: string;
  visibility?: string;
  altText?: string;
  caption?: string;
  title?: string;
  license?: string;
  bucket?: string;
  path?: string;
  width?: number;
  height?: number;
  sizeBytes?: number;
};

export type MediaAssetResponse = {
  assetKey: string;
  deliveryUrl: string;
  status: string;
  data: Record<string, unknown>;
};

async function requestJson<T>(path: string, init: RequestInit): Promise<T> {
  const response = await platformFetch(path, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init.headers ?? {})
    },
    cache: "no-store"
  });

  if (!response.ok) {
    const body = await response.text().catch(() => "");
    throw new Error(body || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function prepareMediaUpload(request: MediaPrepareUploadRequest): Promise<MediaAssetResponse> {
  return requestJson<MediaAssetResponse>("/api/platform/dynamic/media-service/internal/media/assets/prepare-upload", {
    method: "POST",
    body: JSON.stringify(request)
  });
}
