"use client";

import { useMemo, useState } from "react";
import {
  Background,
  Controls,
  MiniMap,
  ReactFlow,
  addEdge,
  type Connection,
  type Edge,
  type Node,
  useEdgesState,
  useNodesState
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { AppShell } from "@/components/app-shell";
import { saveFlow } from "@/lib/bpm-api";

const initialNodes: Node[] = [
  {
    id: "start",
    type: "input",
    position: { x: 80, y: 120 },
    data: { label: "Start / Intake" }
  },
  {
    id: "review",
    position: { x: 420, y: 80 },
    data: { label: "Review request" }
  },
  {
    id: "approved",
    type: "output",
    position: { x: 780, y: 70 },
    data: { label: "Approved" }
  },
  {
    id: "rejected",
    type: "output",
    position: { x: 780, y: 210 },
    data: { label: "Rejected" }
  }
];

const initialEdges: Edge[] = [
  { id: "start-review", source: "start", target: "review", label: "submit" },
  { id: "review-approved", source: "review", target: "approved", label: "approve" },
  { id: "review-rejected", source: "review", target: "rejected", label: "reject" }
];

export default function FlowsPage() {
  const [tenantKey, setTenantKey] = useState("tenant-demo");
  const [siteKey, setSiteKey] = useState("site-commerce");
  const [flowKey, setFlowKey] = useState("custom-approval-flow");
  const [flowName, setFlowName] = useState("Custom Approval Flow");
  const [entityService, setEntityService] = useState("content-service");
  const [entityKey, setEntityKey] = useState("custom-form");
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const flowDefinition = useMemo(() => ({
    flowKey,
    version: 1,
    name: flowName,
    description: "Created from the visual flow maker.",
    startState: nodes[0]?.id ?? "start",
    active: true,
    states: nodes.map((node) => ({
      id: node.id,
      displayName: String(node.data.label ?? node.id),
      terminal: node.type === "output",
      formKey: entityKey,
      entityService,
      entityKey,
      submitMode: "DYNAMIC" as const,
      waitForAutomation: false
    })),
    transitions: edges.map((edge) => ({
      id: edge.id,
      fromState: edge.source,
      toState: edge.target,
      label: String(edge.label ?? "transition"),
      conditionOperator: "AND" as const,
      conditions: []
    }))
  }), [edges, entityKey, entityService, flowKey, flowName, nodes]);

  function onConnect(connection: Connection) {
    setEdges((current) => addEdge({ ...connection, id: `${connection.source}-${connection.target}-${Date.now()}`, label: "transition" }, current));
  }

  function addNode(kind: "form" | "condition" | "action" | "terminal") {
    const id = `${kind}-${Date.now().toString(36)}`;
    setNodes((current) => [
      ...current,
      {
        id,
        type: kind === "terminal" ? "output" : "default",
        position: { x: 180 + current.length * 90, y: 180 + current.length * 35 },
        data: { label: kind === "form" ? "Collect form" : kind === "condition" ? "Check condition" : kind === "action" ? "Run action" : "Done" }
      }
    ]);
  }

  async function publishFlow() {
    setLoading(true);
    setStatus(null);
    try {
      await saveFlow(flowDefinition, { tenantKey, siteKey });
      setStatus("Flow published to bpm-service.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to publish flow");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Flow Maker" subtitle="Create BPM, bot, form, and automation flows with a React Flow-style canvas.">
      <div className="builder-shell flow-builder-shell">
        <aside className="builder-palette">
          <p className="section-title">Nodes</p>
          {(["form", "condition", "action", "terminal"] as const).map((kind) => (
            <button key={kind} type="button" className="builder-tool" onClick={() => addNode(kind)}>
              <span>{kind === "form" ? "Tt" : kind === "condition" ? "?" : kind === "action" ? "Run" : "OK"}</span>
              {kind}
            </button>
          ))}
          <p className="section-title">Scope</p>
          <div className="field">
            <label>Tenant</label>
            <input value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
          </div>
          <div className="field">
            <label>Site</label>
            <input value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
          </div>
        </aside>

        <section className="builder-canvas flow-canvas-wrap">
          <div className="builder-toolbar">
            <div className="segmented">
              <button type="button" className="active">Node editor</button>
              <button type="button">Sequence editor</button>
            </div>
            <button type="button" className="btn" onClick={publishFlow} disabled={loading}>
              {loading ? "Publishing..." : "Publish"}
            </button>
          </div>
          <div className="ai-banner">AI Applied - You can generate a flow from prompt, then adjust states and transitions manually.</div>
          <div className="flow-canvas">
            <ReactFlow
              nodes={nodes}
              edges={edges}
              onNodesChange={onNodesChange}
              onEdgesChange={onEdgesChange}
              onConnect={onConnect}
              fitView
            >
              <Background gap={18} size={1} />
              <Controls />
              <MiniMap />
            </ReactFlow>
          </div>
          {status ? <div className="ai-banner">{status}</div> : null}
        </section>

        <aside className="builder-inspector">
          <p className="section-title">Flow settings</p>
          <div className="form-grid">
            <div className="field">
              <label>Flow key</label>
              <input value={flowKey} onChange={(event) => setFlowKey(event.target.value)} />
            </div>
            <div className="field">
              <label>Name</label>
              <input value={flowName} onChange={(event) => setFlowName(event.target.value)} />
            </div>
            <div className="field">
              <label>Entity service</label>
              <input value={entityService} onChange={(event) => setEntityService(event.target.value)} />
            </div>
            <div className="field">
              <label>Entity key / form key</label>
              <input value={entityKey} onChange={(event) => setEntityKey(event.target.value)} />
            </div>
          </div>
          <p className="section-title" style={{ marginTop: 20 }}>BPM JSON</p>
          <pre className="json-view">{JSON.stringify(flowDefinition, null, 2)}</pre>
        </aside>
      </div>
    </AppShell>
  );
}
