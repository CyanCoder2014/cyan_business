export type PlatformErrorKind = "AUTH_REQUIRED" | "PERMISSION_DENIED" | "PLAN_LOCKED" | "CAPABILITY_DISABLED" | "SERVICE_UNAVAILABLE" | "VALIDATION" | "UNKNOWN";

export class PlatformApiError extends Error {
  constructor(public readonly kind: PlatformErrorKind, message: string, public readonly status: number, public readonly details?: unknown) { super(message); this.name = "PlatformApiError"; }
}

export async function platformErrorFromResponse(response: Response) {
  const details = await response.json().catch(() => null) as { code?: string; message?: string } | null;
  const kind: PlatformErrorKind = response.status === 401 ? "AUTH_REQUIRED" : response.status === 403 ? "PERMISSION_DENIED" : response.status === 422 || response.status === 400 ? "VALIDATION" : response.status >= 500 ? "SERVICE_UNAVAILABLE" : details?.code === "PLAN_LOCKED" ? "PLAN_LOCKED" : details?.code === "CAPABILITY_DISABLED" ? "CAPABILITY_DISABLED" : "UNKNOWN";
  return new PlatformApiError(kind, details?.message ?? `Request failed (${response.status})`, response.status, details);
}
