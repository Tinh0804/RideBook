package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Customer;
import com.project.BookCarOnline.Entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DriverRepository extends JpaRepository<Driver,String> {

//    Optional<Driver> findByAccountNo(String accountId);

    // Trong CustomerRepository.java
    @Query("SELECT c FROM Driver c WHERE c.account.accountId = :accId")
    Optional<Driver> findByAccountId(@Param("accId") String accId);

    // Check if email already exists
    boolean existsByEmail(String email);

    // Check if phone already exists
    boolean existsByPhone(String phone);

    // Check if citizen ID already exists
    boolean existsByCitizenId(String citizenId);

    // Check if license plate already exists
    boolean existsByLicensePlate(String licensePlate);

    // Find all active drivers
    List<Driver> findByActivityStatusTrue();

    // Active drivers by area
    List<Driver> findByAreaAndActivityStatusTrue(String area);

    // Active drivers by vehicle type
    List<Driver> findByVehicleType_VehicleTypeIdAndActivityStatusTrue(String vehicleTypeId);
    // Find driver by phone
    Optional<Driver> findByPhone(String phone);

    // Find driver by email
    Optional<Driver> findByEmail(String email);

    // Find by CCCD
    Optional<Driver> findByCitizenId(String citizenId);

    // Find by license plate
    Optional<Driver> findByLicensePlate(String licensePlate);
}
