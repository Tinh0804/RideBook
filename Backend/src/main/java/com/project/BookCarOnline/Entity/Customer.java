package com.project.BookCarOnline.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "KHACHHANG")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_KH", nullable = false, unique = true, length = 36)
     String customerId;

    @Column(name = "TENKH")
     String customerName;

    @Column(name = "SDT", unique = true, length = 15)
     String phone;

    @Column(name = "DIACHI")
     String address;

//    // Extra fields not in original DB - keep for future use
    @Column(name = "ANHDAIDIEN")
     String avatar;

    @Column(name = "EMAIL")
     String email;

    @Column(name = "NGSINH")
    @Temporal(TemporalType.DATE)
     java.util.Date birthDate;

    @Column(name = "GIOITINH")
     String gender;

    @OneToOne
    @JoinColumn(name = "ID_TAIKHOANNO", referencedColumnName = "ID_TAIKHOAN",columnDefinition = "VARCHAR(36)")
     Account account;

}
