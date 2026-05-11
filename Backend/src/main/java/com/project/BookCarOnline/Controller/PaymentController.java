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

/**
 * Payment Controller
 * Handles VNPay and MoMo payment integration
 * 
 * Endpoints:
 * - POST /payments/vnpay/create         -> Create VNPay payment
 * - GET  /payments/vnpay/callback       -> VNPay IPN callback
 * - GET  /payments/vnpay/return         -> VNPay return URL
 * - POST /payments/momo/create          -> Create MoMo payment
 * - POST /payments/momo/callback        -> MoMo IPN callback
 * - GET  /payments/momo/return          -> MoMo return URL
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    VNPayService vnPayService;
    MoMoService moMoService;

    // ==================== VNPay Endpoints ====================

    /**
     * Create VNPay payment
     * 
     * @param request VNPay payment request
     * @return Payment URL to redirect customer
     */
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

    /**
     * VNPay IPN (Instant Payment Notification) callback
     * This endpoint is called by VNPay server to notify payment status
     * 
     * @param params Payment callback parameters from VNPay
     * @return Callback response
     */
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

    /**
     * VNPay return URL
     * This endpoint is called when customer returns from VNPay payment page
     * 
     * @param params Return parameters from VNPay
     * @return Payment result
     */
    @GetMapping("/vnpay/return")
    public APIResponse<PaymentCallbackResponse> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("REST API: GET /payments/vnpay/return - Handling VNPay return");
        
        PaymentCallbackResponse response = vnPayService.handleCallback(params);
        
        // In real application, you would redirect to frontend with payment status
        // For API, we return the result
        
        return APIResponse.<PaymentCallbackResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xử lý kết quả thanh toán VNPay")
                .result(response)
                .build();
    }

    /**
     * Query VNPay transaction status
     * 
     * @param orderId Order ID
     * @param transactionDate Transaction date (format: yyyyMMddHHmmss)
     * @return Transaction status
     */
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

    /**
     * Create MoMo payment
     * 
     * @param request MoMo payment request
     * @return Payment URL to redirect customer
     */
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

    /**
     * MoMo IPN (Instant Payment Notification) callback
     * This endpoint is called by MoMo server to notify payment status
     * 
     * @param params Payment callback parameters from MoMo
     * @return Callback response
     */
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

    /**
     * MoMo return URL
     * This endpoint is called when customer returns from MoMo payment page
     * 
     * @param params Return parameters from MoMo
     * @return Payment result
     */
    @GetMapping("/momo/return")
    public APIResponse<PaymentCallbackResponse> momoReturn(@RequestParam Map<String, String> params) {
        log.info("REST API: GET /payments/momo/return - Handling MoMo return");
        
        PaymentCallbackResponse response = moMoService.handleCallback(params);
        
        return APIResponse.<PaymentCallbackResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xử lý kết quả thanh toán MoMo")
                .result(response)
                .build();
    }

    /**
     * Query MoMo transaction status
     * 
     * @param orderId Order ID
     * @param requestId Request ID
     * @return Transaction status
     */
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

    /**
     * Refund MoMo transaction
     * 
     * @param orderId Order ID
     * @param requestId Request ID
     * @param amount Refund amount
     * @param description Refund description
     * @return Refund result
     */
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
