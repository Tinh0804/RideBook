package com.project.BookCarOnline.app.service.payment;

import com.project.BookCarOnline.booking.service.BookingService;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.service.WalletService;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackService {

    private static final String TOP_UP_PREFIX = "TOPUP_";

    private final BookingService bookingService;
    private final WalletService walletService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void process(
            String orderId,
            long amount,
            boolean successful,
            String resultCode,
            String providerTransactionId,
            PaymentMethod paymentMethod) {
        if (orderId == null || orderId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        if (orderId.startsWith(TOP_UP_PREFIX)) {
            processTopUp(orderId, amount, successful, resultCode, providerTransactionId, paymentMethod);
            return;
        }

        String bookingId = orderId.split("_", 2)[0];
        if (successful) {
            bookingService.confirmOnlinePayment(bookingId);
        } else {
            bookingService.notifyPaymentFailed(bookingId);
        }
    }

    private void processTopUp(
            String orderId,
            long amount,
            boolean successful,
            String resultCode,
            String providerTransactionId,
            PaymentMethod paymentMethod) {
        String[] reference = orderId.split("_", 3);
        if (reference.length != 3 || reference[1].isBlank() || reference[2].isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        String driverId = reference[1];
        String walletTransactionId = reference[2];
        boolean processed = walletService.processPaymentCallback(
                walletTransactionId, successful, providerTransactionId);
        if (!processed) {
            log.error("Không tìm thấy giao dịch nạp tiền {} từ {}", walletTransactionId, paymentMethod);
            return;
        }

        String notification = successful
                ? "TOPUP_SUCCESS:" + amount
                : "TOPUP_FAILED:" + resultCode;
        messagingTemplate.convertAndSend("/topic/driver/" + driverId, notification);
        log.info("Đã xử lý callback nạp tiền {} từ {}", walletTransactionId, paymentMethod);
    }
}
