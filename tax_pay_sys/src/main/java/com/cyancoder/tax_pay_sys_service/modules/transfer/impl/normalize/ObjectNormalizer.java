package com.cyancoder.tax_pay_sys_service.modules.transfer.impl.normalize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cyancoder.tax_pay_sys_service.modules.transfer.config.TransferConstants;
import com.cyancoder.tax_pay_sys_service.modules.transfer.interfaces.Normalizer;

import java.text.Collator;
import java.util.*;

public class ObjectNormalizer implements Normalizer {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String normalize(Object data, Map<String, String> headers) {

        if (data == null && headers == null)
            return null;

        Map<String, Object> map = null;

        if (data != null) {
            if (data instanceof String) {
                try {
                    data = mapper.readValue((String) data, Object.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }

            if (data instanceof Collection) {
                PacketsWrapper packetsWrapper = new PacketsWrapper((Collection) data);
                map = mapper.convertValue(packetsWrapper, Map.class);
            } else {
                map = mapper.convertValue(data, Map.class);
            }
        }

        if (map == null) {
            map = new HashMap<>();
        }

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equals(TransferConstants.AUTHORIZATION_HEADER) && entry.getValue() != null && entry.getValue().length() > 7) {
                    map.put(entry.getKey(), entry.getValue().substring(7));
                } else {
                    map.put(entry.getKey(), entry.getValue());
                }
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

    private static void flatMap(Map<String, Object> result, String rootKey, Object input) {
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
