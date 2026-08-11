package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.dto.request.CreateRatingRequest;
import com.project.BookCarOnline.booking.dto.response.RatingResponse;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.entity.Rating;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.booking.mapper.RatingMapper;
import com.project.BookCarOnline.booking.repository.RatingRepository;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RatingService {
    RatingRepository ratingRepository;
    BookingRepository bookingRepository;
    RatingMapper ratingMapper;

    public RatingResponse createRating(CreateRatingRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getBookingStatus().equals(BookingStatus.COMPLETED)) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Custom error in real world
        }

        if (ratingRepository.findByBookingNo_BookingId(booking.getBookingId()).isPresent()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Already rated
        }

        Rating rating = Rating.builder()
                .bookingNo(booking)
                .score(request.getRating())
                .review(request.getFeedback())
                .createdAt(new Date())
                .build();

        return ratingMapper.toRatingResponse(ratingRepository.save(rating));
    }

    public List<RatingResponse> getRatingsByDriverId(String driverId) {
        List<Booking> driverBookings = bookingRepository.findByDriverIdOrderByBookingTimeDesc(driverId);
        List<String> bookingIds = driverBookings.stream().map(Booking::getBookingId).collect(Collectors.toList());
        List<Rating> ratings = bookingIds.isEmpty() ? new java.util.ArrayList<>() : ratingRepository.findByBookingNo_BookingIdIn(bookingIds);
        return ratings.stream().map(ratingMapper::toRatingResponse).collect(Collectors.toList());
    }
    public List<RatingResponse> getRatingsByCustomerId(String customerId) {
        List<Booking> customerBookings = bookingRepository.findByCustomerIdOrderByBookingTimeDesc(customerId);
        List<String> bookingIds = customerBookings.stream().map(Booking::getBookingId).collect(Collectors.toList());
        List<Rating> ratings = bookingIds.isEmpty() ? new java.util.ArrayList<>() : ratingRepository.findByBookingNo_BookingIdIn(bookingIds);
        return ratings.stream().map(ratingMapper::toRatingResponse).collect(Collectors.toList());
    }
    public List<RatingResponse> getMyRatings() {
        List<Booking> myBookings = bookingRepository.findByCustomerIdOrderByBookingTimeDesc(
                SecurityUtils.getCurrentAccountId()
                        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED)));
        List<String> bookingIds = myBookings.stream().map(Booking::getBookingId).collect(Collectors.toList());
        List<Rating> ratings = bookingIds.isEmpty() ? new java.util.ArrayList<>() : ratingRepository.findByBookingNo_BookingIdIn(bookingIds);
        return ratings.stream().map(ratingMapper::toRatingResponse).collect(Collectors.toList());
    }
}
