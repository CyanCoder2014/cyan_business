"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { fallbackStats } from "@/lib/panel-fixtures";
import { listRecords } from "@/lib/dynamic-api";
import type { DynamicEntityRecord } from "@/lib/types";

type EntityBucket = {
  key: string;
  titleEn: string;
  titleFa: string;
  serviceKey: "catalog-service" | "content-service" | "crm-service" | "inventory-service";
};

const entityBuckets: EntityBucket[] = [
  { key: "product", titleEn: "Products", titleFa: "محصولات", serviceKey: "catalog-service" },
  { key: "landing-page", titleEn: "Contents", titleFa: "محتوا", serviceKey: "content-service" },
  { key: "customer", titleEn: "Customers", titleFa: "مشتریان", serviceKey: "crm-service" },
  { key: "inventory-item", titleEn: "Inventory", titleFa: "موجودی", serviceKey: "inventory-service" }
];

export default function DataManagerPage() {
  const { locale } = usePanel();
  const [selectedBucket, setSelectedBucket] = useState(entityBuckets[0]);
  const [records, setRecords] = useState<DynamicEntityRecord[]>([]);

  useEffect(() => {
    listRecords(selectedBucket.serviceKey, selectedBucket.key, { tenantKey: "tenant-demo", siteKey: "site-commerce" })
      .then(setRecords)
      .catch(() => setRecords(fallbackRecords));
  }, [selectedBucket]);

  const activeRecord = useMemo(() => records[0] ?? fallbackRecords[0], [records]);

  return (
    <PanelShell
      activeKey="data"
      title="Data Manager"
      titleFa="مدیریت داده"
      subtitle="Manage products, content, comments, orders, CRM, finance, inventory, and report records from one panel."
      subtitleFa="محصولات، محتوا، سفارش‌ها، CRM، موجودی و داده‌های گزارش را از یک پنل واحد مدیریت کنید."
    >
      <section className="metric-grid">
        {fallbackStats.map((stat) => (
          <article key={stat.label} className="stat-card">
            <span className="muted">{locale === "fa" ? statToFa(stat.label) : stat.label}</span>
            <strong>{locale === "fa" ? toFaDigits(stat.value) : stat.value}</strong>
            <div className="stat-delta">{locale === "fa" ? toFaDigits(stat.delta) : stat.delta}</div>
          </article>
        ))}
        <article className="stat-card">
          <span className="muted">{locale === "fa" ? "نظرات در انتظار" : "Pending comments"}</span>
          <strong>{locale === "fa" ? "۱۲۸" : "128"}</strong>
          <div className="stat-delta">{locale === "fa" ? "۱۴ منتظر بررسی" : "14 awaiting review"}</div>
        </article>
      </section>

      <div className="page-grid" style={{ marginTop: 18 }}>
        <section className="data-table-shell">
          <div className="toolbar-row">
            <div className="pill-row">
              {entityBuckets.map((bucket) => (
                <button
                  type="button"
                  key={bucket.key}
                  className={selectedBucket.key === bucket.key ? "pill status-pill info" : "pill"}
                  onClick={() => setSelectedBucket(bucket)}
                >
                  {locale === "fa" ? bucket.titleFa : bucket.titleEn}
                </button>
              ))}
            </div>
            <div className="pill-row">
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "ورود فایل" : "Import"}
              </button>
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "خروجی" : "Export"}
              </button>
              <button type="button" className="primary-pill">
                {locale === "fa" ? "رکورد جدید" : "New record"}
              </button>
            </div>
          </div>

          <table className="data-table" style={{ marginTop: 16 }}>
            <thead>
              <tr>
                <th>{locale === "fa" ? "کلید" : "Record key"}</th>
                <th>{locale === "fa" ? "عنوان" : "Title"}</th>
                <th>{locale === "fa" ? "وضعیت" : "Status"}</th>
                <th>{locale === "fa" ? "به‌روزرسانی" : "Updated"}</th>
              </tr>
            </thead>
            <tbody>
              {(records.length ? records : fallbackRecords).slice(0, 7).map((record) => (
                <tr key={record.recordKey}>
                  <td>{record.recordKey}</td>
                  <td>{String(record.data.title ?? record.data.name ?? record.data.label ?? record.recordKey)}</td>
                  <td>{String(record.data.status ?? "ACTIVE")}</td>
                  <td>{record.updatedAt ?? (locale === "fa" ? "به تازگی" : "Recently")}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>

        <aside className="panel-card">
          <div className="card-title-row">
            <h3>{String(activeRecord.data.title ?? activeRecord.recordKey)}</h3>
            <span className="status-pill success">{String(activeRecord.data.status ?? "Published")}</span>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            {Object.entries(activeRecord.data).slice(0, 8).map(([key, value]) => (
              <div key={key} className="detail-item">
                <strong>{key}</strong>
                <span className="muted-block">{typeof value === "object" ? JSON.stringify(value) : String(value)}</span>
              </div>
            ))}
          </div>
        </aside>
      </div>
    </PanelShell>
  );
}

const fallbackRecords: DynamicEntityRecord[] = [
  {
    recordKey: "llc-001",
    data: {
      title: "Luna Lounge Chair",
      status: "Published",
      category: "Furniture",
      price: 349,
      stock: 38
    }
  },
  {
    recordKey: "bct-002",
    data: {
      title: "Breeze Coffee Table",
      status: "Low stock",
      category: "Furniture",
      price: 229,
      stock: 14
    }
  }
];

function statToFa(value: string) {
  switch (value) {
    case "Visitors":
      return "بازدیدها";
    case "Orders":
      return "سفارش‌ها";
    case "Publish readiness":
      return "آماده انتشار";
    case "Low-stock alerts":
      return "هشدار موجودی";
    default:
      return value;
  }
}

function toFaDigits(value: string) {
  return value.replace(/\d/g, (digit) => "۰۱۲۳۴۵۶۷۸۹"[Number(digit)] ?? digit);
}
