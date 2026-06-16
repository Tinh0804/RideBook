package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Response.WalletResponse;
import com.project.BookCarOnline.DTO.Response.WalletTransactionResponse;
import com.project.BookCarOnline.Service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminWalletController {
    WalletService walletService;

    @GetMapping("/driver/{driverId}")
    public APIResponse<WalletResponse> getAdminWalletBalance(@PathVariable String driverId){
        WalletResponse balance = walletService.getAdminWalletBalance(driverId);
        return APIResponse.<WalletResponse>builder()
                .result(balance)
                .message("Balance retrieved successfully")
                .build();
    }

    @GetMapping("/driver/{driverId}/transactions")
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

    @PostMapping("/driver/{driverId}/adjust")
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
