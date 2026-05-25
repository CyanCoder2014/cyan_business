package com.cyancoder.notification.controller;

import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationDispatchResponse;
import com.cyancoder.notification.service.NotificationDispatchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/endpoint/notifications")
public class EndpointNotificationController {
    private final NotificationDispatchService notificationDispatchService;

    public EndpointNotificationController(NotificationDispatchService notificationDispatchService) {
        this.notificationDispatchService = notificationDispatchService;
    }

    @PostMapping("/send")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public NotificationDispatchResponse send(@RequestBody NotificationDispatchRequest request) {
        return notificationDispatchService.dispatch(request);
    }

    @PostMapping("/send-async")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
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
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public DynamicEntityRecordDocument get(@PathVariable String messageKey) {
        return notificationDispatchService.getMessage(messageKey);
    }
}
