package com.project.BookCarOnline.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "VITAIXE")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_VI", nullable = false, unique = true, length = 36)
     String walletId;

    @OneToOne
    @JoinColumn(name = "ID_TX", unique = true)
     Driver driver;

    @Column(name = "SO_DU")
     Double balance = 0.0; // Mặc định là 0

    // Trạng thái ví: ACTIVE, LOCKED (khóa khi gian lận)
    @Column(name = "TRANG_THAI")
     boolean status = true;
}