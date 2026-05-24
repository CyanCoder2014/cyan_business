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
      <div className="desktop-only page-grid">
        <section className="panel-card">
          <div className="toolbar-row">
            <input
              aria-label="Search blueprints"
              placeholder={locale === "fa" ? "جستجوی قالب‌ها..." : "Search blueprints..."}
              defaultValue=""
            />
            <div className="pill-row">
              <span className="pill">{locale === "fa" ? "همه تگ‌ها" : "All tags"}</span>
              <span className="pill">{locale === "fa" ? "همه پیچیدگی‌ها" : "All complexity"}</span>
            </div>
          </div>
          <div className="pill-row" style={{ marginTop: 18 }}>
            <span className="pill status-pill info">{locale === "fa" ? "همه" : "All"}</span>
            <span className="pill">Website</span>
            <span className="pill">Shop</span>
            <span className="pill">CRM</span>
            <span className="pill">Forms</span>
            <span className="pill">BPM</span>
            <span className="pill">Bots</span>
            <span className="pill">PWA</span>
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
                <div className="blueprint-preview" style={{ background: backgroundByIndex(index), marginBottom: 14 }}>
                  <div className="blueprint-preview-card" />
                  <div className="blueprint-preview-card small" />
                  <div className="blueprint-preview-card tall" />
                </div>
                <strong>{card.title}</strong>
                <div className="muted-block">{card.description}</div>
                <div className="pill-row" style={{ marginTop: 10 }}>
                  <span className="pill">{card.appType}</span>
                  <span className="pill">{card.capabilities?.[0] ?? "automation"}</span>
                </div>
                <div className="toolbar-row" style={{ marginTop: 14 }}>
                  <span className="muted">{card.capabilities?.length ?? 0} {locale === "fa" ? "قابلیت" : "capabilities"}</span>
                  <span className="status-pill success">{locale === "fa" ? "آماده" : "Ready"}</span>
                </div>
                <div className="toolbar-row" style={{ marginTop: 14 }}>
                  <button type="button" className="secondary-pill">{locale === "fa" ? "پیش‌نمایش" : "Preview"}</button>
                  <button type="button" className="primary-pill">{locale === "fa" ? "استفاده از قالب" : "Use Blueprint"}</button>
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
          <div className="card-title-row" style={{ marginTop: 18 }}>
            <h3>{locale === "fa" ? "سرویس‌های موجود" : "Included services"}</h3>
          </div>
          <div className="blueprint-service-grid" style={{ marginTop: 16 }}>
            {(selected.capabilities?.length ? selected.capabilities : ["auth", "users", "contacts", "reports"]).map((capability) => (
              <div key={capability} className="detail-item blueprint-service-chip">
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
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "فضای کاری" : "Workspace"}</strong>
              <span className="muted-block">Acme Corp</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "سایت" : "Site"}</strong>
              <span className="muted-block">acme.cyan.app</span>
            </div>
          </div>
          <div className="toolbar-row" style={{ marginTop: 16 }}>
            <button type="button" className="primary-pill wide-pill">{locale === "fa" ? "تولید از روی قالب" : "Generate from blueprint"}</button>
          </div>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <div className="mobile-brand">
            <div className="brand-badge">C</div>
            <strong style={{ fontSize: "2rem" }}>Cyan</strong>
          </div>
        </div>
        <div>
          <h2 style={{ margin: 0, fontSize: "3rem" }}>{locale === "fa" ? "قالب‌ها" : "Blueprints"}</h2>
          <p className="muted">{locale === "fa" ? "با قالب‌ها سریع‌تر بسازید." : "Use templates to build faster."}</p>
        </div>
        <input placeholder={locale === "fa" ? "جستجوی قالب‌ها..." : "Search blueprints..."} />
        <div className="pill-row">
          <span className="status-pill info">{locale === "fa" ? "همه" : "All"}</span>
          <span className="pill">Shop</span>
          <span className="pill">CRM</span>
          <span className="pill">Bots</span>
        </div>
        <div className="mobile-grid">
          {cards.slice(0, 4).map((card, index) => (
            <div key={card.blueprintKey} className="mobile-card">
              <div style={{ height: 148, borderRadius: 22, background: backgroundByIndex(index), marginBottom: 14 }} />
              <strong style={{ fontSize: "1.7rem" }}>{card.title}</strong>
              <p className="muted">{card.description}</p>
              <div className="toolbar-row">
                <span className="pill">{card.capabilities?.length ?? 0} {locale === "fa" ? "سرویس" : "services"}</span>
                <span className={index % 2 === 0 ? "status-pill success" : "status-pill info"}>{index % 2 === 0 ? (locale === "fa" ? "آماده" : "Ready") : (locale === "fa" ? "نمونه" : "Seeded")}</span>
              </div>
            </div>
          ))}
        </div>
        <div className="mobile-bottom-sheet">
          <div className="mobile-handle" />
          <div className="toolbar-row">
            <div>
              <strong>{locale === "fa" ? "قالب انتخاب‌شده" : "Selected blueprint"}</strong>
              <span className="muted-block">{selected.title}</span>
            </div>
            <button type="button" className="primary-pill">
              {locale === "fa" ? "تولید از قالب" : "Generate from blueprint"}
            </button>
          </div>
        </div>
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
