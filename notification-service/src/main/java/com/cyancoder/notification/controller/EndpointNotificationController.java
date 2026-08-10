package com.cyancoder.notification.controller;

import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationDispatchResponse;
import com.cyancoder.notification.service.NotificationDispatchService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import java.util.List;
import java.util.Map;
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
    public NotificationDispatchResponse send(@RequestHeader(value="X-Tenant-Key",required=false) String tenant,@RequestHeader(value="X-Site-Key",required=false) String site,@RequestBody NotificationDispatchRequest request) {
        return notificationDispatchService.dispatch(request,new DynamicScope(tenant,site));
    }

    @PostMapping("/send-async")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public NotificationDispatchResponse sendAsync(@RequestHeader(value="X-Tenant-Key",required=false) String tenant,@RequestHeader(value="X-Site-Key",required=false) String site,@RequestBody NotificationDispatchRequest request) {
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
        ),new DynamicScope(tenant,site));
    }

    @GetMapping("/messages/{messageKey}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public DynamicEntityRecordDocument get(@PathVariable String messageKey) {
        return notificationDispatchService.getMessage(messageKey);
    }

    @GetMapping("/messages") @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public List<DynamicEntityRecordDocument> list(@RequestHeader(value="X-Tenant-Key",required=false)String tenant,@RequestHeader(value="X-Site-Key",required=false)String site){return notificationDispatchService.listMessages(new DynamicScope(tenant,site));}
    @PostMapping("/preview") @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public Map<String,Object> preview(@RequestHeader(value="X-Tenant-Key",required=false)String tenant,@RequestHeader(value="X-Site-Key",required=false)String site,@RequestBody NotificationDispatchRequest request){return notificationDispatchService.preview(request,new DynamicScope(tenant,site));}
    @PostMapping("/messages/{messageKey}/retry") @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public NotificationDispatchResponse retry(@PathVariable String messageKey,@RequestHeader(value="X-Tenant-Key",required=false)String tenant,@RequestHeader(value="X-Site-Key",required=false)String site){return notificationDispatchService.retry(messageKey,new DynamicScope(tenant,site));}
    @GetMapping("/providers") @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')") public List<Map<String,Object>> providers(){return notificationDispatchService.providers();}
}
