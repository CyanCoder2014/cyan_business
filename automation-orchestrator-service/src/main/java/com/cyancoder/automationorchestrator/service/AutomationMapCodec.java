package com.cyancoder.automationorchestrator.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AutomationMapCodec {
    private static final String DOT = "\uFF0E";
    private AutomationMapCodec() { }

    static Map<String, Object> mongoSafe(Map<?, ?> source) { return transformMap(source, true); }
    static Map<String, Object> restore(Map<?, ?> source) { return transformMap(source, false); }

    private static Map<String, Object> transformMap(Map<?, ?> source, boolean encode) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source == null) return result;
        source.forEach((key, value) -> result.put(transformKey(String.valueOf(key), encode), transformValue(value, encode)));
        return result;
    }

    private static String transformKey(String key, boolean encode) {
        return encode ? key.replace(".", DOT) : key.replace(DOT, ".");
    }

    private static Object transformValue(Object value, boolean encode) {
        if (value instanceof Map<?, ?> map) return transformMap(map, encode);
        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            iterable.forEach(item -> result.add(transformValue(item, encode)));
            return result;
        }
        return value;
    }
}
