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

    // ── Khách hàng ───────────────────────────────────────────────────

    @GetMapping("/active")
    public APIResponse<List<PromotionResponse>> getActivePromotions() {
        return APIResponse.<List<PromotionResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách khuyến mãi khả dụng")
                .result(promotionService.getActivePromotions())
                .build();
    }

    @GetMapping("/{promoCode}")
    public APIResponse<PromotionResponse> getPromotionByCode(@PathVariable String promoCode) {
        return APIResponse.<PromotionResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin khuyến mãi")
                .result(promotionService.getPromotionByCode(promoCode))
                .build();
    }

    @PostMapping("/customer/{customerId}/save/{promoCode}")
    public APIResponse<Void> savePromotionForCustomer(@PathVariable String customerId, @PathVariable String promoCode) {
        promotionService.savePromotionForCustomer(customerId, promoCode);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Lưu khuyến mãi thành công")
                .build();
    }

    @GetMapping("/customer/{customerId}")
    public APIResponse<List<PromotionResponse>> getMyPromotions(@PathVariable String customerId) {
        return APIResponse.<List<PromotionResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Khuyến mãi của tôi")
                .result(promotionService.getMyPromotions(customerId))
                .build();
    }

    // ── Admin ────────────────────────────────────────────────────────

    /** GET /promotions – Lấy tất cả (Admin) */
    @GetMapping
    public APIResponse<List<PromotionResponse>> getAllPromotions() {
        return APIResponse.<List<PromotionResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Tất cả khuyến mãi")
                .result(promotionService.getAllPromotions())
                .build();
    }

    /** POST /promotions – Tạo mới (Admin) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<PromotionResponse> createPromotion(@Valid @RequestBody CreatePromotionRequest request) {
        return APIResponse.<PromotionResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo khuyến mãi thành công")
                .result(promotionService.createPromotion(request))
                .build();
    }

    /** PUT /promotions/{id} – Cập nhật (Admin) */
    @PutMapping("/{id}")
    public APIResponse<PromotionResponse> updatePromotion(
            @PathVariable String id,
            @Valid @RequestBody CreatePromotionRequest request) {
        return APIResponse.<PromotionResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật khuyến mãi thành công")
                .result(promotionService.updatePromotion(id, request))
                .build();
    }

    /** PATCH /promotions/{id}/toggle – Bật/tắt (Admin) */
    @PatchMapping("/{id}/toggle")
    public APIResponse<PromotionResponse> togglePromotion(@PathVariable String id) {
        return APIResponse.<PromotionResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đã thay đổi trạng thái khuyến mãi")
                .result(promotionService.toggleActive(id))
                .build();
    }

    /** DELETE /promotions/{id} – Xóa (Admin) */
    @DeleteMapping("/{id}")
    public APIResponse<Void> deletePromotion(@PathVariable String id) {
        promotionService.deletePromotion(id);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã xóa khuyến mãi")
                .build();
    }
}
