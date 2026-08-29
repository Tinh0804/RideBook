package com.project.BookCarOnline.finance.dto;

import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;

public record PaymentSummary(
        String paymentId,
        PaymentMethod paymentMethod,
        Double amount,
        Boolean paid) {
}
