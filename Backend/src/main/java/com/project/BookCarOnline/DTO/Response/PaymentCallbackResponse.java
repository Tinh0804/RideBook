package com.project.BookCarOnline.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCallbackResponse {

    String bookingId;
    String orderId;
    String transactionId;
    Long amount;
    String paymentStatus; // "SUCCESS", "FAILED", "PENDING"
    String paymentMethod;
    String message;
    String paymentTime;
}
