package com.project.BookCarOnline.Entity;

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
public class VehicleType_Time_ID implements Serializable {
    @Column(length = 36)
    private String vehicleTypeId;

    @Column(length = 36)
    private String timeId;
}
