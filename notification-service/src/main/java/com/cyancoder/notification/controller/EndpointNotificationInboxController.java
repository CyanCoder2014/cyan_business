package com.cyancoder.notification.controller;

import com.cyancoder.notification.model.NotificationInboxContracts.*;
import com.cyancoder.notification.service.NotificationInboxService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/endpoint/notifications/inbox")
public class EndpointNotificationInboxController {
    private final NotificationInboxService service;
    public EndpointNotificationInboxController(NotificationInboxService service){this.service=service;}
    @GetMapping @PreAuthorize("isAuthenticated()") public InboxPage list(Authentication auth,@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(auth.getName(),tenant,site,page,size);}
    @GetMapping("/unread-count") @PreAuthorize("isAuthenticated()") public UnreadCount unread(Authentication auth,@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site){return service.unread(auth.getName(),tenant,site);}
    @PostMapping @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')") public NotificationItem create(@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site,@Valid @RequestBody CreateNotificationRequest request){return service.create(tenant,site,request);}
    @PatchMapping("/{id}/read") @PreAuthorize("isAuthenticated()") public NotificationItem read(@PathVariable String id,Authentication auth,@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site){return service.markRead(id,auth.getName(),tenant,site);}
    @PostMapping("/read-all") @PreAuthorize("isAuthenticated()") public UnreadCount readAll(Authentication auth,@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site){return service.markAllRead(auth.getName(),tenant,site);}
}
