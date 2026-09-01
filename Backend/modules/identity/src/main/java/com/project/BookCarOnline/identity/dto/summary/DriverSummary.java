package com.project.BookCarOnline.identity.dto.summary;

import java.sql.Timestamp;

public record DriverSummary(
        String driverId,
        String driverName,
        String phone,
        String vehicleTypeId,
        String licensePlate,
        String area,
        Double currentLat,
        Double currentLng,
        Timestamp lastTripTime,
        Double score,
        Boolean activityStatus,
        AccountSummary account) {
}
