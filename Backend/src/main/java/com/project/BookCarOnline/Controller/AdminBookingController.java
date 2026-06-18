package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Response.BookingDetailResponse;
import com.project.BookCarOnline.Service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminBookingController {

    BookingService bookingService;

    @GetMapping
    public APIResponse<Page<BookingDetailResponse>> searchBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        log.info("REST API: GET /admin/bookings?page={}&size={}&status={}&search={}&from={}&to={}",
                page, size, status, search, fromDate, toDate);
        Page<BookingDetailResponse> result = bookingService.searchBookingsForAdmin(page, size, status, search, fromDate, toDate);
        return APIResponse.<Page<BookingDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách chuyến đi")
                .result(result)
                .build();
    }

    @GetMapping("/{bookingId}")
    public APIResponse<BookingDetailResponse> getBookingDetail(@PathVariable String bookingId) {
        log.info("REST API: GET /admin/bookings/{}", bookingId);
        BookingDetailResponse result = bookingService.getBookingById(bookingId);
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
                .result(bookingService.getAdminSummary())
                .build();
    }
}
