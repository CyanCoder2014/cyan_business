package com.cyancoder.notification.service;

import com.cyancoder.notification.model.NotificationInboxContracts.*;
import com.cyancoder.notification.model.UserNotificationEntity;
import com.cyancoder.notification.repository.UserNotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class NotificationInboxService {
    private final UserNotificationRepository repository;
    public NotificationInboxService(UserNotificationRepository repository) { this.repository = repository; }

    public InboxPage list(String user, String tenant, String site, int page, int size) {
        var result = repository.findByRecipientAndTenantKeyAndSiteKeyOrderByCreatedAtDesc(user, tenant, site,
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
        return new InboxPage(result.getContent().stream().map(this::item).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
    public UnreadCount unread(String user, String tenant, String site) {
        return new UnreadCount(repository.countByRecipientAndTenantKeyAndSiteKeyAndReadAtIsNull(user, tenant, site), Instant.now());
    }
    @Transactional public NotificationItem create(String tenant, String site, CreateNotificationRequest request) {
        UserNotificationEntity value = repository.findById(request.notificationId()).orElseGet(UserNotificationEntity::new);
        if (value.getNotificationId() != null) return item(value);
        value.setNotificationId(request.notificationId()); value.setTenantKey(tenant); value.setSiteKey(site);
        value.setRecipient(request.recipient()); value.setTitle(request.title()); value.setBody(request.body());
        value.setType(request.type() == null ? "GENERAL" : request.type()); value.setSeverity(request.severity() == null ? "INFO" : request.severity());
        value.setDeepLink(validLink(request.deepLink())); value.setSourceService(request.sourceService()); value.setSourceKey(request.sourceKey());
        value.setCreatedAt(Instant.now()); value.setVersion(1); return item(repository.save(value));
    }
    @Transactional public NotificationItem markRead(String id, String user, String tenant, String site) {
        UserNotificationEntity value = repository.findById(id).filter(v -> user.equals(v.getRecipient()) && eq(tenant,v.getTenantKey()) && eq(site,v.getSiteKey())).orElseThrow(NoSuchElementException::new);
        if (value.getReadAt() == null) { value.setReadAt(Instant.now()); value.setVersion(value.getVersion()+1); repository.save(value); }
        return item(value);
    }
    @Transactional public UnreadCount markAllRead(String user, String tenant, String site) {
        var values = repository.findByRecipientAndTenantKeyAndSiteKeyAndReadAtIsNull(user, tenant, site); Instant now = Instant.now();
        values.forEach(v -> { v.setReadAt(now); v.setVersion(v.getVersion()+1); }); repository.saveAll(values); return unread(user,tenant,site);
    }
    private String validLink(String link) { return link != null && link.startsWith("/") && !link.startsWith("//") ? link : null; }
    private boolean eq(String a,String b){return a==null?b==null:a.equals(b);}
    private NotificationItem item(UserNotificationEntity v){return new NotificationItem(v.getNotificationId(),v.getTenantKey(),v.getSiteKey(),v.getType(),v.getSeverity(),v.getTitle(),v.getBody(),v.getDeepLink(),v.getSourceService(),v.getSourceKey(),v.getCreatedAt(),v.getReadAt(),v.getVersion());}
}
