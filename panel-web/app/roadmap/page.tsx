"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { listFlows } from "@/lib/bpm-api";
import { listDefinitions } from "@/lib/dynamic-api";
import { listBotIntegrations, listClientDrafts, listMiniAppBuilds } from "@/lib/platform-api";
import { productRoadmap } from "@/lib/product-roadmap";

const statusLabels = {
  prototype: "Prototype in repo",
  integration: "Integration work active",
  hardening: "Hardening",
  planned: "Planned gate"
} as const;

export default function RoadmapPage() {
  const [signals, setSignals] = useState({
    drafts: 0,
    flows: 0,
    definitions: 0,
    integrations: 0,
    miniApps: 0
  });
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    Promise.allSettled([
      listClientDrafts({ tenantKey: "tenant-demo", siteKey: "site-commerce" }),
      listFlows({ tenantKey: "tenant-demo", siteKey: "site-commerce" }),
      listDefinitions("storefront-service", { tenantKey: "tenant-demo", siteKey: "site-commerce" }),
      listDefinitions("catalog-service", { tenantKey: "tenant-demo", siteKey: "site-commerce" }),
      listBotIntegrations({ tenantKey: "tenant-demo", siteKey: "site-commerce" }),
      listMiniAppBuilds({ tenantKey: "tenant-demo", siteKey: "site-commerce" })
    ]).then(([drafts, flows, storefrontDefs, catalogDefs, integrations, miniApps]) => {
      const errors: string[] = [];
      if (drafts.status === "rejected") errors.push("Drafts");
      if (flows.status === "rejected") errors.push("Flows");
      if (storefrontDefs.status === "rejected") errors.push("Storefront definitions");
      if (catalogDefs.status === "rejected") errors.push("Catalog definitions");
      if (integrations.status === "rejected") errors.push("Bot integrations");
      if (miniApps.status === "rejected") errors.push("Mini apps");

      setSignals({
        drafts: drafts.status === "fulfilled" ? drafts.value.length : 0,
        flows: flows.status === "fulfilled" ? flows.value.length : 0,
        definitions: (storefrontDefs.status === "fulfilled" ? storefrontDefs.value.length : 0) + (catalogDefs.status === "fulfilled" ? catalogDefs.value.length : 0),
        integrations: integrations.status === "fulfilled" ? integrations.value.length : 0,
        miniApps: miniApps.status === "fulfilled" ? miniApps.value.length : 0
      });
      setStatus(errors.length ? `Live signals unavailable for: ${errors.join(", ")}` : null);
    });
  }, []);

  const liveTracks = useMemo(() => {
    return productRoadmap.map((track) => ({
      ...track,
      liveSummary: resolveLiveSummary(track.id, signals)
    }));
  }, [signals]);

  return (
    <AppShell
      title="Platform roadmap from live workspace signals."
      subtitle="Track launch-critical workstreams against the current drafts, definitions, flows, and bot channels in the platform."
    >
      {status ? <p className="muted">{status}</p> : null}
      <div className="summary-grid" style={{ marginBottom: 24 }}>
        <div className="mini-card"><strong>{signals.drafts}</strong><span className="muted-block">Drafts</span></div>
        <div className="mini-card"><strong>{signals.definitions}</strong><span className="muted-block">Definitions</span></div>
        <div className="mini-card"><strong>{signals.flows}</strong><span className="muted-block">Flows</span></div>
        <div className="mini-card"><strong>{signals.integrations}</strong><span className="muted-block">Bot integrations</span></div>
        <div className="mini-card"><strong>{signals.miniApps}</strong><span className="muted-block">Mini apps</span></div>
      </div>
      <div className="roadmap-stack">
        {liveTracks.map((track) => (
          <section key={track.id} className="roadmap-card">
            <div className="roadmap-topline">
              <div>
                <p className="eyebrow">Track {track.order}</p>
                <h3>{track.title}</h3>
              </div>
              <div className="roadmap-status">{statusLabels[track.status]}</div>
            </div>

            <p className="lede">{track.summary}</p>
            <div className="roadmap-item" style={{ marginBottom: 18 }}>
              <strong>Live status</strong>
              <span>{track.liveSummary}</span>
            </div>

            <div className="roadmap-grid">
              <div className="roadmap-column">
                <p className="section-title">Phases</p>
                {track.phases.map((phase) => (
                  <div key={phase.title} className="roadmap-item">
                    <strong>{phase.title}</strong>
                    <span>{phase.outcome}</span>
                  </div>
                ))}
              </div>

              <div className="roadmap-column">
                <p className="section-title">Dependencies</p>
                {track.dependencies.map((dependency) => (
                  <div key={dependency} className="roadmap-item">
                    <strong>{dependency}</strong>
                  </div>
                ))}

                <p className="section-title" style={{ marginTop: 18 }}>Launch gate</p>
                {track.launchGate.map((item) => (
                  <div key={item} className="roadmap-item">
                    <span>{item}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="roadmap-meta">
              <div>
                <p className="section-title">Panel routes</p>
                <div className="chip-row">
                  {track.routes.map((route) => (
                    <Link key={route} className="chip active" href={route}>
                      {route}
                    </Link>
                  ))}
                </div>
              </div>

              <div>
                <p className="section-title">Owning services</p>
                <div className="chip-row">
                  {track.services.map((service) => (
                    <span key={service} className="chip">
                      {service}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </section>
        ))}
      </div>
    </AppShell>
  );
}

function resolveLiveSummary(trackId: string, signals: { drafts: number; flows: number; definitions: number; integrations: number; miniApps: number }) {
  switch (trackId) {
    case "website-builder":
      return `${signals.definitions} storefront/catalog definitions and ${signals.drafts} drafts are available for publish-oriented work.`;
    case "bot-messaging":
      return `${signals.integrations} bot integrations and ${signals.miniApps} mini app builds are available for channel rollout.`;
    case "form-flow-builder":
      return `${signals.flows} flows and ${signals.definitions} structured definitions are available for builder convergence.`;
    case "test-harness":
      return `${signals.drafts} drafts, ${signals.flows} flows, and ${signals.integrations} integrations are currently visible to the harness.`;
    default:
      return "No live signal available.";
  }
}
