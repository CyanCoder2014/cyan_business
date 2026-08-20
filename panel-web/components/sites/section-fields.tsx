"use client";

import type { SiteSection } from "@/lib/site-sections";

type Props = { section: SiteSection; locale: "en" | "fa"; onChange: (next: SiteSection) => void };

function TextField({ label, value, onChange, textarea }: { label: string; value: string; onChange: (value: string) => void; textarea?: boolean }) {
  return <label className="ui-field"><span>{label}</span>{textarea ? <textarea value={value} onChange={(event) => onChange(event.target.value)} /> : <input value={value} onChange={(event) => onChange(event.target.value)} />}</label>;
}

function str(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function RepeatableRows({ label, addLabel, items, fields, onChange }: {
  label: string;
  addLabel: string;
  items: Array<Record<string, unknown>>;
  fields: Array<{ key: string; label: string; textarea?: boolean }>;
  onChange: (items: Array<Record<string, unknown>>) => void;
}) {
  const update = (index: number, key: string, value: string) => {
    const next = items.map((item, itemIndex) => (itemIndex === index ? { ...item, [key]: value } : item));
    onChange(next);
  };
  return <div className="repeatable-rows">
    <span className="repeatable-rows-label">{label}</span>
    {items.map((item, index) => <div className="repeatable-row" key={index}>
      <div className="repeatable-row-fields">{fields.map((field) => <TextField key={field.key} label={field.label} value={str(item[field.key])} onChange={(value) => update(index, field.key, value)} textarea={field.textarea} />)}</div>
      <button className="repeatable-row-remove" aria-label="Remove" onClick={() => onChange(items.filter((_, itemIndex) => itemIndex !== index))}>×</button>
    </div>)}
    <button className="secondary-pill" onClick={() => onChange([...items, Object.fromEntries(fields.map((field) => [field.key, ""]))])}>＋ {addLabel}</button>
  </div>;
}

export function SectionFields({ section, locale, onChange }: Props) {
  const fa = locale === "fa";
  const content = section.content ?? {};
  const setContent = (patch: Record<string, unknown>) => onChange({ ...section, content: { ...content, ...patch } });
  const items = (key: string) => (Array.isArray(content[key]) ? (content[key] as Array<Record<string, unknown>>) : []);

  return <div className="section-fields">
    <section className="section-fields-group">
      <h3>{fa ? "محتوا" : "Content"}</h3>
      {section.type === "hero" ? <>
        <TextField label={fa ? "عنوان" : "Heading"} value={str(content.heading)} onChange={(value) => setContent({ heading: value })} />
        <TextField label={fa ? "زیرعنوان" : "Subheading"} value={str(content.subheading)} onChange={(value) => setContent({ subheading: value })} textarea />
        <TextField label={fa ? "متن دکمه اصلی" : "Primary button label"} value={str(content.primaryButtonLabel)} onChange={(value) => setContent({ primaryButtonLabel: value })} />
        <TextField label={fa ? "لینک دکمه اصلی" : "Primary button link"} value={str(content.primaryButtonHref)} onChange={(value) => setContent({ primaryButtonHref: value })} />
        <TextField label={fa ? "متن دکمه دوم" : "Secondary button label"} value={str(content.secondaryButtonLabel)} onChange={(value) => setContent({ secondaryButtonLabel: value })} />
        <TextField label={fa ? "لینک دکمه دوم" : "Secondary button link"} value={str(content.secondaryButtonHref)} onChange={(value) => setContent({ secondaryButtonHref: value })} />
      </> : null}
      {section.type === "cta" ? <>
        <TextField label={fa ? "عنوان" : "Heading"} value={str(content.heading)} onChange={(value) => setContent({ heading: value })} />
        <TextField label={fa ? "زیرعنوان" : "Subheading"} value={str(content.subheading)} onChange={(value) => setContent({ subheading: value })} textarea />
        <TextField label={fa ? "متن دکمه" : "Button label"} value={str(content.buttonLabel)} onChange={(value) => setContent({ buttonLabel: value })} />
        <TextField label={fa ? "لینک دکمه" : "Button link"} value={str(content.buttonHref)} onChange={(value) => setContent({ buttonHref: value })} />
      </> : null}
      {section.type === "features" ? <>
        <TextField label={fa ? "عنوان" : "Heading"} value={str(content.heading)} onChange={(value) => setContent({ heading: value })} />
        <TextField label={fa ? "زیرعنوان" : "Subheading"} value={str(content.subheading)} onChange={(value) => setContent({ subheading: value })} textarea />
        <RepeatableRows label={fa ? "ویژگی‌ها" : "Features"} addLabel={fa ? "افزودن ویژگی" : "Add feature"} items={items("items")} fields={[{ key: "title", label: fa ? "عنوان" : "Title" }, { key: "description", label: fa ? "توضیح" : "Description", textarea: true }]} onChange={(value) => setContent({ items: value })} />
      </> : null}
      {section.type === "testimonials" ? <>
        <TextField label={fa ? "عنوان" : "Heading"} value={str(content.heading)} onChange={(value) => setContent({ heading: value })} />
        <RepeatableRows label={fa ? "نظرات" : "Testimonials"} addLabel={fa ? "افزودن نظر" : "Add testimonial"} items={items("items")} fields={[{ key: "quote", label: fa ? "متن نظر" : "Quote", textarea: true }, { key: "author", label: fa ? "نام" : "Author" }, { key: "role", label: fa ? "سمت/شرکت" : "Role / company" }]} onChange={(value) => setContent({ items: value })} />
      </> : null}
      {section.type === "faq" ? <>
        <TextField label={fa ? "عنوان" : "Heading"} value={str(content.heading)} onChange={(value) => setContent({ heading: value })} />
        <RepeatableRows label={fa ? "سوالات" : "Questions"} addLabel={fa ? "افزودن سوال" : "Add question"} items={items("items")} fields={[{ key: "question", label: fa ? "سوال" : "Question" }, { key: "answer", label: fa ? "پاسخ" : "Answer", textarea: true }]} onChange={(value) => setContent({ items: value })} />
      </> : null}
      {section.type === "footer" ? <>
        <TextField label={fa ? "متن" : "Text"} value={str(content.text)} onChange={(value) => setContent({ text: value })} />
        <RepeatableRows label={fa ? "لینک‌ها" : "Links"} addLabel={fa ? "افزودن لینک" : "Add link"} items={items("links")} fields={[{ key: "label", label: fa ? "برچسب" : "Label" }, { key: "href", label: fa ? "لینک" : "Link" }]} onChange={(value) => setContent({ links: value })} />
      </> : null}
    </section>
    <section className="section-fields-group">
      <h3>{fa ? "ظاهر" : "Style"}</h3>
      <label className="ui-field"><span>{fa ? "رنگ پس‌زمینه" : "Background color"}</span>
        <div className="color-field"><input type="color" value={/^#([0-9a-f]{3}){1,2}$/i.test(section.style.backgroundColor ?? "") ? section.style.backgroundColor : "#ffffff"} onChange={(event) => onChange({ ...section, style: { ...section.style, backgroundColor: event.target.value } })} />
          <input value={section.style.backgroundColor ?? ""} placeholder={fa ? "خالی = پیش‌فرض" : "Empty = default"} onChange={(event) => onChange({ ...section, style: { ...section.style, backgroundColor: event.target.value } })} /></div>
      </label>
      <label className="ui-field"><span>{fa ? "فاصله عمودی" : "Padding"}</span>
        <select value={section.style.padding ?? "md"} onChange={(event) => onChange({ ...section, style: { ...section.style, padding: event.target.value as SiteSection["style"]["padding"] } })}>
          <option value="sm">{fa ? "کم" : "Small"}</option><option value="md">{fa ? "متوسط" : "Medium"}</option><option value="lg">{fa ? "زیاد" : "Large"}</option>
        </select>
      </label>
      <div className="align-toggle" role="group" aria-label={fa ? "چیدمان" : "Alignment"}>
        {(["start", "center", "end"] as const).map((align) => <button key={align} className={section.style.align === align || (!section.style.align && align === "start") ? "active" : ""} onClick={() => onChange({ ...section, style: { ...section.style, align } })}>{align}</button>)}
      </div>
    </section>
  </div>;
}
