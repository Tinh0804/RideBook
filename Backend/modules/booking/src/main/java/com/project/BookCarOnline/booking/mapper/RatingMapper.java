package com.project.BookCarOnline.booking.mapper;

import com.project.BookCarOnline.booking.dto.response.RatingResponse;
import com.project.BookCarOnline.booking.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    @Mapping(source = "bookingNo.bookingId", target = "bookingId")
    RatingResponse toRatingResponse(Rating rating);
}
