package com.project.BookCarOnline.communication.service;

import com.project.BookCarOnline.identity.service.FirebaseService;
import com.project.BookCarOnline.communication.dto.response.NotificationResponse;
import com.project.BookCarOnline.identity.dto.summary.AccountSummary;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.communication.entity.Notification;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.communication.mapper.NotificationMapper;
import com.project.BookCarOnline.communication.repository.NotificationRepository;
import com.project.BookCarOnline.communication.dto.request.NotificationRequest;
import com.project.BookCarOnline.communication.entity.NotificationTargetType;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import com.project.BookCarOnline.communication.dto.redis.WebSocketNotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
/** Coordinates persisted, WebSocket, Redis, and push notifications. */
public class NotificationService {
    NotificationRepository notificationRepository;
    IdentityQueryService identityQueryService;
    NotificationMapper notificationMapper;
    SimpMessagingTemplate messagingTemplate;
    FirebaseService firebaseService;
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    public void sendAdminNotification(NotificationRequest request) {
        List<AccountSummary> targetAccounts;
        switch (request.getTargetType()) {
            case ALL:
                targetAccounts = identityQueryService.getAccounts();
                break;
            case DRIVER:
                targetAccounts = identityQueryService.getAccountsByRole(PredefinedRole.RoleName.DRIVER);
                break;
            case CUSTOMER:
                targetAccounts = identityQueryService.getAccountsByRole(PredefinedRole.RoleName.CUSTOMER);
                break;
            case SPECIFIC:
                if (request.getTargetUsername() == null || request.getTargetUsername().trim().isEmpty()) {
                    throw new AppException(ErrorCode.USER_NOT_EXITED);
                }
                AccountSummary account = identityQueryService.getAccountByUsername(request.getTargetUsername());
                targetAccounts = List.of(account);
                break;
            default:
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        for (AccountSummary account : targetAccounts) {
            Notification notification = Notification.builder()
                    .accountId(account.accountId())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .sentAt(new Date())
                    .isRead(false)
                    .build();

            Notification saved = notificationRepository.save(notification);
            NotificationResponse response = notificationMapper.toNotificationResponse(saved);

            // Real-time broadcast via Redis PubSub
            try {
                WebSocketNotificationMessage redisMsg = new WebSocketNotificationMessage(account.userName(), response);
                String jsonMessage = objectMapper.writeValueAsString(redisMsg);
                redisTemplate.convertAndSend("websocket_notifications", jsonMessage);
            } catch (Exception e) {
                log.error("Failed to publish admin notification to Redis for user {}", account.userName(), e);
            }

            // Firebase Cloud Messaging (Push Notification)
            firebaseService.sendNotificationToToken(account.fcmToken(), request.getTitle(), request.getMessage());
        }
    }

    public void sendNotification(String username, String title, String message, String bookingId) {
        AccountSummary account = identityQueryService.getAccountByUsername(username);

        Notification notification = Notification.builder()
                .accountId(account.accountId())
                .title(title)
                .message(message)
                .bookingId(bookingId)
                .sentAt(new Date())
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toNotificationResponse(saved);

        // Real-time broadcast via Redis PubSub
        try {
            WebSocketNotificationMessage redisMsg = new WebSocketNotificationMessage(username, response);
            String jsonMessage = objectMapper.writeValueAsString(redisMsg);
            redisTemplate.convertAndSend("websocket_notifications", jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish notification to Redis", e);
        }

        // Firebase Cloud Messaging (Push Notification)
        firebaseService.sendNotificationToToken(account.fcmToken(), title, message);
    }

    @Transactional
    public void registerDeviceToken(String fcmToken, String deviceType) {
        String accountId = SecurityUtils.getCurrentAccountId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED));
        identityQueryService.updateFcmToken(accountId, fcmToken);
    }

    public List<NotificationResponse> getMyNotifications() {
        String accountId = SecurityUtils.getCurrentAccountId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED));

        identityQueryService.getAccount(accountId);
        List<Notification> notifications = notificationRepository.findByAccountIdOrderBySentAtDesc(accountId);
        return notifications.stream().map(notificationMapper::toNotificationResponse).collect(Collectors.toList());
    }

    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void handleMessage(String message) {
        try {
            WebSocketNotificationMessage notificationMsg = objectMapper.readValue(message, WebSocketNotificationMessage.class);
            log.info("Received notification from Redis PubSub for user: {}", notificationMsg.getUsername());
            
            // Push it to the connected WebSocket client on this node
            messagingTemplate.convertAndSend("/topic/notifications/" + notificationMsg.getUsername(), notificationMsg.getNotification());
            
        } catch (Exception e) {
            log.error("Error processing Redis notification message", e);
        }
    }
}
