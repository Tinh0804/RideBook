package com.project.BookCarOnline.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.BookCarOnline.booking.dto.request.AdminBookingFilter;
import com.project.BookCarOnline.booking.dto.request.AdminBookingSearchRequest;
import com.project.BookCarOnline.booking.dto.response.BookingDetailResponse;
import com.project.BookCarOnline.booking.service.BookingQueryService;
import com.project.BookCarOnline.booking.service.BookingService;
import com.project.BookCarOnline.shared.dto.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@ExtendWith(MockitoExtension.class)
class AdminBookingControllerTest {

    @Mock BookingService bookingService;
    @Mock BookingQueryService bookingQueryService;

    @Test
    void searchUsesFrozenOperationIdAndDelegatesCriteria() throws Exception {
        AdminBookingController controller = new AdminBookingController(bookingService, bookingQueryService);
        AdminBookingSearchRequest request = new AdminBookingSearchRequest();
        Page<BookingDetailResponse> page = new PageImpl<>(List.of(
                BookingDetailResponse.builder().bookingId("booking-1").build()));
        when(bookingQueryService.searchBookingsForAdmin(request)).thenReturn(page);

        APIResponse<Page<BookingDetailResponse>> response = controller.searchBookings(request);

        assertThat(response.getResult()).isSameAs(page);
        verify(bookingQueryService).searchBookingsForAdmin(request);
        Method method = AdminBookingController.class.getMethod(
                "searchBookings", AdminBookingSearchRequest.class);
        assertThat(method.getAnnotation(Operation.class).operationId()).isEqualTo("searchAdminBookings");
    }

    @Test
    void exportStreamsUtf8CsvAttachment() throws Exception {
        AdminBookingController controller = new AdminBookingController(bookingService, bookingQueryService);
        AdminBookingFilter filter = new AdminBookingFilter();
        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(1);
            writer.write("\uFEFFbookingId\r\nbooking-1\r\n");
            return null;
        }).when(bookingQueryService).writeAdminBookingsCsv(any(AdminBookingFilter.class), any(Writer.class));

        ResponseEntity<StreamingResponseBody> response = controller.exportBookings(filter);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        response.getBody().writeTo(output);

        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("text/csv;charset=UTF-8");
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .startsWith("attachment; filename=\"bookings-");
        assertThat(output.toString(StandardCharsets.UTF_8)).contains("booking-1");
        Method method = AdminBookingController.class.getMethod("exportBookings", AdminBookingFilter.class);
        assertThat(method.getAnnotation(Operation.class).operationId()).isEqualTo("exportAdminBookings");
    }
}
