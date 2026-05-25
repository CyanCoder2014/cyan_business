# Hybrid BPM + Automation Scenario

This repo now supports a runnable hybrid BPM + automation flow:

- BPM flow key: `hybrid-screening-intake`
- automation flow key: `hybrid-screening-automation`
- intake form entity: `screening-intake-form`
- review form entity: `screening-review-form`

The flow is seeded by `bpm-service` when `bpm.seed.enabled=true`.
That is already enabled in `bpm-service/src/main/resources/application-localdemo.properties`.

## Flow

1. user creates a BPM managed object for `hybrid-screening-intake`
2. user submits `screening-intake-form`
3. BPM moves to `automated-screening`
4. `START_AUTOMATION_FLOW` calls `automation-orchestrator-service`
5. automation completes and calls BPM callback
6. BPM stores:
   - `payload.automation.screening.executionId`
   - `payload.automation.screening.status`
   - `payload.automation.screening.startResponse`
   - `payload.automation.screening.snapshot`
   - `payload.currentFormValues.riskScore`
   - `payload.currentFormValues.screeningRoute`
   - `payload.currentFormValues.externalRef`
7. BPM auto-transitions to:
   - `fast-track-approved`
   - `manual-review`
   - `screening-rejected`
8. if routed to `manual-review`, admin submits `screening-review-form` and then transitions to approve or reject

## Base Headers

Use these on BPM endpoint calls:

```http
Authorization: Bearer <jwt>
X-Tenant-Key: tenant-demo
X-Site-Key: site-shop-a
Content-Type: application/json
```

## 1. Create Managed Object

```bash
curl -X POST http://localhost:8001/endpoint/bpm/managed-objects \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a" \
  -H "Content-Type: application/json" \
  -d '{
    "flowKey": "hybrid-screening-intake",
    "objectType": "SCREENING_REQUEST",
    "objectRef": {
      "service": "bpm-service",
      "entityKey": "screening-request",
      "recordKey": "screening-request-001"
    },
    "payload": {}
  }'
```

Expected result:

- state: `screening-intake`

## 2. Inspect Active Form

```bash
curl http://localhost:8001/endpoint/bpm/managed-objects/<objectId>/active-form \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a"
```

Expected result:

- `formKey = screening-intake-form`
- `renderer.serviceKey = bpm-service`
- `renderer.entityKey = screening-intake-form`

## 3. Submit Intake Form

```bash
curl -X POST http://localhost:8001/endpoint/bpm/managed-objects/<objectId>/active-form/submissions \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a" \
  -H "Content-Type: application/json" \
  -d '{
    "formData": {
      "fullName": "Jane Roe",
      "nationalId": "99887766",
      "requestedAmount": 15000,
      "notes": "Priority applicant"
    },
    "nextState": "automated-screening",
    "context": {}
  }'
```

Expected result:

- BPM enters `automated-screening`
- `payload.screening-intake.*` contains the submitted values
- `payload.asyncActions.screening.status = PENDING`
- `payload.asyncActions.screening.correlationKey` exists
- `payload.automation.screening.executionId` is populated from the automation start response
- `payload.automation.screening.status = COMPLETED` after callback

## 4. Inspect Object After Automation Callback

```bash
curl http://localhost:8001/endpoint/bpm/managed-objects/<objectId> \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a"
```

Expected payload sections:

```json
{
  "currentFormValues": {
    "riskScore": 23,
    "screeningRoute": "FAST_TRACK",
    "externalRef": "screen-jane-roe-xxxxxxx"
  },
  "automation": {
    "screening": {
      "executionId": "exec-...",
      "status": "COMPLETED",
      "startResponse": {
        "executionId": "exec-...",
        "status": "COMPLETED"
      },
      "snapshot": {
        "executionId": "exec-...",
        "status": "COMPLETED",
        "output": {
          "riskScore": 23,
          "screeningRoute": "FAST_TRACK",
          "externalRef": "screen-jane-roe-xxxxxxx"
        }
      }
    }
  }
}
```

Expected state:

- `fast-track-approved` if route is `FAST_TRACK`
- `manual-review` if route is `MANUAL_REVIEW`
- `screening-rejected` if route is `REJECT`

## 5. Manual Review Path

If the object is routed to `manual-review`, inspect the active form:

```bash
curl http://localhost:8001/endpoint/bpm/managed-objects/<objectId>/active-form \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a"
```

Expected result:

- `formKey = screening-review-form`

Submit the review form:

```bash
curl -X POST http://localhost:8001/endpoint/bpm/managed-objects/<objectId>/active-form/submissions \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a" \
  -H "Content-Type: application/json" \
  -d '{
    "formData": {
      "reviewDecision": "APPROVE",
      "reviewComment": "Manual verification passed.",
      "reviewerReference": "analyst-01"
    },
    "context": {
      "reviewComment": "Manual verification passed."
    }
  }'
```

Then inspect available transitions:

```bash
curl http://localhost:8001/endpoint/bpm/managed-objects/<objectId>/transitions \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a"
```

Expected options:

- `fast-track-approved`
- `screening-rejected`

Approve:

```bash
curl -X POST http://localhost:8001/endpoint/bpm/managed-objects/<objectId>/transitions \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a" \
  -H "Content-Type: application/json" \
  -d '{
    "nextState": "fast-track-approved",
    "context": {
      "decision": "APPROVE",
      "reviewComment": "Manual verification passed."
    }
  }'
```

Reject:

```bash
curl -X POST http://localhost:8001/endpoint/bpm/managed-objects/<objectId>/transitions \
  -H "Authorization: Bearer <jwt>" \
  -H "X-Tenant-Key: tenant-demo" \
  -H "X-Site-Key: site-shop-a" \
  -H "Content-Type: application/json" \
  -d '{
    "nextState": "screening-rejected",
    "context": {
      "decision": "REJECT",
      "reviewComment": "Manual review rejected the request."
    }
  }'
```

## Internal Automation Endpoint

The BPM automation action starts the execution here:

```http
POST /internal/automation-orchestrator/executions/start
```

Service:

- `automation-orchestrator-service`

The service completes a sample screening decision immediately and calls back:

```http
POST /public/bpm/async-actions/callbacks/{correlationKey}
```

## Notes

- callback signing uses the shared dynamic flow callback headers and secret
- for local demo, BPM callback secret is `localdemo-secret`
- the seeded sample is deterministic enough for testing, but the automation scorer is still a demo implementation
- if you want a production-grade version, replace the scorer in `automation-orchestrator-service` with your real rules engine, model, or external vendor integration
