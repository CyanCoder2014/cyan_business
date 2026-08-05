package com.cyancoder.notification.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public final class NotificationInboxContracts {
    private NotificationInboxContracts() {}
    public record CreateNotificationRequest(@NotBlank String notificationId, @NotBlank String recipient,
            @NotBlank String title, String body, String type, String severity, String deepLink,
            String sourceService, String sourceKey) {}
    public record NotificationItem(String notificationId, String tenantKey, String siteKey, String type,
            String severity, String title, String body, String deepLink, String sourceService,
            String sourceKey, Instant createdAt, Instant readAt, long version) {}
    public record InboxPage(List<NotificationItem> content, int page, int size, long totalElements, int totalPages) {}
    public record UnreadCount(long unreadCount, Instant updatedAt) {}
}
