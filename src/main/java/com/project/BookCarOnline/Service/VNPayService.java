package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.Configuration.VNPayConfig;
import com.project.BookCarOnline.DTO.Request.PaymentRequest;
import com.project.BookCarOnline.DTO.Response.PaymentCallbackResponse;
import com.project.BookCarOnline.DTO.Response.PaymentResponse;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Repository.PaymentRepository;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Entity.Driver;
import com.google.maps.model.GeocodingResult;
import com.project.BookCarOnline.Utils.PaymentUtils;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VNPay Payment Service
 * Integration with VNPay Payment Gateway
 * Documentation: https://sandbox.vnpayment.vn/apis/
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayService {

    VNPayConfig vnPayConfig;
    RideBookRepository bookingRepository;
    PaymentRepository paymentRepository;
    SimpMessagingTemplate messagingTemplate;
    RideDispatcherService dispatcherService;
    DriverRepository driverRepository;

    WalletService walletService;
    GoogleMapService googleMapService;


    public PaymentResponse createPayment(PaymentRequest request) {

        log.info("Creating VNPay payment for booking: {}", request.getReferenceId());


        String orderId = PaymentUtils.generateOrderId(request.getReferenceId());
        String vnpCreateDate = PaymentUtils.getVNPayTimestamp();

        // 🔥 PHẢI dùng TreeMap
        Map<String, String> vnpParams = new java.util.TreeMap<>();

        long amountInVND = (long) Math.round(request.getAmount() * 100); // Convert to VND (1 USD = 100 VND)

        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amountInVND));
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_Locale",  "vn");
        vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_ReturnUrl",
                request.getReturnUrl() != null ? request.getReturnUrl() : vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_TxnRef", orderId);

        // 🔥 Build hash data KHÔNG encode
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (hashData.length() > 0) {
                hashData.append("&");
            }
            hashData.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        String secureHash = PaymentUtils.hmacSHA512(
                vnPayConfig.getHashSecret(),
                hashData.toString()
        );

        // 🔥 Sau khi hash xong mới thêm SecureHash
        vnpParams.put("vnp_SecureHash", secureHash);

        // 🔥 Build URL (encode ở đây)
        String paymentUrl = buildUrlWithEncode(vnPayConfig.getApiUrl(), vnpParams);

        log.info("Hash data: {}", hashData);
        log.info("Secure hash: {}", secureHash);
        log.info("Payment URL: {}", paymentUrl);

        return PaymentResponse.builder()
                .status("SUCCESS")
                .message("Tạo link thanh toán VNPay thành công")
                .paymentUrl(paymentUrl)
                .orderId(orderId)
                .amount(request.getAmount())
                .paymentMethod("VNPAY")
                .build();
    }
    @Transactional
    public PaymentResponse createTopUpPayment(String driverId, double amount, String returnUrl,String walletTransactionId) {
        log.info("Creating VNPay top-up for driver: {}", driverId);

        // Gắn tiền tố TOPUP_ để dễ dàng phân biệt khi nhận callback
        String vnpCreateDate = PaymentUtils.getVNPayTimestamp();
        String orderId = "TOPUP_" + driverId + "_" + walletTransactionId;

        Map<String, String> vnpParams = new java.util.TreeMap<>();
        long amountInVND = (long) Math.round(amount * 100);

        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amountInVND));
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_OrderInfo", "Nạp tiền vào ví tài xế " + driverId);
        vnpParams.put("vnp_OrderType", "topup"); // Có thể đổi type theo cấu hình của bạn
        vnpParams.put("vnp_ReturnUrl", returnUrl != null ? returnUrl : vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_TxnRef", orderId);

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (hashData.length() > 0) {
                hashData.append("&");
            }
            hashData.append(entry.getKey()).append("=").append(entry.getValue());
        }

        String secureHash = PaymentUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        vnpParams.put("vnp_SecureHash", secureHash);

        String paymentUrl = buildUrlWithEncode(vnPayConfig.getApiUrl(), vnpParams);

        return PaymentResponse.builder()
                .status("SUCCESS")
                .message("Tạo link nạp tiền VNPay thành công")
                .paymentUrl(paymentUrl)
                .orderId(orderId)
                .amount(amount)
                .paymentMethod("VNPAY")
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


    /**
     * Handle VNPay callback (IPN - Instant Payment Notification)
     */

    public PaymentCallbackResponse handleCallback(Map<String, String> params) {

        log.info("Handling VNPay callback...");

        // 1️⃣ Lấy chữ ký VNPay gửi về
        String vnpSecureHash = params.get("vnp_SecureHash");

        if (vnpSecureHash == null) {
            return PaymentCallbackResponse.builder()
                    .paymentStatus("FAILED")
                    .message("Thiếu chữ ký VNPay")
                    .paymentMethod("VNPAY")
                    .build();
        }

        // 2️⃣ Copy sang TreeMap để sort alphabet
        Map<String, String> sortedParams = new java.util.TreeMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null
                    && !entry.getKey().equals("vnp_SecureHash")
                    && !entry.getKey().equals("vnp_SecureHashType")) {

                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }

        // 3️⃣ Build hash data ĐÃ encode lại giống lúc gửi đi
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

        // 4️⃣ Tính lại chữ ký
        String calculatedHash = PaymentUtils.hmacSHA512(
                vnPayConfig.getHashSecret(),
                hashData.toString()
        );

        log.info("VNPay returned hash: {}", vnpSecureHash);
        log.info("Calculated hash: {}", calculatedHash);
        log.info("Raw hash data: {}", hashData);

        // 5️⃣ So sánh chữ ký
        if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
            log.error("Invalid VNPay signature!");
            return PaymentCallbackResponse.builder()
                    .paymentStatus("FAILED")
                    .message("Chữ ký không hợp lệ")
                    .paymentMethod("VNPAY")
                    .build();
        }

        // 6️⃣ Lấy dữ liệu thanh toán
        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTxnRef = params.get("vnp_TxnRef");
        String vnpTransactionNo = params.get("vnp_TransactionNo");
        String vnpAmount = params.get("vnp_Amount");
        String vnpPayDate = params.get("vnp_PayDate");

        String bookingId = vnpTxnRef.split("_")[0];

        String paymentStatus = "00".equals(vnpResponseCode) ? "SUCCESS" : "FAILED";

        String message = "00".equals(vnpResponseCode)
                ? "Thanh toán thành công"
                : "Thanh toán thất bại - Mã lỗi: " + vnpResponseCode;

        log.info("Payment status: {} - TxnRef: {}", paymentStatus, vnpTxnRef);

        if ("SUCCESS".equals(paymentStatus)) {

            // -------------------------------------------------------------
            // LUỒNG 1: XỬ LÝ NẠP TIỀN VÍ TÀI XẾ (Bắt đầu bằng TOPUP_)
            // -------------------------------------------------------------
            if (vnpTxnRef.startsWith("TOPUP_")) {
                // vnpTxnRef có dạng: TOPUP_{driverId}_{walletTransactionId}
                String driverId = vnpTxnRef.split("_")[1];
                long topUpAmount = Long.parseLong(vnpAmount) / 100;
                String walletTransactionId =  vnpTxnRef.split("_")[2];
                driverRepository.findById(driverId).ifPresent(driver -> {
                    boolean isProcessed =  walletService.processPaymentCallback(vnpTransactionNo, true, vnpTransactionNo);
                    if (isProcessed) {
                        messagingTemplate.convertAndSend(
                                "/topic/driver/" + driverId,
                                "TOPUP_SUCCESS:" + topUpAmount);
                        log.info("Cộng {} VND vào ví tài xế {} thành công qua VNPAY", topUpAmount, driverId);
                    } else {
                        log.error("Xử lý cộng tiền ví thất bại do không tìm thấy giao dịch {}", vnpTransactionNo);
                    }

                });
            }

            // LUỒNG 2: XỬ LÝ THANH TOÁN CHUYẾN XE
            else {
                bookingId = vnpTxnRef.split("_")[0];
                String finalBookingId = bookingId;
                bookingRepository.findById(bookingId).ifPresent(booking -> {
                    booking.setBookingStatus(BookingStatus.PENDING);
                    if (booking.getPaymentNo() != null) {
                        booking.getPaymentNo().setPaymentStatus(true);
                        paymentRepository.save(booking.getPaymentNo());
                    }
                    bookingRepository.save(booking);

                    if (booking.getCustomerNo() != null) {
                        messagingTemplate.convertAndSend(
                                "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                                "PAYMENT_SUCCESS:" + finalBookingId);
                    }

                    GeocodingResult geo = googleMapService.geocode(booking.getPickupLocation());
                    List<Driver> candidates = driverRepository.findTrulyAvailableDriversNearby(
                            geo.geometry.location.lat,
                            geo.geometry.location.lng,
                            5.0);
                    dispatcherService.startDispatching(finalBookingId, candidates);
                });
            }
        } else {
            // Xử lý thất bại
            if (vnpTxnRef.startsWith("TOPUP_")) {
                String driverId = vnpTxnRef.split("_")[1];
                messagingTemplate.convertAndSend(
                        "/topic/driver/" + driverId,
                        "TOPUP_FAILED:" + vnpResponseCode);
            } else {
                bookingId = vnpTxnRef.split("_")[0];
                String finalBookingId = bookingId;
                bookingRepository.findById(bookingId).ifPresent(booking -> {
                    if (booking.getCustomerNo() != null) {
                        messagingTemplate.convertAndSend(
                                "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                                "PAYMENT_FAILED:" + finalBookingId);
                    }
                });
            }
        }

        return PaymentCallbackResponse.builder()
                .bookingId(bookingId)
                .orderId(vnpTxnRef)
                .transactionId(vnpTransactionNo)
                .amount(Long.parseLong(vnpAmount) / 100)
                .paymentStatus(paymentStatus)
                .paymentMethod("VNPAY")
                .message(message)
                .paymentTime(vnpPayDate)
                .build();
    }

    /**
     * Query VNPay transaction status
     */
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

    /**
     * Get VNPay response code meaning
     */
    public String getResponseCodeMessage(String responseCode) {
        Map<String, String> responseCodes = new HashMap<>();
        responseCodes.put("00", "Giao dịch thành công");
        responseCodes.put("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).");
        responseCodes.put("09", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.");
        responseCodes.put("10", "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần");
        responseCodes.put("11", "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.");
        responseCodes.put("12", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.");
        responseCodes.put("13", "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP).");
        responseCodes.put("24", "Giao dịch không thành công do: Khách hàng hủy giao dịch");
        responseCodes.put("51", "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.");
        responseCodes.put("65", "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.");
        responseCodes.put("75", "Ngân hàng thanh toán đang bảo trì.");
        responseCodes.put("79", "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định.");
        responseCodes.put("99", "Các lỗi khác");

        return responseCodes.getOrDefault(responseCode, "Lỗi không xác định");
    }
}
