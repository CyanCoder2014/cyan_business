export type PlatformErrorKind = "AUTH_REQUIRED" | "PERMISSION_DENIED" | "PLAN_LOCKED" | "CAPABILITY_DISABLED" | "SERVICE_UNAVAILABLE" | "VALIDATION" | "CONFLICT" | "UNKNOWN";

export class PlatformApiError extends Error {
  constructor(public readonly kind: PlatformErrorKind, message: string, public readonly status: number, public readonly details?: unknown, public readonly correlationId?:string, public readonly retryable=false) { super(message); this.name = "PlatformApiError"; }
}

export async function platformErrorFromResponse(response: Response) {
  const details = await response.json().catch(() => null) as { code?: string; message?: string; detail?:string; correlationId?:string } | null;
  const kind: PlatformErrorKind = response.status === 401 ? "AUTH_REQUIRED" : response.status === 403 ? "PERMISSION_DENIED" : response.status === 409 ? "CONFLICT" : response.status === 422 || response.status === 400 ? "VALIDATION" : response.status >= 500 ? "SERVICE_UNAVAILABLE" : details?.code === "PLAN_LOCKED" ? "PLAN_LOCKED" : details?.code === "CAPABILITY_DISABLED" ? "CAPABILITY_DISABLED" : "UNKNOWN";
  const safeFallback = kind === "SERVICE_UNAVAILABLE" ? "The service is temporarily unavailable." : `Request failed (${response.status})`;
  const message = typeof details?.message === "string" && !/[<>]|exception|stack trace/i.test(details.message) ? details.message : typeof details?.detail === "string" && !/[<>]|exception|stack trace/i.test(details.detail) ? details.detail : safeFallback;
  return new PlatformApiError(kind, message, response.status, details, details?.correlationId ?? response.headers.get("X-Correlation-Id") ?? undefined, response.status >= 500 || response.status === 429);
}
