package com.cyancoder.bpm.service;

import com.cyancoder.bpm.domain.ConditionLogicalOperator;
import com.cyancoder.bpm.domain.ConditionOperator;
import com.cyancoder.bpm.domain.FlowCondition;
import com.cyancoder.bpm.domain.FlowTransition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FlowTransitionConditionEvaluatorTest {

    private final FlowTransitionConditionEvaluator evaluator = new FlowTransitionConditionEvaluator();

    @Test
    void evaluatesExtendedConditionOperatorsAdvertisedByMetadata() {
        FlowTransition transition = new FlowTransition(
                "route",
                "review",
                "approved",
                "Route",
                Set.of(),
                Set.of(),
                null,
                ConditionLogicalOperator.AND,
                List.of(
                        new FlowCondition("payload.score", ConditionOperator.BETWEEN, List.of(40, 90)),
                        new FlowCondition("payload.tags", ConditionOperator.CONTAINS_ANY, List.of("vip", "screened")),
                        new FlowCondition("payload.dueDate", ConditionOperator.ON_OR_BEFORE, "2026-07-10"),
                        new FlowCondition("payload.nationalId", ConditionOperator.MATCHES, "^[0-9]{8}$")
                )
        );

        assertTrue(evaluator.evaluate(transition, Map.of(
                "score", 72,
                "tags", List.of("screened", "manual-review"),
                "dueDate", "2026-07-02",
                "nationalId", "99887766"
        ), Map.of()));
    }
}
