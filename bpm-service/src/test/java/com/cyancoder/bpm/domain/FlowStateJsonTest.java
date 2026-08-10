package com.cyancoder.bpm.domain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowStateJsonTest {

    @Test
    void optionalBooleanFlagsDefaultToFalseForGeneratedFlowStates() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        FlowState state = mapper.readValue("""
                {
                  "id": "approved-order",
                  "displayName": "Approved Order",
                  "terminal": true
                }
                """, FlowState.class);

        assertThat(state.terminal()).isTrue();
        assertThat(state.reviewCommentRequired()).isFalse();
        assertThat(state.waitForAutomation()).isFalse();
    }
}
