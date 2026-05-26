package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Service.VehicleTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vehicle-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VehicleTypeController {

    VehicleTypeService vehicleTypeService;

    @GetMapping
    public APIResponse<List<VehicleType>> getAllVehicleTypes() {
        return APIResponse.<List<VehicleType>>builder()
                .result(vehicleTypeService.getAllVehicleTypes())
                .build();
    }
//    @GetMapping
//    public APIResponse<List<VehicleType>> getAllVehicleTypesWithTime() {
//        return APIResponse.<List<VehicleType>>builder()
//                .result(vehicleTypeService.getAllVehicleTypesWithTime())
//                .build();
}
