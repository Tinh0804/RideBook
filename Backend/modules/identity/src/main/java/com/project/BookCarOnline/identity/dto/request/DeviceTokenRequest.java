package com.project.BookCarOnline.identity.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceTokenRequest {
    String fcmToken;
    String deviceType;
}
