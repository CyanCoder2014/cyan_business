package com.cyancoder.notification.sender;

import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationSendResult;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.Map;

@Component
public class RestWebhookNotificationSender implements NotificationSender {
    private final RestTemplate restTemplate = new RestTemplate();
    private final boolean enabled;
    public RestWebhookNotificationSender(@Value("${notification.webhook.enabled:false}") boolean enabled){this.enabled=enabled;}
    @Override
    public boolean supports(String channel, String provider) {
        return "WEBHOOK".equalsIgnoreCase(channel) || "REST".equalsIgnoreCase(channel);
    }

    @Override
    public NotificationSendResult send(NotificationDispatchRequest request, String subject, String body) {
        String provider=request.provider()==null||request.provider().isBlank()?"rest-webhook":request.provider();
        if(!enabled)return new NotificationSendResult(false,provider,"","NOT_CONFIGURED","Webhook delivery is not configured");
        try {
            URI target=URI.create(request.recipient());
            if(!"https".equalsIgnoreCase(target.getScheme()))return new NotificationSendResult(false,provider,"","FAILED","Webhook recipient must use HTTPS");
            var response=restTemplate.postForEntity(target,Map.of("messageKey",request.messageKey(),"subject",subject,"body",body,"model",request.model()==null?Map.of():request.model()),String.class);
            boolean ok=response.getStatusCode().is2xxSuccessful();
            return new NotificationSendResult(ok,provider,"",ok?"SENT":"FAILED",ok?"":"Webhook returned "+response.getStatusCode().value());
        } catch(RuntimeException ex){return new NotificationSendResult(false,provider,"","FAILED","Webhook delivery failed");}
    }
}
