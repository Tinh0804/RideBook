package com.project.BookCarOnline.booking.service;

import org.springframework.stereotype.Component;

@Component
public class DriverEarningsPolicy {

    private static final double DRIVER_REVENUE_RATE = 0.8;
    private static final int QUEST_GOAL = 10;
    private static final double QUEST_REWARD = 50_000;

    public double netRevenue(double grossRevenue) {
        return grossRevenue * DRIVER_REVENUE_RATE;
    }

    public DailyEarnings calculateDaily(
            double grossRevenue, double cashIncome, double onlineIncome, int totalTrips) {
        double netIncome = netRevenue(grossRevenue);
        double platformFee = grossRevenue - netIncome;
        boolean questCompleted = totalTrips >= QUEST_GOAL;
        double questEarned = questCompleted ? QUEST_REWARD : 0;

        return new DailyEarnings(
                grossRevenue,
                netIncome,
                platformFee,
                cashIncome,
                onlineIncome,
                totalTrips,
                QUEST_GOAL,
                QUEST_REWARD,
                questCompleted,
                questEarned,
                netIncome + questEarned);
    }

    public record DailyEarnings(
            double grossRevenue,
            double netIncome,
            double platformFee,
            double cashIncome,
            double onlineIncome,
            int totalTrips,
            int questGoal,
            double questReward,
            boolean questCompleted,
            double questEarned,
            double finalIncome) {}
}
