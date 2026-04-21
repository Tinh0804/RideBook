package com.project.BookCarOnline.Service;

import com.google.maps.model.GeocodingResult;
import com.project.BookCarOnline.DTO.Request.CreateBookingRequest;
import com.project.BookCarOnline.DTO.Request.UpdateBookingRequest;
import com.project.BookCarOnline.DTO.Response.AvailableRideResponse;
import com.project.BookCarOnline.DTO.Response.BookingDetailResponse;
import com.project.BookCarOnline.DTO.Response.EstimatePriceResponse;
import com.project.BookCarOnline.Entity.*;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Entity.Enum.PaymentMethod;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.*;
import com.project.BookCarOnline.Entity.Payment;
import com.project.BookCarOnline.Service.PaymentTimeoutService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    RideBookRepository bookingRepository;
    CustomerRepository customerRepository;
    DriverRepository driverRepository;
    GoogleMapService googleMapService;
    RideDispatcherService dispatcherService;
    PaymentRepository paymentRepository;
    PaymentTimeoutService paymentTimeoutService;
    VehicleTypeRepository vehicleTypeRepository;
    VehicleTypeService vehicleTypeService;
    WalletService walletService;
    SimpMessagingTemplate messagingTemplate;

    @NonFinal
    @Value("${app.commission.platform-rate}")
    protected long platformCommissionRate;

    public EstimatePriceResponse estimatePrice(com.project.BookCarOnline.DTO.Request.EstimatePriceRequest request) {
        VehicleType vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        double basePrice = vehicleType.getPricePerKm() * request.getDistance();
        double surcharge = vehicleTypeService.getCurrentSurcharge(vehicleType.getVehicleTypeId());
        double surgeMultiplier = 1.0;

        // Note: Estimate price assumes basic surge (this can be calculated using real-time driver availability if needed)
        double finalPrice = Math.round(basePrice * surcharge * surgeMultiplier /1000.0 ) * 1000.0;

        return com.project.BookCarOnline.DTO.Response.EstimatePriceResponse.builder()
                .vehicleTypeId(vehicleType.getVehicleTypeId())
                .distance(request.getDistance())
                .basePrice(basePrice)
                .surcharge(surcharge)
                .surgeMultiplier(surgeMultiplier)
                .totalPrice(finalPrice)
                .build();
    }

    @Transactional
    public BookingDetailResponse createBooking(CreateBookingRequest request) {
        GeocodingResult result = googleMapService.geocode(request.getPickupLocation());
        double lat = result.geometry.location.lat;
        double lng = result.geometry.location.lng;


        List<Driver> availableDrivers = driverRepository.findTrulyAvailableDriversNearby(
                lat, lng, 3.0);

        if (availableDrivers.isEmpty()) {
            throw new AppException(ErrorCode.NO_DRIVER_AVAILABLE);
        }

        // 2. Thuật toán Surge Pricing (Tăng giá động)
        double surgeMultiplier = 1.0;
        if (availableDrivers.size() == 0) {
            throw new AppException(ErrorCode.NO_DRIVER_AVAILABLE);
        } else if (availableDrivers.size() < 3) {
            surgeMultiplier = 1.3; // Tăng 30% giá nếu chỉ có dưới 3 tài xế rảnh
        }

        VehicleType vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        double basePrice = vehicleType.getPricePerKm() * request.getDistance();
        double surcharge = vehicleTypeService.getCurrentSurcharge(vehicleType.getVehicleTypeId());
        double finalPrice = Math.round(basePrice * surcharge * surgeMultiplier / 1000.0) *1000.0; // Làm tròn đến 0 chữ số thập phân


        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // 3. Lưu Booking với giá đã tính
        String method = request.getPaymentMethod() != null ? request.getPaymentMethod() : "ONLINE";
        boolean cash = PaymentMethod.CASH.name().equalsIgnoreCase(method);


        Payment payment = Payment.builder()
                .paymentType(cash ?  PaymentMethod.CASH.name() : "ONLINE")
                .amount(finalPrice)
                .paymentStatus(cash)
                .build();
        Payment savedPayment = paymentRepository.save(payment);

        Booking booking = Booking.builder()
                .vehicleTypeNo(vehicleType)
                .customerNo(customer)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .totalPrice(finalPrice)
                .bookingTime(Timestamp.valueOf(LocalDateTime.now()))
                .bookingStatus(BookingStatus.PENDING) // Waiting for driver
                .distance(request.getDistance())
                .paymentNo(savedPayment)
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        if (cash) {
            // Cash payments can be dispatched immediately
            List<Driver> candidates = driverRepository.findTrulyAvailableDriversNearby(lat, lng, 5.0);
            dispatcherService.startDispatching(savedBooking.getBookingId(), candidates);
        } else {
            // Schedule timeout: cancel if not paid within 10 minutes
            paymentTimeoutService.schedulePaymentTimeout(savedBooking.getBookingId(), 10 * 60 * 1000L);
        }

        return mapToBookingDetailResponse(savedBooking);
    }



    public List<BookingDetailResponse> getAllBookings() {
        log.info("Fetching all bookings");
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::mapToBookingDetailResponse)
                .collect(Collectors.toList());
    }


    public List<AvailableRideResponse> getAvailableRides(String driverArea) {
        log.info("Fetching available rides for area: {}", driverArea);
        
        List<Booking> availableBookings;
        if (driverArea != null && !driverArea.isEmpty()) {
            // Extract city from driver address
            String city = DriverService.extractCityFromAddress(driverArea);
            availableBookings = bookingRepository.findByBookingStatusAndDriverNoIsNullAndPickupLocationContainingOrderByBookingTimeAsc(BookingStatus.PENDING,city);
        } else {
            availableBookings = bookingRepository.findByBookingStatusAndDriverNoIsNullOrderByBookingTimeAsc(BookingStatus.PENDING);
        }

        return availableBookings.stream()
                .map(this::mapToAvailableRideResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public BookingDetailResponse assignDriver(String bookingId, String driverId) {
        log.info("Assigning driver {} to booking {}", driverId, bookingId);

        // Get booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Check if booking is available
        if (!BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Chuyến xe không còn khả dụng");
        }

        // Get driver
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Check if driver is already on another ride
        Booking ongoingRide = bookingRepository.findByDriverNo_DriverIdAndBookingStatus(driverId, BookingStatus.ACCEPTED);
        if (ongoingRide != null) {
            throw new IllegalStateException("Tài xế đang thực hiện chuyến khác");
        }

        // Assign driver and update status
        booking.setDriverNo(driver);
        booking.setBookingStatus(BookingStatus.ACCEPTED);
        booking.setPickupTime(Timestamp.valueOf(LocalDateTime.now()));

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Driver assigned successfully to booking {}", bookingId);

        return mapToBookingDetailResponse(updatedBooking);
    }

    @Transactional
    public BookingDetailResponse updateStatus(String bookingId, BookingStatus newStatus){

        log.info("Updating booking status for booking {}: {}", bookingId,newStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        BookingStatus currentStatus = booking.getBookingStatus();

        boolean isValidTransition = switch (newStatus){
            case ARRIVED -> currentStatus == BookingStatus.ACCEPTED;
            case IN_PROGRESS -> currentStatus == BookingStatus.ARRIVED;
            case COMPLETED -> currentStatus == BookingStatus.IN_PROGRESS;
            case CANCELLED   -> currentStatus == BookingStatus.PENDING || currentStatus == BookingStatus.ACCEPTED;
            default -> false;
        };

        if (!isValidTransition){
            throw new IllegalStateException("Không thể chuyển trạng thái từ " + currentStatus + " sang " + newStatus);
        }
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        switch (newStatus){
            case ARRIVED -> booking.setPickupTime(now);
            case COMPLETED -> {
                booking.setArrivalTime(now);
                if (booking.getPaymentNo() != null && "ONLINE".equalsIgnoreCase(booking.getPaymentNo().getPaymentType())) {
                    walletService.addBalance(booking.getDriverNo().getDriverId(), booking.getTotalPrice()*(1 - platformCommissionRate));
                }
                else{
                    walletService.deductBalance(booking.getDriverNo().getDriverId(), booking.getTotalPrice()*(platformCommissionRate));
                }
            }
        }
        booking.setBookingStatus(newStatus);
        
        Booking updatedBooking = bookingRepository.save(booking);

        // Phát thông báo trạng thái qua WebSocket đến Khách Hàng
        if (updatedBooking.getCustomerNo() != null) {
            String payload = "STATUS_UPDATE:" + bookingId + ":" + newStatus.name();
            messagingTemplate.convertAndSend("/topic/customer/" + updatedBooking.getCustomerNo().getCustomerId(), payload);
        }

        log.info("Booking status updated successfully for booking {}: {}", bookingId, newStatus);
        return mapToBookingDetailResponse(updatedBooking);
    }


    @Transactional
    public void cancelBooking(String bookingId) {
        log.info("Cancelling booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("Booking cancelled successfully: {}", bookingId);
    }

    @Transactional
    public BookingDetailResponse completeBooking(String bookingId) {
        log.info("Completing booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        if (!BookingStatus.ACCEPTED.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Chuyến xe chưa được bắt đầu");
        }

        booking.setBookingStatus(BookingStatus.COMPLETED);
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
                // .duration(booking.getDuration())
                .paymentMethod(booking.getPaymentNo() != null ? booking.getPaymentNo().getPaymentType() : null)
                .paymentStatus(booking.getPaymentNo() != null ? booking.getPaymentNo().getPaymentStatus() : null)
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
