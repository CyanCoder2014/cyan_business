package com.cyancoder.bpm.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicantAccessPolicyJsonTest {
    @Test
    void deserializesDefinitionConfiguredApplicantRestrictions() throws Exception {
        DynamicFlowDefinition definition = new ObjectMapper().readValue("""
                {"flowKey":"generic-application","applicantAccess":{"enabled":true,"objectType":"APPLICATION",
                "startPayloadFields":["requestKey"],"formStateIds":["collect"],
                "formNextStates":{"collect":"review"},"statusPayloadFields":["result.status"]}}""", DynamicFlowDefinition.class);
        assertThat(definition.getApplicantAccess().isEnabled()).isTrue();
        assertThat(definition.getApplicantAccess().objectType()).isEqualTo("APPLICATION");
        assertThat(definition.getApplicantAccess().formNextStates()).isEqualTo(Map.of("collect", "review"));
        assertThat(definition.getApplicantAccess().statusPayloadFields()).isEqualTo(Set.of("result.status"));
    }
}
