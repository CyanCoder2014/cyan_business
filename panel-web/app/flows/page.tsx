"use client";

import { useEffect, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import {
  activateFlow,
  createManagedObject,
  getConditionMetadata,
  listActionMetadata,
  listAssignedManagedObjects,
  listFlows,
  listTransitionOptions,
  listVisibleManagedObjects,
  saveFlow,
  type BpmActionStructure,
  type BpmConditionStructure,
  type DynamicFlowDefinition,
  type ManagedObject,
  type TransitionOptionResponse
} from "@/lib/bpm-api";

const scope = { tenantKey: "tenant-demo", siteKey: "site-commerce" };

export default function FlowsPage() {
  const { locale } = usePanel();
  const [flows, setFlows] = useState<DynamicFlowDefinition[]>([]);
  const [actions, setActions] = useState<BpmActionStructure[]>([]);
  const [conditions, setConditions] = useState<BpmConditionStructure | null>(null);
  const [assignedObjects, setAssignedObjects] = useState<ManagedObject[]>([]);
  const [visibleObjects, setVisibleObjects] = useState<ManagedObject[]>([]);
  const [transitionOptions, setTransitionOptions] = useState<TransitionOptionResponse[]>([]);
  const [selectedFlowKey, setSelectedFlowKey] = useState<string | null>(null);
  const [selectedObjectId, setSelectedObjectId] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [reloadToken, setReloadToken] = useState(0);

  useEffect(() => {
    async function run() {
      const results = await Promise.allSettled([
        listFlows(scope),
        listActionMetadata(scope),
        getConditionMetadata(scope),
        listAssignedManagedObjects(scope),
        listVisibleManagedObjects(scope)
      ]);
      const [flowItems, actionItems, conditionItems, assignedItems, visibleItems] = results;
      const errors: string[] = [];

      if (flowItems.status === "fulfilled") {
        setFlows(flowItems.value);
        setSelectedFlowKey((current) => current ?? flowItems.value[0]?.flowKey ?? null);
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

      if (assignedItems.status === "fulfilled") {
        setAssignedObjects(assignedItems.value);
        setSelectedObjectId((current) => current ?? assignedItems.value[0]?.id ?? null);
      } else {
        errors.push(locale === "fa" ? "کارتابل اختصاص‌یافته بارگیری نشد." : "Assigned queue could not be loaded.");
      }

      if (visibleItems.status === "fulfilled") {
        setVisibleObjects(visibleItems.value);
        setSelectedObjectId((current) => current ?? visibleItems.value[0]?.id ?? null);
      } else {
        errors.push(locale === "fa" ? "صف قابل مشاهده بارگیری نشد." : "Visible queue could not be loaded.");
      }

      setStatus(errors.length ? errors.join(" ") : null);
    }

    run().catch((error) => {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "BPM بارگیری نشد." : "BPM workspace could not be loaded.");
    });
  }, [locale, reloadToken]);

  useEffect(() => {
    if (!selectedObjectId) {
      setTransitionOptions([]);
      return;
    }
    listTransitionOptions(selectedObjectId, scope)
      .then(setTransitionOptions)
      .catch(() => setTransitionOptions([]));
  }, [selectedObjectId]);

  const flow = flows.find((item) => item.flowKey === selectedFlowKey) ?? flows[0] ?? null;
  const selectedObject = assignedObjects.find((item) => item.id === selectedObjectId)
    ?? visibleObjects.find((item) => item.id === selectedObjectId)
    ?? assignedObjects[0]
    ?? visibleObjects[0]
    ?? null;
  const selectedState = flow?.states.find((state) => state.id === selectedObject?.state)
    ?? flow?.states[0]
    ?? null;
  const selectedAction = actions.find((item) => item.type === selectedState?.onEnterActions?.[0]?.type) ?? actions[0] ?? null;

  async function saveCurrentFlow(shouldActivate: boolean) {
    setStatus(locale === "fa" ? "در حال ذخیره فلو..." : "Saving flow...");
    const draft = flow ?? createStarterFlow();
    try {
      const saved = await saveFlow(draft, scope);
      if (shouldActivate) {
        await activateFlow(saved.flowKey, saved.version ?? 1, scope);
      }
      setSelectedFlowKey(saved.flowKey);
      setReloadToken((current) => current + 1);
      setStatus(shouldActivate ? (locale === "fa" ? "فلو منتشر شد." : "Flow published.") : locale === "fa" ? "پیش‌نویس ذخیره شد." : "Flow draft saved.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ذخیره فلو ناموفق بود." : "Flow save failed.");
    }
  }

  async function startManagedObject() {
    const draft = flow ?? createStarterFlow();
    setStatus(locale === "fa" ? "در حال ایجاد آبجکت..." : "Starting managed object...");
    try {
      const created = await createManagedObject({
        flowKey: draft.flowKey,
        objectType: draft.flowKey.toUpperCase().replace(/[^A-Z0-9]+/g, "_"),
        payload: {
          source: "panel-web",
          requestedAt: new Date().toISOString()
        }
      }, scope);
      setSelectedObjectId(created.id);
      setReloadToken((current) => current + 1);
      setStatus(locale === "fa" ? "آبجکت جدید BPM ایجاد شد." : "Managed object started.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ایجاد آبجکت ناموفق بود." : "Managed object creation failed.");
    }
  }

  return (
    <PanelShell
      activeKey="flows"
      title="Flow Builder"
      titleFa="فلوساز"
      subtitle="Design BPM workflows, inspect action metadata, and work the cartable from one panel."
      subtitleFa="فلوهای BPM را طراحی کنید، متادیتای اکشن‌ها را بررسی کنید و کارتابل را از یک پنل واحد مدیریت کنید."
    >
      <section className="desktop-only metric-grid">
        {[
          { label: locale === "fa" ? "همه فلوها" : "All flows", value: String(flows.length) },
          { label: locale === "fa" ? "فعال" : "Active", value: String(flows.filter((item) => item.active).length) },
          { label: locale === "fa" ? "اختصاص‌یافته به من" : "Assigned to me", value: String(assignedObjects.length) },
          { label: locale === "fa" ? "قابل مشاهده" : "Visible queue", value: String(visibleObjects.length) }
        ].map((stat) => (
          <article key={stat.label} className="stat-card">
            <span className="muted">{stat.label}</span>
            <strong>{stat.value}</strong>
          </article>
        ))}
      </section>

      <div className="desktop-only flow-grid" style={{ marginTop: 18 }}>
        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "کاتالوگ فلو" : "Flow catalog"}</h3>
          </div>
          <div className="flow-list" style={{ marginTop: 16 }}>
            {flows.map((item) => (
              <button
                key={`${item.flowKey}-${item.version ?? "latest"}`}
                type="button"
                className="flow-item"
                style={selectedFlowKey === item.flowKey ? { borderColor: "var(--accent)", background: "rgba(11, 92, 255, 0.08)" } : undefined}
                onClick={() => setSelectedFlowKey(item.flowKey)}
              >
                <strong>{item.name}</strong>
                <span className="muted-block">{item.flowKey} · v{item.version ?? 1}</span>
                <span className="muted-block">{item.states.length} {locale === "fa" ? "استیت" : "states"} · {item.transitions.length} {locale === "fa" ? "ترنزیشن" : "transitions"}</span>
              </button>
            ))}
            {!flows.length ? (
              <div className="flow-item">
                <strong>{locale === "fa" ? "فلویی برنگشته است" : "No flows returned"}</strong>
                <span className="muted-block">{locale === "fa" ? "برای شروع Save draft را بزنید." : "Use Save draft to bootstrap a starter definition."}</span>
              </div>
            ) : null}
          </div>
        </aside>

        <section className="panel-card">
          <div className="toolbar-row">
            <span className={flow?.active ? "status-pill success" : "status-pill warning"}>
              {flow ? `${flow.flowKey} · v${flow.version ?? 1}` : locale === "fa" ? "بدون انتخاب" : "No selection"}
            </span>
            <div className="pill-row">
              <button type="button" className="secondary-pill" onClick={() => setReloadToken((current) => current + 1)}>
                {locale === "fa" ? "بازخوانی" : "Refresh"}
              </button>
              <button type="button" className="secondary-pill" onClick={() => saveCurrentFlow(false)}>
                {locale === "fa" ? "ذخیره" : "Save draft"}
              </button>
              <button type="button" className="secondary-pill" onClick={startManagedObject} disabled={!flow}>
                {locale === "fa" ? "ایجاد آبجکت" : "Start object"}
              </button>
              <button type="button" className="primary-pill" onClick={() => saveCurrentFlow(true)}>
                {locale === "fa" ? "انتشار فلو" : "Publish flow"}
              </button>
            </div>
          </div>
          {status ? <div className="status-pill info" style={{ marginTop: 12 }}>{status}</div> : null}

          <div className="flow-canvas flow-canvas-wide" style={{ marginTop: 18 }}>
            {flow?.states.map((state) => (
              <div
                key={state.id}
                className={`kanban-node${state.terminal ? " green" : ""}`}
                style={selectedState?.id === state.id ? { borderColor: "var(--accent)", transform: "translateY(-2px)" } : undefined}
              >
                <strong>{state.displayName}</strong>
                <span className="muted-block">{state.formKey ?? state.entityKey ?? (locale === "fa" ? "استیت خودکار" : "Automatic state")}</span>
                <span className="muted-block">{state.waitForAutomation ? (locale === "fa" ? "منتظر اتوماسیون" : "Waits for automation") : state.terminal ? (locale === "fa" ? "نهایی" : "Terminal") : locale === "fa" ? "تعاملی" : "Interactive"}</span>
              </div>
            )) ?? (
              <div className="mini-card" style={{ width: "100%" }}>
                <strong>{locale === "fa" ? "هیچ فلویی از API برنگشته است" : "No flow was returned by the API"}</strong>
                <span className="muted-block">{locale === "fa" ? "برای ایجاد فلو اولیه Save draft را بزنید." : "Use Save draft to create the starter BPM definition."}</span>
              </div>
            )}
          </div>

          <div className="data-table-shell" style={{ marginTop: 18 }}>
            <div className="card-title-row">
              <h3>{locale === "fa" ? "ترنزیشن‌ها و شرط‌ها" : "Transitions and conditions"}</h3>
            </div>
            <table className="data-table" style={{ marginTop: 12 }}>
              <thead>
                <tr>
                  <th>{locale === "fa" ? "ترنزیشن" : "Transition"}</th>
                  <th>{locale === "fa" ? "مسیر" : "Path"}</th>
                  <th>{locale === "fa" ? "گروه‌ها" : "Groups"}</th>
                  <th>{locale === "fa" ? "شرط" : "Condition"}</th>
                </tr>
              </thead>
              <tbody>
                {(flow?.transitions ?? []).map((transition) => (
                  <tr key={transition.id}>
                    <td>{transition.label}</td>
                    <td>{transition.fromState} → {transition.toState}</td>
                    <td>{transition.allowedGroups?.join(", ") || "—"}</td>
                    <td>{transition.conditionExpression ?? (transition.conditions?.length ? `${transition.conditions.length} checks` : "—")}</td>
                  </tr>
                ))}
                {!flow?.transitions?.length ? (
                  <tr>
                    <td colSpan={4}>{locale === "fa" ? "ترنزیشنی برای این فلو وجود ندارد." : "No transitions exist for the selected flow."}</td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{locale === "fa" ? "کارتابل و متادیتا" : "Cartable and metadata"}</h3>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "استیت انتخاب‌شده" : "Selected state"}</strong>
              <span className="muted-block">{selectedState?.displayName ?? "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "اکشن اصلی" : "Primary action"}</strong>
              <span className="muted-block">{selectedAction?.type ?? "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "اپراتورهای شرط" : "Condition operators"}</strong>
              <span className="muted-block">{conditions?.operators?.map((item) => item.key).slice(0, 8).join(", ") ?? "—"}</span>
            </div>
          </div>

          <div className="card-title-row" style={{ marginTop: 20 }}>
            <h3>{locale === "fa" ? "اختصاص‌یافته به من" : "Assigned to me"}</h3>
          </div>
          <div className="flow-list" style={{ marginTop: 12 }}>
            {assignedObjects.slice(0, 4).map((object) => (
              <button
                key={object.id}
                type="button"
                className="flow-item"
                style={selectedObjectId === object.id ? { borderColor: "var(--accent)", background: "rgba(11, 92, 255, 0.08)" } : undefined}
                onClick={() => setSelectedObjectId(object.id)}
              >
                <strong>{object.objectType}</strong>
                <span className="muted-block">{object.flowKey} · {object.state}</span>
                <span className="muted-block">{object.assignee ?? "—"}</span>
              </button>
            ))}
            {!assignedObjects.length ? (
              <div className="flow-item">
                <strong>{locale === "fa" ? "موردی اختصاص داده نشده" : "Nothing assigned"}</strong>
                <span className="muted-block">{locale === "fa" ? "این بخش با رسیدن آبجکت‌ها به استیت‌های assigned پر می‌شود." : "This fills when objects route into states assigned to the current actor."}</span>
              </div>
            ) : null}
          </div>

          <div className="card-title-row" style={{ marginTop: 20 }}>
            <h3>{locale === "fa" ? "جزئیات آبجکت" : "Object detail"}</h3>
          </div>
          <div className="detail-list" style={{ marginTop: 12 }}>
            <div className="detail-item">
              <strong>{locale === "fa" ? "شناسه" : "Object id"}</strong>
              <span className="muted-block">{selectedObject?.id ?? "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "ترنزیشن‌های مجاز" : "Available transitions"}</strong>
              <span className="muted-block">{transitionOptions.map((item) => item.label).join(", ") || "—"}</span>
            </div>
            <div className="detail-item">
              <strong>{locale === "fa" ? "بلاک‌های اتوماسیون" : "Automation blocks"}</strong>
              <span className="muted-block">{String(selectedObject?.automationBlockRegistry?.length ?? 0)}</span>
            </div>
          </div>
          <pre className="json-view" style={{ marginTop: 16, maxHeight: 240 }}>{JSON.stringify(selectedObject?.payload ?? {}, null, 2)}</pre>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div className="mobile-screen-header">
          <button type="button" className="icon-pill">←</button>
          <div className="mobile-brand">
            <span className="tile-icon">✎</span>
            <strong style={{ fontSize: "2rem" }}>{locale === "fa" ? "فلوساز" : "Flow Builder"}</strong>
          </div>
          <span className="pill">{flows.length} {locale === "fa" ? "فلو" : "flows"}</span>
        </div>
        <div className="mobile-tab-strip">
          <span className="status-pill info">{locale === "fa" ? "فلوها" : "Flows"}</span>
          <span className="pill">{locale === "fa" ? "کارتابل" : "Cartable"}</span>
          <span className="pill">{locale === "fa" ? "اکشن‌ها" : "Actions"}</span>
        </div>
        <div className="flow-mobile-path">
          {(flow?.states ?? []).map((state) => (
            <div key={state.id} className="flow-mobile-node">
              <strong>{state.displayName}</strong>
              <span className="muted-block">{state.formKey ?? state.entityKey ?? "Auto"}</span>
            </div>
          ))}
        </div>
        <div className="mobile-bottom-sheet">
          <div className="mobile-handle" />
          <div className="toolbar-row">
            <strong>{selectedObject?.flowKey ?? flow?.flowKey ?? (locale === "fa" ? "بدون فلو" : "No flow")}</strong>
            <button type="button" className="icon-pill">×</button>
          </div>
          <div className="mobile-list" style={{ marginTop: 14 }}>
            {(assignedObjects.length ? assignedObjects : visibleObjects).slice(0, 3).map((object) => (
              <div key={object.id} className="mobile-list-item">
                <strong>{object.objectType}</strong>
                <span className="muted-block">{object.state} · {object.assignee ?? "—"}</span>
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
      { id: "Review", displayName: "Review", terminal: false, formKey: "review_form", waitForAutomation: false },
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
