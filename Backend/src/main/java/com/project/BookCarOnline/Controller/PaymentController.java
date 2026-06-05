package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.PaymentRequest;
import com.project.BookCarOnline.DTO.Response.PaymentCallbackResponse;
import com.project.BookCarOnline.DTO.Response.PaymentResponse;
import com.project.BookCarOnline.Service.MoMoService;
import com.project.BookCarOnline.Service.VNPayService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    VNPayService vnPayService;
    MoMoService moMoService;

    // ==================== VNPay Endpoints ====================
    @PostMapping("/vnpay/create")
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<PaymentResponse> createVNPayPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("REST API: POST /payments/vnpay/create - Creating VNPay payment for booking: {}", 
                request.getReferenceId());
        
        PaymentResponse response = vnPayService.createPayment(request);
        
        return APIResponse.<PaymentResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo thanh toán VNPay thành công")
                .result(response)
                .build();
    }

    @GetMapping("/vnpay/callback")
    public APIResponse<PaymentCallbackResponse> vnpayCallback(@RequestParam Map<String, String> params) {
        log.info("REST API: GET /payments/vnpay/callback - Handling VNPay callback");
        
        PaymentCallbackResponse response = vnPayService.handleCallback(params);
        
        return APIResponse.<PaymentCallbackResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xử lý callback VNPay thành công")
                .result(response)
                .build();
    }

    @GetMapping("/vnpay/return")
    public APIResponse<PaymentCallbackResponse> vnpayReturn(@RequestParam Map<String, String> params, jakarta.servlet.http.HttpServletResponse httpResponse) throws java.io.IOException {
        log.info("REST API: GET /payments/vnpay/return - Handling VNPay return");
        
        PaymentCallbackResponse response = vnPayService.handleCallback(params);
        
        String vnpTxnRef = params.get("vnp_TxnRef");
        if (vnpTxnRef != null && vnpTxnRef.startsWith("TOPUP_")) {
            String returnUrl = params.get("vnp_OrderInfo");
            if (returnUrl != null && returnUrl.startsWith("http")) {
                String redirectUrl = returnUrl + "?vnp_ResponseCode=" + params.get("vnp_ResponseCode") 
                        + "&vnp_Amount=" + params.get("vnp_Amount");
                httpResponse.sendRedirect(redirectUrl);
                return null;
            }
        }
        
        return APIResponse.<PaymentCallbackResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xử lý kết quả thanh toán VNPay")
                .result(response)
                .build();
    }

    @GetMapping("/vnpay/query/{orderId}")
    public APIResponse<Map<String, String>> queryVNPayTransaction(
            @PathVariable String orderId,
            @RequestParam String transactionDate) {
        log.info("REST API: GET /payments/vnpay/query/{} - Querying VNPay transaction", orderId);
        
        Map<String, String> response = vnPayService.queryTransaction(orderId, transactionDate);
        
        return APIResponse.<Map<String, String>>builder()
                .status(HttpStatus.OK.value())
                .message("Truy vấn giao dịch VNPay thành công")
                .result(response)
                .build();
    }

    // ==================== MoMo Endpoints ====================
    @PostMapping("/momo/create")
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<PaymentResponse> createMoMoPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("REST API: POST /payments/momo/create - Creating MoMo payment for booking: {}", 
                request.getReferenceId());
        
        PaymentResponse response = moMoService.createPayment(request);
        
        return APIResponse.<PaymentResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo thanh toán MoMo thành công")
                .result(response)
                .build();
    }

    @PostMapping("/momo/callback")
    public APIResponse<PaymentCallbackResponse> momoCallback(@RequestBody Map<String, String> params) {
        log.info("REST API: POST /payments/momo/callback - Handling MoMo callback");
        
        PaymentCallbackResponse response = moMoService.handleCallback(params);
        
        return APIResponse.<PaymentCallbackResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xử lý callback MoMo thành công")
                .result(response)
                .build();
    }

    @GetMapping("/momo/return")
    public APIResponse<PaymentCallbackResponse> momoReturn(@RequestParam Map<String, String> params, jakarta.servlet.http.HttpServletResponse httpResponse) throws java.io.IOException {
        log.info("REST API: GET /payments/momo/return - Handling MoMo return");
        
        PaymentCallbackResponse response = moMoService.handleCallback(params);
        
        String orderId = params.get("orderId");
        if (orderId != null && orderId.startsWith("TOPUP_")) {
            String returnUrl = params.get("extraData");
            if (returnUrl != null && returnUrl.startsWith("http")) {
                String resultCode = params.get("resultCode");
                if (resultCode == null) {
                    resultCode = params.get("errorCode");
                }
                String redirectUrl = returnUrl + "?resultCode=" + resultCode 
                        + "&amount=" + params.get("amount");
                httpResponse.sendRedirect(redirectUrl);
                return null;
            }
        }
        
        return APIResponse.<PaymentCallbackResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xử lý kết quả thanh toán MoMo")
                .result(response)
                .build();
    }

    @GetMapping("/momo/query/{orderId}")
    public APIResponse<Map<String, Object>> queryMoMoTransaction(
            @PathVariable String orderId,
            @RequestParam String requestId) {
        log.info("REST API: GET /payments/momo/query/{} - Querying MoMo transaction", orderId);
        
        Map<String, Object> response = moMoService.queryTransaction(orderId, requestId);
        
        return APIResponse.<Map<String, Object>>builder()
                .status(HttpStatus.OK.value())
                .message("Truy vấn giao dịch MoMo thành công")
                .result(response)
                .build();
    }

    @PostMapping("/momo/refund/{orderId}")
    public APIResponse<Map<String, Object>> refundMoMoTransaction(
            @PathVariable String orderId,
            @RequestParam String requestId,
            @RequestParam Long amount,
            @RequestParam String description) {
        log.info("REST API: POST /payments/momo/refund/{} - Refunding MoMo transaction", orderId);
        
        Map<String, Object> response = moMoService.refundTransaction(orderId, requestId, amount, description);
        
        return APIResponse.<Map<String, Object>>builder()
                .status(HttpStatus.OK.value())
                .message("Hoàn tiền MoMo thành công")
                .result(response)
                .build();
    }
}
