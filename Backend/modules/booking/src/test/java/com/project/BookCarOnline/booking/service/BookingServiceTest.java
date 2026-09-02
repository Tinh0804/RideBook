package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.config.BookingSchedulingProperties;
import com.project.BookCarOnline.booking.dto.redis.FareQuote;
import com.project.BookCarOnline.booking.dto.request.CreateBookingRequest;
import com.project.BookCarOnline.booking.dto.response.BookingDetailResponse;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.repository.BookingPromotionRepository;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.finance.dto.PaymentSummary;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.service.PaymentService;
import com.project.BookCarOnline.identity.dto.summary.CustomerSummary;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    PaymentService paymentService;

    @Mock
    RideDispatcherService rideDispatcherService;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Mock
    BookingQuoteService bookingQuoteService;

    @Mock
    VehicleTypeService vehicleTypeService;

    @Mock
    IdentityQueryService identityQueryService;

    @Mock
    BookingPromotionRepository bookingPromotionRepository;

    @Mock
    BookingQueryService bookingQueryService;

    @Mock
    PaymentTimeoutService paymentTimeoutService;

    @Mock
    BookingSchedulingProperties bookingSchedulingProperties;

    @InjectMocks
    BookingService bookingService;

    @Test
    void duplicateSuccessfulCallbackDoesNotDispatchAgain() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .paymentId("payment-1")
                .build();
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.ONLINE, 100_000D, true));

        bookingService.confirmOnlinePayment("booking-1");

        verify(paymentService, never()).markPaid("payment-1");
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void successfulPaymentKeepsScheduledBookingQueuedWithoutImmediateDispatch() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .customerId("customer-1")
                .paymentId("payment-1")
                .scheduledAt(LocalDateTime.of(2026, 9, 2, 8, 30))
                .bookingStatus(BookingStatus.QUEUED)
                .build();
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.ONLINE, 100_000D, false));

        bookingService.confirmOnlinePayment("booking-1");

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.QUEUED);
        verify(paymentService).markPaid("payment-1");
        verify(bookingRepository).save(booking);
        verify(rideDispatcherService, never()).dispatchNearbyDrivers(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyDouble(),
                org.mockito.ArgumentMatchers.anyDouble(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createsFutureCashBookingAsQueuedWithoutImmediateDispatch() {
        LocalDateTime scheduledAt = LocalDateTime.of(2099, 9, 2, 8, 30);
        FareQuote quote = FareQuote.builder()
                .quoteId("quote-1")
                .vehicleTypeId("vehicle-1")
                .distance(8.5D)
                .originalPrice(120_000D)
                .totalPrice(100_000D)
                .promotionIds(List.of())
                .build();
        CreateBookingRequest request = CreateBookingRequest.builder()
                .customerId("customer-1")
                .quoteId("quote-1")
                .pickupLocation("Pickup")
                .dropoffLocation("Dropoff")
                .pickupLat(10.75D)
                .pickupLng(106.67D)
                .distance(8.5D)
                .vehicleTypeId("vehicle-1")
                .paymentMethod("CASH")
                .scheduledAt(scheduledAt)
                .build();

        when(bookingSchedulingProperties.getZone()).thenReturn("Asia/Ho_Chi_Minh");
        when(bookingQuoteService.getQuote("quote-1")).thenReturn(quote);
        when(vehicleTypeService.getVehicleTypeSummary("vehicle-1"))
                .thenReturn(new VehicleTypeSummary("vehicle-1", "Car", 10_000D, 4, null));
        when(identityQueryService.resolveCustomer("customer-1"))
                .thenReturn(new CustomerSummary("customer-1", "Customer", "0900000000", null));
        when(paymentService.create(PaymentMethod.CASH, 100_000D, true))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.CASH, 100_000D, true));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setBookingId("booking-1");
            return booking;
        });
        when(bookingQueryService.toDetail(any(Booking.class)))
                .thenReturn(BookingDetailResponse.builder().bookingId("booking-1").build());

        bookingService.createBooking(request);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getScheduledAt()).isEqualTo(scheduledAt);
        assertThat(bookingCaptor.getValue().getBookingStatus()).isEqualTo(BookingStatus.QUEUED);
        verify(rideDispatcherService, never()).dispatchNearbyDrivers(any(),
                org.mockito.ArgumentMatchers.anyDouble(),
                org.mockito.ArgumentMatchers.anyDouble(),
                org.mockito.ArgumentMatchers.any());
        verify(paymentTimeoutService, never()).schedulePaymentTimeout(any(),
                org.mockito.ArgumentMatchers.anyLong());
    }
}
