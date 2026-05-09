# API Docs

Generated assets:
- `docs/postman/cyan-business-platform.postman_collection.json`
- `docs/postman/cyan-business-platform.postman_environment.template.json`
- `docs/swagger/cyan-business-platform.openapi.json`
- `docs/swagger/services/*.openapi.json`
- `docs/swagger/index.html`

Usage:
1. Import the Postman collection and environment template.
2. Run `SSO / Login` first. Its test script stores `access_token`, `refresh_token`, and `session_id` in the environment.
3. Open `docs/swagger/index.html` in a browser, then use Swagger's `Authorize` button with either a bearer token or internal basic credentials.
4. Use the Swagger spec selector to switch between the full platform inventory and per-service specs.

Coverage tags:
- `AI Orchestrator`
- `AI Orchestrator Internal`
- `Automation Orchestrator`
- `BPM`
- `BPM Internal Flow`
- `BPM Internal Managed Objects`
- `BPM Internal Metadata`
- `CRM`
- `CRM Automation`
- `Catalog`
- `Checkout`
- `Commerce`
- `Content`
- `Dynamic Entity`
- `Dynamic Entity Internal`
- `Event`
- `Finance`
- `Finance Automation`
- `Inventory`
- `Inventory Automation`
- `Legacy Buyer`
- `Legacy Client`
- `Legacy Factor`
- `Legacy Product`
- `Media`
- `Notification`
- `Notification Internal`
- `Payment`
- `Payment Internal`
- `Payment Orchestrator`
- `Payment Public`
- `Pricing Promotion`
- `Processor`
- `Report`
- `Report Automation`
- `Report Internal`
- `SSO`
- `Search`
- `Search Internal`
- `Storefront`
- `Tax Pay Sys`

Regenerate:
- `python3 scripts/generate_api_docs.py`
