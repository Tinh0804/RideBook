package com.project.BookCarOnline.booking.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DriverEarningsPolicyTest {

    @Test
    void calculatesDriverSharePlatformFeeAndQuestReward() {
        DriverEarningsPolicy policy = new DriverEarningsPolicy();

        DriverEarningsPolicy.DailyEarnings earnings =
                policy.calculateDaily(100_000d, 40_000d, 60_000d, 10);

        assertEquals(80_000d, earnings.netIncome());
        assertEquals(20_000d, earnings.platformFee());
        assertEquals(50_000d, earnings.questEarned());
        assertEquals(130_000d, earnings.finalIncome());
    }
}
