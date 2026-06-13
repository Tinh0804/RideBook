package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.RejectionType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class BookingRejection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    String rejectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    Driver driver;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, length = 20)
    RejectionType rejectionType;

    @Column(nullable = false, updatable = false)
    Timestamp rejectedAt;

    @PrePersist
    protected void onCreate() {
        rejectedAt = new Timestamp(System.currentTimeMillis());
    }
}
