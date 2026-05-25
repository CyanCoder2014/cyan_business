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
    Promise.all([
      listFlows({ tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => []),
      listActionMetadata({ tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => []),
      getConditionMetadata({ tenantKey: "tenant-demo", siteKey: "site-commerce" }).catch(() => null)
    ]).then(([flowItems, actionItems, conditionItems]) => {
      setFlows(flowItems);
      setActions(actionItems);
      setConditions(conditionItems);
    });
  }, []);

  const flow = flows[0] ?? fallbackFlow;

  async function saveCurrentFlow(activate: boolean) {
    setStatus(locale === "fa" ? "در حال ذخیره فلو..." : "Saving flow...");
    const draft = flow.states.length ? flow : fallbackFlow;
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
            {(actions.length ? actions.slice(0, 6) : fallbackActions).map((action) => (
              <div key={action.type} className="flow-item">
                <strong>{action.type}</strong>
                <span className="muted-block">{action.description}</span>
              </div>
            ))}
          </div>
        </aside>

        <section className="panel-card">
          <div className="toolbar-row">
            <span className="status-pill success">{locale === "fa" ? "پیش‌نویس" : "Draft"}</span>
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
            <div className="kanban-node">
              <strong>Draft</strong>
              <span className="muted-block">{locale === "fa" ? "فرم درخواست" : "PO request form"}</span>
            </div>
            <div className="kanban-node green">
              <strong>Submitted</strong>
              <span className="muted-block">{locale === "fa" ? "اعتبارسنجی کامل" : "All required fields valid"}</span>
            </div>
            <div className="kanban-node violet">
              <strong>Review</strong>
              <span className="muted-block">{locale === "fa" ? "فرم بررسی" : "Review form"}</span>
            </div>
            <div className="kanban-node red">
              <strong>Rejected</strong>
              <span className="muted-block">{locale === "fa" ? "عدم تطابق با سیاست" : "Does not meet policy"}</span>
            </div>
            <div className="kanban-node green">
              <strong>Approved</strong>
              <span className="muted-block">{locale === "fa" ? "همه کنترل‌ها موفق" : "All checks passed"}</span>
            </div>
            <div className="kanban-node">
              <strong>Completed</strong>
              <span className="muted-block">{locale === "fa" ? "بدون فرم" : "No form"}</span>
            </div>
          </div>
          <div className="data-table-shell" style={{ marginTop: 18 }}>
            <div className="card-title-row">
              <h3>{locale === "fa" ? "وضعیت فلو" : "Flow metadata"}</h3>
            </div>
            <table className="data-table" style={{ marginTop: 12 }}>
              <tbody>
                <tr>
                  <th>{locale === "fa" ? "کلید فلو" : "Flow key"}</th>
                  <td>{flow.flowKey}</td>
                </tr>
                <tr>
                  <th>{locale === "fa" ? "نام" : "Name"}</th>
                  <td>{flow.name}</td>
                </tr>
                <tr>
                  <th>{locale === "fa" ? "استیت آغازین" : "Start state"}</th>
                  <td>{flow.startState}</td>
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
              <span className="muted-block">Review</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "اکشن‌ها" : "Actions"}</strong>
              <span className="muted-block">Notify reviewer, create audit record</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "رویدادها" : "Events"}</strong>
              <span className="muted-block">review.completed, review.rejected</span>
            </div>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "قوانین انتقال" : "Transition rules"}</strong>
              <span className="muted-block">{locale === "fa" ? "Approve → Approved / Reject → Rejected" : "Approve → Approved / Reject → Rejected"}</span>
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
          {[
            ["Draft", locale === "fa" ? "درخواست ایجاد شد" : "PO is created"],
            ["Submitted", locale === "fa" ? "ارسال برای تایید" : "Sent for approval"],
            ["Review", locale === "fa" ? "بررسی مدیر" : "Manager review"],
            ["Approved", locale === "fa" ? "تایید نهایی" : "PO approved"],
            ["Rejected", locale === "fa" ? "رد درخواست" : "PO rejected"],
            ["Completed", locale === "fa" ? "پایان فرآیند" : "PO process complete"]
          ].map(([title, meta]) => (
            <div key={title} className="flow-mobile-node">
              <strong>{title}</strong>
              <span className="muted-block">{meta}</span>
            </div>
          ))}
        </div>
        <div className="mobile-bottom-sheet">
          <div className="mobile-handle" />
          <div className="toolbar-row">
            <strong>{locale === "fa" ? "Review" : "Review"}</strong>
            <button type="button" className="icon-pill">×</button>
          </div>
          <div className="mobile-list" style={{ marginTop: 14 }}>
            <div className="mobile-list-item">
              <strong>{locale === "fa" ? "تایید" : "Approve"}</strong>
              <span className="muted-block">{locale === "fa" ? "همه شروط برقرار است → Approved" : "All conditions met → Approved"}</span>
            </div>
            <div className="mobile-list-item">
              <strong>{locale === "fa" ? "رد" : "Reject"}</strong>
              <span className="muted-block">{locale === "fa" ? "همیشه در دسترس → Rejected" : "Always available → Rejected"}</span>
            </div>
          </div>
        </div>
      </div>
    </PanelShell>
  );
}

const fallbackFlow: DynamicFlowDefinition = {
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

const fallbackActions: BpmActionStructure[] = [
  { type: "FORM_STEP", description: "Collect data with a form" },
  { type: "APPROVAL", description: "Human approval step" },
  { type: "NOTIFICATION", description: "Send email, in-app, or SMS" },
  { type: "WEBHOOK", description: "Call external endpoint" },
  { type: "AI_ACTION", description: "AI decision or generation" }
];
