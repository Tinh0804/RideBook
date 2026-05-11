package com.project.BookCarOnline.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "DANHGIA")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( name ="ID_DANHGIA",nullable = false, unique = true,length = 36)
     String ratingId;

    @ManyToOne
    @JoinColumn(name = "ID_DATXENO", referencedColumnName = "ID_DATXE",columnDefinition = "VARCHAR(36)")
     Booking bookingNo;

    @Column(name = "LOAIDANHGIA")
     String ratingType;

    @Column(name = "DIEM")
     double score;

    @Column(name = "DANHGIA")
     String review;

    @Column(name = "NGAY_TAO")
     Date createdAt;
}
