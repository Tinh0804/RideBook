package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.Entity.Time;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Entity.VehicleType_Time;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
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

    public VehicleType createVehicleType(VehicleType vehicleType) {
        return vehicleTypeRepository.save(vehicleType);
    }

    public VehicleType updateVehicleType(String id, VehicleType vehicleType) {
        VehicleType existing = vehicleTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("VehicleType not found"));
        existing.setVehicleTypeName(vehicleType.getVehicleTypeName());
        existing.setPricePerKm(vehicleType.getPricePerKm());
        existing.setMaxPassengers(vehicleType.getMaxPassengers());
        existing.setIcon(vehicleType.getIcon());
        return vehicleTypeRepository.save(existing);
    }

    public void deleteVehicleType(String id) {
        vehicleTypeRepository.deleteById(id);
    }

    public List<Time> getAllTimeSlots() {
        return timeRepository.findAll();
    }

    public Time updateTimeSlot(String id, Time time) {
        Time existing = timeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TIME_NOT_FOUND));
        existing.setStartTime(time.getStartTime());
        existing.setEndTime(time.getEndTime());
        existing.setSlotName(time.getSlotName());
        return timeRepository.save(existing);
    }

    public List<VehicleType_Time> getAllPricing() {
        return vehicleTypeTimeRepository.findAll();
    }

    public VehicleType_Time updatePricing(String id, VehicleType_Time pricing) {
        VehicleType_Time existing = vehicleTypeTimeRepository.findById(id).orElseThrow(() -> new RuntimeException("Pricing not found"));
        existing.setSurcharge(pricing.getSurcharge());
        return vehicleTypeTimeRepository.save(existing);
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
