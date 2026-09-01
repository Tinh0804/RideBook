package com.project.BookCarOnline.identity.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.BookCarOnline.identity.dto.request.AdminCustomerFilter;
import com.project.BookCarOnline.identity.dto.request.AdminCustomerSearchRequest;
import com.project.BookCarOnline.identity.dto.response.CustomerResponse;
import com.project.BookCarOnline.identity.service.CustomerService;
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
class AdminCustomerControllerTest {

    @Mock
    CustomerService customerService;

    @Test
    void searchUsesFrozenOperationIdAndDelegatesCriteria() throws Exception {
        AdminCustomerController controller = new AdminCustomerController(customerService);
        AdminCustomerSearchRequest request = new AdminCustomerSearchRequest();
        Page<CustomerResponse> page = new PageImpl<>(List.of(
                CustomerResponse.builder().customerId("customer-1").build()));
        when(customerService.search(request)).thenReturn(page);

        APIResponse<Page<CustomerResponse>> response = controller.getAllCustomer(request);

        assertThat(response.getResult()).isSameAs(page);
        verify(customerService).search(request);
        Method method = AdminCustomerController.class.getMethod(
                "getAllCustomer", AdminCustomerSearchRequest.class);
        assertThat(method.getAnnotation(Operation.class).operationId()).isEqualTo("searchAdminCustomers");
    }

    @Test
    void exportStreamsUtf8CsvAttachment() throws Exception {
        AdminCustomerController controller = new AdminCustomerController(customerService);
        AdminCustomerFilter filter = new AdminCustomerFilter();
        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(1);
            writer.write("\uFEFFcustomerId\r\ncustomer-1\r\n");
            return null;
        }).when(customerService).export(any(AdminCustomerFilter.class), any(Writer.class));

        ResponseEntity<StreamingResponseBody> response = controller.exportCustomers(filter);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        response.getBody().writeTo(output);

        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("text/csv;charset=UTF-8");
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .startsWith("attachment; filename=\"customers-");
        assertThat(output.toString(StandardCharsets.UTF_8)).contains("customer-1");
        Method method = AdminCustomerController.class.getMethod(
                "exportCustomers", AdminCustomerFilter.class);
        assertThat(method.getAnnotation(Operation.class).operationId()).isEqualTo("exportAdminCustomers");
    }
}
