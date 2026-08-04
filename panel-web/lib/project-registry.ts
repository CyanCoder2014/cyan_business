import type { ClientAppDraft, ProjectDraft } from "@/lib/types";
import { withServiceInventory } from "@/lib/platform-service-inventory";
const platformBaseUrl = process.env.AI_ORCHESTRATOR_SERVICE_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:9121";

function mapClientDraft(draft: ClientAppDraft): ProjectDraft {
  return {
    id: draft.draftId,
    title: draft.title,
    prompt: draft.latestIntent,
    tenantKey: draft.tenantKey,
    siteKey: draft.siteKey,
    updatedAt: draft.updatedAt ?? new Date().toISOString(),
    status: draft.status === "PROVISIONED" ? "PROVISIONED" : draft.status === "READY" ? "READY" : "DRAFT",
    dsl: draft.resolvedDsl,
    nextQuestions: draft.pendingQuestions ?? [],
    provisioningResult: null
  };
}

async function requestPlatformJson<T>(pathName: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${platformBaseUrl}${pathName}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    },
    cache: "no-store"
  });

  if (!response.ok) {
    const body = await response.text().catch(() => "");
    throw new Error(body || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

async function listBackendDrafts(): Promise<ProjectDraft[]> {
  const drafts = await requestPlatformJson<ClientAppDraft[]>("/endpoint/ai-orchestrator/drafts");
  return drafts.map(mapClientDraft);
}

async function getBackendDraft(projectId: string): Promise<ProjectDraft | null> {
  try {
    const draft = await requestPlatformJson<ClientAppDraft>(`/endpoint/ai-orchestrator/drafts/${projectId}`);
    return mapClientDraft(draft);
  } catch {
    return null;
  }
}

async function upsertBackendDraft(draft: ProjectDraft): Promise<ProjectDraft | null> {
  try {
    const existing = await getBackendDraft(draft.id);
    if (existing) {
      const updated = await requestPlatformJson<ClientAppDraft>(`/endpoint/ai-orchestrator/drafts/${draft.id}`, {
        method: "PATCH",
        body: JSON.stringify(withServiceInventory({
          prompt: draft.prompt,
          title: draft.title,
          answersPatch: {
            generatedDsl: draft.dsl,
            nextQuestions: draft.nextQuestions
          }
        }))
      });
      return mapClientDraft(updated);
    }

    const created = await requestPlatformJson<ClientAppDraft>("/endpoint/ai-orchestrator/drafts", {
      method: "POST",
      body: JSON.stringify(withServiceInventory({
        appType: draft.dsl.app.type,
        tenantKey: draft.tenantKey,
        siteKey: draft.siteKey,
        title: draft.title,
        prompt: draft.prompt,
        answers: {
          generatedDsl: draft.dsl,
          nextQuestions: draft.nextQuestions
        }
      }))
    });
    return mapClientDraft(created);
  } catch {
    return null;
  }
}

export async function listProjectDrafts(): Promise<ProjectDraft[]> {
  return listBackendDrafts();
}

export async function getProjectDraft(projectId: string): Promise<ProjectDraft | null> {
  return getBackendDraft(projectId);
}

export async function upsertProjectDraft(draft: ProjectDraft): Promise<ProjectDraft[]> {
  const backendDraft = await upsertBackendDraft(draft);
  if (!backendDraft) {
    throw new Error("Draft could not be saved in ai-orchestrator-service");
  }
  return listProjectDrafts();
}
