package com.project.BookCarOnline.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class InvalidToken {
    @Id
    @Column(nullable = false, unique = true, length = 36)
    String id;

    @Column(nullable = false)
    String reason;

    @Column(nullable = false)
    Date expiryTime;
}
