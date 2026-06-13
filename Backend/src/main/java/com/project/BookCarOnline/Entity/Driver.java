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
@Table
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
     String driverId;

    @ManyToOne
    @JoinColumn(name = "vehicle_type_id")
     VehicleType vehicleType;

    @Column
     String driverName;

    @Column
    @Temporal(TemporalType.DATE)
     Date birthDate;

    @Column(unique = true, length = 200)
     String citizenId;

    @Column(length = 200)
     String drivingLicense;

    // Extra field not in original DB - keep for future use
    @Column
     String criminalRecord;

    @Column(unique = true, length = 15)
     String phone;

    @Column(unique = true, length = 100)
     String email;

    @Column(unique = true, length = 20)
     String licensePlate;

    @Column
     String vehicleName;

    // Extra fields not in original DB - keep for future use
    @Column
     String avatar;

    @Column
     Boolean activityStatus;

    @Column
     String gender;

    @Column
     String address;

    @Column
     String area;

    @Column
     Double currentLat;

    @Column
     Double currentLng;

    @Column
     Timestamp lastTripTime;

    @Column
    Double score;

    @OneToOne
    @JoinColumn(name = "account_id")
     Account account;

    @Transient // Không lưu vào DB, chỉ dùng để nhận giá trị tính toán từ Procedure
     Double distance;

}
