package com.project.BookCarOnline.app.service;

import com.project.BookCarOnline.app.dto.reporting.DailyRevenueDTO;
import com.project.BookCarOnline.app.dto.reporting.DriverDashboardResponse;
import com.project.BookCarOnline.app.dto.reporting.DriverRevenueResponse;
import com.project.BookCarOnline.app.dto.reporting.RevenueDetailDTO;
import com.project.BookCarOnline.app.dto.reporting.RevenueSummaryDTO;
import com.project.BookCarOnline.booking.service.BookingReportingService;
import com.project.BookCarOnline.booking.service.DriverCacheService;
import com.project.BookCarOnline.booking.service.DriverEarningsPolicy;
import com.project.BookCarOnline.identity.dto.response.DriverDetailResponse;
import com.project.BookCarOnline.identity.dto.summary.DriverSummary;
import com.project.BookCarOnline.identity.service.DriverManagementService;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverManagementService driverManagementService;
    private final IdentityQueryService identityQueryService;
    private final BookingReportingService bookingReportingService;
    private final DriverCacheService driverCacheService;
    private final DriverEarningsPolicy driverEarningsPolicy;

    public DriverDashboardResponse getDriverDashboard() {
        BookingReportingService.DriverDashboard dashboard =
                bookingReportingService.driverDashboard(currentDriverId());
        return DriverDashboardResponse.builder()
                .totalRides(dashboard.totalRides())
                .totalIncome(dashboard.totalIncome())
                .todayIncome(dashboard.todayIncome())
                .averageRating(Math.round(dashboard.averageRating()))
                .build();
    }

    public DriverRevenueResponse getDriverRevenue(String period) {
        BookingReportingService.DriverRevenue revenue =
                bookingReportingService.driverRevenue(currentDriverId());
        RevenueSummaryDTO summary = RevenueSummaryDTO.builder()
                .totalTrips(revenue.totalTrips())
                .totalRevenue(revenue.totalRevenue())
                .build();

        Map<LocalDate, BookingReportingService.RevenueDay> byDate = new HashMap<>();
        revenue.days().forEach(day -> byDate.put(day.date(), day));
        List<RevenueDetailDTO> details = buildRevenueDetails(period, byDate);

        return DriverRevenueResponse.builder().summary(summary).details(details).build();
    }

    public DailyRevenueDTO getDailyRevenue(String date) {
        LocalDate targetDate;
        try {
            targetDate = date == null ? LocalDate.now() : LocalDate.parse(date);
        } catch (RuntimeException ignored) {
            targetDate = LocalDate.now();
        }

        BookingReportingService.DailyRevenue revenue =
                bookingReportingService.dailyRevenue(currentDriverId(), targetDate);
        DriverEarningsPolicy.DailyEarnings earnings = driverEarningsPolicy.calculateDaily(
                revenue.grossRevenue(), revenue.cashIncome(), revenue.onlineIncome(), revenue.totalTrips());

        return DailyRevenueDTO.builder()
                .date(targetDate.toString())
                .grossRevenue(earnings.grossRevenue())
                .netIncome(earnings.netIncome())
                .platformFee(earnings.platformFee())
                .cashIncome(earnings.cashIncome())
                .onlineIncome(earnings.onlineIncome())
                .totalTrips(earnings.totalTrips())
                .questGoal(earnings.questGoal())
                .questReward(earnings.questReward())
                .isQuestCompleted(earnings.questCompleted())
                .questEarned(earnings.questEarned())
                .finalIncome(earnings.finalIncome())
                .build();
    }

    public boolean toggleDriverActivityStatus(String driverId, Double lat, Double lng) {
        DriverDetailResponse driver = driverManagementService.toggleActivity(driverId, lat, lng);
        if (Boolean.TRUE.equals(driver.getActivityStatus())) {
            if (driver.getCurrentLat() != null && driver.getCurrentLng() != null) {
                driverCacheService.addDriverLocationGeo(
                        driverId, driver.getVehicleTypeId(), driver.getCurrentLat(), driver.getCurrentLng());
            }
        } else {
            driverCacheService.removeDriverLocationGeo(driverId, driver.getVehicleTypeId());
        }
        return Boolean.TRUE.equals(driver.getActivityStatus());
    }

    public void updateFreeLocation(String driverId, Double lat, Double lng) {
        DriverSummary driver = identityQueryService.getDriver(driverId);
        if (Boolean.TRUE.equals(driver.activityStatus())) {
            driverCacheService.addDriverLocationGeo(driverId, driver.vehicleTypeId(), lat, lng);
        }
    }

    private List<RevenueDetailDTO> buildRevenueDetails(
            String period, Map<LocalDate, BookingReportingService.RevenueDay> byDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        List<RevenueDetailDTO> details = new ArrayList<>();

        if ("week".equalsIgnoreCase(period)) {
            for (int i = 6; i >= 0; i--) {
                addDay(details, today.minusDays(i), formatter, byDate);
            }
        } else if ("month".equalsIgnoreCase(period)) {
            LocalDate firstDay = today.withDayOfMonth(1);
            for (int i = 0; i < today.lengthOfMonth(); i++) {
                addDay(details, firstDay.plusDays(i), formatter, byDate);
            }
        } else {
            double[] monthlyRevenue = new double[12];
            long[] monthlyTrips = new long[12];
            byDate.values().stream()
                    .filter(day -> day.date().getYear() == today.getYear())
                    .forEach(day -> {
                        int month = day.date().getMonthValue() - 1;
                        monthlyRevenue[month] += driverEarningsPolicy.netRevenue(day.revenue());
                        monthlyTrips[month] += day.tripCount();
                    });
            for (int i = 0; i < 12; i++) {
                details.add(detail("T" + (i + 1), monthlyTrips[i], monthlyRevenue[i]));
            }
        }
        return details;
    }

    private void addDay(
            List<RevenueDetailDTO> details,
            LocalDate date,
            DateTimeFormatter formatter,
            Map<LocalDate, BookingReportingService.RevenueDay> byDate) {
        BookingReportingService.RevenueDay day = byDate.get(date);
        details.add(detail(
                date.format(formatter),
                day == null ? 0 : day.tripCount(),
                day == null ? 0 : driverEarningsPolicy.netRevenue(day.revenue())));
    }

    private RevenueDetailDTO detail(String label, long trips, double revenue) {
        return RevenueDetailDTO.builder()
                .timeLabel(label)
                .tripCount(trips)
                .revenue(revenue)
                .build();
    }

    private String currentDriverId() {
        return SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
