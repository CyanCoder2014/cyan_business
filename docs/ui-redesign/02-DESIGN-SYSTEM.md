# Cyan Design System

## 1. Visual direction

- Minimal, modern, enterprise-capable SaaS.
- White or deep navy surfaces with restrained cyan/blue/violet gradients.
- Strong hierarchy without oversized marketing typography inside the panel.
- Dense builders use compact controls and larger canvases.
- Cards are functional containers, not decoration.
- Avoid excessive glass effects that reduce contrast.

## 2. Typography

### Fonts
- English: **Roboto**
- Farsi: **Vazir**
- Code/DSL: `Roboto Mono`, `SFMono-Regular`, `Consolas`, monospace

For PWA/offline reliability, self-host the project-approved font assets and load with `next/font/local`. Do not depend on a remote font request at runtime.

### CSS variables

```css
:root {
  --font-ui-ltr: "Roboto", system-ui, -apple-system, "Segoe UI", sans-serif;
  --font-ui-rtl: "Vazir", Tahoma, Arial, sans-serif;
  --font-code: "Roboto Mono", ui-monospace, SFMono-Regular, Consolas, monospace;
}

html[dir="ltr"] body { font-family: var(--font-ui-ltr); }
html[dir="rtl"] body { font-family: var(--font-ui-rtl); }
```

### Type scale

| Token | Desktop | Mobile | Usage |
|---|---:|---:|---|
| `display` | 32/40, 700 | 26/34, 700 | Auth/empty marketing only |
| `page-title` | 26/34, 700 | 22/30, 700 | Panel page titles |
| `section-title` | 18/26, 600 | 17/24, 600 | Major sections |
| `card-title` | 15/22, 600 | 15/22, 600 | Cards |
| `body` | 14/22, 400 | 14/22, 400 | Default |
| `body-sm` | 13/20, 400 | 13/20, 400 | Secondary |
| `label` | 12/18, 500 | 12/18, 500 | Field labels |
| `caption` | 11/16, 400 | 11/16, 400 | Metadata |

Panel titles must not exceed `32px`; normal route titles should use `26px` desktop and `22px` mobile.

## 3. Spacing and radius

Base spacing unit: `4px`.

```text
space-1  4
space-2  8
space-3  12
space-4  16
space-5  20
space-6  24
space-8  32
space-10 40
```

Radii:

```text
control  10px
card     14px
panel    18px
drawer   20px
pill     999px
```

## 4. Color tokens

### Light
- App background: `#F7F9FC`
- Surface: `#FFFFFF`
- Elevated surface: `#FFFFFF`
- Border: `#E3E8F2`
- Strong text: `#10172A`
- Body text: `#4E5A73`
- Muted text: `#7D889E`

### Dark
- App background: `#07111F`
- Surface: `#0C1728`
- Elevated surface: `#111E31`
- Border: `#24324A`
- Strong text: `#F4F7FC`
- Body text: `#B5C0D3`
- Muted text: `#8491A8`

### Brand
- Cyan: `#11B7F4`
- Blue: `#2878FF`
- Violet: `#7A42F4`
- Gradient: `linear-gradient(100deg, #0EA5F5, #3568F5 52%, #813CF0)`

Status:
- Success `#19B56B`
- Warning `#F5A524`
- Danger `#E5484D`
- Info `#2F80ED`

Never use color as the only status indicator.

## 5. Layout

### Desktop
- Sidebar: 248px expanded, 72px collapsed.
- Header: 64px.
- Page content max width: none for builders; 1440px for ordinary pages.
- Standard page padding: 24px.
- Builder padding: 16px.
- Detail inspector: 340–400px.
- Node palette: 260–300px.

### Tablet
- Collapsed sidebar or overlay drawer.
- Inspectors become drawers.
- Tables switch to prioritized columns plus row detail.

### Mobile
- Minimum supported width: 360px.
- Bottom navigation height: 64–72px plus safe area.
- Sticky primary action may sit above bottom navigation.
- Builder inspectors use bottom sheets.
- Avoid independently authored desktop/mobile business logic. Use shared data and action hooks.

## 6. Responsive strategy

Use one semantic component tree where possible:

```tsx
<Page>
  <PageHeader />
  <ResponsiveWorkspace
    desktop={<ThreePaneBuilder />}
    mobile={<MobileBuilderView />}
  />
</Page>
```

Different presentation is acceptable for graph canvases, but both views must use the same query/mutation hooks and validation schema.

Suggested breakpoints:

```css
--bp-sm: 480px;
--bp-md: 768px;
--bp-lg: 1024px;
--bp-xl: 1280px;
```

## 7. RTL rules

- Mirror structural layout, drawers, breadcrumbs, icon placement, and directional arrows.
- Do not mirror non-directional icons.
- Use logical CSS properties: `margin-inline`, `padding-inline`, `inset-inline`.
- Tables preserve semantic column order but begin from the reading edge.
- Graph coordinates do not need automatic mirroring; node text and controls do.
- Use Persian numerals only when the locale preference explicitly requests them; IDs, code, URLs, and API keys remain Latin.
- Text inputs containing URLs, JSON, code, email, or identifiers use `dir="ltr"` inside an RTL page.

## 8. Shared components required before route redesign

- `AppShell`
- `CapabilityNavigation`
- `WorkspaceSiteSelector`
- `PageHeader`
- `Button`
- `IconButton`
- `Field`
- `Select`
- `Combobox`
- `Tabs`
- `SegmentedControl`
- `Badge`
- `StatusBadge`
- `Card`
- `Drawer`
- `Dialog`
- `BottomSheet`
- `Toast`
- `ConfirmDialog`
- `Skeleton`
- `EmptyState`
- `ErrorState`
- `PermissionState`
- `PlanGate`
- `DataGrid`
- `RecordForm`
- `DefinitionFieldEditor`
- `CodeViewer`
- `JsonDiff`
- `ActivityTimeline`
- `ExecutionLog`
- `ResponsiveInspector`

## 9. Accessibility

- WCAG 2.2 AA contrast.
- Visible keyboard focus.
- No nested buttons.
- Icon-only buttons require accessible names and tooltips.
- Dialogs/drawers trap focus and restore it on close.
- Drag-and-drop has keyboard alternatives.
- Graph nodes are reachable and selectable by keyboard.
- Reduced-motion mode disables animated connector flow and large transitions.
- Touch targets are at least 44×44px.
