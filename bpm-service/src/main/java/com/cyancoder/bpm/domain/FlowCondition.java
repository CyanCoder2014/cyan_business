package com.cyancoder.bpm.domain;

public record FlowCondition(String field, ConditionOperator operator, Object value) {
}

