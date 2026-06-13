package com.project.BookCarOnline.Entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false,length = 36)
     String timeId;

    @Column
     String slotName;

    @Column
     LocalTime startTime;

    @Column
     LocalTime endTime;


}
