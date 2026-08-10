package com.cyancoder.notification.service;

import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationDispatchResponse;
import com.cyancoder.notification.model.NotificationSendResult;
import com.cyancoder.notification.model.QueuedNotificationMessage;
import com.cyancoder.notification.sender.NotificationSender;
import com.cyancoder.notification.sender.NotificationSenderRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class NotificationDispatchService {
    private final DynamicRuntimeService dynamicRuntimeService;
    private final NotificationSenderRegistry senderRegistry;
    private final KafkaTemplate<String, QueuedNotificationMessage> kafkaTemplate;

    public NotificationDispatchService(DynamicRuntimeService dynamicRuntimeService,
                                       NotificationSenderRegistry senderRegistry,
                                       KafkaTemplate<String, QueuedNotificationMessage> kafkaTemplate) {
        this.dynamicRuntimeService = dynamicRuntimeService;
        this.senderRegistry = senderRegistry;
        this.kafkaTemplate = kafkaTemplate;
    }

    public NotificationDispatchResponse dispatch(NotificationDispatchRequest request) {
        return dispatch(request, new DynamicScope(null, null));
    }

    public NotificationDispatchResponse dispatch(NotificationDispatchRequest request, DynamicScope scope) {
        ensureDefinition("notification-template", scope);
        ensureDefinition("notification-message", scope);
        Map<String, Object> template = resolveTemplate(request.templateKey(), scope);
        String messageKey = request.messageKey() == null || request.messageKey().isBlank()
                ? "msg-" + UUID.randomUUID()
                : request.messageKey();
        String channel = firstNonBlank(request.channel(), string(template, "channel"), "EMAIL");
        String provider = normalizeProvider(firstNonBlank(request.provider(), string(template, "provider"), defaultProvider(channel)));
        String dispatchMode = firstNonBlank(request.dispatchMode(), "SYNC");
        String subject = renderTemplate(firstNonBlank(request.subject(), string(template, "subjectTemplate")), request.model());
        String body = renderTemplate(firstNonBlank(request.body(), string(template, "bodyTemplate")), request.model());

        Map<String, Object> data = baseMessageData(request, messageKey, channel, provider, subject, body);
        if ("ASYNC".equalsIgnoreCase(dispatchMode) || "QUEUE".equalsIgnoreCase(dispatchMode)) {
            data.put("status", "QUEUED");
            dynamicRuntimeService.submitMap("notification-message", messageKey, data, true, scope);
            kafkaTemplate.send("notification-dispatch", messageKey, new QueuedNotificationMessage(
                    messageKey, channel, request.templateKey(), request.recipient(), subject, body, provider,
                    request.model() == null ? Map.of() : request.model(),
                    request.relatedRef() == null ? Map.of() : request.relatedRef()
            ));
            return new NotificationDispatchResponse(messageKey, "QUEUED", channel, request.recipient());
        }

        NotificationSendResult result = sendNow(new NotificationDispatchRequest(
                messageKey,
                channel,
                request.templateKey(),
                provider,
                "SYNC",
                request.recipient(),
                subject,
                body,
                request.model(),
                request.relatedRef()
        ));
        applyResult(data, result);
        dynamicRuntimeService.submitMap("notification-message", messageKey, data, true, scope);
        return new NotificationDispatchResponse(messageKey, data.get("status").toString(), channel, request.recipient());
    }

    public void processQueued(QueuedNotificationMessage message) {
        Map<String, Object> data = baseMessageData(
                new NotificationDispatchRequest(
                        message.messageKey(),
                        message.channel(),
                        message.templateKey(),
                        message.provider(),
                        "ASYNC",
                        message.recipient(),
                        message.subject(),
                        message.body(),
                        message.model(),
                        message.relatedRef()
                ),
                message.messageKey(),
                message.channel(),
                message.provider(),
                message.subject(),
                message.body()
        );
        NotificationSendResult result = sendNow(new NotificationDispatchRequest(
                message.messageKey(),
                message.channel(),
                message.templateKey(),
                message.provider(),
                "ASYNC",
                message.recipient(),
                message.subject(),
                message.body(),
                message.model(),
                message.relatedRef()
        ));
        applyResult(data, result);
        dynamicRuntimeService.submitMap("notification-message", message.messageKey(), data, true);
    }

    public DynamicEntityRecordDocument getMessage(String messageKey) {
        ensureDefinition("notification-message");
        return dynamicRuntimeService.getRecord("notification-message", messageKey);
    }

    public DynamicEntityRecordDocument getMessage(String messageKey, DynamicScope scope) {
        ensureDefinition("notification-message", scope);
        return dynamicRuntimeService.getRecord("notification-message", messageKey, scope);
    }

    public java.util.List<DynamicEntityRecordDocument> listMessages(DynamicScope scope) {
        ensureDefinition("notification-message", scope);
        return dynamicRuntimeService.listRecords("notification-message", scope);
    }

    public Map<String,Object> preview(NotificationDispatchRequest request, DynamicScope scope) {
        ensureDefinition("notification-template", scope);
        Map<String,Object> template=resolveTemplate(request.templateKey(),scope);
        String channel=firstNonBlank(request.channel(),string(template,"channel"),"EMAIL");
        String provider=normalizeProvider(firstNonBlank(request.provider(),string(template,"provider"),defaultProvider(channel)));
        return Map.of("channel",channel,"provider",provider,"subject",renderTemplate(firstNonBlank(request.subject(),string(template,"subjectTemplate")),request.model()),"body",renderTemplate(firstNonBlank(request.body(),string(template,"bodyTemplate")),request.model()));
    }

    public NotificationDispatchResponse retry(String messageKey, DynamicScope scope) {
        DynamicEntityRecordDocument record=getMessage(messageKey,scope); Map<String,Object> d=record.getData();
        String status=Objects.toString(d.get("status"),"");
        if(!java.util.Set.of("FAILED","NOT_CONFIGURED").contains(status.toUpperCase()))throw new IllegalArgumentException("Only failed or not-configured messages can be retried");
        return dispatch(new NotificationDispatchRequest(messageKey,Objects.toString(d.get("channel"),""),Objects.toString(d.get("templateKey"),""),Objects.toString(d.get("provider"),""),"SYNC",Objects.toString(d.get("recipient"),""),Objects.toString(d.get("subject"),""),Objects.toString(d.get("body"),""),Map.of(),d.get("relatedRef") instanceof Map<?,?> m?(Map<String,Object>)m:Map.of()),scope);
    }

    public java.util.List<Map<String,Object>> providers() {
        return java.util.List.of(
                Map.of("channel","EMAIL","provider","smtp","status","NOT_CONFIGURED"),
                Map.of("channel","SMS","provider","kavenegar","status","NOT_CONFIGURED"),
                Map.of("channel","PUSH","provider","push-default","status","NOT_CONFIGURED"),
                Map.of("channel","MQTT","provider","mqtt-default","status","NOT_CONFIGURED"),
                Map.of("channel","WEBHOOK","provider","rest-webhook","status","CONFIGURATION_DEPENDENT")
        );
    }

    private Map<String, Object> baseMessageData(NotificationDispatchRequest request,
                                                String messageKey,
                                                String channel,
                                                String provider,
                                                String subject,
                                                String body) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("messageKey", messageKey);
        data.put("channel", channel);
        data.put("templateKey", request.templateKey());
        data.put("recipient", request.recipient());
        data.put("subject", subject);
        data.put("body", body);
        data.put("provider", provider);
        data.put("providerMessageId", "");
        data.put("status", "QUEUED");
        data.put("sentAt", "");
        data.put("errorMessage", "");
        data.put("relatedRef", request.relatedRef() == null ? Map.of() : request.relatedRef());
        data.put("payload", Map.of(
                "summary", body.length() > 180 ? body.substring(0, 180) : body,
                "eventCode", request.templateKey() == null ? "manual" : request.templateKey()
        ));
        return data;
    }

    private void ensureDefinition(String entityKey) {
        try {
            dynamicRuntimeService.getDefinition(entityKey);
        } catch (Exception ex) {
            dynamicRuntimeService.createFromTemplate(entityKey, entityKey);
        }
    }

    private void ensureDefinition(String entityKey, DynamicScope scope) {
        try { dynamicRuntimeService.getDefinition(entityKey, scope); }
        catch (Exception ex) { dynamicRuntimeService.createFromTemplate(entityKey, entityKey, scope); }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveTemplate(String templateKey) {
        if (templateKey == null || templateKey.isBlank()) {
            return Map.of();
        }
        try {
            DynamicEntityRecordDocument record = dynamicRuntimeService.getRecord("notification-template", templateKey);
            return record.getData() == null ? Map.of() : record.getData();
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private Map<String,Object> resolveTemplate(String templateKey, DynamicScope scope) {
        if(templateKey==null||templateKey.isBlank())return Map.of();
        try{DynamicEntityRecordDocument record=dynamicRuntimeService.getRecord("notification-template",templateKey,scope);return record.getData()==null?Map.of():record.getData();}catch(Exception ex){return Map.of();}
    }

    private NotificationSendResult sendNow(NotificationDispatchRequest request) {
        NotificationSender sender = senderRegistry.resolve(request.channel(), request.provider());
        return sender.send(request, request.subject(), request.body());
    }

    private void applyResult(Map<String, Object> data, NotificationSendResult result) {
        data.put("provider", result.provider());
        data.put("providerMessageId", firstNonBlank(result.providerMessageId(), ""));
        data.put("status", result.status());
        data.put("sentAt", result.successful() ? Instant.now().toString() : "");
        data.put("errorMessage", firstNonBlank(result.errorMessage(), ""));
    }

    private String renderTemplate(String template, Map<String, Object> model) {
        String rendered = template == null ? "" : template;
        if (model == null) {
            return rendered;
        }
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", Objects.toString(entry.getValue(), ""));
        }
        return rendered;
    }

    private String string(Map<String, Object> map, String key) {
        return map.get(key) == null ? null : String.valueOf(map.get(key));
    }

    private String defaultProvider(String channel) {
        if ("SMS".equalsIgnoreCase(channel)) {
            return "kavenegar";
        }
        if ("MQTT".equalsIgnoreCase(channel)) {
            return "mqtt-default";
        }
        if ("PUSH".equalsIgnoreCase(channel)) {
            return "push-default";
        }
        if ("WEBHOOK".equalsIgnoreCase(channel) || "REST".equalsIgnoreCase(channel)) {
            return "rest-webhook";
        }
        return "smtp";
    }

    private String normalizeProvider(String provider) {
        int statusSeparator = provider.indexOf(" · ");
        return statusSeparator < 0 ? provider : provider.substring(0, statusSeparator).trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
