package com.project.BookCarOnline.communication.mapper;

import com.project.BookCarOnline.communication.dto.response.NotificationResponse;
import com.project.BookCarOnline.communication.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    /** Maps a persisted notification to the API response contract. */
    public NotificationResponse toNotificationResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .sentAt(notification.getSentAt())
                .bookingId(notification.getBookingId())
                .build();
    }
}
