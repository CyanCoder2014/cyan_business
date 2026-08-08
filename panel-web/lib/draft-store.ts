import type { ProjectDraft } from "@/lib/types";

const storageKey = "Cyan-panel-drafts";

export function loadDrafts(): ProjectDraft[] {
  if (typeof window === "undefined") {
    return [];
  }
  try {
    const raw = window.localStorage.getItem(storageKey);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw) as ProjectDraft[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function saveDraft(draft: ProjectDraft): ProjectDraft[] {
  const drafts = loadDrafts();
  const nextDrafts = [draft, ...drafts.filter((item) => item.id !== draft.id)].slice(0, 20);
  if (typeof window !== "undefined") {
    window.localStorage.setItem(storageKey, JSON.stringify(nextDrafts));
  }
  return nextDrafts;
}
