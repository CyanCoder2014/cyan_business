import { expect, test } from "@playwright/test";

test("live admin signs in, navigates Phase 1-7, and publishes a CRM automation project", async ({ page }) => {
  test.skip(process.env.LIVE_PHASES_1_7 !== "1", "Live service mutation is explicit");
  test.setTimeout(360_000);
  page.on("dialog", (dialog) => dialog.accept());

  await page.goto("/auth");
  const form = page.getByTestId("desktop-auth-form");
  await form.getByLabel("Work email").fill("admin@cyan.local");
  await form.getByLabel("Password", { exact: true }).fill("admin123");
  await form.getByRole("button", { name: "Send code" }).click();
  const otpMessage = form.getByText(/Development login code:/);
  await expect(otpMessage).toBeVisible();
  const otpCode = (await otpMessage.textContent())?.match(/\d{6}/)?.[0];
  expect(otpCode).toBeTruthy();
  await form.getByLabel("Two-factor login code").fill(otpCode!);

  const securityAnswer = form.getByLabel("Security answer");
  const challenge = await securityAnswer.getAttribute("placeholder");
  const values = challenge?.match(/\d+/g)?.map(Number) ?? [];
  expect(values).toHaveLength(2);
  await securityAnswer.fill(String(values[0] + values[1]));
  await form.getByRole("button", { name: "Sign in" }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByText("The persisted session scope could not be loaded.")).toHaveCount(0);

  if (await page.getByRole("heading", { name: "Create your first workspace" }).isVisible().catch(() => false)) {
    const workspaceKey = `cyan-live-${Date.now()}`;
    await page.getByLabel("Workspace name").fill("Cyan live verification");
    await page.getByLabel("Workspace key").fill(workspaceKey);
    const createWorkspace = page.getByRole("button", { name: "Create workspace" });
    await createWorkspace.click();
    await expect(page.getByRole("heading", { name: "Choose an available plan" })).toBeVisible({ timeout: 30_000 });
  }
  const workspaceSelect = page.getByLabel("Select workspace");
  if (await workspaceSelect.isVisible().catch(() => false) && await workspaceSelect.inputValue() === "") {
    const workspaceValues = await workspaceSelect.locator("option").evaluateAll((options) =>
      options.map((option) => (option as HTMLOptionElement).value).filter(Boolean)
    );
    if (workspaceValues.length) {
      await workspaceSelect.selectOption(workspaceValues.at(-1)!);
      await expect(workspaceSelect).toHaveValue(workspaceValues.at(-1)!, { timeout: 30_000 });
    }
  }
  if (await page.getByText("No plan has been published by the platform administrator.").isVisible().catch(() => false)) {
    await page.evaluate(async () => {
      const token = localStorage.getItem("cyan.panel.authToken") ?? "";
      const response = await fetch("/api/platform/service/billing-service/endpoint/billing/plans", {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
        body: JSON.stringify({ planKey:"local-platform",displayName:"Local platform",description:"Local end-to-end platform verification",billingMode:"FREE",active:true,features:["ai-orchestrator","dynamic-entities","automation","bpm","site-builder"],limits:{} })
      });
      if (!response.ok) throw new Error(`Plan configuration failed (${response.status})`);
    });
    await page.reload();
  }
  if (await page.getByRole("heading", { name: "Choose an available plan" }).isVisible().catch(() => false)) {
    await page.getByRole("button", { name: "Activate" }).first().click();
    await expect(page.getByRole("link", { name: "AI Studio", exact: true })).toBeVisible({ timeout: 30_000 });
  }
  if (!await page.getByRole("link", { name: "BPM", exact: true }).isVisible().catch(() => false)) {
    const tenantKey = await workspaceSelect.inputValue();
    expect(tenantKey).toBeTruthy();
    await page.evaluate(async ({ tenantKey }) => {
      const token = localStorage.getItem("cyan.panel.authToken") ?? "";
      const headers = { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" };
      const plan = await fetch("/api/platform/service/billing-service/endpoint/billing/plans", {
        method: "POST", headers,
        body: JSON.stringify({ planKey:"local-platform",displayName:"Local platform",description:"Local end-to-end platform verification",billingMode:"FREE",active:true,features:["ai-orchestrator","dynamic-entities","automation","bpm","site-builder"],limits:{} })
      });
      if (!plan.ok) throw new Error(`Plan configuration failed (${plan.status})`);
      const subscription = await fetch(`/api/platform/service/billing-service/endpoint/billing/tenants/${encodeURIComponent(tenantKey)}/subscription/change`, {
        method: "POST", headers: { ...headers, "Idempotency-Key": crypto.randomUUID() }, body: JSON.stringify({ planKey:"local-platform" })
      });
      if (!subscription.ok) throw new Error(`Plan activation failed (${subscription.status})`);
    }, { tenantKey });
    await page.reload();
    await expect(page.getByRole("link", { name: "BPM", exact: true })).toBeVisible({ timeout: 30_000 });
    await expect(page.getByRole("link", { name: "Automation", exact: true })).toBeVisible({ timeout: 30_000 });
  }

  const destinations = [
    ["Home", "/dashboard"],
    ["AI Studio", "/ai"],
    ["Projects", "/projects"],
    ["Definitions", "/definitions"],
    ["Data", "/data"],
    ["BPM", "/bpm"],
    ["Automation", "/automations"]
  ] as const;
  for (const [name, path] of destinations) {
    await page.getByRole("link", { name, exact: true }).click();
    await expect(page).toHaveURL(new RegExp(`${path}$`));
    await expect(page.locator("main")).toBeVisible();
  }
  await page.goto("/work");
  await expect(page.getByRole("heading", { name: "Work Queue" })).toBeVisible();

  await page.goto("/ai");
  await expect(page.getByRole("heading", { name: "AI Studio" })).toBeVisible();
  await page.getByRole("button", { name: "New conversation" }).click();
  await expect(page.getByRole("heading", { name: "Conversation is empty" })).toBeVisible({ timeout: 30_000 });
  const prompt = page.getByPlaceholder("What would you like to build?");
  await prompt.fill("Build a CRM project with lead and contact data plus a native automation flow. Use admin@cyan.local for required notifications and include an AI operation that enriches lead data. Do not invent providers or credentials.");
  const send = page.getByRole("button", { name: "Send" });
  await send.click();
  const openProject = page.getByRole("link", { name: "Open project" });
  await expect(openProject).toBeVisible({ timeout: 60_000 });
  const businessName = page.getByLabel("Answer: What business name should be used?");
  if (await businessName.isVisible().catch(() => false)) {
    await businessName.fill("Cyan CRM Operations");
    const submitAnswers = page.getByRole("button", { name: "Submit answers" });
    await submitAnswers.click();
    await expect(businessName).toHaveCount(0, { timeout: 30_000 });
  }
  await openProject.click();

  await page.getByRole("button", { name: "Plan" }).click();
  await expect(page.getByRole("tab", { name: "Provisioning" })).toHaveAttribute("aria-selected", "true");
  await expect(page.locator(".run-timeline article").first()).toBeVisible({ timeout: 60_000 });
  const apply = page.getByRole("button", { name: "Apply" });
  await expect(apply).toBeEnabled({ timeout: 30_000 });
  await apply.click();
  await expect(page.locator(".run-timeline article").first()).toBeVisible({ timeout: 60_000 });
  const successfulRun = page.locator(".run-timeline article").filter({ has: page.getByText("SUCCESS", { exact: true }) }).first();
  await expect(successfulRun).toBeVisible({ timeout: 60_000 });
  await successfulRun.getByRole("button", { name: "Create release" }).click();
  await expect(page.getByRole("tab", { name: "Releases" })).toHaveAttribute("aria-selected", "true");
  const publish = page.getByRole("button", { name: "Publish" }).first();
  await expect(publish).toBeEnabled();
  await publish.click();
  await expect(page.getByText("ACTIVE", { exact: true }).last()).toBeVisible();
  await page.screenshot({ path: "../docs/ui-redesign/completion/phase-7/screenshots/live-admin-crm-automation-published.png", fullPage: true });
});
