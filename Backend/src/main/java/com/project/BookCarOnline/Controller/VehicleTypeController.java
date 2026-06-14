package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Service.VehicleTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;

import java.util.List;

import com.project.BookCarOnline.Entity.Time;
import com.project.BookCarOnline.Entity.VehicleType_Time;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VehicleTypeController {

    VehicleTypeService vehicleTypeService;

    @GetMapping("/vehicle-types")
    public APIResponse<List<VehicleType>> getAllVehicleTypes() {
        return APIResponse.<List<VehicleType>>builder()
                .result(vehicleTypeService.getAllVehicleTypes())
                .build();
    }

    @PostMapping("/vehicle-types")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<VehicleType> createVehicleType(@RequestBody VehicleType vehicleType) {
        return APIResponse.<VehicleType>builder()
                .result(vehicleTypeService.createVehicleType(vehicleType))
                .build();
    }

    @PutMapping("/vehicle-types/{id}")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<VehicleType> updateVehicleType(@PathVariable String id, @RequestBody VehicleType vehicleType) {
        return APIResponse.<VehicleType>builder()
                .result(vehicleTypeService.updateVehicleType(id, vehicleType))
                .build();
    }

    @DeleteMapping("/vehicle-types/{id}")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Void> deleteVehicleType(@PathVariable String id) {
        vehicleTypeService.deleteVehicleType(id);
        return APIResponse.<Void>builder().build();
    }

    // Time Slots Settings
    @GetMapping("/time-slots")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<List<Time>> getAllTimeSlots() {
        return APIResponse.<List<Time>>builder().result(vehicleTypeService.getAllTimeSlots()).build();
    }

    @PutMapping("/time-slots/{id}")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Time> updateTimeSlot(@PathVariable String id, @RequestBody Time time) {
        return APIResponse.<Time>builder().result(vehicleTypeService.updateTimeSlot(id, time)).build();
    }

    // Pricing Settings
    @GetMapping("/pricing")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<List<VehicleType_Time>> getAllPricing() {
        return APIResponse.<List<VehicleType_Time>>builder().result(vehicleTypeService.getAllPricing()).build();
    }

    @PutMapping("/pricing/{id}")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<VehicleType_Time> updatePricing(@PathVariable String id, @RequestBody VehicleType_Time pricing) {
        return APIResponse.<VehicleType_Time>builder().result(vehicleTypeService.updatePricing(id, pricing)).build();
    }
}
