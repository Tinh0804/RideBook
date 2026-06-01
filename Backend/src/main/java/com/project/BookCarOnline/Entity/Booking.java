package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.BookingStatus;
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
@Table(name = "DATXE")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_DATXE", nullable = false, unique = true, length = 36)
	 String bookingId;

    @ManyToOne
    @JoinColumn(name = "ID_KHNO", referencedColumnName = "ID_KH",columnDefinition = "VARCHAR(36)")
     Customer customerNo;

    @ManyToOne
    @JoinColumn(name = "ID_TXNO", referencedColumnName = "ID_TX",columnDefinition = "VARCHAR(36)")
     Driver driverNo;

    @ManyToOne
    @JoinColumn(name = "ID_THANHTOANNO", referencedColumnName = "ID_THANHTOAN",columnDefinition = "VARCHAR(36)")
     Payment paymentNo;


    @ManyToOne
    @JoinColumn(name = "ID_KHUYENMAINO", referencedColumnName = "ID_KHUYENMAI",columnDefinition = "VARCHAR(36)")
     Promotion promotionNo;

    @ManyToOne
    @JoinColumn(name = "ID_LOAIXENO", referencedColumnName = "ID_LOAIXE",columnDefinition = "VARCHAR(36)")
     VehicleType vehicleTypeNo;


    @Column(name = "DIEMDON")
     String pickupLocation;

    @Column(name = "DIEMTRA")
     String dropoffLocation;

    @Column(name = "PICKUP_LAT")
     Double pickupLat;

    @Column(name = "PICKUP_LNG")
     Double pickupLng;

    @Column(name = "DROPOFF_LAT")
     Double dropoffLat;

    @Column(name = "DROPOFF_LNG")
     Double dropoffLng;

    // Extra field not in original DB - keep for future use
    @Column(name = "GIATIEN")
     Double totalPrice;

    @Column(name = "GIA_GOC")
     Double originalPrice;

    @Column(name = "THOIGIANDAT")
     Timestamp bookingTime;

    @Column(name = "THOIGIANDON")
     Timestamp pickupTime;

    @Column(name = "THOIGIANDEN")
     Timestamp arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANGTHAI", length = 50)
     BookingStatus bookingStatus;

    @Column(name = "KHOANGCACH")
     Double distance;

//    // Extra field not in original DB - keep for future use
//     @Column(name = "THOIGIAN")
//     private Double duration;
//
//     @Column(name = "DIEMSO")
//     private Integer rating;
//
//     @Column(name = "DANHGIA", length = 500)
//     private String review;

}
