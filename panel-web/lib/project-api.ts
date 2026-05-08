import type { ProjectDraft } from "@/lib/types";

async function requestJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
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

export function listProjects(): Promise<ProjectDraft[]> {
  return requestJson<ProjectDraft[]>("/api/projects");
}

export function getProject(projectId: string): Promise<ProjectDraft> {
  return requestJson<ProjectDraft>(`/api/projects/${projectId}`);
}

export function saveProject(draft: ProjectDraft): Promise<ProjectDraft[]> {
  return requestJson<ProjectDraft[]>("/api/projects", {
    method: "POST",
    body: JSON.stringify(draft)
  });
}

export function updateProject(draft: ProjectDraft): Promise<ProjectDraft[]> {
  return requestJson<ProjectDraft[]>(`/api/projects/${draft.id}`, {
    method: "PUT",
    body: JSON.stringify(draft)
  });
}
