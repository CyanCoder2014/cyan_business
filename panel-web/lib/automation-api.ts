import { platformFetch } from "@/lib/platform-auth";
import { platformErrorFromResponse } from "@/lib/api-error";

const base = "/api/platform/service/automation-orchestrator-service";
export type AutomationNodeType = string;
export type AutomationNode = { id:string; type:AutomationNodeType; name:string; enabled:boolean; credentialRef?:string|null; retryPolicy?:{maxAttempts?:number;backoffMs?:number;strategy?:string}; timeoutPolicy?:{connectTimeoutMs?:number;readTimeoutMs?:number}; errorPolicy?:{continueOnFail?:boolean;deadLetterOnFailure?:boolean;fallbackNodeId?:string}; concurrencyPolicy?:{keyExpression?:string;maxConcurrency?:number}; config:Record<string,unknown>; position?:{x?:number;y?:number}; data?:unknown };
export type AutomationEdge = { id:string; fromNodeId:string; fromPort?:string|null; toNodeId:string; toPort?:string|null };
export type AutomationFlow = { id?:string; revision?:number; flowKey:string; version:number; name:string; active:boolean; entryNodeId:string; runtimeMode:"VARIABLES"|"N8N_ITEMS"; nodes:AutomationNode[]; edges:AutomationEdge[]; inputsSchema:Record<string,unknown>; outputsSchema:Record<string,unknown>; labels:string[]; environment:string; lifecycleStatus:string; requiredRoles:string[]; settings:Record<string,unknown>; pinData:Record<string,unknown>; errorWorkflowKey?:string|null; nextScheduledAt?:string|null; lastScheduledAt?:string|null; updatedAt?:string };
export type AutomationExecution = { executionId:string; automationFlowKey?:string; flowVersion?:number; status:string; currentNodeId?:string; input?:Record<string,unknown>; output?:Record<string,unknown>; error?:Record<string,unknown>; steps?:Array<Record<string,unknown>>; deadLetters?:Array<Record<string,unknown>>; createdAt?:string; updatedAt?:string; completedAt?:string };
export type AutomationNodeMetadata = { type:string; commonFields:string[]; configFields:string[]; category?:string; label?:string; description?:string };
export type CredentialReference = { id:string; name:string; type:string; active:boolean; updatedAt?:string };
export type AutomationScope = { tenantKey?: string; siteKey?: string };

async function json<T>(path:string, init:RequestInit = {}, scope:AutomationScope = {}):Promise<T>{
  const response=await platformFetch(`${base}${path}`,{...init,headers:{"Content-Type":"application/json",...(scope.tenantKey?{"X-Tenant-Key":scope.tenantKey}:{}),...(scope.siteKey?{"X-Site-Key":scope.siteKey}:{}),...(init.headers??{})},cache:"no-store"});
  if(!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<T>;
}
export const listAutomationFlows=(scope:AutomationScope={})=>json<AutomationFlow[]>("/endpoint/automation-flows",{},scope);
export const getAutomationFlow=(flowKey:string,version:number,scope:AutomationScope={})=>json<AutomationFlow>(`/endpoint/automation-flows/${encodeURIComponent(flowKey)}/versions/${version}`,{},scope);
export const getActiveAutomationFlow=(flowKey:string,scope:AutomationScope={})=>json<AutomationFlow>(`/endpoint/automation-flows/${encodeURIComponent(flowKey)}/active`,{},scope);
export const saveAutomationFlow=(flow:AutomationFlow,scope:AutomationScope={})=>json<AutomationFlow>("/endpoint/automation-flows",{method:"POST",body:JSON.stringify(flow)},scope);
export const automationLifecycle=(flowKey:string,version:number,action:"SUBMIT"|"APPROVE"|"ACTIVATE",scope:AutomationScope={})=>json<AutomationFlow>(`/endpoint/automation-flows/${encodeURIComponent(flowKey)}/versions/${version}/${action}`,{method:"POST",body:"{}"},scope);
export const listAutomationNodeMetadata=()=>json<AutomationNodeMetadata[]>("/public/automation-flows/node-structures");
export const listCredentials=(scope:AutomationScope={})=>json<CredentialReference[]>("/endpoint/automation-orchestrator/credentials",{},scope);
export const listExecutions=(flowKey?:string,status?:string,scope:AutomationScope={})=>{const q=new URLSearchParams();if(flowKey)q.set("flowKey",flowKey);if(status)q.set("status",status);return json<AutomationExecution[]>(`/endpoint/automation-orchestrator/executions${q.size?`?${q}`:""}`,{},scope)};
export const getExecution=(id:string,scope:AutomationScope={})=>json<AutomationExecution>(`/endpoint/automation-orchestrator/executions/${encodeURIComponent(id)}`,{},scope);
export const startManualExecution=(flowKey:string,input:Record<string,unknown>,scope:AutomationScope={})=>json<AutomationExecution>(`/endpoint/automation-orchestrator/flows/${encodeURIComponent(flowKey)}/manual-run`,{method:"POST",headers:{"Idempotency-Key":crypto.randomUUID()},body:JSON.stringify(input)},scope);
export const cancelExecution=(id:string,scope:AutomationScope={})=>json<AutomationExecution>(`/endpoint/automation-orchestrator/executions/${encodeURIComponent(id)}/cancel`,{method:"POST",body:"{}"},scope);
export const retryExecution=(id:string,scope:AutomationScope={})=>json<AutomationExecution>(`/endpoint/automation-orchestrator/executions/${encodeURIComponent(id)}/retry?fromFailedNode=true`,{method:"POST",headers:{"Idempotency-Key":crypto.randomUUID()},body:"{}"},scope);
export const analyzeN8n=(workflow:unknown,scope:AutomationScope={})=>json<Record<string,unknown>>("/endpoint/automation-flows/n8n/analyze",{method:"POST",body:JSON.stringify(workflow)},scope);
export const importN8n=(workflow:unknown,flowKey?:string,scope:AutomationScope={})=>json<AutomationFlow>(`/endpoint/automation-flows/n8n/import${flowKey?`?flowKey=${encodeURIComponent(flowKey)}`:""}`,{method:"POST",body:JSON.stringify(workflow)},scope);
export const exportN8n=(flowKey:string,version:number,scope:AutomationScope={})=>json<Record<string,unknown>>(`/endpoint/automation-flows/${encodeURIComponent(flowKey)}/versions/${version}/n8n-export`,{},scope);
