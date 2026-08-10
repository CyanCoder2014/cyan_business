# Authentication, Plans, Tenancy, Roles, and Capabilities

## 1. Authentication journeys

### Sign in
Methods:
- username/email + password
- Google
- GitHub
- LinkedIn
- optional OTP/MFA challenge
- recovery and magic link when backend support exists

### Registration
1. Choose identity method.
2. Accept legal terms.
3. Verify email/identity.
4. Create personal account.
5. Continue to onboarding.

Do not ask for a workspace name inside the basic identity form unless registration truly creates the tenant.

### OAuth contract needed

Each provider requires:
- start endpoint
- callback endpoint
- state/PKCE behavior
- provider error mapping
- account-link behavior
- access/refresh/session response contract
- allowed redirect URLs

UI controls must remain disabled with a truthful “Not configured” explanation until the provider contract is available.

## 2. Post-login onboarding state machine

```text
AUTHENTICATED
  -> NO_TENANT
  -> TENANT_SELECTED
  -> NO_PLAN
  -> LIMITED_ACCESS
  -> ACTIVE_PLAN
  -> FULL_OR_PLAN_LIMITED_ACCESS
```

Possible onboarding actions:
- create a tenant/workspace
- accept an invitation
- select an existing tenant
- select a plan
- skip plan selection and enter limited discovery mode

## 3. No-plan / limited access mode

A user without a plan can:
- view the dashboard introduction
- browse capability descriptions
- view plan comparison
- edit profile and security
- accept tenant invitations
- open read-only product tours

They cannot:
- generate or provision AI projects
- create definitions
- execute automations
- activate BPM flows
- add tenant users
- publish sites/bots
- send notifications

Every locked action shows:
- why it is locked
- required plan feature
- link to plan selection
- no fake success or local-only creation

## 4. Tenant roles

Recommended tenant-level roles:

| Role | Purpose |
|---|---|
| `TENANT_OWNER` | Billing, tenant settings, all tenant access |
| `TENANT_ADMIN` | Users, roles, projects, configuration; no ownership transfer |
| `CLIENT_ADMIN` | Manage users under an assigned client/tenant boundary |
| `BUILDER` | Definitions, automations, BPM, sites, bots |
| `OPERATOR` | Data, work queue, notifications, reports |
| `VIEWER` | Read-only |
| `BILLING_ADMIN` | Plan, invoices, payment methods |
| `SECURITY_ADMIN` | Roles, permissions, sessions, audit |

Do not rely only on role names. Resolve effective permissions and capabilities.

## 5. Permission model

Suggested permission naming:

```text
project.read
project.create
project.edit
project.provision
project.publish

definition.read
definition.create
definition.edit
definition.publish

record.read
record.create
record.edit
record.delete
record.export

automation.read
automation.edit
automation.execute
automation.activate
automation.manage_credentials

bpm.read
bpm.edit
bpm.activate
bpm.start_object
bpm.transition
bpm.assign
bpm.comment
bpm.attach

tenant.user.read
tenant.user.invite
tenant.user.edit
tenant.role.manage

billing.read
billing.manage

site.edit
site.publish
bot.manage
notification.send
report.run
domain.manage
```

## 6. Capability model

Capabilities represent deployed/activated product services:

```ts
type EffectiveCapability = {
  key: string;
  enabled: boolean;
  source: "PLAN" | "TENANT_OVERRIDE" | "PLATFORM";
  status: "AVAILABLE" | "DEGRADED" | "UNAVAILABLE";
  limits?: Record<string, number | string | boolean>;
};
```

Examples:
- `ai-orchestrator`
- `dynamic-entities`
- `automation`
- `bpm`
- `processor`
- `notification`
- `report`
- `media`
- `search`
- `site-builder`
- `bot-adapter`
- `commerce`
- `payment`

## 7. Access resolution

Create one server-backed bootstrap call:

```text
GET /api/panel/bootstrap
```

Recommended response:

```json
{
  "user": {},
  "tenants": [],
  "activeTenant": {},
  "sites": [],
  "activeSite": {},
  "plan": {},
  "capabilities": [],
  "permissions": [],
  "featureFlags": {},
  "locale": "en",
  "theme": "system"
}
```

Until such aggregation exists, compose it in the Next.js BFF from the current IAM, tenant/site, and plan services.

## 8. Dynamic navigation and route guards

- Navigation is filtered by capabilities and read permissions.
- Route loaders validate access again.
- Mutation controls validate action permissions.
- Backend authorization remains authoritative.
- A `403` renders a permission state, not a generic error.
- Capability unavailable/degraded states are separate from permission denial.

## 9. Tenant user management

Pages:
- user list
- invitations
- user detail
- role assignments
- group/client membership
- session/device revocation when available

Client administrators can create users only inside their authorized tenant/client boundary and cannot grant permissions they do not possess.

## 10. Plan and billing

Plan page:
- current plan and renewal
- usage against limits
- plan comparison
- invoice history
- payment method
- cancellation/change confirmation
- capability activation timeline

UI values must come from billing/plan APIs. Do not infer active plan from a local flag.
