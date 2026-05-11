package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.VehicleType_Time;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleType_TimeRepository extends JpaRepository<VehicleType_Time, String> {
    Optional<VehicleType_Time> findByVehicleType_VehicleTypeIdAndTime_TimeId(String vehicleTypeId, String timeId);
}
