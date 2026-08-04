export type StatCard = {
  label: string;
  value: string;
  delta: string;
  tone?: "blue" | "green" | "violet" | "amber";
};

export const dashboardCapabilityCards = [
  {
    key: "studio",
    icon: "✦",
    titleEn: "AI Studio",
    titleFa: "استودیوی هوش مصنوعی",
    descEn: "Generate apps, routes, bots, and workflows from one prompt.",
    descFa: "اپ، مسیر، بات و فلو را با یک درخواست تولید کنید."
  },
  {
    key: "templates",
    icon: "▤",
    titleEn: "Blueprints",
    titleFa: "نقشه‌ها و قالب‌ها",
    descEn: "Start from proven templates for shop, CRM, forms, and PWAs.",
    descFa: "از قالب‌های آماده برای فروشگاه، CRM، فرم و PWA شروع کنید."
  },
  {
    key: "maker",
    icon: "✎",
    titleEn: "Maker",
    titleFa: "سازنده",
    descEn: "Model entities, data contracts, and permissions visually.",
    descFa: "موجودیت‌ها، قراردادهای داده و دسترسی را بصری طراحی کنید."
  },
  {
    key: "flows",
    icon: "⌇",
    titleEn: "Flow Builder",
    titleFa: "سازنده فلو",
    descEn: "Design BPM states, approvals, and automation steps.",
    descFa: "وضعیت‌های BPM، تاییدها و مراحل اتوماسیون را طراحی کنید."
  },
  {
    key: "apps",
    icon: "⬡",
    titleEn: "Apps / Bots",
    titleFa: "اپ‌ها / بات‌ها",
    descEn: "Provision channels and mini apps for real customer touchpoints.",
    descFa: "کانال‌ها و مینی‌اپ‌ها را برای تعامل واقعی با مشتری راه‌اندازی کنید."
  },
  {
    key: "data",
    icon: "◍",
    titleEn: "Data",
    titleFa: "داده‌ها",
    descEn: "Manage records, inventory, content, and publishing readiness.",
    descFa: "رکوردها، موجودی، محتوا و آمادگی انتشار را مدیریت کنید."
  }
];

export const blueprintVisuals = [
  { key: "website", hue: "blue" },
  { key: "shop", hue: "violet" },
  { key: "crm", hue: "green" },
  { key: "workflow", hue: "amber" },
  { key: "bot", hue: "blue" },
  { key: "pwa", hue: "violet" }
] as const;
