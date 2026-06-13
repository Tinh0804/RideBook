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
@Table
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false, unique = true,length = 36)
     String ratingId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
     Booking bookingNo;

    @Column
     String ratingType;

    @Column
     double score;

    @Column
     String review;

    @Column
     Date createdAt;
}
