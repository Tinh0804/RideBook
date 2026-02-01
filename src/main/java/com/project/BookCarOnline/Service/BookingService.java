package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.CreateBookingRequest;
import com.project.BookCarOnline.DTO.Request.UpdateBookingRequest;
import com.project.BookCarOnline.DTO.Response.AvailableRideResponse;
import com.project.BookCarOnline.DTO.Response.BookingDetailResponse;
import com.project.BookCarOnline.Entity.*;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Booking Service
 * Converted from: ChuyenXeService.java (Servlet)
 * 
 * Handles ride booking operations:
 * - Create booking (đặt xe)
 * - Get available rides
 * - Assign driver to ride
 * - Cancel ride
 * - Complete ride
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    RideBookRepository bookingRepository;
    CustomerRepository customerRepository;
    DriverRepository driverRepository;

    /**
     * Create new booking
     * Converted from: ChuyenXeService.bookRide()
     */
    @Transactional
    public BookingDetailResponse createBooking(CreateBookingRequest request) {
        log.info("Creating new booking for customer: {}", request.getCustomerId());

        // Get customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Calculate price (basic calculation, can be enhanced)
        double totalPrice = request.getDistance() * 15000; // Default price per km

        // Create booking
        Booking booking = Booking.builder()
                .customerNo(customer)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .totalPrice(totalPrice)
                .bookingTime(Timestamp.valueOf(LocalDateTime.now()))
                .bookingStatus("Đang chờ") // Waiting for driver
                .distance(request.getDistance())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {}", savedBooking.getBookingId());

        return mapToBookingDetailResponse(savedBooking);
    }

    /**
     * Get all bookings
     * Converted from: ChuyenXeController.doGet()
     */
    public List<BookingDetailResponse> getAllBookings() {
        log.info("Fetching all bookings");
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::mapToBookingDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available rides (waiting for driver assignment)
     * Converted from: ChuyenXeService.getAvailableRides()
     */
    public List<AvailableRideResponse> getAvailableRides(String driverArea) {
        log.info("Fetching available rides for area: {}", driverArea);
        
        List<Booking> availableBookings;
        if (driverArea != null && !driverArea.isEmpty()) {
            // Extract city from driver address
            String city = DriverService.extractCityFromAddress(driverArea);
            availableBookings = bookingRepository.findByBookingStatusAndDriverNoIsNullAndPickupLocationContainingOrderByBookingTimeAsc("Đang chờ",city);
        } else {
            availableBookings = bookingRepository.findByBookingStatusAndDriverNoIsNullOrderByBookingTimeAsc("Đang chờ");
        }

        return availableBookings.stream()
                .map(this::mapToAvailableRideResponse)
                .collect(Collectors.toList());
    }

    /**
     * Assign driver to booking
     * Converted from: ChuyenXeService.ganTaiXeNhanDon()
     */
    @Transactional
    public BookingDetailResponse assignDriver(String bookingId, String driverId) {
        log.info("Assigning driver {} to booking {}", driverId, bookingId);

        // Get booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Check if booking is available
        if (!"Đang chờ".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Chuyến xe không còn khả dụng");
        }

        // Get driver
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Check if driver is already on another ride
        Booking ongoingRide = bookingRepository.findByDriverNo_DriverIdAndBookingStatus(driverId,"Đang thực hiện");
        if (ongoingRide != null) {
            throw new IllegalStateException("Tài xế đang thực hiện chuyến khác");
        }

        // Assign driver and update status
        booking.setDriverNo(driver);
        booking.setBookingStatus("Đang thực hiện");
        booking.setPickupTime(Timestamp.valueOf(LocalDateTime.now()));

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Driver assigned successfully to booking {}", bookingId);

        return mapToBookingDetailResponse(updatedBooking);
    }

    /**
     * Cancel booking
     * Converted from: ChuyenXeService.huyChuyenXe()
     */
    @Transactional
    public void cancelBooking(String bookingId) {
        log.info("Cancelling booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        booking.setBookingStatus("Đã huỷ");
        bookingRepository.save(booking);

        log.info("Booking cancelled successfully: {}", bookingId);
    }

    /**
     * Complete booking
     * Converted from driver API
     */
    @Transactional
    public BookingDetailResponse completeBooking(String bookingId) {
        log.info("Completing booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        if (!"Đang thực hiện".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Chuyến xe chưa được bắt đầu");
        }

        booking.setBookingStatus("Hoàn thành");
        booking.setArrivalTime(Timestamp.valueOf(LocalDateTime.now()));

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking completed successfully: {}", bookingId);

        return mapToBookingDetailResponse(updatedBooking);
    }

    /**
     * Get booking by ID
     */
    public BookingDetailResponse getBookingById(String bookingId) {
        log.info("Fetching booking by ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        return mapToBookingDetailResponse(booking);
    }

    /**
     * Get bookings by customer
     */
    public List<BookingDetailResponse> getBookingsByCustomer(String customerId) {
        log.info("Fetching bookings for customer: {}", customerId);
        List<Booking> bookings = bookingRepository.findByCustomerNo_CustomerIdOrderByBookingTimeDesc(customerId);
        return bookings.stream()
                .map(this::mapToBookingDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get bookings by driver
     */
    public List<BookingDetailResponse> getBookingsByDriver(String driverId) {
        log.info("Fetching bookings for driver: {}", driverId);
        List<Booking> bookings = bookingRepository.findByDriverNo_DriverIdOrderByBookingTimeDesc(driverId);
        return bookings.stream()
                .map(this::mapToBookingDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Booking entity to BookingDetailResponse
     */
    private BookingDetailResponse mapToBookingDetailResponse(Booking booking) {
        return BookingDetailResponse.builder()
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomerNo() != null ? booking.getCustomerNo().getCustomerId() : null)
                .customerName(booking.getCustomerNo() != null ? booking.getCustomerNo().getCustomerName() : null)
                .customerPhone(booking.getCustomerNo() != null ? booking.getCustomerNo().getPhone() : null)
                .driverId(booking.getDriverNo() != null ? booking.getDriverNo().getDriverId() : null)
                .driverName(booking.getDriverNo() != null ? booking.getDriverNo().getDriverName() : null)
                .driverPhone(booking.getDriverNo() != null ? booking.getDriverNo().getPhone() : null)
                .vehicleTypeName(booking.getDriverNo() != null && booking.getDriverNo().getVehicleType() != null 
                        ? booking.getDriverNo().getVehicleType().getVehicleTypeName() : null)
                .licensePlate(booking.getDriverNo() != null ? booking.getDriverNo().getLicensePlate() : null)
                .pickupLocation(booking.getPickupLocation())
                .dropoffLocation(booking.getDropoffLocation())
                .totalPrice(booking.getTotalPrice())
                .bookingTime(booking.getBookingTime())
                .pickupTime(booking.getPickupTime())
                .arrivalTime(booking.getArrivalTime())
                .bookingStatus(booking.getBookingStatus())
                .distance(booking.getDistance())
                .duration(booking.getDuration())
                .paymentMethod(booking.getPaymentNo() != null ? booking.getPaymentNo().getPaymentId() : null)
                .promotionCode(booking.getPromotionNo() != null ? booking.getPromotionNo().getPromotionId() : null)
                .build();
    }

    /**
     * Map Booking entity to AvailableRideResponse
     */
    private AvailableRideResponse mapToAvailableRideResponse(Booking booking) {
        return AvailableRideResponse.builder()
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomerNo() != null ? booking.getCustomerNo().getCustomerId() : null)
                .pickupLocation(booking.getPickupLocation())
                .dropoffLocation(booking.getDropoffLocation())
                .distance(booking.getDistance())
                .price(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .build();
    }
}
