package com.project.BookCarOnline.booking.entity;

import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
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

    @Column(name = "customer_id", length = 36)
    String customerId;

    @Column(name = "driver_id", length = 36)
    String driverId;

    @Column(name = "payment_id", length = 36)
    String paymentId;



    @Column(name = "vehicle_type_id", length = 36)
    String vehicleTypeId;


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

    @Version
    @Column(columnDefinition = "integer DEFAULT 0", nullable = false)
    @Builder.Default
    Integer version = 0;

}
