package com.project.BookCarOnline.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {

    String status;
    String message;
    String paymentUrl;
    String orderId;
    String transactionId;
    Long amount;
    String paymentMethod; // "VNPAY" or "MOMO"
}
