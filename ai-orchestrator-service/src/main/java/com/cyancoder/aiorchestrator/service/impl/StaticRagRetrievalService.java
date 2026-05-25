package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.config.RagProperties;
import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.service.RetrievalService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StaticRagRetrievalService implements RetrievalService {
    private final List<String> knowledge = new ArrayList<>();
    private final RagProperties properties;

    public StaticRagRetrievalService(RagProperties properties) {
        this.properties = properties;
        bootstrap();
    }

    @Override
    public List<String> retrieveContext(String prompt,
                                        Map<String, Object> structuredState,
                                        List<String> priorMessages,
                                        AppBlueprint blueprint,
                                        ClientAppDraft draft) {
        if (!properties.isEnabled()) {
            return List.of();
        }
        String lower = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        List<String> context = new ArrayList<>(knowledge.stream()
                .sorted(Comparator.comparingInt((String value) -> score(value.toLowerCase(Locale.ROOT), lower)).reversed())
                .filter(value -> score(value.toLowerCase(Locale.ROOT), lower) > 0)
                .limit(properties.getTopK())
                .toList());
        String userState = renderStructuredState(structuredState);
        if (!userState.isBlank()) {
            context.add(userState);
        }
        String priorDecisionContext = renderPriorDecisions(priorMessages, draft);
        if (!priorDecisionContext.isBlank()) {
            context.add(priorDecisionContext);
        }
        String blueprintContext = renderBlueprint(blueprint);
        if (!blueprintContext.isBlank()) {
            context.add(blueprintContext);
        }
        return context;
    }

    private void bootstrap() {
        if (!properties.isBootstrapEnabled()) {
            return;
        }
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            for (Resource resource : resolver.getResources(properties.getBootstrapResourcePattern())) {
                knowledge.add(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }
    }

    private int score(String text, String prompt) {
        int score = 0;
        for (String token : prompt.split("\\s+")) {
            if (!token.isBlank() && text.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private String renderStructuredState(Map<String, Object> structuredState) {
        if (structuredState == null || structuredState.isEmpty()) {
            return "";
        }
        return "Structured user state:\n" + structuredState.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !String.valueOf(entry.getValue()).isBlank())
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    private String renderPriorDecisions(List<String> priorMessages, ClientAppDraft draft) {
        List<String> sections = new ArrayList<>();
        if (draft != null) {
            sections.add("draftId: " + draft.getDraftId());
            sections.add("appType: " + draft.getAppType());
            sections.add("latestIntent: " + safe(draft.getLatestIntent()));
            if (draft.getAnswers() != null && !draft.getAnswers().isEmpty()) {
                sections.add("answers: " + draft.getAnswers().entrySet().stream()
                        .filter(entry -> entry.getValue() != null && !String.valueOf(entry.getValue()).isBlank())
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(", ")));
            }
            if (draft.getPendingQuestions() != null && !draft.getPendingQuestions().isEmpty()) {
                sections.add("pendingQuestions: " + String.join(" | ", draft.getPendingQuestions()));
            }
        }
        if (priorMessages != null && !priorMessages.isEmpty()) {
            sections.add("recentMessages: " + priorMessages.stream().limit(4).collect(Collectors.joining(" | ")));
        }
        if (sections.isEmpty()) {
            return "";
        }
        return "Prior decisions:\n" + String.join("\n", sections);
    }

    private String renderBlueprint(AppBlueprint blueprint) {
        if (blueprint == null) {
            return "";
        }
        List<String> sections = new ArrayList<>();
        sections.add("blueprintKey: " + safe(blueprint.getBlueprintKey()));
        sections.add("version: " + blueprint.getVersion());
        sections.add("title: " + safe(blueprint.getTitle()));
        sections.add("description: " + safe(blueprint.getDescription()));
        if (blueprint.getCapabilities() != null && !blueprint.getCapabilities().isEmpty()) {
            sections.add("capabilities: " + String.join(", ", blueprint.getCapabilities()));
        }
        if (blueprint.getRequiredQuestions() != null && !blueprint.getRequiredQuestions().isEmpty()) {
            sections.add("requiredQuestions: " + blueprint.getRequiredQuestions().stream()
                    .map(question -> question.getKey() + "=" + question.getPrompt())
                    .collect(Collectors.joining(" | ")));
        }
        return "Blueprint version context:\n" + String.join("\n", sections);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
