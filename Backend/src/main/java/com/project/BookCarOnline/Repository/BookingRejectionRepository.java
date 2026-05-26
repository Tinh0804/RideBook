package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.BookingRejection;
import com.project.BookCarOnline.Entity.Enum.RejectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BookingRejectionRepository extends JpaRepository<BookingRejection, String> {


    @Query("SELECT r.driver.driverId FROM BookingRejection r WHERE r.booking.bookingId = :bookingId")
    Set<String> findDriverIdsByBookingId(@Param("bookingId") String bookingId);

//     Đếm số lần REJECTED của một tài xế (dùng cho scoring).
    @Query("""
        SELECT COUNT(r) FROM BookingRejection r
        WHERE r.driver.driverId = :driverId
          AND r.rejectionType = :type
    """)
    int countByDriverIdAndType(
            @Param("driverId") String driverId,
            @Param("type") RejectionType type
    );

    boolean existsByBooking_BookingIdAndDriver_DriverId(String bookingId, String driverId);
}
