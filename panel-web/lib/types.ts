export type PlatformAppType = "WEBSITE" | "BLOG" | "SHOP" | "CRM" | "FORM_FLOW" | "BPM_PORTAL" | "MIXED_BUSINESS_APP";

export type GeneratePlatformAppRequest = {
  prompt: string;
  tenantKey?: string;
  siteKey?: string;
  execute: boolean;
  answers?: Record<string, unknown>;
};

export type ProvisioningResult = {
  status: string;
  createdDefinitions: Array<Record<string, unknown>>;
  createdRecords: Array<Record<string, unknown>>;
  createdFlows: Array<Record<string, unknown>>;
  deliveryEndpoints: Array<Record<string, unknown>>;
  manualActions: string[];
};

export type PlatformAppDslDefinition = {
  app: {
    appKey?: string;
    title?: string;
    type?: PlatformAppType;
    tenantKey?: string;
    siteKey?: string;
    desiredDomain?: string;
    capabilities?: string[];
  };
  entities: Array<Record<string, unknown>>;
  routes: Array<Record<string, unknown>>;
  flows: Array<Record<string, unknown>>;
  delivery: {
    publicApis: string[];
    botApis: string[];
  };
  manualActions: string[];
};

export type GeneratePlatformAppResponse = {
  dsl: PlatformAppDslDefinition;
  nextQuestions: string[];
  provisioningResult: ProvisioningResult | null;
};

export type ProjectDraft = {
  id: string;
  title: string;
  prompt: string;
  tenantKey: string;
  siteKey: string;
  updatedAt: string;
  status: "DRAFT" | "PROVISIONED" | "READY" | "REVIEW";
  dsl: PlatformAppDslDefinition;
  nextQuestions: string[];
  provisioningResult: ProvisioningResult | null;
};

export type BotChannel = "telegram" | "bale";

export type BotMessageRole = "user" | "assistant" | "system";

export type BotMessage = {
  id: string;
  role: BotMessageRole;
  content: string;
  createdAt: string;
};

export type BotConversationSession = {
  id: string;
  channel: BotChannel;
  title: string;
  tenantKey: string;
  siteKey: string;
  draftId: string | null;
  status: "OPEN" | "WAITING_FOR_ANSWERS" | "RESOLVED" | "FAILED";
  appType: PlatformAppType;
  lastPrompt: string;
  answers: Record<string, unknown>;
  messages: BotMessage[];
  createdAt: string;
  updatedAt: string;
};
