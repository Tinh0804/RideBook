package com.project.BookCarOnline.app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.BookCarOnline.identity.dto.request.AdminDriverFilter;
import com.project.BookCarOnline.identity.dto.request.AdminDriverSearchRequest;
import com.project.BookCarOnline.identity.dto.response.DriverDetailResponse;
import com.project.BookCarOnline.identity.service.DriverManagementService;
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
class AdminDriverControllerTest {

    @Mock
    DriverManagementService driverManagementService;

    @Test
    void searchUsesFrozenOperationIdAndDelegatesCriteria() throws Exception {
        AdminDriverController controller = new AdminDriverController(driverManagementService);
        AdminDriverSearchRequest request = new AdminDriverSearchRequest();
        Page<DriverDetailResponse> page = new PageImpl<>(List.of(
                DriverDetailResponse.builder().driverId("driver-1").build()));
        when(driverManagementService.search(request)).thenReturn(page);

        APIResponse<Page<DriverDetailResponse>> response = controller.getAllDrivers(request);

        assertThat(response.getResult()).isSameAs(page);
        verify(driverManagementService).search(request);
        Method method = AdminDriverController.class.getMethod(
                "getAllDrivers", AdminDriverSearchRequest.class);
        assertThat(method.getAnnotation(Operation.class).operationId()).isEqualTo("searchAdminDrivers");
    }

    @Test
    void exportStreamsUtf8CsvAttachment() throws Exception {
        AdminDriverController controller = new AdminDriverController(driverManagementService);
        AdminDriverFilter filter = new AdminDriverFilter();
        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(1);
            writer.write("\uFEFFdriverId\r\ndriver-1\r\n");
            return null;
        }).when(driverManagementService).export(any(AdminDriverFilter.class), any(Writer.class));

        ResponseEntity<StreamingResponseBody> response = controller.exportDrivers(filter);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        response.getBody().writeTo(output);

        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("text/csv;charset=UTF-8");
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .startsWith("attachment; filename=\"drivers-");
        assertThat(output.toString(StandardCharsets.UTF_8)).contains("driver-1");
        Method method = AdminDriverController.class.getMethod(
                "exportDrivers", AdminDriverFilter.class);
        assertThat(method.getAnnotation(Operation.class).operationId()).isEqualTo("exportAdminDrivers");
    }
}
