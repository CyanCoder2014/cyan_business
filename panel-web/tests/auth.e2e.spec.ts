import { expect, test, type Page } from "@playwright/test";

const definitions = [
  {
    serviceKey: "catalog-service",
    entityKey: "products",
    title: "Products",
    tenantKey: "tenant-demo",
    siteKey: "site-commerce",
    definitionJson: JSON.stringify({
      fields: [
        { key: "title", type: "String", required: true, label: "Product title" },
        { key: "price", type: "Decimal", required: true, label: "Price" }
      ]
    })
  }
];

const storageKeys = {
  accessToken: "cyan.panel.authToken",
  refreshToken: "cyan.panel.refreshToken",
  expiresAt: "cyan.panel.authExpiresAt",
  sessionId: "cyan.panel.sessionId"
};

test("redirects to auth on protected 401 and returns after sign in", async ({ page }) => {
  await page.addInitScript((keys) => {
    window.localStorage.removeItem(keys.accessToken);
    window.localStorage.removeItem(keys.refreshToken);
    window.localStorage.removeItem(keys.expiresAt);
    window.localStorage.removeItem(keys.sessionId);
  }, storageKeys);
  await routeCaptcha(page);
  await page.route("**/api/sso/auth/login", async (route) => {
    const body = route.request().postDataJSON() as Record<string, unknown>;
    expect(body.clientId).toBe("cyan-panel");
    expect(body.username).toBe("user@cyan.local");
    expect(body.password).toBe("user123");
    expect(body.captchaAnswer).toBe("5");
    await route.fulfill({ json: tokenResponse("signed-in-access", "signed-in-refresh") });
  });

  let definitionRequests = 0;
  await page.route("**/api/platform/dynamic/catalog-service/endpoint/entities/definitions**", async (route) => {
    definitionRequests += 1;
    if (!route.request().headers().authorization) {
      await route.fulfill({ status: 401, body: "Unauthorized" });
      return;
    }
    await route.fulfill({ json: definitions });
  });

  await page.goto("/maker");
  await expect(page).toHaveURL(/\/auth\?returnTo=%2Fmaker/);

  const form = page.getByTestId("desktop-auth-form");
  await expect(form.getByPlaceholder("2 + 3 = ?")).toBeVisible();
  await form.getByLabel("Work email").fill("user@cyan.local");
  await form.getByLabel("Password").fill("user123");
  await form.getByLabel("Security answer").fill("5");
  await form.getByRole("button", { name: "Sign in" }).click();

  await expect(page).toHaveURL(/\/maker$/);
  await expect(page.getByText("Products").first()).toBeVisible();
  expect(definitionRequests).toBeGreaterThanOrEqual(2);
});

test("registers a user, logs in, and returns to the requested page", async ({ page }) => {
  await routeCaptcha(page);
  await page.route("**/api/sso/users/register", async (route) => {
    const body = route.request().postDataJSON() as Record<string, unknown>;
    expect(body.username).toBe("new-user@example.com");
    expect(body.email).toBe("new-user@example.com");
    expect(body.password).toBe("StrongPass123!");
    await route.fulfill({
      status: 201,
      json: {
        username: "new-user@example.com",
        email: "new-user@example.com",
        phoneNumber: "+15551234567",
        mfaEnabled: false,
        roles: ["user"],
        active: true
      }
    });
  });
  await page.route("**/api/sso/auth/login", async (route) => {
    const body = route.request().postDataJSON() as Record<string, unknown>;
    expect(body.username).toBe("new-user@example.com");
    expect(body.password).toBe("StrongPass123!");
    await route.fulfill({ json: tokenResponse("registered-access", "registered-refresh") });
  });
  await page.route("**/api/platform/dynamic/catalog-service/endpoint/entities/definitions**", async (route) => {
    expect(route.request().headers().authorization).toBe("Bearer registered-access");
    await route.fulfill({ json: definitions });
  });

  await page.goto("/auth?mode=register&returnTo=%2Fmaker%3Fsection%3Dschema");

  const form = page.getByTestId("desktop-auth-form");
  await form.getByLabel("Work email").fill("new-user@example.com");
  await form.getByLabel("Password").fill("StrongPass123!");
  await form.getByLabel("Workspace name").fill("Example Workspace");
  await form.getByLabel("Phone number (optional)").fill("+15551234567");
  await form.getByLabel("Security answer").fill("5");
  await form.getByRole("button", { name: "Create account" }).click();

  await expect(page).toHaveURL(/\/maker\?section=schema$/);
  await expect(page.getByText("Products").first()).toBeVisible();
});

test("refreshes an expired access token before retrying protected API calls", async ({ page }) => {
  await page.addInitScript((keys) => {
    window.localStorage.setItem(keys.accessToken, "expired-access");
    window.localStorage.setItem(keys.refreshToken, "valid-refresh");
    window.localStorage.setItem(keys.expiresAt, String(Date.now() - 10_000));
    window.localStorage.setItem(keys.sessionId, "session-old");
  }, storageKeys);

  let refreshCalled = false;
  let apiAuthorization = "";

  await page.route("**/api/sso/auth/refresh", async (route) => {
    refreshCalled = true;
    const body = route.request().postDataJSON() as Record<string, unknown>;
    expect(body.clientId).toBe("cyan-panel");
    expect(body.refreshToken).toBe("valid-refresh");
    await route.fulfill({ json: tokenResponse("refreshed-access", "rotated-refresh") });
  });
  await page.route("**/api/platform/dynamic/catalog-service/endpoint/entities/definitions**", async (route) => {
    apiAuthorization = route.request().headers().authorization ?? "";
    await route.fulfill({ json: definitions });
  });

  await page.goto("/maker");

  await expect(page.getByText("Products").first()).toBeVisible();
  await expect.poll(() => refreshCalled).toBe(true);
  await expect.poll(() => apiAuthorization).toBe("Bearer refreshed-access");
});

async function routeCaptcha(page: Page) {
  await page.route("**/api/sso/captcha/challenges**", async (route) => {
    await route.fulfill({
      json: {
        challengeId: "captcha-1",
        prompt: "2 + 3 = ?",
        expiresAtEpochSecond: Math.floor(Date.now() / 1000) + 300
      }
    });
  });
}

function tokenResponse(accessToken: string, refreshToken: string) {
  return {
    accessToken,
    refreshToken,
    tokenType: "Bearer",
    expiresIn: 3600,
    sessionId: `session-${accessToken}`
  };
}
