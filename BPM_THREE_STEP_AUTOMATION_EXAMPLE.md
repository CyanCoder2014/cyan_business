# Three-Step BPM Form and Automation Example

This example implements three human form submissions followed by one automatic enrichment and persistence state.

## Runtime order

For every human form state, BPM now executes:

1. assignment and `accessRule.canEdit` checks
2. transition permission preflight when `nextState` is requested
3. the state-owned `processorKey` through `processor-service`
4. strict dynamic-entity validation
5. dynamic record persistence
6. BPM submission history persistence
7. state transition

The client cannot choose a different processor. BPM always resolves `processorKey` from the active flow state.

## States

Use three dynamic definitions such as `application-profile-form`, `application-documents-form`, and `application-confirmation-form`.
The document definition should model `documents` as a list of objects containing at least `assetKey`, `fileName`, `contentType`, and `sizeBytes`. Upload or prepare each asset in `media-service` first, then submit those asset references in the BPM form.

The flow has three human states, one automatic state, and one terminal state:

```json
{
  "flowKey": "three-step-application",
  "version": 1,
  "name": "Three Step Application",
  "startState": "profile",
  "active": true,
  "states": [
    {
      "id": "profile",
      "displayName": "Profile",
      "terminal": false,
      "formKey": "application-profile-form",
      "processorKey": "application-profile-processor",
      "entityService": "bpm-service",
      "entityKey": "application-profile-form",
      "submitMode": "DYNAMIC",
      "accessRule": {"canRead": ["ROLE_USER"], "canEdit": ["ROLE_USER"], "canApprove": []},
      "candidateGroups": [],
      "onEnterActions": []
    },
    {
      "id": "documents",
      "displayName": "Documents",
      "terminal": false,
      "formKey": "application-documents-form",
      "processorKey": "application-documents-processor",
      "entityService": "bpm-service",
      "entityKey": "application-documents-form",
      "submitMode": "DYNAMIC",
      "accessRule": {"canRead": ["ROLE_USER", "ROLE_DOCUMENT_REVIEW"], "canEdit": ["ROLE_USER"], "canApprove": []},
      "candidateGroups": [],
      "onEnterActions": []
    },
    {
      "id": "confirmation",
      "displayName": "Confirmation",
      "terminal": false,
      "formKey": "application-confirmation-form",
      "processorKey": "application-confirmation-processor",
      "entityService": "bpm-service",
      "entityKey": "application-confirmation-form",
      "submitMode": "DYNAMIC",
      "accessRule": {"canRead": ["ROLE_USER"], "canEdit": ["ROLE_USER"], "canApprove": []},
      "candidateGroups": [],
      "onEnterActions": []
    },
    {
      "id": "automation",
      "displayName": "Verify and Save",
      "terminal": false,
      "formKey": null,
      "processorKey": null,
      "entityKey": null,
      "waitForAutomation": false,
      "candidateGroups": [],
      "accessRule": {"canRead": ["ROLE_USER", "ROLE_APPLICATION_ADMIN"], "canEdit": [], "canApprove": []},
      "onEnterActions": [
        {
          "type": "RUN_AUTOMATION_BLOCK",
          "params": {
            "blockKey": "verify-and-save",
            "executionMode": "SYNC",
            "failurePolicy": "FAIL_FAST",
            "body": {
              "applicationId": "{{objectId}}",
              "profile": "{{payload.profile}}",
              "documents": "{{payload.documents}}",
              "confirmation": "{{payload.confirmation}}"
            },
            "inlineFlow": {
              "type": "PIPELINE",
              "steps": [
                {
                  "type": "FOR_EACH",
                  "sourcePath": "documents.documents",
                  "steps": [
                    {
                      "type": "CALL_API",
                      "serviceKey": "media-service",
                      "path": "/internal/media/assets/{{item.assetKey}}",
                      "method": "GET",
                      "storeResponseAt": "asset"
                    }
                  ],
                  "resultPath": "asset",
                  "targetPath": "verifiedAssets"
                },
                {
                  "type": "SCRIPT",
                  "expression": "#variables['verifiedAssets'].size()",
                  "targetPath": "summary.documentCount"
                },
                {
                  "type": "MAP_FIELDS",
                  "mappings": {
                    "result.applicationId": "applicationId",
                    "result.profile": "profile",
                    "result.confirmation": "confirmation",
                    "result.assets": "verifiedAssets",
                    "result.documentCount": "summary.documentCount"
                  }
                },
                {
                  "type": "CALL_API",
                  "serviceKey": "report-service",
                  "path": "/internal/entities/records/application-result",
                  "method": "POST",
                  "body": {
                    "recordKey": "{{applicationId}}",
                    "data": "{{result}}"
                  },
                  "storeResponseAt": "savedRecord"
                }
              ]
            },
            "storeVariablesAt": "payload.automation.verifyAndSave.output",
            "storeStatusAt": "payload.automation.verifyAndSave.status"
          }
        }
      ]
    },
    {
      "id": "completed",
      "displayName": "Completed",
      "terminal": true,
      "formKey": null,
      "processorKey": null,
      "candidateGroups": [],
      "onEnterActions": [],
      "accessRule": {"canRead": ["ROLE_USER", "ROLE_APPLICATION_ADMIN"], "canEdit": [], "canApprove": []}
    }
  ],
  "transitions": [
    {"id": "profile-next", "fromState": "profile", "toState": "documents", "label": "Continue", "allowedGroups": [], "allowedRoles": ["ROLE_USER"], "conditions": []},
    {"id": "documents-next", "fromState": "documents", "toState": "confirmation", "label": "Continue", "allowedGroups": [], "allowedRoles": ["ROLE_USER"], "conditions": []},
    {"id": "confirmation-submit", "fromState": "confirmation", "toState": "automation", "label": "Submit", "allowedGroups": [], "allowedRoles": ["ROLE_USER"], "conditions": []},
    {"id": "automation-complete", "fromState": "automation", "toState": "completed", "label": "Complete", "allowedGroups": [], "allowedRoles": [], "conditions": []}
  ]
}
```

Create the flow through `POST /endpoint/bpm/flows`, then activate its version. Before running it, create the three dynamic definitions, the three processor definitions, and the target `application-result` definition in `report-service`.

## Collaboration APIs

- `POST|GET /endpoint/bpm/managed-objects/{objectId}/comments`
- `POST|GET /endpoint/bpm/managed-objects/{objectId}/attachments`

Comments can target `visibleToUserIds`, `visibleToRoles`, and `visibleToGroups`. When no target is supplied, a comment defaults to the current assignee. Attachments use `assetKey` references and are public to actors who can access the managed object unless visibility targets are supplied.

`SET_ASSIGNEE` accepts either explicit assignment:

```json
{"type":"SET_ASSIGNEE","params":{"assignee":"ROLE_DOCUMENT_REVIEW","assigneeType":"ROLE"}}
```

or aliases such as `roleAssignee` and `groupAssignee`.
