package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicEntityResponseMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicEntityResponseMapper mapper = new DynamicEntityResponseMapper(
            new DynamicDefinitionParser(objectMapper)
    );

    @Test
    void serializesStoredDefinitionAsStructuredDefinitionObject() throws Exception {
        StoredEntityDefinition stored = new StoredEntityDefinition();
        stored.setServiceKey("bpm-service");
        stored.setEntityKey("approval-form");
        stored.setDefinitionJson("""
                {"entityKey":"approval-form","entityType":"BPM_FORM","fields":{}}
                """);

        JsonNode response = objectMapper.valueToTree(mapper.toDefinitionResponse(stored));

        assertThat(response.get("definition").isObject()).isTrue();
        assertThat(response.at("/definition/entityType").asText()).isEqualTo("BPM_FORM");
        assertThat(response.has("definitionJson")).isFalse();
    }

    @Test
    void serializesTemplateAsStructuredDefinitionObject() {
        DynamicEntityTemplate template = new DynamicEntityTemplate(
                "approval-form",
                "BPM_FORM",
                "Approval Form",
                "Approval template",
                "{\"entityKey\":\"approval-form\",\"fields\":{}}"
        );

        JsonNode response = objectMapper.valueToTree(mapper.toTemplateResponse(template));

        assertThat(response.get("definition").isObject()).isTrue();
        assertThat(response.at("/definition/entityKey").asText()).isEqualTo("approval-form");
        assertThat(response.has("definitionJson")).isFalse();
    }
}
