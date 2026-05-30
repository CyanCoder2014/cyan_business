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
