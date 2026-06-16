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

    @Column(columnDefinition = "varchar")
     String customerName;

    @Column(columnDefinition = "varchar",unique = true, length = 15)
     String phone;

    @Column
     String address;

//    // Extra fields not in original DB - keep for future use
    @Column
     String avatar;

    @Column(columnDefinition = "varchar")
     String email;

    @Column
    @Temporal(TemporalType.DATE)
    Date birthDate;

    @Column
     String gender;

    @OneToOne
    @JoinColumn(name = "account_id")
     Account account;

}
