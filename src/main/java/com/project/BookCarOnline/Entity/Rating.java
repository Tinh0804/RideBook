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
    private String ratingId;

    @ManyToOne
    @JoinColumn(name = "ID_DATXENO", referencedColumnName = "ID_DATXE",columnDefinition = "VARCHAR(36)")
    private Booking bookingNo;

    @Column(name = "LOAIDANHGIA")
    private String ratingType;

    @Column(name = "DIEM")
    private double score;

    @Column(name = "DANHGIA")
    private String review;

    @Column(name = "NGAY_TAO")
    private Date createdAt;
}
