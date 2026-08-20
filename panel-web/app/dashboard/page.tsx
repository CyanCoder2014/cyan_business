"use client";
import Link from "next/link";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { useScopeAccess } from "@/components/scope-access-provider";
import { EmptyState, ErrorState, Skeleton, StatusBadge } from "@/components/ui/primitives";
import { listAssignedManagedObjects, type ManagedObject } from "@/lib/bpm-api";
import { listBotIntegrations, listClientDrafts, listProvisioningRuns } from "@/lib/platform-api";
import { listAutomationExecutions } from "@/lib/service-api";
import { getUnreadCount } from "@/lib/notification-api";
import type { AutomationExecution, BotChannelIntegration, ClientAppDraft, ProvisioningRun } from "@/lib/types";
import type { Subscription } from "@/lib/panel-contracts";
import { appTypeIcon, GridIcon, RocketIcon, InboxIcon, ZapIcon, ShieldCheckIcon, BotIcon, BellIcon, CardIcon, PulseIcon, DatabaseIcon, GlobeDotIcon, WorkflowIcon, CheckCircleIcon, XCircleIcon, ClockIcon, FormIcon, LayoutIcon } from "@/components/nav-icons";

type IconType = (props: { className?: string; size?: number }) => JSX.Element;

type Bucket<T>={data:T;loading:boolean;error:string|null;updatedAt:string|null}; const bucket=<T,>(data:T):Bucket<T>=>({data,loading:true,error:null,updatedAt:null});
export default function Dashboard(){
 const {locale}=usePanel(); const {bootstrap,tenantKey,siteKey,queryVersion,can,hasCapability}=useScopeAccess(); const [drafts,setDrafts]=useState(bucket<ClientAppDraft[]>([])); const [work,setWork]=useState(bucket<ManagedObject[]>([])); const [automations,setAutomations]=useState(bucket<AutomationExecution[]>([])); const [bots,setBots]=useState(bucket<BotChannelIntegration[]>([])); const [runs,setRuns]=useState(bucket<ProvisioningRun[]>([])); const [unread,setUnread]=useState(bucket<number|null>(null));
 const scope=useMemo(()=>({tenantKey:tenantKey||undefined,siteKey:siteKey||undefined}),[tenantKey,siteKey]);
 useEffect(()=>{if(!tenantKey)return; let live=true; const load=<T,>(fn:()=>Promise<T>,set:(v:Bucket<T>)=>void,initial:T)=>{set(bucket(initial));fn().then(data=>live&&set({data,loading:false,error:null,updatedAt:new Date().toISOString()})).catch(e=>live&&set({data:initial,loading:false,error:e instanceof Error?e.message:"Unavailable",updatedAt:null}));};load(()=>listClientDrafts(scope),setDrafts,[]);if(hasCapability("bpm")&&can("bpm.read"))load(()=>listAssignedManagedObjects(scope),setWork,[]);else setWork({data:[],loading:false,error:null,updatedAt:null});if(hasCapability("automation")&&can("automation.read"))load(()=>listAutomationExecutions(scope),setAutomations,[]);else setAutomations({data:[],loading:false,error:null,updatedAt:null});if(hasCapability("bot-adapter")&&can("bot.read"))load(()=>listBotIntegrations(scope),setBots,[]);else setBots({data:[],loading:false,error:null,updatedAt:null});load(()=>getUnreadCount().then(value=>value.unreadCount),setUnread,null);return()=>{live=false};},[queryVersion,tenantKey,siteKey,scope,can,hasCapability]);
 const latest=useMemo(()=>[...drafts.data].sort((a,b)=>Date.parse(b.updatedAt||"")-Date.parse(a.updatedAt||""))[0]||null,[drafts.data]);
 useEffect(()=>{if(!latest){setRuns({data:[],loading:false,error:null,updatedAt:null});return;}setRuns(bucket([]));listProvisioningRuns(latest.draftId).then(data=>setRuns({data,loading:false,error:null,updatedAt:new Date().toISOString()})).catch(e=>setRuns({data:[],loading:false,error:e instanceof Error?e.message:"Unavailable",updatedAt:null}));},[latest?.draftId]);
 const capabilities=bootstrap?.capabilities.filter(c=>c.enabled)??[]; const activity=[...drafts.data.map(item=>({id:item.draftId,label:item.title,status:item.status,at:item.updatedAt,href:`/projects/${item.draftId}`})),...runs.data.map(item=>({id:item.runId,label:item.runId,status:item.status,at:item.startedAt,href:latest?`/projects/${latest.draftId}`:"/projects"}))].sort((a,b)=>Date.parse(b.at||"")-Date.parse(a.at||"")).slice(0,5);
 return <PanelShell activeKey="dashboard" title="Workspace overview" titleFa="نمای کلی فضای کار" subtitle="Resume work and monitor real platform operations." subtitleFa="کار را ادامه دهید و وضعیت واقعی پلتفرم را پایش کنید."><div className="phase-dashboard">
  <section className="active-project-card">{drafts.loading?<Skeleton height={190}/>:drafts.error?<ErrorState title="Projects unavailable" description={drafts.error}/>:latest?<>{(()=>{const AppIcon=appTypeIcon(latest.appType);return <div className="active-project-icon" aria-hidden><AppIcon size={30}/></div>;})()}<div><StatusBadge tone="info">{latest.status}</StatusBadge><h2>{latest.title}</h2><p>{latest.latestIntent}</p><div className="metric-pills"><span><DatabaseIcon size={14}/>{latest.resolvedDsl?.entities?.length??0} {locale==="fa"?"موجودیت":"entities"}</span><span><GlobeDotIcon size={14}/>{latest.resolvedDsl?.routes?.length??0} {locale==="fa"?"مسیر":"routes"}</span><span><WorkflowIcon size={14}/>{latest.resolvedDsl?.flows?.length??0} {locale==="fa"?"فلو":"flows"}</span></div><Link className="primary-pill" href={`/projects/${latest.draftId}`}>{locale==="fa"?"ادامه ساخت":"Continue building"}</Link></div></>:<EmptyState title={locale==="fa"?"پروژه فعالی نیست":"No active project"} description={locale==="fa"?"از استودیوی هوش یا یک قالب واقعی شروع کنید.":"Start with AI Studio or an available blueprint."} action={<Link className="primary-pill" href="/ai">{locale==="fa"?"شروع":"Start building"}</Link>}/>}</section>
  <section className="dashboard-result-links" aria-labelledby="result-links-title"><header><div><p className="page-kicker">{locale==="fa"?"خروجی‌های فضای کاری":"Workspace results"}</p><h2 id="result-links-title">{locale==="fa"?"ساخته‌های خود را باز کنید":"Open what your team built"}</h2></div></header><div>{hasCapability("bpm")&&can("bpm.read")?<Link href="/work"><span aria-hidden><InboxIcon size={20}/></span><div><strong>{locale==="fa"?"کارتابل من":"My cartable"}</strong><small>{locale==="fa"?"کارها، فرم‌های فعال و انتقال‌ها":"Assigned work, active forms, and transitions"}</small></div><b>→</b></Link>:null}{hasCapability("dynamic-entities")?<Link href="/forms"><span aria-hidden><FormIcon size={20}/></span><div><strong>{locale==="fa"?"فرم‌های من":"My forms"}</strong><small>{locale==="fa"?"فرم‌های خصوصی و پیوندهای عمومی":"Private forms and public links"}</small></div><b>→</b></Link>:null}{hasCapability("dynamic-entities")&&can("record.read")?<Link href="/data"><span aria-hidden><DatabaseIcon size={20}/></span><div><strong>{locale==="fa"?"داده و فرم موجودیت":"Entity data & forms"}</strong><small>{locale==="fa"?"رکوردها و فرم ایجاد رکورد":"Records and generated record forms"}</small></div><b>→</b></Link>:null}{hasCapability("bpm")&&can("bpm.read")?<Link href="/bpm"><span aria-hidden><WorkflowIcon size={20}/></span><div><strong>{locale==="fa"?"طراح فرایند":"BPM designer"}</strong><small>{locale==="fa"?"تعریف و انتشار فرایندها":"Design and publish processes"}</small></div><b>→</b></Link>:null}{hasCapability("site-builder")&&can("site.read")?<Link href="/sites"><span aria-hidden><LayoutIcon size={20}/></span><div><strong>{locale==="fa"?"سایت‌ها و پیش‌نمایش":"Sites & previews"}</strong><small>{locale==="fa"?"سازنده، سایت عمومی و کارتابل سایت":"Builder, public site, and site portal"}</small></div><b>→</b></Link>:null}</div>{bootstrap?.sites.length?<footer>{bootstrap.sites.map(site=><span key={site.siteKey}><strong>{site.name}</strong><Link href={`/sites/${encodeURIComponent(site.siteKey)}/portal`}>{locale==="fa"?"کارتابل سایت":"Site cartable"}</Link><Link href={`/sites/${encodeURIComponent(site.siteKey)}/builder`}>{locale==="fa"?"سازنده و پیش‌نمایش":"Builder & preview"}</Link></span>)}</footer>:null}</section>
  <div className="dashboard-widget-grid">
   <Widget title={locale==="fa"?"پروژه‌های اخیر":"Recent projects"} icon={GridIcon} state={drafts} action={<Link href="/projects">{locale==="fa"?"مشاهده همه":"View all"}</Link>}>{drafts.data.slice(0,5).map(x=><Link key={x.draftId} href={`/projects/${x.draftId}`}><strong>{x.title}</strong><StatusBadge tone={x.status==="PROVISIONED"?"success":x.status==="FAILED"?"danger":"info"}>{x.status}</StatusBadge></Link>)}</Widget>
   <Widget title={locale==="fa"?"اجرای استقرار":"Provisioning runs"} icon={RocketIcon} state={runs} action={latest?<Link href={`/projects/${latest.draftId}`}>{locale==="fa"?"مشاهده همه":"View all"}</Link>:undefined}>{runs.data.slice(0,5).map(x=><div key={x.runId}><strong>{x.runId}</strong><StatusBadge tone={x.status==="SUCCEEDED"||x.status==="SUCCESS"?"success":x.status==="FAILED"?"danger":"info"}>{x.status}</StatusBadge></div>)}</Widget>
   {hasCapability("bpm")&&can("bpm.read")?<Widget title={locale==="fa"?"کارهای واگذارشده":"Assigned work"} icon={InboxIcon} state={work} action={<Link href="/work">{locale==="fa"?"مشاهده همه":"View all"}</Link>}>{work.data.slice(0,5).map(x=><Link key={x.id} href={`/work/${x.id}`}><strong>{x.objectType}</strong><span>{x.state}</span></Link>)}</Widget>:null}
   {hasCapability("automation")&&can("automation.read")?<AutomationHealthWidget locale={locale} state={automations}/>:null}
   <Widget title={locale==="fa"?"قابلیت‌های فعال":"Active capabilities"} icon={ShieldCheckIcon} state={{data:capabilities,loading:!bootstrap,error:null,updatedAt:new Date().toISOString()}}>{capabilities.map(x=><div key={x.key}><strong>{x.key}</strong><StatusBadge tone={x.status==="AVAILABLE"?"success":"warning"}>{x.status}</StatusBadge></div>)}</Widget>
   <Widget title={locale==="fa"?"بات‌ها و سایت‌ها":"Bots and sites"} icon={BotIcon} state={bots} action={<Link href="/bots">{locale==="fa"?"مشاهده همه":"View all"}</Link>}>{bots.data.filter(x=>x.active).map(x=><Link key={`${x.channel}-${x.integrationKey}`} href="/bots"><strong>{x.integrationKey}</strong><span>{x.channel}</span></Link>)}{bootstrap?.sites.map(x=><Link key={x.siteKey} href={`/sites/${encodeURIComponent(x.siteKey)}/builder`}><strong>{x.name}</strong><span>{x.status}</span></Link>)}</Widget>
   <Widget title={locale==="fa"?"اعلان‌ها":"Notifications"} icon={BellIcon} state={unread} action={<Link href="/notifications">{locale==="fa"?"مشاهده همه":"View all"}</Link>}>{unread.data!==null?<Link href="/notifications"><strong>{unread.data}</strong><span>{locale==="fa"?"خوانده‌نشده":"unread"}</span></Link>:null}</Widget>
   <PlanLimitsWidget locale={locale} subscription={bootstrap?.subscription??null} loading={!bootstrap}/>
   <Widget title={locale==="fa"?"فعالیت اخیر":"Recent activity"} icon={PulseIcon} state={{data:activity,loading:drafts.loading||runs.loading,error:drafts.error||runs.error,updatedAt:drafts.updatedAt}}>{activity.map(item=><Link key={item.id} href={item.href}><strong>{item.label}</strong><span>{item.status}</span></Link>)}</Widget>
  </div>
  {bootstrap?.subscription?.status==="NONE"?<section className="limited-dashboard"><h2>{locale==="fa"?"دسترسی محدود":"Limited access"}</h2><p>{locale==="fa"?"پلن فعالی ثبت نشده است. عملیات قفل‌شده موفقیت جعلی نمایش نمی‌دهند.":"No active plan is registered. Locked operations remain unavailable and never simulate success."}</p></section>:null}
 </div></PanelShell>;
}
function Widget<T>({title,icon:Icon,state,action,children}:{title:string;icon?:IconType;state:Bucket<T>;action?:ReactNode;children:ReactNode}){const {locale}=usePanel();const empty=Array.isArray(children)?children.length===0:!children;return <section className="dashboard-widget"><header><h3>{Icon?<span className="dashboard-widget-icon" aria-hidden><Icon size={15}/></span>:null}{title}</h3>{action?<span className="dashboard-widget-action">{action}</span>:state.updatedAt?<time>{locale==="fa"?"به‌روزرسانی ":"Updated "}{new Date(state.updatedAt).toLocaleTimeString()}</time>:null}</header>{state.loading?<><Skeleton height={45}/><Skeleton height={45}/></>:state.error?<ErrorState title="Unavailable" description={state.error}/>:empty?<EmptyState title={locale==="fa"?"داده‌ای نیست":"No data"} description={locale==="fa"?"سرویس رکوردی برنگرداند.":"The service returned no records."}/>:children}</section>}

function AutomationHealthWidget({locale,state}:{locale:"en"|"fa";state:Bucket<AutomationExecution[]>}){
 const total=state.data.length;
 const succeeded=state.data.filter(x=>["SUCCEEDED","SUCCESS","COMPLETED"].includes(String(x.status).toUpperCase())).length;
 const failed=state.data.filter(x=>String(x.status).toUpperCase()==="FAILED").length;
 const running=total-succeeded-failed;
 const pct=total?Math.round((succeeded/total)*100):0;
 const failPct=total?(failed/total)*100:0;
 const runPct=total?(running/total)*100:0;
 const ring=total?`conic-gradient(var(--success) 0 ${pct}%, var(--danger) ${pct}% ${pct+failPct}%, var(--warning) ${pct+failPct}% 100%)`:"conic-gradient(var(--border) 0 100%)";
 return <section className="dashboard-widget automation-health-widget"><header><h3><span className="dashboard-widget-icon" aria-hidden><ZapIcon size={15}/></span>{locale==="fa"?"سلامت اتوماسیون":"Automation health"}</h3><span className="dashboard-widget-action"><Link href="/automations">{locale==="fa"?"مشاهده همه":"View all"}</Link></span></header>{state.loading?<Skeleton height={96}/>:state.error?<ErrorState title="Unavailable" description={state.error}/>:total===0?<EmptyState title={locale==="fa"?"اجرایی ثبت نشده":"No executions yet"} description={locale==="fa"?"پس از اجرای فلوها نتایج اینجا نمایش داده می‌شود.":"Results appear here once flows run."}/>:<div className="health-ring-row"><div className="health-ring" style={{backgroundImage:ring}}><div className="health-ring-inner"><strong>{pct}%</strong><span>{locale==="fa"?"موفق":"Healthy"}</span></div></div><ul className="health-legend"><li><CheckCircleIcon size={14} className="tone-success"/><span>{locale==="fa"?"موفق":"Succeeded"}</span><b>{succeeded}</b></li><li><ClockIcon size={14} className="tone-warning"/><span>{locale==="fa"?"در حال اجرا":"Running"}</span><b>{running}</b></li><li><XCircleIcon size={14} className="tone-danger"/><span>{locale==="fa"?"ناموفق":"Failed"}</span><b>{failed}</b></li></ul></div>}</section>;
}

function formatLimitValue(key: string, value: number): string {
  if (/byte/i.test(key)) {
    const units = ["B", "KB", "MB", "GB", "TB"];
    let n = value, i = 0;
    while (n >= 1024 && i < units.length - 1) { n /= 1024; i++; }
    return `${n >= 100 ? Math.round(n) : Math.round(n * 10) / 10} ${units[i]}`;
  }
  return value >= 1000 ? `${Math.round(value / 100) / 10}K` : String(value);
}

function PlanLimitsWidget({ locale, subscription, loading }: { locale: "en" | "fa"; subscription: Subscription | null; loading: boolean }) {
  const limits = subscription?.limits ?? {};
  const usage = subscription?.usage ?? {};
  const entries = Object.entries(limits);
  const empty = entries.length === 0;
  return <Widget title={locale === "fa" ? "محدودیت‌های پلن" : "Plan limits"} icon={CardIcon} state={{ data: entries, loading, error: null, updatedAt: null }} action={<Link href="/billing">{locale === "fa" ? "مشاهده همه" : "View all"}</Link>}>
    {empty ? null : entries.map(([key, rawLimit]) => {
      const limitNumber = typeof rawLimit === "number" ? rawLimit : Number(rawLimit);
      const used = usage[key];
      if (typeof used === "number" && Number.isFinite(limitNumber) && limitNumber > 0) {
        const pct = Math.min(100, Math.round((used / limitNumber) * 100));
        return <div key={key} className="limit-usage-row"><div className="limit-usage-head"><strong>{key}</strong><span>{formatLimitValue(key, used)} / {formatLimitValue(key, limitNumber)}</span></div><div className="limit-usage-track"><div className="limit-usage-fill" style={{ width: `${pct}%`, background: pct >= 90 ? "var(--danger)" : pct >= 70 ? "var(--warning)" : "var(--gradient-brand)" }} /></div></div>;
      }
      return <div key={key}><strong>{key}</strong><span>{String(rawLimit)}</span></div>;
    })}
  </Widget>;
}
