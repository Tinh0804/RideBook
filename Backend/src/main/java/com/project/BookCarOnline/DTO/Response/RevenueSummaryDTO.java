package com.project.BookCarOnline.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueSummaryDTO {
    private double totalRevenue;
    private long totalTrips;
}
