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
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
     String customerId;

    @Column(columnDefinition = "TEXT")
     String customerName;

    @Column(columnDefinition = "VARCHAR",unique = true, length = 15)
     String phone;

    @Column(columnDefinition = "TEXT")
     String address;

//    // Extra fields not in original DB - keep for future use
    @Column(columnDefinition = "VARCHAR", length = 255)
     String avatar;

    @Column(columnDefinition = "VARCHAR", length = 150)
     String email;

    @Column
    @Temporal(TemporalType.DATE)
    Date birthDate;

    @Column(columnDefinition = "TEXT")
     String gender;

    @OneToOne
    @JoinColumn(name = "account_id")
     Account account;

}
