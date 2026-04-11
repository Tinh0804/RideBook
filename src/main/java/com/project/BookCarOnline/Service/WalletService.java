package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Response.WalletResponse;
import com.project.BookCarOnline.Entity.Driver;
import com.project.BookCarOnline.Entity.Wallet;
import com.project.BookCarOnline.Entity.WalletTransaction;
import com.project.BookCarOnline.Entity.Enum.TransactionStatus;
import com.project.BookCarOnline.Entity.Enum.TransactionType;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.WalletMapper;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Repository.WalletRepository;
import com.project.BookCarOnline.Repository.WalletTransactionRepository;
import com.project.BookCarOnline.Utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WalletService {

    WalletRepository walletRepository;
    WalletTransactionRepository transactionRepository;
    DriverRepository driverRepository;
    WalletMapper mapper;

    /**
     * 1. Lấy ví của tài xế (Tự động tạo ví mới với số dư 0đ nếu chưa có)
     */
    @Transactional
    public Wallet getOrCreateWallet(String driverId) {
        return walletRepository.findByDriver_DriverId(driverId)
                .orElseGet(() -> {
                    Driver driver = driverRepository.findById(driverId)
                            .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));

                    Wallet newWallet = new Wallet();
                    newWallet.setDriver(driver);
                    newWallet.setBalance(0.0);
                    newWallet.setStatus(true);
                    return walletRepository.save(newWallet);
                });
    }
    public WalletResponse getMyBlance(){
        String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(()->new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL));
        String role = SecurityUtils.getCurrentRole().orElseThrow(()->new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL));
        if (!role.equals("ROLE_DRIVER"))
            throw new AppException(ErrorCode.ACCESS_DENIED);
        Wallet wallet = getOrCreateWallet(driverId);
        return mapper.toWalletResponse(wallet);
    }

    /**
     * 2. TẠO LỆNH NẠP TIỀN (Chờ VNPay/MoMo xử lý)
     */
    @Transactional
    public WalletTransaction createDepositRequest(String driverId, double amount) {
        if (amount <= 0) throw new IllegalStateException("Số tiền nạp phải lớn hơn 0");

        Wallet wallet = getOrCreateWallet(driverId);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setType(TransactionType.DEPOSIT);
        txn.setStatus(TransactionStatus.PENDING); // Đang chờ khách quét mã
        txn.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        return transactionRepository.save(txn);
    }

    /**
     * 3. XỬ LÝ KẾT QUẢ NẠP TIỀN (Gọi bởi IPN/Callback từ VNPay/MoMo)
     */
    @Transactional
    public void processPaymentCallback(String transactionId, boolean isSuccess, String referenceId) {
        WalletTransaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy giao dịch"));

        // Chống lặp giao dịch (Double-spending prevention)
        if (txn.getStatus() != TransactionStatus.PENDING) {
            log.warn("Giao dịch {} đã được xử lý trước đó với trạng thái {}", transactionId, txn.getStatus());
            return;
        }

        txn.setReferenceId(referenceId);

        if (isSuccess) {
            Wallet wallet = txn.getWallet();
            wallet.setBalance(wallet.getBalance() + txn.getAmount());
            walletRepository.save(wallet);

            txn.setStatus(TransactionStatus.COMPLETED);
            log.info("Nạp tiền thành công {} cho ví {}", txn.getAmount(), wallet.getWalletId());
        } else {
            txn.setStatus(TransactionStatus.FAILED);
            log.info("Giao dịch nạp tiền {} thất bại", transactionId);
        }

        transactionRepository.save(txn);
    }

    /**
     * 4. RÚT TIỀN TỰ ĐỘNG (Giả lập Payout API chuyển khoản tự động)
     */
    @Transactional
    public WalletTransaction autoWithdraw(String driverId, Double amount) {
        if (amount <= 0) throw new IllegalStateException("Số tiền rút phải lớn hơn 0");

        Wallet wallet = getOrCreateWallet(driverId);

        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Số dư không đủ để thực hiện giao dịch");
        }

        // 1. Trừ tiền ngay lập tức trong Database
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        // 2. Ghi nhận giao dịch thành công ngay lập tức
        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setType(TransactionType.WITHDRAWAL);
        txn.setStatus(TransactionStatus.COMPLETED); // Không qua PENDING nữa

        // Sinh một mã giao dịch giả lập từ Ngân hàng trả về
        txn.setReferenceId("PAYOUT_AUTO_" + System.currentTimeMillis());
        txn.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        log.info("Tài xế {} đã rút tự động thành công {}", driverId, amount);
        return transactionRepository.save(txn);
    }

    /**
     * 5. THU PHÍ NỀN TẢNG (Tự động trừ 20% khi hoàn thành cuốc xe)
     */
    @Transactional
    public void deductPlatformFee(String driverId, Double tripTotalPrice, String bookingId) {
        Double platformFee = tripTotalPrice * 0.20; // Cấu hình phí 20%
        Wallet wallet = getOrCreateWallet(driverId);

        // Chấp nhận trừ âm tiền nếu ví hết tiền (Trở thành nợ ứng dụng)
        wallet.setBalance(wallet.getBalance() - platformFee);
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(platformFee);
        txn.setType(TransactionType.TRIP_FEE);
        txn.setStatus(TransactionStatus.COMPLETED);
        txn.setReferenceId("BOOKING_" + bookingId);
        txn.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        transactionRepository.save(txn);
        log.info("Đã trừ {} phí nền tảng từ ví tài xế {} cho chuyến {}", platformFee, driverId, bookingId);
    }
}