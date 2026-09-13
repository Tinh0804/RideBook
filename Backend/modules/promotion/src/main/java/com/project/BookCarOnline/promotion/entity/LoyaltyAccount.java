package com.project.BookCarOnline.promotion.entity;

import com.project.BookCarOnline.promotion.entity.enums.MembershipTier;
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
public class LoyaltyAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
    String loyaltyAccountId;

    @Column(nullable = false, unique = true, length = 36)
    String accountId; // Reference to Customer or Driver Account

    @Column(nullable = false)
    @Builder.Default
    Integer currentPoints = 0;

    @Column(nullable = false)
    @Builder.Default
    Integer lifetimePoints = 0;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 20)
    @Builder.Default
    MembershipTier tier = MembershipTier.BRONZE;

    @Column(nullable = false)
    @Builder.Default
    Double totalSpent = 0.0; // Works for customer spending and driver earnings

    @Column(nullable = false)
    @Builder.Default
    Integer totalRides = 0;

    @Column(insertable = false, updatable = false)
    Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    Timestamp updatedAt;
}
