package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,String> {

    Optional<Customer> findByPhone(String phone);

    // Trong CustomerRepository.java
    @Query("SELECT c FROM Customer c WHERE c.account.accountId = :accId")
    Optional<Customer> findByAccountId(@Param("accId") String accId);

    boolean existsByPhone(String phone);
}
