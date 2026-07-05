import type {
  AiConversationSession,
  AppBlueprint,
  BotChannelIntegration,
  BotMiniAppBuild,
  BotOutboundMessage,
  ClientAppDraft,
  GeneratePlatformAppRequest,
  GeneratePlatformAppResponse,
  ProvisioningRun
} from "@/lib/types";
import { getPlatformAuthToken, platformFetch } from "@/lib/platform-auth";

type PlatformServiceKey = "ai-orchestrator-service" | "bot-adapter-service";

async function requestJson<T>(serviceKey: PlatformServiceKey, path: string, init: RequestInit): Promise<T> {
  const response = await platformFetch(`/api/platform/service/${serviceKey}${path}`, {
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
  return requestJson<GeneratePlatformAppResponse>("ai-orchestrator-service", "/endpoint/ai-orchestrator/generate/app", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function hasAiStudioWebSocket() {
  return Boolean(process.env.NEXT_PUBLIC_AI_STUDIO_WS_URL?.trim());
}

export function generatePlatformAppOverWebSocket(request: GeneratePlatformAppRequest): Promise<GeneratePlatformAppResponse> {
  const configuredUrl = process.env.NEXT_PUBLIC_AI_STUDIO_WS_URL?.trim();
  if (!configuredUrl || typeof WebSocket === "undefined") {
    return Promise.reject(new Error("AI Studio WebSocket URL is not configured."));
  }

  return new Promise((resolve, reject) => {
    const url = new URL(configuredUrl);
    const token = getPlatformAuthToken();
    if (token) {
      url.searchParams.set("access_token", token);
    }

    const socket = new WebSocket(url.toString());
    const timeout = window.setTimeout(() => {
      socket.close();
      reject(new Error("AI Studio WebSocket timed out."));
    }, 30000);

    socket.onopen = () => {
      socket.send(JSON.stringify({ type: "generatePlatformApp", payload: request }));
    };

    socket.onerror = () => {
      window.clearTimeout(timeout);
      reject(new Error("AI Studio WebSocket connection failed."));
    };

    socket.onmessage = (event) => {
      try {
        const message = JSON.parse(String(event.data));
        if (message.type === "error") {
          throw new Error(message.message || "AI Studio WebSocket returned an error.");
        }
        const payload = message.payload ?? message;
        if (payload?.dsl) {
          window.clearTimeout(timeout);
          socket.close();
          resolve(payload as GeneratePlatformAppResponse);
        }
      } catch (error) {
        window.clearTimeout(timeout);
        socket.close();
        reject(error instanceof Error ? error : new Error("AI Studio WebSocket returned an invalid response."));
      }
    };

    socket.onclose = () => {
      window.clearTimeout(timeout);
    };
  });
}

export function listBlueprints(appType?: string): Promise<AppBlueprint[]> {
  const suffix = appType ? `?appType=${encodeURIComponent(appType)}` : "";
  return requestJson<AppBlueprint[]>("ai-orchestrator-service", `/endpoint/ai-orchestrator/blueprints${suffix}`, {
    method: "GET"
  });
}

export function getClientDraft(draftId: string): Promise<ClientAppDraft> {
  return requestJson<ClientAppDraft>("ai-orchestrator-service", `/endpoint/ai-orchestrator/drafts/${encodeURIComponent(draftId)}`, {
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
  return requestJson<ClientAppDraft[]>("ai-orchestrator-service", `/endpoint/ai-orchestrator/drafts${suffix}`, {
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
  return requestJson<ClientAppDraft>("ai-orchestrator-service", "/endpoint/ai-orchestrator/drafts", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function provisionClientDraft(draftId: string): Promise<ProvisioningRun> {
  return requestJson<ProvisioningRun>("ai-orchestrator-service", `/endpoint/ai-orchestrator/drafts/${draftId}/provision`, {
    method: "POST",
    body: JSON.stringify({})
  });
}

export function listProvisioningRuns(draftId: string): Promise<ProvisioningRun[]> {
  return requestJson<ProvisioningRun[]>("ai-orchestrator-service", `/endpoint/ai-orchestrator/drafts/${draftId}/runs`, {
    method: "GET"
  });
}

export function listConversationSessions(params?: {
  tenantKey?: string;
  siteKey?: string;
  clientKey?: string;
  draftId?: string;
}): Promise<AiConversationSession[]> {
  const search = new URLSearchParams();
  if (params?.tenantKey) search.set("tenantKey", params.tenantKey);
  if (params?.siteKey) search.set("siteKey", params.siteKey);
  if (params?.clientKey) search.set("clientKey", params.clientKey);
  if (params?.draftId) search.set("draftId", params.draftId);
  const suffix = search.size ? `?${search.toString()}` : "";
  return requestJson<AiConversationSession[]>("ai-orchestrator-service", `/endpoint/ai-orchestrator/sessions${suffix}`, {
    method: "GET"
  });
}

export function getConversationSession(sessionId: string): Promise<AiConversationSession> {
  return requestJson<AiConversationSession>("ai-orchestrator-service", `/endpoint/ai-orchestrator/sessions/${encodeURIComponent(sessionId)}`, {
    method: "GET"
  });
}

export function createConversationSession(request: {
  channelType?: string;
  tenantKey?: string;
  siteKey?: string;
  clientKey?: string;
  draftId?: string;
  appTypeHint?: string;
  title?: string;
  extractedAnswers?: Record<string, unknown>;
}): Promise<AiConversationSession> {
  return requestJson<AiConversationSession>("ai-orchestrator-service", "/endpoint/ai-orchestrator/sessions", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function appendConversationMessage(
  sessionId: string,
  request: { role: string; content: string; answersPatch?: Record<string, unknown> }
): Promise<AiConversationSession> {
  return requestJson<AiConversationSession>("ai-orchestrator-service", `/endpoint/ai-orchestrator/sessions/${encodeURIComponent(sessionId)}/message`, {
    method: "POST",
    body: JSON.stringify(request)
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
  return requestJson<BotChannelIntegration[]>("bot-adapter-service", `/endpoint/bot-adapter/integrations${suffix}`, {
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
  return requestJson<BotChannelIntegration>("bot-adapter-service", "/endpoint/bot-adapter/integrations", {
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
  return requestJson("bot-adapter-service", `/endpoint/bot-adapter/integrations/${channel}/${integrationKey}/register-webhook`, {
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
  return requestJson("bot-adapter-service", "/endpoint/bot-adapter/messages", {
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
  return requestJson<BotOutboundMessage[]>("bot-adapter-service", `/endpoint/bot-adapter/messages${suffix}`, {
    method: "GET"
  });
}

export function retryBotMessage(messageId: string): Promise<{
  status: string;
  deliveryId: string;
  attemptCount: number;
}> {
  return requestJson("bot-adapter-service", `/endpoint/bot-adapter/messages/${encodeURIComponent(messageId)}/retry`, {
    method: "POST",
    body: JSON.stringify({})
  });
}

export function listMiniAppBuilds(params?: {
  tenantKey?: string;
  siteKey?: string;
}): Promise<BotMiniAppBuild[]> {
  const search = new URLSearchParams();
  if (params?.tenantKey) search.set("tenantKey", params.tenantKey);
  if (params?.siteKey) search.set("siteKey", params.siteKey);
  const suffix = search.size ? `?${search.toString()}` : "";
  return requestJson<BotMiniAppBuild[]>("bot-adapter-service", `/endpoint/bot-adapter/mini-apps${suffix}`, {
    method: "GET"
  });
}

export function upsertMiniAppBuild(request: {
  channel: "TELEGRAM" | "BALE";
  integrationKey: string;
  buildKey: string;
  title: string;
  launchUrl: string;
  manifest: Record<string, unknown>;
}): Promise<BotMiniAppBuild> {
  return requestJson<BotMiniAppBuild>("bot-adapter-service", "/endpoint/bot-adapter/mini-apps", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function publishMiniAppBuild(channel: "TELEGRAM" | "BALE", integrationKey: string, buildKey: string): Promise<BotMiniAppBuild> {
  return requestJson<BotMiniAppBuild>("bot-adapter-service", `/endpoint/bot-adapter/mini-apps/${channel}/${integrationKey}/${buildKey}/publish`, {
    method: "POST",
    body: JSON.stringify({})
  });
}
