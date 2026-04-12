package com.project.BookCarOnline.Mapper;

import com.project.BookCarOnline.DTO.Response.RatingResponse;
import com.project.BookCarOnline.Entity.Rating;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    RatingResponse toRatingResponse(Rating rating);
}
