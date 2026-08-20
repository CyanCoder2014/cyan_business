"use client";

import { useMemo, useState, type CSSProperties, type DragEvent } from "react";
import { ConfirmDialog, Dialog, EmptyState } from "@/components/ui/primitives";
import { fieldTypeIcon } from "@/components/nav-icons";

export type SchemaRule = {
  id?: string;
  order?: number;
  type?: string;
  validation?: string;
  validationParams?: Record<string, unknown>;
  validationMessage?: string;
};

export type SchemaOperation = {
  id?: string;
  order?: number;
  type?: string;
  operation?: string;
  operationParams?: Record<string, unknown>;
  operationMessage?: string;
};

export type SchemaField = {
  id?: string;
  type?: string;
  defaultValue?: unknown;
  validations?: SchemaRule[];
  operations?: SchemaOperation[];
  itemValidations?: Record<string, SchemaField>;
  [key: string]: unknown;
};

type Props = {
  fields: Record<string, SchemaField>;
  locale: "en" | "fa";
  onChange: (fields: Record<string, SchemaField>) => void;
};

const FIELD_TYPES = ["string", "number", "integer", "boolean", "date", "datetime", "object", "list", "enum", "relation"];
const VALIDATORS: Record<string, string[]> = {
  REQUIRED: [], MIN_LENGTH: ["min"], MAX_LENGTH: ["max"], REGEX: ["pattern"],
  DECIMAL_MIN: ["min"], DECIMAL_MAX: ["max"], ENUM: ["values"], AntlrExpression: ["expression", "isRoot"]
};
const OPERATIONS: Record<string, string[]> = {
  SET_FIELD: ["field", "value"], COPY_FIELD: ["sourceField", "targetField"], SUM_FIELDS: ["sourceFields", "targetField"]
};

const pathKey = (path: string[]) => path.join(".");
const isContainer = (field?: SchemaField | null) => field?.type === "object" || field?.type === "list";

function getField(fields: Record<string, SchemaField>, path: string[]): SchemaField | null {
  let current: Record<string, SchemaField> = fields;
  let field: SchemaField | null = null;
  for (const key of path) {
    field = current[key] ?? null;
    if (!field) return null;
    current = field.itemValidations ?? {};
  }
  return field;
}

function updateAt(fields: Record<string, SchemaField>, path: string[], updater: (field: SchemaField) => SchemaField): Record<string, SchemaField> {
  const [key, ...rest] = path;
  if (!key || !fields[key]) return fields;
  if (!rest.length) return { ...fields, [key]: updater(fields[key]) };
  return { ...fields, [key]: { ...fields[key], itemValidations: updateAt(fields[key].itemValidations ?? {}, rest, updater) } };
}

function updateChildren(fields: Record<string, SchemaField>, parent: string[], updater: (children: Record<string, SchemaField>) => Record<string, SchemaField>): Record<string, SchemaField> {
  if (!parent.length) return updater(fields);
  return updateAt(fields, parent, field => ({ ...field, itemValidations: updater(field.itemValidations ?? {}) }));
}

function removeAt(fields: Record<string, SchemaField>, path: string[]): { fields: Record<string, SchemaField>; removed?: SchemaField } {
  const key = path.at(-1);
  const parent = path.slice(0, -1);
  if (!key) return { fields };
  const removed = getField(fields, path) ?? undefined;
  return { removed, fields: updateChildren(fields, parent, children => Object.fromEntries(Object.entries(children).filter(([candidate]) => candidate !== key))) };
}

function insertAfter(children: Record<string, SchemaField>, key: string, field: SchemaField, after?: string) {
  const entries = Object.entries(children).filter(([candidate]) => candidate !== key);
  const next: Array<[string, SchemaField]> = [];
  if (!after) next.push([key, field]);
  for (const entry of entries) {
    next.push(entry);
    if (entry[0] === after) next.push([key, field]);
  }
  if (!next.some(([candidate]) => candidate === key)) next.push([key, field]);
  return Object.fromEntries(next);
}

function parseRuleValue(key: string, value: string): unknown {
  if (key === "values" || key === "sourceFields") return value.split(",").map(item => item.trim()).filter(Boolean);
  if (key === "isRoot") return value === "true";
  if (["min", "max"].includes(key) && value !== "") return Number(value);
  return value;
}

function displayRuleValue(value: unknown) {
  return Array.isArray(value) ? value.join(", ") : value == null ? "" : String(value);
}

export function SchemaTreeEditor({ fields, locale, onChange }: Props) {
  const [selectedPath, setSelectedPath] = useState<string[]>([]);
  const [expanded, setExpanded] = useState<Set<string>>(new Set());
  const [dragged, setDragged] = useState<string[] | null>(null);
  const [addParent, setAddParent] = useState<string[] | null>(null);
  const [newKey, setNewKey] = useState("");
  const [newType, setNewType] = useState("string");
  const [deletePath, setDeletePath] = useState<string[] | null>(null);
  const selected = getField(fields, selectedPath);
  const allPaths = useMemo(() => {
    const values: string[] = [];
    const visit = (items: Record<string, SchemaField>, parent: string[] = []) => Object.entries(items).forEach(([key, field]) => {
      const path = [...parent, key]; values.push(pathKey(path)); visit(field.itemValidations ?? {}, path);
    });
    visit(fields); return values;
  }, [fields]);

  const openAdd = (parent: string[] = []) => { setAddParent(parent); setNewKey(""); setNewType("string"); };
  const add = () => {
    const key = newKey.trim();
    if (!key || !/^[A-Za-z][A-Za-z0-9_-]*$/.test(key) || getField(fields, [...(addParent ?? []), key])) return;
    const field: SchemaField = { id: key, type: newType, validations: [], operations: [], ...(["object", "list"].includes(newType) ? { itemValidations: {} } : {}) };
    const path = [...(addParent ?? []), key];
    onChange(updateChildren(fields, addParent ?? [], children => ({ ...children, [key]: field })));
    setExpanded(current => new Set(current).add(pathKey(addParent ?? [])));
    setSelectedPath(path); setAddParent(null);
  };
  const patchSelected = (patch: Partial<SchemaField>) => selected && onChange(updateAt(fields, selectedPath, field => ({ ...field, ...patch })));
  const moveSibling = (path: string[], offset: number) => {
    const key = path.at(-1); if (!key) return;
    const parent = path.slice(0, -1);
    onChange(updateChildren(fields, parent, children => {
      const entries = Object.entries(children); const index = entries.findIndex(([candidate]) => candidate === key); const target = index + offset;
      if (index < 0 || target < 0 || target >= entries.length) return children;
      [entries[index], entries[target]] = [entries[target], entries[index]]; return Object.fromEntries(entries);
    }));
  };
  const drop = (target: string[]) => {
    if (!dragged || pathKey(target).startsWith(`${pathKey(dragged)}.`) || pathKey(target) === pathKey(dragged)) return setDragged(null);
    const sourceKey = dragged.at(-1)!;
    const targetField = getField(fields, target);
    const destinationParent = isContainer(targetField) ? target : target.slice(0, -1);
    const after = isContainer(targetField) ? undefined : target.at(-1);
    const removed = removeAt(fields, dragged);
    if (!removed.removed) return setDragged(null);
    const moved = updateChildren(removed.fields, destinationParent, children => insertAfter(children, sourceKey, removed.removed!, after));
    const nextPath = [...destinationParent, sourceKey];
    onChange(moved); setSelectedPath(nextPath); setExpanded(current => new Set(current).add(pathKey(destinationParent))); setDragged(null);
  };

  return <div className="schema-tree-workspace">
    <aside className="schema-tree" aria-label={locale === "fa" ? "درخت ساختار موجودیت" : "Entity schema tree"}>
      <header><div><h2>{locale === "fa" ? "ساختار" : "Schema"}</h2><small>{fields ? Object.keys(fields).length : 0} {locale === "fa" ? "فیلد اصلی" : "root fields"}</small></div><button aria-label={locale === "fa" ? "افزودن فیلد اصلی" : "Add root field"} onClick={() => openAdd()}>＋</button></header>
      <div className="schema-tree-scroll">{Object.keys(fields).length ? <TreeLevel fields={fields} parent={[]} selected={selectedPath} expanded={expanded} setExpanded={setExpanded} select={setSelectedPath} add={openAdd} remove={setDeletePath} move={moveSibling} drag={setDragged} drop={drop} locale={locale}/> : <EmptyState title={locale === "fa" ? "هنوز فیلدی نیست" : "No fields yet"} description={locale === "fa" ? "اولین فیلد، شیء یا لیست را اضافه کنید." : "Add the first field, object, or list."} action={<button className="primary-pill" onClick={() => openAdd()}>＋ {locale === "fa" ? "افزودن فیلد" : "Add field"}</button>}/>}</div>
      <p className="schema-tree-help">{locale === "fa" ? "برای تو‌در‌تو کردن، فیلد را روی شیء یا لیست رها کنید. Alt+↑/↓ برای صفحه‌کلید." : "Drop a field on an object or list to nest it. Use Alt+↑/↓ with the keyboard."}</p>
    </aside>
    <aside className="schema-inspector">
      {selected ? <FieldInspector field={selected} path={selectedPath} locale={locale} patch={patchSelected} addChild={() => openAdd(selectedPath)} remove={() => setDeletePath(selectedPath)}/>:<EmptyState title={locale === "fa" ? "فیلدی انتخاب نشده" : "No field selected"} description={locale === "fa" ? "یک گره را برای نوع، اعتبارسنجی و عملیات انتخاب کنید." : "Select a node to edit its type, validations, and operations."}/>}
    </aside>
    <Dialog open={addParent !== null} onClose={() => setAddParent(null)} title={locale === "fa" ? "افزودن فیلد" : "Add field"} description={addParent?.length ? `${locale === "fa" ? "داخل" : "Inside"} ${pathKey(addParent)}` : (locale === "fa" ? "در ریشه موجودیت" : "At the entity root")} size="small"><div className="schema-add-form"><label><span>{locale === "fa" ? "کلید فیلد" : "Field key"}</span><input autoFocus dir="ltr" value={newKey} onChange={event => setNewKey(event.target.value)} placeholder="customerAddress"/></label><label><span>{locale === "fa" ? "نوع" : "Type"}</span><select value={newType} onChange={event => setNewType(event.target.value)}>{FIELD_TYPES.map(type => <option value={type} key={type}>{type}</option>)}</select></label><div className="dialog-actions"><button className="secondary-pill" onClick={() => setAddParent(null)}>{locale === "fa" ? "لغو" : "Cancel"}</button><button className="primary-pill" disabled={!/^[A-Za-z][A-Za-z0-9_-]*$/.test(newKey.trim()) || Boolean(getField(fields, [...(addParent ?? []), newKey.trim()]))} onClick={add}>{locale === "fa" ? "افزودن" : "Add field"}</button></div></div></Dialog>
    <ConfirmDialog open={deletePath !== null} title={locale === "fa" ? "حذف فیلد؟" : "Delete field?"} body={locale === "fa" ? "فیلدهای فرزند و قواعد آن نیز حذف می‌شوند." : "Nested fields and their rules will also be removed."} confirmLabel={locale === "fa" ? "حذف" : "Delete"} onClose={() => setDeletePath(null)} onConfirm={() => { if (!deletePath) return; onChange(removeAt(fields, deletePath).fields); if (pathKey(selectedPath) === pathKey(deletePath)) setSelectedPath([]); setDeletePath(null); }}/>
    <datalist id="schema-field-paths">{allPaths.map(path => <option value={path} key={path}/>)}</datalist>
  </div>;
}

function TreeLevel({ fields, parent, selected, expanded, setExpanded, select, add, remove, move, drag, drop, locale }: { fields: Record<string, SchemaField>; parent: string[]; selected: string[]; expanded: Set<string>; setExpanded: (value: Set<string>) => void; select: (path: string[]) => void; add: (path: string[]) => void; remove: (path: string[]) => void; move: (path: string[], offset: number) => void; drag: (path: string[] | null) => void; drop: (path: string[]) => void; locale: "en" | "fa" }) {
  return <ul>{Object.entries(fields).map(([key, field]) => { const path = [...parent, key]; const id = pathKey(path); const open = expanded.has(id); const container = isContainer(field); return <li key={id}>
    <div className={`schema-tree-row ${pathKey(selected) === id ? "selected" : ""}`} style={{ "--tree-depth": parent.length } as CSSProperties} draggable onDragStart={() => drag(path)} onDragEnd={() => drag(null)} onDragOver={(event: DragEvent) => event.preventDefault()} onDrop={(event: DragEvent) => { event.preventDefault(); drop(path); }}>
      <button className="tree-expander" aria-label={open ? "Collapse" : "Expand"} disabled={!container} onClick={() => { const next = new Set(expanded); open ? next.delete(id) : next.add(id); setExpanded(next); }}>{container ? (open ? "⌄" : "›") : "·"}</button>
      <button className="tree-field" onClick={() => select(path)} onKeyDown={event => { if (event.altKey && event.key === "ArrowUp") { event.preventDefault(); move(path, -1); } if (event.altKey && event.key === "ArrowDown") { event.preventDefault(); move(path, 1); } }}>{(() => { const TypeIcon = fieldTypeIcon(field.type); return <span className={`field-type-icon ${field.type}`}><TypeIcon size={14}/></span>; })()}<span><strong>{key}</strong><small>{field.type ?? "string"} · {field.validations?.length ?? 0} {locale === "fa" ? "قاعده" : "rules"} · {field.operations?.length ?? 0} {locale === "fa" ? "عملیات" : "ops"}</small></span></button>
      {container ? <button className="tree-action" aria-label={locale === "fa" ? "افزودن فیلد فرزند" : "Add child field"} onClick={() => add(path)}>＋</button> : null}<button className="tree-action danger" aria-label={locale === "fa" ? "حذف فیلد" : "Delete field"} onClick={() => remove(path)}>×</button>
    </div>{container && open ? <TreeLevel fields={field.itemValidations ?? {}} parent={path} selected={selected} expanded={expanded} setExpanded={setExpanded} select={select} add={add} remove={remove} move={move} drag={drag} drop={drop} locale={locale}/> : null}
  </li>; })}</ul>;
}

function FieldInspector({ field, path, locale, patch, addChild, remove }: { field: SchemaField; path: string[]; locale: "en" | "fa"; patch: (patch: Partial<SchemaField>) => void; addChild: () => void; remove: () => void }) {
  const validations = field.validations ?? [];
  const operations = field.operations ?? [];
  const updateValidation = (index: number, value: SchemaRule) => patch({ validations: validations.map((rule, itemIndex) => itemIndex === index ? value : rule) });
  const updateOperation = (index: number, value: SchemaOperation) => patch({ operations: operations.map((rule, itemIndex) => itemIndex === index ? value : rule) });
  return <><header><div><small dir="ltr">{pathKey(path)}</small><h2>{path.at(-1)}</h2></div><button className="danger" aria-label={locale === "fa" ? "حذف فیلد" : "Delete field"} onClick={remove}>×</button></header>
    <label><span>{locale === "fa" ? "نوع فیلد" : "Field type"}</span><select value={field.type ?? "string"} onChange={event => { const type = event.target.value; patch({ type, ...(isContainer({ type }) ? { itemValidations: field.itemValidations ?? {} } : {}) }); }}>{FIELD_TYPES.map(type => <option value={type} key={type}>{type}</option>)}</select></label>
    <label><span>{locale === "fa" ? "مقدار پیش‌فرض" : "Default value"}</span><input value={field.defaultValue == null ? "" : String(field.defaultValue)} onChange={event => patch({ defaultValue: event.target.value })}/></label>
    {isContainer(field) ? <button className="secondary-pill" onClick={addChild}>＋ {locale === "fa" ? "افزودن فیلد فرزند" : "Add child field"}</button> : null}
    <RuleEditor title={locale === "fa" ? "اعتبارسنجی‌ها" : "Validations"} rules={validations} kinds={VALIDATORS} locale={locale} kindOf={rule => rule.validation ?? "REQUIRED"} paramsOf={rule => rule.validationParams ?? {}} messageOf={rule => rule.validationMessage ?? ""} add={() => patch({ validations: [...validations, { id: crypto.randomUUID(), order: validations.length + 1, validation: "REQUIRED", validationParams: {} }] })} update={(index, kind, params, message) => updateValidation(index, { ...validations[index], order: index + 1, validation: kind, validationParams: params, validationMessage: message })} removeRule={index => patch({ validations: validations.filter((_, itemIndex) => itemIndex !== index).map((rule, order) => ({ ...rule, order: order + 1 })) })}/>
    <RuleEditor title={locale === "fa" ? "عملیات فیلد" : "Field operations"} rules={operations} kinds={OPERATIONS} locale={locale} kindOf={rule => rule.operation ?? "SET_FIELD"} paramsOf={rule => rule.operationParams ?? {}} messageOf={rule => rule.operationMessage ?? ""} add={() => patch({ operations: [...operations, { id: crypto.randomUUID(), order: operations.length + 1, operation: "SET_FIELD", operationParams: { field: pathKey(path), value: "" } }] })} update={(index, kind, params, message) => updateOperation(index, { ...operations[index], order: index + 1, operation: kind, operationParams: params, operationMessage: message })} removeRule={index => patch({ operations: operations.filter((_, itemIndex) => itemIndex !== index).map((rule, order) => ({ ...rule, order: order + 1 })) })}/>
  </>;
}

function RuleEditor<T>({ title, rules, kinds, locale, kindOf, paramsOf, messageOf, add, update, removeRule }: { title: string; rules: T[]; kinds: Record<string, string[]>; locale: "en" | "fa"; kindOf: (rule: T) => string; paramsOf: (rule: T) => Record<string, unknown>; messageOf: (rule: T) => string; add: () => void; update: (index: number, kind: string, params: Record<string, unknown>, message: string) => void; removeRule: (index: number) => void }) {
  return <section className="schema-rule-editor"><header><div><strong>{title}</strong><small>{rules.length}</small></div><button aria-label={`${locale === "fa" ? "افزودن" : "Add"} ${title}`} onClick={add}>＋</button></header>{rules.length ? rules.map((rule, index) => { const kind = kindOf(rule); const params = paramsOf(rule); return <article key={index}><div className="schema-rule-head"><span>{index + 1}</span><select value={kind} onChange={event => update(index, event.target.value, {}, messageOf(rule))}>{Object.keys(kinds).map(item => <option value={item} key={item}>{item}</option>)}</select><button aria-label={locale === "fa" ? "حذف قاعده" : "Remove rule"} onClick={() => removeRule(index)}>×</button></div>{(kinds[kind] ?? []).map(param => <label key={param}><span>{param}</span>{param === "isRoot" ? <select value={String(params[param] ?? false)} onChange={event => update(index, kind, { ...params, [param]: event.target.value === "true" }, messageOf(rule))}><option value="false">false</option><option value="true">true</option></select> : <input dir="ltr" list={param.toLowerCase().includes("field") ? "schema-field-paths" : undefined} value={displayRuleValue(params[param])} placeholder={param === "values" || param === "sourceFields" ? "value1, value2" : param} onChange={event => update(index, kind, { ...params, [param]: parseRuleValue(param, event.target.value) }, messageOf(rule))}/>}</label>)}<label><span>{locale === "fa" ? "پیام خطا / توضیح" : "Message"}</span><input value={messageOf(rule)} onChange={event => update(index, kind, params, event.target.value)}/></label></article>; }) : <p className="muted">{locale === "fa" ? "قاعده‌ای تعریف نشده است." : "No rules configured."}</p>}</section>;
}
