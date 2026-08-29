package com.project.BookCarOnline.finance.service.payment;

import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;

public interface PaymentCallbackHandler {

    void process(
            String orderId,
            long amount,
            boolean successful,
            String resultCode,
            String providerTransactionId,
            PaymentMethod paymentMethod);
}
