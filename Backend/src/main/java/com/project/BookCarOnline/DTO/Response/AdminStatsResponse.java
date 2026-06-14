package com.project.BookCarOnline.DTO.Response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminStatsResponse {
    long totalCustomers;
    long totalDrivers;
    long totalBookings;
    double totalRevenue;

    List<MonthlyStatResponse> revenueByMonth;
    List<MonthlyStatResponse> tripsByMonth;
}
