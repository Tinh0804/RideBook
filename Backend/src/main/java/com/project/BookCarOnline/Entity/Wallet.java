package com.project.BookCarOnline.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.project.BookCarOnline.Entity.Enum.WalletStatus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Table
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
     String walletId;

    @OneToOne
    @JoinColumn(name = "driver_id", unique = true)
     Driver driver;

    @Column
     Double balance = 0.0; // Mặc định là 0

    // Trạng thái ví: ACTIVE, LOCKED (khóa khi gian lận)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column
    WalletStatus status;
}