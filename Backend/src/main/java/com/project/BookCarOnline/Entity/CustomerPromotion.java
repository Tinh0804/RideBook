package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.CustomerPromotionStatus;
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
@Table(name = "KHACHHANG_KHUYENMAI")
public class CustomerPromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_KHKM", nullable = false, unique = true, length = 36)
    String id;

    @ManyToOne
    @JoinColumn(name = "ID_KHNO", referencedColumnName = "ID_KH", columnDefinition = "VARCHAR(36)")
    Customer customer;

    @ManyToOne
    @JoinColumn(name = "ID_KHUYENMAINO", referencedColumnName = "ID_KHUYENMAI", columnDefinition = "VARCHAR(36)")
    Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANGTHAI", length = 20)
    CustomerPromotionStatus status;

    @Column(name = "THOIGIANLUU")
    Timestamp savedAt;

    @Column(name = "THOIGIANDUNG")
    Timestamp usedAt;
}
