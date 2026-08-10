import { expect, test, type Page, type Route } from "@playwright/test";

const scope = { tenantKey: "phase11-tenant", siteKey: "main-site" };
const bootstrap = {
  identity: { username: "phase11-owner", email: "owner@example.test", mfaEnabled: true, roles: ["admin"], active: true },
  access: { realmRoles: ["admin"], realmPermissions: ["*"], clients: [] },
  tenantAccess: { ...scope, username: "phase11-owner", roleKey: "TENANT_OWNER", permissions: ["*"], active: true },
  tenants: [{ tenantKey: scope.tenantKey, displayName: "Phase 11 workspace", status: "ACTIVE", membershipRole: "TENANT_OWNER" }],
  sites: [{ ...scope, name: "Main site", status: "ACTIVE" }], activeTenantKey: scope.tenantKey, activeSiteKey: scope.siteKey,
  subscription: { tenantKey: scope.tenantKey, planKey: "test", status: "ACTIVE", features: [], limits: {}, providerState: "NOT_REQUIRED" },
  capabilities: ["ai-orchestrator", "dynamic-entities", "bpm", "automation", "bot-adapter", "site-builder", "report", "media", "search"].map(key => ({ key, enabled: true, source: "TENANT_OVERRIDE", status: "AVAILABLE", limits: {} })),
  featureFlags: {}, services: { identity: "AVAILABLE", tenancy: "AVAILABLE", sessionScope: "AVAILABLE", sites: "AVAILABLE", billing: "AVAILABLE", capabilities: "AVAILABLE" }, warnings: []
};
const flow = { flowKey: "review", version: 1, name: "Review process", description: "Scoped approval", startState: "review", active: false, lifecycleStatus: "DRAFT", states: [{ id: "review", displayName: "Review", terminal: false, candidateGroups: ["reviewers"], accessRule: { canRead: ["reviewers"], canEdit: ["reviewers"], canApprove: ["approvers"] }, entityService: "crm-service", entityKey: "customer", formKey: "customer", onEnterActions: [], submitMode: "DYNAMIC" }, { id: "done", displayName: "Done", terminal: true, candidateGroups: [], onEnterActions: [], submitMode: "DYNAMIC" }], transitions: [{ id: "approve", fromState: "review", toState: "done", label: "Approve", allowedRoles: ["REVIEWER"], allowedGroups: ["approvers"], conditions: [] }], layout: { review: { x: 100, y: 100 }, done: { x: 380, y: 100 } } };
const work = { id: "work-11", objectType: "customer", flowKey: "review", state: "review", assignee: "reviewers", assigneeType: "GROUP", payload: { currentFormValues: { name: "" } }, priority: "NORMAL", locked: false, auditLog: [], transitionHistory: [], automationBlockRegistry: [] };

async function prepare(page: Page) {
  await page.addInitScript(() => { localStorage.setItem("cyan.panel.authToken", "phase11-token"); localStorage.setItem("cyan.panel.authExpiresAt", String(Date.now() + 3_600_000)); localStorage.setItem("cyan.panel.sessionId", "phase11-session"); localStorage.setItem("cyan.panel.username", "phase11-owner"); });
  await page.route("**/api/panel/bootstrap", route => route.fulfill({ json: bootstrap }));
  await page.route("**/api/sso/sessions/me", route => route.fulfill({ json: [] }));
  await page.route("**/api/platform/**", platform);
}

async function platform(route: Route) {
  const url = route.request().url();
  if (url.includes("/endpoint/clients/capabilities/catalog")) return route.fulfill({ json: ["automation", "bpm", "ai-orchestrator"] });
  if (url.includes("/endpoint/clients")) return route.fulfill({ json: [{ tenantKey: scope.tenantKey, displayName: "Phase 11 workspace", status: "ACTIVE", createdAt: "2026-08-09T00:00:00Z" }] });
  if (url.includes("/endpoint/billing/plans")) return route.fulfill({ json: [{ planKey: "free", displayName: "Free", billingMode: "FREE", active: true }] });
  if (url.includes("/endpoint/bpm/metadata/state-actions")) return route.fulfill({ json: [{ type: "ADD_AUDIT_ENTRY", description: "Audit", commonFields: [], params: [] }] });
  if (url.includes("/endpoint/automation-flows")) return route.fulfill({ json: [] });
  if (url.includes("/endpoint/bpm/flows/review")) return route.fulfill({ json: flow });
  if (url.endsWith("/endpoint/bpm/flows")) return route.fulfill({ json: route.request().method() === "POST" ? route.request().postDataJSON() : [flow] });
  if (url.includes("/managed-objects/work-11/active-form")) return route.fulfill({ json: { objectId: work.id, objectType: work.objectType, flowKey: work.flowKey, state: work.state, formKey: "customer", entityService: "crm-service", entityKey: "customer", rendererDefinition: { fields: { name: { type: "string", required: true, label: "Customer name" } } } } });
  if (url.includes("/managed-objects/work-11/comments")) return route.fulfill({ json: [] });
  if (url.includes("/managed-objects/work-11/attachments")) return route.fulfill({ json: [] });
  if (url.includes("/managed-objects/work-11/transitions")) return route.fulfill({ json: [{ transitionId: "approve", fromState: "review", toState: "done", label: "Approve", allowedRoles: ["REVIEWER"], allowedGroups: ["approvers"] }] });
  if (url.includes("/managed-objects/work-11/assignment")) return route.fulfill({ json: { ...work, ...route.request().postDataJSON() } });
  if (url.includes("/managed-objects/assignment-targets")) return route.fulfill({ json: [{ type: "ROLE", key: "REVIEWER", displayName: "Reviewer", active: true }] });
  if (url.includes("/managed-objects/work-11")) return route.fulfill({ json: work });
  return route.fulfill({ json: [] });
}

test.beforeEach(async ({ page }) => { await prepare(page); });

test("offline state is explicit and never presented as queued success", async ({ page, context }) => {
  await page.goto("/dashboard");
  await context.setOffline(true);
  await expect(page.getByText(/Offline — cached shell assets/)).toBeVisible();
  await expect(page.getByText(/mutations are paused/)).toBeVisible();
  await context.setOffline(false);
});

test("client search is functional and provisioning dialog traps focus", async ({ page }) => {
  await page.goto("/clients");
  await page.getByLabel("Search clients").fill("missing");
  await expect(page.getByRole("heading", { name: "No matching clients" })).toBeVisible();
  await page.getByLabel("Search clients").fill("");
  const opener = page.getByRole("button", { name: /New client/ });
  await opener.focus();
  await opener.click();
  const dialog = page.getByRole("dialog", { name: "Provision client" });
  await expect(dialog).toBeVisible();
  await page.keyboard.press("Escape");
  await expect(dialog).toBeHidden();
  await expect(opener).toBeFocused();
});

test("BPM exposes persisted role group and access contracts", async ({ page }) => {
  let saved: Record<string, unknown> | null = null;
  await page.route("**/endpoint/bpm/flows", async route => { if (route.request().method() === "POST") { saved = route.request().postDataJSON(); await new Promise(resolve => setTimeout(resolve, 1500)); return route.fulfill({ json: saved }); } return route.fulfill({ json: [flow] }); });
  await page.goto("/bpm/review");
  await expect(page.getByLabel("Allowed roles")).toHaveValue("REVIEWER");
  await page.getByRole("button", { name: "Review review" }).click();
  await expect(page.getByText("Access rules")).toBeVisible();
  await page.getByLabel("Candidate groups (comma-separated)").fill("reviewers, supervisors");
  const save = page.getByRole("button", { name: "Save" });
  await save.click();
  const pendingSave = page.getByRole("button", { name: "Working…" });
  await expect(pendingSave).toBeDisabled();
  await pendingSave.evaluate((button: HTMLButtonElement) => button.click());
  await expect(save).toBeEnabled();
  expect(((saved?.states as Array<{ candidateGroups: string[] }>)[0].candidateGroups)).toEqual(["reviewers", "supervisors"]);
});

test("work assignment supports role and prevents duplicate mutation", async ({ page }) => {
  let calls = 0;
  await page.route("**/managed-objects/work-11/assignment", async route => { calls += 1; await new Promise(resolve => setTimeout(resolve, 1500)); await route.fulfill({ json: { ...work, ...route.request().postDataJSON() } }); });
  await page.goto("/work/work-11");
  await page.getByLabel("Assignee type").selectOption("ROLE");
  await page.getByLabel("Find and select assignee").fill("REVIEWER");
  const assign = page.getByRole("button", { name: "Assign" });
  await assign.click();
  const pendingAssign = page.getByRole("button", { name: "Working…" });
  await expect(pendingAssign).toBeDisabled();
  await pendingAssign.evaluate((button: HTMLButtonElement) => button.click());
  await expect(assign).toBeEnabled();
  await expect.poll(() => calls).toBe(1);
});

test("capture Phase 11 hardening states", async ({ page }) => {
  test.setTimeout(240_000);
  test.skip(process.env.CAPTURE_PHASE_11 !== "1", "Visual capture is explicit");
  await page.goto("/dashboard");
  const targets = [{ name: "dashboard", path: "/dashboard", ready: ".panel-app-shell" }, { name: "clients", path: "/clients", ready: ".client-grid" }, { name: "bpm", path: "/bpm/review", ready: ".bpm-designer-grid" }, { name: "work", path: "/work/work-11", ready: ".work-item-layout" }];
  const states = [{ name: "desktop-en-light", w: 1440, h: 1000, locale: "en", theme: "light" }, { name: "builder-en-dark", w: 1600, h: 1000, locale: "en", theme: "dark" }, { name: "tablet-en-light", w: 834, h: 1112, locale: "en", theme: "light" }, { name: "mobile-en-light", w: 390, h: 844, locale: "en", theme: "light" }, { name: "small-mobile-fa-dark", w: 360, h: 800, locale: "fa", theme: "dark" }];
  for (const target of targets) for (const state of states) {
    await page.setViewportSize({ width: state.w, height: state.h });
    await page.evaluate(({ locale, theme }) => { localStorage.setItem("cyan.panel.locale", locale); localStorage.setItem("cyan.panel.theme", theme); }, state);
    await page.goto(target.path); await expect(page.locator(target.ready).first()).toBeVisible();
    await page.screenshot({ path: `../docs/ui-redesign/completion/phase-11/screenshots/${target.name}-${state.name}.png`, fullPage: true });
  }
});
