import { PanelShell } from "@/components/panel-shell";
import { TeamConsole } from "@/components/iam/team-console";
export default function TeamRolesPage(){return <PanelShell activeKey="team-roles" kicker="Access" kickerFa="دسترسی" title="Roles & permissions" titleFa="نقش‌ها و مجوزها" subtitle="Create tenant roles without granting more access than you hold." subtitleFa="نقش‌های فضای کاری را بدون افزایش غیرمجاز دسترسی بسازید."><TeamConsole view="roles"/></PanelShell>}
