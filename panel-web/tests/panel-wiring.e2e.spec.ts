import { expect, test, type Page } from "@playwright/test";

const storageKeys = {
  accessToken: "cyan.panel.authToken",
  refreshToken: "cyan.panel.refreshToken",
  expiresAt: "cyan.panel.authExpiresAt",
  sessionId: "cyan.panel.sessionId",
  username: "cyan.panel.username"
};

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
  await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/sessions?draftId=draft-retail", async (route) => {
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

  await page.goto("/projects/draft-retail");

  await expect(page.getByRole("heading", { name: "Retail Hub" })).toBeVisible();
  await expect(page.getByText("Build a retail workspace with storefront and approvals.")).toBeVisible();
  await expect(page.getByText("Review copy")).toBeVisible();
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

test("data and flows pages show backend-empty states instead of fixture data", async ({ page }) => {
  await page.route("**/api/platform/dynamic/**/endpoint/entities/records/**", async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):(?:8001|18001)\/endpoint\/bpm\/flows.*/, async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):(?:8001|18001)\/endpoint\/bpm\/metadata\/state-actions.*/, async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):(?:8001|18001)\/endpoint\/bpm\/metadata\/transition-conditions.*/, async (route) => {
    await route.fulfill({
      json: {
        operators: ["EQ"],
        logicalOperators: ["AND"],
        supportedFields: ["status"]
      }
    });
  });

  await page.goto("/data");
  await expect(page.getByText("No records were returned for this bucket.")).toBeVisible();
  await expect(page.getByText("Luna Lounge Chair")).toHaveCount(0);

  await page.goto("/flows");
  await expect(page.getByText("No flow was returned by the API")).toBeVisible();
  await expect(page.getByText("Route to review")).toHaveCount(0);
});

test("site builder loads backend routes and publishes a route without static page fixtures", async ({ page }) => {
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
        definitionJson: "{\"fields\":[]}"
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

  await page.goto("/site-builder");

  await expect(page.getByRole("button", { name: /Home/ }).first()).toBeVisible();
  await expect(page.getByText("/", { exact: true }).first()).toBeVisible();
  await expect(page.getByText("About")).toHaveCount(0);

  await page.getByLabel("Page title").fill("Support");
  await page.getByLabel("Path").fill("/support");
  await page.getByRole("button", { name: "Publish" }).first().click();

  await expect(page.getByText("Published.")).toBeVisible();
  await expect(page.getByLabel("Path")).toHaveValue("/support");
  await expect(page.getByRole("button", { name: /Support/ }).first()).toBeVisible();
});

test("integrations page stays empty without backend data and reflects real mini app publishing", async ({ page }) => {
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

  await expect(page.getByText("No outbound messages were returned for this channel.")).toBeVisible();
  await expect(page.getByText("Bale Bot")).toHaveCount(0);
  await expect(page.getByText("1248")).toHaveCount(0);

  await page.getByRole("button", { name: "Create build" }).click();
  await expect(page.getByText("Mini app provisioned.")).toBeVisible();

  const publishButton = page.getByRole("button", { name: "Publish mini app" });
  await publishButton.scrollIntoViewIfNeeded();
  await publishButton.click({ force: true });
  await expect(page.getByText("Mini app published.")).toBeVisible();
  await expect(page.getByText("https://miniapp.cyan.app/telegram-main").first()).toBeVisible();
});

test("profile page renders live account data and logout returns to auth", async ({ page }) => {
  let logoutCalls = 0;

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

  await expect(page.getByRole("heading", { name: "Profile & Settings" })).toBeVisible();
  await expect(page.getByText("user@cyan.local").first()).toBeVisible();
  await expect(page.getByText("+989121234567")).toBeVisible();
  await expect(page.getByText("\"builder:*\"")).toBeVisible();

  await page.getByRole("button", { name: "Sign out" }).click();

  await expect(page).toHaveURL(/\/auth$/);
  await expect.poll(() => logoutCalls).toBe(1);
  await expect(page.getByRole("button", { name: "Sign in" }).first()).toBeVisible();
});

async function seedAuth(page: Page) {
  await page.addInitScript((keys) => {
    window.localStorage.setItem(keys.accessToken, "seeded-access");
    window.localStorage.setItem(keys.refreshToken, "seeded-refresh");
    window.localStorage.setItem(keys.expiresAt, String(Date.now() + 3_600_000));
    window.localStorage.setItem(keys.sessionId, "session-seeded");
    window.localStorage.setItem(keys.username, "user@cyan.local");
  }, storageKeys);
}
