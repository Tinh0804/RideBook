package com.project.BookCarOnline.catalog.repository;

import com.project.BookCarOnline.catalog.entity.VehicleTypePricing;
import com.project.BookCarOnline.catalog.entity.VehicleTypePricingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VehicleTypePricingRepository extends JpaRepository<VehicleTypePricing, VehicleTypePricingId> {
    Optional<VehicleTypePricing> findByVehicleType_VehicleTypeIdAndTime_TimeId(String vehicleTypeId, String timeId);

    @Transactional
    void deleteByTime_TimeId(String timeId);

    @Transactional
    void deleteByVehicleType_VehicleTypeId(String vehicleTypeId);

    @Transactional
    void deleteByVehicleType_VehicleTypeIdAndTime_TimeId(String vehicleTypeId, String timeId);
}
