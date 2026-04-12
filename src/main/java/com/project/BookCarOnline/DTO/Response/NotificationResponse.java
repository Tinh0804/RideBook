package com.project.BookCarOnline.DTO.Response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String notificationId;
    String title;
    String message;
    boolean isRead;
    Date sentAt;
    String bookingId;
}
