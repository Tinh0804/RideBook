package com.project.BookCarOnline.finance.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.project.BookCarOnline.finance.entity.enums.WalletStatus;

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

    @Column(name = "driver_id", unique = true, length = 36)
    String driverId;

    @Column
     Double balance = 0.0; // Mặc định là 0

    // Trạng thái ví: ACTIVE, LOCKED (khóa khi gian lận)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column
    WalletStatus status;
}
