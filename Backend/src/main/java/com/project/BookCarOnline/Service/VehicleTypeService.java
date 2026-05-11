package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.Entity.Time;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Entity.VehicleType_Time;
import com.project.BookCarOnline.Repository.TimeRepository;
import com.project.BookCarOnline.Repository.VehicleTypeRepository;
import com.project.BookCarOnline.Repository.VehicleType_TimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VehicleTypeService {

    VehicleTypeRepository vehicleTypeRepository;
    TimeRepository timeRepository;
    VehicleType_TimeRepository vehicleTypeTimeRepository;

    public List<VehicleType> getAllVehicleTypes() {
        return vehicleTypeRepository.findAll();
    }

    public double getCurrentSurcharge(String vehicleTypeId) {
        return this.findTimeByCurrentTime(LocalTime.now() )
                .flatMap(time -> vehicleTypeTimeRepository
                        .findByVehicleType_VehicleTypeIdAndTime_TimeId(
                                vehicleTypeId, time.getTimeId()
                        )
                )
                .map(VehicleType_Time::getSurcharge)
                .orElse(1.0);
    }

    private Optional<Time> findTimeByCurrentTime(LocalTime currentTime) {
        return timeRepository.findAllValidTimes(currentTime)
                .stream()
                .sorted(Comparator.comparing(Time::getStartTime).reversed())
                .findFirst();
    }
}
