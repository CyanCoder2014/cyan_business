package com.cyancoder.notification.repository;

import com.cyancoder.notification.model.UserNotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, String> {
    Page<UserNotificationEntity> findByRecipientAndTenantKeyAndSiteKeyOrderByCreatedAtDesc(String recipient, String tenantKey, String siteKey, Pageable pageable);
    long countByRecipientAndTenantKeyAndSiteKeyAndReadAtIsNull(String recipient, String tenantKey, String siteKey);
    List<UserNotificationEntity> findByRecipientAndTenantKeyAndSiteKeyAndReadAtIsNull(String recipient, String tenantKey, String siteKey);
}
