package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.DTO.Response.MonthlyStatProjection;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.sql.Timestamp;
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

    @Query("SELECT b FROM Booking b WHERE b.customerNo.customerId = :customerId AND b.bookingStatus IN ('PENDING', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS') ORDER BY b.bookingTime DESC")
    List<Booking> findActiveByCustomer(@Param("customerId") String customerId);

    @Query("SELECT b FROM Booking b WHERE b.driverNo.driverId = :driverId AND b.bookingStatus IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'ARRIVED')")
    List<Booking> findActiveBookingByDriverId(@Param("driverId") String driverId);

    // Bổ sung cho Admin Thống kê
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.bookingStatus = 'COMPLETED'")
    Double calculateTotalRevenue();

    @Query(value = "SELECT EXTRACT(MONTH FROM booking_time) AS month, SUM(total_price) AS value " +
                   "FROM booking " +
                   "WHERE EXTRACT(YEAR FROM booking_time) = :year AND booking_status = 'COMPLETED' " +
                   "GROUP BY EXTRACT(MONTH FROM booking_time) " +
                   "ORDER BY month", nativeQuery = true)
    List<MonthlyStatProjection> getRevenueByMonth(@Param("year") int year);

    @Query(value = "SELECT EXTRACT(MONTH FROM booking_time) AS month, CAST(COUNT(*) AS DOUBLE PRECISION) AS value " +
                   "FROM booking " +
                   "WHERE EXTRACT(YEAR FROM booking_time) = :year " +
                   "GROUP BY EXTRACT(MONTH FROM booking_time) " +
                   "ORDER BY month", nativeQuery = true)
    List<MonthlyStatProjection> getTripsByMonth(@Param("year") int year);

    @Query("SELECT b FROM Booking b WHERE b.driverNo.driverId = :driverId AND b.bookingStatus IN ('ACCEPTED', 'ARRIVED', 'IN_PROGRESS') ORDER BY b.bookingTime DESC")
    List<Booking> findActiveByDriver(@Param("driverId") String driverId);
    
    @Query("""
        SELECT b FROM Booking b 
        WHERE b.driverNo.driverId = :driverId 
        AND b.bookingStatus = :status 
        AND b.bookingTime >= :startOfDay 
        AND b.bookingTime < :endOfDay
    """)
    List<Booking> findByDriverAndStatusAndDateRange(
            @Param("driverId") String driverId,
            @Param("status") BookingStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT 
            CAST(b.bookingTime AS date) AS date,
            COUNT(b) AS tripCount,
            SUM(b.totalPrice) AS revenue
        FROM Booking b
        WHERE b.driverNo.driverId = :driverId
        AND b.bookingStatus = 'COMPLETED'
        GROUP BY CAST(b.bookingTime AS date)
        ORDER BY date DESC
    """)
    List<Object[]> getRevenueByDate(@Param("driverId") String driverId);

    @Query("""
        SELECT COUNT(b), SUM(b.totalPrice)
        FROM Booking b
        WHERE b.driverNo.driverId = :driverId
        AND b.bookingStatus = 'COMPLETED'
    """)
    Object[] getRevenueSummary(@Param("driverId") String driverId);

    List<Booking> findByDriverNo_DriverIdOrderByBookingTimeDesc(String driverId);
    Page<Booking> findByDriverNo_DriverIdOrderByBookingTimeDesc(String driverId, Pageable pageable);
    Page<Booking> findByDriverNo_DriverIdAndBookingStatusOrderByBookingTimeDesc(String driverId, BookingStatus status, Pageable pageable);
    List<Booking> findByBookingStatusOrderByBookingTimeDesc(BookingStatus status);
    List<Booking> findByBookingStatusAndDriverNoIsNullOrderByBookingTimeAsc(BookingStatus bookingStatus);
    List<Booking> findByBookingStatusAndDriverNoIsNullAndPickupLocationContainingOrderByBookingTimeAsc(BookingStatus bookingStatus, String area);
    Booking findByDriverNo_DriverIdAndBookingStatus(String driverId, BookingStatus bookingStatus);
    List<Booking> findByCustomerNo_CustomerIdAndBookingStatusOrderByBookingTimeDesc(String customerId, BookingStatus status);
    boolean existsByDriverNo_DriverIdAndBookingStatusIn(String driverId, List<BookingStatus> statuses);

    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN b.customerNo c
        LEFT JOIN b.driverNo d
        WHERE (CAST(:status AS string) IS NULL OR b.bookingStatus = :status)
        AND (CAST(:fromDate AS timestamp) IS NULL OR b.bookingTime >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR b.bookingTime < :toDate)
        AND (CAST(:search AS string) IS NULL
            OR LOWER(CAST(c.customerName AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR CAST(c.phone AS string) LIKE CONCAT('%', CAST(:search AS string), '%')
            OR LOWER(CAST(d.driverName AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR CAST(d.phone AS string) LIKE CONCAT('%', CAST(:search AS string), '%')
            OR LOWER(CAST(b.bookingId AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        ORDER BY b.bookingTime DESC
    """)
    Page<Booking> searchForAdmin(
            @Param("status") BookingStatus status,
            @Param("search") String search,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            Pageable pageable
    );
}