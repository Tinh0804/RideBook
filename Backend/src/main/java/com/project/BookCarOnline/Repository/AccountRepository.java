package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // Spring sẽ tự hiểu userName map với cột SDT vì bạn đặt @Id trên userName
    boolean existsByUserName(String userName);
    Optional<Account> findByUserName(String userName);


    @Query("SELECT a FROM Account a WHERE a.userName = :userName AND a.passWord = :passWord AND a.roleNo.roleId = :role")
    Optional<Account> login(@Param("userName") String userName, @Param("passWord") String passWord, @Param("role") String role);
    Optional<Account> findByProviderAndProviderId(com.project.BookCarOnline.Entity.Enum.Provider provider, String providerId);
    
    List<Account> findByRoleNo_RoleId(String roleId);
}