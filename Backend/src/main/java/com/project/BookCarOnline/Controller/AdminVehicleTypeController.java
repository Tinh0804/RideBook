package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Entity.Time;
import com.project.BookCarOnline.Entity.VehicleType_Time;
import com.project.BookCarOnline.Service.VehicleTypeService;
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
    public APIResponse<Time> createTimeSlot(@RequestBody Time time) {
        return APIResponse.<Time>builder().result(vehicleTypeService.createTimeSlot(time)).build();
    }

    @GetMapping("/time-slots")
    public APIResponse<List<Time>> getAllTimeSlots() {
        return APIResponse.<List<Time>>builder().result(vehicleTypeService.getAllTimeSlots()).build();
    }

    @PutMapping("/time-slots/{id}")
    public APIResponse<Time> updateTimeSlot(@PathVariable String id, @RequestBody Time time) {
        return APIResponse.<Time>builder().result(vehicleTypeService.updateTimeSlot(id, time)).build();
    }

    @DeleteMapping("/time-slots/{id}")
    public APIResponse<Void> deleteTimeSlot(@PathVariable String id) {
        vehicleTypeService.deleteTimeSlot(id);
        return APIResponse.<Void>builder().build();
    }

    // Pricing Settings
    @PostMapping("/pricing")
    public APIResponse<VehicleType_Time> createPricing(@RequestBody VehicleType_Time pricing) {
        return APIResponse.<VehicleType_Time>builder().result(vehicleTypeService.createPricing(pricing)).build();
    }

    @GetMapping("/pricing")
    public APIResponse<List<VehicleType_Time>> getAllPricing() {
        return APIResponse.<List<VehicleType_Time>>builder().result(vehicleTypeService.getAllPricing()).build();
    }

    @PutMapping("/pricing/{vehicleTypeId}/{timeId}")
    public APIResponse<VehicleType_Time> updatePricing(@PathVariable String vehicleTypeId, @PathVariable String timeId, @RequestBody VehicleType_Time pricing) {
        return APIResponse.<VehicleType_Time>builder().result(vehicleTypeService.updatePricing(vehicleTypeId, timeId, pricing)).build();
    }

    @DeleteMapping("/pricing/{vehicleTypeId}/{timeId}")
    public APIResponse<Void> deletePricing(@PathVariable String vehicleTypeId, @PathVariable String timeId) {
        vehicleTypeService.deletePricing(vehicleTypeId, timeId);
        return APIResponse.<Void>builder().build();
    }
}
