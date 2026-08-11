package com.project.BookCarOnline.communication.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.UUID;

import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
     String notificationId;

    @Column(name = "booking_id", length = 36)
    String bookingId;

    @Column(name = "account_id", length = 36)
    String accountId;

    @Column
     String title;

    @Column
     String message;

    @Column
    @Builder.Default
     boolean isRead = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    @Builder.Default
     Date sentAt = new Date();
}
