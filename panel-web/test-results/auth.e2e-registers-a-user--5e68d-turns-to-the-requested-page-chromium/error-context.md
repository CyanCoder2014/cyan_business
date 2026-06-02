# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: auth.e2e.spec.ts >> registers a user, logs in, and returns to the requested page
- Location: tests/auth.e2e.spec.ts:68:5

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: locator.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for getByTestId('desktop-auth-form').getByRole('button', { name: 'Create account' })
    - locator resolved to <button disabled type="submit" class="primary-pill auth-submit">Create account</button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not enabled
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not enabled
    - retrying click action
      - waiting 100ms
    14 × waiting for element to be visible, enabled and stable
       - element is not enabled
     - retrying click action
       - waiting 500ms

```

# Page snapshot

```yaml
- main [ref=e2]:
  - generic [ref=e3]:
    - link "C CyanAI-native app platform" [ref=e4] [cursor=pointer]:
      - /url: /
      - generic [ref=e5]: C
      - generic [ref=e6]:
        - strong [ref=e7]: Cyan
        - text: AI-native app platform
    - generic [ref=e8]:
      - paragraph [ref=e9]: AI-native app platform
      - heading "Launch your business app with AI" [level=1] [ref=e10]
      - paragraph [ref=e11]: Build websites, PWAs, commerce, CRM, forms, automation, and bots from one workspace.
    - generic [ref=e12]:
      - generic [ref=e13]:
        - generic [ref=e14]: ✦
        - generic [ref=e15]:
          - strong [ref=e16]: Website & PWA
          - paragraph [ref=e17]: Marketing sites, portals, and progressive web apps.
      - generic [ref=e18]:
        - generic [ref=e19]: ✦
        - generic [ref=e20]:
          - strong [ref=e21]: Automation
          - paragraph [ref=e22]: Connect services and remove repetitive busywork.
      - generic [ref=e23]:
        - generic [ref=e24]: ✦
        - generic [ref=e25]:
          - strong [ref=e26]: CRM
          - paragraph [ref=e27]: Manage leads, contacts, deals, and relationships.
      - generic [ref=e28]:
        - generic [ref=e29]: ✦
        - generic [ref=e30]:
          - strong [ref=e31]: Telegram & Bale
          - paragraph [ref=e32]: Run customer support and workflows in messaging channels.
    - generic [ref=e33]:
      - generic [ref=e34]:
        - generic [ref=e35]: ◈
        - generic [ref=e36]:
          - strong [ref=e37]: Secure by design
          - paragraph [ref=e38]: Enterprise-grade security and encrypted data.
      - generic [ref=e39]:
        - generic [ref=e40]: ◎
        - generic [ref=e41]:
          - strong [ref=e42]: Multi-tenant ready
          - paragraph [ref=e43]: Isolated workspaces for teams and clients.
      - generic [ref=e44]:
        - generic [ref=e45]: ▣
        - generic [ref=e46]:
          - strong [ref=e47]: Mobile-friendly
          - paragraph [ref=e48]: PWA-ready experience on every device.
  - generic [ref=e49]:
    - generic [ref=e50]:
      - text: Already have an account?
      - button "Sign in" [ref=e51]
    - generic [ref=e52]:
      - generic [ref=e53]:
        - button "Sign in" [ref=e54]
        - button "Create account" [ref=e55]
      - generic [ref=e56]:
        - generic [ref=e57]:
          - text: Work email
          - textbox "Work email" [ref=e58]:
            - /placeholder: name@company.com
            - text: new-user@example.com
        - generic [ref=e59]:
          - text: Password
          - textbox "Password" [ref=e60]:
            - /placeholder: Create a strong password
            - text: StrongPass123!
        - generic [ref=e61]:
          - text: Workspace name
          - textbox "Workspace name" [ref=e62]:
            - /placeholder: Acme Corp
            - text: Example Workspace
        - generic [ref=e63]:
          - text: Phone number (optional)
          - textbox "Phone number (optional)" [ref=e64]:
            - /placeholder: +1 (555) 123-4567
            - text: "+15551234567"
        - generic [ref=e65]:
          - text: Security answer
          - generic [ref=e66]:
            - textbox "Security answer Refresh" [active] [ref=e67]:
              - /placeholder: Loading...
              - text: "5"
            - button "Refresh" [ref=e68]
        - button "Create account" [disabled] [ref=e69]
        - paragraph [ref=e70]: By continuing, you agree to our Terms of Service and Privacy Policy.
    - generic [ref=e71]:
      - strong [ref=e73]: 9:41
      - generic [ref=e74]:
        - generic [ref=e75]:
          - generic [ref=e76]: C
          - strong [ref=e77]: Cyan
        - heading "Launch your business app with AI" [level=2] [ref=e78]
        - paragraph [ref=e79]: Create, automate, and scale in minutes.
        - generic [ref=e80]:
          - button "Sign in" [ref=e81]
          - button "Create account" [ref=e82]
        - generic [ref=e83]:
          - generic [ref=e84]:
            - text: Work email
            - textbox "Work email" [ref=e85]:
              - /placeholder: name@company.com
          - generic [ref=e86]:
            - text: Password
            - textbox "Password" [ref=e87]:
              - /placeholder: Create a strong password
          - generic [ref=e88]:
            - text: Workspace name
            - textbox "Workspace name" [ref=e89]:
              - /placeholder: Acme Corp
          - generic [ref=e90]:
            - text: Phone number (optional)
            - textbox "Phone number (optional)" [ref=e91]:
              - /placeholder: +1 (555) 123-4567
          - generic [ref=e92]:
            - text: Security answer
            - generic [ref=e93]:
              - textbox "Security answer Refresh" [ref=e94]:
                - /placeholder: Loading...
              - button "Refresh" [ref=e95]
          - button "Create account" [disabled] [ref=e96]
```

# Test source

```ts
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
> 106 |   await form.getByRole("button", { name: "Create account" }).click();
      |                                                              ^ Error: locator.click: Test timeout of 30000ms exceeded.
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