"use client";

import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { blueprintVisuals } from "@/lib/panel-fixtures";
import { listBlueprints, listClientDrafts } from "@/lib/platform-api";
import type { AppBlueprint, ClientAppDraft } from "@/lib/types";

export default function BlueprintsPage() {
  const { locale } = usePanel();
  const [blueprints, setBlueprints] = useState<AppBlueprint[]>([]);
  const [drafts, setDrafts] = useState<ClientAppDraft[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);

  useEffect(() => {
    Promise.all([listBlueprints().catch(() => []), listClientDrafts().catch(() => [])]).then(([items, draftItems]) => {
      setBlueprints(items);
      setDrafts(draftItems);
    });
  }, []);

  const cards = blueprints.length ? blueprints : fallbackBlueprints();
  const selected = cards[selectedIndex] ?? cards[0];

  return (
    <PanelShell
      activeKey="blueprints"
      title="Blueprints"
      titleFa="قالب‌ها"
      subtitle="Start from ready blueprints or saved drafts, then generate production-ready apps with the right service mix."
      subtitleFa="از قالب‌های آماده یا پیش‌نویس‌های ذخیره‌شده شروع کنید و با ترکیب درست سرویس‌ها، اپ‌های آماده انتشار بسازید."
    >
      <div className="page-grid">
        <section className="panel-card">
          <div className="toolbar-row">
            <input
              aria-label="Search blueprints"
              placeholder={locale === "fa" ? "جستجوی قالب‌ها..." : "Search blueprints..."}
              defaultValue=""
            />
            <div className="pill-row">
              <span className="pill">{locale === "fa" ? "همه" : "All"}</span>
              <span className="pill">CRM</span>
              <span className="pill">Shop</span>
              <span className="pill">PWA</span>
            </div>
          </div>

          <div className="blueprint-grid" style={{ marginTop: 18 }}>
            {cards.slice(0, 6).map((card, index) => (
              <button
                type="button"
                key={card.blueprintKey}
                className="blueprint-card"
                onClick={() => setSelectedIndex(index)}
                style={{
                  textAlign: "start",
                  borderColor: index === selectedIndex ? "rgba(127, 72, 255, 0.28)" : undefined
                }}
              >
                <div
                  style={{
                    height: 116,
                    borderRadius: 20,
                    background: backgroundByIndex(index),
                    marginBottom: 14
                  }}
                />
                <strong>{card.title}</strong>
                <div className="muted-block">{card.description}</div>
                <div className="toolbar-row" style={{ marginTop: 14 }}>
                  <span className="muted">{card.capabilities?.length ?? 0} {locale === "fa" ? "قابلیت" : "capabilities"}</span>
                  <span className="status-pill success">{locale === "fa" ? "آماده" : "Ready"}</span>
                </div>
              </button>
            ))}
          </div>

          <div className="data-table-shell" style={{ marginTop: 18 }}>
            <div className="card-title-row">
              <h3>{locale === "fa" ? "پیش‌نویس‌های ذخیره‌شده" : "Saved drafts"}</h3>
            </div>
            <table className="data-table" style={{ marginTop: 12 }}>
              <thead>
                <tr>
                  <th>{locale === "fa" ? "نام" : "Name"}</th>
                  <th>{locale === "fa" ? "قالب" : "Blueprint"}</th>
                  <th>{locale === "fa" ? "وضعیت" : "Status"}</th>
                  <th>{locale === "fa" ? "به‌روزرسانی" : "Updated"}</th>
                </tr>
              </thead>
              <tbody>
                {(drafts.length ? drafts : fallbackDraftRows(locale)).slice(0, 4).map((draft) => (
                  <tr key={draft.title}>
                    <td>{draft.title}</td>
                    <td>{draft.blueprintKey ?? draft.appType}</td>
                    <td>{draft.status}</td>
                    <td>{draft.updatedAt ?? (locale === "fa" ? "همین حالا" : "Just now")}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{selected.title}</h3>
            <span className="status-pill info">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
          </div>
          <p className="muted">{selected.description}</p>
          <div className="activity-list" style={{ marginTop: 16 }}>
            {(selected.capabilities?.length ? selected.capabilities : ["auth", "users", "contacts", "reports"]).map((capability) => (
              <div key={capability} className="detail-item">
                <strong>{capability}</strong>
                <span className="muted-block">{locale === "fa" ? "فعال" : "Included"}</span>
              </div>
            ))}
          </div>
          <div className="card-title-row" style={{ marginTop: 20 }}>
            <h3>{locale === "fa" ? "پیش‌نمایش DSL" : "Draft DSL preview"}</h3>
          </div>
          <pre className="code-block" style={{ marginTop: 12 }}>
{`blueprint: ${selected.blueprintKey}
version: ${selected.version ?? 1}
appType: ${selected.appType}
capabilities:
${(selected.capabilities ?? ["website", "shop", "crm"]).map((item) => `- ${item}`).join("\n")}`}
          </pre>
        </aside>
      </div>
    </PanelShell>
  );
}

function fallbackBlueprints(): AppBlueprint[] {
  return blueprintVisuals.map((item, index) => ({
    blueprintKey: item.key,
    appType: item.key.toUpperCase(),
    version: 1,
    title: ["Starter Website", "Online Shop", "Sales CRM", "Approval Workflow", "Support Bot", "Portfolio PWA"][index] ?? item.key,
    description: "Reference-aligned starter configuration with service-owned templates and generated delivery paths.",
    active: true,
    capabilities: [item.key, "automation", "analytics"],
    requiredQuestions: [],
    defaultAnswers: {},
    baseDsl: {
      app: {},
      entities: [],
      routes: [],
      flows: [],
      delivery: { publicApis: [], botApis: [] },
      manualActions: []
    }
  }));
}

function backgroundByIndex(index: number) {
  const item = blueprintVisuals[index % blueprintVisuals.length];
  if (item.hue === "blue") {
    return "linear-gradient(135deg, rgba(47,133,255,0.18), rgba(93,208,255,0.18))";
  }
  if (item.hue === "green") {
    return "linear-gradient(135deg, rgba(41,190,114,0.18), rgba(180,240,204,0.18))";
  }
  if (item.hue === "amber") {
    return "linear-gradient(135deg, rgba(255,158,55,0.18), rgba(255,214,136,0.18))";
  }
  return "linear-gradient(135deg, rgba(127,72,255,0.18), rgba(215,182,255,0.18))";
}

function fallbackDraftRows(locale: "en" | "fa") {
  return [
    {
      title: locale === "fa" ? "CRM فروش" : "Sales CRM Custom",
      blueprintKey: "sales_crm",
      appType: "CRM",
      status: "DRAFT",
      updatedAt: locale === "fa" ? "همین حالا" : "Just now"
    },
    {
      title: locale === "fa" ? "فلو تایید" : "Approval Flow - HR",
      blueprintKey: "approval_workflow",
      appType: "BPM",
      status: "READY",
      updatedAt: locale === "fa" ? "۲ ساعت پیش" : "2 hours ago"
    }
  ] as Array<Pick<ClientAppDraft, "title" | "blueprintKey" | "appType" | "status" | "updatedAt">>;
}
