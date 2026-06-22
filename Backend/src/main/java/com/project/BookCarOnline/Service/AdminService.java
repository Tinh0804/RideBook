package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Response.AdminStatsResponse;
import com.project.BookCarOnline.DTO.Response.MonthlyStatProjection;
import com.project.BookCarOnline.DTO.Response.MonthlyStatResponse;
import com.project.BookCarOnline.Repository.CustomerRepository;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Repository.RideBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;
    private final RideBookRepository rideBookRepository;

    public AdminStatsResponse getOverviewStats(String period, int year) {
        long totalCustomers = customerRepository.count();
        long totalDrivers = driverRepository.count();
        long totalBookings = rideBookRepository.count();
        Double totalRev = rideBookRepository.calculateTotalRevenue();
        double totalRevenue = totalRev != null ? totalRev : 0.0;

        List<MonthlyStatProjection> rawRevenue;
        List<MonthlyStatProjection> rawTrips;

        LocalDateTime now = LocalDateTime.now();

        if ("DAY".equalsIgnoreCase(period)) {
            LocalDateTime start = now.with(LocalTime.MIN);
            LocalDateTime end = now.with(LocalTime.MAX);
            rawRevenue = rideBookRepository.getRevenueByHour(start, end);
            rawTrips = rideBookRepository.getTripsByHour(start, end);
        } else if ("WEEK".equalsIgnoreCase(period)) {
            LocalDateTime start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
            LocalDateTime end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
            rawRevenue = rideBookRepository.getRevenueByDayOfWeek(start, end);
            rawTrips = rideBookRepository.getTripsByDayOfWeek(start, end);
        } else if ("MONTH".equalsIgnoreCase(period)) {
            LocalDateTime start = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
            LocalDateTime end = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
            rawRevenue = rideBookRepository.getRevenueByDayOfMonth(start, end);
            rawTrips = rideBookRepository.getTripsByDayOfMonth(start, end);
        } else {
            // Default to YEAR
            period = "YEAR";
            rawRevenue = rideBookRepository.getRevenueByMonth(year);
            rawTrips = rideBookRepository.getTripsByMonth(year);
        }

        List<MonthlyStatResponse> revenueByMonth = processStats(rawRevenue, period, now);
        List<MonthlyStatResponse> tripsByMonth = processStats(rawTrips, period, now);

        return AdminStatsResponse.builder()
                .totalCustomers(totalCustomers)
                .totalDrivers(totalDrivers)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
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
