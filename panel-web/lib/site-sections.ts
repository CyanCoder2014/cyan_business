import type { SectionType } from "@/components/nav-icons";
import type { SchemaField } from "@/components/definitions/schema-tree-editor";

export const SECTIONS_FIELD_SCHEMA: SchemaField = {
  id: "sections",
  type: "list",
  itemValidations: {
    sectionKey: { id: "sectionKey", type: "string", validations: [{ validation: "REQUIRED", order: 1 }] },
    type: { id: "type", type: "string", validations: [{ validation: "REQUIRED", order: 1 }, { validation: "ENUM", order: 2, validationParams: { values: ["hero", "features", "testimonials", "faq", "cta", "footer"] } }] },
    order: { id: "order", type: "number" },
    visible: { id: "visible", type: "string", validations: [{ validation: "ENUM", order: 1, validationParams: { values: ["true", "false"] } }] },
    content: { id: "content", type: "object" },
    style: { id: "style", type: "object", itemValidations: {
      backgroundColor: { id: "backgroundColor", type: "string" },
      padding: { id: "padding", type: "string", validations: [{ validation: "ENUM", order: 1, validationParams: { values: ["sm", "md", "lg"] } }] },
      align: { id: "align", type: "string", validations: [{ validation: "ENUM", order: 1, validationParams: { values: ["start", "center", "end"] } }] }
    } }
  }
};

export type SiteSection = {
  sectionKey: string;
  type: SectionType;
  order: number;
  visible: string;
  content: Record<string, unknown>;
  style: { backgroundColor?: string; padding?: "sm" | "md" | "lg"; align?: "start" | "center" | "end" };
};

export const SECTION_TYPES: { type: SectionType; en: string; fa: string }[] = [
  { type: "hero", en: "Hero", fa: "بخش اصلی" },
  { type: "features", en: "Features", fa: "ویژگی‌ها" },
  { type: "testimonials", en: "Testimonials", fa: "نظرات مشتریان" },
  { type: "faq", en: "FAQ", fa: "سوالات متداول" },
  { type: "cta", en: "CTA", fa: "دعوت به اقدام" },
  { type: "footer", en: "Footer", fa: "پاورقی" }
];

export function defaultContent(type: SectionType): Record<string, unknown> {
  switch (type) {
    case "hero":
      return { heading: "Your headline goes here", subheading: "Describe what you offer in one or two sentences.", primaryButtonLabel: "Get started", primaryButtonHref: "/" };
    case "features":
      return { heading: "Everything you need", subheading: "", items: [{ title: "Feature one", description: "Describe this feature." }, { title: "Feature two", description: "Describe this feature." }] };
    case "testimonials":
      return { heading: "What customers say", items: [{ quote: "This product changed how we work.", author: "Customer name", role: "Role, Company" }] };
    case "faq":
      return { heading: "Frequently asked questions", items: [{ question: "Your question here?", answer: "Your answer here." }] };
    case "cta":
      return { heading: "Ready to get started?", subheading: "", buttonLabel: "Get started", buttonHref: "/" };
    case "footer":
      return { text: "© Your company", links: [{ label: "Privacy", href: "/privacy" }] };
    default:
      return {};
  }
}

export function sectionLabel(section: SiteSection): string {
  const heading = section.content?.heading ?? section.content?.text;
  return typeof heading === "string" && heading.trim() ? heading.trim() : section.type;
}

export function newSection(type: SectionType, order: number): SiteSection {
  return {
    sectionKey: `${type}-${Date.now().toString(36)}`,
    type,
    order,
    visible: "true",
    content: defaultContent(type),
    style: { padding: "md", align: "start" }
  };
}

export function reorderSections(sections: SiteSection[], fromKey: string, toKey: string): SiteSection[] {
  const fromIndex = sections.findIndex((section) => section.sectionKey === fromKey);
  const toIndex = sections.findIndex((section) => section.sectionKey === toKey);
  if (fromIndex < 0 || toIndex < 0) return sections;
  const next = [...sections];
  const [moved] = next.splice(fromIndex, 1);
  next.splice(toIndex, 0, moved);
  return next.map((section, index) => ({ ...section, order: index }));
}

export function normalizeSections(raw: unknown): SiteSection[] {
  if (!Array.isArray(raw)) return [];
  return raw
    .filter((item): item is Record<string, unknown> => Boolean(item) && typeof item === "object")
    .map((item, index) => ({
      sectionKey: typeof item.sectionKey === "string" ? item.sectionKey : `section-${index}`,
      type: (typeof item.type === "string" ? item.type : "hero") as SectionType,
      order: typeof item.order === "number" ? item.order : index,
      visible: item.visible === "false" ? "false" : "true",
      content: (item.content && typeof item.content === "object" ? item.content : {}) as Record<string, unknown>,
      style: (item.style && typeof item.style === "object" ? item.style : {}) as SiteSection["style"]
    }))
    .sort((a, b) => a.order - b.order);
}
