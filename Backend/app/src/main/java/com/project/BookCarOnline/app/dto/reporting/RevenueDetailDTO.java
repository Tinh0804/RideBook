package com.project.BookCarOnline.app.dto.reporting;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueDetailDTO {
    private String timeLabel;
    private long tripCount;
    private double revenue;
}
