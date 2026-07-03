package com.project.BookCarOnline.DTO.Redis;

import com.project.BookCarOnline.DTO.Response.NotificationResponse;

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
