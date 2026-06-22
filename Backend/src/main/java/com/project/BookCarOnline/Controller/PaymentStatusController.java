package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Response.PaymentStatusResponse;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.PaymentStatus;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Service.RideDispatcherService;
import com.project.BookCarOnline.Entity.Driver;
import com.project.BookCarOnline.Service.GoogleMapService;
import com.google.maps.model.GeocodingResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentStatusController {
    RideBookRepository bookingRepository;
    RideDispatcherService dispatcherService;
    DriverRepository driverRepository;
    GoogleMapService googleMapService;

    @GetMapping("/status/{bookingId}")
    public APIResponse<PaymentStatusResponse> getPaymentStatus(@PathVariable String bookingId) {
        log.info("REST API: GET /payments/status/{} - Checking payment status", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        PaymentStatus status = PaymentStatus.PENDING;
        if (booking == null) {
            status = PaymentStatus.FAILED;
        } else {
            if (booking.getPaymentNo() != null && Boolean.TRUE.equals(booking.getPaymentNo().getPaymentStatus())) {
                status = PaymentStatus.SUCCESS;
            }
        }
        if (PaymentStatus.SUCCESS.equals(status)) {
            // Dispatch after payment success
            if (booking.getDriverNo() == null) {
                GeocodingResult geo = googleMapService.geocode(booking.getPickupLocation());
                List<Driver> candidates = driverRepository.findTrulyAvailableDriversNearby(
                        geo.geometry.location.lat,
                        geo.geometry.location.lng,
                        5.0,
                        booking.getVehicleTypeNo().getVehicleTypeId()
                );
                dispatcherService.startDispatching(bookingId, candidates);
            }
        }

        PaymentStatusResponse response = PaymentStatusResponse.builder()
                .bookingId(bookingId)
                .paymentStatus(status.name())
                .build();
        return APIResponse.<PaymentStatusResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Trạng thái thanh toán")
                .result(response)
                .build();
    }
}
