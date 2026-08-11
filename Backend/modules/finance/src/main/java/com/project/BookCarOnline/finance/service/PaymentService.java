package com.project.BookCarOnline.finance.service;

import com.project.BookCarOnline.finance.dto.PaymentSummary;
import com.project.BookCarOnline.finance.entity.Payment;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.repository.PaymentRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentSummary create(PaymentMethod method, double amount, boolean paid) {
        return toSummary(paymentRepository.save(Payment.builder()
                .paymentType(method)
                .amount(amount)
                .paymentStatus(paid)
                .build()));
    }

    public PaymentSummary get(String paymentId) {
        return toSummary(getEntity(paymentId));
    }

    @Transactional
    public PaymentSummary markPaid(String paymentId) {
        Payment payment = getEntity(paymentId);
        payment.setPaymentStatus(true);
        return toSummary(paymentRepository.save(payment));
    }

    private Payment getEntity(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private PaymentSummary toSummary(Payment payment) {
        return new PaymentSummary(
                payment.getPaymentId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getPaymentStatus());
    }
}
