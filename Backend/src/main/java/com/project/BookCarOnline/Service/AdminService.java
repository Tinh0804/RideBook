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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;
    private final RideBookRepository rideBookRepository;

    public AdminStatsResponse getOverviewStats(int year) {
        long totalCustomers = customerRepository.count();
        long totalDrivers = driverRepository.count();
        long totalBookings = rideBookRepository.count();
        Double totalRev = rideBookRepository.calculateTotalRevenue();
        double totalRevenue = totalRev != null ? totalRev : 0.0;

        List<MonthlyStatProjection> rawRevenue = rideBookRepository.getRevenueByMonth(year);
        List<MonthlyStatProjection> rawTrips = rideBookRepository.getTripsByMonth(year);

        List<MonthlyStatResponse> revenueByMonth = processMonthlyStats(rawRevenue);
        List<MonthlyStatResponse> tripsByMonth = processMonthlyStats(rawTrips);

        return AdminStatsResponse.builder()
                .totalCustomers(totalCustomers)
                .totalDrivers(totalDrivers)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .revenueByMonth(revenueByMonth)
                .tripsByMonth(tripsByMonth)
                .build();
    }

    private List<MonthlyStatResponse> processMonthlyStats(List<MonthlyStatProjection> rawStats) {
        List<MonthlyStatResponse> result = new ArrayList<>();
        // Initialize 12 months with 0
        for (int i = 1; i <= 12; i++) {
            result.add(new MonthlyStatResponse("T" + i, 0.0));
        }

        // Fill in data
        for (MonthlyStatProjection stat : rawStats) {
            if (stat.getMonth() != null && stat.getMonth() >= 1 && stat.getMonth() <= 12) {
                int index = stat.getMonth() - 1;
                result.get(index).setValue(stat.getValue() != null ? stat.getValue() : 0.0);
            }
        }

        return result;
    }
}
