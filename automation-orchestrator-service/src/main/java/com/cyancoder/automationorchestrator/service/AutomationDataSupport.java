package com.cyancoder.automationorchestrator.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AutomationDataSupport {
    private static final Pattern TEMPLATE = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*}}");
    private AutomationDataSupport() { }

    static Object readPath(Object root, String path) {
        if (root == null || path == null || path.isBlank()) return root;
        Object current = root;
        for (String part : path.replace("$.", "").split("\\.")) {
            if (current instanceof Map<?, ?> map) current = map.get(part);
            else if (current instanceof List<?> list && part.matches("\\d+")) current = list.get(Integer.parseInt(part));
            else return null;
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    static void setPath(Map<String, Object> root, String path, Object value) {
        if (root == null || path == null || path.isBlank()) throw new IllegalArgumentException("target path is required");
        String[] parts = path.replace("$.", "").split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object child = current.get(parts[i]);
            if (!(child instanceof Map<?, ?>)) { child = new LinkedHashMap<String, Object>(); current.put(parts[i], child); }
            current = (Map<String, Object>) child;
        }
        current.put(parts[parts.length - 1], value);
    }

    static Object materialize(Object value, Map<String, Object> variables, Map<String, Object> context) {
        if (value instanceof Map<?, ?> map) { Map<String,Object> result=new LinkedHashMap<>(); map.forEach((k,v)->{if(k!=null)result.put(k.toString(),materialize(v,variables,context));}); return result; }
        if (value instanceof Iterable<?> iterable) { List<Object> result=new ArrayList<>(); iterable.forEach(v->result.add(materialize(v,variables,context))); return result; }
        if (!(value instanceof String text) || !text.contains("{{")) return value;
        Matcher matcher=TEMPLATE.matcher(text);
        if (matcher.matches()) return resolve(matcher.group(1), variables, context);
        StringBuffer result=new StringBuffer();
        while(matcher.find()){Object replacement=resolve(matcher.group(1),variables,context);matcher.appendReplacement(result,Matcher.quoteReplacement(replacement==null?"":replacement.toString()));}
        matcher.appendTail(result); return result.toString();
    }

    static Object resolve(Object pathOrTemplate, Map<String,Object> variables, Map<String,Object> context) {
        Object value=materialize(pathOrTemplate,variables,context);
        if(value instanceof String path && !path.contains(" ")) { Object fromContext=readPath(context,path); if(fromContext!=null)return fromContext; Object fromVariables=readPath(variables,path); if(fromVariables!=null)return fromVariables; }
        return value;
    }

    private static Object resolve(String expression, Map<String,Object> variables, Map<String,Object> context) {
        String path=expression.trim();
        if(path.startsWith("$json.")) path=path.substring(6);
        if(path.startsWith("variables.")) return readPath(variables,path.substring(10));
        if(path.startsWith("context.")) return readPath(context,path.substring(8));
        Object c=readPath(context,path); return c==null?readPath(variables,path):c;
    }

    static boolean compare(Object left,String operator,Object right){String op=operator==null?"EQ":operator.trim().toUpperCase(Locale.ROOT);return switch(op){case"EQ"->Objects.equals(norm(left),norm(right));case"NE","NEQ"->!Objects.equals(norm(left),norm(right));case"GT"->number(left)>number(right);case"GTE","GE"->number(left)>=number(right);case"LT"->number(left)<number(right);case"LTE","LE"->number(left)<=number(right);case"IN"->right instanceof Collection<?> c&&c.stream().anyMatch(v->Objects.equals(norm(left),norm(v)));case"CONTAINS"->left instanceof Collection<?> c&&c.stream().anyMatch(v->Objects.equals(norm(v),norm(right)))||left instanceof String s&&right!=null&&s.contains(right.toString());case"EMPTY"->left==null||left.toString().isBlank();case"NOT_EMPTY"->left!=null&&!left.toString().isBlank();default->false;};}
    static Map<String,Object> map(Object value){Map<String,Object> result=new LinkedHashMap<>();if(value instanceof Map<?,?> map)map.forEach((k,v)->{if(k!=null)result.put(k.toString(),v);});return result;}
    static List<Object> list(Object value){if(value instanceof List<?> list)return new ArrayList<>(list);if(value instanceof Iterable<?> it){List<Object> result=new ArrayList<>();it.forEach(result::add);return result;}return List.of();}
    static Object copy(Object value){if(value instanceof Map<?,?> map){Map<String,Object> result=new LinkedHashMap<>();map.forEach((k,v)->{if(k!=null)result.put(k.toString(),copy(v));});return result;}if(value instanceof List<?> list)return list.stream().map(AutomationDataSupport::copy).toList();return value;}
    static Map<String,Object> fileMetadata(Object value){Map<String,Object> source=map(value);Map<String,Object> out=new LinkedHashMap<>(source);String encoded=string(source.get("base64"));if(encoded==null)encoded=string(source.get("data"));if(encoded==null)return out;try{byte[] bytes=Base64.getDecoder().decode(encoded);out.put("size",bytes.length);out.put("sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));out.putIfAbsent("contentType","application/octet-stream");}catch(Exception ex){out.put("binaryError","invalidBase64");}return out;}
    static Instant instant(Object value){if(value instanceof Instant instant)return instant;if(value==null)return null;try{return Instant.parse(value.toString());}catch(Exception ex){return null;}}
    static String string(Object value){return value==null?null:value.toString();}
    static long longValue(Object value,long fallback){if(value instanceof Number n)return n.longValue();try{return value==null?fallback:Long.parseLong(value.toString());}catch(Exception ex){return fallback;}}
    static boolean bool(Object value,boolean fallback){return value==null?fallback:value instanceof Boolean b?b:Boolean.parseBoolean(value.toString());}
    private static Object norm(Object value){return value instanceof String s?s.trim():value;}
    private static double number(Object value){if(value instanceof Number n)return n.doubleValue();try{return value==null?0:Double.parseDouble(value.toString());}catch(Exception ex){return 0;}}
}
