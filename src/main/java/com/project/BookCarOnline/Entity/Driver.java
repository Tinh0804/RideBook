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
@Table(name = "TAIXE")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_TX", nullable = false, unique = true, length = 36)
    private String driverId;

    @ManyToOne
    @JoinColumn(name = "ID_LOAIXENO", referencedColumnName = "ID_LOAIXE")
    private VehicleType vehicleType;

    @Column(name = "TENTX")
    private String driverName;

    @Column(name = "NGSINH")
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @Column(name = "CCCD", unique = true, length = 200)
    private String citizenId;

    @Column(name = "GPLX", length = 200)
    private String drivingLicense;

    // Extra field not in original DB - keep for future use
    @Column(name = "LLTP")
    private String criminalRecord;

    @Column(name = "SDT", unique = true, length = 15)
    private String phone;

    @Column(name = "EMAIL", unique = true, length = 100)
    private String email;

    @Column(name = "BIENSOXE", unique = true, length = 20)
    private String licensePlate;

    @Column(name = "TENXE")
    private String vehicleName;

    // Extra fields not in original DB - keep for future use
    @Column(name = "ANHDAIDIEN")
    private String avatar;

    @Column(name = "TRANGTHAIHD")
    private Boolean activityStatus;

    @Column(name = "GIOITINH")
    private String gender;

    @Column(name = "DIACHI")
    private String address;

    @Column(name = "KHUVUC")
    private String area;

    @Column(name = "VIDO")
    private Double currentLat;

    @Column(name = "KINHDO")
    private Double currentLng;

    @OneToOne
    @JoinColumn(name = "ID_TAIKHOANNO", referencedColumnName = "ID_TAIKHOAN",columnDefinition = "VARCHAR(36)")
    private Account account;

    @Transient // Không lưu vào DB, chỉ dùng để nhận giá trị tính toán từ Procedure
    private Double distance;

    
    

}
