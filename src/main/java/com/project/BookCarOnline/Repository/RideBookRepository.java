package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideBookRepository extends JpaRepository<Booking, String> {

    // Customer bookings
    List<Booking> findByCustomerNo_CustomerIdOrderByBookingTimeDesc(String customerId);

    // Driver bookings
    List<Booking> findByDriverNo_DriverIdOrderByBookingTimeDesc(String driverId);

    // By status
    List<Booking> findByBookingStatusOrderByBookingTimeDesc(String status);

    // Available rides (waiting & no driver)
    List<Booking> findByBookingStatusAndDriverNoIsNullOrderByBookingTimeAsc(String bookingStatus);

    // Available rides by area
    List<Booking> findByBookingStatusAndDriverNoIsNullAndPickupLocationContainingOrderByBookingTimeAsc(
            String bookingStatus,
            String area
    );

    // Ongoing booking of a driver
    Booking findByDriverNo_DriverIdAndBookingStatus(String driverId, String bookingStatus);

    // Customer bookings by status
    List<Booking> findByCustomerNo_CustomerIdAndBookingStatusOrderByBookingTimeDesc(
            String customerId,
            String status
    );
}
