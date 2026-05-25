export type RoadmapPhase = {
  title: string;
  outcome: string;
};

export type RoadmapTrack = {
  id: string;
  order: number;
  title: string;
  status: "prototype" | "integration" | "hardening" | "planned";
  summary: string;
  routes: string[];
  services: string[];
  dependencies: string[];
  phases: RoadmapPhase[];
  launchGate: string[];
};

export const productRoadmap: RoadmapTrack[] = [
  {
    id: "website-builder",
    order: 1,
    title: "Visual website/page builder",
    status: "prototype",
    summary: "Turn landing pages, theme layouts, and storefront routes into a visual publish flow backed by content-service and storefront-service records.",
    routes: ["/site-builder", "/projects/new", "/integrations"],
    services: ["storefront-service", "content-service", "media-service", "search-index-service", "api-gateway"],
    dependencies: ["Publish-state workflow", "Reusable presentation template registry", "Domain binding and preview URLs"],
    phases: [
      {
        title: "Phase 1: builder baseline",
        outcome: "Keep the current block palette, live preview, theme record, and route record publisher stable for tenant/site-scoped content."
      },
      {
        title: "Phase 2: reusable sections and themes",
        outcome: "Add saved sections, theme presets, header/footer variants, and media selection without breaking strict dynamic records."
      },
      {
        title: "Phase 3: publishing controls",
        outcome: "Introduce draft vs published states, preview links, SEO metadata checks, sitemap updates, and domain readiness checks."
      }
    ],
    launchGate: [
      "A non-technical operator can create a landing page and publish it to `/public/storefront/render` without hand-editing JSON.",
      "Preview and published routes remain tenant/site-correct.",
      "SEO metadata, robots, sitemap, and public media references are smoke-tested through the gateway."
    ]
  },
  {
    id: "bot-messaging",
    order: 2,
    title: "Outbound Telegram/Bale messaging",
    status: "integration",
    summary: "Finish the missing half of the bot channel story so integrations can both ingest webhooks and send messages or workflow updates back to Telegram and Bale safely.",
    routes: ["/integrations", "/bot"],
    services: ["bot-adapter-service", "ai-orchestrator-service", "notification-service", "bpm-service", "api-gateway"],
    dependencies: ["Secret manager for bot tokens", "Outbound provider client contracts", "Retry and delivery audit trail"],
    phases: [
      {
        title: "Phase 1: secure integration registry",
        outcome: "Keep `tokenSecretRef` as the persisted source of truth, fingerprint write-only tokens, and preserve tenant/site/client mappings."
      },
      {
        title: "Phase 2: outbound delivery pipeline",
        outcome: "Add service-owned send APIs, provider-specific payload builders, and idempotent delivery records for notifications, workflow prompts, and AI responses."
      },
      {
        title: "Phase 3: operator controls",
        outcome: "Expose webhook health, delivery status, retry actions, and channel capability flags in the panel."
      }
    ],
    launchGate: [
      "Inbound and outbound message flow share the same session continuity rules.",
      "No bot token value is returned by API responses or logs.",
      "Telegram and Bale delivery retries are observable per tenant/site/integration."
    ]
  },
  {
    id: "form-flow-builder",
    order: 3,
    title: "Advanced form/flow builder",
    status: "prototype",
    summary: "Merge the current entity maker and flow canvas into a single build path for forms, validations, BPM states, submit actions, and automation fan-out.",
    routes: ["/maker", "/flows", "/data"],
    services: ["dynamic-entity-core", "bpm-service", "event-service", "notification-service", "automation-orchestrator-service"],
    dependencies: ["Shared entity-to-form schema mapping", "BPM state action templates", "Event fan-out action presets"],
    phases: [
      {
        title: "Phase 1: schema and state alignment",
        outcome: "Link maker fields directly to BPM form keys, state submit modes, and strict validation previews."
      },
      {
        title: "Phase 2: action-rich flow builder",
        outcome: "Add transition conditions, notification actions, API actions, and event emission presets without bypassing `event-service`."
      },
      {
        title: "Phase 3: managed object lifecycle",
        outcome: "Support active-form rendering, record locking, revision history, and resubmission paths for live workflows."
      }
    ],
    launchGate: [
      "A generated or manual form can be published, attached to a BPM flow, submitted, and advanced across at least one approval path.",
      "Validation errors match service behavior before publish.",
      "Automation side effects use event fan-out instead of direct cross-service shortcuts."
    ]
  },
  {
    id: "test-harness",
    order: 4,
    title: "End-to-end test harness and market-readiness checklist",
    status: "planned",
    summary: "Turn the existing market-ready notes into a repeatable gate covering panel build quality, gateway contracts, bot/session flow, storefront rendering, and multilingual/mobile checks.",
    routes: ["/", "/projects/new", "/site-builder", "/bot", "/integrations"],
    services: ["panel-web", "api-gateway", "ai-orchestrator-service", "storefront-service", "bpm-service", "bot-adapter-service"],
    dependencies: ["Stable local demo environment", "Seed data for tenant/site scenarios", "Scripted smoke-test entrypoints"],
    phases: [
      {
        title: "Phase 1: smoke harness",
        outcome: "Script core happy paths for AI draft generation, site publish/render, bot integration save, and BPM flow publish."
      },
      {
        title: "Phase 2: readiness checklist",
        outcome: "Turn mobile viewport, PWA metadata, Farsi/English direction, and dark/light checks into explicit pass/fail items."
      },
      {
        title: "Phase 3: release gate",
        outcome: "Require panel build/lint plus targeted backend tests before calling the app market-ready."
      }
    ],
    launchGate: [
      "The harness can verify at least one full path from prompt to public route and one path from bot session to outbound delivery.",
      "Checklist results are visible and actionable, not implicit tribal knowledge.",
      "Release claims are blocked when gateway, panel, or core service smoke tests fail."
    ]
  }
];
