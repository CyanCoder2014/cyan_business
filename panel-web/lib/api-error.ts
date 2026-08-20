export type PlatformErrorKind = "AUTH_REQUIRED" | "PERMISSION_DENIED" | "PLAN_LOCKED" | "CAPABILITY_DISABLED" | "SERVICE_UNAVAILABLE" | "VALIDATION" | "CONFLICT" | "UNKNOWN";

export type PlatformFieldError = {
  field: string;
  message: string;
  rejectedValue?: unknown;
};

export class PlatformApiError extends Error {
  constructor(
    public readonly kind: PlatformErrorKind,
    message: string,
    public readonly status: number,
    public readonly details?: unknown,
    public readonly correlationId?: string,
    public readonly retryable = false,
    public readonly fieldErrors: PlatformFieldError[] = [],
    public readonly errorCode?: string
  ) {
    super(message);
    this.name = "PlatformApiError";
  }
}

export async function platformErrorFromResponse(response: Response) {
  const details = await response.json().catch(() => null) as {
    code?: string;
    errorCode?: string;
    message?: string;
    detail?: string;
    correlationId?: string;
    retryable?: boolean;
    fieldErrors?: Array<{ field?: string; path?: string; message?: string; rejectedValue?: unknown }>;
    details?: { reason?: string; fieldErrors?: Array<{ field?: string; path?: string; message?: string; rejectedValue?: unknown }> };
  } | null;
  const errorCode = details?.errorCode ?? details?.code;
  const kind: PlatformErrorKind = errorCode === "PLAN_LOCKED" ? "PLAN_LOCKED" : errorCode === "CAPABILITY_DISABLED" ? "CAPABILITY_DISABLED" : response.status === 401 ? "AUTH_REQUIRED" : response.status === 403 ? "PERMISSION_DENIED" : response.status === 409 ? "CONFLICT" : response.status === 422 || response.status === 400 ? "VALIDATION" : response.status >= 500 ? "SERVICE_UNAVAILABLE" : "UNKNOWN";
  const safeFallback = kind === "SERVICE_UNAVAILABLE" ? "The service is temporarily unavailable." : `Request failed (${response.status})`;
  const isSafe = (value: unknown): value is string => typeof value === "string" && value.trim().length > 0 && !/[<>]|exception|stack trace/i.test(value);
  const rawFieldErrors = details?.fieldErrors ?? details?.details?.fieldErrors ?? [];
  // Generic exception paths put diagnostic detail in details.reason while leaving the top-level
  // message as boilerplate ("Request validation failed."). When there are no structured field
  // errors to explain the failure, prefer the reason so the real cause isn't silently dropped.
  const reason = details?.details?.reason;
  const base = [details?.message, details?.detail].find(isSafe) ?? safeFallback;
  const message = rawFieldErrors.length === 0 && isSafe(reason) && reason !== base ? reason : base;
  const fieldErrors = rawFieldErrors
    .filter((item) => item && (item.field || item.path || item.message))
    .map((item) => ({
      field: String(item.field ?? item.path ?? "request").replace(/^data\./, ""),
      message: String(item.message ?? "Invalid value"),
      rejectedValue: item.rejectedValue
    }));
  return new PlatformApiError(
    kind,
    message,
    response.status,
    details,
    details?.correlationId ?? response.headers.get("X-Correlation-Id") ?? undefined,
    details?.retryable ?? (response.status >= 500 || response.status === 429),
    fieldErrors,
    errorCode
  );
}

export function fieldErrorsByPath(error: unknown): Record<string, string> {
  if (!(error instanceof PlatformApiError)) return {};
  return Object.fromEntries(error.fieldErrors.map((item) => [item.field, item.message]));
}

/** Renders a caught error (ideally a PlatformApiError) into a toast-ready title/message, including per-field validation detail when present. */
export function describeApiError(error: unknown, fallbackTitle: string): { title: string; message: string } {
  if (error instanceof PlatformApiError) {
    const fieldSummary = error.fieldErrors.map((item) => `${item.field}: ${item.message}`).join(" · ");
    return { title: fallbackTitle, message: fieldSummary ? `${error.message} (${fieldSummary})` : error.message };
  }
  return { title: fallbackTitle, message: error instanceof Error ? error.message : String(error) };
}
