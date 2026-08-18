import { expect, test, type Page } from "@playwright/test";
import { resolveAccessState } from "@/components/access-gates";
import { grantsPermission } from "@/components/scope-access-provider";

const bootstrap = {
  identity: { username: "reviewer@cyan.local", email: "reviewer@cyan.local", mfaEnabled: true, roles: ["user"], active: true },
  access: { realmRoles: ["tenant-admin"], realmPermissions: ["panel:read", "project.create", "project.read", "definition.read", "record.read", "bpm.read", "settings.read"], clients: [{ clientId: "cyan-panel", clientRoles: ["builder"], clientPermissions: ["panel:read"] }] },
  tenants: [{ tenantKey: "north-star", displayName: "North Star", status: "ACTIVE", membershipRole: "TENANT_OWNER" }],
  sites: [{ tenantKey: "north-star", siteKey: "main-store", name: "Main Store", status: "ACTIVE" }],
  activeTenantKey: "north-star",
  activeSiteKey: "main-store",
  subscription: { tenantKey: "north-star", planKey: null, status: "NONE", features: [], limits: {}, providerState: "NOT_CONFIGURED" },
  capabilities: [
    { key: "ai-orchestrator", enabled: true, source: "TENANT_OVERRIDE", status: "AVAILABLE", limits: {} },
    { key: "dynamic-entities", enabled: true, source: "TENANT_OVERRIDE", status: "AVAILABLE", limits: {} },
    { key: "bpm", enabled: true, source: "TENANT_OVERRIDE", status: "AVAILABLE", limits: {} },
    { key: "automation", enabled: false, source: "PLAN", status: "UNAVAILABLE", limits: {}, reason: "Required service is not registered" }
  ],
  featureFlags: {}, services: { identity: "AVAILABLE", tenancy: "AVAILABLE", sessionScope: "AVAILABLE", sites: "AVAILABLE", billing: "NOT_CONFIGURED", capabilities: "AVAILABLE" }, warnings: []
};

test("access resolver keeps denial reasons distinct", () => {
  expect(resolveAccessState({ authenticated: true, permission: false })).toBe("permission-denied");
  expect(resolveAccessState({ authenticated: true, permission: true, plan: false })).toBe("plan-locked");
  expect(resolveAccessState({ authenticated: true, permission: true, plan: true, capability: true, service: false })).toBe("service-unavailable");
});

test("legacy client grants resolve to the granular panel navigation contract", () => {
  const builder = new Set(["builder:*", "panel:read"]);
  expect(grantsPermission(builder, "bpm.read")).toBe(true);
  expect(grantsPermission(builder, "automation.read")).toBe(true);
  expect(grantsPermission(builder, "definition.manage")).toBe(true);
  expect(grantsPermission(builder, "billing.manage")).toBe(false);

  const operator = new Set(["operations:*"]);
  expect(grantsPermission(operator, "report.read")).toBe(true);
  expect(grantsPermission(operator, "automation.execute")).toBe(true);
  expect(grantsPermission(operator, "project.create")).toBe(false);
});

async function prepare(page: Page, locale: "en" | "fa" = "en", theme: "light" | "dark" = "light") {
  await page.addInitScript(({ locale, theme }) => {
    localStorage.setItem("cyan.panel.authToken", "phase-one-token");
    localStorage.setItem("cyan.panel.authExpiresAt", String(Date.now() + 3_600_000));
    localStorage.setItem("cyan.panel.sessionId", "phase-one-session");
    localStorage.setItem("cyan.panel.username", "reviewer@cyan.local");
    localStorage.setItem("cyan.panel.locale", locale);
    localStorage.setItem("cyan.panel.theme", theme);
  }, { locale, theme });
  await page.route("**/api/panel/bootstrap", (route) => route.fulfill({ json: bootstrap }));
  await page.route("**/api/platform/**", (route) => route.fulfill({ json: [] }));
}

test("renders a real scoped shell and persists scope changes", async ({ page }) => {
  await prepare(page);
  let submitted: unknown;
  await page.route("**/api/panel/scope", async (route) => { submitted = route.request().postDataJSON(); await route.fulfill({ json: { sessionId: "phase-one-session", tenantKey: "north-star", siteKey: null } }); });
  await page.goto("/");
  await expect(page.getByRole("navigation", { name: "Primary navigation" })).toBeVisible();
  await expect(page.getByLabel("Select workspace")).toHaveValue("north-star");
  await expect(page.getByLabel("Select site")).toHaveValue("main-store");
  await expect(page.getByText("Automation", { exact: true }).locator("..")).toHaveAttribute("aria-disabled", "true");
  await page.getByLabel("Select site").selectOption("");
  await expect.poll(() => submitted).toEqual({ tenantKey: "north-star", siteKey: null });
});

test("mobile shell exposes five destinations and accessible sheets", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await prepare(page);
  await page.goto("/");
  const nav = page.getByRole("navigation", { name: "Mobile navigation" });
  await expect(nav).toBeVisible();
  await expect(nav.locator("a,button")).toHaveCount(5);
  await nav.getByRole("button", { name: /Build/ }).click();
  await expect(page.getByRole("dialog", { name: "Build navigation" })).toBeVisible();
});

test("shell renders across required breakpoints and preferences survive reload", async ({ page }) => {
  await page.addInitScript(() => { localStorage.setItem("cyan.panel.authToken", "phase-one-token"); localStorage.setItem("cyan.panel.authExpiresAt", String(Date.now() + 3_600_000)); localStorage.setItem("cyan.panel.sessionId", "phase-one-session"); localStorage.setItem("cyan.panel.username", "reviewer@cyan.local"); });
  await page.route("**/api/panel/bootstrap", (route) => route.fulfill({ json: bootstrap }));
  await page.route("**/api/platform/**", (route) => route.fulfill({ json: [] }));
  for (const width of [1440, 1024, 834, 390, 360]) {
    await page.setViewportSize({ width, height: width <= 390 ? 844 : 900 });
    await page.goto("/");
    await expect(page.locator(".panel-app-shell")).toBeVisible();
  }
  await page.evaluate(() => { localStorage.setItem("cyan.panel.locale", "fa"); localStorage.setItem("cyan.panel.theme", "dark"); });
  await page.reload();
  await expect(page.locator("html")).toHaveAttribute("dir", "rtl");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
});

test("Farsi shell mirrors direction and preserves readable labels", async ({ page }) => {
  await prepare(page, "fa", "light");
  await page.goto("/");
  await expect(page.locator("html")).toHaveAttribute("dir", "rtl");
  await expect(page.getByRole("navigation", { name: "ناوبری اصلی" })).toBeVisible();
  await expect(page.getByLabel("انتخاب فضای کار")).toHaveValue("north-star");
});

test("captures Phase 1 visual states", async ({ page }) => {
  test.skip(process.env.CAPTURE_PHASE1 !== "1", "Visual capture is run explicitly for the completion report.");
  const states = [
    { name: "desktop-en-light", width: 1440, height: 1000, locale: "en" as const, theme: "light" as const },
    { name: "desktop-en-dark", width: 1440, height: 1000, locale: "en" as const, theme: "dark" as const },
    { name: "tablet-en-light", width: 834, height: 1112, locale: "en" as const, theme: "light" as const },
    { name: "mobile-en-light", width: 390, height: 844, locale: "en" as const, theme: "light" as const },
    { name: "mobile-en-dark", width: 390, height: 844, locale: "en" as const, theme: "dark" as const },
    { name: "desktop-fa-rtl-light", width: 1440, height: 1000, locale: "fa" as const, theme: "light" as const },
    { name: "mobile-fa-rtl-light", width: 390, height: 844, locale: "fa" as const, theme: "light" as const }
  ];
  await page.route("**/api/panel/bootstrap", (route) => route.fulfill({ json: bootstrap }));
  await page.route("**/api/platform/**", (route) => route.fulfill({ json: [] }));
  for (const state of states) {
    await page.setViewportSize({ width: state.width, height: state.height });
    await page.addInitScript(({ locale, theme }) => {
      localStorage.setItem("cyan.panel.authToken", "phase-one-token"); localStorage.setItem("cyan.panel.authExpiresAt", String(Date.now() + 3_600_000)); localStorage.setItem("cyan.panel.sessionId", "phase-one-session"); localStorage.setItem("cyan.panel.username", "reviewer@cyan.local"); localStorage.setItem("cyan.panel.locale", locale); localStorage.setItem("cyan.panel.theme", theme);
    }, { locale: state.locale, theme: state.theme });
    await page.goto("/");
    await expect(page.locator(".panel-app-shell")).toBeVisible();
    await page.screenshot({ path: `../docs/ui-redesign/completion/phase-1/screenshots/${state.name}.png`, fullPage: false });
  }
});
