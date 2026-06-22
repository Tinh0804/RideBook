package com.project.BookCarOnline.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.BookCarOnline.Configuration.MoMoConfig;
import com.project.BookCarOnline.DTO.Request.PaymentRequest;
import com.project.BookCarOnline.DTO.Response.PaymentCallbackResponse;
import com.project.BookCarOnline.DTO.Response.PaymentResponse;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.PaymentMethod;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Repository.PaymentRepository;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Entity.Driver;
import com.google.maps.model.GeocodingResult;
import com.project.BookCarOnline.Utils.PaymentUtils;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MoMoService {

    MoMoConfig moMoConfig;
    RideBookRepository bookingRepository;
    PaymentRepository paymentRepository;
    SimpMessagingTemplate messagingTemplate;
    RideDispatcherService dispatcherService;
    WalletService walletService;
    DriverRepository driverRepository;
    GoogleMapService googleMapService;
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();

    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating MoMo payment for booking: {}", request.getReferenceId());

        try {

            String orderId = PaymentUtils.generateOrderId(request.getReferenceId());
            String requestId = orderId;

            String returnUrl = moMoConfig.getReturnUrl();
            String notifyUrl = moMoConfig.getNotifyUrl();
            String extraData = "";
            long amount = Math.round(request.getAmount());

            String rawSignature =
                    "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + request.getOrderInfo() +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + moMoConfig.getRequestType();

            String signature = PaymentUtils.hmacSHA256(
                    moMoConfig.getSecretKey(),
                    rawSignature
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", request.getOrderInfo());
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("ipnUrl", notifyUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", moMoConfig.getRequestType());
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");


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
            log.info("MoMo response: {}", responseBody);

            if (responseBody != null &&
                    "0".equals(String.valueOf(responseBody.get("resultCode")))) {

                String payUrl = (String) responseBody.get("payUrl");

                return PaymentResponse.builder()
                        .status("SUCCESS")
                        .message("Tạo link thanh toán MoMo thành công")
                        .paymentUrl(payUrl)
                        .orderId(orderId)
                        .amount(request.getAmount())
                        .paymentMethod("MOMO")
                        .build();
            } else {
                String errorMessage = responseBody != null
                        ? String.valueOf(responseBody.get("message"))
                        : "Unknown error";

                throw new IllegalStateException("Tạo thanh toán MoMo thất bại: " + errorMessage);
            }


        } catch (Exception e) {
            log.error("Error creating MoMo payment: {}", e.getMessage());
            throw new IllegalStateException("Lỗi khi tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    public PaymentResponse createTopUpPayment(String driverId, double amountValue, String returnUrlFrontend,String walletTransactionId) {
        log.info("Creating MoMo top-up for driver: {}", driverId);

        try {
            // Sử dụng tiền tố TOPUP_ để đánh dấu giao dịch nạp tiền
            String orderId = "TOPUP_" + driverId + "_" + walletTransactionId;
            String requestId = orderId;

            // Nếu không truyền returnUrl riêng thì dùng mặc định
            String finalReturnUrl = moMoConfig.getReturnUrl();
            String notifyUrl = moMoConfig.getNotifyUrl();
            String extraData = returnUrlFrontend ;
            long amount = Math.round(amountValue);
            String orderInfo = "Nạp tiền vào ví tài xế " + driverId;

            String rawSignature =
                    "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&redirectUrl=" + finalReturnUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + moMoConfig.getRequestType();

            String signature = PaymentUtils.hmacSHA256(moMoConfig.getSecretKey(), rawSignature);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", finalReturnUrl);
            requestBody.put("ipnUrl", notifyUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", moMoConfig.getRequestType());
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Sending TopUp request to MoMo API: {}", moMoConfig.getApiUrl());
            ResponseEntity<Map> response = restTemplate.postForEntity(moMoConfig.getApiUrl(), entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && "0".equals(String.valueOf(responseBody.get("resultCode")))) {
                String payUrl = (String) responseBody.get("payUrl");
                return PaymentResponse.builder()
                        .status("SUCCESS")
                        .message("Tạo link nạp tiền MoMo thành công")
                        .paymentUrl(payUrl)
                        .orderId(orderId)
                        .amount(amountValue)
                        .paymentMethod(PaymentMethod.MOMO.getMethod())
                        .build();
            } else {
                String errorMessage = responseBody != null ? String.valueOf(responseBody.get("message")) : "Unknown error";
                throw new IllegalStateException("Tạo thanh toán nạp tiền MoMo thất bại: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("Error creating MoMo top-up: {}", e.getMessage());
            throw new IllegalStateException("Lỗi khi tạo nạp tiền MoMo: " + e.getMessage());
        }
    }
    @Transactional
    public PaymentCallbackResponse handleCallback(Map<String, String> params) {

        log.info("Handling MoMo callback");

        try {

            String partnerCode = params.get("partnerCode");
            String accessKey = moMoConfig.getAccessKey();
            String orderId = params.get("orderId");
            String requestId = params.get("requestId");
            String amount = params.get("amount");
            String orderInfo = params.get("orderInfo");
            String orderType = params.get("orderType");
            String transId = params.get("transId");
            String message = params.get("message");
            String responseTime = params.get("responseTime");
            String errorCode = params.get("resultCode");
            if (errorCode == null) {
                errorCode = params.get("errorCode");
            }
            String payType = params.get("payType");
            String extraData = params.get("extraData");
            String receivedSignature = params.get("signature");


            if (errorCode == null) errorCode = "";
            if (extraData == null) extraData = "";
            if (message == null) message = "";
            if (orderType == null) orderType = "";
            if (payType == null) payType = "";


            String rawSignature =
                    "accessKey=" + accessKey +
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
                    "&resultCode=" + errorCode +
                    "&transId=" + transId;

            String calculatedSignature = PaymentUtils.hmacSHA256(
                    moMoConfig.getSecretKey(),
                    rawSignature
            );

            if (!calculatedSignature.equals(receivedSignature)) {
                log.error("Invalid signature");
                log.error("Expected: {}", calculatedSignature);
                log.error("Received: {}", receivedSignature);

                return PaymentCallbackResponse.builder()
                        .paymentStatus("FAILED")
                        .message("Chữ ký không hợp lệ")
                        .paymentMethod("MOMO")
                        .build();
            }

            log.info("Signature verified successfully!");

//            String bookingId = orderId.split("_")[0];

            String paymentStatus = "0".equals(errorCode) ? "SUCCESS" : "FAILED";
            String normalizedMessage = getResultCodeMessage(errorCode);

            String statusMessage = "0".equals(errorCode) ? "Thanh toán thành công" : "Thanh toán thất bại: " + normalizedMessage;
            log.info("MoMo payment status: {} for booking: {}", paymentStatus, orderId);
            if ("SUCCESS".equals(paymentStatus)) {
                // LUỒNG 1: XỬ LÝ NẠP TIỀN VÍ TÀI XẾ (Bắt đầu bằng TOPUP_)
                if (orderId.startsWith("TOPUP_")) {
                    String driverId = orderId.split("_")[1];
                    long topUpAmount = Long.parseLong(amount);
                    String walletTransactionId = orderId.split("_")[2];

                    driverRepository.findById(driverId).ifPresent(driver -> {
                        boolean isProcessed = walletService.processPaymentCallback(walletTransactionId, true, transId);

                        if (isProcessed) {
                            messagingTemplate.convertAndSend(
                                    "/topic/driver/" + driverId,
                                    "TOPUP_SUCCESS:" + topUpAmount);
                            log.info("Cộng {} VND vào ví tài xế {} thành công qua MoMo", topUpAmount, driverId);
                        } else {
                            log.error("Xử lý cộng tiền ví thất bại do không tìm thấy giao dịch {}", orderId);
                        }
                    });
                }
                // LUỒNG 2: XỬ LÝ THANH TOÁN CHUYẾN XE
                else {
                    String bookingId = orderId.split("_")[0];
                    bookingRepository.findById(bookingId).ifPresent(
                            booking -> {
                                booking.setBookingStatus(BookingStatus.PENDING);
                                if (booking.getPaymentNo() != null) {
                                    booking.getPaymentNo().setPaymentStatus(true);
                                    paymentRepository.save(booking.getPaymentNo());
                                }
                                bookingRepository.save(booking);

                                if (booking.getCustomerNo() != null) {
                                    messagingTemplate.convertAndSend("/topic/customer/" + booking.getCustomerNo().getCustomerId(), "PAYMENT_SUCCESS:" + bookingId);
                                }

                                GeocodingResult geo = googleMapService.geocode(booking.getPickupLocation());
                                List<Driver> candidates = driverRepository.findTrulyAvailableDriversNearby(
                                        geo.geometry.location.lat,
                                        geo.geometry.location.lng,
                                        5.0,
                                        booking.getVehicleTypeNo().getVehicleTypeId()
                                );
                                dispatcherService.startDispatching(bookingId, candidates);
                            });
                }
            } else {
                // Xử lý thất bại
                if (orderId.startsWith("TOPUP_")) {
                    String driverId = orderId.split("_")[1];
                    messagingTemplate.convertAndSend("/topic/driver/" + driverId, "TOPUP_FAILED:" + errorCode);
                } else {
                    String bookingId = orderId.split("_")[0];
                    bookingRepository.findById(bookingId).ifPresent(
                            booking -> {
                                if (booking.getCustomerNo() != null) {
                                    messagingTemplate.convertAndSend("/topic/customer/" + booking.getCustomerNo().getCustomerId(), "PAYMENT_FAILED:" + bookingId);
                                }
                            });
                }
            }

            // Tách bookingId để trả về object. Nếu là TOPUP thì gán bookingId = rỗng hoặc chính orderId để tránh lỗi Null
            String returnedBookingId = orderId.startsWith("TOPUP_") ? "" : orderId.split("_")[0];

            return PaymentCallbackResponse.builder()
                    .bookingId(returnedBookingId)
                    .orderId(orderId)
                    .transactionId(transId)
                    .amount(Long.parseLong(amount))
                    .paymentStatus(paymentStatus)
                    .paymentMethod(PaymentMethod.MOMO.getMethod())
                    .message(statusMessage)
                    .paymentTime(responseTime)
                    .build();

        } catch (Exception e) {
            log.error("Error handling MoMo callback: {}", e.getMessage());
            return PaymentCallbackResponse.builder()
                    .paymentStatus("FAILED")
                    .message("Lỗi xử lý callback")
                    .paymentMethod("MOMO")
                    .build();
        }
    }


    public Map<String, Object> queryTransaction(String orderId, String requestId) {
        log.info("Querying MoMo transaction: {}", orderId);

        try {

            String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                    "&orderId=" + orderId +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&requestId=" + requestId;

            String signature = PaymentUtils.hmacSHA256(moMoConfig.getSecretKey(), rawSignature);


            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("requestId", requestId);
            requestBody.put("orderId", orderId);
            requestBody.put("lang", "vi");
            requestBody.put("signature", signature);


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

    public Map<String, Object> refundTransaction(String orderId, String requestId, Long amount, String description) {
        log.info("Refunding MoMo transaction: {}", orderId);

        try {
            String transId = PaymentUtils.getCurrentTimestamp();


            String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&description=" + description +
                    "&orderId=" + orderId +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&requestId=" + requestId +
                    "&transId=" + transId;

            String signature = PaymentUtils.hmacSHA256(moMoConfig.getSecretKey(), rawSignature);


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
