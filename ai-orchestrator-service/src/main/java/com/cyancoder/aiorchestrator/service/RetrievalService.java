package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;

import java.util.List;
import java.util.Map;

public interface RetrievalService {
    default List<String> retrieveContext(String prompt) {
        return retrieveContext(prompt, Map.of(), List.of(), null, null);
    }

    List<String> retrieveContext(String prompt,
                                 Map<String, Object> structuredState,
                                 List<String> priorMessages,
                                 AppBlueprint blueprint,
                                 ClientAppDraft draft);
}
