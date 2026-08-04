package com.cyancoder.bpm.service;

import com.cyancoder.bpm.domain.ManagedObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ActionPayloadSupport {
    private ActionPayloadSupport() {
    }

    static Object resolveObjectValue(ManagedObject object, String expression, String actor) {
        if (expression == null || expression.isBlank() || object == null) {
            return null;
        }
        if (expression.startsWith("payload.")) {
            return readPath(object.getPayload(), expression.substring("payload.".length()));
        }
        return switch (expression) {
            case "objectId" -> object.getId();
            case "actorUserId" -> actor;
            case "state" -> object.getState();
            case "assignee" -> object.getAssignee();
            case "flowKey" -> object.getFlowKey();
            case "objectType" -> object.getObjectType();
            case "locked" -> object.isLocked();
            default -> null;
        };
    }

    static Object readPath(Map<String, Object> source, String path) {
        if (source == null || path == null || path.isBlank()) {
            return null;
        }
        Object current = source;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    static void setPayloadPath(ManagedObject object, String targetPath, Object value) {
        if (object == null || targetPath == null || targetPath.isBlank()) {
            return;
        }
        String normalized = normalizePayloadPath(targetPath);
        String[] parts = normalized.split("\\.");
        Map<String, Object> current = object.getPayload();
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                return;
            }
            Object next = current.get(part);
            if (!(next instanceof Map<?, ?> nextMap)) {
                Map<String, Object> nested = new LinkedHashMap<>();
                current.put(part, nested);
                current = nested;
                continue;
            }
            Map<String, Object> nested = mutableMap(nextMap);
            current.put(part, nested);
            current = nested;
        }
        String last = parts[parts.length - 1].trim();
        if (!last.isEmpty()) {
            current.put(last, value);
        }
    }

    static void removePayloadPath(ManagedObject object, String targetPath) {
        if (object == null || targetPath == null || targetPath.isBlank()) {
            return;
        }
        String normalized = normalizePayloadPath(targetPath);
        String[] parts = normalized.split("\\.");
        Map<String, Object> current = object.getPayload();
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                return;
            }
            Object next = current.get(part);
            if (!(next instanceof Map<?, ?> nextMap)) {
                return;
            }
            Map<String, Object> nested = mutableMap(nextMap);
            current.put(part, nested);
            current = nested;
        }
        String last = parts[parts.length - 1].trim();
        if (!last.isEmpty()) {
            current.remove(last);
        }
    }

    static List<String> toStringList(Object value) {
        if (value instanceof Iterable<?> iterable) {
            List<String> result = new ArrayList<>();
            for (Object item : iterable) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        if (value == null) {
            return List.of();
        }
        return List.of(value.toString());
    }

    private static String normalizePayloadPath(String path) {
        return path.startsWith("payload.") ? path.substring("payload.".length()) : path;
    }

    private static Map<String, Object> mutableMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() != null) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return result;
    }
}
