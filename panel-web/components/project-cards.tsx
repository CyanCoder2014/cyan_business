import Link from "next/link";
import type { ProjectDraft } from "@/lib/types";

type ProjectCardsProps = {
  drafts: ProjectDraft[];
};

export function ProjectCards({ drafts }: ProjectCardsProps) {
  return (
    <div className="mini-grid">
      {drafts.map((draft) => (
        <Link key={draft.id} href={`/projects/${draft.id}`} className="mini-card">
          <h3>{draft.title}</h3>
          <p>{draft.prompt}</p>
          <p style={{ marginTop: 14, color: "var(--cyan)" }}>
            {draft.status} | {draft.dsl.app.type}
          </p>
        </Link>
      ))}
    </div>
  );
}
