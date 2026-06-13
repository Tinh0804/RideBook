package com.project.BookCarOnline.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class VehicleType_Time {

    @EmbeddedId
     VehicleType_Time_ID id;

    @ManyToOne
    @MapsId("vehicleTypeId")
    @JoinColumn(name = "vehicle_type_id")
     VehicleType vehicleType;

    @ManyToOne
    @MapsId("timeId")
    @JoinColumn(name = "time_id")
     Time time;

    @Column
     double surcharge;


}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class VehicleType_Time_ID implements Serializable {
    @Column(length = 36)
    private String vehicleTypeId;

    @Column(length = 36)
    private String timeId;
}
