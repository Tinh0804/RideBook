package com.project.BookCarOnline.DTO.Response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverDashboardResponse {
    long totalRides;
    double totalIncome;
    double todayIncome;
    double averageRating;
}
