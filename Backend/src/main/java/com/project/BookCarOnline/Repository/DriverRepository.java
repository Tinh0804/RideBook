package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Customer;
import com.project.BookCarOnline.Entity.Driver;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface DriverRepository extends JpaRepository<Driver,String> {

//    Optional<Driver> findByAccountNo(String accountId);

    // Trong CustomerRepository.java
    @Query("SELECT c FROM Driver c WHERE c.account.accountId = :accId")
    Optional<Driver> findByAccountId(@Param("accId") String accId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByCitizenId(String citizenId);

    boolean existsByLicensePlate(String licensePlate);

    List<Driver> findByActivityStatusTrue();

    List<Driver> findByAreaAndActivityStatusTrue(String area);

    List<Driver> findByVehicleType_VehicleTypeIdAndActivityStatusTrue(String vehicleTypeId);

    Optional<Driver> findByPhone(String phone);

    Optional<Driver> findByEmail(String email);

    Optional<Driver> findByCitizenId(String citizenId);

    Optional<Driver> findByLicensePlate(String licensePlate);

    @Query(value = "SELECT * FROM Pr_FindAvailableDriversCloserCustomer(:lat, :lng, :radius, :vehicle_type_id)",
            nativeQuery = true)
    List<Driver> findTrulyAvailableDriversNearby(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius,
            @Param("vehicle_type_id") String vehicleTypeId
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE Driver d
        SET d.lastTripTime = :time
        WHERE d.driverId = :driverId
    """)
    void updateLastTripTime(
            @Param("driverId") String driverId,
            @Param("time") LocalDateTime time
    );

    @Query("SELECT d FROM Driver d WHERE "+
            "LOWER(d.driverName) LIKE :search " +
            "OR d.phone LIKE :search "+
            "OR LOWER(d.email) LIKE :search " +
            "OR d.citizenId LIKE :search " +
            "OR d.licensePlate LIKE :search"
    )
    Page<Driver>  searchDrivers( @Param("search") String search, Pageable pageable);

}
