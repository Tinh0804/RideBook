package com.project.BookCarOnline.identity.mapper;

import com.project.BookCarOnline.identity.dto.request.CreateDriverRequest;
import com.project.BookCarOnline.identity.dto.request.UpdateDriverRequest;
import com.project.BookCarOnline.identity.dto.response.DriverDetailResponse;
import com.project.BookCarOnline.identity.entity.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        uses = AccountMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DriverMapper {
    default String map(UUID value) {
        return value != null ? value.toString() : null;
    }

    default UUID map(String value) {
        return value != null ? UUID.fromString(value) : null;
    }

    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "activityStatus", constant = "true")
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "currentLat", ignore = true)
    @Mapping(target = "currentLng", ignore = true)
    @Mapping(target = "lastTripTime", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "distance", ignore = true)
    Driver toDriverFromCreateRequest(CreateDriverRequest request);

    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "citizenId", ignore = true)
    @Mapping(target = "drivingLicense", ignore = true)
    @Mapping(target = "criminalRecord", ignore = true)
    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "lastTripTime", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "distance", ignore = true)
    void updateDriver(@MappingTarget Driver driver, UpdateDriverRequest request);

    @Mapping(target = "vehicleTypeName", ignore = true)
    @Mapping(target = "vehicleTypeIcon", ignore = true)
    @Mapping(target = "pricePerKm", ignore = true)

    DriverDetailResponse toDriverDetailResponse(Driver driver);
}
