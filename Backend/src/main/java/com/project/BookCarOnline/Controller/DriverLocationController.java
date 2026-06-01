package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.Request.DriverLocationRequest;
import com.project.BookCarOnline.Service.DriverLocationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.project.BookCarOnline.DTO.APIResponse;
import org.springframework.http.HttpStatus;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class DriverLocationController {

    SimpMessagingTemplate messagingTemplate;
    DriverLocationService driverLocationService;


    @MessageMapping("/driver/location")
    public void updateDriverLocation(@Payload DriverLocationRequest request) {
        if (request.getBookingId() == null || request.getLat() == null || request.getLng() == null) {
            log.warn("Invalid driver location payload received: {}", request);
            return;
        }

        log.debug("Driver location update for booking {}: lat={}, lng={}",
                request.getBookingId(), request.getLat(), request.getLng());

        // Save location to Redis
        driverLocationService.saveLocation(request.getBookingId(), request.getLat(), request.getLng());

        // Broadcast to the customer who is tracking this specific booking
        messagingTemplate.convertAndSend(
                "/topic/booking/" + request.getBookingId() + "/driver-location",
                request
        );
    }

    @GetMapping("/drivers/location/{bookingId}")
    public APIResponse<DriverLocationService.DriverLocation> getDriverLocation(@PathVariable String bookingId) {
        DriverLocationService.DriverLocation location = driverLocationService.getLocation(bookingId);
        return APIResponse.<DriverLocationService.DriverLocation>builder()
                .status(HttpStatus.OK.value())
                .message("Driver location")
                .result(location) // Will be null if not found
                .build();
    }
}
