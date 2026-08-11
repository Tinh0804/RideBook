package com.project.BookCarOnline.app.dto.reporting;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueSummaryDTO {
    private double totalRevenue;
    private long totalTrips;
}
