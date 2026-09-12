package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationEdge;
import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNode;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import com.cyancoder.automationorchestrator.repo.AutomationFlowDefinitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutomationFlowDefinitionServiceTest {

    private final AutomationFlowDefinitionRepository repository = mock(AutomationFlowDefinitionRepository.class);
    private final AutomationFlowDefinitionService service = new AutomationFlowDefinitionService(repository, new ObjectMapper().findAndRegisterModules());

    @Test
    void savesMongoSafeConfigAndReturnsOriginalDottedMappingPaths() {
        when(repository.save(any(AutomationFlowDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AutomationFlowDefinition definition = definition("mapping-flow", 1, "default", "DRAFT");
        definition.setNodes(List.of(
                node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("map", AutomationNodeType.MAP_FIELDS, Map.of("mappings", Map.of("result.amount", "amount"))),
                node("end", AutomationNodeType.END, Map.of())
        ));
        definition.setEdges(List.of(edge("trigger", null, "map"), edge("map", null, "end")));

        AutomationFlowDefinition saved = service.save("tenant", "site", definition, "builder");

        assertTrue(saved.getNodes().get(1).configOrEmpty().toString().contains("result.amount"));
        ArgumentCaptor<AutomationFlowDefinition> captor = ArgumentCaptor.forClass(AutomationFlowDefinition.class);
        verify(repository).save(captor.capture());
        assertTrue(captor.getValue().getNodes().get(1).configOrEmpty().toString().contains("result\uFF0Eamount"));
    }

    @Test
    void promotionCreatesSeparateApprovedVersionInTargetEnvironment() {
        AutomationFlowDefinition source = definition("promoted-flow", 3, "default", "ACTIVE");
        source.setId("source-id");
        source.setActive(true);
        when(repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyAndVersion("tenant", "site", "promoted-flow", 3))
                .thenReturn(Optional.of(source));
        when(repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc("tenant", "site", "promoted-flow"))
                .thenReturn(Optional.of(source));
        when(repository.save(any(AutomationFlowDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AutomationFlowDefinition promoted = service.lifecycle("tenant", "site", "promoted-flow", 3,
                "PROMOTE", "approver", "stage");

        assertNull(promoted.getId());
        assertEquals(4, promoted.getVersion());
        assertEquals("stage", promoted.getEnvironment());
        assertEquals("APPROVED", promoted.getLifecycleStatus());
        assertFalse(promoted.isActive());
        assertEquals("default", source.getEnvironment());
        assertTrue(source.isActive());
    }

    @Test
    void activeLookupIsEnvironmentSpecific() {
        AutomationFlowDefinition stage = definition("environment-flow", 2, "stage", "ACTIVE");
        stage.setActive(true);
        when(repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyAndEnvironmentAndActiveTrueOrderByVersionDesc(
                "tenant", "site", "environment-flow", "stage")).thenReturn(Optional.of(stage));

        AutomationFlowDefinition found = service.active("tenant", "site", "environment-flow", "stage");

        assertEquals("stage", found.getEnvironment());
    }

    @Test
    void validatesGenericParallelAndExternalOperationContracts() {
        AutomationFlowDefinition parallel = definition("parallel-flow", 1, "default", "DRAFT");
        parallel.setNodes(List.of(
                node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("fanout", AutomationNodeType.PARALLEL_SUBFLOWS, Map.of("branches", Map.of(
                        "first", Map.of("flowKey", "child-a"), "second", Map.of("flowKey", "child-b")), "resultPath", "results")),
                node("end", AutomationNodeType.END, Map.of())));
        parallel.setEdges(List.of(edge("trigger", null, "fanout"), edge("fanout", null, "end")));
        service.validate(parallel);

        AutomationFlowDefinition invalid = definition("operation-flow", 1, "default", "DRAFT");
        invalid.setNodes(List.of(node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("operation", AutomationNodeType.EXTERNAL_OPERATION, Map.of("operationKey", "caseId")), node("end", AutomationNodeType.END, Map.of())));
        invalid.setEdges(List.of(edge("trigger", null, "operation"), edge("operation", null, "end")));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.validate(invalid));
    }

    private AutomationFlowDefinition definition(String key, int version, String environment, String status) {
        AutomationFlowDefinition value = new AutomationFlowDefinition();
        value.setTenantKey("tenant");
        value.setSiteKey("site");
        value.setFlowKey(key);
        value.setVersion(version);
        value.setEnvironment(environment);
        value.setLifecycleStatus(status);
        value.setEntryNodeId("trigger");
        value.setNodes(List.of(node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()), node("end", AutomationNodeType.END, Map.of())));
        value.setEdges(List.of(edge("trigger", null, "end")));
        return value;
    }

    private AutomationNode node(String id, AutomationNodeType type, Map<String, Object> config) {
        return new AutomationNode(id, type, id, true, null, null, null, null, null, config, null, null);
    }

    private AutomationEdge edge(String from, String port, String to) {
        return new AutomationEdge(from + "-" + to, from, port, to);
    }
}
