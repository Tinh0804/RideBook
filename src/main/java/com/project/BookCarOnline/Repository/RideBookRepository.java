package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Driver;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideBookRepository extends JpaRepository<Booking, String> {

    @Query("SELECT b.bookingStatus FROM Booking b WHERE b.bookingId = :id")
    BookingStatus findStatusByBookingId(@Param("id") String id);
    BookingStatus findBookingStatusByBookingId(String bookingId);
    // Customer bookings
    List<Booking> findByCustomerNo_CustomerIdOrderByBookingTimeDesc(String customerId);

    // Driver bookings
    List<Booking> findByDriverNo_DriverIdOrderByBookingTimeDesc(String driverId);

    // By status
    List<Booking> findByBookingStatusOrderByBookingTimeDesc(BookingStatus status);

    // Available rides (waiting & no driver)
    List<Booking> findByBookingStatusAndDriverNoIsNullOrderByBookingTimeAsc(BookingStatus bookingStatus);

    // Available rides by area
    List<Booking> findByBookingStatusAndDriverNoIsNullAndPickupLocationContainingOrderByBookingTimeAsc(
            BookingStatus bookingStatus,
            String area
    );



    // Ongoing booking of a driver
    Booking findByDriverNo_DriverIdAndBookingStatus(String driverId, BookingStatus bookingStatus);

    // Customer bookings by status
    List<Booking> findByCustomerNo_CustomerIdAndBookingStatusOrderByBookingTimeDesc(
            String customerId,
            BookingStatus status
    );
}
