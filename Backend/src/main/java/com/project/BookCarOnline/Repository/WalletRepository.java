package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Driver;
import com.project.BookCarOnline.Entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {
     Optional<Wallet> findByDriver_DriverId(String driverId);
}
