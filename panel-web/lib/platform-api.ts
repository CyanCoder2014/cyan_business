import type {
  AppBlueprint,
  BotChannelIntegration,
  BotOutboundMessage,
  ClientAppDraft,
  GeneratePlatformAppRequest,
  GeneratePlatformAppResponse,
  ProvisioningRun
} from "@/lib/types";

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

export function listBlueprints(appType?: string): Promise<AppBlueprint[]> {
  const suffix = appType ? `?appType=${encodeURIComponent(appType)}` : "";
  return requestJson<AppBlueprint[]>(`/endpoint/ai-orchestrator/blueprints${suffix}`, {
    method: "GET"
  });
}

export function listClientDrafts(params?: {
  tenantKey?: string;
  siteKey?: string;
  clientKey?: string;
}): Promise<ClientAppDraft[]> {
  const search = new URLSearchParams();
  if (params?.tenantKey) search.set("tenantKey", params.tenantKey);
  if (params?.siteKey) search.set("siteKey", params.siteKey);
  if (params?.clientKey) search.set("clientKey", params.clientKey);
  const suffix = search.size ? `?${search.toString()}` : "";
  return requestJson<ClientAppDraft[]>(`/endpoint/ai-orchestrator/drafts${suffix}`, {
    method: "GET"
  });
}

export function createClientDraft(request: {
  appType?: string;
  blueprintKey?: string;
  tenantKey?: string;
  siteKey?: string;
  clientKey?: string;
  title?: string;
  prompt?: string;
  answers?: Record<string, unknown>;
}): Promise<ClientAppDraft> {
  return requestJson<ClientAppDraft>("/endpoint/ai-orchestrator/drafts", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function provisionClientDraft(draftId: string): Promise<ProvisioningRun> {
  return requestJson<ProvisioningRun>(`/endpoint/ai-orchestrator/drafts/${draftId}/provision`, {
    method: "POST",
    body: JSON.stringify({})
  });
}

export function listProvisioningRuns(draftId: string): Promise<ProvisioningRun[]> {
  return requestJson<ProvisioningRun[]>(`/endpoint/ai-orchestrator/drafts/${draftId}/runs`, {
    method: "GET"
  });
}

export function listBotIntegrations(params?: {
  tenantKey?: string;
  siteKey?: string;
}): Promise<BotChannelIntegration[]> {
  const search = new URLSearchParams();
  if (params?.tenantKey) search.set("tenantKey", params.tenantKey);
  if (params?.siteKey) search.set("siteKey", params.siteKey);
  const suffix = search.size ? `?${search.toString()}` : "";
  return requestJson<BotChannelIntegration[]>(`/endpoint/bot-adapter/integrations${suffix}`, {
    method: "GET"
  });
}

export function upsertBotIntegration(request: {
  channel: "TELEGRAM" | "BALE";
  integrationKey: string;
  tenantKey: string;
  siteKey: string;
  clientKey?: string;
  appTypeHint?: string;
  botId?: string;
  botUsername?: string;
  botToken?: string;
  tokenSecretRef?: string;
  webhookSecret?: string;
  miniAppUrl?: string;
  miniAppEnabled?: boolean;
  miniAppStartParam?: string;
  active?: boolean;
}): Promise<BotChannelIntegration> {
  return requestJson<BotChannelIntegration>("/endpoint/bot-adapter/integrations", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function registerBotWebhook(channel: "TELEGRAM" | "BALE", integrationKey: string): Promise<{
  status: string;
  channel: string;
  integrationKey: string;
  webhookUrl: string;
}> {
  return requestJson(`/endpoint/bot-adapter/integrations/${channel}/${integrationKey}/register-webhook`, {
    method: "POST",
    body: JSON.stringify({})
  });
}

export function sendBotMessage(request: {
  channel: "TELEGRAM" | "BALE";
  integrationKey: string;
  externalChatId: string;
  text: string;
}): Promise<{
  status: string;
  provider: string;
  externalChatId: string;
  messageText: string;
  deliveryId: string;
  attemptCount: number;
}> {
  return requestJson("/endpoint/bot-adapter/messages", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function listBotMessages(params?: {
  tenantKey?: string;
  siteKey?: string;
  integrationKey?: string;
}): Promise<BotOutboundMessage[]> {
  const search = new URLSearchParams();
  if (params?.tenantKey) search.set("tenantKey", params.tenantKey);
  if (params?.siteKey) search.set("siteKey", params.siteKey);
  if (params?.integrationKey) search.set("integrationKey", params.integrationKey);
  const suffix = search.size ? `?${search.toString()}` : "";
  return requestJson<BotOutboundMessage[]>(`/endpoint/bot-adapter/messages${suffix}`, {
    method: "GET"
  });
}

export function retryBotMessage(messageId: string): Promise<{
  status: string;
  deliveryId: string;
  attemptCount: number;
}> {
  return requestJson(`/endpoint/bot-adapter/messages/${encodeURIComponent(messageId)}/retry`, {
    method: "POST",
    body: JSON.stringify({})
  });
}
