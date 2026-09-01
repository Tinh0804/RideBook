package com.project.BookCarOnline.app.service;

import com.project.BookCarOnline.app.dto.reporting.AdminStatsResponse;
import com.project.BookCarOnline.booking.dto.response.MonthlyStatProjection;
import com.project.BookCarOnline.app.dto.reporting.MonthlyStatResponse;
import com.project.BookCarOnline.booking.service.BookingReportingService;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final IdentityQueryService identityQueryService;
    private final BookingReportingService bookingReportingService;

    public AdminStatsResponse getOverviewStats(String period, int year) {
        LocalDateTime now = LocalDateTime.now();
        BookingReportingService.AdminBookingStats bookingStats =
                bookingReportingService.adminStats(period, year, now);
        List<MonthlyStatResponse> revenueByMonth = processStats(bookingStats.revenue(), bookingStats.period(), now);
        List<MonthlyStatResponse> tripsByMonth = processStats(bookingStats.trips(), bookingStats.period(), now);

        return AdminStatsResponse.builder()
                .totalCustomers(identityQueryService.countCustomers())
                .totalDrivers(identityQueryService.countDrivers())
                .totalBookings(bookingStats.totalBookings())
                .totalRevenue(bookingStats.totalRevenue())
                .revenueByMonth(revenueByMonth)
                .tripsByMonth(tripsByMonth)
                .build();
    }

    private List<MonthlyStatResponse> processStats(List<MonthlyStatProjection> rawStats, String period, LocalDateTime referenceDate) {
        List<MonthlyStatResponse> result = new ArrayList<>();

        if ("DAY".equalsIgnoreCase(period)) {
            // 24 hours: 0 to 23
            for (int i = 0; i < 24; i++) {
                result.add(new MonthlyStatResponse(String.format("%02d:00", i), 0.0));
            }
            for (MonthlyStatProjection stat : rawStats) {
                if (stat.getMonth() != null && stat.getMonth() >= 0 && stat.getMonth() < 24) {
                    result.get(stat.getMonth()).setValue(stat.getValue() != null ? stat.getValue() : 0.0);
                }
            }
        } else if ("WEEK".equalsIgnoreCase(period)) {
            // 7 days: 1 (Monday) to 7 (Sunday)
            String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            for (int i = 0; i < 7; i++) {
                result.add(new MonthlyStatResponse(days[i], 0.0));
            }
            for (MonthlyStatProjection stat : rawStats) {
                if (stat.getMonth() != null && stat.getMonth() >= 1 && stat.getMonth() <= 7) {
                    result.get(stat.getMonth() - 1).setValue(stat.getValue() != null ? stat.getValue() : 0.0);
                }
            }
        } else if ("MONTH".equalsIgnoreCase(period)) {
            // Days in month
            int daysInMonth = referenceDate.toLocalDate().lengthOfMonth();
            for (int i = 1; i <= daysInMonth; i++) {
                result.add(new MonthlyStatResponse(String.format("%02d", i), 0.0));
            }
            for (MonthlyStatProjection stat : rawStats) {
                if (stat.getMonth() != null && stat.getMonth() >= 1 && stat.getMonth() <= daysInMonth) {
                    result.get(stat.getMonth() - 1).setValue(stat.getValue() != null ? stat.getValue() : 0.0);
                }
            }
        } else {
            // YEAR
            for (int i = 1; i <= 12; i++) {
                result.add(new MonthlyStatResponse("T" + i, 0.0));
            }
            for (MonthlyStatProjection stat : rawStats) {
                if (stat.getMonth() != null && stat.getMonth() >= 1 && stat.getMonth() <= 12) {
                    result.get(stat.getMonth() - 1).setValue(stat.getValue() != null ? stat.getValue() : 0.0);
                }
            }
        }

        return result;
    }
}
