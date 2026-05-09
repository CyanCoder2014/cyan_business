package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.config.AutomationCallbackProperties;
import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.cyancoder.automationorchestrator.model.AutomationStartRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class AutomationExecutionServiceTest {

    @Test
    void startCompletesExecutionAndCallbacksBpm() {
        AutomationExecutionRepository repository = mock(AutomationExecutionRepository.class);
        InternalServiceHttpSupport httpSupport = mock(InternalServiceHttpSupport.class);
        AutomationCallbackProperties properties = new AutomationCallbackProperties();
        properties.setSecret("localdemo-secret");
        AutomationExecutionService service = new AutomationExecutionService(repository, httpSupport, properties, new ObjectMapper());

        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(any(AutomationExecution.class));
        when(httpSupport.internalHeaders("bpm-service", "tenant-demo", "site-demo")).thenReturn(new HttpHeaders());

        AutomationStartResponse response = service.start(new AutomationStartRequest(
                "hybrid-screening-automation",
                "obj-1:screening",
                "/public/bpm/async-actions/callbacks/obj-1:screening",
                "tenant-demo",
                "site-demo",
                Map.of("fullName", "Jane Roe", "nationalId", "99887766", "requestedAmount", 15000),
                Map.of("managedObjectId", "obj-1")
        ));

        assertEquals("COMPLETED", response.status());
        assertEquals("hybrid-screening-automation", response.automationFlowKey());
        verify(httpSupport).exchange(eq("bpm-service"), eq("/public/bpm/async-actions/callbacks/obj-1:screening"), eq(HttpMethod.POST), any(), any(HttpHeaders.class), eq(Map.class));
    }
}
