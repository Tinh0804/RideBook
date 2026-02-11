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
	private String bookingId;

    @ManyToOne
    @JoinColumn(name = "ID_KHNO", referencedColumnName = "ID_KH",columnDefinition = "VARCHAR(36)")
    private Customer customerNo;

    @ManyToOne
    @JoinColumn(name = "ID_TXNO", referencedColumnName = "ID_TX",columnDefinition = "VARCHAR(36)")
    private Driver driverNo;

    @ManyToOne
    @JoinColumn(name = "ID_THANHTOANNO", referencedColumnName = "ID_THANHTOAN",columnDefinition = "VARCHAR(36)")
    private Payment paymentNo;

    @ManyToOne
    @JoinColumn(name = "ID_KHUYENMAINO", referencedColumnName = "ID_KHUYENMAI",columnDefinition = "VARCHAR(36)")
    private Promotion promotionNo;

    @Column(name = "DIEMDON")
    private String pickupLocation;

    @Column(name = "DIEMTRA")
    private String dropoffLocation;

    // Extra field not in original DB - keep for future use
    @Column(name = "GIATIEN")
    private Double totalPrice;

    @Column(name = "THOIGIANDAT")
    private Timestamp bookingTime;

    @Column(name = "THOIGIANDON")
    private Timestamp pickupTime;

    @Column(name = "THOIGIANDEN")
    private Timestamp arrivalTime;

    @Column(name = "TRANGTHAI", length = 50)
    private BookingStatus bookingStatus;

    @Column(name = "KHOANGCACH")
    private Double distance;

//    // Extra field not in original DB - keep for future use
    @Column(name = "THOIGIAN")
    private Double duration;

    @Column(name = "DIEMSO")
    private Integer rating;

    @Column(name = "DANHGIA", length = 500)
    private String review;

}
