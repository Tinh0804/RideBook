package com.project.BookCarOnline.DTO.Response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverRevenueResponse {
    private RevenueSummaryDTO summary;
    private List<RevenueDetailDTO> details;
}
