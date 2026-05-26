package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.TransactionStatus;
import com.project.BookCarOnline.Entity.Enum.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;


@Entity
@Table(name = "GIAODICHVI")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "ID_VI")
    private Wallet wallet;

    @Column(name = "SOTIEN")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "LOAIGIAODICH")
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANGTHAI")
    private TransactionStatus status;

    @Column(name = "MATHAMCHIEU")
    private String referenceId;

    @Column(name = "THOIGIAN")
    private Timestamp createdAt;
}
