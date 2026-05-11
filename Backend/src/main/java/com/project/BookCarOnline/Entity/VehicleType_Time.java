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
@Table(name = "LOAIXE_GIO")
public class VehicleType_Time {

    @EmbeddedId
     VehicleType_Time_ID id;

    @ManyToOne
    @MapsId("vehicleTypeId")
    @JoinColumn(name = "ID_LOAIXENO",columnDefinition = "VARCHAR(36)")
     VehicleType vehicleType;

    @ManyToOne
    @MapsId("timeId")
    @JoinColumn(name = "ID_GIONO",columnDefinition = "VARCHAR(36)")
     Time time;

    @Column(name = "PHUTHU")
     double surcharge;


}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class VehicleType_Time_ID implements Serializable {
    @Column(name = "ID_LOAIXENO")
    private String vehicleTypeId;

    @Column(name = "ID_GIONO")
    private String timeId;
}
