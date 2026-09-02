package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.config.BookingSchedulingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledBookingQueue {

    public static final String BOOKING_ID_FIELD = "bookingId";

    private static final DefaultRedisScript<Long> PROMOTE_DUE_SCRIPT = new DefaultRedisScript<>("""
            local bookingIds = redis.call('ZRANGEBYSCORE', KEYS[1], '-inf', ARGV[1], 'LIMIT', 0, ARGV[2])
            local promoted = 0
            for _, bookingId in ipairs(bookingIds) do
                if redis.call('ZREM', KEYS[1], bookingId) == 1 then
                    redis.call('XADD', KEYS[2], '*', 'bookingId', bookingId)
                    promoted = promoted + 1
                end
            end
            return promoted
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final BookingSchedulingProperties properties;

    public void schedule(String bookingId, LocalDateTime scheduledAt) {
        long dispatchAt = scheduledAt
                .minus(properties.getDispatchBefore())
                .atZone(ZoneId.of(properties.getZone()))
                .toInstant()
                .toEpochMilli();
        redisTemplate.opsForZSet().add(properties.getZsetKey(), bookingId, dispatchAt);
    }

    public void remove(String bookingId) {
        redisTemplate.opsForZSet().remove(properties.getZsetKey(), bookingId);
    }

    public long promoteDue(Instant now) {
        Long promoted = redisTemplate.execute(
                PROMOTE_DUE_SCRIPT,
                List.of(properties.getZsetKey(), properties.getStreamKey()),
                Long.toString(now.toEpochMilli()),
                Integer.toString(properties.getBatchSize()));
        return promoted == null ? 0L : promoted;
    }

    public void acknowledge(MapRecord<String, String, String> message) {
        redisTemplate.<String, String>opsForStream().acknowledge(
                properties.getStreamKey(),
                properties.getConsumerGroup(),
                message.getId());
        redisTemplate.<String, String>opsForStream().delete(properties.getStreamKey(), message.getId());
    }

    public void initializeConsumerGroup() {
        byte[] streamKey = redisTemplate.getStringSerializer().serialize(properties.getStreamKey());
        if (streamKey == null) {
            throw new IllegalStateException("Cannot serialize scheduled booking stream key");
        }

        try {
            redisTemplate.execute((RedisCallback<String>) connection -> connection.streamCommands().xGroupCreate(
                    streamKey,
                    properties.getConsumerGroup(),
                    ReadOffset.from("0-0"),
                    true));
        } catch (RuntimeException exception) {
            if (!containsBusyGroup(exception)) {
                throw exception;
            }
        }
    }

    public List<MapRecord<String, String, String>> claimStale(String consumerName) {
        PendingMessages pendingMessages = redisTemplate.<String, String>opsForStream().pending(
                properties.getStreamKey(),
                properties.getConsumerGroup(),
                Range.unbounded(),
                properties.getBatchSize());
        if (pendingMessages == null || pendingMessages.isEmpty()) {
            return List.of();
        }

        List<RecordId> staleIds = new ArrayList<>();
        for (PendingMessage pendingMessage : pendingMessages) {
            if (pendingMessage.getElapsedTimeSinceLastDelivery().compareTo(properties.getPendingMinIdle()) >= 0) {
                staleIds.add(pendingMessage.getId());
            }
        }
        if (staleIds.isEmpty()) {
            return List.of();
        }

        List<MapRecord<String, String, String>> claimed = redisTemplate.<String, String>opsForStream().claim(
                properties.getStreamKey(),
                properties.getConsumerGroup(),
                consumerName,
                RedisStreamCommands.XClaimOptions.minIdle(properties.getPendingMinIdle())
                        .ids(staleIds.toArray(RecordId[]::new)));
        return claimed == null ? List.of() : claimed;
    }

    private boolean containsBusyGroup(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains("BUSYGROUP")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
