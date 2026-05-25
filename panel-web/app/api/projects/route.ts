import { NextResponse } from "next/server";
import { listProjectDrafts, upsertProjectDraft } from "@/lib/project-registry";
import type { ProjectDraft } from "@/lib/types";

export async function GET() {
  const drafts = await listProjectDrafts();
  return NextResponse.json(drafts);
}

export async function POST(request: Request) {
  const draft = (await request.json()) as ProjectDraft;
  const drafts = await upsertProjectDraft(draft);
  return NextResponse.json(drafts, { status: 201 });
}
