package com.cyancoder.notification.service;

import com.cyancoder.notification.model.NotificationInboxContracts.CreateNotificationRequest;
import com.cyancoder.notification.model.UserNotificationEntity;
import com.cyancoder.notification.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationInboxServiceTest {
    @Test void creationIsIdempotentAndRejectsExternalDeepLinks() {
        UserNotificationRepository repository=mock(UserNotificationRepository.class);
        when(repository.findById("event-1")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation->invocation.getArgument(0));
        var service=new NotificationInboxService(repository);
        var item=service.create("tenant","site",new CreateNotificationRequest("event-1","user","Ready",null,"PROJECT","INFO","https://unsafe.example","ai-orchestrator-service","draft-1"));
        assertThat(item.deepLink()).isNull();
        verify(repository).save(any(UserNotificationEntity.class));
    }
}
