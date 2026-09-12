package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.config.BookingSchedulingProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledBookingStreamConsumer {

    private final RedisConnectionFactory connectionFactory;
    private final ScheduledBookingQueue scheduledBookingQueue;
    private final ScheduledBookingDispatcher scheduledBookingDispatcher;
    private final BookingSchedulingProperties properties;

    private final String consumerName = "booking-" + UUID.randomUUID();
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Scheduled(
            initialDelayString = "${app.booking.scheduling.consumer-start-delay:1s}",
            fixedDelayString = "${app.booking.scheduling.consumer-reconnect-interval:30s}")
    public synchronized void ensureConsumerStarted() {
        if (container != null && container.isRunning()) {
            return;
        }

        try {
            scheduledBookingQueue.initializeConsumerGroup();
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions<
                    String, MapRecord<String, String, String>> options =
                    StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                            .pollTimeout(properties.getListenerPollTimeout())
                            .batchSize(properties.getBatchSize())
                            .serializer(StringRedisSerializer.UTF_8)
                            .errorHandler(error -> log.error(
                                    "[ScheduledBooking] Redis Stream consumer gặp lỗi", error))
                            .build();

            container = StreamMessageListenerContainer.create(connectionFactory, options);
            container.receive(
                    Consumer.from(properties.getConsumerGroup(), consumerName),
                    StreamOffset.create(properties.getStreamKey(), ReadOffset.lastConsumed()),
                    scheduledBookingDispatcher::onMessage);
            container.start();
            log.info("[ScheduledBooking] Consumer={} đã lắng nghe stream={}",
                    consumerName,
                    properties.getStreamKey());
        } catch (RuntimeException exception) {
            stopContainer();
            log.error("[ScheduledBooking] Chưa thể khởi động Redis Stream consumer; sẽ retry", exception);
        }
    }

    @Scheduled(
            initialDelayString = "${app.booking.scheduling.pending-recovery-interval:30s}",
            fixedDelayString = "${app.booking.scheduling.pending-recovery-interval:30s}")
    public void recoverStaleMessages() {
        if (container == null || !container.isRunning()) {
            return;
        }

        try {
            List<MapRecord<String, String, String>> claimed = scheduledBookingQueue.claimStale(consumerName);
            claimed.forEach(scheduledBookingDispatcher::onMessage);
        } catch (RuntimeException exception) {
            log.error("[ScheduledBooking] Không thể recover pending stream messages", exception);
        }
    }

    @PreDestroy
    public synchronized void stopContainer() {
        if (container != null) {
            container.stop();
            container = null;
        }
    }
}
