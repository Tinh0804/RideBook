package com.project.BookCarOnline.promotion.entity;

import com.project.BookCarOnline.promotion.entity.enums.PointTransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.sql.Timestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class PointTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
    String pointTransactionId;

    @Column(nullable = false, length = 36)
    String loyaltyAccountId;

    @Column(nullable = false)
    Integer amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 20)
    PointTransactionType type;

    @Column(length = 255)
    String description;

    @Column(length = 36)
    String referenceId; // Booking ID or Promotion ID

    @Column(insertable = false, updatable = false)
    Timestamp createdAt;
}
