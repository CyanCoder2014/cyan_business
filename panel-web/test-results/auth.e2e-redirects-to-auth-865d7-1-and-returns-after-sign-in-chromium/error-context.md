# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: auth.e2e.spec.ts >> redirects to auth on protected 401 and returns after sign in
- Location: tests/auth.e2e.spec.ts:26:5

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: page.goto: Test timeout of 30000ms exceeded.
Call log:
  - navigating to "http://127.0.0.1:3100/maker", waiting until "load"

```

# Page snapshot

```yaml
- generic [ref=e2]:
  - complementary [ref=e3]:
    - link "C CyanAI-native app platform" [ref=e4] [cursor=pointer]:
      - /url: /
      - generic [ref=e5]: C
      - generic [ref=e6]:
        - strong [ref=e7]: Cyan
        - text: AI-native app platform
    - navigation "Primary navigation" [ref=e8]:
      - link "Dashboard" [ref=e9] [cursor=pointer]:
        - /url: /
        - text: ⌘Dashboard
      - link "AI Studio" [ref=e10] [cursor=pointer]:
        - /url: /projects/new
        - text: ✦AI Studio
      - link "Blueprints" [ref=e11] [cursor=pointer]:
        - /url: /projects
        - text: ▤Blueprints
      - link "Maker" [ref=e12] [cursor=pointer]:
        - /url: /maker
        - text: ✎Maker
      - link "Data" [ref=e13] [cursor=pointer]:
        - /url: /data
        - text: ◍Data
      - link "Flow Builder" [ref=e14] [cursor=pointer]:
        - /url: /flows
        - text: ⌇Flow Builder
      - link "Client Apps/Bots" [ref=e15] [cursor=pointer]:
        - /url: /integrations
        - text: ⬡Client Apps/Bots
      - link "Bot Experience" [ref=e16] [cursor=pointer]:
        - /url: /bot
        - text: ☻Bot Experience
      - link "Site Builder" [ref=e17] [cursor=pointer]:
        - /url: /site-builder
        - text: ▣Site Builder
      - link "Media" [ref=e18] [cursor=pointer]:
        - /url: /search
        - text: ⌕Media
      - link "Analytics" [ref=e19] [cursor=pointer]:
        - /url: /automation
        - text: ◔Analytics
      - link "Settings" [ref=e20] [cursor=pointer]:
        - /url: /iam
        - text: ⚙Settings
    - generic [ref=e21]:
      - paragraph [ref=e22]: Pro plan
      - text: Unlimited projects, premium modules, and priority support.
      - button "Manage plan" [ref=e23]
    - generic [ref=e24]:
      - generic [ref=e25]: AC
      - generic [ref=e26]:
        - strong [ref=e27]: Acme Corp
        - text: Workspace
  - generic [ref=e28]:
    - banner [ref=e29]:
      - generic [ref=e30]:
        - generic [ref=e31]:
          - text: Workspace
          - strong [ref=e32]: Acme Corp
        - generic [ref=e33]:
          - text: Site
          - strong [ref=e34]: acme.cyan.app
      - generic [ref=e35]:
        - button "فا" [ref=e36]
        - button "☾" [ref=e37]
        - text: ⍰
        - generic [ref=e38]:
          - generic [ref=e39]: AM
          - generic [ref=e40]:
            - strong [ref=e41]: Ali Mohammadi
            - text: Admin
    - main [ref=e42]:
      - generic [ref=e44]:
        - paragraph [ref=e45]: AI-native business platform
        - heading "Maker - Definitions" [level=1] [ref=e46]
        - paragraph [ref=e47]: Design entities, fields, relations, validations, and permissions from the structured runtime contract.
      - generic [ref=e48]:
        - generic [ref=e49]:
          - generic [ref=e50]:
            - textbox "Search entities..." [ref=e51]
            - generic [ref=e52]:
              - button "Create from template" [ref=e53]
              - button "Publish schema" [ref=e54]
          - generic [ref=e56]:
            - generic [ref=e57]: Template
            - combobox "Template" [ref=e58]
          - generic [ref=e59]:
            - generic [ref=e61]:
              - strong [ref=e62]: No definitions returned by backend
              - text: This list fills after definitions are created in the service.
            - generic [ref=e63]:
              - generic [ref=e64]: FieldsValidationsRelationsPermissions
              - generic [ref=e65]:
                - strong [ref=e66]: No selection
                - generic [ref=e67]:
                  - button "Reorder" [ref=e68]
                  - button "Bulk actions" [ref=e69]
              - table [ref=e70]:
                - rowgroup [ref=e71]:
                  - row "Field name Type Required Description" [ref=e72]:
                    - columnheader "Field name" [ref=e73]
                    - columnheader "Type" [ref=e74]
                    - columnheader "Required" [ref=e75]
                    - columnheader "Description" [ref=e76]
                - rowgroup [ref=e77]:
                  - row "No fields are available for this definition." [ref=e78]:
                    - cell "No fields are available for this definition." [ref=e79]
              - generic [ref=e80]:
                - generic [ref=e81]: Definition JSON
                - textbox [ref=e82]
        - complementary [ref=e83]:
          - generic [ref=e84]:
            - heading "API & DSL summary" [level=3] [ref=e85]
            - text: Empty
          - generic [ref=e86]:
            - strong [ref=e87]: No API summary data available
            - text: Select a template and create the definition from backend templates.
          - heading "Schema map" [level=3] [ref=e89]
          - generic [ref=e90]:
            - generic [ref=e91]:
              - strong [ref=e92]: —
              - generic [ref=e93]: 0 fields
            - generic [ref=e94]:
              - strong [ref=e95]: "0"
              - text: definitions
      - generic [ref=e96]:
        - generic [ref=e97]:
          - button "←" [ref=e98]
          - generic [ref=e99]:
            - strong [ref=e100]: Maker
            - text: No entity loaded
          - button "…" [ref=e101]
        - generic [ref=e102]: FieldsValidationsRelations
        - generic [ref=e104]:
          - generic [ref=e105]:
            - strong [ref=e106]: Scope
            - text: Define where this entity is available.
          - text: Organization
        - generic [ref=e108]:
          - strong [ref=e109]: API identifier
          - text: —
        - button "Publish schema" [disabled] [ref=e110]
      - navigation "Mobile navigation" [ref=e111]:
        - link "⌘Dashboard" [ref=e112] [cursor=pointer]:
          - /url: /
        - link "✦AI Studio" [ref=e113] [cursor=pointer]:
          - /url: /projects/new
        - link "◍Data" [ref=e114] [cursor=pointer]:
          - /url: /data
        - link "⌇Flow Builder" [ref=e115] [cursor=pointer]:
          - /url: /flows
        - link "⬡Client Apps/Bots" [ref=e116] [cursor=pointer]:
          - /url: /integrations
```

# Test source

```ts
  1   | import { expect, test, type Page } from "@playwright/test";
  2   | 
  3   | const definitions = [
  4   |   {
  5   |     serviceKey: "catalog-service",
  6   |     entityKey: "products",
  7   |     title: "Products",
  8   |     tenantKey: "tenant-demo",
  9   |     siteKey: "site-commerce",
  10  |     definitionJson: JSON.stringify({
  11  |       fields: [
  12  |         { key: "title", type: "String", required: true, label: "Product title" },
  13  |         { key: "price", type: "Decimal", required: true, label: "Price" }
  14  |       ]
  15  |     })
  16  |   }
  17  | ];
  18  | 
  19  | const storageKeys = {
  20  |   accessToken: "cyan.panel.authToken",
  21  |   refreshToken: "cyan.panel.refreshToken",
  22  |   expiresAt: "cyan.panel.authExpiresAt",
  23  |   sessionId: "cyan.panel.sessionId"
  24  | };
  25  | 
  26  | test("redirects to auth on protected 401 and returns after sign in", async ({ page }) => {
  27  |   await page.addInitScript((keys) => {
  28  |     window.localStorage.removeItem(keys.accessToken);
  29  |     window.localStorage.removeItem(keys.refreshToken);
  30  |     window.localStorage.removeItem(keys.expiresAt);
  31  |     window.localStorage.removeItem(keys.sessionId);
  32  |   }, storageKeys);
  33  |   await routeCaptcha(page);
  34  |   await page.route("**/api/sso/auth/login", async (route) => {
  35  |     const body = route.request().postDataJSON() as Record<string, unknown>;
  36  |     expect(body.clientId).toBe("cyan-panel");
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
> 53  |   await page.goto("/maker");
      |              ^ Error: page.goto: Test timeout of 30000ms exceeded.
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
  137 |   await expect(page.getByText("Products").first()).toBeVisible();
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
```