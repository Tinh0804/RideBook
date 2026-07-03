package com.project.BookCarOnline.DTO.Request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceTokenRequest {
    String fcmToken;
    String deviceType;
}
