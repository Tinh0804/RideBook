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
@Table(name = "NOTIFICATION")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_NOTIFICATION", nullable = false, unique = true, length = 36)
     String notificationId;

    @ManyToOne
    @JoinColumn(
            name = "ID_DATXENO",
            referencedColumnName = "ID_DATXE",
            columnDefinition = "VARCHAR(36)"
    )
     Booking bookingNo;

    @ManyToOne
    @JoinColumn(
            name = "ID_TAIKHOANNO",
            referencedColumnName = "ID_TAIKHOAN",
            columnDefinition = "VARCHAR(36)"
    )
     Account accountNo;

    @Column(name = "TIEU_DE")
     String title;

    @Column(name = "NOI_DUNG", columnDefinition = "NVARCHAR(MAX)")
     String message;

    @Column(name = "DOC")
     boolean isRead = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NGAY_GUI")
     Date sentAt = new Date();
}