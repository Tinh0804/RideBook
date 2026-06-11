package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.CreateBookingRequest;
import com.project.BookCarOnline.DTO.Request.UpdateBookingRequest;
import com.project.BookCarOnline.DTO.Response.AvailableRideResponse;
import com.project.BookCarOnline.DTO.Response.BookingDetailResponse;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingService bookingService;

    @GetMapping
    public APIResponse<List<BookingDetailResponse>> getAllBookings() {
        log.info("REST API: GET /bookings - Fetching all bookings");
        List<BookingDetailResponse> bookings = bookingService.getAllBookings();
        return APIResponse.<List<BookingDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tất cả chuyến xe")
                .result(bookings)
                .build();
    }


    @GetMapping("/{bookingId}")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<BookingDetailResponse> getBookingById(@PathVariable String bookingId) {
        log.info("REST API: GET /bookings/{} - Fetching booking by ID", bookingId);
        BookingDetailResponse booking = bookingService.getBookingById(bookingId);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin chuyến xe")
                .result(booking)
                .build();
    }


    @GetMapping("/available")
    public APIResponse<List<AvailableRideResponse>> getAvailableRides(
            @RequestParam(required = false) String driverArea) {
        log.info("REST API: GET /bookings/available?driverArea={} - Fetching available rides", driverArea);
        List<AvailableRideResponse> rides = bookingService.getAvailableRides(driverArea);
        return APIResponse.<List<AvailableRideResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách chuyến xe khả dụng")
                .result(rides)
                .build();
    }


    @GetMapping("/customer/{customerId}")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<List<BookingDetailResponse>> getBookingsByCustomer(@PathVariable String customerId) {
        log.info("REST API: GET /bookings/customer/{} - Fetching bookings by customer", customerId);
        List<BookingDetailResponse> bookings = bookingService.getBookingsByCustomer(customerId);
        return APIResponse.<List<BookingDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách chuyến xe của khách hàng")
                .result(bookings)
                .build();
    }

    @GetMapping("/customer/{customerId}/active")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<BookingDetailResponse> getActiveBookingByCustomer(@PathVariable String customerId) {
        log.info("REST API: GET /bookings/customer/{}/active - Fetching active booking by customer", customerId);
        BookingDetailResponse booking = bookingService.getActiveBookingByCustomer(customerId);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Chuyến xe đang hoạt động của khách hàng")
                .result(booking)
                .build();
    }

    @GetMapping("/driver/{driverId}")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<List<BookingDetailResponse>> getBookingsByDriver(@PathVariable String driverId) {
        log.info("REST API: GET /bookings/driver/{} - Fetching bookings by driver", driverId);
        List<BookingDetailResponse> bookings = bookingService.getBookingsByDriver(driverId);
        return APIResponse.<List<BookingDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách chuyến xe của tài xế")
                .result(bookings)
                .build();
    }

    @GetMapping("/driver/{driverId}/active")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<BookingDetailResponse> getActiveBookingByDriver(@PathVariable String driverId) {
        log.info("REST API: GET /bookings/driver/{}/active - Fetching active booking by driver", driverId);
        BookingDetailResponse booking = bookingService.getActiveBookingByDriver(driverId);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Chuyến xe đang hoạt động của tài xế")
                .result(booking)
                .build();
    }

    @GetMapping("/driver/{driverId}/page")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Page<BookingDetailResponse>> getBookingsByDriverPaginated(
            @PathVariable String driverId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST API: GET /bookings/driver/{}/page?status={}&page={}&size={} - Fetching bookings paginated", driverId, status, page, size);
       Page<BookingDetailResponse> bookings = bookingService.getBookingsByDriverPaginated(driverId, status, page, size);
        return APIResponse.<org.springframework.data.domain.Page<BookingDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách chuyến xe của tài xế (có phân trang)")
                .result(bookings)
                .build();
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<BookingDetailResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        log.info("REST API: POST /bookings - Creating new booking for customer: {}", request.getCustomerId());
        BookingDetailResponse booking = bookingService.createBooking(request);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Đã đặt xe thành công!")
                .result(booking)
                .build();
    }

    @PutMapping("/{bookingId}/assign-driver")
    public APIResponse<BookingDetailResponse> assignDriver(
            @PathVariable String bookingId,
            @RequestParam String driverId) {
        log.info("REST API: PUT /bookings/{}/assign-driver?driverId={} - Assigning driver to booking", 
                bookingId, driverId);
        BookingDetailResponse booking = bookingService.assignDriver(bookingId, driverId);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đã gán tài xế thành công")
                .result(booking)
                .build();
    }

    @PutMapping("/{bookingId}/status")
    public APIResponse<BookingDetailResponse> updateBookingStatus(
            @PathVariable String bookingId,
            @RequestParam String status) {
        log.info("REST API: PUT /bookings/{}/status/{} - Updating booking status", bookingId, status);
        BookingDetailResponse booking = bookingService.updateStatus(bookingId, BookingStatus.valueOf(status));
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật trạng thái chuyến xe thành công")
                .result(booking)
                .build();
    }

    @PutMapping("/{bookingId}/complete")
    public APIResponse<BookingDetailResponse> completeBooking(@PathVariable String bookingId) {
        log.info("REST API: PUT /bookings/{}/complete - Completing booking", bookingId);
        BookingDetailResponse booking = bookingService.completeBooking(bookingId);
        return APIResponse.<BookingDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Hoàn thành chuyến xe thành công")
                .result(booking)
                .build();
    }


    @DeleteMapping("/{bookingId}")
    public APIResponse<Void> cancelBooking(@PathVariable String bookingId) {
        log.info("REST API: DELETE /bookings/{} - Cancelling booking", bookingId);
        bookingService.cancelBooking(bookingId);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã hủy chuyến xe thành công")
                .build();
    }

    @DeleteMapping("/{bookingId}/driver/{driverId}")
    public APIResponse<Void> cancelBookingByDriver(@PathVariable String bookingId, @PathVariable String driverId) {
        log.info("REST API: DELETE /bookings/{}/driver/{} - Driver cancelling booking", bookingId, driverId);
        bookingService.cancelBookingByDriver(bookingId, driverId);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Tài xế đã hủy chuyến xe thành công")
                .build();
    }

    @PostMapping("/{bookingId}/reject")
    public APIResponse<Void> rejectBooking(
            @PathVariable String bookingId,
            @RequestParam String driverId) {
        log.info("REST API: POST /bookings/{}/reject?driverId={} - Driver rejecting booking", bookingId, driverId);
        bookingService.rejectBooking(bookingId, driverId);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã từ chối chuyến xe")
                .build();
    }

    @PostMapping("/estimate-price")
    public APIResponse<List<com.project.BookCarOnline.DTO.Response.EstimatePriceResponse>> estimatePrice(@Valid @RequestBody com.project.BookCarOnline.DTO.Request.EstimatePriceRequest request) {
        log.info("REST API: POST /bookings/estimate-price - Estimating price for all vehicles");
        return APIResponse.<List<com.project.BookCarOnline.DTO.Response.EstimatePriceResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy giá ước tính thành công")
                .result(bookingService.estimatePrice(request))
                .build();
    }

    // ── Admin endpoints ──────────────────────────────────────────────

    /** GET /bookings/admin/all – Lấy toàn bộ booking (Admin) */
    @GetMapping("/admin/all")
    public APIResponse<List<BookingDetailResponse>> adminGetAll() {
        log.info("REST API: GET /bookings/admin/all");
        return APIResponse.<List<BookingDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Toàn bộ chuyến xe")
                .result(bookingService.getAllBookings())
                .build();
    }

    /** GET /bookings/admin/summary – Thống kê tổng quan (Admin Dashboard) */
    @GetMapping("/admin/summary")
    public APIResponse<java.util.Map<String, Object>> adminSummary() {
        log.info("REST API: GET /bookings/admin/summary");
        return APIResponse.<java.util.Map<String, Object>>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê tổng quan")
                .result(bookingService.getAdminSummary())
                .build();
    }

}
