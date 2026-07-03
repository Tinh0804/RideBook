package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Response.NotificationResponse;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Notification;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.NotificationMapper;
import com.project.BookCarOnline.Repository.AccountRepository;
import com.project.BookCarOnline.Repository.NotificationRepository;
import com.project.BookCarOnline.Utils.SecurityUtils;
import com.project.BookCarOnline.DTO.Redis.WebSocketNotificationMessage;
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
public class NotificationService {
    NotificationRepository notificationRepository;
    AccountRepository accountRepository;
    NotificationMapper notificationMapper;
    SimpMessagingTemplate messagingTemplate;
    FirebaseService firebaseService;
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    public void sendNotification(String username, String title, String message, Booking booking) {
        Account account = accountRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        Notification notification = Notification.builder()
                .accountNo(account)
                .title(title)
                .message(message)
                .bookingNo(booking)
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
        firebaseService.sendNotificationToToken(account.getFcmToken(), title, message);
    }

    @Transactional
    public void registerDeviceToken(String fcmToken, String deviceType) {
        String accountId = SecurityUtils.getCurrentAccountId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED));
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        account.setFcmToken(fcmToken);
        accountRepository.save(account);
    }

    public List<NotificationResponse> getMyNotifications() {
        String accountId = SecurityUtils.getCurrentAccountId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED));

        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        
        List<Notification> notifications = notificationRepository.findByAccountNo_UserNameOrderBySentAtDesc(account.getUserName());
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
