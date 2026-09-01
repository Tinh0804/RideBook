package com.project.BookCarOnline.communication.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.identity.dto.request.DeviceTokenRequest;
import com.project.BookCarOnline.communication.dto.request.NotificationRequest;
import com.project.BookCarOnline.communication.dto.response.NotificationResponse;
import com.project.BookCarOnline.communication.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
/** REST boundary for user and administrator notifications. */
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

    @PostMapping("/admin/send")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Void> sendAdminNotification(@RequestBody NotificationRequest request) {
        notificationService.sendAdminNotification(request);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Gửi thông báo thành công")
                .build();
    }
}
