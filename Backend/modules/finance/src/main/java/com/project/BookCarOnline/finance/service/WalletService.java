package com.project.BookCarOnline.finance.service;

import com.project.BookCarOnline.finance.dto.response.WalletResponse;
import com.project.BookCarOnline.finance.dto.response.WalletTransactionResponse;
import com.project.BookCarOnline.finance.dto.WalletTransactionResult;
import com.project.BookCarOnline.finance.entity.Wallet;
import com.project.BookCarOnline.finance.entity.WalletTransaction;
import com.project.BookCarOnline.finance.entity.enums.TransactionStatus;
import com.project.BookCarOnline.finance.entity.enums.TransactionType;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.finance.mapper.WalletMapper;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.finance.repository.WalletRepository;
import com.project.BookCarOnline.finance.repository.WalletTransactionRepository;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WalletService {

    WalletRepository walletRepository;
    WalletTransactionRepository transactionRepository;
    IdentityQueryService identityQueryService;
    WalletMapper mapper;

    @Value("${app.commission.platform-rate}")
    @NonFinal
    private double platformRate;

    @Transactional
    private Wallet getOrCreateWallet(String driverId) {
        return walletRepository.findByDriverId(driverId)
                .orElseGet(() -> {
                    identityQueryService.getDriver(driverId);

                    Wallet newWallet = new Wallet();
                    newWallet.setDriverId(driverId);
                    newWallet.setBalance(0.0);
                    newWallet.setStatus(com.project.BookCarOnline.finance.entity.enums.WalletStatus.ACTIVE);
                    return walletRepository.save(newWallet);
                });
    }
    public WalletResponse getMyBlance(){
        String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(()->new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL));
        Wallet wallet = getOrCreateWallet(driverId);
        return mapper.toWalletResponse(wallet);
    }
    public void addBalance(String driverId, Double amount) {
        Wallet wallet = getOrCreateWallet(driverId);
        wallet.setBalance(wallet.getBalance() + amount);
        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setType(TransactionType.TRIP_INCOME);
        txn.setStatus(TransactionStatus.COMPLETED);
        txn.setReferenceId("AUTO_DEPOSIT_" + System.currentTimeMillis());
        txn.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        transactionRepository.save(txn);
        walletRepository.save(wallet);
    }
    public void deductBalance(String driverId, Double amount,String typeDeduct) {
        Wallet wallet = getOrCreateWallet(driverId);
        if (!typeDeduct.equalsIgnoreCase("FEE_BOOKING")){
            if (wallet.getBalance() < amount) {
                throw new IllegalStateException("Số dư không đủ để thực hiện giao dịch");
            }
        }
        wallet.setBalance(wallet.getBalance() - amount);
        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setType(TransactionType.TRIP_FEE);
        txn.setStatus(TransactionStatus.COMPLETED);
        txn.setReferenceId("AUTO_WITHDRAW_" + System.currentTimeMillis());
        txn.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        transactionRepository.save(txn);
        walletRepository.save(wallet);
    }

    @Transactional
    public WalletTransactionResult createDepositRequest(String driverId, double amount) {
        if (amount <= 0) throw new IllegalStateException("Số tiền nạp phải lớn hơn 0");

        Wallet wallet = getOrCreateWallet(driverId);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setType(TransactionType.DEPOSIT);
        txn.setStatus(TransactionStatus.PENDING); // Đang chờ khách quét mã
        txn.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        WalletTransaction saved = transactionRepository.save(txn);
        return new WalletTransactionResult(saved.getTransactionId(), saved.getAmount(), wallet.getBalance());
    }

    @Transactional
    public boolean processPaymentCallback(
            String walletTransactionId, boolean isSuccess, String providerTransactionId) {
        Optional<WalletTransaction> txnOpt = transactionRepository.findById(walletTransactionId);

        if (txnOpt.isEmpty()) {
            log.error("Không tìm thấy giao dịch nạp tiền với ID: {}", walletTransactionId);
            return false;
        }

        WalletTransaction txn = txnOpt.get();

        // Chống lặp giao dịch (Double-spending prevention)
        if (txn.getStatus() != TransactionStatus.PENDING) {
            log.warn("Giao dịch {} đã được xử lý trước đó với trạng thái {}", walletTransactionId, txn.getStatus());
            return true; // Trả về true vì thực tế nó đã thành công từ trước rồi
        }
        txn.setReferenceId(providerTransactionId);

        if (isSuccess) {
            Wallet wallet = txn.getWallet();
            wallet.setBalance(wallet.getBalance() + txn.getAmount());
            walletRepository.save(wallet);

            txn.setStatus(TransactionStatus.COMPLETED);
            log.info("Nạp tiền thành công {} cho ví {}", txn.getAmount(), wallet.getWalletId());
        } else {
            txn.setStatus(TransactionStatus.FAILED);
            log.info("Giao dịch nạp tiền {} thất bại", walletTransactionId);
        }

        transactionRepository.save(txn);
        return true;
    }

    @Transactional
    public WalletTransactionResult autoWithdraw(String driverId, Double amount) {
        if (amount <= 0) throw new IllegalStateException("Số tiền rút phải lớn hơn 0");

        Wallet wallet = getOrCreateWallet(driverId);

        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Số dư không đủ để thực hiện giao dịch");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setType(TransactionType.WITHDRAWAL);
        txn.setStatus(TransactionStatus.COMPLETED); // Không qua PENDING nữa

        txn.setReferenceId("PAYOUT_AUTO_" + System.currentTimeMillis());
        txn.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        log.info("Tài xế {} đã rút tự động thành công {}", driverId, amount);
        WalletTransaction saved = transactionRepository.save(txn);
        return new WalletTransactionResult(saved.getTransactionId(), saved.getAmount(), wallet.getBalance());
    }


    @Transactional
    public void deductPlatformFee(String driverId, Double tripTotalPrice, String bookingId) {
        Double platformFee = tripTotalPrice * platformRate / 100;
        Wallet wallet = getOrCreateWallet(driverId);


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

    public Page<WalletTransactionResponse> getTransactionHistory(String driverId, String walletId, int page, int size) {
        Wallet wallet = getOrCreateWallet(driverId);
        if (!wallet.getWalletId().equals(walletId)) {
            throw new AppException(ErrorCode.WALLET_NOT_FOUND);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransaction> transactionHistory = transactionRepository.findByWallet_WalletIdOrderByCreatedAtDesc(wallet.getWalletId(), pageable);
        return transactionHistory.map(txn -> {
            List<WalletTransactionResponse> mapped = mapper.toTransactionHistoryResponse(List.of(txn));
            return mapped.isEmpty() ? null : mapped.get(0);
        });
    }

    // ==================== ADMIN METHODS ====================

    public WalletResponse getAdminWalletBalance(String driverId) {
        Wallet wallet = getOrCreateWallet(driverId);
        return mapper.toWalletResponse(wallet);
    }

    public Page<WalletTransactionResponse> getAdminTransactionHistory(String driverId, int page, int size) {
        Wallet wallet = getOrCreateWallet(driverId);
        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransaction> transactionHistory = transactionRepository.findByWallet_WalletIdOrderByCreatedAtDesc(wallet.getWalletId(), pageable);
        return transactionHistory.map(txn -> {
            List<WalletTransactionResponse> mapped = mapper.toTransactionHistoryResponse(List.of(txn));
            return mapped.isEmpty() ? null : mapped.get(0);
        });
    }

    @Transactional
    public WalletTransactionResponse adjustBalanceAdmin(String driverId, Double amount, String reason) {
        Wallet wallet = getOrCreateWallet(driverId);
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(Math.abs(amount));
        txn.setType(amount >= 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL);
        txn.setStatus(TransactionStatus.COMPLETED);
        // Save reason in referenceId or similar. Here we prepend ADMIN_ADJUST
        txn.setReferenceId("ADMIN_ADJUST_" + reason);
        txn.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        transactionRepository.save(txn);

        List<WalletTransactionResponse> mapped = mapper.toTransactionHistoryResponse(List.of(txn));
        return mapped.isEmpty() ? null : mapped.get(0);
    }
}
