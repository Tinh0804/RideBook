package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.CreateRatingRequest;
import com.project.BookCarOnline.DTO.Response.RatingResponse;
import com.project.BookCarOnline.Service.RatingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RatingController {

    RatingService ratingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<RatingResponse> createRating(@Valid @RequestBody CreateRatingRequest request) {
        RatingResponse response = ratingService.createRating(request);
        return APIResponse.<RatingResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Đánh giá chuyến đi thành công")
                .result(response)
                .build();
    }

    @GetMapping("/driver/{driverId}")
    public APIResponse<List<RatingResponse>> getRatingsByDriverId(@PathVariable String driverId) {
        List<RatingResponse> ratings = ratingService.getRatingsByDriverId(driverId);
        return APIResponse.<List<RatingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách đánh giá của tài xế")
                .result(ratings)
                .build();
    }
}
