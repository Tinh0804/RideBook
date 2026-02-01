package com.project.BookCarOnline.Entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalTime;
import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "GIO")
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_GIO", nullable = false,length = 36)
    private String timeId;

    @Column(name="TEN")
    private String slotName;

    @Column(name = "THOIGIANBATDAU")
    private LocalTime startTime;

    @Column(name = "THOIGIANKETTHUC")
    private LocalTime endTime;


}
