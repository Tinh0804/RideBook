package com.project.BookCarOnline.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyRevenueDTO {
    private String date;
    private Double grossRevenue;
    private Double netIncome;
    private Double platformFee;
    private Double cashIncome;
    private Double onlineIncome;
    private Integer totalTrips;
    private Integer questGoal;
    private Double questReward;
    private Boolean isQuestCompleted;
    private Double questEarned;
    private Double finalIncome;
}
