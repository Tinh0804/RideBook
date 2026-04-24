package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    List<Rating> findByBookingNo_BookingIdIn(List<String> bookingIds);
    Optional<Rating> findByBookingNo_BookingId(String bookingId);
    @Query("""
    SELECT AVG(r.score)
    FROM Rating r
    JOIN r.bookingNo b
    WHERE b.driverNo.driverId = :driverId
    AND b.bookingStatus = 'COMPLETED'
""")
    Double getAverageRatingByDriver(@Param("driverId") String driverId);
}
