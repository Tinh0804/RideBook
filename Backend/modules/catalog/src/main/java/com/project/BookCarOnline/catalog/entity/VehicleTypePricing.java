package com.project.BookCarOnline.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity(name = "VehicleTypePricing")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "vehicle_type_time")
public class VehicleTypePricing {

    @EmbeddedId
    VehicleTypePricingId id;

    @ManyToOne
    @MapsId("vehicleTypeId")
    @JoinColumn(name = "vehicle_type_id")
    VehicleType vehicleType;

    @ManyToOne
    @MapsId("timeId")
    @JoinColumn(name = "time_id")
    TimeSlot time;

    @Column
    double surcharge;
}

