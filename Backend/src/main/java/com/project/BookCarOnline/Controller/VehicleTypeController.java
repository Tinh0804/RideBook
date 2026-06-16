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


}
