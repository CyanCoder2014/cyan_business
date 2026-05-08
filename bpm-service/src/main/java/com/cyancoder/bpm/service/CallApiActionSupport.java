package com.cyancoder.bpm.service;

import com.cyancoder.bpm.domain.ManagedObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CallApiActionSupport {
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*}}");

    private CallApiActionSupport() {
    }

    static Object resolveTemplate(Object value, ManagedObject object, String actor) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> resolved = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    resolved.put(entry.getKey().toString(), resolveTemplate(entry.getValue(), object, actor));
                }
            }
            return resolved;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> resolved = new ArrayList<>();
            for (Object item : iterable) {
                resolved.add(resolveTemplate(item, object, actor));
            }
            return resolved;
        }
        if (!(value instanceof String template)) {
            return value;
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        if (!matcher.find()) {
            return template;
        }
        matcher.reset();
        if (matcher.matches()) {
            return ActionPayloadSupport.resolveObjectValue(object, matcher.group(1).trim(), actor);
        }
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            Object resolved = ActionPayloadSupport.resolveObjectValue(object, matcher.group(1).trim(), actor);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(resolved == null ? "" : resolved.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    static void applyResponseMappings(ManagedObject object, Map<String, Object> response, Object mappingsObject) {
        if (!(mappingsObject instanceof Map<?, ?> mappings)) {
            return;
        }
        for (Map.Entry<?, ?> entry : mappings.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            String targetPath = entry.getKey().toString();
            Object value = ActionPayloadSupport.readPath(response, entry.getValue().toString());
            ActionPayloadSupport.setPayloadPath(object, targetPath, value);
        }
    }

    static void applyResponseMappingsFromCallback(ManagedObject object, Map<String, Object> callbackPayload, Object mappingsObject) {
        applyResponseMappings(object, callbackPayload, mappingsObject);
    }
}

