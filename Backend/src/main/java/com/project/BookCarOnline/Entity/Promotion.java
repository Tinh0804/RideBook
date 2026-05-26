
package com.project.BookCarOnline.Entity;

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
@Table(name = "KHUYENMAI")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_KHUYENMAI", nullable = false, unique = true, length = 36)
     String promotionId;

    @Column(name = "MAKHUYENMAI")
     String promotionCode;

    @Column(name = "TENKM")
     String promotionName;

    @Column(name = "HANMUC")
     Double discountLimit;

    @Column(name = "TGBATDAU")
     Timestamp startTime;

    @Column(name = "TGKETTHUC")
     Timestamp endTime;

    @Column(name = "DIEUKIENAPDUNG")
     String applicationCondition;

    @Column(name = "SOLUONG")
     Integer quantity;

    @Column(name = "HOATDONG")
     Boolean isActive;


    
}
