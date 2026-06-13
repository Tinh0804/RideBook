package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.TransactionStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.project.BookCarOnline.Entity.Enum.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;


@Entity
@Table
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column
    private Double amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column
    private TransactionStatus status;

    @Column
    private String referenceId;

    @Column
    private Timestamp createdAt;
}
