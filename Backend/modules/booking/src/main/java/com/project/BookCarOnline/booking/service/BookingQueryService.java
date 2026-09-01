package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.dto.response.AvailableRideResponse;
import com.project.BookCarOnline.booking.dto.response.BookingDetailResponse;
import com.project.BookCarOnline.booking.dto.response.BookingPromotionDTO;
import com.project.BookCarOnline.booking.dto.request.AdminBookingSearchRequest;
import com.project.BookCarOnline.booking.dto.request.AdminBookingFilter;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.BookingPromotion;
import com.project.BookCarOnline.booking.entity.Rating;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.repository.BookingPromotionRepository;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.booking.repository.RatingRepository;
import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.finance.dto.PaymentSummary;
import com.project.BookCarOnline.finance.service.PaymentService;
import com.project.BookCarOnline.identity.dto.summary.CustomerSummary;
import com.project.BookCarOnline.identity.dto.summary.DriverSummary;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.promotion.dto.PromotionQuote;
import com.project.BookCarOnline.promotion.service.PricingService;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.shared.util.CsvUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingQueryService {

    private final BookingRepository bookingRepository;
    private final BookingPromotionRepository bookingPromotionRepository;
    private final RatingRepository ratingRepository;
    private final IdentityQueryService identityQueryService;
    private final VehicleTypeService vehicleTypeService;
    private final PaymentService paymentService;
    private final PricingService pricingService;

    public List<BookingDetailResponse> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::toDetail).toList();
    }

    public Map<String, Object> getAdminSummary() {
        List<Booking> bookings = bookingRepository.findAll();
        long completed = 0;
        long cancelled = 0;
        double revenue = 0;
        for (Booking booking : bookings) {
            switch (booking.getBookingStatus()) {
                case COMPLETED -> {
                    completed++;
                    revenue += booking.getTotalPrice() != null ? booking.getTotalPrice() : 0;
                }
                case CANCELLED -> cancelled++;
                default -> {
                }
            }
        }
        return Map.of(
                "totalBookings", (long) bookings.size(),
                "completedRides", completed,
                "cancelledRides", cancelled,
                "totalRevenue", revenue);
    }

    public Page<BookingDetailResponse> searchBookingsForAdmin(AdminBookingSearchRequest request) {
        Pageable pageable = request.toPageable();
        return bookingRepository.findAll(buildAdminSpecification(request), pageable).map(this::toDetail);
    }

    public void writeAdminBookingsCsv(AdminBookingFilter request, Writer writer) {
        request.validateRanges();
        CsvUtils.writeBom(writer);
        CsvUtils.writeRow(writer,
                "bookingId", "customerId", "customerName", "customerPhone", "driverId", "driverName",
                "driverPhone", "vehicleTypeName", "licensePlate", "pickupLocation", "dropoffLocation",
                "originalPrice", "totalPrice", "bookingTime", "pickupTime", "arrivalTime", "bookingStatus",
                "distance", "paymentMethod", "paymentStatus", "rating", "review");

        int pageNumber = 0;
        Page<Booking> bookings;
        do {
            bookings = bookingRepository.findAll(
                    buildAdminSpecification(request), request.toExportPageable(pageNumber, 500));
            for (Booking booking : bookings.getContent()) {
                BookingDetailResponse row = toDetail(booking);
                CsvUtils.writeRow(writer,
                        row.getBookingId(), row.getCustomerId(), row.getCustomerName(), row.getCustomerPhone(),
                        row.getDriverId(), row.getDriverName(), row.getDriverPhone(), row.getVehicleTypeName(),
                        row.getLicensePlate(), row.getPickupLocation(), row.getDropoffLocation(),
                        row.getOriginalPrice(), row.getTotalPrice(), row.getBookingTime(), row.getPickupTime(),
                        row.getArrivalTime(), row.getBookingStatus(), row.getDistance(), row.getPaymentMethod(),
                        row.getPaymentStatus(), row.getRating(), row.getReview());
            }
            pageNumber++;
        } while (bookings.hasNext());
        try {
            writer.flush();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private Specification<Booking> buildAdminSpecification(AdminBookingFilter request) {
        request.validateRanges();
        String normalizedSearch = request.normalizedSearch();
        String searchPattern = normalizedSearch != null ? "%" + normalizedSearch + "%" : null;
        List<String> customerIds = searchPattern != null
                ? identityQueryService.searchCustomerIds(searchPattern)
                : List.of();
        List<String> driverIds = searchPattern != null
                ? identityQueryService.searchDriverIds(searchPattern)
                : List.of();

        Specification<Booking> specification = (root, query, criteriaBuilder) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
                predicates.add(root.get("bookingStatus").in(request.getStatuses()));
            }
            if (request.getBookingFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("bookingTime"), Timestamp.valueOf(request.getBookingFrom())));
            }
            if (request.getBookingTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("bookingTime"), Timestamp.valueOf(request.getBookingTo())));
            }
            if (request.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalPrice"), request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalPrice"), request.getMaxPrice()));
            }
            if (request.getMinRating() != null || request.getMaxRating() != null) {
                Subquery<String> ratingSubquery = query.subquery(String.class);
                Root<Rating> rating = ratingSubquery.from(Rating.class);
                List<Predicate> ratingPredicates = new ArrayList<>();
                if (request.getMinRating() != null) {
                    ratingPredicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            rating.get("score"), request.getMinRating()));
                }
                if (request.getMaxRating() != null) {
                    ratingPredicates.add(criteriaBuilder.lessThanOrEqualTo(
                            rating.get("score"), request.getMaxRating()));
                }
                ratingSubquery.select(rating.get("bookingNo").get("bookingId"))
                        .where(ratingPredicates.toArray(new Predicate[0]));
                predicates.add(root.get("bookingId").in(ratingSubquery));
            }
            if (searchPattern != null) {
                List<Predicate> searchPredicates = new ArrayList<>();
                if (!customerIds.isEmpty()) {
                    searchPredicates.add(root.get("customerId").in(customerIds));
                }
                if (!driverIds.isEmpty()) {
                    searchPredicates.add(root.get("driverId").in(driverIds));
                }
                searchPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("bookingId").as(String.class)), searchPattern));
                searchPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("pickupLocation")), searchPattern));
                searchPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("dropoffLocation")), searchPattern));
                predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return specification;
    }

    public BookingDetailResponse getBookingById(String bookingId) {
        return toDetail(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND)));
    }

    public List<BookingDetailResponse> getBookingsByCustomer(String customerId) {
        return bookingRepository.findByCustomerIdOrderByBookingTimeDesc(customerId)
                .stream().map(this::toDetail).toList();
    }

    public BookingDetailResponse getActiveBookingByCustomer(String customerId) {
        return bookingRepository.findActiveByCustomer(customerId)
                .stream().findFirst().map(this::toDetail).orElse(null);
    }

    public List<BookingDetailResponse> getBookingsByDriver(String driverId) {
        return bookingRepository.findByDriverIdOrderByBookingTimeDesc(driverId)
                .stream().map(this::toDetail).toList();
    }

    public BookingDetailResponse getActiveBookingByDriver(String driverId) {
        return bookingRepository.findActiveByDriver(driverId)
                .stream().findFirst().map(this::toDetail).orElse(null);
    }

    public Page<BookingDetailResponse> getBookingsByDriverPaginated(
            String driverId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            return bookingRepository.findByDriverIdAndBookingStatusOrderByBookingTimeDesc(
                    driverId, BookingStatus.valueOf(status), pageable).map(this::toDetail);
        }
        return bookingRepository.findByDriverIdOrderByBookingTimeDesc(driverId, pageable).map(this::toDetail);
    }

    public List<AvailableRideResponse> getAvailableRides(String driverArea) {
        List<Booking> available = driverArea != null && !driverArea.isBlank()
                ? bookingRepository.findByBookingStatusAndDriverIdIsNullAndPickupLocationContainingOrderByBookingTimeAsc(
                        BookingStatus.PENDING, extractCityFromAddress(driverArea))
                : bookingRepository.findByBookingStatusAndDriverIdIsNullOrderByBookingTimeAsc(BookingStatus.PENDING);
        return available.stream().map(this::toAvailableRide).toList();
    }

    BookingDetailResponse toDetail(Booking booking) {
        Rating rating = ratingRepository.findByBookingNo_BookingId(booking.getBookingId()).orElse(null);
        List<BookingPromotion> bookingPromotions =
                bookingPromotionRepository.findByBooking_BookingId(booking.getBookingId());
        Map<String, PromotionQuote> promotions = pricingService
                .getPromotions(bookingPromotions.stream().map(BookingPromotion::getPromotionId).toList())
                .stream()
                .collect(Collectors.toMap(PromotionQuote::promotionId, promotion -> promotion));
        List<BookingPromotionDTO> promotionResponses = bookingPromotions.stream()
                .map(bookingPromotion -> {
                    PromotionQuote promotion = promotions.get(bookingPromotion.getPromotionId());
                    return BookingPromotionDTO.builder()
                            .promotionCode(promotion != null ? promotion.promotionCode() : null)
                            .promotionName(promotion != null ? promotion.promotionName() : null)
                            .discountAmount(bookingPromotion.getDiscountAmount())
                            .build();
                })
                .toList();

        CustomerSummary customer = booking.getCustomerId() != null
                ? identityQueryService.getCustomer(booking.getCustomerId())
                : null;
        DriverSummary driver = booking.getDriverId() != null
                ? identityQueryService.getDriver(booking.getDriverId())
                : null;
        VehicleTypeSummary vehicleType = booking.getVehicleTypeId() != null
                ? vehicleTypeService.getVehicleTypeSummary(booking.getVehicleTypeId())
                : null;
        PaymentSummary payment = booking.getPaymentId() != null
                ? paymentService.get(booking.getPaymentId())
                : null;

        return BookingDetailResponse.builder()
                .bookingId(booking.getBookingId())
                .customerId(customer != null ? customer.customerId() : null)
                .customerName(customer != null ? customer.customerName() : null)
                .customerPhone(customer != null ? customer.phone() : null)
                .driverId(driver != null ? driver.driverId() : null)
                .driverName(driver != null ? driver.driverName() : null)
                .driverPhone(driver != null ? driver.phone() : null)
                .vehicleTypeName(vehicleType != null ? vehicleType.vehicleTypeName() : null)
                .vehicleTypeIcon(vehicleType != null ? vehicleType.icon() : null)
                .licensePlate(driver != null ? driver.licensePlate() : null)
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
                .paymentMethod(payment != null && payment.paymentMethod() != null
                        ? payment.paymentMethod().name()
                        : null)
                .paymentStatus(payment != null ? payment.paid() : null)
                .appliedPromotions(promotionResponses)
                .rating(rating != null ? rating.getScore() : null)
                .review(rating != null ? rating.getReview() : null)
                .build();
    }

    private AvailableRideResponse toAvailableRide(Booking booking) {
        return AvailableRideResponse.builder()
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomerId())
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

    private String extractCityFromAddress(String address) {
        String[] parts = address.split(",");
        return parts[parts.length - 1].trim();
    }
}
