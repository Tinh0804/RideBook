package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
}
