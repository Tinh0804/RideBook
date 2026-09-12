# Scheduled booking dispatch

## Runtime flow

1. `BookingService` persists a scheduled booking with status `QUEUED` and adds
   its `bookingId` to the Redis sorted set. The score is
   `scheduledAt - dispatchBefore` as epoch milliseconds.
2. A lightweight promoter checks Redis every second. One Lua script atomically
   removes due members from the sorted set and appends them to the Redis Stream.
   It never scans the `booking` table.
3. Each application instance joins the same Stream consumer group. A consumer
   loads one booking by ID, verifies payment, atomically changes
   `QUEUED -> PENDING`, then triggers `RideDispatcherService`.
4. The consumer sends `XACK` and deletes the Stream entry only after dispatch is
   triggered or when the database says the message is stale/terminal.
5. Failed and unpaid messages remain in the pending entries list. A recovery
   task uses `XPENDING` and `XCLAIM` after `pendingMinIdle`, which provides
   at-least-once processing across instance crashes.

PostgreSQL remains the source of truth. Redis determines when work becomes due;
the conditional database update prevents two consumers from claiming the same
`QUEUED` booking. Creating a scheduled booking fails and rolls back when the
Redis schedule cannot be written, preventing a committed booking with no timer.

## Redis keys

Both keys use the `{scheduled-booking}` hash tag so the Lua script remains valid
with Redis Cluster.

- ZSET: `ridebook:{scheduled-booking}:due`
- Stream: `ridebook:{scheduled-booking}:stream`
- Consumer group: `scheduled-booking-dispatchers`

## Configuration

- `BOOKING_DISPATCH_BEFORE` (default `15m`)
- `BOOKING_SCHEDULING_PROMOTE_INTERVAL` (default `1s`)
- `BOOKING_SCHEDULING_BATCH_SIZE` (default `100`)
- `BOOKING_SCHEDULING_PENDING_MIN_IDLE` (default `30s`)
- `BOOKING_SCHEDULING_PENDING_RECOVERY_INTERVAL` (default `30s`)
- `BOOKING_SCHEDULING_CONSUMER_RECONNECT_INTERVAL` (default `30s`)
