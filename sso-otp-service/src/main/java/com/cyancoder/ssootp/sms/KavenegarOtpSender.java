package com.cyancoder.ssootp.sms;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Delivers OTP codes through Kavenegar's Verify Lookup API, which sends a
 * pre-approved template rather than free-text SMS. This is intentionally
 * separate from notification-service's SMS sender: OTP delivery stays on
 * Kavenegar's compliance-approved template even after other notification
 * SMS moves to a different provider.
 */
@Component
public class KavenegarOtpSender {
    private final RestTemplate restTemplate = new RestTemplate();
    private final KavenegarProperties properties;

    public KavenegarOtpSender(KavenegarProperties properties) {
        this.properties = properties;
    }

    public boolean isConfigured() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    public boolean send(String receptor, String code, String purpose, String language) {
        String apiKey = properties.getApiKey();
        String template = properties.resolveTemplate(purpose, language);
        if (!isConfigured() || template.isBlank() || receptor == null || receptor.isBlank()) {
            return false;
        }
        try {
            var uri = UriComponentsBuilder
                    .fromUriString("https://api.kavenegar.com/v1/{apiKey}/verify/lookup.json")
                    .queryParam("receptor", receptor)
                    .queryParam("token", code)
                    .queryParam("template", template)
                    .encode(StandardCharsets.UTF_8)
                    .buildAndExpand(apiKey)
                    .toUri();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            Map<String, Object> returnBlock = asMap(response == null ? null : response.get("return"));
            int status = returnBlock.get("status") instanceof Number number ? number.intValue() : 0;
            return status == 200;
        } catch (RestClientException ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }
}
