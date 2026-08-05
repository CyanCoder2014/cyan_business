# Visual Deliverables Manifest

Static screenshots alone are insufficient for complex builders. The design handoff should combine:

1. annotated screen images
2. design tokens JSON
3. component/state specifications
4. responsive behavior notes
5. API/state mapping
6. clickable prototype or coded reference where interaction is critical

## 1. Viewports

- Desktop: 1440×1024
- Wide builder: 1600×1000
- Tablet: 834×1112
- Mobile: 390×844
- Small mobile validation: 360×800

## 2. Required variants

For every Tier 1 screen:
- English light
- English dark
- Farsi light RTL
- Farsi dark RTL
- desktop
- mobile

That is eight variants per Tier 1 screen.

## 3. Tier 1 screens

1. Auth sign in
2. Registration/OAuth
3. No-plan dashboard
4. Full dashboard
5. AI Studio empty
6. AI Studio active generation
7. Project workspace overview
8. Definition editor
9. Dynamic data grid
10. Dynamic record form
11. Automation canvas
12. Automation node inspector
13. Automation execution detail
14. BPM designer
15. BPM state inspector
16. Work queue
17. Active work item/form
18. Team users
19. Role/permission editor
20. Billing/plan
21. Bot integration
22. Site builder
23. Domain manager
24. Notification center

## 4. State images

Each major screen also needs targeted state frames:
- loading skeleton
- empty
- partial service failure
- validation errors
- permission denied
- plan locked
- offline/stale
- destructive confirmation
- success toast
- mobile bottom sheet open

## 5. Automation-specific frames

- node palette
- IF branches
- loop feedback
- merge multi-input
- expression editor
- credential reference picker
- schedule trigger
- import analysis with unsupported nodes
- running execution with live node states
- failed node with retry
- callback wait
- execution history

## 6. BPM-specific frames

- blank flow
- populated flow
- state form mapping
- transition condition builder
- action metadata editor
- RUN_AUTOMATION_BLOCK mapping
- validation errors on graph
- work queue
- active form
- transition confirmation
- comments/attachments
- history timeline

## 7. Figma-compatible handoff

Recommended:
- import `design-tokens.tokens.json` using Tokens Studio
- build components with variants:
  - theme: light/dark
  - locale direction: LTR/RTL
  - size: desktop/mobile
  - state: default/hover/focus/disabled/loading/error
- name frames by route and state:
  - `automation/editor/desktop/en/light/default`
  - `automation/editor/mobile/fa/dark/node-selected`

A native `.fig` file cannot be reliably reconstructed from generated raster images. Use the token file and coded component specs as the source for a real Figma system.
