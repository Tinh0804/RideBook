package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.PaymentRequest;
import com.project.BookCarOnline.DTO.Response.PaymentResponse;
import com.project.BookCarOnline.DTO.Response.WalletResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole("+ PredefinedRole.RoleName.DRIVER +")")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    WalletService walletService;
    VNPayService vnPayService;
    MoMoService moMoService;

    @GetMapping("/my-wallet")
    public APIResponse<WalletResponse> getMyBalance(){
        WalletResponse balance = walletService.getMyBlance();
        return APIResponse.<WalletResponse>builder()
                .result(balance)
                .message("Balance retrieved successfully")
                .build();
    }

    @PostMapping("/deposit")
    public APIResponse<PaymentResponse> requestDeposit(@RequestBody PaymentRequest request) {
        String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(()->new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL));
        Double amount = request.getAmount();

        // 1. Lưu DB trạng thái PENDING
        WalletTransaction txn = walletService.createDepositRequest(driverId, amount);

        PaymentResponse paymentResponse =  switch (request.getMethod()) {
            case VNPAY -> vnPayService.createPayment(request);
            case MOMO -> moMoService.createPayment(request);
            default -> throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        };

        return APIResponse.<PaymentResponse>builder()
                .result(paymentResponse)
                .message("Yêu cầu nạp tiền đã được tạo. Vui lòng hoàn tất thanh toán.")
                .build();

    }

    /**
     * 3. Rút tiền tự động (Trừ tiền và chuyển thành SUCCESS ngay)
     * API: POST /api/wallets/withdraw
     * Body: { "driverId": "...", "amount": 500000 }
     */
    @PostMapping("/withdraw")
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

    /**
     * 4. API IPN (Webhook) dành riêng cho VNPay gọi về (KHÔNG PHẢI APP GỌI)
     * API: GET /api/wallets/vnpay-ipn
     */
    @GetMapping("/vnpay-ipn")
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
}
