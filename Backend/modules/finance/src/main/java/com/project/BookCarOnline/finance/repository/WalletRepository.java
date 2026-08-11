package com.project.BookCarOnline.finance.repository;

import com.project.BookCarOnline.finance.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {
     Optional<Wallet> findByDriverId(String driverId);
}
