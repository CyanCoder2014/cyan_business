package com.cyancoder.bpm.domain;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Set;

public record FlowTransition(
        @Field("id")
        String id,
        String fromState,
        String toState,
        String label,
        Set<String> allowedGroups,
        Set<String> allowedRoles,
        String conditionExpression,
        ConditionLogicalOperator conditionOperator,
        List<FlowCondition> conditions
) {
}

