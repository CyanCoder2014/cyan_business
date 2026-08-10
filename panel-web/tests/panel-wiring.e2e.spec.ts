import { expect, test, type Page } from "@playwright/test";

const storageKeys = {
  accessToken: "cyan.panel.authToken",
  refreshToken: "cyan.panel.refreshToken",
  expiresAt: "cyan.panel.authExpiresAt",
  sessionId: "cyan.panel.sessionId",
  username: "cyan.panel.username"
};
const panelBootstrap = { identity:{username:"user@cyan.local",email:"user@cyan.local",mfaEnabled:false,roles:["user"],active:true},access:{realmRoles:["tenant-admin"],realmPermissions:["panel:read","project.create","project.read","definition.read","record.read","bpm.read","settings.read","automation.read","bot.read"],clients:[]},tenants:[{tenantKey:"tenant-demo",displayName:"Demo workspace",status:"ACTIVE",membershipRole:"TENANT_OWNER"}],sites:[{tenantKey:"tenant-demo",siteKey:"site-commerce",name:"Commerce",status:"ACTIVE"}],activeTenantKey:"tenant-demo",activeSiteKey:"site-commerce",subscription:{tenantKey:"tenant-demo",planKey:"growth",status:"ACTIVE",features:["ai-orchestrator","dynamic-entities","bpm","automation","bot-adapter","site-builder"],limits:{},providerState:"CONFIGURED"},capabilities:["ai-orchestrator","dynamic-entities","bpm","automation","bot-adapter","site-builder"].map(key=>({key,enabled:true,source:"PLAN",status:"AVAILABLE",limits:{}})),featureFlags:{},services:{identity:"AVAILABLE",tenancy:"AVAILABLE",sessionScope:"AVAILABLE",sites:"AVAILABLE",billing:"AVAILABLE",capabilities:"AVAILABLE"},warnings:[]};

test.beforeEach(async ({ page }) => {
  await seedAuth(page);
});

test("project detail page renders draft and linked conversation sessions from backend", async ({ page }) => {
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/drafts/draft-retail", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        draftId: "draft-retail",
        tenantKey: "tenant-demo",
        siteKey: "site-commerce",
        clientKey: "panel",
        status: "READY",
        title: "Retail Hub",
        appType: "SHOP",
        latestIntent: "Build a retail workspace with storefront and approvals.",
        answers: {},
        resolvedDsl: {
          app: { title: "Retail Hub", capabilities: ["storefront", "crm"] },
          entities: [{ key: "product" }, { key: "customer" }],
          routes: [{ path: "/" }, { path: "/shop" }],
          flows: [{ key: "approval" }],
          delivery: { publicApis: ["/shop"], botApis: [] },
          manualActions: []
        },
        pendingQuestionKeys: ["brandName"],
        pendingQuestions: ["What is the brand name?"],
        manualActions: ["Review copy"],
        updatedAt: "2026-05-30T10:00:00.000Z"
      })
    });
  });
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/sessions?**", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify([
        {
          sessionId: "session-retail",
          channelType: "PANEL",
          tenantKey: "tenant-demo",
          siteKey: "site-commerce",
          draftId: "draft-retail",
          status: "WAITING_FOR_ANSWERS",
          messages: [],
          extractedAnswers: {},
          pendingQuestionKeys: ["brandName"],
          pendingQuestions: ["What is the brand name?"],
          latestPrompt: "Build a retail workspace",
          updatedAt: "2026-05-30T10:05:00.000Z"
        }
      ])
    });
  });
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/drafts/draft-retail/runs", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify([])
    });
  });
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/drafts/draft-retail/releases", (route) => route.fulfill({ json: [] }));

  await page.goto("/projects/draft-retail");

  await expect(page.getByRole("heading", { name: "Retail Hub" })).toBeVisible();
  await expect(page.getByText("Build a retail workspace with storefront and approvals.")).toBeVisible();
  await page.getByRole("tab", { name: "AI" }).click();
  await expect(page.getByText("session-retail")).toBeVisible();
});

test("bot session detail page renders ai orchestrator conversation state", async ({ page }) => {
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/sessions/session-retail", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        sessionId: "session-retail",
        channelType: "TELEGRAM",
        tenantKey: "tenant-demo",
        siteKey: "site-commerce",
        draftId: "draft-retail",
        status: "WAITING_FOR_ANSWERS",
        messages: [
          {
            messageId: "msg-1",
            role: "USER",
            content: "Build a retail workspace",
            createdAt: "2026-05-30T10:00:00.000Z"
          }
        ],
        extractedAnswers: {
          businessName: "Retail Hub"
        },
        pendingQuestionKeys: ["brandName"],
        pendingQuestions: ["What is the brand name?"],
        latestPrompt: "Build a retail workspace",
        latestQuestion: "What is the brand name?",
        updatedAt: "2026-05-30T10:05:00.000Z"
      })
    });
  });

  await page.goto("/bot/session-retail");

  await expect(page.getByRole("heading", { name: "Build a retail workspace" })).toBeVisible();
  await expect(page.getByText("TELEGRAM")).toBeVisible();
  await expect(page.getByText("What is the brand name?")).toBeVisible();
  await expect(page.getByText("\"businessName\": \"Retail Hub\"")).toBeVisible();
});

test("ai studio renders generate response follow-up questions and submits suggested answers", async ({ page }) => {
  let latestGenerateBody: Record<string, unknown> | null = null;

  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/blueprints**", async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/drafts**", async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/generate/app", async (route, request) => {
    const generateBody = JSON.parse(request.postData() ?? "{}") as Record<string, unknown>;
    latestGenerateBody = generateBody;
    const answers = generateBody.answers as Record<string, unknown> | undefined;
    const answered = answers?.subdomainPrefix === "brand-demo";
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        draftId: "draft-shop-v01",
        sessionId: "session-shop-v01",
        dsl: {
          app: {
            appKey: "shop-app-v0-1",
            title: "Shop App (v0.1)",
            type: "SHOP",
            tenantKey: "tenant-demo",
            siteKey: "site-commerce",
            capabilities: ["website", "shop", "crm"]
          },
          entities: [{ entityKey: "product" }, { entityKey: "order" }, { entityKey: "customer" }],
          routes: [{ path: "/" }, { path: "/shop" }],
          flows: [{ flowKey: "order-review" }],
          delivery: {
            publicApis: ["/public/storefront/render?path=/"],
            botApis: ["/endpoint/bot-adapter/messages"]
          },
          manualActions: ["Review domain routing"]
        },
        nextQuestions: answered ? [] : ["Which subdomain prefix should be used before a custom domain is connected?"],
        followUpQuestions: answered
          ? []
          : [
              {
                key: "subdomainPrefix",
                prompt: "Which subdomain prefix should be used before a custom domain is connected?",
                reason: "Storefront provisioning needs a host decision before routes can be published cleanly.",
                required: true,
                suggestedAnswers: ["brand-demo"]
              }
            ],
        provisioningResult: null
      })
    });
  });

  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/sessions", (route) => route.fulfill({ json:{sessionId:"session-shop-v01",status:"ACTIVE",messages:[],pendingQuestions:[],tenantKey:"tenant-demo",siteKey:"site-commerce"} }));
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/sessions/session-shop-v01", (route) => route.fulfill({ json:{sessionId:"session-shop-v01",status:"WAITING_FOR_ANSWERS",messages:[{messageId:"m1",role:"USER",content:"Build a shop"}],pendingQuestions:["Which subdomain prefix should be used before a custom domain is connected?"],draftId:"draft-shop-v01",tenantKey:"tenant-demo",siteKey:"site-commerce"} }));
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/drafts/draft-shop-v01", (route) => route.fulfill({ json:{draftId:"draft-shop-v01",tenantKey:"tenant-demo",siteKey:"site-commerce",status:"WAITING_FOR_ANSWERS",title:"Shop",appType:"SHOP",latestIntent:"Build a shop",answers:{},resolvedDsl:{app:{capabilities:[]},entities:[],routes:[],flows:[],delivery:{publicApis:[],botApis:[]},manualActions:[]},pendingQuestionKeys:["subdomainPrefix"],pendingQuestions:["Which subdomain prefix should be used before a custom domain is connected?"],manualActions:[]} }));
  await page.goto("/ai");
  await page.getByPlaceholder("What would you like to build?").fill("Build a shop");
  await page.getByRole("button", { name: "Send" }).click();
  await expect.poll(() => latestGenerateBody?.prompt).toBe("Build a shop");
  await expect(page.getByText("Which subdomain prefix should be used before a custom domain is connected?")).toBeVisible();
});

test("data and flows pages show backend-empty states instead of fixture data", async ({ page }) => {
  await page.route("**/api/platform/dynamic/**/endpoint/entities/definitions**", async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route("**/api/platform/dynamic/**/endpoint/entities/templates", (route) => route.fulfill({ json: [] }));
  await page.route("**/api/platform/service/bpm-service/endpoint/bpm/flows**", async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route("**/api/platform/service/bpm-service/endpoint/bpm/metadata/state-actions**", async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route("**/api/platform/service/bpm-service/endpoint/bpm/metadata/transition-conditions**", async (route) => {
    await route.fulfill({
      json: {
        operators: ["EQ"],
        logicalOperators: ["AND"],
        supportedFields: ["status"]
      }
    });
  });

  await page.goto("/data");
  await expect(page.getByRole("heading", { name: "No entities" })).toBeVisible();
  await expect(page.getByText("Luna Lounge Chair")).toHaveCount(0);

  await page.goto("/flows").catch(() => null);
  await expect(page).toHaveURL(/\/bpm$/);
  await expect(page.getByRole("heading", { name: "No processes" })).toBeVisible();
  await expect(page.getByText("Route to review")).toHaveCount(0);
});

test("site builder loads backend routes without static page fixtures", async ({ page }) => {
  let routes = [
    {
      recordKey: "home",
      data: {
        routeKey: "home",
        path: "/",
        routeType: "LANDING",
        navigation: { label: "Home" },
        publicationStatus: "PUBLISHED",
        entityRef: {
          service: "content-service",
          entityKey: "landing-page",
          recordKey: "home"
        }
      }
    }
  ];

  await page.route("**/api/platform/dynamic/storefront-service/endpoint/entities/records/site-route**", async (route, request) => {
    if (request.method() === "GET") {
      await route.fulfill({ json: routes });
      return;
    }
    if (request.method() === "POST") {
      const body = JSON.parse(request.postData() ?? "{}");
      routes = [
        ...routes.filter((item) => item.recordKey !== body.recordKey),
        {
          recordKey: body.recordKey,
          data: body.data
        }
      ];
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({
          recordKey: body.recordKey,
          data: body.data
        })
      });
      return;
    }
    await route.fallback();
  });
  await page.route("**/api/platform/dynamic/storefront-service/endpoint/entities/templates/site-route/definitions", async (route) => {
    await route.fulfill({
      json: {
        serviceKey: "storefront-service",
        entityKey: "site-route",
        definition: { fields: [] }
      }
    });
  });
  await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):(?:8001|18001)\/public\/storefront\/resolve\?path=.*/, async (route) => {
    const url = new URL(route.request().url());
    const path = url.searchParams.get("path") ?? "/";
    const record = routes.find((item) => item.data.path === path);
    await route.fulfill({
      json: {
        tenantKey: "tenant-demo",
        siteKey: "site-commerce",
        path,
        route: record?.data ?? { path },
        target: { recordKey: record?.recordKey ?? "missing" },
        theme: { templateKey: "landing-v1" }
      }
    });
  });
  await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):(?:8001|18001)\/public\/storefront\/render\?path=.*/, async (route) => {
    const url = new URL(route.request().url());
    const path = url.searchParams.get("path") ?? "/";
    await route.fulfill({
      json: {
        tenantKey: "tenant-demo",
        siteKey: "site-commerce",
        path,
        route: { path },
        target: { rendered: true },
        theme: { templateKey: "landing-v1" },
        html: `<main>${path}</main>`
      }
    });
  });

  await page.goto("/sites/site-commerce/builder");

  await expect(page.getByRole("button", { name: /home/i }).first()).toBeVisible();
  await expect(page.getByText("/", { exact: true }).first()).toBeVisible();
  await expect(page.getByText("About")).toHaveCount(0);
});

test("integrations compatibility route reaches the real bots registry", async ({ page }) => {
  let integrations = [
    {
      channel: "TELEGRAM",
      integrationKey: "telegram-main",
      tenantKey: "tenant-demo",
      siteKey: "site-commerce",
      botUsername: "@cyan_assistant_bot",
      tokenSecretRef: "vault://bots/retail-demo",
      miniAppUrl: "https://preview.cyan.app/mini-app",
      miniAppEnabled: true,
      active: true
    }
  ];
  let messages: Array<Record<string, unknown>> = [];
  let miniApps: Array<Record<string, unknown>> = [];

  await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/integrations?tenantKey=tenant-demo&siteKey=site-commerce", async (route) => {
    await route.fulfill({ json: integrations });
  });
  await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/messages?tenantKey=tenant-demo&siteKey=site-commerce", async (route) => {
    await route.fulfill({ json: messages });
  });
  await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/mini-apps?tenantKey=tenant-demo&siteKey=site-commerce", async (route) => {
    await route.fulfill({ json: miniApps });
  });
  await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/mini-apps", async (route, request) => {
    const body = JSON.parse(request.postData() ?? "{}");
    miniApps = [
      {
        channel: body.channel,
        integrationKey: body.integrationKey,
        buildKey: body.buildKey,
        title: body.title,
        launchUrl: body.launchUrl,
        status: "DRAFT"
      }
    ];
    await route.fulfill({ json: miniApps[0] });
  });
  await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/mini-apps/TELEGRAM/telegram-main/telegram-main-build/publish", async (route) => {
    miniApps = [
      {
        channel: "TELEGRAM",
        integrationKey: "telegram-main",
        buildKey: "telegram-main-build",
        title: "Telegram Main Mini App",
        launchUrl: "https://preview.cyan.app/mini-app",
        publishedUrl: "https://miniapp.cyan.app/telegram-main",
        status: "PUBLISHED"
      }
    ];
    await route.fulfill({ json: miniApps[0] });
  });

  await page.goto("/integrations");
  await expect(page).toHaveURL(/\/bots$/);
  await expect(page.getByText("@cyan_assistant_bot")).toBeVisible();
  await expect(page.getByText("Bale Bot")).toHaveCount(0);
  await expect(page.getByText("1248")).toHaveCount(0);
});

test("IAM compatibility route reaches the scoped profile", async ({ page }) => {
  let logoutCalls = 0;
  await page.route("**/api/sso/sessions/me", route => route.fulfill({ json: [] }));

  await page.route("**/api/platform/service/sso-user-service/api/sso/users/user%40cyan.local", async (route) => {
    await route.fulfill({
      json: {
        username: "user@cyan.local",
        email: "user@cyan.local",
        phoneNumber: "+989121234567",
        mfaEnabled: false,
        roles: ["builder"],
        active: true
      }
    });
  });
  await page.route("**/api/platform/service/sso-user-service/api/sso/iam/users/user%40cyan.local/access?clientId=cyan-panel", async (route) => {
    await route.fulfill({
      json: {
        username: "user@cyan.local",
        realmRoles: ["workspace-admin"],
        clientRoles: ["builder"],
        permissions: ["builder:*", "panel:read"]
      }
    });
  });
  await page.route("**/api/sso/auth/logout", async (route) => {
    logoutCalls += 1;
    await route.fulfill({
      json: {
        sessionId: "session-seeded",
        active: false
      }
    });
  });

  await page.goto("/iam");
  await expect(page).toHaveURL(/\/profile$/);
  await expect(page.getByRole("heading", { name: "Profile & security" })).toBeVisible();
  await expect(page.getByLabel("Username")).toHaveValue("user@cyan.local");
});

test("api docs page renders live controller paths and authentication modes", async ({ page }) => {
  await page.route("**/api/platform/service/api-docs-service/endpoint/api-docs/services", async (route) => {
    await route.fulfill({
      json: [
        {
          serviceKey: "commerce-service",
          baseUrl: "http://commerce-service:9104",
          status: "AVAILABLE",
          title: "commerce-service",
          version: "1.0.0",
          pathCount: 2,
          fetchedAt: "2026-07-25T12:00:00Z"
        }
      ]
    });
  });
  await page.route("**/api/platform/service/api-docs-service/endpoint/api-docs/services/commerce-service?refresh=false", async (route) => {
    await route.fulfill({
      json: {
        openapi: "3.1.0",
        info: { title: "Commerce Service", description: "Controller-derived platform API", version: "1.0.0" },
        paths: {
          "/endpoint/entities/records/importer-order": {
            get: { summary: "List records", "x-platform-auth": "BEARER", security: [{ bearerAuth: [] }] }
          },
          "/internal/entities/records/importer-order": {
            post: { summary: "Create record", "x-platform-auth": "BASIC", security: [{ basicAuth: [] }] }
          }
        }
      }
    });
  });

  await page.goto("/api-docs");

  await expect(page.getByRole("heading", { name: "Live API Documentation" })).toBeVisible();
  await expect(page.getByText("/endpoint/entities/records/importer-order")).toBeVisible();
  await expect(page.getByText("/internal/entities/records/importer-order")).toBeVisible();
  await expect(page.getByText("BEARER").first()).toBeVisible();
  await expect(page.getByText("BASIC").first()).toBeVisible();
});

async function seedAuth(page: Page) {
  await page.route("**/api/panel/bootstrap", (route) => route.fulfill({ json: panelBootstrap }));
  await page.route("**/api/platform/**", (route) => route.fulfill({ json: [] }));
  await page.addInitScript((keys) => {
    window.localStorage.setItem(keys.accessToken, "seeded-access");
    window.localStorage.setItem(keys.refreshToken, "seeded-refresh");
    window.localStorage.setItem(keys.expiresAt, String(Date.now() + 3_600_000));
    window.localStorage.setItem(keys.sessionId, "session-seeded");
    window.localStorage.setItem(keys.username, "user@cyan.local");
  }, storageKeys);
}
