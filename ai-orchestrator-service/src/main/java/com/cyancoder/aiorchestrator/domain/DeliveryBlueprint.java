package com.cyancoder.aiorchestrator.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeliveryBlueprint {
    private List<String> publicApis = new ArrayList<>();
    private List<String> botApis = new ArrayList<>();

    public List<String> getPublicApis() { return publicApis; }
    public void setPublicApis(List<?> publicApis) { this.publicApis = normalizeApiList(publicApis); }
    public List<String> getBotApis() { return botApis; }
    public void setBotApis(List<?> botApis) { this.botApis = normalizeApiList(botApis); }

    private static List<String> normalizeApiList(List<?> apis) {
        if (apis == null) {
            return new ArrayList<>();
        }
        return apis.stream()
                .map(DeliveryBlueprint::normalizeApi)
                .filter(api -> api != null && !api.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String normalizeApi(Object api) {
        if (api == null) {
            return null;
        }
        if (api instanceof String value) {
            return value.trim();
        }
        if (api instanceof Map<?, ?> map) {
            String path = firstString(map, "path", "url", "endpoint", "api", "route");
            if (path != null && !path.isBlank()) {
                return path.trim();
            }
            String label = firstString(map, "name", "title", "description");
            if (label != null && !label.isBlank()) {
                return label.trim();
            }
        }
        return String.valueOf(api).trim();
    }

    private static String firstString(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof String stringValue && !stringValue.isBlank()) {
                return stringValue;
            }
        }
        return null;
    }
}
