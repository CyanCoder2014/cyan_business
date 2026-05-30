import { expect, test, type Page } from "@playwright/test";

const storageKeys = {
  accessToken: "cyan.panel.authToken",
  refreshToken: "cyan.panel.refreshToken",
  expiresAt: "cyan.panel.authExpiresAt",
  sessionId: "cyan.panel.sessionId"
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
  await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):8001\/endpoint\/bpm\/flows.*/, async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):8001\/endpoint\/bpm\/metadata\/state-actions.*/, async (route) => {
    await route.fulfill({ json: [] });
  });
  await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):8001\/endpoint\/bpm\/metadata\/transition-conditions.*/, async (route) => {
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

async function seedAuth(page: Page) {
  await page.addInitScript((keys) => {
    window.localStorage.setItem(keys.accessToken, "seeded-access");
    window.localStorage.setItem(keys.refreshToken, "seeded-refresh");
    window.localStorage.setItem(keys.expiresAt, String(Date.now() + 3_600_000));
    window.localStorage.setItem(keys.sessionId, "session-seeded");
  }, storageKeys);
}
