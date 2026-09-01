package com.project.BookCarOnline.booking.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái vòng đời booking")
public enum BookingStatus {
    PENDING,
    ACCEPTED,
    ARRIVED,
    IN_PROGRESS,
    CANCELLED,
    COMPLETED
}
