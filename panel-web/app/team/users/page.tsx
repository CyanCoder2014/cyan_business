import { PanelShell } from "@/components/panel-shell";
import { TeamConsole } from "@/components/iam/team-console";
export default function TeamUsersPage(){return <PanelShell activeKey="team-users" kicker="Access" kickerFa="دسترسی" title="Team members" titleFa="اعضای تیم" subtitle="Provision identities, assign bounded tenant roles, and suspend access." subtitleFa="هویت بسازید، نقش‌های محدود تخصیص دهید و دسترسی را مدیریت کنید."><TeamConsole view="users"/></PanelShell>}
