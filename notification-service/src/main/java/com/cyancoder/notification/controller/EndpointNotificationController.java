package com.cyancoder.notification.controller;

import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationDispatchResponse;
import com.cyancoder.notification.service.NotificationDispatchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/endpoint/notifications")
public class EndpointNotificationController {
    private final NotificationDispatchService notificationDispatchService;

    public EndpointNotificationController(NotificationDispatchService notificationDispatchService) {
        this.notificationDispatchService = notificationDispatchService;
    }

    @PostMapping("/send")
    public NotificationDispatchResponse send(@RequestBody NotificationDispatchRequest request) {
        return notificationDispatchService.dispatch(request);
    }

    @PostMapping("/send-async")
    public NotificationDispatchResponse sendAsync(@RequestBody NotificationDispatchRequest request) {
        return notificationDispatchService.dispatch(new NotificationDispatchRequest(
                request.messageKey(),
                request.channel(),
                request.templateKey(),
                request.provider(),
                "ASYNC",
                request.recipient(),
                request.subject(),
                request.body(),
                request.model(),
                request.relatedRef()
        ));
    }

    @GetMapping("/messages/{messageKey}")
    public DynamicEntityRecordDocument get(@PathVariable String messageKey) {
        return notificationDispatchService.getMessage(messageKey);
    }
}
