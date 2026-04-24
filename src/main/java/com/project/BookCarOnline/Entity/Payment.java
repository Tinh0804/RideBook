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
@Table(name = "PHUONGTHUCTHANHTOAN")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_THANHTOAN", nullable = false, unique = true, length = 36)
     String paymentId;

    @Column(name = "LOAIHINHTHANHTOAN")
     String paymentType;

    @Column(name = "GIATIEN")
     Double amount;

    @Column(name = "TRANGTHAITT")
     Boolean paymentStatus;

}
