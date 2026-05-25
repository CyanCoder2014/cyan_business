package com.cyancoder.dynamiccore.template;

import java.util.Comparator;
import java.util.List;

public class DynamicTemplateRegistry {

    private final List<DynamicTemplateProvider> providers;

    public DynamicTemplateRegistry(List<DynamicTemplateProvider> providers) {
        this.providers = providers;
    }

    public List<DynamicEntityTemplate> list() {
        return providers.stream()
                .flatMap(provider -> provider.templates().stream())
                .sorted(Comparator.comparing(DynamicEntityTemplate::getTemplateKey))
                .toList();
    }

    public DynamicEntityTemplate get(String templateKey) {
        return list().stream()
                .filter(template -> template.getTemplateKey().equals(templateKey))
                .findFirst()
                .orElseThrow();
    }
}
