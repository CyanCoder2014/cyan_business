import { NextResponse } from "next/server";
import { getProjectDraft, upsertProjectDraft } from "@/lib/project-registry";
import type { ProjectDraft } from "@/lib/types";

type RouteContext = {
  params: {
    projectId: string;
  };
};

export async function GET(_: Request, context: RouteContext) {
  const draft = await getProjectDraft(context.params.projectId);
  if (!draft) {
    return NextResponse.json({ message: "Project not found" }, { status: 404 });
  }
  return NextResponse.json(draft);
}

export async function PUT(request: Request, context: RouteContext) {
  const draft = (await request.json()) as ProjectDraft;
  if (draft.id !== context.params.projectId) {
    return NextResponse.json({ message: "Project id mismatch" }, { status: 400 });
  }
  const drafts = await upsertProjectDraft(draft);
  return NextResponse.json(drafts);
}
