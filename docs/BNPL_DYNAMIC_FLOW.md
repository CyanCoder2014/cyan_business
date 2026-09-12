# Dynamic BNPL journey: BPM, automation, and rules

## Status and constraints

This is a **configuration-only design and gap report**, not a claim that the complete journey is deployed or production-ready.

- BNPL-specific material belongs only in this Markdown file. Do not create BNPL Java classes, controllers, configuration files, scripts, or standalone JSON files.
- Do not depend on `finance-service`: it is not deployed on the target server.
- Forms, states, transitions, inquiry subflows, credit policy, sponsor selection, account opening, and disbursement must be configured through generic platform capabilities.
- Any necessary engine changes must be reusable across business scenarios and approved before implementation. No BNPL-specific HTTP client, backend facade, or hardcoded state machine is acceptable.
- Existing SSO is the intended applicant authentication mechanism. Provider contracts and credit rules below are demonstration assumptions, not verified bank specifications or Central Bank policy.
- No external provider calls or server provisioning were performed for this document. The attempted implementation was removed, including its unfinished generic engine changes.

## Intended journey

| Stage | Owner | Behavior | Applicant interaction |
| --- | --- | --- | --- |
| Identity input | BPM | Collect national code and mobile | Form 1 |
| SHAHKAR | Automation inquiry subflow | Verify that national code and mobile match | Processing only |
| Birthdate input | BPM | Reach this state only after an explicit positive match | Form 2 |
| Registration inquiry | Automation inquiry subflow | Verify birthdate/identity; retain verified name, family name and provider reference | Processing only |
| Credit inquiries | Automation | Start SAMAT, bounced-cheque and FARAJA inquiries concurrently, then join all results | Processing only |
| Credit decision | JDM rule engine through automation | Validate complete results, evaluate red flags and calculate final credit | Processing only |
| Eligibility gate | BPM/automation conditions | Continue only if `credit > minimumCredit` AND no red flag | Processing only |
| Account | Automation inquiry/action subflows | Select sponsor bank; look up account; open only if absent | Processing only |
| Disbursement | Automation action subflow | Use the sponsor's configured instrument to transfer approved credit to the verified account | Processing only |
| Final result | BPM | Completed only after confirmed disbursement; otherwise rejection or recovery hold | Read-only result |

A negative identity match rejects the application. An unavailable provider, malformed result or timeout is a technical failure—not a negative match or a zero-risk result. After bounded transient retries, hold for operations without requesting another applicant form.

An atomic subflow means a reusable operation with defined input/output and independent execution tracking. It does **not** imply a transaction spanning several banks/providers.

## Dynamic definitions to provision after gaps are resolved

The following are definition keys stored through APIs, **not source-code filenames**:

- `bnpl-main-v1`: BPM journey; owns applicant states and transitions.
- `bnpl-identity-form-v1` and `bnpl-birthdate-form-v1`: strict dynamic form definitions hosted by BPM's existing dynamic runtime.
- `inquiry-shahkar-v1`, `inquiry-registration-v1`, `inquiry-samat-v1`, `inquiry-bounced-cheque-v1`, `inquiry-faraja-v1`: reusable inquiry flows, not coupled to BNPL application endpoints.
- `bnpl-credit-assessment-v1`: parallel inquiry composition followed by a versioned JDM decision.
- `account-lookup-v1`, `account-open-v1`, `credit-disburse-v1`, `transfer-status-v1`: reusable provider action flows.
- Sponsor/instrument configuration: a strictly validated dynamic definition on an already deployed dynamic runtime. Bind it using trusted identity/context, not applicant-submitted bank, amount or destination fields.

The main flow supplies inputs explicitly and maps named subflow outputs into its own context. Inquiry credentials belong in generic credential references, not JSON payloads. Bank account and payout APIs belong to the actual provider; checkout payment initiation is not an outbound-transfer API.

### Example normalized contracts

| Operation | Input | Output |
| --- | --- | --- |
| SHAHKAR | `nationalCode`, `mobile`, `requestKey` | `status`, `matched`, `reference` |
| Registration | `nationalCode`, `birthdate`, `requestKey` | `status`, `matched`, verified identity object, `reference` |
| SAMAT | verified identity reference, `requestKey` | `status`, debt metrics, available-credit metrics, `reference` |
| Bounced cheque | verified identity reference, `requestKey` | `status`, unresolved cheque metrics, `reference` |
| FARAJA | verified identity reference, `requestKey` | `status`, configured risk flags, `reference` |
| Account lookup/open | verified identity, trusted sponsor, stable operation key | `status`, account reference |
| Disbursement/status | stable operation key, verified account, trusted instrument, approved integer IRR amount | `status`, transfer reference, confirmed account/amount |

These contracts must be adapted to provider specifications. A status lookup returning `NOT_FOUND` is safe grounds for resubmission only if the provider contract makes that answer authoritative and creation is idempotent. A timeout does not prove that no transfer occurred.

### Demonstration rule policy

Keep policy in JDM, not Java. First validate all inquiry results. Then evaluate configured red flags, calculate a risk-adjusted credit limit and apply the sponsor cap. Output `creditLimit`, `redFlag`, `eligible`, `reasonCodes` and `policyVersion`. The final gate is strictly greater than the configured minimum: equality must reject.

Use synthetic cases for positive identity, mismatch, red flag, exactly-at-minimum credit, existing account, account opening, provider unavailability and lost successful disbursement response. No real policy thresholds or real identity fixtures are supplied here.

## Capability and gap report

| Capability | Current repository evidence | Assessment |
| --- | --- | --- |
| Dynamic forms and transitions | BPM `FlowState`, `ObjectFlowService`, dynamic entity integration | Available |
| API calls and response mappings | Automation `CALL_API`, credential references, mapping nodes | Available; actual provider contracts still required |
| Reusable subflows | VARIABLES runtime `SUBFLOW` | Available, but it runs children inline and does not provide native fan-out |
| Parallel all-results join | `PARALLEL_SUBFLOWS` persists named child execution identities and definition snapshots before work is claimable | Available as a generic VARIABLES-runtime node |
| Complex rules | `JDM_DECISION` backed by GoRules/Zen | Available; define and test the policy as data |
| Safe applicant lifecycle | `/endpoint/bpm/applications` uses per-definition `applicantAccess`, authenticated ownership, server-chosen transitions and output projection | Available; do not grant applicants operator permissions |
| Trusted sponsor and output protection | Dynamic data/context exists, but this journey needs verified principal binding and restricted projections | **Generic configuration/access gap**; do not solve with a BNPL facade |
| Durable child recovery | Parallel child plan and stable execution identities are stored before scheduling | Available; completed branches are retained during parent recovery |
| Background callback delivery | Signed callbacks exist; callback delivery and BPM state persistence must be reconciled | Verify/fix generic durable delivery and early-callback races before release |
| Safe account opening and payout | `EXTERNAL_OPERATION` persists operation state, reconciles before invoke, and applies a stable idempotency key | Available when the provider supplies an authoritative reconcile endpoint and honors idempotency |
| Finance ledger service | Not deployed on target server | Excluded; do not introduce a dependency on it |

The generic platform capabilities above are implemented. Provider credentials, an authoritative status/reconcile API, and reviewed flow definitions are still mandatory before provisioning a real financial journey.

### Generic configuration additions

`PARALLEL_SUBFLOWS` accepts named `branches`, each with `flowKey` and optional mapped `input`; `maxConcurrency` bounds active children and `resultPath` receives `{branchName: childOutput}` only after all branches complete.

`EXTERNAL_OPERATION` requires `operationKey`, `reconcile`, and `execute`. It reconciles first on every retry, sends the stable key in `Idempotency-Key` by default, and saves `PREPARED`, `IN_FLIGHT`, `SUBMITTED`, or `CONFIRMED` operation state with the execution. It waits and reconciles again until `confirmed` (or configured success paths) is true unless `awaitConfirmation` is explicitly disabled. A retry therefore never blindly re-invokes an uncertain external operation.

`applicantAccess` on a BPM flow definition requires `objectType`, `startPayloadFields`, `formStateIds`, `formNextStates`, and optional role/status-field allowlists. Applicant APIs always discard a client-selected next state and only return the allowlisted status projection.

## Curl: existing administrator capabilities

These use existing routes. Set URLs to the actual deployed gateway/services. Never use internal basic credentials in a browser/app. The identity-provider login request depends on the configured SSO realm/client; use its existing login flow to obtain `ADMIN_TOKEN` and later `USER_TOKEN`.

```bash
export GATEWAY_URL='https://your-platform.example'
export ADMIN_TOKEN='<existing SSO administrator access token>'
export TENANT_KEY='<authorized tenant>'
export SITE_KEY='<authorized site>'
export INQUIRY_URL='https://your-provider.example/shahkar'
export INQUIRY_CREDENTIAL_REF='<configured automation credential reference>'
```

### Save a reusable inquiry definition

This is an existing-runtime example, not a live provider adapter. The provider must accept the illustrated body and return the agreed normalized response, or additional mapping nodes must be configured.

```bash
jq -n --arg url "$INQUIRY_URL" --arg credential "$INQUIRY_CREDENTIAL_REF" '{
  flowKey: "inquiry-shahkar-v1", version: 1,
  name: "Reusable national-code/mobile inquiry",
  runtimeMode: "VARIABLES", environment: "default",
  lifecycleStatus: "DRAFT", active: false, entryNodeId: "call",
  nodes: [
    {id: "call", type: "CALL_API", credentialRef: $credential,
     retryPolicy: {maxAttempts: 3, backoffMs: 1000, strategy: "exponential"},
     config: {url: $url, method: "POST",
       body: {nationalCode: "{{nationalCode}}", mobile: "{{mobile}}", requestKey: "{{requestKey}}"},
       storeResponseAt: "inquiry"}},
    {id: "end", type: "END", config: {}}
  ],
  edges: [{id: "done", fromNodeId: "call", toNodeId: "end"}]
}' | curl --fail-with-body -sS "$GATEWAY_URL/endpoint/automation-flows" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" -H "X-Site-Key: $SITE_KEY" --data-binary @-
```

### Approve, activate and inspect the inquiry

```bash
for action in submit approve activate; do
  curl --fail-with-body -sS -X POST \
    "$GATEWAY_URL/endpoint/automation-flows/inquiry-shahkar-v1/versions/1/$action" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "X-Tenant-Key: $TENANT_KEY" -H "X-Site-Key: $SITE_KEY"
done

curl --fail-with-body -sS \
  "$GATEWAY_URL/endpoint/automation-flows/inquiry-shahkar-v1/active?environment=default" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant-Key: $TENANT_KEY" -H "X-Site-Key: $SITE_KEY"
```

### Existing sequential reuse syntax

This node can be embedded in an administrator-created VARIABLES flow. It receives the parent's variables and places the child's full output under `shahkar`. It does not perform concurrent fan-out.

```json
{
  "id": "verify-mobile-owner",
  "type": "SUBFLOW",
  "config": {
    "flowKey": "inquiry-shahkar-v1",
    "resultPath": "shahkar"
  }
}
```

### Inspect current BPM definitions

```bash
curl --fail-with-body -sS "$GATEWAY_URL/endpoint/bpm/flows" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant-Key: $TENANT_KEY" -H "X-Site-Key: $SITE_KEY"
```

Existing BPM provisioning routes are `POST /endpoint/bpm/flows` and `POST /endpoint/bpm/flows/{flowKey}/activate/{version}`. Use the generic `applicantAccess`, `PARALLEL_SUBFLOWS`, and `EXTERNAL_OPERATION` contracts above when authoring the main flow.

## Generic applicant BPM curls

The restricted surface is available only for definitions with enabled `applicantAccess`; it does not use or grant `bpm.manage`, `bpm.transition`, or `bpm.read`.

```bash
export USER_TOKEN='<existing applicant SSO access token>'

curl --fail-with-body -sS -X POST "$GATEWAY_URL/endpoint/bpm/applications" \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" -H "X-Site-Key: $SITE_KEY" \
  --data '{"flowKey":"<enabled-flow-key>","payload":{}}'

curl --fail-with-body -sS "$GATEWAY_URL/endpoint/bpm/applications/<object-id>/active-form" \
  -H "Authorization: Bearer $USER_TOKEN" -H "X-Tenant-Key: $TENANT_KEY" -H "X-Site-Key: $SITE_KEY"

curl --fail-with-body -sS -X POST "$GATEWAY_URL/endpoint/bpm/applications/<object-id>/active-form/submissions" \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" -H "X-Site-Key: $SITE_KEY" \
  --data '<exact fields required by the active form>'

curl --fail-with-body -sS "$GATEWAY_URL/endpoint/bpm/applications/<object-id>" \
  -H "Authorization: Bearer $USER_TOKEN" -H "X-Tenant-Key: $TENANT_KEY" -H "X-Site-Key: $SITE_KEY"
```

The client never sends a next state, automation result, policy key, payout destination, or status projection field.

## Acceptance before calling the journey complete

- Provision everything through generic APIs using payloads embedded in this file; no BNPL source files or finance-service dependency.
- Verify ownership, tenant/site isolation, sponsor binding and field allowlists with two applicants and two sponsors.
- Prove actual overlap of all three credit inquiries and durable all-results join after worker restart.
- Verify negative identity, technical failure, missing inquiry output, red flag and strict minimum-credit boundary separately.
- Verify only two applicant forms; no hidden operator form or manual transition requirement in the happy path.
- Verify account reuse and stable creation/disbursement keys; reconcile a lost successful transfer response without a second transfer.
- Verify recovery preserves completed branches and approved rule version; final completion requires confirmed transfer.
- Execute and record results for every admin/user curl. Until then, this document remains a gap report and design, not an end-to-end completion report.

## Repository references

- [BPM states](../bpm-service/src/main/java/com/cyancoder/bpm/domain/FlowState.java)
- [BPM object/form lifecycle and access checks](../bpm-service/src/main/java/com/cyancoder/bpm/service/ObjectFlowService.java)
- [BPM endpoint permissions](../bpm-service/src/main/java/com/cyancoder/bpm/api/EndpointManagedObjectFlowController.java)
- [BPM automation actions](../bpm-service/src/main/java/com/cyancoder/bpm/service/FlowActionExecutor.java)
- [Automation graph and subflow runtime](../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/GraphAutomationRuntime.java)
- [Automation lifecycle APIs](../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/controller/EndpointAutomationFlowController.java)
- [Automation execution and callback delivery](../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/AutomationExecutionService.java)
- [JDM evaluation](../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/GoRulesDecisionService.java)
