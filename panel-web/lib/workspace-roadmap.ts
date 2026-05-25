export const makerLanes = [
  {
    title: "Part 1: entity maker",
    fa: "سازنده ساختار",
    description: "Create and adjust dynamic definitions, fields, validations, relations, and tenant/site scope before provisioning.",
    services: ["dynamic-entity-core", "content-service", "catalog-service", "crm-service", "commerce-service", "bpm-service"]
  },
  {
    title: "Part 2: data manager",
    fa: "مدیریت داده",
    description: "Manage records for content, products, customers, forms, invoices, inventory, and reports through endpoint APIs.",
    services: ["content-service", "catalog-service", "crm-service", "finance-service", "inventory-service", "report-service"]
  },
  {
    title: "Automation and BPM",
    fa: "اتوماسیون و فرآیند",
    description: "Edit workflow states, submit forms, notifications, API actions, and evented automations without bypassing event-service.",
    services: ["bpm-service", "automation-orchestrator-service", "notification-service", "event-service"]
  },
  {
    title: "Presentation apps",
    fa: "وب سایت و PWA",
    description: "Publish storefront websites, PWA apps, landing pages, shops, CRM portals, and SEO-friendly public routes.",
    services: ["storefront-service", "media-service", "search-index-service", "cart-service", "checkout-service"]
  },
  {
    title: "Bot channels",
    fa: "تلگرام و بله",
    description: "Connect Telegram and Bale bot sessions to the same AI draft, provisioning, BPM, content, catalog, and CRM APIs.",
    services: ["ai-orchestrator-service", "notification-service", "bpm-service", "catalog-service", "crm-service"]
  }
];

export const blueprintCards = [
  "Shop with catalog, cart, checkout, payment, invoice, inventory, and storefront.",
  "CRM with lead forms, customer records, tasks, notifications, and dashboard reports.",
  "BPM portal with dynamic forms, approval states, managed objects, and automation actions.",
  "Content website with blog, landing pages, media, sitemap, robots.txt, and search indexing.",
  "Telegram/Bale service bot connected to app drafts, records, workflows, and support flows."
];
