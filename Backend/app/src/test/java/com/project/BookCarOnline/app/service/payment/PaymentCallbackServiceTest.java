package com.project.BookCarOnline.app.service.payment;

import com.project.BookCarOnline.booking.service.BookingService;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentCallbackServiceTest {

    @Test
    void failedTopUpMarksTransactionFailedAndNotifiesDriver() {
        BookingService bookingService = mock(BookingService.class);
        WalletService walletService = mock(WalletService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        PaymentCallbackService service = new PaymentCallbackService(
                bookingService, walletService, messagingTemplate);
        when(walletService.processPaymentCallback("transaction-1", false, "provider-1"))
                .thenReturn(true);

        service.process(
                "TOPUP_driver-1_transaction-1",
                100_000,
                false,
                "51",
                "provider-1",
                PaymentMethod.VNPAY);

        verify(walletService).processPaymentCallback("transaction-1", false, "provider-1");
        verify(messagingTemplate).convertAndSend("/topic/driver/driver-1", "TOPUP_FAILED:51");
    }
}
