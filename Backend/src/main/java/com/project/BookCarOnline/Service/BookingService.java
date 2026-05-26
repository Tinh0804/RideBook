package com.project.BookCarOnline.Service;

import com.google.maps.model.GeocodingResult;
import com.project.BookCarOnline.DTO.Request.CreateBookingRequest;
import com.project.BookCarOnline.DTO.Request.EstimatePriceRequest;
import com.project.BookCarOnline.DTO.Request.UpdateBookingRequest;
import com.project.BookCarOnline.DTO.Response.AvailableRideResponse;
import com.project.BookCarOnline.DTO.Response.BookingDetailResponse;
import com.project.BookCarOnline.DTO.Response.EstimatePriceResponse;
import com.project.BookCarOnline.Entity.*;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Entity.Enum.PaymentMethod;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Entity.Enum.DiscountType;
import com.project.BookCarOnline.Entity.Enum.CustomerPromotionStatus;
import com.project.BookCarOnline.Entity.Enum.RejectionType;
import com.project.BookCarOnline.Entity.CustomerPromotion;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.*;
import com.project.BookCarOnline.Entity.Payment;
import com.project.BookCarOnline.DTO.Redis.FareQuote;
import org.springframework.data.redis.core.RedisTemplate;
import com.project.BookCarOnline.Utils.Constant;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    RideBookRepository        bookingRepository;
    CustomerRepository        customerRepository;
    DriverRepository          driverRepository;
    GoogleMapService          googleMapService;
    RideDispatcherService     dispatcherService;
    RatingRepository          ratingRepository;
    PaymentRepository         paymentRepository;
    PaymentTimeoutService     paymentTimeoutService;
    VehicleTypeRepository     vehicleTypeRepository;
    VehicleTypeService        vehicleTypeService;
    WalletService             walletService;
    SimpMessagingTemplate     messagingTemplate;
    Constant                  constant;
    BookingRejectionRepository rejectionRepository;
    PromotionRepository       promotionRepository;
    com.project.BookCarOnline.Repository.CustomerPromotionRepository customerPromotionRepository;
    RedisTemplate<String, Object> redisTemplate;

    @NonFinal
    @Value("${app.commission.platform-rate}")
    protected double platformCommissionRate;

    @NonFinal
    @Value("${app.time-to-live.quote}")
    protected long quoteTtlMillis;

    public List<EstimatePriceResponse> estimatePrice(EstimatePriceRequest request) {
        
        double distance = calculateDistanceKm(request.getPickupLat(), request.getPickupLng(), request.getDropoffLat(), request.getDropoffLng());
        
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findAll();
        List<EstimatePriceResponse> responses = new ArrayList<>();
        
        long expiryTime = System.currentTimeMillis() + 120000; // 120 seconds
        
        for (VehicleType vehicleType : vehicleTypes) {
            double basePrice      = vehicleType.getPricePerKm() * distance;
            double surcharge      = vehicleTypeService.getCurrentSurcharge(vehicleType.getVehicleTypeId());
            double surgeMultiplier = 1.0;
            
            double discount = 0.0;
            if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
                Promotion promotion = promotionRepository.findByPromotionCode(request.getPromotionCode()).orElse(null);
                if (promotion != null && promotion.getIsActive() 
                    && promotion.getQuantity() > 0 
                    && !promotion.getEndTime().before(Timestamp.from(Instant.now()))) {
                    
                    double rawPrice = basePrice * surcharge * surgeMultiplier;
                    if (promotion.getMinTripValue() == null || rawPrice >= promotion.getMinTripValue()) {
                        if (promotion.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                            discount = promotion.getDiscountValue() != null ? promotion.getDiscountValue() : 0.0;
                        } else if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
                            double percent = promotion.getDiscountValue() != null ? promotion.getDiscountValue() : 0.0;
                            discount = (rawPrice * percent) / 100.0;
                            if (promotion.getDiscountLimit() != null && discount > promotion.getDiscountLimit()) {
                                discount = promotion.getDiscountLimit();
                            }
                        } else {
                            // Fallback
                            discount = promotion.getDiscountLimit() != null ? promotion.getDiscountLimit() : 0.0;
                        }
                    }
                }
            }

            double originalPrice = roundToThousand(basePrice * surcharge * surgeMultiplier);
            double finalPrice = roundToThousand(originalPrice - discount);
            if (finalPrice < 0) finalPrice = 0.0;

            String quoteId = UUID.randomUUID().toString();
            
            FareQuote quote = FareQuote.builder()
                    .quoteId(quoteId)
                    .vehicleTypeId(vehicleType.getVehicleTypeId())
                    .distance(distance)
                    .basePrice(basePrice)
                    .surcharge(surcharge)
                    .surgeMultiplier(surgeMultiplier)
                    .originalPrice(originalPrice)
                    .totalPrice(finalPrice)
                    .discount(discount)
                    .promotionId(request.getPromotionCode())
                    .build();
                    
            redisTemplate.opsForValue().set("quote:" + quoteId, quote, 120, TimeUnit.SECONDS);

            responses.add(EstimatePriceResponse.builder()
                    .vehicleTypeId(vehicleType.getVehicleTypeId())
                    .distance(distance)
                    .basePrice(basePrice)
                    .surcharge(surcharge)
                    .surgeMultiplier(surgeMultiplier)
                    .originalPrice(originalPrice)
                    .discount(discount)
                    .totalPrice(finalPrice)
                    .quoteId(quoteId)
                    .expiryTime(expiryTime)
                    .build());
        }
        
        return responses;
    }


    @Transactional
    public BookingDetailResponse createBooking(CreateBookingRequest request) {

        // 1. Geocode điểm đón → lấy tọa độ
        GeocodingResult geocoded = googleMapService.geocode(request.getPickupLocation());
        double lat = geocoded.geometry.location.lat;
        double lng = geocoded.geometry.location.lng;

        // 2. Kiểm tra có tài xế gần không
        List<Driver> nearbyDrivers = driverRepository.findTrulyAvailableDriversNearby(
                lat, lng, constant.getSEARCH_RADIUS_KM());

        String quoteId = request.getQuoteId();
        if (quoteId == null || quoteId.isBlank()) {
            throw new AppException(ErrorCode.QUOTE_EXPIRED);
        }
        
        FareQuote quote = (FareQuote) redisTemplate.opsForValue().get("quote:" + quoteId);
        if (quote == null) {
            throw new AppException(ErrorCode.QUOTE_EXPIRED);
        }

        VehicleType vehicleType = vehicleTypeRepository.findById(quote.getVehicleTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        double finalPrice = quote.getTotalPrice();
        double distance = quote.getDistance();

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseGet(() -> customerRepository.findByAccountId(request.getCustomerId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED)));

        Promotion validPromotion = null;
        if (request.getPromotionId() != null && !request.getPromotionId().isBlank()) {
            // request.getPromotionId() carries the promotionCode from EstimatePriceRequest
            validPromotion = promotionRepository.findByPromotionCode(request.getPromotionId()).orElse(null);
            if (validPromotion != null) {
                if (!validPromotion.getIsActive() || validPromotion.getQuantity() <= 0 
                    || validPromotion.getEndTime().before(Timestamp.from(Instant.now()))) {
                    throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE);
                }
                
                // Verify usage limit
                if (validPromotion.getUsageLimitPerUser() != null && validPromotion.getUsageLimitPerUser() > 0) {
                    int usageCount = customerPromotionRepository.countByCustomer_CustomerIdAndPromotion_PromotionIdAndStatus(
                            customer.getCustomerId(), validPromotion.getPromotionId(), CustomerPromotionStatus.USED);
                    if (usageCount >= validPromotion.getUsageLimitPerUser()) {
                        throw new RuntimeException("Bạn đã hết lượt sử dụng mã khuyến mãi này");
                    }
                }
            }
        }

        // 4. Tạo Payment
        boolean isCash = PaymentMethod.CASH.name().equalsIgnoreCase(
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "ONLINE");

        Payment payment = Payment.builder()
                .paymentType(isCash ? PaymentMethod.CASH.name() : "ONLINE")
                .amount(finalPrice)
                .paymentStatus(isCash) // CASH = đã thanh toán ngay
                .build();
        Payment savedPayment = paymentRepository.save(payment);

        Booking booking = Booking.builder()
                .vehicleTypeNo(vehicleType)
                .customerNo(customer)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .originalPrice(quote.getOriginalPrice() != null ? quote.getOriginalPrice() : finalPrice)
                .totalPrice(finalPrice)
                .bookingTime(Timestamp.valueOf(LocalDateTime.now()))
                .bookingStatus(BookingStatus.PENDING)
                .distance(distance)
                .paymentNo(savedPayment)
                .promotionNo(validPromotion) // Gắn khuyến mãi vào chuyến
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        if (validPromotion != null) {
            validPromotion.setQuantity(validPromotion.getQuantity() - 1);
            promotionRepository.save(validPromotion);
            
            // Mark customer promotion as USED if it exists, or create a new USED record
            CustomerPromotion cp = customerPromotionRepository.findByCustomer_CustomerIdAndPromotion_PromotionId(
                    customer.getCustomerId(), validPromotion.getPromotionId()).orElse(
                            CustomerPromotion.builder()
                            .customer(customer)
                            .promotion(validPromotion)
                            .savedAt(Timestamp.from(Instant.now()))
                            .build()
                    );
            cp.setStatus(CustomerPromotionStatus.USED);
            cp.setUsedAt(Timestamp.from(Instant.now()));
            customerPromotionRepository.save(cp);
        }

        // 6. Điều phối tài xế
        if (isCash) {
            // CASH → dispatch ngay, không cần chờ thanh toán online
            dispatchWithPriority(savedBooking.getBookingId(), lat, lng, Set.of());
        } else {
            // ONLINE → đặt hẹn: nếu sau 10 phút chưa thanh toán thì hủy
            paymentTimeoutService.schedulePaymentTimeout(savedBooking.getBookingId(), 10 * 60 * 1000L);
        }

        return mapToBookingDetailResponse(savedBooking);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GỌI SAU KHI THANH TOÁN ONLINE THÀNH CÔNG
    //  (PaymentController gọi phương thức này sau khi verify callback)
    // ─────────────────────────────────────────────────────────────────────────

    public void dispatchAfterPayment(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Chỉ dispatch nếu vẫn còn PENDING
        if (!BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            log.warn("[Booking] dispatchAfterPayment: booking {} không còn PENDING", bookingId);
            return;
        }

        // Geocode lại điểm đón
        GeocodingResult geocoded = googleMapService.geocode(booking.getPickupLocation());
        double lat = geocoded.geometry.location.lat;
        double lng = geocoded.geometry.location.lng;

        // Lấy blacklist đã có (trường hợp này luôn rỗng vì booking mới tạo)
        Set<String> blacklist = rejectionRepository.findDriverIdsByBookingId(bookingId);

        dispatchWithPriority(bookingId, lat, lng, blacklist);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TÀI XẾ TỪ CHỐI CHỦ ĐỘNG (gọi từ BookingController)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void rejectBooking(String bookingId, String driverId) {
        // Kiểm tra booking vẫn còn PENDING
        BookingStatus status = bookingRepository.findBookingStatusByBookingId(bookingId);
        if (status == null || status != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }
        // Ghi nhận từ chối → dispatcher đang chạy sẽ tự phát hiện qua polling
        dispatcherService.recordRejection(bookingId, driverId);
    }

    @Transactional
    public BookingDetailResponse assignDriver(String bookingId, String driverId) {
        log.info("[Booking] Gán tài xế {} vào booking {}", driverId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Chuyến xe không còn khả dụng");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Không cho nhận nếu đang thực hiện chuyến khác
        Booking ongoingRide = bookingRepository.findByDriverNo_DriverIdAndBookingStatus(
                driverId, BookingStatus.ACCEPTED);
        if (ongoingRide != null) {
            throw new IllegalStateException("Tài xế đang thực hiện chuyến khác");
        }

        booking.setDriverNo(driver);
        booking.setBookingStatus(BookingStatus.ACCEPTED);
        booking.setPickupTime(Timestamp.valueOf(LocalDateTime.now()));
        Booking updated = bookingRepository.save(booking);

        // Thông báo cho khách hàng
        if (updated.getCustomerNo() != null) {
            String payload = "DRIVER_ASSIGNED:" + bookingId
                    + ":" + driver.getDriverName()
                    + ":" + driver.getPhone()
                    + ":" + driver.getLicensePlate();
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + updated.getCustomerNo().getCustomerId(), payload);
        }

        log.info("[Booking] Gán tài xế thành công: booking={}", bookingId);
        return mapToBookingDetailResponse(updated);
    }


    @Transactional
    public BookingDetailResponse updateStatus(String bookingId, BookingStatus newStatus) {
        log.info("[Booking] Cập nhật trạng thái: booking={} → {}", bookingId, newStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        BookingStatus current = booking.getBookingStatus();

        boolean valid = switch (newStatus) {
            case ARRIVED      -> current == BookingStatus.ACCEPTED;
            case IN_PROGRESS  -> current == BookingStatus.ARRIVED;
            case COMPLETED    -> current == BookingStatus.IN_PROGRESS;
            case CANCELLED    -> current == BookingStatus.PENDING || current == BookingStatus.ACCEPTED;
            default           -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Không thể chuyển trạng thái từ " + current + " sang " + newStatus);
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        switch (newStatus) {
            case ARRIVED -> booking.setPickupTime(now);
            case COMPLETED -> {
                booking.setArrivalTime(now);
                Payment pmt = booking.getPaymentNo();
                Driver  drv = booking.getDriverNo();
                if (drv != null && pmt != null) {
                    double commission = booking.getTotalPrice() * platformCommissionRate;
                    if (!PaymentMethod.CASH.name().equalsIgnoreCase(pmt.getPaymentType())) {

                        walletService.addBalance(drv.getDriverId(),
                                                  booking.getTotalPrice() - commission);
                    } else {

                        walletService.deductBalance(drv.getDriverId(), commission);
                    }
                    driverRepository.updateLastTripTime(drv.getDriverId(), LocalDateTime.now());
                }
            }
            default -> { /* không cần xử lý thêm */ }
        }

        booking.setBookingStatus(newStatus);
        Booking updated = bookingRepository.save(booking);

        // Broadcast trạng thái mới cho khách hàng
        if (updated.getCustomerNo() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + updated.getCustomerNo().getCustomerId(),
                    "STATUS_UPDATE:" + bookingId + ":" + newStatus.name());
        }

        return mapToBookingDetailResponse(updated);
    }


    @Transactional
    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("[Booking] Đã hủy booking {}", bookingId);
    }


    public List<BookingDetailResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream().map(this::mapToBookingDetailResponse).collect(Collectors.toList());
    }

    /** Thống kê tổng quan cho Admin Dashboard */
    public java.util.Map<String, Object> getAdminSummary() {
        List<Booking> all = bookingRepository.findAll();
        long total     = all.size();
        long completed = all.stream().filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED).count();
        long cancelled = all.stream().filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED).count();
        double revenue = all.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED)
                .mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice() : 0)
                .sum();

        return java.util.Map.of(
            "totalBookings",   total,
            "completedRides",  completed,
            "cancelledRides",  cancelled,
            "totalRevenue",    revenue
        );
    }

    public BookingDetailResponse getBookingById(String bookingId) {
        return mapToBookingDetailResponse(
                bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND)));
    }

    public List<BookingDetailResponse> getBookingsByCustomer(String customerId) {
        return bookingRepository.findByCustomerNo_CustomerIdOrderByBookingTimeDesc(customerId)
                .stream().map(this::mapToBookingDetailResponse).collect(Collectors.toList());
    }

    public List<BookingDetailResponse> getBookingsByDriver(String driverId) {
        return bookingRepository.findByDriverNo_DriverIdOrderByBookingTimeDesc(driverId)
                .stream().map(this::mapToBookingDetailResponse).collect(Collectors.toList());
    }

    public List<AvailableRideResponse> getAvailableRides(String driverArea) {
        List<Booking> available;
        if (driverArea != null && !driverArea.isBlank()) {
            String city = DriverService.extractCityFromAddress(driverArea);
            available = bookingRepository
                    .findByBookingStatusAndDriverNoIsNullAndPickupLocationContainingOrderByBookingTimeAsc(
                            BookingStatus.PENDING, city);
        } else {
            available = bookingRepository
                    .findByBookingStatusAndDriverNoIsNullOrderByBookingTimeAsc(BookingStatus.PENDING);
        }
        return available.stream().map(this::mapToAvailableRideResponse).collect(Collectors.toList());
    }

    @Transactional
    public BookingDetailResponse completeBooking(String bookingId) {
        return updateStatus(bookingId, BookingStatus.COMPLETED);
    }

    private void dispatchWithPriority(String bookingId, double lat, double lng,
                                      Set<String> existingBlacklist) {
        // Lấy tài xế trong bán kính bình thường, loại trừ blacklist
        List<Driver> candidates = driverRepository
                .findTrulyAvailableDriversNearby(lat, lng, constant.getSEARCH_RADIUS_KM())
                .stream()
                .filter(d -> !existingBlacklist.contains(d.getDriverId()))
                .collect(Collectors.toList());

        candidates.forEach(d -> {
            int rejectCount = rejectionRepository.countByDriverIdAndType(
                    d.getDriverId(), RejectionType.REJECTED);
            int ignoreCount = rejectionRepository.countByDriverIdAndType(
                    d.getDriverId(), RejectionType.IGNORED);
            // d.getDistance() được stored procedure gán qua @Transient
            double distanceKm = d.getDistance() != null ? d.getDistance() : constant.getSEARCH_RADIUS_KM();
            d.setScore(calculateScore(d, distanceKm, rejectCount, ignoreCount));
        });

        // Sắp xếp: điểm cao hơn → ưu tiên trước
        candidates.sort(Comparator.comparingDouble(Driver::getScore).reversed());

        log.info("[Booking] Dispatch booking={} cho {} tài xế theo điểm ưu tiên", bookingId, candidates.size());
        dispatcherService.startDispatching(bookingId, candidates);
    }

    private double calculateScore(Driver driver, double distanceKm, int rejectCount, int ignoreCount) {
        // 1. Điểm khoảng cách: gần → cao, xa → thấp
        double distanceScore = Math.max(0.0, 1.0 - (distanceKm / constant.getMAX_DISTANCE_KM()));

        // 2. Điểm rating trung bình (0–5 → 0.0–1.0)
        Double avg = ratingRepository.getAverageRatingByDriver(driver.getDriverId());
        double ratingScore = (avg != null ? avg : 3.0) / 5.0; // default 3 sao nếu chưa có rating

        // 3. Điểm "nhàn rỗi": càng lâu chưa có chuyến → càng ưu tiên (công bằng)
        long   idleMinutes = getIdleMinutes(driver);
        double idleScore   = Math.min(idleMinutes / constant.getMAX_IDLE_MIN(), 1.0);

        // 4. Phạt từ chối chủ động (nặng hơn)
        double rejectPenalty = Math.min(rejectCount * 0.5, 1.0);

        // 5. Phạt bỏ qua / không phản hồi (nhẹ hơn)
        double ignorePenalty = Math.min(ignoreCount * 0.2, 1.0);

        return (constant.getW_DISTANCE() * distanceScore)
             + (constant.getW_RATING()   * ratingScore)
             + (constant.getW_IDLE()     * idleScore)
             - (constant.getW_REJECT()   * rejectPenalty)
             - (constant.getW_IGNORE()   * ignorePenalty);
    }

    private long getIdleMinutes(Driver driver) {
        if (driver.getLastTripTime() == null)
            return 60L;
        return Duration.between(
                Instant.ofEpochMilli(driver.getLastTripTime().getTime()),
                Instant.now()
        ).toMinutes();
    }

    private double roundToThousand(double value) {
        return Math.round(value / 1000.0) * 1000.0;
    }


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
                .originalPrice(booking.getOriginalPrice())
                .totalPrice(booking.getTotalPrice())
                .bookingTime(booking.getBookingTime())
                .pickupTime(booking.getPickupTime())
                .arrivalTime(booking.getArrivalTime())
                .bookingStatus(booking.getBookingStatus())
                .distance(booking.getDistance())
                .paymentMethod(booking.getPaymentNo() != null ? booking.getPaymentNo().getPaymentType() : null)
                .paymentStatus(booking.getPaymentNo() != null ? booking.getPaymentNo().getPaymentStatus() : null)
                .promotionCode(booking.getPromotionNo() != null ? booking.getPromotionNo().getPromotionId() : null)
                .build();
    }

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

    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
