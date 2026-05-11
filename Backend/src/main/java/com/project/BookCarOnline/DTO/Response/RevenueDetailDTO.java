package com.project.BookCarOnline.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueDetailDTO {
    private String timeLabel;
    private long tripCount;
    private double revenue;
}
