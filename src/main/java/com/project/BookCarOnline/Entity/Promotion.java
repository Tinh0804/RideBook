
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
    private String promotionId;

    // Extra field not in original DB - keep for future use
    @Column(name = "MAKHUYENMAI")
    private String promotionCode;

    @Column(name = "TENKM")
    private String promotionName;

    @Column(name = "HANMUC")
    private Double discountLimit;

    @Column(name = "TGBATDAU")
    private Timestamp startTime;

    @Column(name = "TGKETTHUC")
    private Timestamp endTime;

    @Column(name = "DIEUKIENAPDUNG")
    private String applicationCondition;

    @Column(name = "SOLUONG")
    private Integer quantity;

    // Extra field not in original DB - keep for future use
    @Column(name = "HOATDONG")
    private Boolean isActive;


    
}
