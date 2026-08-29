package com.project.BookCarOnline.booking.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.finance.dto.response.PaymentStatusResponse;
import com.project.BookCarOnline.finance.entity.enums.PaymentStatus;
import com.project.BookCarOnline.booking.service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentStatusController {
    BookingService bookingService;

    @GetMapping("/status/{bookingId}")
    public APIResponse<PaymentStatusResponse> getPaymentStatus(@PathVariable String bookingId) {
        log.info("REST API: GET /payments/status/{} - Checking payment status", bookingId);
        PaymentStatus status = bookingService.getPaymentStatus(bookingId);
        if (PaymentStatus.SUCCESS.equals(status)) {
            bookingService.dispatchAfterPayment(bookingId);
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
