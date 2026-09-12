package com.project.BookCarOnline.booking.repository;

import com.project.BookCarOnline.booking.dto.response.MonthlyStatProjection;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String>, JpaSpecificationExecutor<Booking> {

    @Query("SELECT b.bookingStatus FROM Booking b WHERE b.bookingId = :id")
    BookingStatus findStatusByBookingId(@Param("id") String id);

    @Query("SELECT b.bookingStatus FROM Booking b WHERE b.bookingId = :bookingId")
    BookingStatus findBookingStatusByBookingId(@Param("bookingId") String bookingId);

    @Query("""
                SELECT COUNT(b)
                FROM Booking b
                WHERE b.driverId = :driverId
                AND b.bookingStatus = 'COMPLETED'
            """)
    long countCompletedRides(@Param("driverId") String driverId);

    @Query("""
                SELECT SUM(b.totalPrice)
                FROM Booking b
                WHERE b.driverId = :driverId
                AND b.bookingStatus = 'COMPLETED'
            """)
    Double sumTotalIncome(@Param("driverId") String driverId);

    @Query("""
                SELECT SUM(b.totalPrice)
                FROM Booking b
                WHERE b.driverId = :driverId
                AND b.bookingStatus = 'COMPLETED'
                AND b.bookingTime >= :startOfDay
                AND b.bookingTime < :endOfDay
            """)
    Double sumTodayIncome(
            @Param("driverId") String driverId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // ✅ Customer bookings
    List<Booking> findByCustomerIdOrderByBookingTimeDesc(String customerId);

    @Query("SELECT b FROM Booking b WHERE b.customerId = :customerId AND b.bookingStatus IN ('QUEUED', 'PENDING', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS') ORDER BY b.bookingTime DESC")
    List<Booking> findActiveByCustomer(@Param("customerId") String customerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @Query("""
            UPDATE Booking b
            SET b.bookingStatus = :targetStatus,
                b.version = b.version + 1
            WHERE b.bookingId = :bookingId
              AND b.bookingStatus = :expectedStatus
              AND b.driverId IS NULL
              AND b.scheduledAt IS NOT NULL
              AND b.scheduledAt <= :cutoff
            """)
    int claimScheduledBooking(
            @Param("bookingId") String bookingId,
            @Param("expectedStatus") BookingStatus expectedStatus,
            @Param("targetStatus") BookingStatus targetStatus,
            @Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT b FROM Booking b WHERE b.driverId = :driverId AND b.bookingStatus IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'ARRIVED')")
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

    @Query(value = "SELECT EXTRACT(HOUR FROM booking_time) AS month, SUM(total_price) AS value " +
            "FROM booking " +
            "WHERE booking_time >= :start AND booking_time < :end AND booking_status = 'COMPLETED' " +
            "GROUP BY EXTRACT(HOUR FROM booking_time) " +
            "ORDER BY month", nativeQuery = true)
    List<MonthlyStatProjection> getRevenueByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT EXTRACT(HOUR FROM booking_time) AS month, CAST(COUNT(*) AS DOUBLE PRECISION) AS value " +
            "FROM booking " +
            "WHERE booking_time >= :start AND booking_time < :end " +
            "GROUP BY EXTRACT(HOUR FROM booking_time) " +
            "ORDER BY month", nativeQuery = true)
    List<MonthlyStatProjection> getTripsByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT EXTRACT(ISODOW FROM booking_time) AS month, SUM(total_price) AS value " +
            "FROM booking " +
            "WHERE booking_time >= :start AND booking_time < :end AND booking_status = 'COMPLETED' " +
            "GROUP BY EXTRACT(ISODOW FROM booking_time) " +
            "ORDER BY month", nativeQuery = true)
    List<MonthlyStatProjection> getRevenueByDayOfWeek(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT EXTRACT(ISODOW FROM booking_time) AS month, CAST(COUNT(*) AS DOUBLE PRECISION) AS value " +
            "FROM booking " +
            "WHERE booking_time >= :start AND booking_time < :end " +
            "GROUP BY EXTRACT(ISODOW FROM booking_time) " +
            "ORDER BY month", nativeQuery = true)
    List<MonthlyStatProjection> getTripsByDayOfWeek(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT EXTRACT(DAY FROM booking_time) AS month, SUM(total_price) AS value " +
            "FROM booking " +
            "WHERE booking_time >= :start AND booking_time < :end AND booking_status = 'COMPLETED' " +
            "GROUP BY EXTRACT(DAY FROM booking_time) " +
            "ORDER BY month", nativeQuery = true)
    List<MonthlyStatProjection> getRevenueByDayOfMonth(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT EXTRACT(DAY FROM booking_time) AS month, CAST(COUNT(*) AS DOUBLE PRECISION) AS value " +
            "FROM booking " +
            "WHERE booking_time >= :start AND booking_time < :end " +
            "GROUP BY EXTRACT(DAY FROM booking_time) " +
            "ORDER BY month", nativeQuery = true)
    List<MonthlyStatProjection> getTripsByDayOfMonth(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.driverId = :driverId AND b.bookingStatus IN ('ACCEPTED', 'ARRIVED', 'IN_PROGRESS') ORDER BY b.bookingTime DESC")
    List<Booking> findActiveByDriver(@Param("driverId") String driverId);

    @Query("""
                SELECT b FROM Booking b
                WHERE b.driverId = :driverId
                AND b.bookingStatus = :status
                AND b.bookingTime >= :startOfDay
                AND b.bookingTime < :endOfDay
            """)
    List<Booking> findByDriverAndStatusAndDateRange(
            @Param("driverId") String driverId,
            @Param("status") BookingStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
                SELECT
                    CAST(b.bookingTime AS date) AS date,
                    COUNT(b) AS tripCount,
                    SUM(b.totalPrice) AS revenue
                FROM Booking b
                WHERE b.driverId = :driverId
                AND b.bookingStatus = 'COMPLETED'
                GROUP BY CAST(b.bookingTime AS date)
                ORDER BY date DESC
            """)
    List<Object[]> getRevenueByDate(@Param("driverId") String driverId);

    List<Booking> findByDriverIdOrderByBookingTimeDesc(String driverId);

    Page<Booking> findByDriverIdOrderByBookingTimeDesc(String driverId, Pageable pageable);

    Page<Booking> findByDriverIdAndBookingStatusOrderByBookingTimeDesc(String driverId, BookingStatus status,
            Pageable pageable);

    List<Booking> findByBookingStatusOrderByBookingTimeDesc(BookingStatus status);

    List<Booking> findByBookingStatusAndDriverIdIsNullOrderByBookingTimeAsc(BookingStatus bookingStatus);

    List<Booking> findByBookingStatusAndDriverIdIsNullAndPickupLocationContainingOrderByBookingTimeAsc(
            BookingStatus bookingStatus, String area);

    Booking findByDriverIdAndBookingStatus(String driverId, BookingStatus bookingStatus);

    List<Booking> findByCustomerIdAndBookingStatusOrderByBookingTimeDesc(String customerId,
            BookingStatus status);

    boolean existsByDriverIdAndBookingStatusIn(String driverId, List<BookingStatus> statuses);
}
