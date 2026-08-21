package com.cyancoder.notification.sender;

import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationSendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SmsKavenegarNotificationSender implements NotificationSender {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;
    private final String senderLine;

    public SmsKavenegarNotificationSender(
            @Value("${notification.sms.kavenegar.api-key:}") String apiKey,
            @Value("${notification.sms.kavenegar.sender-line:}") String senderLine
    ) {
        this.apiKey = apiKey;
        this.senderLine = senderLine;
    }

    @Override
    public boolean supports(String channel, String provider) {
        return "SMS".equalsIgnoreCase(channel) && (provider == null || provider.isBlank() || "kavenegar".equalsIgnoreCase(provider) || "default".equalsIgnoreCase(provider));
    }

    @Override
    public NotificationSendResult send(NotificationDispatchRequest request, String subject, String body) {
        if (apiKey == null || apiKey.isBlank()) {
            return new NotificationSendResult(false, "kavenegar", "", "NOT_CONFIGURED", "Kavenegar provider is not configured");
        }
        if (request.recipient() == null || request.recipient().isBlank()) {
            return new NotificationSendResult(false, "kavenegar", "", "FAILED", "Recipient phone number is required");
        }
        if (body == null || body.isBlank()) {
            return new NotificationSendResult(false, "kavenegar", "", "FAILED", "Message body is required");
        }

        try {
            var uri = UriComponentsBuilder
                    .fromUriString("https://api.kavenegar.com/v1/{apiKey}/sms/send.json")
                    .queryParam("receptor", request.recipient())
                    .queryParam("message", body)
                    .queryParamIfPresent("sender", Optional.ofNullable(senderLine).filter(line -> !line.isBlank()))
                    .encode(StandardCharsets.UTF_8)
                    .buildAndExpand(apiKey)
                    .toUri();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            Map<String, Object> returnBlock = asMap(response == null ? null : response.get("return"));
            int status = returnBlock.get("status") instanceof Number number ? number.intValue() : 0;
            if (status != 200) {
                String message = String.valueOf(returnBlock.getOrDefault("message", "Kavenegar rejected the request"));
                return new NotificationSendResult(false, "kavenegar", "", "FAILED", message);
            }

            List<Map<String, Object>> entries = castEntries(response.get("entries"));
            String messageId = entries.isEmpty() ? "" : String.valueOf(entries.get(0).get("messageid"));
            return new NotificationSendResult(true, "kavenegar", messageId, "SENT", "");
        } catch (RestClientException ex) {
            return new NotificationSendResult(false, "kavenegar", "", "FAILED", "Kavenegar delivery failed");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castEntries(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }
}
