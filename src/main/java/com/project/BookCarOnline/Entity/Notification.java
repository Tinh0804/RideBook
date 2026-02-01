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
    private String notificationId; // Lưu ý: Bỏ @GeneratedValue nếu bạn tự gán UUID từ code Servlet cũ sang

    @ManyToOne
    @JoinColumn(
            name = "ID_DATXENO",
            referencedColumnName = "ID_DATXE",
            columnDefinition = "VARCHAR(36)"
    )
    private Booking bookingNo;

    @ManyToOne
    @JoinColumn(
            name = "USERNAME",
            referencedColumnName = "TENDANGNHAP",
            columnDefinition = "VARCHAR(11)"
    )
    private Account accountNo;

    @Column(name = "TIEU_DE")
    private String title;

    @Column(name = "NOI_DUNG", columnDefinition = "NVARCHAR(MAX)") // Để lưu tiếng Việt có dấu
    private String message;

    @Column(name = "DOC")
    private boolean isRead = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NGAY_GUI")
    private Date sentAt = new Date();
}