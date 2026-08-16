import { expect, test, type Page } from "@playwright/test";

const definitions = [
  {
    serviceKey: "bpm-service",
    entityKey: "products",
    title: "Products",
    tenantKey: "tenant-demo",
    siteKey: "site-commerce",
    definition: {
      fields: [
        { key: "title", type: "String", required: true, label: "Product title" },
        { key: "price", type: "Decimal", required: true, label: "Price" }
      ]
    }
  }
];

const storageKeys = {
  accessToken: "cyan.panel.authToken",
  refreshToken: "cyan.panel.refreshToken",
  expiresAt: "cyan.panel.authExpiresAt",
  sessionId: "cyan.panel.sessionId",
  username: "cyan.panel.username"
};
const panelBootstrap = { identity:{username:"user@cyan.local",email:"user@cyan.local",mfaEnabled:false,roles:["user"],active:true},access:{realmRoles:["tenant-admin"],realmPermissions:["panel:read","project.create","project.read","definition.read","record.read","bpm.read","settings.read"],clients:[]},tenants:[{tenantKey:"tenant-demo",displayName:"Demo workspace",status:"ACTIVE",membershipRole:"TENANT_OWNER"}],sites:[{tenantKey:"tenant-demo",siteKey:"site-commerce",name:"Commerce",status:"ACTIVE"}],activeTenantKey:"tenant-demo",activeSiteKey:"site-commerce",subscription:{tenantKey:"tenant-demo",planKey:null,status:"NONE",features:[],limits:{},providerState:"NOT_CONFIGURED"},capabilities:[{key:"dynamic-entities",enabled:true,source:"TENANT_OVERRIDE",status:"AVAILABLE",limits:{}}],featureFlags:{},services:{identity:"AVAILABLE",tenancy:"AVAILABLE",sessionScope:"AVAILABLE",sites:"AVAILABLE",billing:"NOT_CONFIGURED",capabilities:"AVAILABLE"},warnings:[]};

test("redirects to auth without a token and returns after sign in", async ({ page }) => {
  await page.addInitScript((keys) => {
    window.localStorage.removeItem(keys.accessToken);
    window.localStorage.removeItem(keys.refreshToken);
    window.localStorage.removeItem(keys.expiresAt);
    window.localStorage.removeItem(keys.sessionId);
    window.localStorage.removeItem(keys.username);
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
  await page.route("**/api/platform/dynamic/content-service/endpoint/entities/definitions**", async (route) => {
    definitionRequests += 1;
    if (!route.request().headers().authorization) {
      await route.fulfill({ status: 401, body: "Unauthorized" });
      return;
    }
    await route.fulfill({ json: definitions });
  });
  await routeBpmTemplates(page);

  await page.goto("/maker");
  await expect(page).toHaveURL(/\/auth\?returnTo=%2Fdefinitions/, { timeout: 15_000 });

  const form = page.getByTestId("desktop-auth-form");
  await expect(form.getByPlaceholder("2 + 3 = ?")).toBeVisible();
  await form.getByLabel("Work email").fill("user@cyan.local");
  await form.getByLabel("Password", { exact: true }).fill("user123");
  await form.getByLabel("Security answer").fill("5");
  await form.getByRole("button", { name: "Sign in" }).click();

  await expect(page).toHaveURL(/\/definitions$/);
  await expect(page.getByRole("heading", { name: "Definitions & Forms" })).toBeVisible();
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
  await page.route("**/api/platform/dynamic/content-service/endpoint/entities/definitions**", async (route) => {
    expect(route.request().headers().authorization).toBe("Bearer registered-access");
    await route.fulfill({ json: definitions });
  });
  await routeBpmTemplates(page);

  await page.goto("/auth?mode=register&returnTo=%2Fmaker%3Fsection%3Dschema");

  const form = page.getByTestId("desktop-auth-form");
  await expect(form.getByPlaceholder("2 + 3 = ?")).toBeVisible();
  await form.getByLabel("Work email").fill("new-user@example.com");
  await form.getByLabel("Password", { exact: true }).fill("StrongPass123!");
  await form.getByLabel("Workspace name").fill("Example Workspace");
  await form.getByLabel("Phone (optional)").fill("+15551234567");
  await form.getByLabel("Security answer").fill("5");
  await form.getByRole("button", { name: "Continue with email" }).click();

  await expect(page).toHaveURL(/\/definitions$/);
  await expect(page.getByRole("heading", { name: "Definitions & Forms" })).toBeVisible();
});

test("protects MFA code requests with captcha and presents the development code", async ({ page }) => {
  await routeCaptcha(page);
  let otpRequests = 0;
  await page.route("**/api/sso/auth/otp/send?**", async (route) => {
    otpRequests += 1;
    const url = new URL(route.request().url());
    expect(url.searchParams.get("captchaChallengeId")).toBe("captcha-1");
    expect(url.searchParams.get("captchaAnswer")).toBe("5");
    expect(route.request().postDataJSON()).toMatchObject({
      username: "user@cyan.local",
      clientId: "cyan-panel",
      purpose: "LOGIN"
    });
    await route.fulfill({ json: { codeId: "otp-1", sent: true, deliveryTarget: "user@cyan.local", devCode: "123456" } });
  });

  await page.goto("/auth");
  const form = page.getByTestId("desktop-auth-form");
  await form.getByLabel("Work email").fill("user@cyan.local");
  await form.getByLabel("Security answer").fill("5");
  await form.getByRole("button", { name: "Send code" }).click();

  await expect(form.getByText("Development login code: 123456")).toBeVisible();
  expect(otpRequests).toBe(1);
});

test("refreshes an expired access token before retrying protected API calls", async ({ page }) => {
  await page.addInitScript((keys) => {
    window.localStorage.setItem(keys.accessToken, "expired-access");
    window.localStorage.setItem(keys.refreshToken, "valid-refresh");
    window.localStorage.setItem(keys.expiresAt, String(Date.now() - 10_000));
    window.localStorage.setItem(keys.sessionId, "session-old");
    window.localStorage.setItem(keys.username, "user@cyan.local");
  }, storageKeys);

  let refreshCalls = 0;
  let apiAuthorization = "";

  await page.route("**/api/sso/auth/refresh", async (route) => {
    refreshCalls += 1;
    const body = route.request().postDataJSON() as Record<string, unknown>;
    expect(body.clientId).toBe("cyan-panel");
    expect(["valid-refresh", "rotated-refresh"]).toContain(body.refreshToken);
    await route.fulfill({ json: tokenResponse("refreshed-access", "rotated-refresh") });
  });
  await page.route("**/api/platform/dynamic/content-service/endpoint/entities/definitions**", async (route) => {
    apiAuthorization = route.request().headers().authorization ?? "";
    await route.fulfill({ json: definitions });
  });
  await routeBpmTemplates(page);

  await page.goto("/maker");

  await expect(page.getByRole("heading", { name: "Definitions & Forms" })).toBeVisible();
  await expect.poll(() => refreshCalls).toBeGreaterThan(0);
  await expect.poll(() => page.evaluate(() => localStorage.getItem("cyan.panel.authToken"))).toBe("refreshed-access");
});

async function routeCaptcha(page: Page) {
  await page.route("**/api/panel/bootstrap", (route) => route.fulfill({ json: panelBootstrap }));
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

async function routeBpmTemplates(page: Page) {
  await page.route("**/api/platform/dynamic/content-service/endpoint/entities/templates", async (route) => {
    await route.fulfill({
      json: [
        {
          serviceKey: "bpm-service",
          templateKey: "screening-intake-form",
          title: "Screening intake form"
        }
      ]
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
