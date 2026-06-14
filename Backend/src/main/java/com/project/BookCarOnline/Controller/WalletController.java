package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.PaymentRequest;
import com.project.BookCarOnline.DTO.Response.PaymentResponse;
import com.project.BookCarOnline.DTO.Response.WalletResponse;
import com.project.BookCarOnline.DTO.Response.WalletTransactionResponse;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.WalletTransaction;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Service.MoMoService;
import com.project.BookCarOnline.Service.VNPayService;
import com.project.BookCarOnline.Service.WalletService;
import com.project.BookCarOnline.Utils.SecurityUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    WalletService walletService;
    VNPayService vnPayService;
    MoMoService moMoService;

    @GetMapping("/my-wallet")
    @PreAuthorize(PredefinedRole.HAS_ROLE_DRIVER)
    public APIResponse<WalletResponse> getMyBalance(){
        WalletResponse balance = walletService.getMyBlance();
        return APIResponse.<WalletResponse>builder()
                .result(balance)
                .message("Balance retrieved successfully")
                .build();
    }



    @PostMapping("/deposit")
    @PreAuthorize(PredefinedRole.HAS_ROLE_DRIVER)
    public APIResponse<PaymentResponse> requestDeposit(@RequestBody PaymentRequest request) {
        String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(()->new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL));
        Double amount = request.getAmount();

        // 1. Lưu DB trạng thái PENDING
        WalletTransaction txn = walletService.createDepositRequest(driverId, amount);

        PaymentResponse paymentResponse =  switch (request.getMethod()) {
            case VNPAY -> vnPayService.createTopUpPayment(driverId,request.getAmount(),request.getReturnUrl(),txn.getTransactionId());
            case MOMO -> moMoService.createTopUpPayment(driverId,request.getAmount(),request.getReturnUrl(),txn.getTransactionId());
            default -> throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        };

        return APIResponse.<PaymentResponse>builder()
                .result(paymentResponse)
                .message("Yêu cầu nạp tiền đã được tạo. Vui lòng hoàn tất thanh toán.")
                .build();

    }

    @PostMapping("/withdraw")
    @PreAuthorize(PredefinedRole.HAS_ROLE_DRIVER)
    public APIResponse<?> autoWithdraw(@RequestParam Double amount) {
            String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(()->new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL));

            // Gọi hàm rút tự động từ Service
            WalletTransaction txn = walletService.autoWithdraw(driverId, amount);

            Map<String, Object> response = new HashMap<>();

            response.put("transactionId", txn.getTransactionId());
            response.put("amount", txn.getAmount());
            response.put("newBalance", txn.getWallet().getBalance());

            return APIResponse.builder()
                    .status(200)
                    .message("Rút tiền thành công. Tiền sẽ được chuyển vào tài khoản ngân hàng của bạn.")
                    .result(response)
                    .build();
    }

    @GetMapping("/history-transactions")
    @PreAuthorize(PredefinedRole.HAS_ROLE_DRIVER)
    public APIResponse<Page<WalletTransactionResponse>> getTransactionHistory(
            @RequestParam String walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(()->new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL));
        Page<WalletTransactionResponse> response = walletService.getTransactionHistory(driverId, walletId, page, size);
        return APIResponse.<org.springframework.data.domain.Page<WalletTransactionResponse>>builder()
                .result(response)
                .message("Lịch sử giao dịch retrieved successfully")
                .build();
    }

    @GetMapping("/vnpay-ipn")
    // public API - no auth needed
    public APIResponse<?> vnPayIPN(
            @RequestParam("vnp_TxnRef") String transactionId,
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TransactionNo") String vnpTransactionNo) {

        try {
            // 00 là mã thành công của VNPay
            boolean isSuccess = "00".equals(responseCode);

            // Xử lý cập nhật số dư
            walletService.processPaymentCallback(transactionId, isSuccess, vnpTransactionNo);

            // Phải trả về format chuẩn này thì VNPay mới hiểu là bạn đã nhận được IPN
            return APIResponse.builder()
                    .result(Map.of("RspCode", "00", "Message", "Confirm Success"))
                    .build();
        } catch (Exception e) {
            return APIResponse.builder().result(Map.of("RspCode", "99", "Message", "Unknown Error"))
                    .build();
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/driver/{driverId}")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<WalletResponse> getAdminWalletBalance(@PathVariable String driverId){
        WalletResponse balance = walletService.getAdminWalletBalance(driverId);
        return APIResponse.<WalletResponse>builder()
                .result(balance)
                .message("Balance retrieved successfully")
                .build();
    }

    @GetMapping("/admin/driver/{driverId}/transactions")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Page<WalletTransactionResponse>> getAdminTransactionHistory(
            @PathVariable String driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<WalletTransactionResponse> response = walletService.getAdminTransactionHistory(driverId, page, size);
        return APIResponse.<Page<WalletTransactionResponse>>builder()
                .result(response)
                .message("Transaction history retrieved successfully")
                .build();
    }

    @PostMapping("/admin/driver/{driverId}/adjust")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<WalletTransactionResponse> adjustBalanceAdmin(
            @PathVariable String driverId,
            @RequestParam Double amount,
            @RequestParam String reason) {
        WalletTransactionResponse txn = walletService.adjustBalanceAdmin(driverId, amount, reason);
        return APIResponse.<WalletTransactionResponse>builder()
                .result(txn)
                .message("Balance adjusted successfully")
                .build();
    }

}
