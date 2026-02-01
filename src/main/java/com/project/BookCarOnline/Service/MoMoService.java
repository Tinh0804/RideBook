package com.project.BookCarOnline.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.BookCarOnline.Configuration.MoMoConfig;
import com.project.BookCarOnline.DTO.Request.MoMoPaymentRequest;
import com.project.BookCarOnline.DTO.Response.PaymentCallbackResponse;
import com.project.BookCarOnline.DTO.Response.PaymentResponse;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Utils.PaymentUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * MoMo Payment Service
 * Integration with MoMo Payment Gateway
 * Documentation: https://developers.momo.vn/
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MoMoService {

    MoMoConfig moMoConfig;
    RideBookRepository bookingRepository;
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create MoMo payment request
     */
    public PaymentResponse createPayment(MoMoPaymentRequest request) {
        log.info("Creating MoMo payment for booking: {}", request.getBookingId());

        // Validate booking exists
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        try {
            // Generate order ID and request ID
            String orderId = request.getBookingId() + "_" + PaymentUtils.getCurrentTimestamp();
            String requestId = orderId;

            // Build MoMo request parameters
            String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() : moMoConfig.getReturnUrl();
            String notifyUrl = request.getNotifyUrl() != null ? request.getNotifyUrl() : moMoConfig.getNotifyUrl();

            // Build raw signature string
            String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + request.getAmount() +
                    "&extraData=" + "" +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + request.getOrderInfo() +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + moMoConfig.getRequestType();

            // Generate signature
            String signature = PaymentUtils.hmacSHA256(moMoConfig.getSecretKey(), rawSignature);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("partnerName", "Book Car Online");
            requestBody.put("storeId", "BookCarStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", request.getAmount());
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", request.getOrderInfo());
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("ipnUrl", notifyUrl);
            requestBody.put("lang", "vi");
            requestBody.put("extraData", "");
            requestBody.put("requestType", moMoConfig.getRequestType());
            requestBody.put("signature", signature);

            // Make HTTP POST request to MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Sending request to MoMo API: {}", moMoConfig.getApiUrl());
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    moMoConfig.getApiUrl(),
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && "0".equals(String.valueOf(responseBody.get("resultCode")))) {
                String payUrl = (String) responseBody.get("payUrl");
                String deeplink = (String) responseBody.get("deeplink");
                String qrCodeUrl = (String) responseBody.get("qrCodeUrl");

                log.info("MoMo payment URL created successfully for order: {}", orderId);

                return PaymentResponse.builder()
                        .status("SUCCESS")
                        .message("Tạo link thanh toán MoMo thành công")
                        .paymentUrl(payUrl != null ? payUrl : deeplink)
                        .orderId(orderId)
                        .amount(request.getAmount())
                        .paymentMethod("MOMO")
                        .build();
            } else {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("MoMo payment creation failed: {}", errorMessage);
                throw new IllegalStateException("Tạo thanh toán MoMo thất bại: " + errorMessage);
            }

        } catch (Exception e) {
            log.error("Error creating MoMo payment: {}", e.getMessage());
            throw new IllegalStateException("Lỗi khi tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    /**
     * Handle MoMo callback (IPN - Instant Payment Notification)
     */
    public PaymentCallbackResponse handleCallback(Map<String, String> params) {
        log.info("Handling MoMo callback");

        try {
            // Extract parameters
            String partnerCode = params.get("partnerCode");
            String orderId = params.get("orderId");
            String requestId = params.get("requestId");
            String amount = params.get("amount");
            String orderInfo = params.get("orderInfo");
            String orderType = params.get("orderType");
            String transId = params.get("transId");
            String resultCode = params.get("resultCode");
            String message = params.get("message");
            String payType = params.get("payType");
            String responseTime = params.get("responseTime");
            String extraData = params.get("extraData");
            String receivedSignature = params.get("signature");

            // Build raw signature for verification
            String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + payType +
                    "&requestId=" + requestId +
                    "&responseTime=" + responseTime +
                    "&resultCode=" + resultCode +
                    "&transId=" + transId;

            // Verify signature
            String calculatedSignature = PaymentUtils.hmacSHA256(moMoConfig.getSecretKey(), rawSignature);

            if (!calculatedSignature.equals(receivedSignature)) {
                log.error("Invalid MoMo signature");
                return PaymentCallbackResponse.builder()
                        .paymentStatus("FAILED")
                        .message("Chữ ký không hợp lệ")
                        .paymentMethod("MOMO")
                        .build();
            }

            // Extract booking ID from order ID
            String bookingId = orderId.split("_")[0];

            // Determine payment status
            String paymentStatus = "0".equals(resultCode) ? "SUCCESS" : "FAILED";
            String statusMessage = "0".equals(resultCode) 
                    ? "Thanh toán thành công" 
                    : "Thanh toán thất bại: " + message;

            log.info("MoMo payment status: {} for booking: {}", paymentStatus, bookingId);

            return PaymentCallbackResponse.builder()
                    .bookingId(bookingId)
                    .orderId(orderId)
                    .transactionId(transId)
                    .amount(Long.parseLong(amount))
                    .paymentStatus(paymentStatus)
                    .paymentMethod("MOMO")
                    .message(statusMessage)
                    .paymentTime(responseTime)
                    .build();

        } catch (Exception e) {
            log.error("Error handling MoMo callback: {}", e.getMessage());
            return PaymentCallbackResponse.builder()
                    .paymentStatus("FAILED")
                    .message("Lỗi xử lý callback: " + e.getMessage())
                    .paymentMethod("MOMO")
                    .build();
        }
    }

    /**
     * Query MoMo transaction status
     */
    public Map<String, Object> queryTransaction(String orderId, String requestId) {
        log.info("Querying MoMo transaction: {}", orderId);

        try {
            // Build raw signature
            String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                    "&orderId=" + orderId +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&requestId=" + requestId;

            String signature = PaymentUtils.hmacSHA256(moMoConfig.getSecretKey(), rawSignature);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("requestId", requestId);
            requestBody.put("orderId", orderId);
            requestBody.put("lang", "vi");
            requestBody.put("signature", signature);

            // Make HTTP POST request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String queryUrl = moMoConfig.getApiUrl().replace("/create", "/query");
            ResponseEntity<Map> response = restTemplate.postForEntity(queryUrl, entity, Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Error querying MoMo transaction: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", -1);
            errorResponse.put("message", "Lỗi truy vấn giao dịch: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Refund MoMo transaction
     */
    public Map<String, Object> refundTransaction(String orderId, String requestId, Long amount, String description) {
        log.info("Refunding MoMo transaction: {}", orderId);

        try {
            String transId = PaymentUtils.getCurrentTimestamp();

            // Build raw signature
            String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&description=" + description +
                    "&orderId=" + orderId +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&requestId=" + requestId +
                    "&transId=" + transId;

            String signature = PaymentUtils.hmacSHA256(moMoConfig.getSecretKey(), rawSignature);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("orderId", orderId);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("transId", transId);
            requestBody.put("lang", "vi");
            requestBody.put("description", description);
            requestBody.put("signature", signature);

            // Make HTTP POST request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String refundUrl = moMoConfig.getApiUrl().replace("/create", "/refund");
            ResponseEntity<Map> response = restTemplate.postForEntity(refundUrl, entity, Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Error refunding MoMo transaction: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", -1);
            errorResponse.put("message", "Lỗi hoàn tiền: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Get MoMo result code meaning
     */
    public String getResultCodeMessage(String resultCode) {
        Map<String, String> resultCodes = new HashMap<>();
        resultCodes.put("0", "Giao dịch thành công");
        resultCodes.put("9000", "Giao dịch được khởi tạo, chờ người dùng xác nhận thanh toán");
        resultCodes.put("8000", "Giao dịch đang được xử lý");
        resultCodes.put("7000", "Giao dịch đang chờ thanh toán");
        resultCodes.put("1000", "Giao dịch đã được khởi tạo, chờ người dùng xác nhận thanh toán");
        resultCodes.put("11", "Truy cập bị từ chối");
        resultCodes.put("12", "Phiên bản API không được hỗ trợ cho yêu cầu này");
        resultCodes.put("13", "Xác thực doanh nghiệp thất bại");
        resultCodes.put("20", "Yêu cầu sai định dạng");
        resultCodes.put("21", "Số tiền giao dịch không hợp lệ");
        resultCodes.put("40", "RequestId bị trùng");
        resultCodes.put("41", "OrderId bị trùng");
        resultCodes.put("42", "OrderId không hợp lệ hoặc không được tìm thấy");
        resultCodes.put("43", "Yêu cầu bị từ chối vì xung đột trong quá trình xử lý giao dịch");
        resultCodes.put("1001", "Giao dịch thanh toán thất bại do tài khoản người dùng không đủ tiền");
        resultCodes.put("1002", "Giao dịch bị từ chối do nhà phát hành tài khoản thanh toán");
        resultCodes.put("1003", "Giao dịch đã bị hủy");
        resultCodes.put("1004", "Giao dịch thất bại do số tiền thanh toán vượt quá hạn mức thanh toán của người dùng");
        resultCodes.put("1005", "Giao dịch thất bại do url hoặc QR code đã hết hạn");
        resultCodes.put("1006", "Giao dịch thất bại do người dùng đã từ chối xác nhận thanh toán");
        resultCodes.put("1007", "Giao dịch bị từ chối vì tài khoản người dùng đang ở trạng thái tạm khóa");
        resultCodes.put("1026", "Giao dịch bị hạn chế theo thể lệ chương trình khuyến mãi");
        resultCodes.put("1080", "Giao dịch hoàn tiền bị từ chối. Giao dịch thanh toán ban đầu không được tìm thấy");
        resultCodes.put("1081", "Giao dịch hoàn tiền bị từ chối. Giao dịch thanh toán ban đầu đã được hoàn");
        resultCodes.put("2001", "Giao dịch thất bại do sai thông tin liên kết");
        resultCodes.put("2007", "Giao dịch thất bại do thông tin tài khoản không hợp lệ");
        resultCodes.put("3001", "Liên kết thất bại do người dùng từ chối xác nhận thanh toán");
        resultCodes.put("3002", "Liên kết bị từ chối do không thỏa quy tắc liên kết");
        resultCodes.put("3003", "Tài khoản đã được liên kết với tài khoản MoMo khác");
        resultCodes.put("3004", "Liên kết thất bại do số lần nhập OTP sai vượt quá quy định");
        resultCodes.put("4001", "Giao dịch bị hạn chế theo thể lệ chương trình khuyến mãi hoặc theo thể lệ giao dịch của ví");
        resultCodes.put("4010", "Tài khoản người dùng không tồn tại trên hệ thống MoMo");
        resultCodes.put("4011", "Tài khoản người dùng bị khóa hoặc chưa được kích hoạt");
        resultCodes.put("4100", "Giao dịch thất bại do người dùng không xác nhận thanh toán trong thời gian quy định");
        resultCodes.put("10", "Hệ thống đang được bảo trì");
        resultCodes.put("99", "Lỗi không xác định");

        return resultCodes.getOrDefault(resultCode, "Lỗi không xác định");
    }
}
