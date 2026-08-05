# BPM Flow Builder and Work Queue Specification

## 1. Product separation

### BPM Designer
Defines:
- states
- transitions
- active forms
- processors
- candidate users/roles/groups
- access rules
- on-enter actions
- automation blocks

### Work Queue
Operates:
- managed objects
- assignments
- active-form submission
- transition execution
- comments
- attachments
- history

Do not combine definition editing and runtime work into one crowded page.

## 2. Routes

- `/bpm` — flow catalog
- `/bpm/new` — create flow
- `/bpm/[flowKey]` — designer
- `/work` — assigned/visible managed objects
- `/work/[objectId]` — active work item

## 3. BPM designer layout

Desktop:
- left state/action palette
- center XYFlow canvas
- right selected state/transition inspector
- bottom validation/history drawer

Mobile:
- canvas viewer/editor
- add-state floating action
- selected state/transition bottom sheet
- flow settings separate screen
- searchable state list as an alternative to canvas

## 4. State node

Visual summary:
- display name
- terminal/start indicator
- active form/entity
- processor
- assignment type
- number of actions
- validation/error badge

Editable state fields:
- `id`
- `displayName`
- `terminal`
- `formKey`
- `processorKey`
- `reviewCommentRequired`
- `candidateGroups`
- `accessRule`
- `entityService`
- `entityKey`
- `rendererService`
- `rendererKey`
- `submitMode`
- `submitUrl`
- `onEnterActions`

## 5. Form/entity connection

State form selector:
1. choose submit mode
2. choose service
3. choose entity/form definition
4. choose renderer definition
5. preview generated form
6. map state payload to form defaults
7. validate required fields

The active form must render from the backend `rendererDefinition`. Do not hardcode form fields.

## 6. Processors

Processor selector loads available processor definitions where supported.

Show:
- processor key
- purpose
- input/output expectations
- failure behavior

Explain that processor failure blocks persistence.

## 7. Actions

Supported actions include:
- `ADD_AUDIT_ENTRY`
- `SET_ASSIGNEE`
- `SET_ACCESS_RULE`
- `UPDATE_OBJECT_FIELDS`
- `COPY_FIELDS`
- `REMOVE_FIELDS`
- `CALL_API`
- `CALL_OPERATOR`
- `NOTIFY_OWNER`
- `RUN_AUTOMATION_BLOCK`

Action editor must be metadata-driven. Do not add generic placeholder parameters.

`RUN_AUTOMATION_BLOCK` selector:
- select automation flow/version or inline pipeline
- choose sync/async
- map BPM payload to automation input
- map output back to managed-object payload
- define error behavior

## 8. Transitions

Transition edge editor:
- id
- display label
- source
- destination
- allowed roles/groups/users
- comment requirement
- conditions
- priority/order
- optional actions

Condition builder:
- metadata-driven operators
- nested AND/OR groups
- field picker from managed object/form payload
- value/expression editor
- human-readable preview
- raw JSON advanced view

## 9. Validation

Before save/activate:
- unique state IDs
- one valid start state
- reachable non-terminal states
- transition source/target existence
- terminal state rules
- required form/service fields
- required action parameters
- processor references
- automation references
- assignment/access conflicts

Show errors on nodes, edges, inspector fields, and a summary list.

## 10. Work queue `/work`

Views:
- Assigned to me
- My groups/roles
- Visible to me
- Unassigned
- Completed, when permitted

Filters:
- flow
- state
- assignee type
- date
- priority
- tenant/site
- search

Rows/cards:
- object title/reference
- flow/state
- assignee
- SLA/age
- last activity
- status

## 11. Work item `/work/[objectId]`

Sections:
- header and assignment
- active form
- transition actions
- comments
- attachments
- payload details
- audit log
- transition history

Actions:
- submit active form
- execute allowed transition
- add comment
- add media attachment
- reassign when permitted
- lock/unlock where API supports

After transition, refresh transition options and active form.

## 12. API integration

Definition:
- flow list/read/save
- action metadata
- transition condition metadata
- activate/version lifecycle when available

Runtime:
- assigned-to-me
- visible-to-me
- managed object detail
- transition options
- active form load
- active form submit
- transition execute
- comments
- attachments

## 13. Acceptance scenarios

1. Create a three-state approval flow.
2. Attach a dynamic entity form to the first state.
3. Set a processor.
4. Add role assignment.
5. Add `NOTIFY_OWNER`.
6. Add `RUN_AUTOMATION_BLOCK`.
7. Configure approve/reject transitions.
8. Validate and activate.
9. Start a managed object.
10. Submit active form.
11. Transition from mobile.
12. Add scoped comment and media attachment.
13. Verify transition history and audit log.
