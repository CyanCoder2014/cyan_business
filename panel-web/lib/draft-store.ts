import type { ProjectDraft } from "@/lib/types";

const storageKey = "naviya-panel-drafts";

export function loadDrafts(): ProjectDraft[] {
  if (typeof window === "undefined") {
    return [];
  }
  try {
    const raw = window.localStorage.getItem(storageKey);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw) as ProjectDraft[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function saveDraft(draft: ProjectDraft): ProjectDraft[] {
  const drafts = loadDrafts();
  const nextDrafts = [draft, ...drafts.filter((item) => item.id !== draft.id)].slice(0, 20);
  if (typeof window !== "undefined") {
    window.localStorage.setItem(storageKey, JSON.stringify(nextDrafts));
  }
  return nextDrafts;
}

export function seedDrafts(): ProjectDraft[] {
  return [
    {
      id: "demo-commerce-portal",
      title: "Commerce Portal",
      prompt: "Build a small ecommerce + CRM portal with storefront, product catalog, and invoices.",
      tenantKey: "tenant-demo",
      siteKey: "site-commerce",
      updatedAt: "2025-05-08T00:00:00.000Z",
      status: "PROVISIONED",
      dsl: {
        app: {
          appKey: "commerce-portal",
          title: "Commerce Portal",
          type: "MIXED_BUSINESS_APP",
          tenantKey: "tenant-demo",
          siteKey: "site-commerce",
          capabilities: ["website", "shop", "crm"]
        },
        entities: [],
        routes: [],
        flows: [],
        delivery: {
          publicApis: ["/public/storefront/render?path=/", "/public/storefront/sitemap"],
          botApis: ["/api/catalog-service/**", "/api/crm-service/**"]
        },
        manualActions: []
      },
      nextQuestions: [],
      provisioningResult: null
    },
    {
      id: "demo-blog-site",
      title: "Editorial Site",
      prompt: "Create a blog and landing page experience for a B2B SaaS product.",
      tenantKey: "tenant-demo",
      siteKey: "site-editorial",
      updatedAt: "2025-05-08T00:00:00.000Z",
      status: "DRAFT",
      dsl: {
        app: {
          appKey: "editorial-site",
          title: "Editorial Site",
          type: "BLOG",
          tenantKey: "tenant-demo",
          siteKey: "site-editorial",
          capabilities: ["website", "blog"]
        },
        entities: [],
        routes: [],
        flows: [],
        delivery: {
          publicApis: ["/public/storefront/render?path=/blog"],
          botApis: ["/api/content-service/**"]
        },
        manualActions: ["Connect domain when registrar workflow is available."]
      },
      nextQuestions: ["Which public pages should be created first?"],
      provisioningResult: null
    }
  ];
}
