package com.project.BookCarOnline.catalog.service;

import com.project.BookCarOnline.catalog.entity.TimeSlot;
import com.project.BookCarOnline.catalog.entity.VehicleType;
import com.project.BookCarOnline.catalog.entity.VehicleTypePricing;
import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.catalog.repository.TimeSlotRepository;
import com.project.BookCarOnline.catalog.repository.VehicleTypeRepository;
import com.project.BookCarOnline.catalog.repository.VehicleTypePricingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VehicleTypeService {

    VehicleTypeRepository vehicleTypeRepository;
    TimeSlotRepository timeSlotRepository;
    VehicleTypePricingRepository vehicleTypePricingRepository;

    @Cacheable("vehicleTypes")
    public List<VehicleType> getAllVehicleTypes() {
        return vehicleTypeRepository.findAll();
    }

    public List<VehicleTypeSummary> getVehicleTypeSummaries() {
        return vehicleTypeRepository.findAll().stream().map(this::toSummary).toList();
    }

    public VehicleTypeSummary getVehicleTypeSummary(String vehicleTypeId) {
        return toSummary(vehicleTypeRepository.findById(vehicleTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_TYPE_NOT_FOUND)));
    }

    @CacheEvict(value = "vehicleTypes", allEntries = true)
    public VehicleType createVehicleType(VehicleType vehicleType) {
        return vehicleTypeRepository.save(vehicleType);
    }

    @CacheEvict(value = "vehicleTypes", allEntries = true)
    public VehicleType updateVehicleType(String id, VehicleType vehicleType) {
        VehicleType existing = vehicleTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("VehicleType not found"));
        existing.setVehicleTypeName(vehicleType.getVehicleTypeName());
        existing.setPricePerKm(vehicleType.getPricePerKm());
        existing.setMaxPassengers(vehicleType.getMaxPassengers());
        existing.setIcon(vehicleType.getIcon());
        return vehicleTypeRepository.save(existing);
    }

    @Caching(evict = {
        @CacheEvict(value = "vehicleTypes", allEntries = true),
        @CacheEvict(value = "vehicleTypeTimes", allEntries = true)
    })
    public void deleteVehicleType(String id) {
        vehicleTypePricingRepository.deleteByVehicleType_VehicleTypeId(id);
        vehicleTypeRepository.deleteById(id);
    }

    @CacheEvict(value = "timeSlots", allEntries = true)
    public TimeSlot createTimeSlot(TimeSlot timeSlot) {
        return timeSlotRepository.save(timeSlot);
    }

    @Cacheable("timeSlots")
    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    @CacheEvict(value = "timeSlots", allEntries = true)
    public TimeSlot updateTimeSlot(String id, TimeSlot timeSlot) {
        TimeSlot existing = timeSlotRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TIME_NOT_FOUND));
        existing.setStartTime(timeSlot.getStartTime());
        existing.setEndTime(timeSlot.getEndTime());
        existing.setSlotName(timeSlot.getSlotName());
        return timeSlotRepository.save(existing);
    }

    @Caching(evict = {
        @CacheEvict(value = "timeSlots", allEntries = true),
        @CacheEvict(value = "vehicleTypeTimes", allEntries = true)
    })
    public void deleteTimeSlot(String id) {
        vehicleTypePricingRepository.deleteByTime_TimeId(id);
        timeSlotRepository.deleteById(id);
    }

    @Cacheable("vehicleTypeTimes")
    public List<VehicleTypePricing> getAllPricing() {
        return vehicleTypePricingRepository.findAll();
    }

    @CacheEvict(value = "vehicleTypeTimes", allEntries = true)
    public VehicleTypePricing createPricing(VehicleTypePricing pricing) {
        // Find existing relations to set them
        VehicleType v = vehicleTypeRepository.findById(pricing.getId().getVehicleTypeId()).orElseThrow();
        TimeSlot timeSlot = timeSlotRepository.findById(pricing.getId().getTimeId()).orElseThrow();
        pricing.setVehicleType(v);
        pricing.setTime(timeSlot);
        return vehicleTypePricingRepository.save(pricing);
    }

    @CacheEvict(value = "vehicleTypeTimes", allEntries = true)
    public VehicleTypePricing updatePricing(String vehicleTypeId, String timeId, VehicleTypePricing pricing) {
        VehicleTypePricing existing = vehicleTypePricingRepository
                .findByVehicleType_VehicleTypeIdAndTime_TimeId(vehicleTypeId, timeId)
                .orElseThrow(() -> new RuntimeException("Pricing not found"));
        existing.setSurcharge(pricing.getSurcharge());
        return vehicleTypePricingRepository.save(existing);
    }

    @CacheEvict(value = "vehicleTypeTimes", allEntries = true)
    public void deletePricing(String vehicleTypeId, String timeId) {
        vehicleTypePricingRepository.deleteByVehicleType_VehicleTypeIdAndTime_TimeId(vehicleTypeId, timeId);
    }

    public double getCurrentSurcharge(
            String vehicleTypeId, List<TimeSlot> timeSlots, List<VehicleTypePricing> pricingRules) {
        LocalTime currentTime = LocalTime.now();

        return timeSlots.stream()
                .filter(timeSlot -> timeSlot.getStartTime() != null
                        && !currentTime.isBefore(timeSlot.getStartTime())
                        && (timeSlot.getEndTime() == null || !currentTime.isAfter(timeSlot.getEndTime())))
                .sorted(Comparator.comparing(TimeSlot::getStartTime).reversed())
                .findFirst()
                .flatMap(time -> pricingRules.stream()
                        .filter(pricing -> pricing.getVehicleType().getVehicleTypeId().equals(vehicleTypeId)
                                && pricing.getTime().getTimeId().equals(time.getTimeId()))
                        .findFirst()
                )
                .map(VehicleTypePricing::getSurcharge)
                .orElse(1.0);
    }

    public double getCurrentSurcharge(String vehicleTypeId) {
        return getCurrentSurcharge(
                vehicleTypeId, timeSlotRepository.findAll(), vehicleTypePricingRepository.findAll());
    }

    private VehicleTypeSummary toSummary(VehicleType vehicleType) {
        return new VehicleTypeSummary(
                vehicleType.getVehicleTypeId(),
                vehicleType.getVehicleTypeName(),
                vehicleType.getPricePerKm(),
                vehicleType.getMaxPassengers(),
                vehicleType.getIcon());
    }
}
