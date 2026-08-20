"use client";

import { useState, type DragEvent } from "react";
import { EmptyState } from "@/components/ui/primitives";
import { sectionTypeIcon, type SectionType } from "@/components/nav-icons";
import { SECTION_TYPES, sectionLabel, type SiteSection } from "@/lib/site-sections";

type Props = {
  sections: SiteSection[];
  selectedKey: string | null;
  locale: "en" | "fa";
  onSelect: (sectionKey: string) => void;
  onReorder: (fromKey: string, toKey: string) => void;
  onToggleVisible: (sectionKey: string) => void;
  onRemove: (sectionKey: string) => void;
  onAdd: (type: SectionType) => void;
};

export function SectionListEditor({ sections, selectedKey, locale, onSelect, onReorder, onToggleVisible, onRemove, onAdd }: Props) {
  const [dragged, setDragged] = useState<string | null>(null);
  const [pickerOpen, setPickerOpen] = useState(false);
  const move = (sectionKey: string, offset: number) => {
    const index = sections.findIndex((section) => section.sectionKey === sectionKey);
    const target = index + offset;
    if (index < 0 || target < 0 || target >= sections.length) return;
    onReorder(sectionKey, sections[target].sectionKey);
  };
  return <div className="section-list-editor">
    <div className="section-list-rows">
      {sections.length ? sections.map((section) => {
        const Icon = sectionTypeIcon(section.type);
        return <div key={section.sectionKey}
          className={`section-list-row ${selectedKey === section.sectionKey ? "selected" : ""} ${section.visible === "false" ? "hidden-section" : ""}`}
          draggable
          onDragStart={() => setDragged(section.sectionKey)}
          onDragEnd={() => setDragged(null)}
          onDragOver={(event: DragEvent) => event.preventDefault()}
          onDrop={(event: DragEvent) => { event.preventDefault(); if (dragged && dragged !== section.sectionKey) onReorder(dragged, section.sectionKey); setDragged(null); }}>
          <span className="section-list-drag" aria-hidden>⠿</span>
          <span className="section-list-icon" aria-hidden><Icon size={15} /></span>
          <button className="section-list-select" onClick={() => onSelect(section.sectionKey)}
            onKeyDown={(event) => { if (event.altKey && event.key === "ArrowUp") { event.preventDefault(); move(section.sectionKey, -1); } if (event.altKey && event.key === "ArrowDown") { event.preventDefault(); move(section.sectionKey, 1); } }}>
            <strong>{sectionLabel(section)}</strong><small>{section.type}</small>
          </button>
          <button className="section-list-action" aria-label={locale === "fa" ? "پنهان/آشکار" : "Toggle visible"} onClick={() => onToggleVisible(section.sectionKey)}>{section.visible === "false" ? "◌" : "●"}</button>
          <button className="section-list-action danger" aria-label={locale === "fa" ? "حذف بخش" : "Remove section"} onClick={() => onRemove(section.sectionKey)}>×</button>
        </div>;
      }) : <EmptyState title={locale === "fa" ? "بخشی نیست" : "No sections yet"} description={locale === "fa" ? "یک بخش برای شروع اضافه کنید." : "Add a section to start building the page."} />}
    </div>
    <div className="section-add-wrap">
      <button className="secondary-pill" onClick={() => setPickerOpen((value) => !value)}>＋ {locale === "fa" ? "افزودن بخش" : "Add section"}</button>
      {pickerOpen ? <div className="section-add-palette">{SECTION_TYPES.map(({ type, en, fa }) => { const Icon = sectionTypeIcon(type); return <button key={type} onClick={() => { onAdd(type); setPickerOpen(false); }}><Icon size={16} /><span>{locale === "fa" ? fa : en}</span></button>; })}</div> : null}
    </div>
  </div>;
}
