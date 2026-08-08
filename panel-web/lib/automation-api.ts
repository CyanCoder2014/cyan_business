import { platformFetch } from "@/lib/platform-auth";

const base = "/api/platform/service/automation-orchestrator-service";
export type AutomationNodeType = string;
export type AutomationNode = { id:string; type:AutomationNodeType; name:string; enabled:boolean; credentialRef?:string|null; retryPolicy?:{maxAttempts?:number;backoffMs?:number;strategy?:string}; timeoutPolicy?:{connectTimeoutMs?:number;readTimeoutMs?:number}; errorPolicy?:{continueOnFail?:boolean;deadLetterOnFailure?:boolean;fallbackNodeId?:string}; concurrencyPolicy?:{keyExpression?:string;maxConcurrency?:number}; config:Record<string,unknown>; position?:{x?:number;y?:number}; data?:unknown };
export type AutomationEdge = { id:string; fromNodeId:string; fromPort?:string|null; toNodeId:string; toPort?:string|null };
export type AutomationFlow = { id?:string; revision?:number; flowKey:string; version:number; name:string; active:boolean; entryNodeId:string; runtimeMode:"VARIABLES"|"N8N_ITEMS"; nodes:AutomationNode[]; edges:AutomationEdge[]; inputsSchema:Record<string,unknown>; outputsSchema:Record<string,unknown>; labels:string[]; environment:string; lifecycleStatus:string; requiredRoles:string[]; settings:Record<string,unknown>; pinData:Record<string,unknown>; errorWorkflowKey?:string|null; nextScheduledAt?:string|null; lastScheduledAt?:string|null; updatedAt?:string };
export type AutomationExecution = { executionId:string; automationFlowKey?:string; flowVersion?:number; status:string; currentNodeId?:string; input?:Record<string,unknown>; output?:Record<string,unknown>; error?:Record<string,unknown>; steps?:Array<Record<string,unknown>>; deadLetters?:Array<Record<string,unknown>>; createdAt?:string; updatedAt?:string; completedAt?:string };
export type AutomationNodeMetadata = { type:string; commonFields:string[]; configFields:string[]; category?:string; label?:string };
export type CredentialReference = { id:string; name:string; type:string; active:boolean; updatedAt?:string };

async function json<T>(path:string, init:RequestInit = {}):Promise<T>{
  const response=await platformFetch(`${base}${path}`,{...init,headers:{"Content-Type":"application/json",...(init.headers??{})},cache:"no-store"});
  if(!response.ok) throw new Error((await response.json().catch(()=>null))?.message ?? `Request failed (${response.status})`);
  return response.json() as Promise<T>;
}
export const listAutomationFlows=()=>json<AutomationFlow[]>("/endpoint/automation-flows");
export const getAutomationFlow=(flowKey:string,version:number)=>json<AutomationFlow>(`/endpoint/automation-flows/${encodeURIComponent(flowKey)}/versions/${version}`);
export const getActiveAutomationFlow=(flowKey:string)=>json<AutomationFlow>(`/endpoint/automation-flows/${encodeURIComponent(flowKey)}/active`);
export const saveAutomationFlow=(flow:AutomationFlow)=>json<AutomationFlow>("/endpoint/automation-flows",{method:"POST",body:JSON.stringify(flow)});
export const automationLifecycle=(flowKey:string,version:number,action:"SUBMIT"|"APPROVE"|"ACTIVATE")=>json<AutomationFlow>(`/endpoint/automation-flows/${encodeURIComponent(flowKey)}/versions/${version}/${action}`,{method:"POST",body:"{}"});
export const listAutomationNodeMetadata=()=>json<AutomationNodeMetadata[]>("/public/automation-flows/node-structures");
export const listCredentials=()=>json<CredentialReference[]>("/endpoint/automation-orchestrator/credentials");
export const listExecutions=(flowKey?:string,status?:string)=>{const q=new URLSearchParams();if(flowKey)q.set("flowKey",flowKey);if(status)q.set("status",status);return json<AutomationExecution[]>(`/endpoint/automation-orchestrator/executions${q.size?`?${q}`:""}`)};
export const getExecution=(id:string)=>json<AutomationExecution>(`/endpoint/automation-orchestrator/executions/${encodeURIComponent(id)}`);
export const startManualExecution=(flowKey:string,input:Record<string,unknown>)=>json<AutomationExecution>(`/endpoint/automation-orchestrator/flows/${encodeURIComponent(flowKey)}/manual-run`,{method:"POST",headers:{"Idempotency-Key":crypto.randomUUID()},body:JSON.stringify(input)});
export const cancelExecution=(id:string)=>json<AutomationExecution>(`/endpoint/automation-orchestrator/executions/${encodeURIComponent(id)}/cancel`,{method:"POST",body:"{}"});
export const retryExecution=(id:string)=>json<AutomationExecution>(`/endpoint/automation-orchestrator/executions/${encodeURIComponent(id)}/retry?fromFailedNode=true`,{method:"POST",headers:{"Idempotency-Key":crypto.randomUUID()},body:"{}"});
export const analyzeN8n=(workflow:unknown)=>json<Record<string,unknown>>("/endpoint/automation-flows/n8n/analyze",{method:"POST",body:JSON.stringify(workflow)});
export const importN8n=(workflow:unknown,flowKey?:string)=>json<AutomationFlow>(`/endpoint/automation-flows/n8n/import${flowKey?`?flowKey=${encodeURIComponent(flowKey)}`:""}`,{method:"POST",body:JSON.stringify(workflow)});
export const exportN8n=(flowKey:string,version:number)=>json<Record<string,unknown>>(`/endpoint/automation-flows/${encodeURIComponent(flowKey)}/versions/${version}/n8n-export`);
