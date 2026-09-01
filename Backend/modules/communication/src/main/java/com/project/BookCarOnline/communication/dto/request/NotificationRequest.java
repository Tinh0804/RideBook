package com.project.BookCarOnline.communication.dto.request;

import com.project.BookCarOnline.communication.entity.NotificationTargetType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {
    String title;
    String message;
    NotificationTargetType targetType;
    String targetUsername; // Dùng khi targetType là SPECIFIC
}
