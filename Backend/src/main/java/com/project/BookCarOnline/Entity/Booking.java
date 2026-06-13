package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.BookingStatus;
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
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
	 String bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
     Customer customerNo;

    @ManyToOne
    @JoinColumn(name = "driver_id")
     Driver driverNo;

    @ManyToOne
    @JoinColumn(name = "payment_id")
     Payment paymentNo;


    @ManyToOne
    @JoinColumn(name = "promotion_id")
     Promotion promotionNo;

    @ManyToOne
    @JoinColumn(name = "vehicle_type_id")
     VehicleType vehicleTypeNo;


    @Column
     String pickupLocation;

    @Column
     String dropoffLocation;

    @Column
     Double pickupLat;

    @Column
     Double pickupLng;

    @Column
     Double dropoffLat;

    @Column
     Double dropoffLng;

    // Extra field not in original DB - keep for future use
    @Column
     Double totalPrice;

    @Column
     Double originalPrice;

    @Column
     Timestamp bookingTime;

    @Column
     Timestamp pickupTime;

    @Column
     Timestamp arrivalTime;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 50)
     BookingStatus bookingStatus;

    @Column
     Double distance;

//    // Extra field not in original DB - keep for future use
//     @Column
//     private Double duration;
//
//     @Column
//     private Integer rating;
//
//     @Column(length = 500)
//     private String review;

}
