# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: panel-wiring.e2e.spec.ts >> integrations page stays empty without backend data and reflects real mini app publishing
- Location: tests/panel-wiring.e2e.spec.ts:251:5

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: locator.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for getByRole('button', { name: 'Create build' })

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
        - heading "Client Apps / Bots" [level=1] [ref=e46]
        - paragraph [ref=e47]: Connect and manage website, Telegram, Bale, mini-app, and mobile channels from one operational control room.
      - generic [ref=e48]:
        - generic [ref=e49]:
          - generic [ref=e50]:
            - generic [ref=e51]: No channel returnedNo mini app buildNo delivery history
            - generic [ref=e52]:
              - button "Refresh" [ref=e53]
              - button "Add channel" [ref=e54]
          - article [ref=e56]:
            - strong [ref=e57]: No channels returned by backend
            - generic [ref=e58]: This page no longer falls back to fabricated channel cards.
          - generic [ref=e59]: Channel SettingsSession MappingProvisioning
          - generic [ref=e60]:
            - heading "Outbound message delivery" [level=3] [ref=e62]
            - table [ref=e63]:
              - rowgroup [ref=e64]:
                - row "Channel Recipient Message Status" [ref=e65]:
                  - columnheader "Channel" [ref=e66]
                  - columnheader "Recipient" [ref=e67]
                  - columnheader "Message" [ref=e68]
                  - columnheader "Status" [ref=e69]
              - rowgroup [ref=e70]:
                - row "No outbound messages were returned for this channel." [ref=e71]:
                  - cell "No outbound messages were returned for this channel." [ref=e72]
            - generic [ref=e73]:
              - generic [ref=e74]:
                - strong [ref=e75]: "0"
                - text: Sent
              - generic [ref=e76]:
                - strong [ref=e77]: "0"
                - text: Delivered
              - generic [ref=e78]:
                - strong [ref=e79]: "0"
                - text: Failed
        - complementary [ref=e80]:
          - generic [ref=e81]:
            - heading "No channel selected" [level=3] [ref=e82]
            - text: Empty
          - generic [ref=e83]:
            - strong [ref=e84]: No channel is available to manage
            - text: Use Add channel to create a real backend integration.
      - generic [ref=e85]:
        - generic [ref=e86]:
          - generic [ref=e87]:
            - strong [ref=e88]: Client Apps / Bots
            - text: Channels control room
          - button "Add" [ref=e89]
        - generic [ref=e91]:
          - strong [ref=e92]: No channels found
          - text: The backend has not returned any channels yet.
      - navigation "Mobile navigation" [ref=e93]:
        - link "⌘Dashboard" [ref=e94] [cursor=pointer]:
          - /url: /
        - link "✦AI Studio" [ref=e95] [cursor=pointer]:
          - /url: /projects/new
        - link "◍Data" [ref=e96] [cursor=pointer]:
          - /url: /data
        - link "⌇Flow Builder" [ref=e97] [cursor=pointer]:
          - /url: /flows
        - link "⬡Client Apps/Bots" [ref=e98] [cursor=pointer]:
          - /url: /integrations
```

# Test source

```ts
  212 |         siteKey: "site-commerce",
  213 |         path,
  214 |         route: record?.data ?? { path },
  215 |         target: { recordKey: record?.recordKey ?? "missing" },
  216 |         theme: { templateKey: "landing-v1" }
  217 |       }
  218 |     });
  219 |   });
  220 |   await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):8001\/public\/storefront\/render\?path=.*/, async (route) => {
  221 |     const url = new URL(route.request().url());
  222 |     const path = url.searchParams.get("path") ?? "/";
  223 |     await route.fulfill({
  224 |       json: {
  225 |         tenantKey: "tenant-demo",
  226 |         siteKey: "site-commerce",
  227 |         path,
  228 |         route: { path },
  229 |         target: { rendered: true },
  230 |         theme: { templateKey: "landing-v1" },
  231 |         html: `<main>${path}</main>`
  232 |       }
  233 |     });
  234 |   });
  235 | 
  236 |   await page.goto("/site-builder");
  237 | 
  238 |   await expect(page.getByRole("button", { name: /Home/ }).first()).toBeVisible();
  239 |   await expect(page.getByText("/", { exact: true }).first()).toBeVisible();
  240 |   await expect(page.getByText("About")).toHaveCount(0);
  241 | 
  242 |   await page.getByLabel("Page title").fill("Support");
  243 |   await page.getByLabel("Path").fill("/support");
  244 |   await page.getByRole("button", { name: "Publish" }).first().click();
  245 | 
  246 |   await expect(page.getByText("Published.")).toBeVisible();
  247 |   await expect(page.getByLabel("Path")).toHaveValue("/support");
  248 |   await expect(page.getByRole("button", { name: /Support/ }).first()).toBeVisible();
  249 | });
  250 | 
  251 | test("integrations page stays empty without backend data and reflects real mini app publishing", async ({ page }) => {
  252 |   let integrations = [
  253 |     {
  254 |       channel: "TELEGRAM",
  255 |       integrationKey: "telegram-main",
  256 |       tenantKey: "tenant-demo",
  257 |       siteKey: "site-commerce",
  258 |       botUsername: "@cyan_assistant_bot",
  259 |       tokenSecretRef: "vault://bots/retail-demo",
  260 |       miniAppUrl: "https://preview.cyan.app/mini-app",
  261 |       miniAppEnabled: true,
  262 |       active: true
  263 |     }
  264 |   ];
  265 |   let messages: Array<Record<string, unknown>> = [];
  266 |   let miniApps: Array<Record<string, unknown>> = [];
  267 | 
  268 |   await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/integrations?tenantKey=tenant-demo&siteKey=site-commerce", async (route) => {
  269 |     await route.fulfill({ json: integrations });
  270 |   });
  271 |   await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/messages?tenantKey=tenant-demo&siteKey=site-commerce", async (route) => {
  272 |     await route.fulfill({ json: messages });
  273 |   });
  274 |   await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/mini-apps?tenantKey=tenant-demo&siteKey=site-commerce", async (route) => {
  275 |     await route.fulfill({ json: miniApps });
  276 |   });
  277 |   await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/mini-apps", async (route, request) => {
  278 |     const body = JSON.parse(request.postData() ?? "{}");
  279 |     miniApps = [
  280 |       {
  281 |         channel: body.channel,
  282 |         integrationKey: body.integrationKey,
  283 |         buildKey: body.buildKey,
  284 |         title: body.title,
  285 |         launchUrl: body.launchUrl,
  286 |         status: "DRAFT"
  287 |       }
  288 |     ];
  289 |     await route.fulfill({ json: miniApps[0] });
  290 |   });
  291 |   await page.route("**/api/platform/service/bot-adapter-service/endpoint/bot-adapter/mini-apps/TELEGRAM/telegram-main/telegram-main-build/publish", async (route) => {
  292 |     miniApps = [
  293 |       {
  294 |         channel: "TELEGRAM",
  295 |         integrationKey: "telegram-main",
  296 |         buildKey: "telegram-main-build",
  297 |         title: "Telegram Main Mini App",
  298 |         launchUrl: "https://preview.cyan.app/mini-app",
  299 |         publishedUrl: "https://miniapp.cyan.app/telegram-main",
  300 |         status: "PUBLISHED"
  301 |       }
  302 |     ];
  303 |     await route.fulfill({ json: miniApps[0] });
  304 |   });
  305 | 
  306 |   await page.goto("/integrations");
  307 | 
  308 |   await expect(page.getByText("No outbound messages were returned for this channel.")).toBeVisible();
  309 |   await expect(page.getByText("Bale Bot")).toHaveCount(0);
  310 |   await expect(page.getByText("1248")).toHaveCount(0);
  311 | 
> 312 |   await page.getByRole("button", { name: "Create build" }).click();
      |                                                            ^ Error: locator.click: Test timeout of 30000ms exceeded.
  313 |   await expect(page.getByText("Mini app provisioned.")).toBeVisible();
  314 | 
  315 |   const publishButton = page.getByRole("button", { name: "Publish mini app" });
  316 |   await publishButton.scrollIntoViewIfNeeded();
  317 |   await publishButton.click({ force: true });
  318 |   await expect(page.getByText("Mini app published.")).toBeVisible();
  319 |   await expect(page.getByText("https://miniapp.cyan.app/telegram-main").first()).toBeVisible();
  320 | });
  321 | 
  322 | async function seedAuth(page: Page) {
  323 |   await page.addInitScript((keys) => {
  324 |     window.localStorage.setItem(keys.accessToken, "seeded-access");
  325 |     window.localStorage.setItem(keys.refreshToken, "seeded-refresh");
  326 |     window.localStorage.setItem(keys.expiresAt, String(Date.now() + 3_600_000));
  327 |     window.localStorage.setItem(keys.sessionId, "session-seeded");
  328 |   }, storageKeys);
  329 | }
  330 | 
```