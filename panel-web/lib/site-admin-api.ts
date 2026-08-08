import {platformFetch} from "@/lib/platform-auth";
export type SiteSummary={tenantKey:string;siteKey:string;name:string;status:string;createdAt:string};
export type DomainBinding={id:number;tenantKey:string;siteKey:string;domainName:string;environment:string;verificationToken:string;verificationStatus:string;certificateStatus:string;redirectTarget?:string|null;lastCheckedAt?:string|null;createdAt:string;updatedAt:string};
export type DomainEvent={id:number;eventType:string;status:string;detail?:string;createdAt:string}; export type DnsInstruction={type:string;name:string;value:string};
async function call<T>(path:string,scope:{tenantKey:string;siteKey?:string},init:RequestInit={}):Promise<T>{const r=await platformFetch(`/api/platform/service/storefront-service${path}`,{...init,headers:{"Content-Type":"application/json","X-Tenant-Key":scope.tenantKey,...(scope.siteKey?{"X-Site-Key":scope.siteKey}:{}),...(init.headers??{})},cache:"no-store"});if(!r.ok)throw new Error((await r.text().catch(()=>""))||`Storefront request failed (${r.status})`);return r.status===204?undefined as T:r.json() as Promise<T>}
export const listSites=(tenantKey:string)=>call<SiteSummary[]>("/endpoint/sites",{tenantKey});
export const createSite=(tenantKey:string,request:{name:string;siteKey?:string})=>call<SiteSummary>("/endpoint/sites",{tenantKey},{method:"POST",headers:{"Idempotency-Key":crypto.randomUUID()},body:JSON.stringify(request)});
export const listDomains=(scope:{tenantKey:string;siteKey:string})=>call<DomainBinding[]>("/endpoint/domains",scope);
export const createDomain=(scope:{tenantKey:string;siteKey:string},request:{domainName:string;environment:string;redirectTarget?:string})=>call<DomainBinding>("/endpoint/domains",scope,{method:"POST",body:JSON.stringify(request)});
export const verifyDomain=(scope:{tenantKey:string;siteKey:string},id:number)=>call<DomainBinding>(`/endpoint/domains/${id}/verify`,scope,{method:"POST",body:"{}"});
export const domainInstructions=(scope:{tenantKey:string;siteKey:string},id:number)=>call<DnsInstruction[]>(`/endpoint/domains/${id}/dns-instructions`,scope);
export const domainHistory=(scope:{tenantKey:string;siteKey:string},id:number)=>call<DomainEvent[]>(`/endpoint/domains/${id}/history`,scope);
export const deleteDomain=(scope:{tenantKey:string;siteKey:string},id:number)=>call<void>(`/endpoint/domains/${id}`,scope,{method:"DELETE"});
