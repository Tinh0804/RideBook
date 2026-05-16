package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.CreateRatingRequest;
import com.project.BookCarOnline.DTO.Response.RatingResponse;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Entity.Rating;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.RatingMapper;
import com.project.BookCarOnline.Repository.RatingRepository;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Utils.SecurityUtils;
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
    RideBookRepository rideBookRepository;
    RatingMapper ratingMapper;

    public RatingResponse createRating(CreateRatingRequest request) {
        Booking booking = rideBookRepository.findById(request.getBookingId())
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
        List<Booking> driverBookings = rideBookRepository.findByDriverNo_DriverIdOrderByBookingTimeDesc(driverId);
        List<String> bookingIds = driverBookings.stream().map(Booking::getBookingId).collect(Collectors.toList());
        List<Rating> ratings = bookingIds.isEmpty() ? new java.util.ArrayList<>() : ratingRepository.findByBookingNo_BookingIdIn(bookingIds);
        return ratings.stream().map(ratingMapper::toRatingResponse).collect(Collectors.toList());
    }
    public List<RatingResponse> getRatingsByCustomerId(String customerId) {
        List<Booking> customerBookings = rideBookRepository.findByCustomerNo_CustomerIdOrderByBookingTimeDesc(customerId);
        List<String> bookingIds = customerBookings.stream().map(Booking::getBookingId).collect(Collectors.toList());
        List<Rating> ratings = bookingIds.isEmpty() ? new java.util.ArrayList<>() : ratingRepository.findByBookingNo_BookingIdIn(bookingIds);
        return ratings.stream().map(ratingMapper::toRatingResponse).collect(Collectors.toList());
    }
    public List<RatingResponse> getMyRatings() {
        List<Booking> myBookings = rideBookRepository.findByCustomerNo_CustomerIdOrderByBookingTimeDesc(SecurityUtils.getCurrentAccountId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED)));
        List<String> bookingIds = myBookings.stream().map(Booking::getBookingId).collect(Collectors.toList());
        List<Rating> ratings = bookingIds.isEmpty() ? new java.util.ArrayList<>() : ratingRepository.findByBookingNo_BookingIdIn(bookingIds);
        return ratings.stream().map(ratingMapper::toRatingResponse).collect(Collectors.toList());
    }
}
