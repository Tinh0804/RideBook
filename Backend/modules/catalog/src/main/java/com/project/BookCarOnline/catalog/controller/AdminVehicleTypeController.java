package com.project.BookCarOnline.catalog.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.catalog.entity.VehicleType;
import com.project.BookCarOnline.catalog.entity.TimeSlot;
import com.project.BookCarOnline.catalog.entity.VehicleTypePricing;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminVehicleTypeController {

    VehicleTypeService vehicleTypeService;

    @PostMapping("/vehicle-types")
    public APIResponse<VehicleType> createVehicleType(@RequestBody VehicleType vehicleType) {
        return APIResponse.<VehicleType>builder()
                .result(vehicleTypeService.createVehicleType(vehicleType))
                .build();
    }

    @PutMapping("/vehicle-types/{id}")
    public APIResponse<VehicleType> updateVehicleType(@PathVariable String id, @RequestBody VehicleType vehicleType) {
        return APIResponse.<VehicleType>builder()
                .result(vehicleTypeService.updateVehicleType(id, vehicleType))
                .build();
    }

    @DeleteMapping("/vehicle-types/{id}")
    public APIResponse<Void> deleteVehicleType(@PathVariable String id) {
        vehicleTypeService.deleteVehicleType(id);
        return APIResponse.<Void>builder().build();
    }

    // Time Slots Settings
    @PostMapping("/time-slots")
    public APIResponse<TimeSlot> createTimeSlot(@RequestBody TimeSlot timeSlot) {
        return APIResponse.<TimeSlot>builder().result(vehicleTypeService.createTimeSlot(timeSlot)).build();
    }

    @GetMapping("/time-slots")
    public APIResponse<List<TimeSlot>> getAllTimeSlots() {
        return APIResponse.<List<TimeSlot>>builder().result(vehicleTypeService.getAllTimeSlots()).build();
    }

    @PutMapping("/time-slots/{id}")
    public APIResponse<TimeSlot> updateTimeSlot(@PathVariable String id, @RequestBody TimeSlot timeSlot) {
        return APIResponse.<TimeSlot>builder().result(vehicleTypeService.updateTimeSlot(id, timeSlot)).build();
    }

    @DeleteMapping("/time-slots/{id}")
    public APIResponse<Void> deleteTimeSlot(@PathVariable String id) {
        vehicleTypeService.deleteTimeSlot(id);
        return APIResponse.<Void>builder().build();
    }

    // Pricing Settings
    @PostMapping("/pricing")
    public APIResponse<VehicleTypePricing> createPricing(@RequestBody VehicleTypePricing pricing) {
        return APIResponse.<VehicleTypePricing>builder().result(vehicleTypeService.createPricing(pricing)).build();
    }

    @GetMapping("/pricing")
    public APIResponse<List<VehicleTypePricing>> getAllPricing() {
        return APIResponse.<List<VehicleTypePricing>>builder().result(vehicleTypeService.getAllPricing()).build();
    }

    @PutMapping("/pricing/{vehicleTypeId}/{timeId}")
    public APIResponse<VehicleTypePricing> updatePricing(
            @PathVariable String vehicleTypeId,
            @PathVariable String timeId,
            @RequestBody VehicleTypePricing pricing) {
        return APIResponse.<VehicleTypePricing>builder()
                .result(vehicleTypeService.updatePricing(vehicleTypeId, timeId, pricing))
                .build();
    }

    @DeleteMapping("/pricing/{vehicleTypeId}/{timeId}")
    public APIResponse<Void> deletePricing(@PathVariable String vehicleTypeId, @PathVariable String timeId) {
        vehicleTypeService.deletePricing(vehicleTypeId, timeId);
        return APIResponse.<Void>builder().build();
    }
}
