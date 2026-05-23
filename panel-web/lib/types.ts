export type PlatformAppType = "WEBSITE" | "BLOG" | "SHOP" | "CRM" | "FORM_FLOW" | "BPM_PORTAL" | "MIXED_BUSINESS_APP";

export type GeneratePlatformAppRequest = {
  prompt: string;
  tenantKey?: string;
  siteKey?: string;
  clientKey?: string;
  sessionId?: string;
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
  draftId?: string | null;
  sessionId?: string | null;
  dsl: PlatformAppDslDefinition;
  nextQuestions: string[];
  provisioningResult: ProvisioningResult | null;
};

export type AppBlueprint = {
  id?: string;
  blueprintKey: string;
  appType: string;
  version: number;
  title: string;
  description: string;
  active: boolean;
  capabilities: string[];
  requiredQuestions: Array<Record<string, unknown>>;
  defaultAnswers: Record<string, unknown>;
  baseDsl: PlatformAppDslDefinition;
};

export type ClientAppDraft = {
  id?: string;
  draftId: string;
  tenantKey: string;
  siteKey: string;
  clientKey?: string | null;
  blueprintKey?: string | null;
  blueprintVersion?: number | null;
  status: "WAITING_FOR_ANSWERS" | "READY" | "PROVISIONED" | "FAILED" | string;
  title: string;
  appType: string;
  latestIntent: string;
  answers: Record<string, unknown>;
  resolvedDsl: PlatformAppDslDefinition;
  pendingQuestionKeys: string[];
  pendingQuestions: string[];
  manualActions: string[];
  latestSessionId?: string | null;
  revision?: number | null;
  createdAt?: string;
  updatedAt?: string;
};

export type ProvisioningRun = {
  runId: string;
  draftId: string;
  tenantKey?: string;
  siteKey?: string;
  status: string;
  triggerType?: string;
  triggeredBy?: string;
  startedAt?: string;
  finishedAt?: string;
  stepResults: Array<Record<string, unknown>>;
  result?: ProvisioningResult | null;
};

export type DynamicServiceKey =
  | "content-service"
  | "catalog-service"
  | "crm-service"
  | "commerce-service"
  | "finance-service"
  | "inventory-service"
  | "report-service"
  | "storefront-service"
  | "media-service"
  | "cart-service"
  | "checkout-service"
  | "payment-service"
  | "pricing-promotion-service"
  | "notification-service"
  | "search-index-service"
  | "bpm-service";

export type DynamicEntityTemplate = {
  templateKey: string;
  entityType?: string;
  title?: string;
  description?: string;
  definitionJson?: string;
};

export type DynamicEntityDefinition = {
  id?: number | string;
  serviceKey: string;
  entityKey: string;
  tenantKey?: string | null;
  siteKey?: string | null;
  entityType?: string;
  title?: string;
  definitionJson: string;
  active?: boolean;
};

export type UserSummary = {
  username: string;
  email?: string;
  phoneNumber?: string;
  mfaEnabled: boolean;
  roles: string[];
  active: boolean;
};

export type DynamicEntityRecord = {
  id?: string;
  serviceKey?: string;
  entityKey?: string;
  recordKey: string;
  tenantKey?: string | null;
  siteKey?: string | null;
  data: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
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

export type BotChannelIntegration = {
  id?: string;
  channel: "TELEGRAM" | "BALE";
  integrationKey: string;
  tenantKey: string;
  siteKey: string;
  clientKey?: string | null;
  appTypeHint?: string | null;
  botId?: string | null;
  botUsername?: string | null;
  tokenSecretRef?: string | null;
  tokenFingerprint?: string | null;
  webhookSecret?: string | null;
  miniAppUrl?: string | null;
  miniAppEnabled?: boolean;
  miniAppStartParam?: string | null;
  providerConfig?: Record<string, unknown>;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type BotOutboundMessage = {
  id?: string;
  channel: "TELEGRAM" | "BALE";
  integrationKey: string;
  tenantKey?: string;
  siteKey?: string;
  clientKey?: string | null;
  externalChatId: string;
  sessionId?: string | null;
  text: string;
  status: string;
  attemptCount: number;
  errorMessage?: string | null;
  providerResponse?: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
  lastAttemptAt?: string;
  deliveredAt?: string;
};

export type BotMiniAppBuild = {
  id?: string;
  channel: "TELEGRAM" | "BALE";
  integrationKey: string;
  buildKey: string;
  tenantKey?: string;
  siteKey?: string;
  title?: string;
  status?: string;
  launchUrl?: string;
  publishedUrl?: string;
  manifest?: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
};

export type RealmSummary = {
  realmKey: string;
  displayName: string;
  description?: string;
  active: boolean;
};

export type ClientSummary = {
  clientId: string;
  realmKey: string;
  displayName: string;
  description?: string;
  active: boolean;
  publicClient: boolean;
  redirectUris: string[];
};

export type RoleCatalogSummary = {
  scopeType: string;
  scopeKey: string;
  roleKey: string;
  displayName: string;
  description?: string;
  active: boolean;
  permissions: string[];
};

export type IamClientAccessSummary = {
  clientId: string;
  realmKey: string;
  clientRoles: string[];
  clientPermissions: string[];
};

export type IamUserAccessSummary = {
  username: string;
  realmKey: string;
  realmRoles: string[];
  realmPermissions: string[];
  clients: IamClientAccessSummary[];
};

export type NotificationDispatchResponse = {
  messageKey?: string;
  status?: string;
  provider?: string;
  providerMessageId?: string;
  details?: string;
  [key: string]: unknown;
};

export type SearchQueryResponse = {
  query?: string;
  total?: number;
  page?: number;
  size?: number;
  results?: Array<Record<string, unknown>>;
  [key: string]: unknown;
};

export type SearchSuggestionResponse = {
  query?: string;
  suggestions?: string[];
  [key: string]: unknown;
};

export type AutomationExecution = {
  executionId?: string;
  automationKey?: string;
  status?: string;
  startedAt?: string;
  finishedAt?: string;
  [key: string]: unknown;
};

export type PaymentMethodRequest = {
  methodKey: string;
  displayName: string;
  providerCode: string;
  region: string;
  flowType: string;
  enabled: boolean;
  active: boolean;
  priorityOrder: number;
  supportedCurrencies: string[];
  configuration: Record<string, unknown>;
  description?: string;
};

export type PaymentMethodAdmin = PaymentMethodRequest & {
  createdAt?: string;
  updatedAt?: string;
};

export type PaymentSessionRequest = {
  paymentMethodKey: string;
  orderKey?: string;
  invoiceKey?: string;
  customerKey?: string;
  relatedService?: string;
  relatedEntityType?: string;
  relatedEntityKey?: string;
  amount: number;
  currency: string;
  callbackUrl?: string;
  successUrl?: string;
  failureUrl?: string;
  metaData?: Record<string, string>;
};

export type PaymentSessionResponse = {
  paymentSessionKey?: string;
  transactionKey?: string;
  status?: string;
  paymentUrl?: string;
  methodKey?: string;
  [key: string]: unknown;
};
