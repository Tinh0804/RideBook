package com.project.BookCarOnline.promotion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.loyalty")
public class LoyaltyProperties {

    private Double earnRate;
    private Double coinValue;
    private Map<String, TierProperties> tiers;

    @Data
    public static class TierProperties {
        private Integer threshold;
        private Double customerMultiplier;
        private Double driverCommission;
    }
}
