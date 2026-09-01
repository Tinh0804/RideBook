package com.project.BookCarOnline.booking.controller;

import com.project.BookCarOnline.booking.dto.request.AdminBookingSearchRequest;
import com.project.BookCarOnline.booking.dto.request.AdminBookingFilter;
import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.booking.dto.response.BookingDetailResponse;
import com.project.BookCarOnline.booking.service.BookingService;
import com.project.BookCarOnline.booking.service.BookingQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springdoc.core.annotations.ParameterObject;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin Bookings", description = "Tìm kiếm, lọc và export booking dành cho admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminBookingController {

    BookingService bookingService;
    BookingQueryService bookingQueryService;

    @GetMapping
    @Operation(
            operationId = "searchAdminBookings",
            summary = "Search and filter bookings",
            description = "Phân trang, multi-field sort và kết hợp các bộ lọc booking dành cho admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching bookings"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or sort"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    public APIResponse<Page<BookingDetailResponse>> searchBookings(
            @Valid @ParameterObject AdminBookingSearchRequest request) {
        log.info("REST API: GET /admin/bookings page={} size={}", request.getPage(), request.getSize());
        Page<BookingDetailResponse> result = bookingQueryService.searchBookingsForAdmin(request);
        return APIResponse.<Page<BookingDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách chuyến đi")
                .result(result)
                .build();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(
            operationId = "exportAdminBookings",
            summary = "Export filtered bookings as UTF-8 CSV",
            description = "Dùng cùng filter và sort với search; page và size bị bỏ qua.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UTF-8 CSV attachment"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or sort"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    public ResponseEntity<StreamingResponseBody> exportBookings(
            @Valid @ParameterObject AdminBookingFilter request) {
        StreamingResponseBody body = outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                bookingQueryService.writeAdminBookingsCsv(request, writer);
            }
        };
        String filename = "bookings-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(body);
    }

    @GetMapping("/{bookingId}")
    public APIResponse<BookingDetailResponse> getBookingDetail(@PathVariable String bookingId) {
        log.info("REST API: GET /admin/bookings/{}", bookingId);
        BookingDetailResponse result = bookingQueryService.getBookingById(bookingId);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Chi tiết chuyến đi")
                .result(result)
                .build();
    }

    @DeleteMapping("/{bookingId}")
    public APIResponse<BookingDetailResponse> forceCancel(@PathVariable String bookingId) {
        log.info("REST API: DELETE /admin/bookings/{} - Admin force cancel", bookingId);
        BookingDetailResponse result = bookingService.adminForceCancel(bookingId);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đã huỷ chuyến đi")
                .result(result)
                .build();
    }

    @PutMapping("/{bookingId}/assign-driver")
    public APIResponse<BookingDetailResponse> assignDriver(
            @PathVariable String bookingId,
            @RequestParam String driverId) {
        log.info("REST API: PUT /admin/bookings/{}/assign-driver?driverId={}", bookingId, driverId);
        BookingDetailResponse result = bookingService.adminAssignDriver(bookingId, driverId);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đã gán tài xế thành công")
                .result(result)
                .build();
    }

    @GetMapping("/summary")
    public APIResponse<Map<String, Object>> getSummary() {
        log.info("REST API: GET /admin/bookings/summary");
        return APIResponse.<Map<String, Object>>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê chuyến đi")
                .result(bookingQueryService.getAdminSummary())
                .build();
    }
}
