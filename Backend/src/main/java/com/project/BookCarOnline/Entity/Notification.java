package com.project.BookCarOnline.Entity;

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

    @ManyToOne
    @JoinColumn(name = "booking_id")
     Booking bookingNo;

    @ManyToOne
    @JoinColumn(name = "account_id")
     Account accountNo;

    @Column
     String title;

    @Column
     String message;

    @Column
     boolean isRead = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
     Date sentAt = new Date();
}