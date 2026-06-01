
package com.project.BookCarOnline.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@Table(name = "TAIKHOAN")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_TAIKHOAN", nullable = false, unique = true, length = 36)
     String accountId;

    @Column(name = "TENDANGNHAP", nullable = false, unique = true, length = 11)
     String userName;

    @JsonIgnore
    @Column(name = "MATKHAU", nullable = false, length = 100)
     String passWord;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "ID_VAITRONO", referencedColumnName = "ID_VAITRO", nullable = false,columnDefinition = "VARCHAR(36)")
     Role roleNo;


    @Column(name = "NGAY_TAO")
     Date createdAt = new Date();

    @Column(name = "TRANGTHAITK")
     Boolean accountStatus;

    @Column(name = "PROVIDER", length = 100)
     String provider="local";

    @Column(name = "PROVIDER_ID", length = 255)
     String providerId;

}
