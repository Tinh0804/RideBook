package com.project.BookCarOnline.app.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.finance.dto.request.PaymentRequest;
import com.project.BookCarOnline.finance.dto.response.PaymentCallbackResponse;
import com.project.BookCarOnline.finance.dto.response.PaymentResponse;
import com.project.BookCarOnline.finance.dto.response.WalletResponse;
import com.project.BookCarOnline.finance.dto.response.WalletTransactionResponse;
import com.project.BookCarOnline.finance.dto.WalletTransactionResult;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.app.service.payment.MoMoService;
import com.project.BookCarOnline.app.service.payment.VNPayService;
import com.project.BookCarOnline.finance.service.WalletService;
import com.project.BookCarOnline.shared.security.SecurityUtils;
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
        WalletTransactionResult txn = walletService.createDepositRequest(driverId, amount);

        PaymentResponse paymentResponse =  switch (request.getMethod()) {
            case VNPAY -> vnPayService.createTopUpPayment(driverId,request.getAmount(),request.getReturnUrl(),txn.transactionId());
            case MOMO -> moMoService.createTopUpPayment(driverId,request.getAmount(),request.getReturnUrl(),txn.transactionId());
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
            WalletTransactionResult txn = walletService.autoWithdraw(driverId, amount);

            Map<String, Object> response = new HashMap<>();

            response.put("transactionId", txn.transactionId());
            response.put("amount", txn.amount());
            response.put("newBalance", txn.newBalance());

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
    public APIResponse<?> vnPayIPN(@RequestParam Map<String, String> params) {
        try {
            PaymentCallbackResponse callback = vnPayService.handleCallback(params);
            if (callback.getOrderId() == null) {
                return APIResponse.builder()
                        .result(Map.of("RspCode", "97", "Message", "Invalid Signature"))
                        .build();
            }
            return APIResponse.builder()
                    .result(Map.of("RspCode", "00", "Message", "Confirm Success"))
                    .build();
        } catch (RuntimeException exception) {
            return APIResponse.builder()
                    .result(Map.of("RspCode", "99", "Message", "Unknown Error"))
                    .build();
        }
    }

}
