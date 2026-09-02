package com.project.BookCarOnline.booking.service;

import com.google.maps.model.GeocodingResult;
import com.project.BookCarOnline.booking.config.BookingSchedulingProperties;
import com.project.BookCarOnline.booking.dto.request.CreateBookingRequest;
import com.project.BookCarOnline.booking.dto.request.EstimatePriceRequest;
import com.project.BookCarOnline.booking.dto.response.BookingDetailResponse;
import com.project.BookCarOnline.booking.dto.response.EstimatePriceResponse;
import com.project.BookCarOnline.booking.entity.*;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.entity.enums.PaymentStatus;
import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.identity.dto.summary.CustomerSummary;
import com.project.BookCarOnline.identity.dto.summary.DriverSummary;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.booking.entity.enums.WaitResult;
import com.project.BookCarOnline.promotion.dto.PromotionQuote;
import com.project.BookCarOnline.promotion.service.PricingService;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.booking.repository.*;
import com.project.BookCarOnline.finance.dto.PaymentSummary;
import com.project.BookCarOnline.finance.service.PaymentService;
import com.project.BookCarOnline.finance.service.WalletService;
import com.project.BookCarOnline.booking.dto.redis.DriverLocation;
import com.project.BookCarOnline.booking.dto.redis.FareQuote;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    BookingRepository bookingRepository;
    BookingRejectionRepository rejectionRepository;
    BookingPromotionRepository bookingPromotionRepository;

    IdentityQueryService identityQueryService;
    RideDispatcherService dispatcherService;
    VehicleTypeService vehicleTypeService;
    PaymentService paymentService;
    WalletService walletService;
    PricingService pricingService;
    PaymentTimeoutService paymentTimeoutService;
    GoogleMapService googleMapService;
    DriverCacheService driverCacheService;
    BookingQueryService bookingQueryService;
    BookingQuoteService bookingQuoteService;
    BookingSchedulingProperties bookingSchedulingProperties;

    SimpMessagingTemplate messagingTemplate;

    @NonFinal
    @Value("${app.commission.platform-rate}")
    protected double platformCommissionRate;

    public List<EstimatePriceResponse> estimatePrice(EstimatePriceRequest request) {
        return bookingQuoteService.estimate(request);
    }

    @Transactional
    public BookingDetailResponse createBooking(CreateBookingRequest request) {

        LocalDateTime now = LocalDateTime.now(ZoneId.of(bookingSchedulingProperties.getZone()));
        LocalDateTime scheduledAt = request.getScheduledAt();
        if (scheduledAt != null && !scheduledAt.isAfter(now)) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        FareQuote quote = bookingQuoteService.getQuote(request.getQuoteId());

        VehicleTypeSummary vehicleType = vehicleTypeService.getVehicleTypeSummary(quote.getVehicleTypeId());

        CustomerSummary customer = identityQueryService.resolveCustomer(request.getCustomerId());

        List<PromotionQuote> validPromotions = new ArrayList<>();
        if (quote.getPromotionIds() != null && !quote.getPromotionIds().isEmpty()) {
            // Áp dụng và trừ số lượt tất cả các mã khuyến mãi
            for (String promoCode : quote.getPromotionIds()) {
                if (promoCode != null && !promoCode.isBlank()) {
                    try {
                        PromotionQuote p = pricingService.validateAndConsumePromotion(
                                promoCode, customer.customerId(), quote.getTotalPrice());
                        if (p != null) {
                            validPromotions.add(p);
                        }
                    } catch (Exception e) {
                        log.warn("[Booking] Không thể áp dụng promo {}: {}", promoCode, e.getMessage());
                    }
                }
            }
        }

        boolean isCash = isCashPayment(request.getPaymentMethod());
        PaymentSummary payment = paymentService.create(
                isCash ? PaymentMethod.CASH : PaymentMethod.ONLINE,
                quote.getTotalPrice(),
                isCash);

        // 5. Geocode điểm đón (chỉ dùng khi request không có tọa độ)
        double pickupLat = request.getPickupLat() != null ? request.getPickupLat()
                : geocodeLat(request.getPickupLocation());
        double pickupLng = request.getPickupLng() != null ? request.getPickupLng()
                : geocodeLng(request.getPickupLocation());

        // 6. Tạo Booking
        Booking booking = Booking.builder()
                .vehicleTypeId(vehicleType.vehicleTypeId())
                .customerId(customer.customerId())
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .pickupLat(pickupLat)
                .pickupLng(pickupLng)
                .dropoffLat(request.getDropoffLat())
                .dropoffLng(request.getDropoffLng())
                .originalPrice(quote.getOriginalPrice())
                .totalPrice(quote.getTotalPrice())
                .bookingTime(Timestamp.valueOf(now))
                .scheduledAt(scheduledAt)
                .bookingStatus(scheduledAt == null ? BookingStatus.PENDING : BookingStatus.QUEUED)
                .distance(quote.getDistance())
                .paymentId(payment.paymentId())
                .build();
        Booking saved = bookingRepository.save(booking);

        // Lưu lịch sử sử dụng cho TẤT CẢ các mã khuyến mãi được áp dụng và lưu chi tiết BookingPromotion
        double remainingPrice = quote.getOriginalPrice() != null ? quote.getOriginalPrice() : 0.0;
        List<BookingPromotion> bookingPromotionsToSave = new ArrayList<>();

        for (PromotionQuote p : validPromotions) {
            double discountAmount = pricingService.calculateDiscount(p, remainingPrice);
            if (discountAmount > 0) {
                BookingPromotion bp = BookingPromotion.builder()
                        .booking(saved)
                        .promotionId(p.promotionId())
                        .discountAmount(discountAmount)
                        .build();
                bookingPromotionsToSave.add(bp);
                remainingPrice = Math.max(0.0, remainingPrice - discountAmount);
            }
            pricingService.markCustomerPromotionUsed(customer.customerId(), p.promotionId());
        }
        if (!bookingPromotionsToSave.isEmpty()) {
            bookingPromotionRepository.saveAll(bookingPromotionsToSave);
        }

        // Xóa quote khỏi Redis (tránh dùng lại)
        bookingQuoteService.deleteQuote(request.getQuoteId());

        if (scheduledAt == null) {
            if (isCash) {
                dispatcherService.dispatchNearbyDrivers(saved, pickupLat, pickupLng, Set.of());
            } else {
                paymentTimeoutService.schedulePaymentTimeout(saved.getBookingId(), 10 * 60 * 1000L);
            }
        } else if (!isCash) {
            paymentTimeoutService.schedulePaymentTimeout(saved.getBookingId(), 10 * 60 * 1000L);
        }

        return bookingQueryService.toDetail(saved);
    }

    public void dispatchAfterPayment(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getScheduledAt() != null) {
            log.debug("[Booking] Booking={} là chuyến hẹn giờ, chờ scheduler điều phối", bookingId);
            return;
        }

        // Chỉ dispatch nếu vẫn còn PENDING
        if (!BookingStatus.PENDING.equals(booking.getBookingStatus()) || booking.getDriverId() != null) {
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

        dispatcherService.dispatchNearbyDrivers(booking, lat, lng, blacklist);
    }

    public PaymentStatus getPaymentStatus(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return PaymentStatus.FAILED;
        }
        return booking.getPaymentId() != null
                && Boolean.TRUE.equals(paymentService.get(booking.getPaymentId()).paid())
                ? PaymentStatus.SUCCESS
                : PaymentStatus.PENDING;
    }

    @Transactional
    public void confirmOnlinePayment(String bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (booking.getPaymentId() != null) {
            PaymentSummary payment = paymentService.get(booking.getPaymentId());
            if (Boolean.TRUE.equals(payment.paid())) {
                log.info("[Booking] Bỏ qua callback thanh toán trùng cho booking={}", bookingId);
                return;
            }
            paymentService.markPaid(booking.getPaymentId());
        }
        boolean scheduled = booking.getScheduledAt() != null;
        booking.setBookingStatus(scheduled ? BookingStatus.QUEUED : BookingStatus.PENDING);
        bookingRepository.save(booking);
        if (booking.getCustomerId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + booking.getCustomerId(),
                    "PAYMENT_SUCCESS:" + bookingId);
        }
        if (!scheduled) {
            dispatchAfterPayment(bookingId);
        }
    }

    public void notifyPaymentFailed(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null && booking.getCustomerId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + booking.getCustomerId(),
                    "PAYMENT_FAILED:" + bookingId);
        }
    }

    // TÀI XẾ TỪ CHỐI CHỦ ĐỘNG
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

        DriverSummary driver = identityQueryService.getDriver(driverId);

        // Không cho nhận nếu đang thực hiện chuyến khác
        boolean driverBusy = bookingRepository.existsByDriverIdAndBookingStatusIn(
                driverId, List.of(BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS, BookingStatus.ARRIVED));
        if (driverBusy) {
            throw new IllegalStateException("Tài xế đang thực hiện chuyến khác");
        }

        booking.setDriverId(driver.driverId());
        booking.setBookingStatus(BookingStatus.ACCEPTED);
        booking.setPickupTime(Timestamp.valueOf(LocalDateTime.now()));

        Booking updated;
        try {
            updated = bookingRepository.save(booking);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("[Booking] Tranh chấp đồng thời: Chuyến {} đã bị nhận bởi tài xế khác", bookingId);
            throw new AppException(ErrorCode.BOOKING_ALREADY_TAKEN);
        }

        // Thông báo cho khách hàng
        notifyCustomerDriverAssigned(updated, driver);
        dispatcherService.resolveDispatch(bookingId, WaitResult.ACCEPTED);

        // Seed vị trí tức thì của tài xế từ Redis GEO vào booking location cache
        // Để khách hàng thấy ngay vị trí tài xế trên bản đồ mà không cần đợi WebSocket GPS
        String vehicleTypeId = driver.vehicleTypeId();
        DriverLocation geoPos = driverCacheService.getDriverPositionFromGeo(driverId, vehicleTypeId);
        if (geoPos != null) {
            driverCacheService.saveLocation(bookingId, geoPos.getLat(), geoPos.getLng());
            log.info("[Booking] Seed vị trí tài xế từ Redis GEO cho booking={}: ({}, {})", bookingId, geoPos.getLat(), geoPos.getLng());
        }

        log.info("[Booking] Gán tài xế thành công: booking={}", bookingId);
        return bookingQueryService.toDetail(updated);
    }

    @Transactional
    public BookingDetailResponse updateStatus(String bookingId, BookingStatus newStatus) {
        log.info("[Booking] Cập nhật trạng thái: booking={} → {}", bookingId, newStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        validateStatusTransition(booking.getBookingStatus(), newStatus);

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        switch (newStatus) {
            case ARRIVED -> booking.setPickupTime(now);
            case COMPLETED -> handleCompleteRide(booking, now);
            case CANCELLED -> driverCacheService.clearLocation(bookingId);
            default -> {
            }
        }

        booking.setBookingStatus(newStatus);
        Booking updated = bookingRepository.save(booking);

        broadcastStatusToCustomer(updated, newStatus);

        return bookingQueryService.toDetail(updated);
    }

    @Transactional
    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        if (booking.getDriverId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/driver/" + booking.getDriverId(),
                    "CUSTOMER_CANCELLED:" + bookingId);
        }

        driverCacheService.clearLocation(bookingId);
        dispatcherService.resolveDispatch(bookingId, WaitResult.CUSTOMER_CANCELLED);
        log.info("[Booking] Đã hủy booking {}", bookingId);
    }

    @Transactional
    public void cancelBookingByDriver(String bookingId, String driverId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getDriverId() == null || !booking.getDriverId().equals(driverId)) {
            throw new IllegalStateException("Tài xế không có quyền huỷ chuyến này");
        }

        Set<BookingStatus> cancellableStatuses = Set.of(BookingStatus.ACCEPTED, BookingStatus.ARRIVED);
        if (!cancellableStatuses.contains(booking.getBookingStatus())) {
            throw new IllegalStateException("Không thể huỷ chuyến xe ở trạng thái hiện tại");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        if (booking.getCustomerId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + booking.getCustomerId(),
                    "DRIVER_CANCELLED:" + bookingId);
        }

        driverCacheService.clearLocation(bookingId);
        log.info("[Booking] Tài xế {} đã hủy booking {}", driverId, bookingId);
    }

    @Transactional
    public BookingDetailResponse adminForceCancel(String bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        Set<BookingStatus> cancellable = Set.of(
                BookingStatus.QUEUED, BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.ARRIVED);
        if (!cancellable.contains(booking.getBookingStatus())) {
            throw new IllegalStateException("Chỉ có thể huỷ chuyến khi chưa đón khách (trạng thái hiện tại: "
                    + booking.getBookingStatus() + ")");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);

        if (booking.getDriverId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/driver/" + booking.getDriverId(),
                    "ADMIN_CANCELLED:" + bookingId);
        }
        if (booking.getCustomerId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + booking.getCustomerId(),
                    "ADMIN_CANCELLED:" + bookingId);
        }
        driverCacheService.clearLocation(bookingId);
        dispatcherService.resolveDispatch(bookingId, WaitResult.CUSTOMER_CANCELLED);
        log.info("[Admin] Đã huỷ booking {}", bookingId);
        return bookingQueryService.toDetail(updated);
    }

    @Transactional
    public BookingDetailResponse adminAssignDriver(String bookingId, String driverId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Chỉ có thể gán tài xế khi chuyến đang ở trạng thái PENDING");
        }
        if (booking.getDriverId() != null) {
            throw new IllegalStateException("Chuyến đã có tài xế");
        }
        DriverSummary driver = identityQueryService.getDriver(driverId);

        boolean driverBusy = bookingRepository.existsByDriverIdAndBookingStatusIn(
                driverId, List.of(BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS, BookingStatus.ARRIVED));
        if (driverBusy) {
            throw new IllegalStateException("Tài xế đang thực hiện chuyến khác");
        }

        booking.setDriverId(driver.driverId());
        booking.setBookingStatus(BookingStatus.ACCEPTED);
        booking.setPickupTime(Timestamp.valueOf(LocalDateTime.now()));
        Booking updated = bookingRepository.save(booking);
        notifyCustomerDriverAssigned(updated, driver);

        // Seed vị trí tức thì từ Redis GEO
        String vehicleTypeId = driver.vehicleTypeId();
        com.project.BookCarOnline.booking.dto.redis.DriverLocation geoPos = driverCacheService.getDriverPositionFromGeo(driverId, vehicleTypeId);
        if (geoPos != null) {
            driverCacheService.saveLocation(bookingId, geoPos.getLat(), geoPos.getLng());
        }

        log.info("[Admin] Gán tài xế {} vào booking {}", driverId, bookingId);
        return bookingQueryService.toDetail(updated);
    }

    @Transactional
    public BookingDetailResponse completeBooking(String bookingId) {
        return updateStatus(bookingId, BookingStatus.COMPLETED);
    }

    private void handleCompleteRide(Booking booking, Timestamp completedAt) {
        booking.setArrivalTime(completedAt);

        PaymentSummary payment = booking.getPaymentId() != null
                ? paymentService.get(booking.getPaymentId())
                : null;
        String driverId = booking.getDriverId();

        if (driverId == null || payment == null) {
            log.warn("[Booking] Thiếu driver/payment khi complete booking {}", booking.getBookingId());
            return;
        }

        double commission = booking.getTotalPrice() * platformCommissionRate;
        String typeDeduct = "FEE_BOOKING";
        if (PaymentMethod.CASH == payment.paymentMethod()) {
            walletService.deductBalance(driverId, commission, typeDeduct);
        } else {
            walletService.addBalance(driverId, booking.getTotalPrice() - commission);
        }

        identityQueryService.updateLastTripTime(driverId, LocalDateTime.now());
        driverCacheService.clearLocation(booking.getBookingId());

        // Xóa cache điểm số tài xế để tính lại lần sau
        driverCacheService.evictDriverStats(driverId);
    }

    private boolean isCashPayment(String paymentMethod) {
        return PaymentMethod.CASH.name().equalsIgnoreCase(paymentMethod != null ? paymentMethod : "ONLINE");
    }

    private void validateStatusTransition(BookingStatus current, BookingStatus next) {
        boolean valid = switch (next) {
            case ARRIVED -> current == BookingStatus.ACCEPTED;
            case IN_PROGRESS -> current == BookingStatus.ARRIVED;
            case COMPLETED -> current == BookingStatus.IN_PROGRESS;
            case CANCELLED -> current == BookingStatus.QUEUED
                    || current == BookingStatus.PENDING
                    || current == BookingStatus.ACCEPTED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Không thể chuyển trạng thái từ " + current + " sang " + next);
        }
    }

    private void notifyCustomerDriverAssigned(Booking booking, DriverSummary driver) {
        if (booking.getCustomerId() == null)
            return;
        String payload = "DRIVER_ASSIGNED:" + booking.getBookingId()
                + ":" + driver.driverName()
                + ":" + driver.phone()
                + ":" + driver.licensePlate();
        messagingTemplate.convertAndSend(
                "/topic/customer/" + booking.getCustomerId(), payload);
    }

    private void broadcastStatusToCustomer(Booking booking, BookingStatus status) {
        if (booking.getCustomerId() == null)
            return;
        messagingTemplate.convertAndSend(
                "/topic/customer/" + booking.getCustomerId(),
                "STATUS_UPDATE:" + booking.getBookingId() + ":" + status.name());
    }

    private Booking getBookingOrThrow(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
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
