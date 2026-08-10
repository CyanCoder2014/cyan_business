# UI Review Checklist

## User flow
- [ ] Primary task and action are clear.
- [ ] Secondary actions do not compete.
- [ ] Destructive actions require confirmation.

## API truthfulness
- [ ] All displayed data comes from real APIs.
- [ ] No hardcoded tenant/site IDs.
- [ ] No fake success, previews, metrics, or seeded messages.
- [ ] Loading, empty, partial, error, stale, and retry states exist.
- [ ] Permission, plan, and capability states are distinct.

## Visual quality
- [ ] Title is not oversized.
- [ ] Spacing and colors use tokens.
- [ ] Cards are used only when containment helps.
- [ ] Brand gradient is restrained.
- [ ] Status is not color-only.
- [ ] Long text and IDs are handled.

## Responsive/PWA
- [ ] 1440, 1024, 834, 390, and 360 widths reviewed.
- [ ] Mobile is not a shrunken desktop.
- [ ] Safe areas and virtual keyboard are considered.
- [ ] Offline/stale state is truthful.

## Locale/theme
- [ ] English/Roboto verified.
- [ ] Farsi/Vazir verified.
- [ ] LTR/RTL verified.
- [ ] Code/URL/key fields stay LTR.
- [ ] Light and dark modes verified.

## Accessibility
- [ ] Keyboard flow works.
- [ ] Focus is visible.
- [ ] Icon buttons have names.
- [ ] No nested interactive controls.
- [ ] Dialog focus is managed.
- [ ] Touch targets are at least 44px.
- [ ] Reduced motion and AA contrast verified.

## Completion
- [ ] Lint passes.
- [ ] Build passes.
- [ ] Relevant tests pass.
- [ ] Desktop/mobile screenshots were reviewed.
- [ ] Remaining backend gaps are documented.
