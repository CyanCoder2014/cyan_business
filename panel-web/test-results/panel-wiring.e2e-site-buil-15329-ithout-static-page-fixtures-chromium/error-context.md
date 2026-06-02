# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: panel-wiring.e2e.spec.ts >> site builder loads backend routes and publishes a route without static page fixtures
- Location: tests/panel-wiring.e2e.spec.ts:151:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByRole('button', { name: /Home/ }).first()
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByRole('button', { name: /Home/ }).first()

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
  - heading "Site Builder" [level=1]
  - paragraph: Visually build, preview, and publish production-ready routes without dropping into raw JSON.
  - complementary:
    - heading "Pages" [level=3]
    - button "Add page"
    - strong: No stored routes were returned
    - text: This page no longer shows a fabricated page list.
  - text: Backend themeDesktop
  - button "Preview"
  - button "Draft"
  - button "Publish"
  - text: Page title
  - textbox "Page title": Home
  - text: Path
  - textbox "Path": /
  - text: "Route: /Domain: site-commerce.cyan.appNot rendered Built by storefront-service"
  - heading "Home" [level=2]
  - paragraph: The backend has not returned a rendered payload for this route yet.
  - text: LANDINGDRAFTsite-commerce
  - strong: Home
  - heading / [level=3]
  - paragraph: No rendered HTML yet
  - button "Publish this route"
  - article:
    - strong: Stored routes
    - text: "0"
  - article:
    - strong: Rendered HTML
    - text: —
  - article:
    - strong: Target
    - text: Pending
  - complementary:
    - heading "Route details" [level=3]
    - text: "#home ContentSEORendering"
    - strong: Path
    - text: /
    - strong: Title
    - text: Home
    - strong: Target entity
    - text: —
    - strong: Publish status
    - text: DRAFT
    - button "Save draft"
    - button "Publish"
  - strong: Site Builder
  - text: site-commerce.cyan.app
  - button "Publish"
  - text: Draft/
  - heading "Home" [level=3]
  - paragraph: A live backend preview is not available yet.
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
  138 |       }
  139 |     });
  140 |   });
  141 | 
  142 |   await page.goto("/data");
  143 |   await expect(page.getByText("No records were returned for this bucket.")).toBeVisible();
  144 |   await expect(page.getByText("Luna Lounge Chair")).toHaveCount(0);
  145 | 
  146 |   await page.goto("/flows");
  147 |   await expect(page.getByText("No flow was returned by the API")).toBeVisible();
  148 |   await expect(page.getByText("Route to review")).toHaveCount(0);
  149 | });
  150 | 
  151 | test("site builder loads backend routes and publishes a route without static page fixtures", async ({ page }) => {
  152 |   let routes = [
  153 |     {
  154 |       recordKey: "home",
  155 |       data: {
  156 |         routeKey: "home",
  157 |         path: "/",
  158 |         routeType: "LANDING",
  159 |         navigation: { label: "Home" },
  160 |         publicationStatus: "PUBLISHED",
  161 |         entityRef: {
  162 |           service: "content-service",
  163 |           entityKey: "landing-page",
  164 |           recordKey: "home"
  165 |         }
  166 |       }
  167 |     }
  168 |   ];
  169 | 
  170 |   await page.route("**/api/platform/dynamic/storefront-service/endpoint/entities/records/site-route", async (route, request) => {
  171 |     if (request.method() === "GET") {
  172 |       await route.fulfill({ json: routes });
  173 |       return;
  174 |     }
  175 |     if (request.method() === "POST") {
  176 |       const body = JSON.parse(request.postData() ?? "{}");
  177 |       routes = [
  178 |         ...routes.filter((item) => item.recordKey !== body.recordKey),
  179 |         {
  180 |           recordKey: body.recordKey,
  181 |           data: body.data
  182 |         }
  183 |       ];
  184 |       await route.fulfill({
  185 |         status: 200,
  186 |         contentType: "application/json",
  187 |         body: JSON.stringify({
  188 |           recordKey: body.recordKey,
  189 |           data: body.data
  190 |         })
  191 |       });
  192 |       return;
  193 |     }
  194 |     await route.fallback();
  195 |   });
  196 |   await page.route("**/api/platform/dynamic/storefront-service/endpoint/entities/templates/site-route/definitions", async (route) => {
  197 |     await route.fulfill({
  198 |       json: {
  199 |         serviceKey: "storefront-service",
  200 |         entityKey: "site-route",
  201 |         definitionJson: "{\"fields\":[]}"
  202 |       }
  203 |     });
  204 |   });
  205 |   await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):8001\/public\/storefront\/resolve\?path=.*/, async (route) => {
  206 |     const url = new URL(route.request().url());
  207 |     const path = url.searchParams.get("path") ?? "/";
  208 |     const record = routes.find((item) => item.data.path === path);
  209 |     await route.fulfill({
  210 |       json: {
  211 |         tenantKey: "tenant-demo",
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
> 238 |   await expect(page.getByRole("button", { name: /Home/ }).first()).toBeVisible();
      |                                                                    ^ Error: expect(locator).toBeVisible() failed
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
  312 |   await page.getByRole("button", { name: "Create build" }).click();
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