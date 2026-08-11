package com.project.BookCarOnline.app.service.payment;

import com.project.BookCarOnline.app.config.VNPayConfig;
import com.project.BookCarOnline.finance.dto.response.PaymentCallbackResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class VNPayServiceTest {

    @Test
    void callbackWithoutSignatureCannotUpdatePaymentState() {
        PaymentCallbackService callbackService = mock(PaymentCallbackService.class);
        VNPayService service = new VNPayService(mock(VNPayConfig.class), callbackService);

        PaymentCallbackResponse response = service.handleCallback(Map.of());

        assertEquals("FAILED", response.getPaymentStatus());
        assertNull(response.getOrderId());
        verifyNoInteractions(callbackService);
    }
}
