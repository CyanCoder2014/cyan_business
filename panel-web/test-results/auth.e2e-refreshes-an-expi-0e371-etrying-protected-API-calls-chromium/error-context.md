# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: auth.e2e.spec.ts >> refreshes an expired access token before retrying protected API calls
- Location: tests/auth.e2e.spec.ts:112:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByText('Products').first()
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByText('Products').first()

```

```yaml
- complementary:
  - link "C CyanAI-native app platform":
    - /url: /
    - text: C
    - strong: Cyan
    - text: AI-native app platform
  - navigation "Primary navigation":
    - link "Dashboard":
      - /url: /
    - link "AI Studio":
      - /url: /projects/new
    - link "Blueprints":
      - /url: /projects
    - link "Maker":
      - /url: /maker
    - link "Data":
      - /url: /data
    - link "Flow Builder":
      - /url: /flows
    - link "Client Apps/Bots":
      - /url: /integrations
    - link "Bot Experience":
      - /url: /bot
    - link "Site Builder":
      - /url: /site-builder
    - link "Media":
      - /url: /search
    - link "Analytics":
      - /url: /automation
    - link "Settings":
      - /url: /iam
  - paragraph: Pro plan
  - text: Unlimited projects, premium modules, and priority support.
  - button "Manage plan"
  - text: AC
  - strong: Acme Corp
  - text: Workspace
- banner:
  - text: Workspace
  - strong: Acme Corp
  - text: Site
  - strong: acme.cyan.app
  - button "فا"
  - button "☾"
  - text: AM
  - strong: Ali Mohammadi
  - text: Admin
- main:
  - paragraph: AI-native business platform
  - heading "Maker - Definitions" [level=1]
  - paragraph: Design entities, fields, relations, validations, and permissions from the structured runtime contract.
  - textbox "Search entities..."
  - button "Create from template"
  - button "Publish schema"
  - text: Template
  - combobox "Template"
  - strong: No definitions returned by backend
  - text: This list fills after definitions are created in the service. FieldsValidationsRelationsPermissions
  - strong: No selection
  - button "Reorder"
  - button "Bulk actions"
  - table:
    - rowgroup:
      - row "Field name Type Required Description":
        - columnheader "Field name"
        - columnheader "Type"
        - columnheader "Required"
        - columnheader "Description"
    - rowgroup:
      - row "No fields are available for this definition.":
        - cell "No fields are available for this definition."
  - text: Definition JSON
  - textbox
  - complementary:
    - heading "API & DSL summary" [level=3]
    - text: Empty
    - strong: No API summary data available
    - text: Select a template and create the definition from backend templates.
    - heading "Schema map" [level=3]
    - strong: —
    - text: 0 fields
    - strong: "0"
    - text: definitions
  - button "←"
  - strong: Maker
  - text: No entity loaded
  - button "…"
  - text: FieldsValidationsRelations
  - strong: Scope
  - text: Define where this entity is available. Organization
  - strong: API identifier
  - text: —
  - button "Publish schema" [disabled]
  - navigation "Mobile navigation":
    - link "⌘Dashboard":
      - /url: /
    - link "✦AI Studio":
      - /url: /projects/new
    - link "◍Data":
      - /url: /data
    - link "⌇Flow Builder":
      - /url: /flows
    - link "⬡Client Apps/Bots":
      - /url: /integrations
```

# Test source

```ts
  37  |     expect(body.username).toBe("user@cyan.local");
  38  |     expect(body.password).toBe("user123");
  39  |     expect(body.captchaAnswer).toBe("5");
  40  |     await route.fulfill({ json: tokenResponse("signed-in-access", "signed-in-refresh") });
  41  |   });
  42  | 
  43  |   let definitionRequests = 0;
  44  |   await page.route("**/api/platform/dynamic/catalog-service/endpoint/entities/definitions**", async (route) => {
  45  |     definitionRequests += 1;
  46  |     if (!route.request().headers().authorization) {
  47  |       await route.fulfill({ status: 401, body: "Unauthorized" });
  48  |       return;
  49  |     }
  50  |     await route.fulfill({ json: definitions });
  51  |   });
  52  | 
  53  |   await page.goto("/maker");
  54  |   await expect(page).toHaveURL(/\/auth\?returnTo=%2Fmaker/);
  55  | 
  56  |   const form = page.getByTestId("desktop-auth-form");
  57  |   await expect(form.getByPlaceholder("2 + 3 = ?")).toBeVisible();
  58  |   await form.getByLabel("Work email").fill("user@cyan.local");
  59  |   await form.getByLabel("Password").fill("user123");
  60  |   await form.getByLabel("Security answer").fill("5");
  61  |   await form.getByRole("button", { name: "Sign in" }).click();
  62  | 
  63  |   await expect(page).toHaveURL(/\/maker$/);
  64  |   await expect(page.getByText("Products").first()).toBeVisible();
  65  |   expect(definitionRequests).toBeGreaterThanOrEqual(2);
  66  | });
  67  | 
  68  | test("registers a user, logs in, and returns to the requested page", async ({ page }) => {
  69  |   await routeCaptcha(page);
  70  |   await page.route("**/api/sso/users/register", async (route) => {
  71  |     const body = route.request().postDataJSON() as Record<string, unknown>;
  72  |     expect(body.username).toBe("new-user@example.com");
  73  |     expect(body.email).toBe("new-user@example.com");
  74  |     expect(body.password).toBe("StrongPass123!");
  75  |     await route.fulfill({
  76  |       status: 201,
  77  |       json: {
  78  |         username: "new-user@example.com",
  79  |         email: "new-user@example.com",
  80  |         phoneNumber: "+15551234567",
  81  |         mfaEnabled: false,
  82  |         roles: ["user"],
  83  |         active: true
  84  |       }
  85  |     });
  86  |   });
  87  |   await page.route("**/api/sso/auth/login", async (route) => {
  88  |     const body = route.request().postDataJSON() as Record<string, unknown>;
  89  |     expect(body.username).toBe("new-user@example.com");
  90  |     expect(body.password).toBe("StrongPass123!");
  91  |     await route.fulfill({ json: tokenResponse("registered-access", "registered-refresh") });
  92  |   });
  93  |   await page.route("**/api/platform/dynamic/catalog-service/endpoint/entities/definitions**", async (route) => {
  94  |     expect(route.request().headers().authorization).toBe("Bearer registered-access");
  95  |     await route.fulfill({ json: definitions });
  96  |   });
  97  | 
  98  |   await page.goto("/auth?mode=register&returnTo=%2Fmaker%3Fsection%3Dschema");
  99  | 
  100 |   const form = page.getByTestId("desktop-auth-form");
  101 |   await form.getByLabel("Work email").fill("new-user@example.com");
  102 |   await form.getByLabel("Password").fill("StrongPass123!");
  103 |   await form.getByLabel("Workspace name").fill("Example Workspace");
  104 |   await form.getByLabel("Phone number (optional)").fill("+15551234567");
  105 |   await form.getByLabel("Security answer").fill("5");
  106 |   await form.getByRole("button", { name: "Create account" }).click();
  107 | 
  108 |   await expect(page).toHaveURL(/\/maker\?section=schema$/);
  109 |   await expect(page.getByText("Products").first()).toBeVisible();
  110 | });
  111 | 
  112 | test("refreshes an expired access token before retrying protected API calls", async ({ page }) => {
  113 |   await page.addInitScript((keys) => {
  114 |     window.localStorage.setItem(keys.accessToken, "expired-access");
  115 |     window.localStorage.setItem(keys.refreshToken, "valid-refresh");
  116 |     window.localStorage.setItem(keys.expiresAt, String(Date.now() - 10_000));
  117 |     window.localStorage.setItem(keys.sessionId, "session-old");
  118 |   }, storageKeys);
  119 | 
  120 |   let refreshCalled = false;
  121 |   let apiAuthorization = "";
  122 | 
  123 |   await page.route("**/api/sso/auth/refresh", async (route) => {
  124 |     refreshCalled = true;
  125 |     const body = route.request().postDataJSON() as Record<string, unknown>;
  126 |     expect(body.clientId).toBe("cyan-panel");
  127 |     expect(body.refreshToken).toBe("valid-refresh");
  128 |     await route.fulfill({ json: tokenResponse("refreshed-access", "rotated-refresh") });
  129 |   });
  130 |   await page.route("**/api/platform/dynamic/catalog-service/endpoint/entities/definitions**", async (route) => {
  131 |     apiAuthorization = route.request().headers().authorization ?? "";
  132 |     await route.fulfill({ json: definitions });
  133 |   });
  134 | 
  135 |   await page.goto("/maker");
  136 | 
> 137 |   await expect(page.getByText("Products").first()).toBeVisible();
      |                                                    ^ Error: expect(locator).toBeVisible() failed
  138 |   await expect.poll(() => refreshCalled).toBe(true);
  139 |   await expect.poll(() => apiAuthorization).toBe("Bearer refreshed-access");
  140 | });
  141 | 
  142 | async function routeCaptcha(page: Page) {
  143 |   await page.route("**/api/sso/captcha/challenges**", async (route) => {
  144 |     await route.fulfill({
  145 |       json: {
  146 |         challengeId: "captcha-1",
  147 |         prompt: "2 + 3 = ?",
  148 |         expiresAtEpochSecond: Math.floor(Date.now() / 1000) + 300
  149 |       }
  150 |     });
  151 |   });
  152 | }
  153 | 
  154 | function tokenResponse(accessToken: string, refreshToken: string) {
  155 |   return {
  156 |     accessToken,
  157 |     refreshToken,
  158 |     tokenType: "Bearer",
  159 |     expiresIn: 3600,
  160 |     sessionId: `session-${accessToken}`
  161 |   };
  162 | }
  163 | 
```