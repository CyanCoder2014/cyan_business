import type { GeneratePlatformAppRequest, GeneratePlatformAppResponse } from "@/lib/types";

const baseUrl = process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8001";

async function requestJson<T>(path: string, init: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
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

export async function generatePlatformApp(request: GeneratePlatformAppRequest): Promise<GeneratePlatformAppResponse> {
  return requestJson<GeneratePlatformAppResponse>("/endpoint/ai-orchestrator/generate/app", {
    method: "POST",
    body: JSON.stringify(request)
  });
}
