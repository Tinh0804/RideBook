package com.project.BookCarOnline.booking.repository;

import com.project.BookCarOnline.booking.entity.BookingPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingPromotionRepository extends JpaRepository<BookingPromotion, String> {
    List<BookingPromotion> findByBooking_BookingId(String bookingId);
}
