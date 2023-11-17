package com.cyancoder.taxpaysys.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.Collator;
import java.util.*;
public class CryptoUtils {

    private CryptoUtils(){}

    private final static ObjectMapper mapper = new ObjectMapper();
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    public static String normalJson(Object object, Map<String, Object> header) {
        if (object == null && header == null)
            return null;
        Map<String, Object> map = null;
        if (object != null) {
            if (object instanceof String) {
                try {
                    object = mapper.readValue((String) object, Object.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            if (object instanceof Collection) {
                PacketsWrapper packetsWrapper = new PacketsWrapper((Collection) object);
                map = mapper.convertValue(packetsWrapper, Map.class);
            } else {
                map = mapper.convertValue(object, Map.class);
            }
        }

        if (map == null && header != null) {
            map = header;
        }
        if (map != null && header != null) {
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        Map<String, Object> result = new HashMap<>();



        flatMap(result, null, map);
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<>(result.keySet());
        Collections.sort(keys, Collator.getInstance(Locale.ENGLISH));
        for (String key : keys) {
            String textValue;
            Object value = result.get(key);
            if (value != null) {
                textValue = value.toString();
                if (textValue == null || textValue.equals("")) {
                    textValue = "#";
                } else {
                    textValue = textValue.replaceAll("#", "##");
                }
            } else {
                textValue = "#";
            }
            sb.append(textValue).append('#');
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }



    private static String getKey(String rootKey, String myKey) {
        if (rootKey != null) {
            return rootKey + "." + myKey;
        } else {
            return myKey;
        }



    }
    private static void flatMap(Map<String, Object> result, String rootKey,
                                Object input) {
        if (input instanceof Collection) {
            Collection list = (Collection) input;
            int i = 0;
            for (Object e : list) {
                String key = getKey(rootKey, "E" + i++);
                flatMap(result, key, e);
            }
        } else if (input instanceof Map) {
            Map<String, Object> map = (Map) input;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                flatMap(result, getKey(rootKey, entry.getKey()), entry.getValue());
            }
        } else {
            result.put(rootKey, input);
        }
    }



    private static class PacketsWrapper {
        private Collection packets;
        public PacketsWrapper() {
        }
        public PacketsWrapper(Collection packets) {
            this.packets = packets;
        }
        public Collection getPackets() {
            return packets;
        }


        public void setPackets(Collection packets) {
            this.packets = packets;
        }
    }
}
