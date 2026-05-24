package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface RideBookRepository extends JpaRepository<Booking, String> {

    @Query("SELECT b.bookingStatus FROM Booking b WHERE b.bookingId = :id")
    BookingStatus findStatusByBookingId(@Param("id") String id);

    @Query("SELECT b.bookingStatus FROM Booking b WHERE b.bookingId = :bookingId")
    BookingStatus findBookingStatusByBookingId(@Param("bookingId") String bookingId);

    @Query("""
            SELECT COUNT(b)
            FROM Booking b
            WHERE b.driverNo.driverId = :driverId
            AND b.bookingStatus = 'COMPLETED'
        """)
    long countCompletedRides(@Param("driverId") String driverId);

    @Query("""
            SELECT SUM(b.totalPrice)
            FROM Booking b
            WHERE b.driverNo.driverId = :driverId
            AND b.bookingStatus = 'COMPLETED'
        """)
    Double sumTotalIncome(@Param("driverId") String driverId);


    @Query("""
        SELECT SUM(b.totalPrice)
        FROM Booking b
        WHERE b.driverNo.driverId = :driverId
        AND b.bookingStatus = 'COMPLETED'
        AND b.bookingTime >= :startOfDay
        AND b.bookingTime < :endOfDay
    """)
    Double sumTodayIncome(
            @Param("driverId") String driverId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );


    // ✅ Customer bookings
    List<Booking> findByCustomerNo_CustomerIdOrderByBookingTimeDesc(String customerId);
    @Query(value = """
        SELECT 
            CAST(b.THOIGIANDAT AS DATE) AS date,
            COUNT(*) AS tripCount,
            SUM(b.GIATIEN) AS revenue
        FROM DATXE b
        WHERE b.ID_TXNO = :driverId
        AND b.TRANGTHAI = 'COMPLETED'
        GROUP BY CAST(b.THOIGIANDAT AS DATE)
        ORDER BY date DESC
    """, nativeQuery = true)
    List<Object[]> getRevenueByDate(@Param("driverId") String driverId);

    @Query("""
        SELECT COUNT(b), SUM(b.totalPrice)
        FROM Booking b
        WHERE b.driverNo.driverId = :driverId
        AND b.bookingStatus = 'COMPLETED'
    """)
    Object[] getRevenueSummary(@Param("driverId") String driverId);

    List<Booking> findByDriverNo_DriverIdOrderByBookingTimeDesc(String driverId);
    List<Booking> findByBookingStatusOrderByBookingTimeDesc(BookingStatus status);
    List<Booking> findByBookingStatusAndDriverNoIsNullOrderByBookingTimeAsc(BookingStatus bookingStatus);
    List<Booking> findByBookingStatusAndDriverNoIsNullAndPickupLocationContainingOrderByBookingTimeAsc(BookingStatus bookingStatus, String area);
    Booking findByDriverNo_DriverIdAndBookingStatus(String driverId, BookingStatus bookingStatus);
    List<Booking> findByCustomerNo_CustomerIdAndBookingStatusOrderByBookingTimeDesc(String customerId, BookingStatus status);
}