package com.project.BookCarOnline.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "LOAIXE")
public class VehicleType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_LOAIXE", nullable = false, length = 36)
    private String vehicleTypeId;

    @Column(name = "TENLOAIXE")
    private String vehicleTypeName;

    @Column(name = "GIA1KM")
    private Double pricePerKm;

    // Extra field not in original DB - keep for future use
    @Column(name = "SOCHONGOI")
    private Integer maxPassengers;

}
