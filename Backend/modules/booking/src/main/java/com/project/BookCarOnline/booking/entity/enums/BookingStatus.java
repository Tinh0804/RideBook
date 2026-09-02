package com.project.BookCarOnline.booking.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái chuyến; QUEUED là chuyến hẹn giờ chưa vào cửa sổ điều phối")
public enum BookingStatus {
    QUEUED,
    PENDING,
    ACCEPTED,
    ARRIVED,
    IN_PROGRESS,
    CANCELLED,
    COMPLETED
}
