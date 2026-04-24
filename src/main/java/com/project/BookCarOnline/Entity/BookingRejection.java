package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.RejectionType;
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
@Table(
    name = "TUCHOI_CHUYENXE",
    indexes = {
        @Index(name = "idx_rejection_booking", columnList = "ID_DATXE"),
        @Index(name = "idx_rejection_driver",  columnList = "ID_TX")
    }
)
public class BookingRejection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_TUCHOI", nullable = false, length = 36)
    String rejectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DATXE", referencedColumnName = "ID_DATXE", nullable = false,
                columnDefinition = "VARCHAR(36)")
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TX", referencedColumnName = "ID_TX", nullable = false,
                columnDefinition = "VARCHAR(36)")
    Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "THELOAI", nullable = false, length = 20)
    RejectionType rejectionType;

    @Column(name = "THOIGIAN", nullable = false, updatable = false)
    Timestamp rejectedAt;

    @PrePersist
    protected void onCreate() {
        rejectedAt = new Timestamp(System.currentTimeMillis());
    }
}
