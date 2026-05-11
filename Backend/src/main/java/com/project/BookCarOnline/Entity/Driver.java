package com.project.BookCarOnline.Entity;

import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

import java.sql.Timestamp;
import java.util.Date;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "TAIXE")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_TX", nullable = false, unique = true, length = 36)
     String driverId;

    @ManyToOne
    @JoinColumn(name = "ID_LOAIXENO", referencedColumnName = "ID_LOAIXE")
     VehicleType vehicleType;

    @Column(name = "TENTX")
     String driverName;

    @Column(name = "NGSINH")
    @Temporal(TemporalType.DATE)
     Date birthDate;

    @Column(name = "CCCD", unique = true, length = 200)
     String citizenId;

    @Column(name = "GPLX", length = 200)
     String drivingLicense;

    // Extra field not in original DB - keep for future use
    @Column(name = "LLTP")
     String criminalRecord;

    @Column(name = "SDT", unique = true, length = 15)
     String phone;

    @Column(name = "EMAIL", unique = true, length = 100)
     String email;

    @Column(name = "BIENSOXE", unique = true, length = 20)
     String licensePlate;

    @Column(name = "TENXE")
     String vehicleName;

    // Extra fields not in original DB - keep for future use
    @Column(name = "ANHDAIDIEN")
     String avatar;

    @Column(name = "TRANGTHAIHD")
     Boolean activityStatus;

    @Column(name = "GIOITINH")
     String gender;

    @Column(name = "DIACHI")
     String address;

    @Column(name = "KHUVUC")
     String area;

    @Column(name = "VIDO")
     Double currentLat;

    @Column(name = "KINHDO")
     Double currentLng;

    @Column(name = "CHUYENCUOI")
     Timestamp lastTripTime;

    @Column(name = "DIEMSO")
    Double score;

    @OneToOne
    @JoinColumn(name = "ID_TAIKHOANNO", referencedColumnName = "ID_TAIKHOAN",columnDefinition = "VARCHAR(36)")
     Account account;

    @Transient // Không lưu vào DB, chỉ dùng để nhận giá trị tính toán từ Procedure
     Double distance;

}
