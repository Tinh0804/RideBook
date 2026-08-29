package com.project.BookCarOnline.communication.dto.redis;

import com.project.BookCarOnline.communication.dto.response.NotificationResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebSocketNotificationMessage {
    String username;
    NotificationResponse notification;
}
