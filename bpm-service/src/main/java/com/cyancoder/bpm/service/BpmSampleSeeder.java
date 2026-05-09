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
        seedCommerceReview(scope);
        seedHybridScreening(scope);
    }

    private void seedCommerceReview(BpmScope scope) {
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
                        null,
                        false
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
                        null,
                        false
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
                        null,
                        false
                )
        ));
        definition.setTransitions(List.of(
                new FlowTransition("approve", "draft-order", "approved-order", "Approve", Set.of(), Set.of("ROLE_ADMIN"), null, null, List.of()),
                new FlowTransition("fulfill", "approved-order", "fulfilled-order", "Fulfill", Set.of(), Set.of("ROLE_ADMIN"), null, null, List.of())
        ));
        flowDefinitionService.save(scope, definition);
    }

    private void seedHybridScreening(BpmScope scope) {
        try {
            flowDefinitionService.getLatestByFlowKey(scope, "hybrid-screening-intake");
            return;
        } catch (Exception ignored) {
        }

        DynamicFlowDefinition definition = new DynamicFlowDefinition();
        definition.setFlowKey("hybrid-screening-intake");
        definition.setName("Hybrid Screening Intake");
        definition.setDescription("User intake followed by automation-assisted screening and conditional routing.");
        definition.setStartState("screening-intake");
        definition.setActive(true);
        definition.setStates(List.of(
                new FlowState(
                        "screening-intake",
                        "Screening Intake",
                        false,
                        "screening-intake-form",
                        "screening-intake-form",
                        false,
                        Set.of("ROLE_USER"),
                        List.of(new FlowActionConfig(ActionType.ADD_AUDIT_ENTRY, Map.of("message", "screening intake submitted"))),
                        new FlowAccessRule(Set.of("ROLE_USER", "ROLE_ADMIN"), Set.of("ROLE_USER"), Set.of()),
                        "bpm-service",
                        "screening-intake-form",
                        "bpm-service",
                        "screening-intake-form",
                        SubmitMode.DYNAMIC,
                        null,
                        false
                ),
                new FlowState(
                        "automated-screening",
                        "Automated Screening",
                        false,
                        null,
                        null,
                        false,
                        Set.of("ROLE_ADMIN"),
                        List.of(
                                new FlowActionConfig(ActionType.ADD_AUDIT_ENTRY, Map.of("message", "entered automated screening")),
                                new FlowActionConfig(ActionType.RUN_AUTOMATION_BLOCK, screeningAutomationParams())
                        ),
                        new FlowAccessRule(Set.of("ROLE_USER", "ROLE_ADMIN"), Set.of("ROLE_ADMIN"), Set.of("ROLE_ADMIN")),
                        null,
                        null,
                        null,
                        null,
                        SubmitMode.DYNAMIC,
                        null,
                        true
                ),
                new FlowState(
                        "manual-review",
                        "Manual Review",
                        false,
                        "screening-review-form",
                        "screening-review-form",
                        true,
                        Set.of("ROLE_ADMIN"),
                        List.of(new FlowActionConfig(ActionType.ADD_AUDIT_ENTRY, Map.of("message", "sent to manual review"))),
                        new FlowAccessRule(Set.of("ROLE_ADMIN"), Set.of("ROLE_ADMIN"), Set.of("ROLE_ADMIN")),
                        "bpm-service",
                        "screening-review-form",
                        "bpm-service",
                        "screening-review-form",
                        SubmitMode.DYNAMIC,
                        null,
                        false
                ),
                new FlowState(
                        "fast-track-approved",
                        "Fast Track Approved",
                        true,
                        null,
                        null,
                        false,
                        Set.of("ROLE_ADMIN"),
                        List.of(new FlowActionConfig(ActionType.ADD_AUDIT_ENTRY, Map.of("message", "screening fast tracked"))),
                        new FlowAccessRule(Set.of("ROLE_USER", "ROLE_ADMIN"), Set.of(), Set.of()),
                        null,
                        null,
                        null,
                        null,
                        SubmitMode.DYNAMIC,
                        null,
                        false
                ),
                new FlowState(
                        "screening-rejected",
                        "Screening Rejected",
                        true,
                        null,
                        null,
                        false,
                        Set.of("ROLE_ADMIN"),
                        List.of(new FlowActionConfig(ActionType.ADD_AUDIT_ENTRY, Map.of("message", "screening rejected"))),
                        new FlowAccessRule(Set.of("ROLE_USER", "ROLE_ADMIN"), Set.of(), Set.of()),
                        null,
                        null,
                        null,
                        null,
                        SubmitMode.DYNAMIC,
                        null,
                        false
                )
        ));
        definition.setTransitions(List.of(
                new FlowTransition("submit-intake", "screening-intake", "automated-screening", "Submit Intake", Set.of(), Set.of("ROLE_USER"), null, null, List.of()),
                new FlowTransition("route-fast-track", "automated-screening", "fast-track-approved", "Fast Track", Set.of(), Set.of(), "payload.currentFormValues.screeningRoute == \"FAST_TRACK\"", null, List.of()),
                new FlowTransition("route-manual", "automated-screening", "manual-review", "Manual Review", Set.of(), Set.of(), "payload.currentFormValues.screeningRoute == \"MANUAL_REVIEW\"", null, List.of()),
                new FlowTransition("route-reject", "automated-screening", "screening-rejected", "Reject", Set.of(), Set.of(), "payload.currentFormValues.screeningRoute == \"REJECT\"", null, List.of()),
                new FlowTransition("manual-approve", "manual-review", "fast-track-approved", "Approve", Set.of(), Set.of("ROLE_ADMIN"), null, null, List.of()),
                new FlowTransition("manual-reject", "manual-review", "screening-rejected", "Reject", Set.of(), Set.of("ROLE_ADMIN"), null, null, List.of())
        ));
        flowDefinitionService.save(scope, definition);
    }

    private Map<String, Object> screeningAutomationParams() {
        Map<String, Object> params = new java.util.LinkedHashMap<>();
        params.put("blockKey", "screening");
        params.put("automationFlowKey", "hybrid-screening-automation");
        params.put("executionMode", "ASYNC");
        params.put("failurePolicy", "RETRY");
        params.put("maxRetries", 1);
        params.put("timeoutSeconds", 300);
        params.put("body", Map.of(
                "fullName", "{{payload.screening-intake.fullName}}",
                "nationalId", "{{payload.screening-intake.nationalId}}",
                "requestedAmount", "{{payload.screening-intake.requestedAmount}}"
        ));
        params.put("responseMappings", Map.of(
                "payload.automation.screening.executionId", "executionId",
                "payload.automation.screening.status", "status"
        ));
        params.put("storeFullResponseAt", "payload.automation.screening.startResponse");
        params.put("callbackResponseMappings", Map.of(
                "payload.automation.screening.executionId", "executionId",
                "payload.automation.screening.status", "status",
                "payload.currentFormValues.riskScore", "riskScore",
                "payload.currentFormValues.screeningRoute", "screeningRoute",
                "payload.currentFormValues.externalRef", "externalRef"
        ));
        params.put("callbackStoreFullResponseAt", "payload.automation.screening.snapshot");
        return params;
    }
}
