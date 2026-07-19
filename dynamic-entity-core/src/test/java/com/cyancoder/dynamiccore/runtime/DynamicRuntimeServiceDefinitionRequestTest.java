package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.model.EntityDefinitionModel;
import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.service.DynamicOperatorEngine;
import com.cyancoder.dynamiccore.service.DynamicValidationEngine;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinitionRepository;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordRepository;
import com.cyancoder.dynamiccore.template.DynamicTemplateRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicRuntimeServiceDefinitionRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DynamicRuntimeService runtimeService;

    @BeforeEach
    void setUp() {
        StoredEntityDefinitionRepository definitionRepository = mock(StoredEntityDefinitionRepository.class);
        when(definitionRepository.findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(
                anyString(), any(), any(), anyString()
        )).thenReturn(Optional.empty());
        when(definitionRepository.save(any(StoredEntityDefinition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DynamicRuntimeProperties properties = new DynamicRuntimeProperties();
        properties.setServiceKey("bpm-service");
        runtimeService = new DynamicRuntimeService(
                definitionRepository,
                mock(DynamicEntityRecordRepository.class),
                new DynamicDefinitionParser(objectMapper),
                mock(DynamicValidationEngine.class),
                mock(DynamicOperatorEngine.class),
                properties,
                mock(DynamicTemplateRegistry.class)
        );
    }

    @Test
    void acceptsStructuredDefinitionAndSerializesItForStorage() throws Exception {
        DynamicEntityDefinitionRequest request = objectMapper.readValue("""
                {
                  "entityKey": "leave-request-form",
                  "tenantKey": "demo-tenant",
                  "siteKey": "main-site",
                  "definition": {
                    "entityType": "BPM_FORM",
                    "title": "Leave Request",
                    "fields": {}
                  }
                }
                """, DynamicEntityDefinitionRequest.class);

        StoredEntityDefinition saved = runtimeService.saveDefinition(request);
        EntityDefinitionModel storedModel = objectMapper.readValue(saved.getDefinitionJson(), EntityDefinitionModel.class);

        assertThat(saved.getServiceKey()).isEqualTo("bpm-service");
        assertThat(saved.getEntityKey()).isEqualTo("leave-request-form");
        assertThat(saved.getEntityType()).isEqualTo("BPM_FORM");
        assertThat(saved.getTitle()).isEqualTo("Leave Request");
        assertThat(storedModel.getServiceKey()).isEqualTo("bpm-service");
        assertThat(storedModel.getEntityKey()).isEqualTo("leave-request-form");
        assertThat(storedModel.getFields()).isEqualTo(Map.of());
    }

    @Test
    void continuesToAcceptLegacyDefinitionJson() {
        String definitionJson = """
                {"serviceKey":"bpm-service","entityKey":"legacy-form","entityType":"BPM_FORM","title":"Legacy Form","fields":{}}
                """.trim();
        DynamicEntityDefinitionRequest request = new DynamicEntityDefinitionRequest();
        request.setEntityKey("legacy-form");
        request.setDefinitionJson(definitionJson);

        StoredEntityDefinition saved = runtimeService.saveDefinition(request);

        assertThat(saved.getTitle()).isEqualTo("Legacy Form");
        assertThat(saved.getDefinitionJson()).isEqualTo(definitionJson);
    }

    @Test
    void structuredDefinitionTakesPrecedenceDuringMigration() {
        EntityDefinitionModel structured = new EntityDefinitionModel();
        structured.setEntityKey("preferred-form");
        structured.setEntityType("BPM_FORM");
        structured.setTitle("Structured Form");
        structured.setFields(Map.of());

        DynamicEntityDefinitionRequest request = new DynamicEntityDefinitionRequest();
        request.setEntityKey("preferred-form");
        request.setDefinition(structured);
        request.setDefinitionJson("not valid json");

        StoredEntityDefinition saved = runtimeService.saveDefinition(request);

        assertThat(saved.getTitle()).isEqualTo("Structured Form");
    }

    @Test
    void rejectsRequestsWithoutEitherDefinitionRepresentation() {
        DynamicEntityDefinitionRequest request = new DynamicEntityDefinitionRequest();
        request.setEntityKey("missing-definition");

        assertThatThrownBy(() -> runtimeService.saveDefinition(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("definition or definitionJson is required");
    }
}
