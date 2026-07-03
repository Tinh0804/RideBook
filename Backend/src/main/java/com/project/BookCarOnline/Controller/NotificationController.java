package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.DeviceTokenRequest;
import com.project.BookCarOnline.DTO.Response.NotificationResponse;
import com.project.BookCarOnline.Service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @GetMapping
    public APIResponse<List<NotificationResponse>> getMyNotifications() {
        List<NotificationResponse> notifications = notificationService.getMyNotifications();
        return APIResponse.<List<NotificationResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách thông báo")
                .result(notifications)
                .build();
    }

    @PutMapping("/{id}/read")
    public APIResponse<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã nhận thông báo")
                .build();
    }
    
    @PostMapping("/token")
    public APIResponse<Void> registerDeviceToken(@RequestBody DeviceTokenRequest request) {
        notificationService.registerDeviceToken(request.getFcmToken(), request.getDeviceType());
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đăng ký token thiết bị thành công")
                .build();
    }
}
