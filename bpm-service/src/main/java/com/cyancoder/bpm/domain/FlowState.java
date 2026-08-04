package com.cyancoder.bpm.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FlowState(
        @Field("id")
        String id,
        String displayName,
        boolean terminal,
        @JsonAlias("formId") String formKey,
        @JsonAlias("processorId") String processorKey,
        boolean reviewCommentRequired,
        Set<String> candidateGroups,
        List<FlowActionConfig> onEnterActions,
        FlowAccessRule accessRule,
        String entityService,
        String entityKey,
        String rendererService,
        String rendererKey,
        SubmitMode submitMode,
        String submitUrl,
        boolean waitForAutomation
) {
}
