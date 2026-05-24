"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { fallbackStats } from "@/lib/panel-fixtures";
import { listRecords, submitRecord, updateRecord } from "@/lib/dynamic-api";
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
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    listRecords(selectedBucket.serviceKey, selectedBucket.key, { tenantKey: "tenant-demo", siteKey: "site-commerce" })
      .then(setRecords)
      .catch(() => setRecords(fallbackRecords));
  }, [selectedBucket]);

  const activeRecord = useMemo(() => records[0] ?? fallbackRecords[0], [records]);

  async function createDemoRecord() {
    setStatus(locale === "fa" ? "در حال ایجاد رکورد..." : "Creating record...");
    const recordKey = `${selectedBucket.key}-${Date.now()}`;
    try {
      const created = await submitRecord(
        selectedBucket.serviceKey,
        selectedBucket.key,
        recordKey,
        {
          title: selectedBucket.key === "product" ? "New Product" : "New Record",
          status: "DRAFT",
          category: "General",
          price: 0,
          stock: 0
        },
        { tenantKey: "tenant-demo", siteKey: "site-commerce" }
      );
      setRecords((current) => [created, ...current]);
      setStatus(locale === "fa" ? "رکورد ایجاد شد." : "Record created.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ایجاد رکورد ناموفق بود." : "Create failed.");
    }
  }

  async function markActiveRecordEdited() {
    setStatus(locale === "fa" ? "در حال ذخیره..." : "Saving...");
    try {
      const updated = await updateRecord(
        selectedBucket.serviceKey,
        selectedBucket.key,
        activeRecord.recordKey,
        { ...activeRecord.data, status: "Published" },
        { tenantKey: "tenant-demo", siteKey: "site-commerce" }
      );
      setRecords((current) => current.map((item) => (item.recordKey === updated.recordKey ? updated : item)));
      setStatus(locale === "fa" ? "رکورد ذخیره شد." : "Record saved.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ذخیره ناموفق بود." : "Save failed.");
    }
  }

  return (
    <PanelShell
      activeKey="data"
      title="Data Manager"
      titleFa="مدیریت داده"
      subtitle="Manage products, content, comments, orders, CRM, finance, inventory, and report records from one panel."
      subtitleFa="محصولات، محتوا، سفارش‌ها، CRM، موجودی و داده‌های گزارش را از یک پنل واحد مدیریت کنید."
    >
      <section className="desktop-only metric-grid">
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

      <div className="desktop-only data-manager-grid" style={{ marginTop: 18 }}>
        <section className="panel-card">
          <div className="entity-list">
            {entityBuckets.map((bucket) => (
              <button
                type="button"
                key={bucket.key}
                className={selectedBucket.key === bucket.key ? "entity-item active" : "entity-item"}
                onClick={() => setSelectedBucket(bucket)}
              >
                <strong>{locale === "fa" ? bucket.titleFa : bucket.titleEn}</strong>
                <span className="muted-block">{locale === "fa" ? "رکوردها" : "records"}</span>
              </button>
            ))}
          </div>
        </section>

        <section className="data-table-shell">
          <div className="toolbar-row">
            <div className="pill-row">
              <input placeholder={locale === "fa" ? "جستجوی محصولات..." : "Search products..."} />
            </div>
            <div className="pill-row">
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "فیلترها" : "Filters"}
              </button>
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "ورود فایل" : "Import"}
              </button>
              <button type="button" className="secondary-pill">
                {locale === "fa" ? "خروجی" : "Export"}
              </button>
              <button type="button" className="primary-pill" onClick={createDemoRecord}>
                {locale === "fa" ? "رکورد جدید" : "New record"}
              </button>
              </div>
            </div>
          {status ? <div className="status-pill info" style={{ marginTop: 12 }}>{status}</div> : null}

          <table className="data-table" style={{ marginTop: 16 }}>
            <thead>
              <tr>
                <th>{locale === "fa" ? "کلید" : "Record key"}</th>
                <th>{locale === "fa" ? "عنوان" : "Title"}</th>
                <th>{locale === "fa" ? "دسته‌بندی" : "Category"}</th>
                <th>{locale === "fa" ? "قیمت" : "Price"}</th>
                <th>{locale === "fa" ? "موجودی" : "Stock"}</th>
                <th>{locale === "fa" ? "وضعیت" : "Status"}</th>
                <th>{locale === "fa" ? "به‌روزرسانی" : "Updated"}</th>
              </tr>
            </thead>
            <tbody>
              {(records.length ? records : fallbackRecords).slice(0, 7).map((record) => (
                <tr key={record.recordKey}>
                  <td>{record.recordKey}</td>
                  <td>{String(record.data.title ?? record.data.name ?? record.data.label ?? record.recordKey)}</td>
                  <td>{String(record.data.category ?? "Furniture")}</td>
                  <td>{String(record.data.price ?? "—")}</td>
                  <td>{String(record.data.stock ?? "—")}</td>
                  <td>{String(record.data.status ?? "ACTIVE")}</td>
                  <td>{record.updatedAt ?? (locale === "fa" ? "به تازگی" : "Recently")}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="summary-grid" style={{ marginTop: 18 }}>
            <article className="mini-card">
              <strong>{locale === "fa" ? "روند فروش" : "Sales trend"}</strong>
              <span className="muted-block">{locale === "fa" ? "۷ روز گذشته" : "Last 7 days"}</span>
            </article>
            <article className="mini-card">
              <strong>{locale === "fa" ? "وضعیت موجودی" : "Inventory status"}</strong>
              <span className="muted-block">{locale === "fa" ? "در انبار / کم‌موجودی / ناموجود" : "In stock / low stock / out of stock"}</span>
            </article>
          </div>
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
          <div className="toolbar-row" style={{ marginTop: 18 }}>
            <button type="button" className="secondary-pill">{locale === "fa" ? "پیش‌نمایش" : "Preview"}</button>
            <button type="button" className="primary-pill" onClick={markActiveRecordEdited}>{locale === "fa" ? "ویرایش محصول" : "Edit product"}</button>
          </div>
        </aside>
      </div>

      <div className="mobile-only mobile-screen">
        <div>
          <h2 style={{ margin: 0, fontSize: "3rem" }}>{locale === "fa" ? "مدیریت داده" : "Data Manager"}</h2>
        </div>
        <div className="toolbar-row">
          <input placeholder={locale === "fa" ? "جستجوی محصولات، سفارش‌ها..." : "Search products, orders, customers..."} />
          <button type="button" className="icon-pill">⎚</button>
        </div>
        <section className="three-column-grid">
          {fallbackStats.slice(0, 3).map((stat) => (
            <article key={stat.label} className="stat-card">
              <span className="muted">{locale === "fa" ? statToFa(stat.label) : stat.label}</span>
              <strong>{locale === "fa" ? toFaDigits(stat.value) : stat.value}</strong>
            </article>
          ))}
        </section>
        <div className="pill-row">
          {entityBuckets.map((bucket) => (
            <button key={bucket.key} type="button" className={selectedBucket.key === bucket.key ? "status-pill info" : "pill"} onClick={() => setSelectedBucket(bucket)}>
              {locale === "fa" ? bucket.titleFa : bucket.titleEn}
            </button>
          ))}
        </div>
        <div className="mobile-list">
          {(records.length ? records : fallbackRecords).map((record) => (
            <div key={record.recordKey} className="mobile-list-item">
              <strong>{String(record.data.title ?? record.recordKey)}</strong>
              <span className="muted-block">
                {String(record.data.category ?? "")} {record.data.price ? `• $${String(record.data.price)}` : ""} {record.data.stock ? `• Stock: ${String(record.data.stock)}` : ""}
              </span>
              <span className={String(record.data.status).toLowerCase().includes("low") ? "status-pill warning" : "status-pill success"}>
                {String(record.data.status ?? "Published")}
              </span>
            </div>
          ))}
        </div>
        <div className="mobile-bottom-sheet">
          <div className="mobile-handle" />
          <div className="toolbar-row">
            <div>
              <strong>{String(activeRecord.data.title ?? activeRecord.recordKey)}</strong>
              <span className="muted-block">{String(activeRecord.data.category ?? "")}</span>
            </div>
            <button type="button" className="icon-pill">×</button>
          </div>
          <div className="three-column-grid" style={{ marginTop: 14 }}>
            <div className="mini-card">
              <span className="muted">Price</span>
              <strong>{activeRecord.data.price ? `$${String(activeRecord.data.price)}` : "—"}</strong>
            </div>
            <div className="mini-card">
              <span className="muted">Stock</span>
              <strong>{String(activeRecord.data.stock ?? "—")}</strong>
            </div>
            <div className="mini-card">
              <span className="muted">Status</span>
              <strong>{String(activeRecord.data.status ?? "Published")}</strong>
            </div>
          </div>
          <div className="toolbar-row" style={{ marginTop: 16 }}>
            <button type="button" className="secondary-pill">{locale === "fa" ? "پیش‌نمایش" : "Preview"}</button>
            <button type="button" className="primary-pill" onClick={markActiveRecordEdited}>{locale === "fa" ? "ویرایش محصول" : "Edit product"}</button>
          </div>
        </div>
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
