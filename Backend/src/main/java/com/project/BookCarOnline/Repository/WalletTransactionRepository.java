package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    List<WalletTransaction> findByWallet_WalletId(String walletId);

}
