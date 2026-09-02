package com.project.BookCarOnline.booking.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.booking.scheduling")
public class BookingSchedulingProperties {

    @NotNull
    private Duration dispatchBefore = Duration.ofMinutes(15);

    @Min(1)
    private int batchSize = 100;

    @NotBlank
    private String zone = "Asia/Ho_Chi_Minh";
}
