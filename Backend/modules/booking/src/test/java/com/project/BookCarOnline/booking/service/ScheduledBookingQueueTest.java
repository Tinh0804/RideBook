package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.config.BookingSchedulingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.domain.Range;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledBookingQueueTest {

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ZSetOperations<String, String> zSetOperations;

    @Mock
    StreamOperations<String, String, String> streamOperations;

    BookingSchedulingProperties properties;
    ScheduledBookingQueue queue;

    @BeforeEach
    void setUp() {
        properties = new BookingSchedulingProperties();
        properties.setDispatchBefore(Duration.ofMinutes(15));
        properties.setZone("Asia/Ho_Chi_Minh");
        properties.setBatchSize(25);
        queue = new ScheduledBookingQueue(redisTemplate, properties);
    }

    @Test
    void schedulesBookingAtConfiguredDispatchTime() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 9, 2, 8, 30);

        queue.schedule("booking-1", scheduledAt);

        double expectedScore = scheduledAt.minusMinutes(15)
                .atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .toInstant()
                .toEpochMilli();
        verify(zSetOperations).add(properties.getZsetKey(), "booking-1", expectedScore);
    }

    @Test
    void atomicallyPromotesDueMembersToStreamUsingLua() {
        Instant now = Instant.parse("2026-09-02T01:15:00Z");
        when(redisTemplate.execute(
                any(RedisScript.class),
                eq(List.of(properties.getZsetKey(), properties.getStreamKey())),
                eq(Long.toString(now.toEpochMilli())),
                eq("25")))
                .thenReturn(3L);

        long promoted = queue.promoteDue(now);

        assertThat(promoted).isEqualTo(3L);
        verify(redisTemplate).execute(
                any(RedisScript.class),
                eq(List.of(properties.getZsetKey(), properties.getStreamKey())),
                eq(Long.toString(now.toEpochMilli())),
                eq("25"));
    }

    @Test
    void acknowledgesAndDeletesProcessedStreamMessage() {
        MapRecord<String, String, String> message = message("booking-1", "1725152400000-0");
        when(redisTemplate.<String, String>opsForStream()).thenReturn(streamOperations);

        queue.acknowledge(message);

        verify(streamOperations).acknowledge(
                properties.getStreamKey(),
                properties.getConsumerGroup(),
                message.getId());
        verify(streamOperations).delete(properties.getStreamKey(), message.getId());
    }

    @Test
    void claimsOnlyMessagesThatExceededPendingIdleTime() {
        RecordId staleId = RecordId.of("1725152400000-0");
        PendingMessages pendingMessages = new PendingMessages(
                properties.getConsumerGroup(),
                List.of(
                        new PendingMessage(
                                staleId,
                                Consumer.from(properties.getConsumerGroup(), "dead-consumer"),
                                Duration.ofSeconds(31),
                                2),
                        new PendingMessage(
                                RecordId.of("1725152401000-0"),
                                Consumer.from(properties.getConsumerGroup(), "live-consumer"),
                                Duration.ofSeconds(5),
                                1)));
        MapRecord<String, String, String> claimedMessage = message("booking-1", staleId.getValue());
        when(redisTemplate.<String, String>opsForStream()).thenReturn(streamOperations);
        when(streamOperations.pending(
                properties.getStreamKey(),
                properties.getConsumerGroup(),
                Range.unbounded(),
                properties.getBatchSize()))
                .thenReturn(pendingMessages);
        when(streamOperations.claim(
                eq(properties.getStreamKey()),
                eq(properties.getConsumerGroup()),
                eq("replacement-consumer"),
                any(RedisStreamCommands.XClaimOptions.class)))
                .thenReturn(List.of(claimedMessage));

        List<MapRecord<String, String, String>> claimed = queue.claimStale("replacement-consumer");

        assertThat(claimed).containsExactly(claimedMessage);
    }

    private MapRecord<String, String, String> message(String bookingId, String recordId) {
        return StreamRecords.newRecord()
                .ofMap(Map.of(ScheduledBookingQueue.BOOKING_ID_FIELD, bookingId))
                .withStreamKey(properties.getStreamKey())
                .withId(RecordId.of(recordId));
    }
}
