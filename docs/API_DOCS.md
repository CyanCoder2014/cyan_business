# API Docs

The authoritative API documentation is generated from live Spring controllers.
See [`DYNAMIC_OPENAPI_PLATFORM.md`](DYNAMIC_OPENAPI_PLATFORM.md).

Primary export:

```bash
export API_DOCS_CATALOG_URL=http://localhost:9128/internal/api-docs
export API_DOCS_USERNAME=api_docs_internal
export API_DOCS_PASSWORD=api_docs_secret
python3 scripts/export_live_api_docs.py --refresh
```

The assets below are legacy offline snapshots. They are not authoritative when
controllers have changed.

Generated assets:
- `docs/postman/cyan-business-platform.postman_collection.json`
- `docs/postman/cyan-business-platform.postman_environment.template.json`
- `docs/swagger/cyan-business-platform.openapi.json`
- `docs/swagger/services/*.openapi.json`
- `docs/swagger/index.html`

Usage:
1. Import the Postman collection and environment template.
2. Run `SSO / Login` first. Its test script stores `access_token`, `refresh_token`, and `session_id` in the environment.
3. Set `dynamic_service_base_url` to the dynamic service under test; it defaults to local `bpm-service` on port `9119`.
4. Definition list requests use `definition_page`, `definition_page_size`, and `definition_sort`; their tests verify the pagination envelope.
5. Batch, automation, BPM, and API Docs internal folders use their own `*_internal_username` and secret `*_internal_password` variables.
6. `Start Batch Run`, `Start Automation Execution`, and credential/managed-object creation requests store their returned IDs for later requests.
7. Open `docs/swagger/index.html` in a browser, then use Swagger's `Authorize` button with either a bearer token or internal basic credentials.
8. Use the Swagger spec selector to switch between the full platform inventory and per-service specs.

Coverage tags:
- `AI Orchestrator`
- `AI Orchestrator Internal`
- `API Docs Catalog`
- `API Docs Catalog Internal`
- `Automation Credentials`
- `Automation Flows`
- `Automation Flows Internal`
- `Automation Orchestrator`
- `Automation Orchestrator Internal`
- `Automation Public`
- `BPM`
- `BPM Internal Flow`
- `BPM Internal Managed Objects`
- `BPM Internal Metadata`
- `BPM Public Metadata`
- `Batch Worker`
- `Batch Worker Internal`
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
