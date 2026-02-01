package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.CreateBookingRequest;
import com.project.BookCarOnline.DTO.Request.UpdateBookingRequest;
import com.project.BookCarOnline.DTO.Response.AvailableRideResponse;
import com.project.BookCarOnline.DTO.Response.BookingDetailResponse;
import com.project.BookCarOnline.Service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Booking Controller
 * Converted from: ChuyenXeController.java (Servlet)
 * 
 * Original endpoints:
 * - GET  /api-rides              -> Get all rides
 * - GET  /api-rides/available    -> Get available rides
 * - POST /api-rides              -> Create booking
 * - PUT  /api-rides              -> Assign driver
 * - DELETE /api-rides            -> Cancel booking
 * 
 * New REST endpoints:
 * - GET    /bookings                    -> Get all bookings
 * - GET    /bookings/{id}               -> Get booking by ID
 * - POST   /bookings                    -> Create booking
 * - PUT    /bookings/{id}/assign-driver -> Assign driver to booking
 * - PUT    /bookings/{id}/complete      -> Complete booking
 * - DELETE /bookings/{id}               -> Cancel booking
 * - GET    /bookings/available          -> Get available rides
 * - GET    /bookings/customer/{customerId} -> Get customer bookings
 * - GET    /bookings/driver/{driverId}  -> Get driver bookings
 */
@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingService bookingService;

    /**
     * Get all bookings
     * Converted from: ChuyenXeController.doGet()
     */
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<List<BookingDetailResponse>> getAllBookings() {
        log.info("REST API: GET /bookings - Fetching all bookings");
        List<BookingDetailResponse> bookings = bookingService.getAllBookings();
        return APIResponse.<List<BookingDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tất cả chuyến xe")
                .result(bookings)
                .build();
    }

    /**
     * Get booking by ID
     */
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

    /**
     * Get available rides for drivers
     * Converted from: ChuyenXeController.doGet() with path /api-rides/available
     */
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

    /**
     * Get bookings by customer
     */
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

    /**
     * Get bookings by driver
     */
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

    /**
     * Create new booking
     * Converted from: ChuyenXeController.doPost()
     */
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

    /**
     * Assign driver to booking
     * Converted from: ChuyenXeController.doPut()
     */
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

    /**
     * Complete booking
     */
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

    /**
     * Cancel booking
     * Converted from: ChuyenXeController.doDelete()
     */
    @DeleteMapping("/{bookingId}")
    public APIResponse<Void> cancelBooking(@PathVariable String bookingId) {
        log.info("REST API: DELETE /bookings/{} - Cancelling booking", bookingId);
        bookingService.cancelBooking(bookingId);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Huỷ chuyến thành công")
                .build();
    }
}
