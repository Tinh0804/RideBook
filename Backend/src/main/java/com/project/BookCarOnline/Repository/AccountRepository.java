package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // Spring sẽ tự hiểu userName map với cột SDT vì bạn đặt @Id trên userName
    boolean existsByUserName(String userName);
    Optional<Account> findByUserName(String userName);


    @Query(value = "SELECT * FROM TAIKHOAN WHERE SDT = :userName AND MATKHAU = :passWord AND ID_VAITRONO = :role", nativeQuery = true)
    Optional<Account> login(@Param("userName") String userName, @Param("passWord") String passWord, @Param("role") String role);
    Optional<Account> findByProviderAndProviderId(String provider,String providerId);
}