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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;
    AccountRepository accountRepository;
    NotificationMapper notificationMapper;
    SimpMessagingTemplate messagingTemplate;

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

        // Real-time broadcast
        messagingTemplate.convertAndSend("/topic/notifications/" + username, response);
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
}
