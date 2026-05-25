export type StatCard = {
  label: string;
  value: string;
  delta: string;
  tone?: "blue" | "green" | "violet" | "amber";
};

export const fallbackStats: StatCard[] = [
  { label: "Visitors", value: "12,540", delta: "+18%", tone: "blue" },
  { label: "Orders", value: "2,843", delta: "+12%", tone: "green" },
  { label: "Publish readiness", value: "92%", delta: "8 checks left", tone: "violet" },
  { label: "Low-stock alerts", value: "23", delta: "5 urgent", tone: "amber" }
];

export const dashboardActivities = [
  {
    en: "Homepage sections refreshed and republished.",
    fa: "صفحه اصلی به‌روزرسانی و دوباره منتشر شد.",
    timeEn: "2 hours ago",
    timeFa: "۲ ساعت پیش"
  },
  {
    en: "CRM pipeline gained a new follow-up stage.",
    fa: "به پایپ‌لاین CRM یک مرحله پیگیری جدید اضافه شد.",
    timeEn: "6 hours ago",
    timeFa: "۶ ساعت پیش"
  },
  {
    en: "Support bot quick replies were improved.",
    fa: "پاسخ‌های سریع ربات پشتیبانی بهبود پیدا کرد.",
    timeEn: "1 day ago",
    timeFa: "۱ روز پیش"
  }
];

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
