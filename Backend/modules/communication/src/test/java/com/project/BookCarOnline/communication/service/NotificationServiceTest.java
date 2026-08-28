package com.project.BookCarOnline.communication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.BookCarOnline.communication.dto.request.NotificationRequest;
import com.project.BookCarOnline.communication.dto.response.NotificationResponse;
import com.project.BookCarOnline.communication.entity.Notification;
import com.project.BookCarOnline.communication.entity.NotificationTargetType;
import com.project.BookCarOnline.communication.mapper.NotificationMapper;
import com.project.BookCarOnline.communication.repository.NotificationRepository;
import com.project.BookCarOnline.identity.dto.summary.AccountSummary;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.service.FirebaseService;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    IdentityQueryService identityQueryService;

    @Mock
    NotificationMapper notificationMapper;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Mock
    FirebaseService firebaseService;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    NotificationService notificationService;

    AccountSummary sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = new AccountSummary(
                "acc-1",
                "user@example.com",
                "CUSTOMER",
                "fcm-token-123",
                true
        );
    }

    @Test
    void sendNotification_Success_SavesNotificationAndSendsPushNotification() throws Exception {
        when(identityQueryService.getAccountByUsername("user@example.com")).thenReturn(sampleAccount);

        Notification savedNotification = Notification.builder()
                .notificationId("notif-1")
                .accountId("acc-1")
                .title("Trip confirmed")
                .message("Driver is on the way")
                .bookingId("bk-101")
                .sentAt(new Date())
                .isRead(false)
                .build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        NotificationResponse response = NotificationResponse.builder()
                .notificationId("notif-1")
                .title("Trip confirmed")
                .message("Driver is on the way")
                .bookingId("bk-101")
                .build();
        when(notificationMapper.toNotificationResponse(savedNotification)).thenReturn(response);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"mock\":\"json\"}");

        notificationService.sendNotification("user@example.com", "Trip confirmed", "Driver is on the way", "bk-101");

        ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notifCaptor.capture());

        Notification captured = notifCaptor.getValue();
        assertEquals("acc-1", captured.getAccountId());
        assertEquals("Trip confirmed", captured.getTitle());
        assertEquals("Driver is on the way", captured.getMessage());
        assertEquals("bk-101", captured.getBookingId());
        assertFalse(captured.isRead());

        verify(redisTemplate).convertAndSend(eq("websocket_notifications"), eq("{\"mock\":\"json\"}"));
        verify(firebaseService).sendNotificationToToken("fcm-token-123", "Trip confirmed", "Driver is on the way");
    }

    @Test
    void sendAdminNotification_TargetAll_SendsToAllAccounts() {
        when(identityQueryService.getAccounts()).thenReturn(List.of(sampleAccount));

        Notification savedNotification = Notification.builder()
                .notificationId("notif-admin")
                .accountId("acc-1")
                .title("Maintenance")
                .message("System update tonight")
                .sentAt(new Date())
                .build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        NotificationRequest request = NotificationRequest.builder()
                .title("Maintenance")
                .message("System update tonight")
                .targetType(NotificationTargetType.ALL)
                .build();

        notificationService.sendAdminNotification(request);

        verify(identityQueryService).getAccounts();
        verify(notificationRepository).save(any(Notification.class));
        verify(firebaseService).sendNotificationToToken("fcm-token-123", "Maintenance", "System update tonight");
    }

    @Test
    void sendAdminNotification_SpecificUser_EmptyUsername_ThrowsAppException() {
        NotificationRequest request = NotificationRequest.builder()
                .title("Maintenance")
                .message("System update tonight")
                .targetType(NotificationTargetType.SPECIFIC)
                .targetUsername("   ")
                .build();

        AppException exception = assertThrows(
                AppException.class,
                () -> notificationService.sendAdminNotification(request));

        assertEquals(ErrorCode.USER_NOT_EXITED, exception.getErrorCode());
    }

    @Test
    void markAsRead_Found_SetsReadToTrueAndSaves() {
        Notification notification = Notification.builder()
                .notificationId("notif-1")
                .isRead(false)
                .build();
        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(notification));

        notificationService.markAsRead("notif-1");

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_NotFound_ThrowsAppException() {
        when(notificationRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> notificationService.markAsRead("non-existent"));

        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());
    }
}
