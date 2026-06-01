"use client";

import { useEffect, useMemo, useState } from "react";
import { PanelShell } from "@/components/panel-shell";
import { usePanel } from "@/components/panel-provider";
import { createDefinitionFromTemplate, listRecords, submitRecord, updateRecord } from "@/lib/dynamic-api";
import type { DynamicEntityRecord } from "@/lib/types";

type EntityBucket = {
  key: string;
  templateKey: string;
  titleEn: string;
  titleFa: string;
  serviceKey: "catalog-service" | "content-service" | "crm-service" | "inventory-service";
};

const entityBuckets: EntityBucket[] = [
  { key: "catalog-product", templateKey: "catalog-product", titleEn: "Products", titleFa: "محصولات", serviceKey: "catalog-service" },
  { key: "landing-page", templateKey: "landing-page", titleEn: "Contents", titleFa: "محتوا", serviceKey: "content-service" },
  { key: "crm-contact", templateKey: "crm-contact", titleEn: "Customers", titleFa: "مشتریان", serviceKey: "crm-service" },
  { key: "stock-item", templateKey: "stock-item", titleEn: "Inventory", titleFa: "موجودی", serviceKey: "inventory-service" }
];

export default function DataManagerPage() {
  const { locale } = usePanel();
  const [selectedBucket, setSelectedBucket] = useState(entityBuckets[0]);
  const [records, setRecords] = useState<DynamicEntityRecord[]>([]);
  const [recordCounts, setRecordCounts] = useState<Record<string, number>>({});
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    listRecords(selectedBucket.serviceKey, selectedBucket.key, { tenantKey: "tenant-demo", siteKey: "site-commerce" })
      .then(setRecords)
      .catch((error) => {
        setRecords([]);
        setStatus(error instanceof Error ? error.message : locale === "fa" ? "رکوردها بارگیری نشدند." : "Records could not be loaded.");
      });
  }, [locale, selectedBucket]);

  useEffect(() => {
    Promise.allSettled(
      entityBuckets.map((bucket) =>
        listRecords(bucket.serviceKey, bucket.key, { tenantKey: "tenant-demo", siteKey: "site-commerce" }).then((items) => ({
          key: bucket.key,
          count: items.length
        }))
      )
    ).then((results) => {
      const nextCounts: Record<string, number> = {};
      for (const result of results) {
        if (result.status === "fulfilled") {
          nextCounts[result.value.key] = result.value.count;
        }
      }
      setRecordCounts(nextCounts);
    });
  }, []);

  const activeRecord = useMemo(() => records[0] ?? null, [records]);
  const stats = [
    { label: locale === "fa" ? "همه رکوردها" : "All records", value: String(Object.values(recordCounts).reduce((sum, count) => sum + count, 0)) },
    { label: locale === "fa" ? "رکوردهای این بخش" : "This bucket", value: String(records.length) },
    { label: locale === "fa" ? "منتشرشده" : "Published", value: String(records.filter((item) => String(item.data.status ?? "").toUpperCase().includes("PUBLISH")).length) },
    { label: locale === "fa" ? "نیازمند بررسی" : "Needs review", value: String(records.filter((item) => String(item.data.status ?? "").toUpperCase().includes("DRAFT") || String(item.data.status ?? "").toUpperCase().includes("LOW")).length) }
  ];

  async function createDemoRecord() {
    setStatus(locale === "fa" ? "در حال ایجاد رکورد..." : "Creating record...");
    const recordKey = `${selectedBucket.key}-${Date.now()}`;
    try {
      await createDefinitionFromTemplate(selectedBucket.serviceKey, selectedBucket.templateKey, selectedBucket.key, {
        tenantKey: "tenant-demo",
        siteKey: "site-commerce"
      }).catch(() => null);
      const created = await submitRecord(
        selectedBucket.serviceKey,
        selectedBucket.key,
        recordKey,
        buildRecordData(selectedBucket, recordKey),
        { tenantKey: "tenant-demo", siteKey: "site-commerce" }
      );
      setRecords((current) => [created, ...current]);
      setRecordCounts((current) => ({
        ...current,
        [selectedBucket.key]: (current[selectedBucket.key] ?? 0) + 1
      }));
      setStatus(locale === "fa" ? "رکورد ایجاد شد." : "Record created.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : locale === "fa" ? "ایجاد رکورد ناموفق بود." : "Create failed.");
    }
  }

  async function markActiveRecordEdited() {
    setStatus(locale === "fa" ? "در حال ذخیره..." : "Saving...");
    if (!activeRecord) {
      setStatus(locale === "fa" ? "رکوردی برای ویرایش وجود ندارد." : "No record is available to edit.");
      return;
    }
    try {
      const updated = await updateRecord(
        selectedBucket.serviceKey,
        selectedBucket.key,
        activeRecord.recordKey,
        buildEditedRecordData(selectedBucket, activeRecord),
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
        {stats.map((stat) => (
          <article key={stat.label} className="stat-card">
            <span className="muted">{stat.label}</span>
            <strong>{locale === "fa" ? toFaDigits(stat.value) : stat.value}</strong>
          </article>
        ))}
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
                <span className="muted-block">{recordCounts[bucket.key] ?? 0} {locale === "fa" ? "رکورد" : "records"}</span>
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
              {records.slice(0, 7).map((record) => (
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
              {!records.length ? (
                <tr>
                  <td colSpan={7}>{locale === "fa" ? "رکوردی از API این بخش دریافت نشد." : "No records were returned for this bucket."}</td>
                </tr>
              ) : null}
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
            <h3>{activeRecord ? String(activeRecord.data.title ?? activeRecord.recordKey) : locale === "fa" ? "رکوردی انتخاب نشده" : "No record selected"}</h3>
            <span className={activeRecord ? "status-pill success" : "status-pill warning"}>{activeRecord ? String(activeRecord.data.status ?? "Published") : locale === "fa" ? "خالی" : "Empty"}</span>
          </div>
          <div className="detail-list" style={{ marginTop: 16 }}>
            {activeRecord ? Object.entries(activeRecord.data).slice(0, 8).map(([key, value]) => (
              <div key={key} className="detail-item">
                <strong>{key}</strong>
                <span className="muted-block">{typeof value === "object" ? JSON.stringify(value) : String(value)}</span>
              </div>
            )) : (
              <div className="detail-item">
                <strong>{locale === "fa" ? "هیچ داده‌ای برای نمایش وجود ندارد" : "No record data available"}</strong>
                <span className="muted-block">{locale === "fa" ? "صفحه دیگر از داده ساختگی استفاده نمی‌کند." : "This page no longer renders fabricated records."}</span>
              </div>
            )}
          </div>
          <div className="toolbar-row" style={{ marginTop: 18 }}>
            <button type="button" className="secondary-pill">{locale === "fa" ? "پیش‌نمایش" : "Preview"}</button>
            <button type="button" className="primary-pill" onClick={markActiveRecordEdited} disabled={!activeRecord}>{locale === "fa" ? "به‌روزرسانی رکورد" : "Update record"}</button>
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
          {stats.slice(0, 3).map((stat) => (
            <article key={stat.label} className="stat-card">
              <span className="muted">{stat.label}</span>
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
          {records.map((record) => (
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
          {!records.length ? (
            <div className="mobile-list-item">
              <strong>{locale === "fa" ? "رکوردی یافت نشد" : "No records found"}</strong>
              <span className="muted-block">{locale === "fa" ? "برای این entity داده‌ای از backend برنگشته است." : "The backend returned no data for this entity."}</span>
            </div>
          ) : null}
        </div>
        <div className="mobile-bottom-sheet">
          <div className="mobile-handle" />
          <div className="toolbar-row">
            <div>
              <strong>{activeRecord ? String(activeRecord.data.title ?? activeRecord.recordKey) : locale === "fa" ? "بدون رکورد" : "No record"}</strong>
              <span className="muted-block">{activeRecord ? String(activeRecord.data.category ?? "") : "—"}</span>
            </div>
            <button type="button" className="icon-pill">×</button>
          </div>
          <div className="three-column-grid" style={{ marginTop: 14 }}>
            <div className="mini-card">
              <span className="muted">Price</span>
              <strong>{activeRecord?.data.price ? `$${String(activeRecord.data.price)}` : "—"}</strong>
            </div>
            <div className="mini-card">
              <span className="muted">Stock</span>
              <strong>{String(activeRecord?.data.stock ?? "—")}</strong>
            </div>
            <div className="mini-card">
              <span className="muted">Status</span>
              <strong>{String(activeRecord?.data.status ?? "—")}</strong>
            </div>
          </div>
          <div className="toolbar-row" style={{ marginTop: 16 }}>
            <button type="button" className="secondary-pill">{locale === "fa" ? "پیش‌نمایش" : "Preview"}</button>
            <button type="button" className="primary-pill" onClick={markActiveRecordEdited} disabled={!activeRecord}>{locale === "fa" ? "به‌روزرسانی رکورد" : "Update record"}</button>
          </div>
        </div>
      </div>
    </PanelShell>
  );
}

function buildRecordData(bucket: EntityBucket, recordKey: string) {
  if (bucket.key === "catalog-product") {
    return {
      itemType: "PRODUCT",
      name: "Live Starter Product",
      sku: `LIVE-${recordKey.slice(-6).toUpperCase()}`,
      categoryKey: "platform",
      unit: "pcs",
      defaultPrice: 1250000,
      currency: "IRR",
      active: true,
      slug: recordKey.toLowerCase(),
      details: {
        brand: "Cyan",
        shortDescription: "Created from the live panel flow."
      }
    };
  }
  if (bucket.key === "landing-page") {
    return {
      slug: recordKey.toLowerCase(),
      title: "Live Landing Page",
      heroTitle: "Built from the panel",
      heroSubtitle: "This page was created during the live end-to-end flow.",
      publicationStatus: "DRAFT",
      sections: [
        {
          blockType: "TEXT",
          title: "Launch faster",
          body: "Connected to the real content-service and storefront route pipeline."
        }
      ]
    };
  }
  if (bucket.key === "crm-contact") {
    return {
      recordType: "CONTACT",
      fullName: "Live Customer",
      companyName: "Cyan Demo",
      email: `contact-${recordKey.slice(-6)}@example.com`,
      mobile: "+15550002222",
      status: "ACTIVE",
      source: "PANEL",
      notes: "Created from the live panel flow."
    };
  }
  return {
    catalogItemKey: "starter-product",
    warehouseKey: "main-warehouse",
    onHandQuantity: 12,
    reservedQuantity: 0,
    reorderPoint: 2,
    unit: "pcs"
  };
}

function buildEditedRecordData(bucket: EntityBucket, record: DynamicEntityRecord) {
  if (bucket.key === "catalog-product") {
    return {
      ...record.data,
      defaultPrice: Number(record.data.defaultPrice ?? 0) + 1000,
      active: true
    };
  }
  if (bucket.key === "landing-page") {
    return {
      ...record.data,
      heroSubtitle: "Updated from the live panel flow.",
      publicationStatus: "PUBLISHED"
    };
  }
  if (bucket.key === "crm-contact") {
    return {
      ...record.data,
      status: "ACTIVE",
      notes: "Updated from the live panel flow."
    };
  }
  return {
    ...record.data,
    onHandQuantity: Number(record.data.onHandQuantity ?? 0) + 5
  };
}

function toFaDigits(value: string) {
  return value.replace(/\d/g, (digit) => "۰۱۲۳۴۵۶۷۸۹"[Number(digit)] ?? digit);
}
