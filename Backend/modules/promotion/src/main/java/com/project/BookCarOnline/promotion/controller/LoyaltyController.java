package com.project.BookCarOnline.promotion.controller;

import com.project.BookCarOnline.promotion.entity.LoyaltyAccount;
import com.project.BookCarOnline.promotion.entity.PointTransaction;
import com.project.BookCarOnline.promotion.service.LoyaltyService;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;

@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty System", description = "Points and Tier Management API")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @Operation(summary = "Get current loyalty account info")
    @PreAuthorize(PredefinedRole.HAS_ROLE_CUSTOMER + " or " + PredefinedRole.HAS_ROLE_DRIVER)
    @GetMapping("/my-account")
    public APIResponse<LoyaltyAccount> getMyAccount() {
        String accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        
        return APIResponse.<LoyaltyAccount>builder()
                .result(loyaltyService.getOrCreateAccount(accountId))
                .build();
    }

    @Operation(summary = "Get transaction history for points/coins")
    @PreAuthorize(PredefinedRole.HAS_ROLE_CUSTOMER + " or " + PredefinedRole.HAS_ROLE_DRIVER)
    @GetMapping("/my-history")
    public APIResponse<Page<PointTransaction>> getMyHistory(
            @PageableDefault(size = 20) Pageable pageable) {
            
        String accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        
        return APIResponse.<Page<PointTransaction>>builder()
                .result(loyaltyService.getTransactionHistory(accountId, pageable))
                .build();
    }

    @Operation(summary = "Get loyalty configuration")
    @GetMapping("/config")
    public APIResponse<com.project.BookCarOnline.promotion.config.LoyaltyProperties> getConfig() {
        return APIResponse.<com.project.BookCarOnline.promotion.config.LoyaltyProperties>builder()
                .result(loyaltyService.getLoyaltyProperties())
                .build();
    }
}
