package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.CreatePromotionRequest;
import com.project.BookCarOnline.DTO.Response.PromotionResponse;
import com.project.BookCarOnline.Service.PromotionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionController {

    PromotionService promotionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<PromotionResponse> createPromotion(@Valid @RequestBody CreatePromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(request);
        return APIResponse.<PromotionResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo khuyến mãi thành công")
                .result(response)
                .build();
    }

    @GetMapping("/active")
    public APIResponse<List<PromotionResponse>> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return APIResponse.<List<PromotionResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách khuyến mãi khả dụng")
                .result(promotions)
                .build();
    }

    @GetMapping("/{promoCode}")
    public APIResponse<PromotionResponse> getPromotionByCode(@PathVariable String promoCode) {
        PromotionResponse promotion = promotionService.getPromotionByCode(promoCode);
        return APIResponse.<PromotionResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin khuyến mãi")
                .result(promotion)
                .build();
    }
}
