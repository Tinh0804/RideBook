package com.project.BookCarOnline.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.BookCarOnline.shared.dto.APIResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandleTest {

    @Test
    void queryParameterTypeMismatchReturnsBadRequestEnvelope() {
        ConversionFailedException cause = new ConversionFailedException(
                TypeDescriptor.valueOf(String.class),
                TypeDescriptor.valueOf(Integer.class),
                "invalid",
                new IllegalArgumentException("invalid"));
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "invalid", Integer.class, "page", null, cause);

        ResponseEntity<APIResponse> response = new GlobalExceptionHandle().handleQueryParameterException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("page");
    }
}
