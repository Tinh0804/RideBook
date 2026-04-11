package com.project.BookCarOnline.Entity;

import com.project.BookCarOnline.Entity.Enum.TransactionStatus;
import com.project.BookCarOnline.Entity.Enum.TransactionType;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;


@Entity
@Table(name = "GIAODICHVI")
@Data
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "ID_VI")
    private Wallet wallet;

    @Column(name = "SOTIEN")
    private Double amount;

    // Loại GD: DEPOSIT (Nạp), WITHDRAW (Rút), TRIP_FEE (Trừ phí cuốc), TRIP_INCOME (Cộng tiền cuốc)
    @Enumerated(EnumType.STRING)
    @Column(name = "LOAIGIAODICH")
    private TransactionType type;

    // Trạng thái: PENDING (Đang chờ VNPay/MoMo xử lý), SUCCESS, FAILED
    @Enumerated(EnumType.STRING)
    @Column(name = "TRANGTHAI")
    private TransactionStatus status;

    @Column(name = "MATHAMCHIEU")
    private String referenceId; // Lưu mã giao dịch của VNPay/MoMo trả về để đối soát

    @Column(name = "THOIGIAN")
    private Timestamp createdAt;
}
