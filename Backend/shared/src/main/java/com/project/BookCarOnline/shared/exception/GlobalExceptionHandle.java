package com.project.BookCarOnline.shared.exception;

import com.project.BookCarOnline.shared.dto.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandle {
    @ExceptionHandler(value = org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<APIResponse> handleQueryParameterException(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception) {
        String message = "Tham số " + exception.getName() + " không hợp lệ";
        return ResponseEntity.badRequest().body(APIResponse.builder()
                .status(400)
                .message(message)
                .build());
    }

    @ExceptionHandler(value = org.springframework.validation.BindException.class)
    public ResponseEntity<APIResponse> handleBindException(org.springframework.validation.BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((first, second) -> first + "; " + second)
                .orElse("Tham số truy vấn không hợp lệ");
        return ResponseEntity.badRequest().body(APIResponse.builder()
                .status(400)
                .message(message)
                .build());
    }

    @ExceptionHandler(value= RuntimeException.class)
    public ResponseEntity<APIResponse> handleRuntime(RuntimeException exception) {
        APIResponse apiResponse=APIResponse.builder()
                .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatus())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage() + ": " + exception.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<APIResponse> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(APIResponse.builder()
                        .status(errorCode.getStatus())
                        .message(errorCode.getMessage())
                        .build());
    }
    @ExceptionHandler(value = JwtException.class)
    public ResponseEntity<APIResponse> handleJWT(JwtException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;
        String message = exception.getMessage();

        return ResponseEntity.badRequest()
                .body(APIResponse.builder()
                        .status(errorCode.getStatus())
                        .message(message)
                        .build());
    }
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<APIResponse> handleAccessDenied(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        String message = exception.getMessage();

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(APIResponse.builder()
                        .status(errorCode.getStatus())
                        .message(message)
                        .build());
    }
    @ExceptionHandler(value = AuthenticationServiceException.class)
    public ResponseEntity<APIResponse> handleAuthenticationServiceException(AuthenticationServiceException exception) {
        ErrorCode errorCode = ErrorCode.AUTHENTICATION_SERVICE_ERROR;
        String message = exception.getMessage();

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(APIResponse.builder()
                        .status(errorCode.getStatus())
                        .message(message)
                        .build());
    }


    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<APIResponse> handleIllegalStateException(IllegalStateException exception) {
        String message = exception.getMessage();

        return ResponseEntity.badRequest()
                .body(APIResponse.builder()
                        .status(400)
                        .message(message != null ? message : "Trạng thái không hợp lệ")
                        .build());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<APIResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        String message = exception.getMessage();

        return ResponseEntity.badRequest()
                .body(APIResponse.builder()
                        .status(400)
                        .message(message != null ? message : "Tham số không hợp lệ")
                        .build());
    }

    @ExceptionHandler(value = org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException exception) {
        
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Dữ liệu không hợp lệ");

        return ResponseEntity.badRequest()
                .body(APIResponse.builder()
                        .status(400)
                        .message(message)
                        .build());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<APIResponse> handleGenericException(Exception exception) {
        exception.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.builder()
                        .status(500)
                        .message("Đã xảy ra lỗi: " + exception.getClass().getSimpleName() + " - " + exception.getMessage())
                        .build());
    }

}
