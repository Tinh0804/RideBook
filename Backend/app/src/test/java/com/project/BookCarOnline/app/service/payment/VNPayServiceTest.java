package com.project.BookCarOnline.app.service.payment;

import com.project.BookCarOnline.finance.config.VNPayConfig;
import com.project.BookCarOnline.finance.dto.response.PaymentCallbackResponse;
import com.project.BookCarOnline.finance.service.payment.PaymentCallbackHandler;
import com.project.BookCarOnline.finance.service.payment.VNPayService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class VNPayServiceTest {

    @Test
    void callbackWithoutSignatureCannotUpdatePaymentState() {
        PaymentCallbackHandler callbackHandler = mock(PaymentCallbackHandler.class);
        VNPayService service = new VNPayService(mock(VNPayConfig.class), callbackHandler);

        PaymentCallbackResponse response = service.handleCallback(Map.of());

        assertEquals("FAILED", response.getPaymentStatus());
        assertNull(response.getOrderId());
        verifyNoInteractions(callbackHandler);
    }
}
