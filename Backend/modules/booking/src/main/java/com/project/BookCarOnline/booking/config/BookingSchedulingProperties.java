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

    @NotBlank
    private String zsetKey = "ridebook:{scheduled-booking}:due";

    @NotBlank
    private String streamKey = "ridebook:{scheduled-booking}:stream";

    @NotBlank
    private String consumerGroup = "scheduled-booking-dispatchers";

    @NotNull
    private Duration listenerPollTimeout = Duration.ofSeconds(2);

    @NotNull
    private Duration pendingMinIdle = Duration.ofSeconds(30);
}
