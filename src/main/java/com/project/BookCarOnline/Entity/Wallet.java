package com.project.BookCarOnline.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "VITAIXE")
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_VI", nullable = false, unique = true, length = 36)
    private String walletId;

    @OneToOne
    @JoinColumn(name = "ID_TX", unique = true)
    private Driver driver;

    @Column(name = "SO_DU")
    private Double balance = 0.0; // Mặc định là 0

    // Trạng thái ví: ACTIVE, LOCKED (khóa khi gian lận)
    @Column(name = "TRANG_THAI")
    private boolean status = true;
}