package com.project.BookCarOnline.finance.service.payment;

import com.project.BookCarOnline.finance.config.VNPayConfig;
import com.project.BookCarOnline.finance.dto.request.PaymentRequest;
import com.project.BookCarOnline.finance.dto.response.PaymentCallbackResponse;
import com.project.BookCarOnline.finance.dto.response.PaymentResponse;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.util.PaymentUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayService {

    VNPayConfig vnPayConfig;
    PaymentCallbackHandler paymentCallbackHandler;

    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating VNPay payment for booking: {}", request.getReferenceId());
        String orderId = PaymentUtils.generateOrderId(request.getReferenceId());
        String returnUrl = request.getReturnUrl() != null
                ? request.getReturnUrl()
                : vnPayConfig.getReturnUrl();
        return createPaymentResponse(
                orderId,
                request.getAmount(),
                request.getOrderInfo(),
                vnPayConfig.getOrderType(),
                returnUrl,
                "Tạo link thanh toán VNPay thành công",
                "VNPAY");
    }

    public PaymentResponse createTopUpPayment(
            String driverId, double amount, String returnUrl, String walletTransactionId) {
        log.info("Creating VNPay top-up for driver: {}", driverId);
        String orderId = "TOPUP_" + driverId + "_" + walletTransactionId;
        return createPaymentResponse(
                orderId,
                amount,
                returnUrl,
                "topup",
                vnPayConfig.getReturnUrl(),
                "Tạo link nạp tiền VNPay thành công",
                PaymentMethod.VNPAY.name());
    }

    private PaymentResponse createPaymentResponse(
            String orderId,
            double amount,
            String orderInfo,
            String orderType,
            String returnUrl,
            String message,
            String paymentMethod) {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(Math.round(amount * 100)));
        vnpParams.put("vnp_CreateDate", PaymentUtils.getVNPayTimestamp());
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_TxnRef", orderId);

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (hashData.length() > 0) {
                hashData.append("&");
            }
            hashData.append(entry.getKey()).append("=").append(entry.getValue());
        }

        vnpParams.put("vnp_SecureHash", PaymentUtils.hmacSHA512(
                vnPayConfig.getHashSecret(), hashData.toString()));

        log.info("Created VNPay payment order={}", orderId);
        return PaymentResponse.builder()
                .status("SUCCESS")
                .message(message)
                .paymentUrl(buildUrlWithEncode(vnPayConfig.getApiUrl(), vnpParams))
                .orderId(orderId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .build();
    }

    private String buildUrlWithEncode(String baseUrl, Map<String, String> params) {

        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey())
                    .append("=")
                    .append(java.net.URLEncoder.encode(entry.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                    .append("&");
        }

        url.deleteCharAt(url.length() - 1);

        return url.toString();
    }



    public PaymentCallbackResponse handleCallback(Map<String, String> params) {

        log.info("Handling VNPay callback...");

        String vnpSecureHash = params.get("vnp_SecureHash");

        if (vnpSecureHash == null) {
            return PaymentCallbackResponse.builder()
                    .paymentStatus("FAILED")
                    .message("Thiếu chữ ký VNPay")
                    .paymentMethod(PaymentMethod.VNPAY.name())
                    .build();
        }

        Map<String, String> sortedParams = new TreeMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null
                    && !entry.getKey().equals("vnp_SecureHash")
                    && !entry.getKey().equals("vnp_SecureHashType")) {

                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }

        StringBuilder hashData = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {

            if (entry.getValue() != null && !entry.getValue().isEmpty()) {

                if (hashData.length() > 0) {
                    hashData.append("&");
                }

                hashData.append(entry.getKey())
                        .append("=")
                        .append(java.net.URLEncoder.encode(
                                entry.getValue(),
                                java.nio.charset.StandardCharsets.US_ASCII
                        ));
            }
        }


        String calculatedHash = PaymentUtils.hmacSHA512(
                vnPayConfig.getHashSecret(),
                hashData.toString()
        );

        if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
            log.error("Invalid VNPay signature!");
            return PaymentCallbackResponse.builder()
                    .paymentStatus("FAILED")
                    .message("Chữ ký không hợp lệ")
                    .paymentMethod("VNPAY")
                    .build();
        }

        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTxnRef = params.get("vnp_TxnRef");
        String vnpTransactionNo = params.get("vnp_TransactionNo");
        String vnpAmount = params.get("vnp_Amount");
        String vnpPayDate = params.get("vnp_PayDate");

        String bookingId = vnpTxnRef.split("_", 2)[0];

        String paymentStatus = "00".equals(vnpResponseCode) ? "SUCCESS" : "FAILED";

        String message = "00".equals(vnpResponseCode)
                ? "Thanh toán thành công"
                : "Thanh toán thất bại - Mã lỗi: " + vnpResponseCode;

        log.info("Payment status: {} - TxnRef: {}", paymentStatus, vnpTxnRef);

        long callbackAmount = Long.parseLong(vnpAmount) / 100;
        paymentCallbackHandler.process(
                vnpTxnRef,
                callbackAmount,
                "SUCCESS".equals(paymentStatus),
                vnpResponseCode,
                vnpTransactionNo,
                PaymentMethod.VNPAY);

        return PaymentCallbackResponse.builder()
                .bookingId(bookingId)
                .orderId(vnpTxnRef)
                .transactionId(vnpTransactionNo)
                .amount(callbackAmount)
                .paymentStatus(paymentStatus)
                .paymentMethod(PaymentMethod.VNPAY.name())
                .message(message)
                .paymentTime(vnpPayDate)
                .build();
    }

    public Map<String, String> queryTransaction(String orderId, String transactionDate) {
        log.info("Querying VNPay transaction: {}", orderId);

        String vnpRequestId = PaymentUtils.getRandomNumber(8);
        String vnpCreateDate = PaymentUtils.getVNPayTimestamp();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_RequestId", vnpRequestId);
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", "querydr");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", "Query transaction " + orderId);
        vnpParams.put("vnp_TransactionDate", transactionDate);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");

        String hashData = PaymentUtils.buildQueryString(vnpParams);
        String vnpSecureHash = PaymentUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        vnpParams.put("vnp_SecureHash", vnpSecureHash);

        // Note: In production, you need to make HTTP POST request to VNPay API
        // and parse the response. This is a simplified version.

        return vnpParams;
    }

}
