package com.project.BookCarOnline.Exception;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(1000,"Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    KEY_INVALID(401,"Key invalid",HttpStatus.BAD_REQUEST),
    USER_EXITED(401,"User existed",HttpStatus.BAD_REQUEST),
    USER_EXISTED(401,"User existed",HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(402,"Username must be at least {min} character",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(402,"Password must be at least {min} character",HttpStatus.BAD_REQUEST),
    USER_NOT_EXITED(404,"User not existed",HttpStatus.NOT_FOUND),
    UNAUTHENTACATED(403,"Unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZE(401,"You do not have permission",HttpStatus.UNAUTHORIZED),
    INVALID_DOB(402,"Your age must be at least {min}",HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(402,"Your token invalid",HttpStatus.UNAUTHORIZED),
    ROLE_NOT_FOUND(402,"Role not found",HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(403,"You haven't permission to access this endpoint",HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTS(402,"Role not exists",HttpStatus.BAD_REQUEST),
    AUTHENTICATION_SERVICE_ERROR(500,"Authentication service error",HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_NOT_FOUND(404,"Account not found",HttpStatus.NOT_FOUND),
    ACCOUNT_DISABLED(403,"Account disabled",HttpStatus.FORBIDDEN),
    LOGIN_FAILED(401,"Login failed",HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_ACTIVE(403,"Account not active",HttpStatus.FORBIDDEN),
    EXCHANGE_TOKEN_FAIL(403,"Exchange token fail",HttpStatus.FORBIDDEN),
    TOKEN_NOT_FOUND(404,"Token not found",HttpStatus.NOT_FOUND),
    TOKEN_BLACKLISTED(403,"Token blacklisted",HttpStatus.FORBIDDEN),
    PROFILE_NOT_FOUND(404,"Profile not found",HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_EXISTS(404,"Account not exists",HttpStatus.NOT_FOUND),
    AVATAR_NOT_FOUND(404,"Avatar not found",HttpStatus.NOT_FOUND),
    USERNAME_OR_PASSWORD_INVALID(401,"Username or password invalid",HttpStatus.UNAUTHORIZED),
    
    // Driver related errors
    DRIVER_EMAIL_EXISTED(400,"Email tài xế đã tồn tại",HttpStatus.BAD_REQUEST),
    DRIVER_PHONE_EXISTED(400,"Số điện thoại tài xế đã tồn tại",HttpStatus.BAD_REQUEST),
    DRIVER_CITIZEN_ID_EXISTED(400,"CCCD đã tồn tại",HttpStatus.BAD_REQUEST),
    DRIVER_LICENSE_PLATE_EXISTED(400,"Biển số xe đã tồn tại",HttpStatus.BAD_REQUEST),
    DRIVER_NOT_FOUND(404,"Tài xế không tồn tại",HttpStatus.NOT_FOUND),
    DRIVER_ON_RIDE(400,"Tài xế đang thực hiện chuyến khác",HttpStatus.BAD_REQUEST),

    // Booking related errors
    BOOKING_NOT_FOUND(404,"Chuyến xe không tồn tại",HttpStatus.NOT_FOUND),
    BOOKING_NOT_AVAILABLE(400,"Chuyến xe không còn khả dụng",HttpStatus.BAD_REQUEST),
    BOOKING_NOT_STARTED(400,"Chuyến xe chưa được bắt đầu",HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_CANCELLED(400,"Chuyến xe đã bị huỷ",HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_TAKEN(400,"Chuyến xe đã được nhận bởi tài xế khác",HttpStatus.BAD_REQUEST),
    
    
    // Validation errors
    INVALID_INPUT(400,"Dữ liệu đầu vào không hợp lệ",HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD(400,"Thiếu trường bắt buộc",HttpStatus.BAD_REQUEST),

    NO_DRIVER_AVAILABLE(400,"Không có tài xế khả dụng",HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_METHOD(400,"Phương thức thanh toán không hợp lệ",HttpStatus.BAD_REQUEST),
    WALLET_NOT_FOUND(404,"Ví không tồn tại",HttpStatus.NOT_FOUND),
    CUSTOMER_NOT_FOUND(404,"Khách hàng không tồn tại",HttpStatus.NOT_FOUND),

    // Promotion related errors
    PROMOTION_NOT_FOUND(404, "Không tìm thấy mã khuyến mãi", HttpStatus.NOT_FOUND),
    PROMOTION_ALREADY_EXISTS(400, "Mã khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST),
    PROMOTION_EXPIRED(400, "Mã khuyến mãi đã hết hạn", HttpStatus.BAD_REQUEST),
    PROMOTION_OUT_OF_STOCK(400, "Mã khuyến mãi đã hết lượt sử dụng", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_ACTIVE(400, "Mã khuyến mãi hiện không khả dụng", HttpStatus.BAD_REQUEST),
    QUOTE_EXPIRED(400, "Báo giá đã hết hạn hoặc không tồn tại, vui lòng lấy giá mới", HttpStatus.BAD_REQUEST),
    CUSTOMER_PROMOTION_NOT_FOUND(404, "Khuyến mãi của khách hàng không tồn tại", HttpStatus.NOT_FOUND),

    // Vehicle type related errors
    VEHICLE_TYPE_NOT_FOUND(404, "Loại xe không tồn tại", HttpStatus.NOT_FOUND),
    VEHICLE_TYPE_ALREADY_EXISTS(400, "Loại xe đã tồn tại", HttpStatus.BAD_REQUEST),


    //Time related errors
    TIME_NOT_FOUND(404, "Time slot không tồn tại", HttpStatus.NOT_FOUND),
    TIME_SLOT_ALREADY_EXISTS(400, "Time slot đã tồn tại", HttpStatus.BAD_REQUEST),
    TIME_SLOT_NOT_ACTIVE(400, "Time slot hiện không khả dụng", HttpStatus.BAD_REQUEST);

    private final int status;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int status,String message,HttpStatusCode statusCode){
        this.status=status;
        this.message=message;
        this.statusCode=statusCode;
    }
}
