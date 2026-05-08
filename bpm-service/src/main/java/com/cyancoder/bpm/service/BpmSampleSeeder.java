package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.domain.ActionType;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.FlowAccessRule;
import com.cyancoder.bpm.domain.FlowActionConfig;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.FlowTransition;
import com.cyancoder.bpm.domain.SubmitMode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class BpmSampleSeeder {
    private final FlowDefinitionService flowDefinitionService;
    private final boolean enabled;

    public BpmSampleSeeder(FlowDefinitionService flowDefinitionService,
                           @Value("${bpm.seed.enabled:false}") boolean enabled) {
        this.flowDefinitionService = flowDefinitionService;
        this.enabled = enabled;
    }

    @PostConstruct
    public void seed() {
        if (!enabled) {
            return;
        }
        BpmScope scope = new BpmScope("tenant-demo", "site-shop-a");
        try {
            flowDefinitionService.getLatestByFlowKey(scope, "commerce-order-review");
            return;
        } catch (Exception ignored) {
        }

        DynamicFlowDefinition definition = new DynamicFlowDefinition();
        definition.setFlowKey("commerce-order-review");
        definition.setName("Commerce Order Review");
        definition.setDescription("Review and approve structured shop orders before fulfillment.");
        definition.setStartState("draft-order");
        definition.setActive(true);
        definition.setStates(List.of(
                new FlowState(
                        "draft-order",
                        "Draft Order",
                        false,
                        "shop-order",
                        "order-review",
                        false,
                        Set.of("ROLE_USER"),
                        List.of(new FlowActionConfig(ActionType.ADD_AUDIT_ENTRY, Map.of("message", "entered draft state"))),
                        new FlowAccessRule(Set.of("ROLE_USER", "ROLE_ADMIN"), Set.of("ROLE_USER"), Set.of()),
                        "commerce-service",
                        "shop-order",
                        "commerce-service",
                        "shop-order",
                        SubmitMode.DYNAMIC,
                        null
                ),
                new FlowState(
                        "approved-order",
                        "Approved Order",
                        false,
                        null,
                        null,
                        false,
                        Set.of("ROLE_ADMIN"),
                        List.of(
                                new FlowActionConfig(ActionType.ADD_AUDIT_ENTRY, Map.of("message", "order approved")),
                                new FlowActionConfig(ActionType.NOTIFY_OWNER, Map.of(
                                        "serviceKey", "notification-service",
                                        "path", "/internal/notifications/send",
                                        "method", "POST"
                                ))
                        ),
                        new FlowAccessRule(Set.of("ROLE_USER", "ROLE_ADMIN"), Set.of("ROLE_ADMIN"), Set.of("ROLE_ADMIN")),
                        null,
                        null,
                        null,
                        null,
                        SubmitMode.DYNAMIC,
                        null
                ),
                new FlowState(
                        "fulfilled-order",
                        "Fulfilled Order",
                        true,
                        null,
                        null,
                        false,
                        Set.of("ROLE_ADMIN"),
                        List.of(new FlowActionConfig(ActionType.ADD_AUDIT_ENTRY, Map.of("message", "order fulfilled"))),
                        new FlowAccessRule(Set.of("ROLE_USER", "ROLE_ADMIN"), Set.of(), Set.of()),
                        null,
                        null,
                        null,
                        null,
                        SubmitMode.DYNAMIC,
                        null
                )
        ));
        definition.setTransitions(List.of(
                new FlowTransition("approve", "draft-order", "approved-order", "Approve", Set.of(), Set.of("ROLE_ADMIN"), null, null, List.of()),
                new FlowTransition("fulfill", "approved-order", "fulfilled-order", "Fulfill", Set.of(), Set.of("ROLE_ADMIN"), null, null, List.of())
        ));
        flowDefinitionService.save(scope, definition);
    }
}
