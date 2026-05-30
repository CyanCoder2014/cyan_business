"use client";

import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { activateFlow, getConditionMetadata, listActionMetadata, listFlows, saveFlow, type BpmActionStructure, type BpmConditionStructure, type DynamicFlowDefinition } from "@/lib/bpm-api";

export default function FlowsPage() {
  const { locale } = usePanel();
  const [flows, setFlows] = useState<DynamicFlowDefinition[]>([]);
  const [actions, setActions] = useState<BpmActionStructure[]>([]);
  const [conditions, setConditions] = useState<BpmConditionStructure | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    Promise.allSettled([
      listFlows({ tenantKey: "tenant-demo", siteKey: "site-commerce" }),
      listActionMetadata({ tenantKey: "tenant-demo", siteKey: "site-commerce" }),
      getConditionMetadata({ tenantKey: "tenant-demo", siteKey: "site-commerce" })
    ]).then(([flowItems, actionItems, conditionItems]) => {
      const errors: string[] = [];
      if (flowItems.status === "fulfilled") {
        setFlows(flowItems.value);
      } else {
        errors.push(locale === "fa" ? "فلوها بارگیری نشدند." : "Flows could not be loaded.");
      }
      if (actionItems.status === "fulfilled") {
        setActions(actionItems.value);
      } else {
        errors.push(locale === "fa" ? "متادیتای اکشن‌ها بارگیری نشد." : "Action metadata could not be loaded.");
      }
      if (conditionItems.status === "fulfilled") {
        setConditions(conditionItems.value);
      } else {
        errors.push(locale === "fa" ? "متادیتای شرط‌ها بارگیری نشد." : "Condition metadata could not be loaded.");
      }
      setStatus(errors.length ? errors.join(" ") : null);
    });
  }, [locale]);

  const flow = flows[0] ?? null;

  async function saveCurrentFlow(activate: boolean) {
    setStatus(locale === "fa" ? "در حال ذخیره فلو..." : "Saving flow...");
    const draft = flow ?? createStarterFlow();
    try {
      const saved = await saveFlow(draft, { tenantKey: "tenant-demo", siteKey: "site-commerce" });
      if (activate) {
        await activateFlow(saved.flowKey, saved.version ?? 1, { tenantKey: "tenant-demo", siteKey: "site-commerce" });
      }
      setFlows((current) => {
        const next = current.filter((item) => item.flowKey !== saved.flowKey);
        return [saved, ...next];
      });
      setStatus(activate ? (locale === "fa" ? "فلو منتشر شد." : "Flow published.") : locale === "fa" ? "پیش‌نویس فلو ذخیره شد." : "Flow draft saved.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ذخیره فلو ناموفق بود." : "Flow save failed.");
    }
  }

  return (
    <PanelShell
      activeKey="flows"
      title="Flow Builder"
      titleFa="فلوساز"
      subtitle="Design BPM workflows with forms, rules, approvals, and automation handoffs."
      subtitleFa="فلوهای BPM را همراه با فرم، قوانین، تاییدها و هندآف‌های اتوماسیون طراحی کنید."
    >
      <div className="desktop-only flow-grid">
        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "افزودن نود" : "Add node"}</h3>
          </div>
          <div className="flow-list" style={{ marginTop: 16 }}>
            {actions.slice(0, 6).map((action) => (
              <div key={action.type} className="flow-item">
                <strong>{action.type}</strong>
                <span className="muted-block">{action.description}</span>
              </div>
            ))}
            {!actions.length ? (
              <div className="flow-item">
                <strong>{locale === "fa" ? "اکشنی دریافت نشد" : "No actions returned"}</strong>
                <span className="muted-block">{locale === "fa" ? "متادیتای BPM باید این لیست را تامین کند." : "The BPM metadata endpoint should populate this list."}</span>
              </div>
            ) : null}
          </div>
        </aside>

        <section className="panel-card">
          <div className="toolbar-row">
            <span className={flow ? "status-pill success" : "status-pill warning"}>{flow ? (locale === "fa" ? "بارگیری شد" : "Loaded") : locale === "fa" ? "خالی" : "Empty"}</span>
            <div className="pill-row">
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "تست ارسال" : "Test submission"}
              </button>
              <button type="button" className="secondary-pill" onClick={() => saveCurrentFlow(false)}>
                {locale === "fa" ? "ذخیره" : "Save draft"}
              </button>
              <button type="button" className="primary-pill" onClick={() => saveCurrentFlow(true)}>
                {locale === "fa" ? "انتشار فلو" : "Publish flow"}
              </button>
            </div>
          </div>
          {status ? <div className="status-pill info" style={{ marginTop: 12 }}>{status}</div> : null}
          <div className="flow-canvas flow-canvas-wide" style={{ marginTop: 18 }}>
            {flow ? flow.states.map((state) => (
              <div key={state.id} className={`kanban-node${state.terminal ? " green" : ""}`}>
                <strong>{state.displayName}</strong>
                <span className="muted-block">{state.formKey ?? (locale === "fa" ? "بدون فرم" : "No form")}</span>
              </div>
            )) : (
              <div className="mini-card" style={{ width: "100%" }}>
                <strong>{locale === "fa" ? "هیچ فلویی از API برنگشته است" : "No flow was returned by the API"}</strong>
                <span className="muted-block">{locale === "fa" ? "برای ایجاد فلو اولیه می‌توانید Save draft را بزنید." : "Use Save draft to create a starter flow if needed."}</span>
              </div>
            )}
          </div>
          <div className="data-table-shell" style={{ marginTop: 18 }}>
            <div className="card-title-row">
              <h3>{locale === "fa" ? "وضعیت فلو" : "Flow metadata"}</h3>
            </div>
            <table className="data-table" style={{ marginTop: 12 }}>
              <tbody>
                <tr>
                  <th>{locale === "fa" ? "کلید فلو" : "Flow key"}</th>
                  <td>{flow?.flowKey ?? "—"}</td>
                </tr>
                <tr>
                  <th>{locale === "fa" ? "نام" : "Name"}</th>
                  <td>{flow?.name ?? "—"}</td>
                </tr>
                <tr>
                  <th>{locale === "fa" ? "استیت آغازین" : "Start state"}</th>
                  <td>{flow?.startState ?? "—"}</td>
                </tr>
                <tr>
                  <th>{locale === "fa" ? "عملگرهای شرط" : "Condition operators"}</th>
                  <td>{conditions?.operators?.join(", ") ?? "EQ, GT, LT, CONTAINS"}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div className="summary-grid" style={{ marginTop: 18 }}>
            <div className="mini-card">
              <strong>{locale === "fa" ? "گزارش فعالیت" : "Activity log"}</strong>
              <span className="muted-block">May 20, 2025 10:16 AM</span>
            </div>
            <div className="mini-card">
              <strong>{locale === "fa" ? "تست فلو" : "Test this flow"}</strong>
              <span className="muted-block">{locale === "fa" ? "شبیه‌سازی ارسال" : "Simulate a submission"}</span>
            </div>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "نود انتخاب‌شده" : "Selected node"}</h3>
          </div>
          <div className="pill-row" style={{ marginTop: 12 }}>
            <span className="pill status-pill info">{locale === "fa" ? "پیکربندی" : "Configure"}</span>
            <span className="pill">{locale === "fa" ? "ترنزیشن‌ها" : "Transitions"}</span>
            <span className="pill">{locale === "fa" ? "اکشن‌ها" : "Actions"}</span>
            <span className="pill">{locale === "fa" ? "رویدادها" : "Events"}</span>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "عنوان" : "Title"}</strong>
              <span className="muted-block">{flow?.states[0]?.displayName ?? "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "اکشن‌ها" : "Actions"}</strong>
              <span className="muted-block">{actions.slice(0, 2).map((item) => item.type).join(", ") || "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "رویدادها" : "Events"}</strong>
              <span className="muted-block">{flow?.transitions.map((item) => item.label).join(", ") || "—"}</span>
            </div>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "قوانین انتقال" : "Transition rules"}</strong>
              <span className="muted-block">{flow?.transitions.length ? `${flow.transitions.length} ${locale === "fa" ? "ترنزیشن" : "transitions"}` : "—"}</span>
            </div>
          </div>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <button type="button" className="icon-pill">←</button>
          <div className="mobile-brand">
            <span className="tile-icon">✎</span>
            <strong style={{ fontSize: "2rem" }}>{locale === "fa" ? "فلوساز" : "Flow Builder"}</strong>
          </div>
          <span className="pill">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
        </div>
        <div className="mobile-tab-strip">
          <span className="status-pill info">{locale === "fa" ? "فلو" : "Flow"}</span>
          <span className="pill">{locale === "fa" ? "اکشن‌ها" : "Actions"}</span>
          <span className="pill">{locale === "fa" ? "رویدادها" : "Events"}</span>
        </div>
        <div className="flow-mobile-path">
          {(flow?.states ?? []).map((state) => (
            <div key={state.id} className="flow-mobile-node">
              <strong>{state.displayName}</strong>
              <span className="muted-block">{state.formKey ?? (locale === "fa" ? "بدون فرم" : "No form")}</span>
            </div>
          ))}
          {!flow ? (
            <div className="flow-mobile-node">
              <strong>{locale === "fa" ? "فلویی وجود ندارد" : "No flow available"}</strong>
              <span className="muted-block">{locale === "fa" ? "خروجی مستقیم از BPM نمایش داده می‌شود." : "This now renders directly from BPM output."}</span>
            </div>
          ) : null}
        </div>
        <div className="mobile-bottom-sheet">
          <div className="mobile-handle" />
          <div className="toolbar-row">
            <strong>{flow?.states[0]?.displayName ?? (locale === "fa" ? "بدون فلو" : "No flow")}</strong>
            <button type="button" className="icon-pill">×</button>
          </div>
          <div className="mobile-list" style={{ marginTop: 14 }}>
            {(flow?.transitions ?? []).slice(0, 2).map((transition) => (
              <div key={transition.id} className="mobile-list-item">
                <strong>{transition.label}</strong>
                <span className="muted-block">{transition.fromState} → {transition.toState}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </PanelShell>
  );
}

function createStarterFlow(): DynamicFlowDefinition {
  return {
    flowKey: "purchase_order_approval",
    name: "Purchase Order Approval",
    startState: "Draft",
    states: [
      { id: "Draft", displayName: "Draft", terminal: false, formKey: "po_request" },
      { id: "Submitted", displayName: "Submitted", terminal: false, formKey: "po_request" },
      { id: "Review", displayName: "Review", terminal: false, formKey: "review_form" },
      { id: "Approved", displayName: "Approved", terminal: false, formKey: "approval_form" },
      { id: "Rejected", displayName: "Rejected", terminal: true, formKey: "rejection_form" },
      { id: "Completed", displayName: "Completed", terminal: true }
    ],
    transitions: [
      { id: "submit", fromState: "Draft", toState: "Submitted", label: "Submit" },
      { id: "route-to-review", fromState: "Submitted", toState: "Review", label: "Route to review" },
      { id: "approve", fromState: "Review", toState: "Approved", label: "Approve" },
      { id: "reject", fromState: "Review", toState: "Rejected", label: "Reject" },
      { id: "complete", fromState: "Approved", toState: "Completed", label: "Auto-complete" }
    ]
  };
}
