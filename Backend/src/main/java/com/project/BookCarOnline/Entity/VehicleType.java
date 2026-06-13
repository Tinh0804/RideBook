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
@Table
public class VehicleType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
     String vehicleTypeId;

    @Column
     String vehicleTypeName;

    @Column
     Double pricePerKm;

    // Extra field not in original DB - keep for future use
    @Column
     Integer maxPassengers;

    @Column
    String icon;

}
