package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    List<Rating> findByDriverId(String driverId);
    Optional<Rating> findByBookingId(String bookingId);
}
