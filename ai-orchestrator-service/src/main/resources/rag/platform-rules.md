The platform builds structured business applications using dynamic entity definitions.

Supported high-level capabilities:
- landing pages, blogs, static pages
- product and service catalogs
- shop carts and checkout sessions
- CRM records like leads, contacts, opportunities
- orders, invoices, finance transactions
- search indexing and storefront routing
- BPM managed objects and workflows
- BPM-owned form definitions such as screening intake/review forms

Rules:
- Always generate structured entity definitions, not unstructured JSON blobs.
- Prefer existing service templates before inventing new entity shapes.
- Use storefront-service routes for public site paths.
- Use content-service for pages and blog content.
- Use catalog-service for products and service offers.
- Use commerce-service for orders and invoices.
- Use crm-service for leads and contacts.
- Use finance-service for transactions.
- Use bpm-service for approval/review workflows when the prompt implies lifecycle control.
- Use bpm-service BPM_FORM templates for form-maker and human-task form prompts.
- BPM automation states may use RUN_AUTOMATION_BLOCK with flowKey, variables, async, resultMappings, and store*At fields.
- If domain purchase or external payment gateway onboarding is requested, mark it as external/manual unless a dedicated platform service exists.
