export const DEFAULT_AVAILABLE_SERVICE_KEYS = [
  "ai-orchestrator-service",
  "notification-service",
  "bpm-service",
  "automation-orchestrator-service",
  "report-service",
  "sso-auth-service",
  "sso-user-service",
  "sso-captcha-service",
  "media-service",
  "processor-service"
] as const;

export function availablePlatformServiceKeys(): string[] {
  const configured = process.env.NEXT_PUBLIC_AVAILABLE_SERVICE_KEYS
    ?.split(",")
    .map((value) => value.trim())
    .filter(Boolean);
  return configured?.length ? configured : [...DEFAULT_AVAILABLE_SERVICE_KEYS];
}

export function withServiceInventory<T extends object>(body: T): T & {
  availableServiceKeys: string[];
} {
  return {
    ...body,
    availableServiceKeys: Array.isArray((body as Record<string, unknown>).availableServiceKeys)
      ? (body as Record<string, unknown>).availableServiceKeys as string[]
      : availablePlatformServiceKeys()
  };
}
