package com.project.BookCarOnline.promotion.entity;

import com.project.BookCarOnline.promotion.entity.enums.CustomerPromotionStatus;
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
public class CustomerPromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
    String id;

    @Column(name = "customer_id", length = 36)
    String customerId;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    Promotion promotion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 20)
    CustomerPromotionStatus status;

    @Column
    Timestamp savedAt;

    @Column
    Timestamp usedAt;

    @Column(columnDefinition = "integer DEFAULT 1", nullable = false)
    @Builder.Default
    Integer quantity = 1;
}
