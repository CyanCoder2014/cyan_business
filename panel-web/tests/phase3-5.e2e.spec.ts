import { expect, test, type Page, type Route } from "@playwright/test";

const bootstrap = {
  identity:{username:"operator@cyan.local",email:"operator@cyan.local",mfaEnabled:true,roles:["user"],active:true},
  access:{realmRoles:["tenant-admin"],realmPermissions:["panel:read","project.create","project.read","definition.read","record.read","bpm.read","automation.read","bot.read","settings.read"],clients:[{clientId:"cyan-panel",clientRoles:["builder"],clientPermissions:["media:write"]}]},
  tenants:[{tenantKey:"acme",displayName:"Acme Workspace",status:"ACTIVE",membershipRole:"TENANT_OWNER"}],
  sites:[{tenantKey:"acme",siteKey:"main",name:"Main Site",status:"ACTIVE"}],activeTenantKey:"acme",activeSiteKey:"main",
  subscription:{tenantKey:"acme",planKey:"growth",status:"ACTIVE",features:["ai"],limits:{projects:20,sites:3},providerState:"CONFIGURED"},
  capabilities:["ai-orchestrator","dynamic-entities","bpm","automation","bot-adapter","site-builder","search"].map((key)=>({key,enabled:true,source:"PLAN",status:"AVAILABLE",limits:{}})),
  featureFlags:{},services:{identity:"AVAILABLE",tenancy:"AVAILABLE",sessionScope:"AVAILABLE",sites:"AVAILABLE",billing:"AVAILABLE",capabilities:"AVAILABLE"},warnings:[]
};
const dsl={app:{title:"Customer Portal",capabilities:["site-builder","automation","bpm"]},entities:[{entityKey:"customer"}],routes:[{path:"/"}],flows:[{flowKey:"review"}],delivery:{publicApis:["/public/portal"],botApis:[]},manualActions:[]};
const draft={draftId:"draft-1",tenantKey:"acme",siteKey:"main",status:"READY",title:"Customer Portal",appType:"WEBSITE",latestIntent:"Build a customer portal",answers:{},resolvedDsl:dsl,pendingQuestionKeys:[],pendingQuestions:[],manualActions:[],revision:3,updatedAt:"2026-08-05T08:00:00Z"};
const session={sessionId:"session-1",tenantKey:"acme",siteKey:"main",draftId:"draft-1",status:"ACTIVE",latestPrompt:"Build a customer portal",pendingQuestions:[],messages:[{messageId:"message-1",role:"USER",content:"Build a customer portal",createdAt:"2026-08-05T08:00:00Z"}]};
const definition={id:1,serviceKey:"content-service",tenantKey:"acme",siteKey:"main",entityKey:"customer",entityType:"CRM",title:"Customer",active:true,revision:2,definition:{entityKey:"customer",entityType:"CRM",title:"Customer",fields:{name:{id:"name",type:"string",validations:[{validation:"REQUIRED"}]},profile:{id:"profile",type:"object",itemValidations:{email:{id:"email",type:"string",validations:[{validation:"REQUIRED"}]}}}}}};

async function prepare(page:Page, locale="en", theme="light") {
  await page.addInitScript(({locale,theme})=>{localStorage.setItem("cyan.panel.authToken","e2e-token");localStorage.setItem("cyan.panel.authExpiresAt",String(Date.now()+3600000));localStorage.setItem("cyan.panel.sessionId","e2e-session");localStorage.setItem("cyan.panel.username","operator@cyan.local");localStorage.setItem("cyan.panel.locale",locale);localStorage.setItem("cyan.panel.theme",theme);},{locale,theme});
  await page.route("**/api/panel/bootstrap",route=>route.fulfill({json:bootstrap}));
  await page.route("**/api/platform/**",route=>fulfillPlatform(route));
}

async function fulfillPlatform(route:Route) {
  const url=route.request().url(); const method=route.request().method();
  if(url.includes("notifications/inbox/unread-count")) return route.fulfill({json:{unreadCount:2,updatedAt:"2026-08-05T08:02:00Z"}});
  if(url.includes("notifications/inbox/read-all")) return route.fulfill({json:{unreadCount:0,updatedAt:"2026-08-05T08:03:00Z"}});
  if(url.includes("notifications/inbox")) return route.fulfill({json:{content:[{notificationId:"n-1",tenantKey:"acme",siteKey:"main",type:"PROJECT",severity:"INFO",title:"Project is ready",body:"Provisioning plan completed",deepLink:"/projects/draft-1",createdAt:"2026-08-05T08:01:00Z",version:0}],page:0,size:20,totalElements:1,totalPages:1}});
  if(url.includes("drafts/draft-1/releases")) return route.fulfill({json:method==="POST"?{releaseId:"release-2",draftId:"draft-1",tenantKey:"acme",siteKey:"main",sourceRevision:3,provisioningRunId:"run-1",status:"DRAFT",snapshot:dsl,createdBy:"operator",createdAt:"2026-08-05T08:05:00Z"}:[]});
  if(url.includes("drafts/draft-1/runs")) return route.fulfill({json:[{runId:"run-1",draftId:"draft-1",status:"SUCCESS",startedAt:"2026-08-05T08:02:00Z",finishedAt:"2026-08-05T08:03:00Z",stepResults:[{step:"definition",status:"SUCCESS"}]}]});
  if(url.includes("drafts/draft-1/provision")) return route.fulfill({json:{runId:"run-2",draftId:"draft-1",status:"RUNNING",startedAt:"2026-08-05T08:06:00Z",stepResults:[]}});
  if(url.includes("drafts/draft-1/attachments")) return route.fulfill({json:{assetKey:"asset-1"}});
  if(url.includes("drafts/draft-1")) return route.fulfill({json:draft});
  if(url.includes("/drafts")) return route.fulfill({json:[draft]});
  if(url.includes("/sessions/session-1")) return route.fulfill({json:session});
  if(url.includes("/sessions")) return route.fulfill({json:[session]});
  if(url.includes("/blueprints")) return route.fulfill({json:[{blueprintKey:"portal",appType:"WEBSITE",version:1,title:"Customer Portal",description:"Build a customer portal",active:true,capabilities:["site-builder"],requiredQuestions:[],defaultAnswers:{},baseDsl:dsl}]});
  if(url.includes("media/uploads/prepare")) return route.fulfill({json:{uploadId:"upload-1",assetKey:"asset-1",uploadUrl:"/endpoint/media/uploads/upload-1",method:"PUT",status:"PREPARED",expectedSizeBytes:4,uploadedSizeBytes:0,expiresAt:"2026-08-05T09:00:00Z"}});
  if(url.includes("media/uploads/upload-1")) return route.fulfill({json:{uploadId:"upload-1",assetKey:"asset-1",uploadUrl:"/endpoint/media/uploads/upload-1",method:"PUT",status:"UPLOADED",expectedSizeBytes:4,uploadedSizeBytes:4,expiresAt:"2026-08-05T09:00:00Z"}});
  if(url.includes("managed-objects/assigned-to-me")) return route.fulfill({json:[{id:"work-1",objectType:"Approval",state:"REVIEW"}]});
  if(url.includes("automation-orchestrator/executions")) return route.fulfill({json:[{executionId:"execution-1",automationKey:"sync",status:"FAILED"}]});
  if(url.includes("bot-adapter/integrations")) return route.fulfill({json:[]});
  if(url.includes("definitions/customer/versions")) return route.fulfill({json:[{revision:2,status:"DRAFT",definition:JSON.stringify(definition.definition),createdAt:"2026-08-05T08:00:00Z"}]});
  if(url.includes("definitions/customer")) return route.fulfill({json:{...definition,revision:method==="PUT"?3:2}});
  if(url.includes("/definitions")) return route.fulfill({json:{content:[definition],page:0,size:200,totalElements:1,totalPages:1}});
  if(url.includes("/templates")) return route.fulfill({json:[]});
  if(url.includes("records/customer")&&method==="GET") return route.fulfill({json:{content:[{recordKey:"customer-1",data:{name:"A real customer",profile:{email:"owner@example.com"}}}],page:0,size:25,totalElements:1,totalPages:1}});
  if(url.includes("records/customer")) return route.fulfill({json:{recordKey:"customer-2",data:route.request().postDataJSON()?.data??{}}});
  return route.fulfill({json:[]});
}

test("Phase 3 dashboard and notification center use independently sourced data",async({page})=>{await prepare(page);await page.goto("/dashboard");await expect(page.getByRole("heading",{name:"Customer Portal"})).toBeVisible();await expect(page.getByText("sync")).toBeVisible();await expect(page.getByText("2").last()).toBeVisible();await page.getByRole("button",{name:/Notifications/}).click();await expect(page.getByText("Project is ready")).toBeVisible();await page.getByRole("button",{name:"Mark all read"}).click();});

test("Phase 4 project persists runs, creates releases, and uploads real bytes",async({page})=>{await prepare(page);let uploaded=0;page.on("dialog",dialog=>dialog.accept());page.on("request",request=>{if(request.url().includes("media/uploads/upload-1"))uploaded=request.postDataBuffer()?.length??request.postData()?.length??Number(request.headers()["content-length"]??0);});await page.goto("/projects/draft-1");await page.getByRole("tab",{name:"Provisioning"}).click();await expect(page.getByText("run-1")).toBeVisible();await page.getByRole("button",{name:"Create release"}).click();await page.getByRole("tab",{name:"Releases"}).click();await expect(page.getByText("release-2")).toBeVisible();await page.goto("/ai");await expect(page.getByRole("link",{name:"Open project"})).toBeVisible();await page.locator('input[type="file"]').setInputFiles({name:"brief.txt",mimeType:"text/plain",buffer:Buffer.from("test")});await expect.poll(()=>uploaded).toBe(4);});

test("Phase 5 edits nested definitions and renders definition-driven records",async({page})=>{await prepare(page);let savedRevision:unknown;await page.route("**/api/platform/dynamic/content-service/endpoint/entities/definitions/customer",async route=>{if(route.request().method()==="PUT")savedRevision=route.request().postDataJSON()?.expectedRevision;await fulfillPlatform(route);});await page.goto("/definitions/content-service/customer");await page.getByRole("button",{name:/profile object 1 nested/}).click();const nested=page.getByLabel("Nested fields");await expect(nested).toBeVisible();await nested.fill('{"email":{"id":"email","type":"string","validations":[{"validation":"REQUIRED"}]},"phone":{"id":"phone","type":"string"}}');await page.getByRole("button",{name:"Save"}).click();await expect.poll(()=>savedRevision).toBe(2);await page.goto("/data/content-service/customer");await expect(page.getByText("A real customer")).toBeVisible();await page.getByRole("button",{name:"New record"}).click();await expect(page.getByText("profile",{exact:true}).last()).toBeVisible();});

test("captures Phase 3-5 desktop tablet mobile light dark and RTL",async({page})=>{test.skip(process.env.CAPTURE_PHASES_3_5!=="1","Visual capture is explicit.");await prepare(page);await page.goto("/dashboard");const phases=[{phase:3,path:"/dashboard",ready:".active-project-card h2"},{phase:4,path:"/ai",ready:'a[href="/projects/draft-1"]'},{phase:5,path:"/definitions/content-service/customer",ready:".definition-editor-toolbar"}];const states=[{name:"desktop-en-light",w:1440,h:1000,l:"en",t:"light"},{name:"desktop-en-dark",w:1440,h:1000,l:"en",t:"dark"},{name:"tablet-en-light",w:834,h:1112,l:"en",t:"light"},{name:"mobile-en-light",w:390,h:844,l:"en",t:"light"},{name:"mobile-en-dark",w:390,h:844,l:"en",t:"dark"},{name:"desktop-fa-rtl-light",w:1440,h:1000,l:"fa",t:"light"},{name:"mobile-fa-rtl-light",w:390,h:844,l:"fa",t:"light"}];for(const phase of phases){for(const state of states){await page.setViewportSize({width:state.w,height:state.h});await page.evaluate(({l,t})=>{localStorage.setItem("cyan.panel.locale",l);localStorage.setItem("cyan.panel.theme",t);},{l:state.l,t:state.t});await page.goto(phase.path);await expect(page.locator(".panel-app-shell")).toBeVisible();await expect(page.locator(phase.ready).first()).toBeVisible();await page.screenshot({path:`../docs/ui-redesign/completion/phase-${phase.phase}/screenshots/${state.name}.png`,fullPage:false});}}});
