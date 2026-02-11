package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.Configuration.VNPayConfig;
import com.project.BookCarOnline.DTO.Request.VNPayPaymentRequest;
import com.project.BookCarOnline.DTO.Response.PaymentCallbackResponse;
import com.project.BookCarOnline.DTO.Response.PaymentResponse;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Repository.PaymentRepository;
import com.project.BookCarOnline.Utils.PaymentUtils;
import com.project.BookCarOnline.Entity.Payment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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


    public PaymentResponse createPayment(VNPayPaymentRequest request) {
        log.info("Creating VNPay payment for booking: {}", request.getBookingId());

        // Validate booking exists
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Generate order ID and timestamp
        String orderId = PaymentUtils.generateOrderId(request.getBookingId());
        String vnpTxnRef = orderId;
        String vnpCreateDate = PaymentUtils.getVNPayTimestamp();
        String vnpExpireDate = PaymentUtils.getVNPayExpireTime();

        // Build VNPay parameters
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(request.getAmount() * 100)); // VNPay uses smallest currency unit
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", request.getLocale() != null ? request.getLocale() : "vn");
        vnpParams.put("vnp_ReturnUrl", request.getReturnUrl() != null ? request.getReturnUrl() : vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", "127.0.0.1"); // Should be client IP
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build hash data and signature
        String hashData = PaymentUtils.buildQueryString(vnpParams);
        String vnpSecureHash = PaymentUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        vnpParams.put("vnp_SecureHash", vnpSecureHash);

        // Build payment URL
        String paymentUrl = PaymentUtils.buildPaymentUrl(vnPayConfig.getApiUrl(), vnpParams);

        log.info("VNPay payment URL created successfully for order: {}", orderId);
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

    /**
     * Handle VNPay callback (IPN - Instant Payment Notification)
     */
    public PaymentCallbackResponse handleCallback(Map<String, String> params) {
        log.info("Handling VNPay callback");

        // Get signature from params
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        // Validate signature
        String hashData = PaymentUtils.buildQueryString(params);
        String calculatedHash = PaymentUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

        if (!calculatedHash.equals(vnpSecureHash)) {
            log.error("Invalid VNPay signature");
            return PaymentCallbackResponse.builder()
                    .paymentStatus("FAILED")
                    .message("Chữ ký không hợp lệ")
                    .paymentMethod("VNPAY")
                    .build();
        }

        // Extract payment info
        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTxnRef = params.get("vnp_TxnRef");
        String vnpTransactionNo = params.get("vnp_TransactionNo");
        String vnpAmount = params.get("vnp_Amount");
        String vnpPayDate = params.get("vnp_PayDate");

        // Extract booking ID from order ID
        String bookingId = vnpTxnRef.split("_")[0];

        // Determine payment status
        String paymentStatus = "00".equals(vnpResponseCode) ? "SUCCESS" : "FAILED";
        String message = "00".equals(vnpResponseCode) 
                ? "Thanh toán thành công" 
                : "Thanh toán thất bại - Mã lỗi: " + vnpResponseCode;

        log.info("VNPay payment status: {} for booking: {}", paymentStatus, bookingId);

        if ("SUCCESS".equals(paymentStatus)) {
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
                            "PAYMENT_SUCCESS:" + bookingId);
                }
            });
        } else {
            bookingRepository.findById(bookingId).ifPresent(booking -> {
                if (booking.getCustomerNo() != null) {
                    messagingTemplate.convertAndSend(
                            "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                            "PAYMENT_FAILED:" + bookingId);
                }
            });
        }

        return PaymentCallbackResponse.builder()
                .bookingId(bookingId)
                .orderId(vnpTxnRef)
                .transactionId(vnpTransactionNo)
                .amount(Long.parseLong(vnpAmount) / 100) // Convert back to VND
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
