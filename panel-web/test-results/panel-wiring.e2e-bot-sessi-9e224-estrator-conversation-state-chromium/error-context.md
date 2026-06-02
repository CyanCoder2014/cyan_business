# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: panel-wiring.e2e.spec.ts >> bot session detail page renders ai orchestrator conversation state
- Location: tests/panel-wiring.e2e.spec.ts:82:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByRole('heading', { name: 'Build a retail workspace' })
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByRole('heading', { name: 'Build a retail workspace' })

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
  - heading "Bot session" [level=1]
  - paragraph: Persistent bot conversation thread.
  - paragraph: Session
  - paragraph: Loading session...
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
  16  |     await route.fulfill({
  17  |       status: 200,
  18  |       contentType: "application/json",
  19  |       body: JSON.stringify({
  20  |         draftId: "draft-retail",
  21  |         tenantKey: "tenant-demo",
  22  |         siteKey: "site-commerce",
  23  |         clientKey: "panel",
  24  |         status: "READY",
  25  |         title: "Retail Hub",
  26  |         appType: "SHOP",
  27  |         latestIntent: "Build a retail workspace with storefront and approvals.",
  28  |         answers: {},
  29  |         resolvedDsl: {
  30  |           app: { title: "Retail Hub", capabilities: ["storefront", "crm"] },
  31  |           entities: [{ key: "product" }, { key: "customer" }],
  32  |           routes: [{ path: "/" }, { path: "/shop" }],
  33  |           flows: [{ key: "approval" }],
  34  |           delivery: { publicApis: ["/shop"], botApis: [] },
  35  |           manualActions: []
  36  |         },
  37  |         pendingQuestionKeys: ["brandName"],
  38  |         pendingQuestions: ["What is the brand name?"],
  39  |         manualActions: ["Review copy"],
  40  |         updatedAt: "2026-05-30T10:00:00.000Z"
  41  |       })
  42  |     });
  43  |   });
  44  |   await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/sessions?draftId=draft-retail", async (route) => {
  45  |     await route.fulfill({
  46  |       status: 200,
  47  |       contentType: "application/json",
  48  |       body: JSON.stringify([
  49  |         {
  50  |           sessionId: "session-retail",
  51  |           channelType: "PANEL",
  52  |           tenantKey: "tenant-demo",
  53  |           siteKey: "site-commerce",
  54  |           draftId: "draft-retail",
  55  |           status: "WAITING_FOR_ANSWERS",
  56  |           messages: [],
  57  |           extractedAnswers: {},
  58  |           pendingQuestionKeys: ["brandName"],
  59  |           pendingQuestions: ["What is the brand name?"],
  60  |           latestPrompt: "Build a retail workspace",
  61  |           updatedAt: "2026-05-30T10:05:00.000Z"
  62  |         }
  63  |       ])
  64  |     });
  65  |   });
  66  |   await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/drafts/draft-retail/runs", async (route) => {
  67  |     await route.fulfill({
  68  |       status: 200,
  69  |       contentType: "application/json",
  70  |       body: JSON.stringify([])
  71  |     });
  72  |   });
  73  | 
  74  |   await page.goto("/projects/draft-retail");
  75  | 
  76  |   await expect(page.getByRole("heading", { name: "Retail Hub" })).toBeVisible();
  77  |   await expect(page.getByText("Build a retail workspace with storefront and approvals.")).toBeVisible();
  78  |   await expect(page.getByText("Review copy")).toBeVisible();
  79  |   await expect(page.getByText("session-retail")).toBeVisible();
  80  | });
  81  | 
  82  | test("bot session detail page renders ai orchestrator conversation state", async ({ page }) => {
  83  |   await page.route("**/api/platform/service/ai-orchestrator-service/endpoint/ai-orchestrator/sessions/session-retail", async (route) => {
  84  |     await route.fulfill({
  85  |       status: 200,
  86  |       contentType: "application/json",
  87  |       body: JSON.stringify({
  88  |         sessionId: "session-retail",
  89  |         channelType: "TELEGRAM",
  90  |         tenantKey: "tenant-demo",
  91  |         siteKey: "site-commerce",
  92  |         draftId: "draft-retail",
  93  |         status: "WAITING_FOR_ANSWERS",
  94  |         messages: [
  95  |           {
  96  |             messageId: "msg-1",
  97  |             role: "USER",
  98  |             content: "Build a retail workspace",
  99  |             createdAt: "2026-05-30T10:00:00.000Z"
  100 |           }
  101 |         ],
  102 |         extractedAnswers: {
  103 |           businessName: "Retail Hub"
  104 |         },
  105 |         pendingQuestionKeys: ["brandName"],
  106 |         pendingQuestions: ["What is the brand name?"],
  107 |         latestPrompt: "Build a retail workspace",
  108 |         latestQuestion: "What is the brand name?",
  109 |         updatedAt: "2026-05-30T10:05:00.000Z"
  110 |       })
  111 |     });
  112 |   });
  113 | 
  114 |   await page.goto("/bot/session-retail");
  115 | 
> 116 |   await expect(page.getByRole("heading", { name: "Build a retail workspace" })).toBeVisible();
      |                                                                                 ^ Error: expect(locator).toBeVisible() failed
  117 |   await expect(page.getByText("TELEGRAM")).toBeVisible();
  118 |   await expect(page.getByText("What is the brand name?")).toBeVisible();
  119 |   await expect(page.getByText("\"businessName\": \"Retail Hub\"")).toBeVisible();
  120 | });
  121 | 
  122 | test("data and flows pages show backend-empty states instead of fixture data", async ({ page }) => {
  123 |   await page.route("**/api/platform/dynamic/**/endpoint/entities/records/**", async (route) => {
  124 |     await route.fulfill({ json: [] });
  125 |   });
  126 |   await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):8001\/endpoint\/bpm\/flows.*/, async (route) => {
  127 |     await route.fulfill({ json: [] });
  128 |   });
  129 |   await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):8001\/endpoint\/bpm\/metadata\/state-actions.*/, async (route) => {
  130 |     await route.fulfill({ json: [] });
  131 |   });
  132 |   await page.route(/http:\/\/(?:localhost|127\.0\.0\.1):8001\/endpoint\/bpm\/metadata\/transition-conditions.*/, async (route) => {
  133 |     await route.fulfill({
  134 |       json: {
  135 |         operators: ["EQ"],
  136 |         logicalOperators: ["AND"],
  137 |         supportedFields: ["status"]
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
```