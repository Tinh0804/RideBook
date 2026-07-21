package com.project.BookCarOnline.Mapper;

import com.project.BookCarOnline.DTO.Request.CreateDriverRequest;
import com.project.BookCarOnline.DTO.Request.UpdateDriverRequest;
import com.project.BookCarOnline.DTO.Response.DriverDetailResponse;
import com.project.BookCarOnline.DTO.Response.DriverResponse;
import com.project.BookCarOnline.Entity.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DriverMapper {
    default String map(UUID value) {
        return value != null ? value.toString() : null;
    }

    default UUID map(String value) {
        return value != null ? UUID.fromString(value) : null;
    }

    DriverResponse toDriverResponse(Driver driver);

    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "vehicleType", ignore = true)

    @Mapping(target = "activityStatus", constant = "true")
    @Mapping(target = "avatar", ignore = true)
    Driver toDriverFromCreateRequest(CreateDriverRequest request);

    @Mapping(target = "vehicleType", ignore = true)

    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "citizenId", ignore = true)
    @Mapping(target = "drivingLicense", ignore = true)
    @Mapping(target = "criminalRecord", ignore = true)
    void updateDriver(@MappingTarget Driver driver, UpdateDriverRequest request);

    @Mapping(source = "vehicleType.vehicleTypeId", target = "vehicleTypeId")
    @Mapping(source = "vehicleType.vehicleTypeName", target = "vehicleTypeName")
    @Mapping(source = "vehicleType.icon", target = "vehicleTypeIcon")
    @Mapping(source = "vehicleType.pricePerKm", target = "pricePerKm")

    DriverDetailResponse toDriverDetailResponse(Driver driver);
}
