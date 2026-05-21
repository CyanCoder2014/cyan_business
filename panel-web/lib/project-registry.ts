import { promises as fs } from "fs";
import path from "path";
import type { ClientAppDraft, ProjectDraft } from "@/lib/types";
import { seedDrafts } from "@/lib/draft-store";

const registryPath = path.join(process.cwd(), "data", "project-drafts.json");
const platformBaseUrl = process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8001";

async function ensureDir() {
  await fs.mkdir(path.dirname(registryPath), { recursive: true });
}

async function readRegistryFile(): Promise<ProjectDraft[]> {
  try {
    const raw = await fs.readFile(registryPath, "utf8");
    const parsed = JSON.parse(raw) as ProjectDraft[];
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") {
      return [];
    }
    throw error;
  }
}

async function writeRegistryFile(drafts: ProjectDraft[]) {
  await ensureDir();
  await fs.writeFile(registryPath, `${JSON.stringify(drafts, null, 2)}\n`, "utf8");
}

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
        body: JSON.stringify({
          prompt: draft.prompt,
          title: draft.title,
          answersPatch: {
            generatedDsl: draft.dsl,
            nextQuestions: draft.nextQuestions
          }
        })
      });
      return mapClientDraft(updated);
    }

    const created = await requestPlatformJson<ClientAppDraft>("/endpoint/ai-orchestrator/drafts", {
      method: "POST",
      body: JSON.stringify({
        appType: draft.dsl.app.type,
        tenantKey: draft.tenantKey,
        siteKey: draft.siteKey,
        title: draft.title,
        prompt: draft.prompt,
        answers: {
          generatedDsl: draft.dsl,
          nextQuestions: draft.nextQuestions
        }
      })
    });
    return mapClientDraft(created);
  } catch {
    return null;
  }
}

export async function listProjectDrafts(): Promise<ProjectDraft[]> {
  try {
    const backendDrafts = await listBackendDrafts();
    if (backendDrafts.length > 0) {
      return backendDrafts;
    }
  } catch {
    // Local registry remains a development fallback when the orchestrator is not running.
  }

  const stored = await readRegistryFile();
  if (stored.length > 0) {
    return stored;
  }
  const seeded = seedDrafts();
  await writeRegistryFile(seeded);
  return seeded;
}

export async function getProjectDraft(projectId: string): Promise<ProjectDraft | null> {
  const backendDraft = await getBackendDraft(projectId);
  if (backendDraft) {
    return backendDraft;
  }
  const drafts = await listProjectDrafts();
  return drafts.find((draft) => draft.id === projectId) ?? null;
}

export async function upsertProjectDraft(draft: ProjectDraft): Promise<ProjectDraft[]> {
  const backendDraft = await upsertBackendDraft(draft);
  if (backendDraft) {
    return listProjectDrafts();
  }

  const drafts = await listProjectDrafts();
  const nextDrafts = [draft, ...drafts.filter((item) => item.id !== draft.id)];
  await writeRegistryFile(nextDrafts);
  return nextDrafts;
}
