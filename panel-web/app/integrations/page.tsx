import Link from "next/link";
import { AppShell } from "@/components/app-shell";

const channels = [
  ["Website", "SEO storefront routes, sitemap, robots.txt, media, search, cart, checkout"],
  ["PWA", "Installable control panel and future customer app shell"],
  ["Telegram bot", "Conversation sessions mapped to AI drafts and platform APIs"],
  ["Bale bot", "Same draft/session flow with Bale channel identity"],
  ["Telegram mini app", "Next phase client shell once bot channel contracts stabilize"],
  ["Mobile app", "Next phase native wrapper around public APIs and PWA flows"]
];

export default function IntegrationsPage() {
  return (
    <AppShell title="Client Apps And Bots" subtitle="Manage every presentation channel attached to a generated business app.">
      <section className="panel rail" style={{ marginTop: 24 }}>
        <div className="editor-toolbar">
          <div>
            <p className="section-title">Integrated channels</p>
            <div className="meta">Current market focus: website/PWA plus Telegram and Bale bot maker.</div>
          </div>
          <Link className="btn" href="/bot">
            Open bot flow
          </Link>
        </div>
        <div className="mini-grid">
          {channels.map(([title, description]) => (
            <div key={title} className="mini-card">
              <h3>{title}</h3>
              <p>{description}</p>
            </div>
          ))}
        </div>
      </section>
    </AppShell>
  );
}
