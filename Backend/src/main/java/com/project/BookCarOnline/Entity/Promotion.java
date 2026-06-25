
package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.DiscountType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

import java.sql.Timestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
     String promotionId;

    @Column
     String promotionCode;

    @Column
     String promotionName;

    @Column
     Double discountLimit;

    @Column
     Timestamp startTime;

    @Column
     Timestamp endTime;

    @Column
     String applicationCondition;

    @Column
     Integer quantity;

    @Column
     Boolean isActive;
    @Column
    @Builder.Default
     Boolean isPublic = true;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 20)
     DiscountType discountType;

    @Column
     Double discountValue;

    @Column
     Double minTripValue;

    @Column
     Integer usageLimitPerUser;

    @Column
     String promotionImage;

}
