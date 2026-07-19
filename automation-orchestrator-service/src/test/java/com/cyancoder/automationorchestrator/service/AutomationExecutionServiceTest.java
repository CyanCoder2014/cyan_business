package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.config.AutomationCallbackProperties;
import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.cyancoder.automationorchestrator.domain.AutomationExecutionMode;
import com.cyancoder.automationorchestrator.domain.AutomationFailurePolicy;
import com.cyancoder.automationorchestrator.model.AutomationStartRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutomationExecutionServiceTest {

    @Test
    void startCompletesSyncExecutionAndCallbacksBpm() {
        AutomationExecutionRepository repository = mock(AutomationExecutionRepository.class);
        InternalServiceHttpSupport httpSupport = mock(InternalServiceHttpSupport.class);
        AutomationCallbackProperties properties = new AutomationCallbackProperties();
        properties.setSecret("localdemo-secret");
        AutomationExecutionService service = new AutomationExecutionService(repository, httpSupport, properties, new ObjectMapper());

        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(any(AutomationExecution.class));
        when(httpSupport.internalHeaders("bpm-service", "tenant-demo", "site-demo")).thenReturn(new HttpHeaders());

        AutomationStartResponse response = service.start(new AutomationStartRequest(
                "screening",
                "hybrid-screening-automation",
                AutomationExecutionMode.SYNC,
                AutomationFailurePolicy.MARK_FAILED,
                "obj-1:screening",
                "/public/bpm/async-actions/callbacks/obj-1:screening",
                "tenant-demo",
                "site-demo",
                Map.of("fullName", "Jane Roe", "nationalId", "99887766", "requestedAmount", 15000),
                Map.of("managedObjectId", "obj-1"),
                Map.of(),
                0,
                60L,
                0L,
                null,
                "obj-1",
                "obj-1:screening",
                null,
                null
        ));

        assertEquals("COMPLETED", response.status());
        assertEquals("screening", response.blockKey());
        assertEquals("hybrid-screening-automation", response.automationFlowKey());
        verify(httpSupport).exchange(eq("bpm-service"), eq("/public/bpm/async-actions/callbacks/obj-1:screening"), eq(HttpMethod.POST), any(), any(HttpHeaders.class), eq(Map.class));
    }

    @Test
    void startAcceptsWorkflowGenerationAliases() {
        AutomationExecutionRepository repository = mock(AutomationExecutionRepository.class);
        InternalServiceHttpSupport httpSupport = mock(InternalServiceHttpSupport.class);
        AutomationCallbackProperties properties = new AutomationCallbackProperties();
        properties.setSecret("localdemo-secret");
        AutomationExecutionService service = new AutomationExecutionService(repository, httpSupport, properties, new ObjectMapper());

        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(any(AutomationExecution.class));

        AutomationStartResponse response = service.start(new AutomationStartRequest(
                null,
                null,
                AutomationExecutionMode.SYNC,
                AutomationFailurePolicy.CONTINUE,
                null,
                null,
                "tenant-demo",
                "site-demo",
                null,
                Map.of("source", "panel"),
                null,
                0,
                60L,
                0L,
                "form-enrichment-flow",
                "managed-1",
                "managed-1:form-enrichment",
                Map.of("fullName", "Jane Roe"),
                Map.of("type", "MAP_OUTPUT", "output", Map.of("accepted", true))
        ));

        assertEquals("COMPLETED", response.status());
        assertEquals("form-enrichment-flow", response.automationFlowKey());
        assertEquals("managed-1", response.managedObjectId());
        assertEquals("managed-1:form-enrichment", response.idempotencyKey());
        assertEquals(true, response.output().get("accepted"));
    }

    @Test
    void startEscapesDottedPipelineMappingKeysForMongoAndRestoresThemForExecution() {
        AutomationExecutionRepository repository = mock(AutomationExecutionRepository.class);
        InternalServiceHttpSupport httpSupport = mock(InternalServiceHttpSupport.class);
        AutomationCallbackProperties properties = new AutomationCallbackProperties();
        AutomationExecutionService service = new AutomationExecutionService(repository, httpSupport, properties, new ObjectMapper());
        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(any(AutomationExecution.class));

        AutomationStartResponse response = service.start(new AutomationStartRequest(
                "pipeline", null, AutomationExecutionMode.SYNC, AutomationFailurePolicy.MARK_FAILED,
                null, null, "tenant-demo", "site-demo", Map.of("name", "Farid"), Map.of(),
                Map.of("type", "PIPELINE", "outputPath", "result", "steps", java.util.List.of(
                        Map.of("type", "MAP_FIELDS", "mappings", Map.of("result.greeting", "Hello {{name}}"))
                )), 0, 60L, 0L, null, null, null, null, null
        ));

        assertEquals("COMPLETED", response.status());
        assertEquals("Hello Farid", response.output().get("greeting"));
        ArgumentCaptor<AutomationExecution> captor = ArgumentCaptor.forClass(AutomationExecution.class);
        verify(repository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        Map<?, ?> storedMappings = (Map<?, ?>) ((Map<?, ?>) ((java.util.List<?>) captor.getAllValues().get(0)
                .getInlineFragment().get("steps")).get(0)).get("mappings");
        assertTrue(storedMappings.containsKey("result\uFF0Egreeting"));
    }

    @Test
    void cancelMarksAsyncExecutionCancelled() {
        AutomationExecutionRepository repository = mock(AutomationExecutionRepository.class);
        InternalServiceHttpSupport httpSupport = mock(InternalServiceHttpSupport.class);
        AutomationCallbackProperties properties = new AutomationCallbackProperties();
        properties.setSecret("localdemo-secret");
        AutomationExecutionService service = new AutomationExecutionService(repository, httpSupport, properties, new ObjectMapper());

        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(any(AutomationExecution.class));

        AutomationExecution stored = new AutomationExecution();
        stored.setExecutionId("exec-cancel");
        stored.setBlockKey("screening");
        stored.setAutomationFlowKey("hybrid-screening-automation");
        stored.setExecutionMode(AutomationExecutionMode.ASYNC);
        stored.setFailurePolicy(AutomationFailurePolicy.MARK_FAILED);
        stored.setStatus("RUNNING");
        when(repository.findByExecutionId("exec-cancel")).thenReturn(java.util.Optional.of(stored));

        AutomationStartResponse cancelled = service.cancel("exec-cancel");

        assertEquals("CANCELLED", cancelled.status());
        assertTrue(stored.isCancelRequested());
    }
}
