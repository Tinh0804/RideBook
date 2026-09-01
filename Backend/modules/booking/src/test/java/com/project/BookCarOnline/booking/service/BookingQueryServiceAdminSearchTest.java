package com.project.BookCarOnline.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.project.BookCarOnline.booking.dto.request.AdminBookingSearchRequest;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.repository.BookingPromotionRepository;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.booking.repository.RatingRepository;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.finance.service.PaymentService;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.promotion.service.PricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookingQueryServiceAdminSearchTest {

    @Mock BookingRepository bookingRepository;
    @Mock BookingPromotionRepository bookingPromotionRepository;
    @Mock RatingRepository ratingRepository;
    @Mock IdentityQueryService identityQueryService;
    @Mock VehicleTypeService vehicleTypeService;
    @Mock PaymentService paymentService;
    @Mock PricingService pricingService;
    @InjectMocks BookingQueryService service;

    @Test
    void searchUsesValidatedPagingAndMultiFieldSort() {
        AdminBookingSearchRequest request = new AdminBookingSearchRequest();
        request.setPage(1);
        request.setSize(30);
        request.setSort("distance:desc,totalPrice:asc");
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<?> result = service.searchBookingsForAdmin(request);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(bookingRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(result).isEmpty();
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getSort().stream().map(order -> order.getProperty()))
                .containsExactly("distance", "totalPrice", "bookingId");
    }

    @Test
    void invalidRangeFailsBeforeRepositoryAccess() {
        AdminBookingSearchRequest request = new AdminBookingSearchRequest();
        request.setMinRating(5D);
        request.setMaxRating(2D);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.searchBookingsForAdmin(request))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(bookingRepository);
    }
}
