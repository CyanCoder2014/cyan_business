import { expect, test, type Page, type Route } from "@playwright/test";

const scope = { tenantKey: "result-tenant", siteKey: "main-site" };
const bootstrap = {
  identity: { username: "member", roles: ["user"], active: true },
  access: { realmRoles: ["realm-user"], realmPermissions: ["panel:read"], clients: [] },
  tenantAccess: { ...scope, username: "member", roleKey: "MEMBER", permissions: ["panel:read", "bpm.read", "record.read", "site.read"], active: true },
  tenants: [{ tenantKey: scope.tenantKey, displayName: "Result workspace", status: "ACTIVE", membershipRole: "MEMBER" }],
  sites: [{ ...scope, name: "Main site", status: "ACTIVE" }], activeTenantKey: scope.tenantKey, activeSiteKey: scope.siteKey,
  subscription: { tenantKey: scope.tenantKey, planKey: "test", status: "ACTIVE", features: [], limits: {}, providerState: "NOT_REQUIRED" },
  capabilities: ["dynamic-entities", "bpm", "site-builder"].map(key => ({ key, enabled: true, source: "PLAN", status: "AVAILABLE", limits: {} })),
  featureFlags: {}, services: { identity: "AVAILABLE", tenancy: "AVAILABLE", sessionScope: "AVAILABLE", billing: "AVAILABLE", capabilities: "AVAILABLE" }, warnings: []
};
const view = { slug: "staff-request", title: "Staff request", description: "Private request form", visibility: "AUTHENTICATED", serviceKey: "crm-service", entityKey: "request", definition: { fields: { email: { type: "email", validations: [{ validation: "REQUIRED" }] }, details: { type: "string" } } } };

async function prepare(page: Page) {
  await page.addInitScript(() => { localStorage.setItem("cyan.panel.authToken", "result-token"); localStorage.setItem("cyan.panel.authExpiresAt", String(Date.now() + 3_600_000)); localStorage.setItem("cyan.panel.sessionId", "result-session"); localStorage.setItem("cyan.panel.username", "member"); });
  await page.route("**/api/panel/bootstrap", route => route.fulfill({ json: bootstrap }));
  await page.route("**/api/platform/**", platform);
}
async function platform(route: Route) {
  const url = route.request().url();
  if (url.includes("/endpoint/forms/staff-request/submissions")) return route.fulfill({ json: { submissionKey: "form-result", status: "ACCEPTED" }, status: 201 });
  if (url.includes("/endpoint/forms/staff-request")) return route.fulfill({ json: view });
  if (url.endsWith("/endpoint/forms")) return route.fulfill({ json: [{ ...view, tenantKey: scope.tenantKey, siteKey: scope.siteKey, status: "PUBLISHED", createdAt: "2026-08-18T00:00:00Z", updatedAt: "2026-08-18T00:00:00Z" }] });
  if (url.includes("site-route")) return route.fulfill({ json: [{ recordKey: "home", data: { routeKey: "home", path: "/", publicationStatus: "PUBLISHED", seo: { title: "Home" } } }] });
  if (url.includes("managed-objects/assigned-to-me")) return route.fulfill({ json: [] });
  if (url.includes("notifications/inbox/unread-count")) return route.fulfill({ json: { unreadCount: 0 } });
  return route.fulfill({ json: [] });
}

test("dashboard exposes the cartable form data BPM and site result destinations", async ({ page }) => {
  await prepare(page); await page.goto("/dashboard");
  const results = page.locator(".dashboard-result-links");
  await expect(results.getByRole("link", { name: /My cartable/ })).toHaveAttribute("href", "/work");
  await expect(results.getByRole("link", { name: /My forms/ })).toHaveAttribute("href", "/forms");
  await expect(results.getByRole("link", { name: /Entity data & forms/ })).toHaveAttribute("href", "/data");
  await expect(results.getByRole("link", { name: /BPM designer/ })).toHaveAttribute("href", "/bpm");
  await expect(results.getByRole("link", { name: /Sites & previews/ })).toHaveAttribute("href", "/sites");
});

test("a signed-in member opens and submits a private workspace form only once", async ({ page }) => {
  await prepare(page); let calls = 0;
  await page.route("**/endpoint/forms/staff-request/submissions", async route => { calls += 1; await new Promise(resolve => setTimeout(resolve, 600)); await route.fulfill({ json: { submissionKey: "form-result", status: "ACCEPTED" }, status: 201 }); });
  await page.goto("/forms/staff-request"); await page.getByLabel(/Email/).fill("member@example.test"); await page.getByLabel(/Details/).fill("Please review");
  const submit = page.getByRole("button", { name: "Submit form" }); await submit.click();
  await expect(page.getByRole("button", { name: "Submitting…" })).toBeDisabled();
  await page.getByRole("button", { name: "Submitting…" }).evaluate((button: HTMLButtonElement) => button.click());
  await expect.poll(() => calls).toBe(1);
});

test("a public form works without a panel session", async ({ page }) => {
  await page.route("**/api/public/forms/contact-us", route => route.fulfill({ json: { ...view, slug: "contact-us", title: "Contact us", visibility: "PUBLIC" } }));
  await page.route("**/api/public/forms/contact-us/submissions", route => route.fulfill({ json: { submissionKey: "form-public", status: "ACCEPTED" }, status: 201 }));
  await page.goto("/f/contact-us"); await expect(page.getByRole("heading", { name: "Contact us" })).toBeVisible(); await page.getByLabel(/Email/).fill("visitor@example.test"); await page.getByRole("button", { name: "Submit form" }).click(); await expect(page.getByText("Form submitted")).toBeVisible();
});

test("site result page exposes a Cyan-hosted URL before custom DNS", async ({ page }) => {
  await prepare(page); await page.goto("/sites/main-site/published");
  const link = page.getByRole("link", { name: "View page" });
  await expect(link).toHaveAttribute("href", "/s/result-tenant/main-site");
});

test("capture result destinations for visual review", async ({ page }) => {
  test.skip(process.env.CAPTURE_RESULTS !== "1", "Visual capture is explicit");
  await prepare(page);
  await page.goto("/dashboard");
  for (const state of [{ name: "desktop-light", width: 1440, height: 1000, locale: "en", theme: "light" }, { name: "mobile-dark", width: 390, height: 844, locale: "en", theme: "dark" }, { name: "mobile-fa-rtl", width: 390, height: 844, locale: "fa", theme: "light" }]) {
    await page.setViewportSize({ width: state.width, height: state.height });
    await page.evaluate(({ locale, theme }) => { localStorage.setItem("cyan.panel.locale", locale); localStorage.setItem("cyan.panel.theme", theme); }, state);
    for (const target of [{ name: "dashboard", path: "/dashboard" }, { name: "forms", path: "/forms" }, { name: "private-form", path: "/forms/staff-request" }, { name: "website", path: "/sites/main-site/published" }]) {
      await page.goto(target.path); await page.screenshot({ path: `/tmp/cyan-result-pages/${target.name}-${state.name}.png`, fullPage: true });
    }
  }
  await page.route("**/api/public/forms/contact-us", route => route.fulfill({ json: { ...view, slug: "contact-us", title: "Contact us", visibility: "PUBLIC" } }));
  await page.setViewportSize({ width: 390, height: 844 }); await page.goto("/f/contact-us"); await page.screenshot({ path: "/tmp/cyan-result-pages/public-form-mobile.png", fullPage: true });
});
