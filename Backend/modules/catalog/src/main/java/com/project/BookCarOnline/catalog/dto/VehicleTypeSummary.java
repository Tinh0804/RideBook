package com.project.BookCarOnline.catalog.dto;

public record VehicleTypeSummary(
        String vehicleTypeId,
        String vehicleTypeName,
        Double pricePerKm,
        Integer maxPassengers,
        String icon) {
}
