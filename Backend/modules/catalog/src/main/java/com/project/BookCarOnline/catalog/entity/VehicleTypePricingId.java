package com.project.BookCarOnline.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTypePricingId implements Serializable {
    @Column(length = 36)
    private String vehicleTypeId;

    @Column(length = 36)
    private String timeId;
}
