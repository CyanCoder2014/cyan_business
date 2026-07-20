package com.cyancoder.automationorchestrator.domain;

public enum AutomationNodeType {
    WEBHOOK_TRIGGER,
    WAIT,
    WAIT_FOR_CALLBACK,
    CALL_API,
    PAGINATED_CALL_API,
    IF,
    SWITCH,
    MERGE,
    FOR_EACH,
    SUBFLOW,
    JDM_DECISION,
    MAP_FIELDS,
    JSON_TRANSFORM,
    FILE_METADATA,
    DEDUP_BY_KEY,
    CODE,
    N8N_WORKFLOW,
    END
}
