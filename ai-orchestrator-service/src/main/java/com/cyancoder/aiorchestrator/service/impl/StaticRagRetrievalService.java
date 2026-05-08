package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.config.RagProperties;
import com.cyancoder.aiorchestrator.service.RetrievalService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class StaticRagRetrievalService implements RetrievalService {
    private final List<String> knowledge = new ArrayList<>();
    private final RagProperties properties;

    public StaticRagRetrievalService(RagProperties properties) {
        this.properties = properties;
        bootstrap();
    }

    @Override
    public List<String> retrieveContext(String prompt) {
        if (!properties.isEnabled()) {
            return List.of();
        }
        String lower = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        return knowledge.stream()
                .sorted(Comparator.comparingInt((String value) -> score(value.toLowerCase(Locale.ROOT), lower)).reversed())
                .filter(value -> score(value.toLowerCase(Locale.ROOT), lower) > 0)
                .limit(properties.getTopK())
                .toList();
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
}

