package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.dto.response.MonthlyStatProjection;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.repository.RatingRepository;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingReportingService {

    private final BookingRepository bookingRepository;
    private final RatingRepository ratingRepository;
    private final PaymentService paymentService;

    public DriverDashboard driverDashboard(String driverId) {
        LocalDate today = LocalDate.now();
        return new DriverDashboard(
                bookingRepository.countCompletedRides(driverId),
                value(bookingRepository.sumTotalIncome(driverId)),
                value(bookingRepository.sumTodayIncome(
                        driverId, today.atStartOfDay(), today.plusDays(1).atStartOfDay())),
                value(ratingRepository.getAverageRatingByDriver(driverId), 5.0));
    }

    public DriverRevenue driverRevenue(String driverId) {
        List<RevenueDay> days = bookingRepository.getRevenueByDate(driverId).stream()
                .map(this::toRevenueDay)
                .filter(day -> day.date() != null)
                .toList();
        return new DriverRevenue(
                bookingRepository.countCompletedRides(driverId),
                value(bookingRepository.sumTotalIncome(driverId)),
                days);
    }

    public DailyRevenue dailyRevenue(String driverId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        List<Booking> bookings = bookingRepository.findByDriverAndStatusAndDateRange(
                driverId, BookingStatus.COMPLETED, start, date.plusDays(1).atStartOfDay());
        double gross = 0;
        double cash = 0;
        double online = 0;
        for (Booking booking : bookings) {
            double amount = value(booking.getTotalPrice());
            gross += amount;
            if (booking.getPaymentId() != null
                    && paymentService.get(booking.getPaymentId()).paymentMethod() == PaymentMethod.CASH) {
                cash += amount;
            } else {
                online += amount;
            }
        }
        return new DailyRevenue(gross, cash, online, bookings.size());
    }

    public AdminBookingStats adminStats(String requestedPeriod, int year, LocalDateTime now) {
        String period = normalizePeriod(requestedPeriod);
        List<MonthlyStatProjection> revenue;
        List<MonthlyStatProjection> trips;

        if ("DAY".equals(period)) {
            LocalDateTime start = now.with(LocalTime.MIN);
            LocalDateTime end = now.with(LocalTime.MAX);
            revenue = bookingRepository.getRevenueByHour(start, end);
            trips = bookingRepository.getTripsByHour(start, end);
        } else if ("WEEK".equals(period)) {
            LocalDateTime start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
            LocalDateTime end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
            revenue = bookingRepository.getRevenueByDayOfWeek(start, end);
            trips = bookingRepository.getTripsByDayOfWeek(start, end);
        } else if ("MONTH".equals(period)) {
            LocalDateTime start = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
            LocalDateTime end = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
            revenue = bookingRepository.getRevenueByDayOfMonth(start, end);
            trips = bookingRepository.getTripsByDayOfMonth(start, end);
        } else {
            revenue = bookingRepository.getRevenueByMonth(year);
            trips = bookingRepository.getTripsByMonth(year);
        }

        return new AdminBookingStats(
                period,
                bookingRepository.count(),
                value(bookingRepository.calculateTotalRevenue()),
                revenue,
                trips);
    }

    private RevenueDay toRevenueDay(Object[] row) {
        LocalDate date = row[0] instanceof Date sqlDate ? sqlDate.toLocalDate() : (LocalDate) row[0];
        long trips = row[1] instanceof Number number ? number.longValue() : 0;
        double revenue = row[2] instanceof Number number ? number.doubleValue() : 0;
        return new RevenueDay(date, trips, revenue);
    }

    private String normalizePeriod(String period) {
        if (period == null) {
            return "YEAR";
        }
        String normalized = period.toUpperCase();
        return switch (normalized) {
            case "DAY", "WEEK", "MONTH" -> normalized;
            default -> "YEAR";
        };
    }

    private double value(Double value) {
        return value(value, 0);
    }

    private double value(Double value, double fallback) {
        return value == null ? fallback : value;
    }

    public record DriverDashboard(long totalRides, double totalIncome, double todayIncome, double averageRating) {
    }

    public record DriverRevenue(long totalTrips, double totalRevenue, List<RevenueDay> days) {
    }

    public record RevenueDay(LocalDate date, long tripCount, double revenue) {
    }

    public record DailyRevenue(double grossRevenue, double cashIncome, double onlineIncome, int totalTrips) {
    }

    public record AdminBookingStats(
            String period,
            long totalBookings,
            double totalRevenue,
            List<MonthlyStatProjection> revenue,
            List<MonthlyStatProjection> trips) {
    }
}
