import { promises as fs } from "fs";
import path from "path";
import type { ProjectDraft } from "@/lib/types";
import { seedDrafts } from "@/lib/draft-store";

const registryPath = path.join(process.cwd(), "data", "project-drafts.json");

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

export async function listProjectDrafts(): Promise<ProjectDraft[]> {
  const stored = await readRegistryFile();
  if (stored.length > 0) {
    return stored;
  }
  const seeded = seedDrafts();
  await writeRegistryFile(seeded);
  return seeded;
}

export async function getProjectDraft(projectId: string): Promise<ProjectDraft | null> {
  const drafts = await listProjectDrafts();
  return drafts.find((draft) => draft.id === projectId) ?? null;
}

export async function upsertProjectDraft(draft: ProjectDraft): Promise<ProjectDraft[]> {
  const drafts = await listProjectDrafts();
  const nextDrafts = [draft, ...drafts.filter((item) => item.id !== draft.id)];
  await writeRegistryFile(nextDrafts);
  return nextDrafts;
}
