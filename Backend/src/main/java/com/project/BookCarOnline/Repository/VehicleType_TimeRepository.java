package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.VehicleType_Time;
import com.project.BookCarOnline.Entity.VehicleType_Time_ID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VehicleType_TimeRepository extends JpaRepository<VehicleType_Time, VehicleType_Time_ID> {
    Optional<VehicleType_Time> findByVehicleType_VehicleTypeIdAndTime_TimeId(String vehicleTypeId, String timeId);

    @Transactional
    void deleteByTime_TimeId(String timeId);

    @Transactional
    void deleteByVehicleType_VehicleTypeId(String vehicleTypeId);

    @Transactional
    void deleteByVehicleType_VehicleTypeIdAndTime_TimeId(String vehicleTypeId, String timeId);
}
