import { AppShell } from "@/components/app-shell";

const serviceGroups = [
  ["Content", "Pages, blog posts, landing blocks, SEO metadata"],
  ["Catalog", "Products, categories, services, prices, attributes"],
  ["CRM", "Leads, customers, companies, tasks, tickets"],
  ["Commerce", "Orders, invoices, returns, documents"],
  ["Finance", "Transactions, settlements, payable/receivable records"],
  ["Inventory", "Warehouses, stock movements, reservations"]
];

export default function DataPage() {
  return (
    <AppShell title="Data Manager" subtitle="Manage tenant/site-scoped records after definitions are provisioned.">
      <section className="panel rail" style={{ marginTop: 24 }}>
        <div className="editor-toolbar">
          <div>
            <p className="section-title">Entity data workbench</p>
            <div className="meta">Uses each service endpoint API and keeps strict dynamic validation intact.</div>
          </div>
          <span className="tag">Part 2</span>
        </div>
        <div className="mini-grid">
          {serviceGroups.map(([title, description]) => (
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
