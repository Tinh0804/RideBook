package com.project.BookCarOnline.DTO.Redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverStats {
    double avgRating;
    int rejectCount;
    int ignoreCount;
}