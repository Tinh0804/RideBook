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

    @Column(name = "ID_DATXENO",unique = true,columnDefinition = "VARCHAR(36)")
    private String bookingId;

    @Column(name = "ID_KHNO",unique = true,columnDefinition = "VARCHAR(36)")
    private String customerId;

    @Column(name = "ID_TXNO",unique = true,columnDefinition = "VARCHAR(36)")
    private String driverId;

    @Column(name = "DIEM")
    private double score;

    @Column(name = "DANHGIA")
    private String review;

    @Column(name = "NGAY_TAO")
    private Date createdAt;





}
