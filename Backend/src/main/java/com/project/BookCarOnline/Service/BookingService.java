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
import com.project.BookCarOnline.Entity.Enum.WaitResult;
import com.project.BookCarOnline.Entity.CustomerPromotion;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.*;
import com.project.BookCarOnline.Entity.Payment;
import com.project.BookCarOnline.DTO.Redis.DriverGeoResult;
import com.project.BookCarOnline.DTO.Redis.DriverStats;
import com.project.BookCarOnline.DTO.Redis.FareQuote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Map;
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
    RatingRepository          ratingRepository;
    PaymentRepository         paymentRepository;
    VehicleTypeRepository     vehicleTypeRepository;
    BookingRejectionRepository rejectionRepository;
    PromotionRepository       promotionRepository;
    CustomerPromotionRepository customerPromotionRepository;

    RideDispatcherService     dispatcherService;
    VehicleTypeService        vehicleTypeService;
    WalletService             walletService;
    PricingService            pricingService;
    PaymentTimeoutService     paymentTimeoutService;
    GoogleMapService          googleMapService;
    DriverCacheService        driverCacheService;

    SimpMessagingTemplate     messagingTemplate;
    RedisTemplate<String, Object> redisTemplate;
    Constant                  constant;

    @NonFinal
    @Value("${app.commission.platform-rate}")
    protected double platformCommissionRate;

    @NonFinal
    @Value("${app.time-to-live.quote}")
    protected long quoteTtlMillis;

    public List<EstimatePriceResponse> estimatePrice(EstimatePriceRequest request) {
        double distance = calculateDistanceKm(
                request.getPickupLat(), request.getPickupLng(),
                request.getDropoffLat(), request.getDropoffLng());

        // Resolve nhiều mã khuyến mãi
        List<Promotion> promotions = pricingService.resolvePromotions(request.getPromotionCodes());

        List<VehicleType> vehicleTypes = vehicleTypeRepository.findAll();
        long expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(120);

        return vehicleTypes.stream()
                .map(vt -> buildEstimateForVehicleType(vt, distance, promotions, expiryTime))
                .collect(Collectors.toList());
    }


    @Transactional
    public BookingDetailResponse createBooking(CreateBookingRequest request) {

        FareQuote quote = validateAndGetQuote(request.getQuoteId());

        VehicleType vehicleType = vehicleTypeRepository.findById(quote.getVehicleTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        Customer customer = resolveCustomer(request.getCustomerId());

        Promotion validPromotion = null;
        if (quote.getPromotionIds() != null && !quote.getPromotionIds().isEmpty()) {
            // Áp dụng và trừ số lượt tất cả các mã khuyến mãi
            for (String promoCode : quote.getPromotionIds()) {
                if (promoCode != null && !promoCode.isBlank()) {
                    try {
                        validPromotion = pricingService.validateAndConsumePromotion(
                                promoCode, customer.getCustomerId(), quote.getTotalPrice());
                    } catch (Exception e) {
                        log.warn("[Booking] Không thể áp dụng promo {}: {}", promoCode, e.getMessage());
                    }
                }
            }
        }

        boolean isCash = isCashPayment(request.getPaymentMethod());
        Payment payment = paymentRepository.save(Payment.builder()
                .paymentType(isCash ? PaymentMethod.CASH : PaymentMethod.ONLINE)
                .amount(quote.getTotalPrice())
                .paymentStatus(isCash)
                .build());

        // 5. Geocode điểm đón (chỉ dùng khi request không có tọa độ)
        double pickupLat = request.getPickupLat() != null ? request.getPickupLat() : geocodeLat(request.getPickupLocation());
        double pickupLng = request.getPickupLng() != null ? request.getPickupLng() : geocodeLng(request.getPickupLocation());

        // 6. Tạo Booking
        Booking booking = Booking.builder()
                .vehicleTypeNo(vehicleType)
                .customerNo(customer)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .pickupLat(pickupLat)
                .pickupLng(pickupLng)
                .dropoffLat(request.getDropoffLat())
                .dropoffLng(request.getDropoffLng())
                .originalPrice(quote.getOriginalPrice())
                .totalPrice(quote.getTotalPrice())
                .bookingTime(Timestamp.valueOf(LocalDateTime.now()))
                .bookingStatus(BookingStatus.PENDING)
                .distance(quote.getDistance())
                .paymentNo(payment)
                .promotionNo(validPromotion)
                .build();
        Booking saved = bookingRepository.save(booking);

        if (validPromotion != null) {
            recordCustomerPromotion(customer, validPromotion);
        }

        // Xóa quote khỏi Redis (tránh dùng lại)
        redisTemplate.delete("quote:" + request.getQuoteId());

        if (isCash) {
            dispatchWithPriority(saved, pickupLat, pickupLng, Set.of());
        } else {
            paymentTimeoutService.schedulePaymentTimeout(saved.getBookingId(), 10 * 60 * 1000L);
        }

        return mapToBookingDetailResponse(saved);
    }

    public void dispatchAfterPayment(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Chỉ dispatch nếu vẫn còn PENDING
        if (!BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            log.warn("[Booking] dispatchAfterPayment: booking {} không còn PENDING", bookingId);
            return;
        }

        // Geocode lại điểm đón
        double lat = booking.getPickupLat() != null
                ? booking.getPickupLat()
                : geocodeLat(booking.getPickupLocation());
        double lng = booking.getPickupLng() != null
                ? booking.getPickupLng()
                : geocodeLng(booking.getPickupLocation());

        // Lấy blacklist đã có (trường hợp này luôn rỗng vì booking mới tạo)
        Set<String> blacklist = rejectionRepository.findDriverIdsByBookingId(bookingId);

        dispatchWithPriority(booking, lat, lng, blacklist);
    }

    //  TÀI XẾ TỪ CHỐI CHỦ ĐỘNG 
    @Transactional
    public void rejectBooking(String bookingId, String driverId) {
        BookingStatus status = bookingRepository.findBookingStatusByBookingId(bookingId);
        if (status == null || status != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }
        dispatcherService.recordRejection(bookingId, driverId);
    }

    @Transactional
    public BookingDetailResponse assignDriver(String bookingId, String driverId) {
        log.info("[Booking] Gán tài xế {} vào booking {}", driverId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_TAKEN);
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Không cho nhận nếu đang thực hiện chuyến khác
        boolean driverBusy = bookingRepository.existsByDriverNo_DriverIdAndBookingStatusIn(
                driverId, List.of(BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS, BookingStatus.ARRIVED));
        if (driverBusy) {
            throw new IllegalStateException("Tài xế đang thực hiện chuyến khác");
        }

        booking.setDriverNo(driver);
        booking.setBookingStatus(BookingStatus.ACCEPTED);
        booking.setPickupTime(Timestamp.valueOf(LocalDateTime.now()));
        Booking updated = bookingRepository.save(booking);

        // Thông báo cho khách hàng
        notifyCustomerDriverAssigned(updated, driver);
        dispatcherService.resolveDispatch(bookingId, WaitResult.ACCEPTED);

        log.info("[Booking] Gán tài xế thành công: booking={}", bookingId);
        return mapToBookingDetailResponse(updated);
    }


    @Transactional
    public BookingDetailResponse updateStatus(String bookingId, BookingStatus newStatus) {
        log.info("[Booking] Cập nhật trạng thái: booking={} → {}", bookingId, newStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        validateStatusTransition(booking.getBookingStatus(), newStatus);

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        switch (newStatus) {
            case ARRIVED     -> booking.setPickupTime(now);
            case COMPLETED   -> handleCompleteRide(booking, now);
            case CANCELLED   -> driverCacheService.clearLocation(bookingId);
            default          -> {  }
        }

        booking.setBookingStatus(newStatus);
        Booking updated = bookingRepository.save(booking);

        broadcastStatusToCustomer(updated, newStatus);

        return mapToBookingDetailResponse(updated);
    }


    @Transactional
    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        if (booking.getDriverNo() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/driver/" + booking.getDriverNo().getDriverId(),
                    "CUSTOMER_CANCELLED:" + bookingId);
        }

        driverCacheService.clearLocation(bookingId);
        dispatcherService.resolveDispatch(bookingId, WaitResult.CUSTOMER_CANCELLED);
        log.info("[Booking] Đã hủy booking {}", bookingId);
    }

    @Transactional
    public void cancelBookingByDriver(String bookingId, String driverId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getDriverNo() == null || !booking.getDriverNo().getDriverId().equals(driverId)) {
            throw new IllegalStateException("Tài xế không có quyền huỷ chuyến này");
        }

        Set<BookingStatus> cancellableStatuses = Set.of(BookingStatus.ACCEPTED, BookingStatus.ARRIVED);
        if (!cancellableStatuses.contains(booking.getBookingStatus())) {
            throw new IllegalStateException("Không thể huỷ chuyến xe ở trạng thái hiện tại");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        if (booking.getCustomerNo() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                    "DRIVER_CANCELLED:" + bookingId);
        }

        driverCacheService.clearLocation(bookingId);
        log.info("[Booking] Tài xế {} đã hủy booking {}", driverId, bookingId);
    }
 

    public List<BookingDetailResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream().map(this::mapToBookingDetailResponse).collect(Collectors.toList());
    }

    public java.util.Map<String, Object> getAdminSummary() {
        List<Booking> all = bookingRepository.findAll();
        long completed = 0, cancelled = 0;
        double revenue = 0.0;
        for (Booking b : all) {
            switch (b.getBookingStatus()) {
                case COMPLETED -> { completed++; revenue += b.getTotalPrice() != null ? b.getTotalPrice() : 0; }
                case CANCELLED -> cancelled++;
                default -> {}
            }
        }

        return java.util.Map.of(
            "totalBookings",   (long) all.size(),
            "completedRides",  completed,
            "cancelledRides",  cancelled,
            "totalRevenue",    revenue
        );
    }

    public Page<BookingDetailResponse> searchBookingsForAdmin(int page, int size, String status, String search,
                                                              String fromDate, String toDate) {
        Pageable pageable = PageRequest.of(page, size);
        BookingStatus bookingStatus = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            bookingStatus = BookingStatus.valueOf(status);
        }
        Timestamp from = null, to = null;
        if (fromDate != null && !fromDate.isBlank()) {
            from = Timestamp.valueOf(java.time.LocalDate.parse(fromDate).atStartOfDay());
        }
        if (toDate != null && !toDate.isBlank()) {
            to = Timestamp.valueOf(java.time.LocalDate.parse(toDate).plusDays(1).atStartOfDay());
        }
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return bookingRepository.searchForAdmin(bookingStatus, searchParam, from, to, pageable)
                .map(this::mapToBookingDetailResponse);
    }

    @Transactional
    public BookingDetailResponse adminForceCancel(String bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        Set<BookingStatus> cancellable = Set.of(BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.ARRIVED);
        if (!cancellable.contains(booking.getBookingStatus())) {
            throw new IllegalStateException("Chỉ có thể huỷ chuyến khi chưa đón khách (trạng thái hiện tại: " + booking.getBookingStatus() + ")");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);

        if (booking.getDriverNo() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/driver/" + booking.getDriverNo().getDriverId(),
                    "ADMIN_CANCELLED:" + bookingId);
        }
        if (booking.getCustomerNo() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                    "ADMIN_CANCELLED:" + bookingId);
        }
        driverCacheService.clearLocation(bookingId);
        dispatcherService.resolveDispatch(bookingId, WaitResult.CUSTOMER_CANCELLED);
        log.info("[Admin] Đã huỷ booking {}", bookingId);
        return mapToBookingDetailResponse(updated);
    }

    @Transactional
    public BookingDetailResponse adminAssignDriver(String bookingId, String driverId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Chỉ có thể gán tài xế khi chuyến đang ở trạng thái PENDING");
        }
        if (booking.getDriverNo() != null) {
            throw new IllegalStateException("Chuyến đã có tài xế");
        }
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        boolean driverBusy = bookingRepository.existsByDriverNo_DriverIdAndBookingStatusIn(
                driverId, List.of(BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS, BookingStatus.ARRIVED));
        if (driverBusy) {
            throw new IllegalStateException("Tài xế đang thực hiện chuyến khác");
        }

        booking.setDriverNo(driver);
        booking.setBookingStatus(BookingStatus.ACCEPTED);
        booking.setPickupTime(Timestamp.valueOf(LocalDateTime.now()));
        Booking updated = bookingRepository.save(booking);
        notifyCustomerDriverAssigned(updated, driver);
        log.info("[Admin] Gán tài xế {} vào booking {}", driverId, bookingId);
        return mapToBookingDetailResponse(updated);
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

    public BookingDetailResponse getActiveBookingByCustomer(String customerId) {
        return bookingRepository.findActiveByCustomer(customerId)
                .stream().findFirst().map(this::mapToBookingDetailResponse).orElse(null);
    }

    public List<BookingDetailResponse> getBookingsByDriver(String driverId) {
        return bookingRepository.findByDriverNo_DriverIdOrderByBookingTimeDesc(driverId)
                .stream().map(this::mapToBookingDetailResponse).collect(Collectors.toList());
    }

    public BookingDetailResponse getActiveBookingByDriver(String driverId) {
        return bookingRepository.findActiveByDriver(driverId)
                .stream().findFirst().map(this::mapToBookingDetailResponse).orElse(null);
    }

    public Page<BookingDetailResponse> getBookingsByDriverPaginated(String driverId, String status, int page, int size) {
       Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            return bookingRepository.findByDriverNo_DriverIdAndBookingStatusOrderByBookingTimeDesc(driverId, BookingStatus.valueOf(status), pageable)
                    .map(this::mapToBookingDetailResponse);
        }
        return bookingRepository.findByDriverNo_DriverIdOrderByBookingTimeDesc(driverId, pageable)
                .map(this::mapToBookingDetailResponse);
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

    private void handleCompleteRide(Booking booking, Timestamp completedAt) {
        booking.setArrivalTime(completedAt);

        Payment pmt = booking.getPaymentNo();
        Driver  drv = booking.getDriverNo();

        if (drv == null || pmt == null) {
            log.warn("[Booking] Thiếu driver/payment khi complete booking {}", booking.getBookingId());
            return;
        }

        double commission = booking.getTotalPrice() * platformCommissionRate;
        String typeDeduct = "FEE_BOOKING";
        if (PaymentMethod.CASH == pmt.getPaymentType()) {
            walletService.deductBalance(drv.getDriverId(), commission,typeDeduct);
        } else {
            walletService.addBalance(drv.getDriverId(), booking.getTotalPrice() - commission);
        }

        driverRepository.updateLastTripTime(drv.getDriverId(), LocalDateTime.now());
        driverCacheService.clearLocation(booking.getBookingId());

        // Xóa cache điểm số tài xế để tính lại lần sau
        driverCacheService.evictDriverStats(drv.getDriverId());
    }

    private EstimatePriceResponse buildEstimateForVehicleType(
            VehicleType vehicleType, double distance,
            List<Promotion> promotions, long expiryTime) {

        double basePrice       = vehicleType.getPricePerKm() * distance;
        double surcharge       = vehicleTypeService.getCurrentSurcharge(vehicleType.getVehicleTypeId());
        double surgeMultiplier = 1.0;
        double rawPrice        = basePrice * surcharge * surgeMultiplier;

        // Tính tổng discount từ nhiều mã khuyến mãi
        double discount        = pricingService.calculateTotalDiscount(promotions, rawPrice);
        double originalPrice   = roundToThousand(rawPrice);
        double finalPrice      = Math.max(0.0, roundToThousand(originalPrice - discount));

        // Lưu list mã khuyến mãi vào FareQuote
        List<String> promoIds = (promotions != null)
                ? promotions.stream().map(Promotion::getPromotionCode).toList()
                : List.of();

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
                .promotionIds(promoIds)
                .build();
        redisTemplate.opsForValue().set("quote:" + quoteId, quote, 120, TimeUnit.SECONDS);

        return EstimatePriceResponse.builder()
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
                .build();
    }

    private void dispatchWithPriority(Booking booking, double lat, double lng,
                                      Set<String> existingBlacklist) {

        String vehicleTypeId = booking.getVehicleTypeNo().getVehicleTypeId();
        List<DriverGeoResult> nearbyResults =
                driverCacheService.findNearbyDrivers(lat, lng, constant.getSEARCH_RADIUS_KM());

        if (nearbyResults.isEmpty()) {
            log.info("[Booking] Không tìm thấy tài xế nào quanh đây cho booking={}", booking.getBookingId());
            dispatcherService.startDispatching(booking.getBookingId(), List.of());
            return;
        }


        List<String> driverIds = nearbyResults.stream()
                .map(DriverGeoResult::getDriverId)
                .filter(id -> !existingBlacklist.contains(id))
                .collect(Collectors.toList());

        if (driverIds.isEmpty()) {
            log.info("[Booking] Tất cả tài xế quanh đây đều trong blacklist cho booking={}", booking.getBookingId());
            dispatcherService.startDispatching(booking.getBookingId(), List.of());
            return;
        }

        Map<String, Double> distanceMap = nearbyResults.stream()
                .collect(Collectors.toMap(
                        DriverGeoResult::getDriverId,
                        DriverGeoResult::getDistanceKm,
                        (a, b) -> a  // giải quyết trùng key
                ));


        List<Driver> candidates = driverRepository.findAllById(driverIds).stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivityStatus()))          // Đang online
                .filter(d -> vehicleTypeId.equals(
                        d.getVehicleType() != null ? d.getVehicleType().getVehicleTypeId() : null)) // Đúng loại xe
                .collect(Collectors.toList());

        // === CAFFEINE CACHE: Tính điểm ưu tiên từ RAM thay vì chọc DB ===
        candidates.forEach(d -> {
            double distanceKm = distanceMap.getOrDefault(d.getDriverId(), constant.getSEARCH_RADIUS_KM());
            d.setDistance(distanceKm);
            DriverStats stats = driverCacheService.getDriverStats(d.getDriverId());
            d.setScore(calculateScore(d, distanceKm, stats));
        });

        candidates.sort(Comparator.comparingDouble(Driver::getScore).reversed());

        log.info("[Booking] Dispatch booking={} cho {} tài xế (Redis GEO + Cache)", booking.getBookingId(), candidates.size());
        dispatcherService.startDispatching(booking.getBookingId(), candidates);
    }

    private double calculateScore(Driver driver, double distanceKm, DriverStats stats) {
        double distanceScore = Math.max(0.0, 1.0 - (distanceKm / constant.getMAX_DISTANCE_KM()));

        // Điểm rating trung bình (0–5 → 0.0–1.0)
        double ratingScore = stats.getAvgRating() / 5.0;

        // Điểm "nhàn rỗi"
        long   idleMinutes = getIdleMinutes(driver);
        double idleScore   = Math.min(idleMinutes / constant.getMAX_IDLE_MIN(), 1.0);

        // Phạt từ chối chủ động (nặng hơn)
        double rejectPenalty = Math.min(stats.getRejectCount() * 0.5, 1.0);

        // Phạt bỏ qua / không phản hồi (nhẹ hơn)
        double ignorePenalty = Math.min(stats.getIgnoreCount() * 0.2, 1.0);

        return (constant.getW_DISTANCE() * distanceScore)
             + (constant.getW_RATING()   * ratingScore)
             + (constant.getW_IDLE()     * idleScore)
             - (constant.getW_REJECT()   * rejectPenalty)
             - (constant.getW_IGNORE()   * ignorePenalty);
    }

    private FareQuote validateAndGetQuote(String quoteId) {
        if (quoteId == null || quoteId.isBlank()) {
            throw new AppException(ErrorCode.QUOTE_EXPIRED);
        }
        FareQuote quote = (FareQuote) redisTemplate.opsForValue().get("quote:" + quoteId);
        if (quote == null) {
            throw new AppException(ErrorCode.QUOTE_EXPIRED);
        }
        return quote;
    }

    private Customer resolveCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .or(() -> customerRepository.findByAccountId(customerId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
    }

    private Promotion resolvePromotion(String promotionCode) {
        if (promotionCode == null || promotionCode.isBlank()) return null;
        return promotionRepository.findByPromotionCode(promotionCode).orElse(null);
    }

    private void recordCustomerPromotion(Customer customer, Promotion promotion) {
        CustomerPromotion cp = customerPromotionRepository
                .findByCustomer_CustomerIdAndPromotion_PromotionId(
                        customer.getCustomerId(), promotion.getPromotionId())
                .orElseGet(() -> CustomerPromotion.builder()
                        .customer(customer)
                        .promotion(promotion)
                        .savedAt(Timestamp.from(Instant.now()))
                        .build());
        cp.setStatus(CustomerPromotionStatus.USED);
        cp.setUsedAt(Timestamp.from(Instant.now()));
        customerPromotionRepository.save(cp);
    }

    private boolean isCashPayment(String paymentMethod) {
        return PaymentMethod.CASH.name().equalsIgnoreCase(paymentMethod != null ? paymentMethod : "ONLINE");
    }

    private void validateStatusTransition(BookingStatus current, BookingStatus next) {
        boolean valid = switch (next) {
            case ARRIVED     -> current == BookingStatus.ACCEPTED;
            case IN_PROGRESS -> current == BookingStatus.ARRIVED;
            case COMPLETED   -> current == BookingStatus.IN_PROGRESS;
            case CANCELLED   -> current == BookingStatus.PENDING || current == BookingStatus.ACCEPTED;
            default          -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Không thể chuyển trạng thái từ " + current + " sang " + next);
        }
    }

    private void notifyCustomerDriverAssigned(Booking booking, Driver driver) {
        if (booking.getCustomerNo() == null) return;
        String payload = "DRIVER_ASSIGNED:" + booking.getBookingId()
                + ":" + driver.getDriverName()
                + ":" + driver.getPhone()
                + ":" + driver.getLicensePlate();
        messagingTemplate.convertAndSend(
                "/topic/customer/" + booking.getCustomerNo().getCustomerId(), payload);
    }

    private void broadcastStatusToCustomer(Booking booking, BookingStatus status) {
        if (booking.getCustomerNo() == null) return;
        messagingTemplate.convertAndSend(
                "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                "STATUS_UPDATE:" + booking.getBookingId() + ":" + status.name());
    }

    private Booking getBookingOrThrow(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
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
                .pickupLat(booking.getPickupLat())
                .pickupLng(booking.getPickupLng())
                .dropoffLat(booking.getDropoffLat())
                .dropoffLng(booking.getDropoffLng())
                .originalPrice(booking.getOriginalPrice())
                .totalPrice(booking.getTotalPrice())
                .bookingTime(booking.getBookingTime())
                .pickupTime(booking.getPickupTime())
                .arrivalTime(booking.getArrivalTime())
                .bookingStatus(booking.getBookingStatus())
                .distance(booking.getDistance())
                .paymentMethod(booking.getPaymentNo() != null ? booking.getPaymentNo().getPaymentType().name() : null)
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
                .pickupLat(booking.getPickupLat())
                .pickupLng(booking.getPickupLng())
                .dropoffLat(booking.getDropoffLat())
                .dropoffLng(booking.getDropoffLng())
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

    private GeocodingResult geocodeOnce(String address) {
        return googleMapService.geocode(address);
    }

    private double geocodeLat(String address) {
        return geocodeOnce(address).geometry.location.lat;
    }

    private double geocodeLng(String address) {
        return geocodeOnce(address).geometry.location.lng;
    }
}
