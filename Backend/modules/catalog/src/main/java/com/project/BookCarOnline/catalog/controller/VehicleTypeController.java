package com.project.BookCarOnline.catalog.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.catalog.entity.VehicleType;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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


}
