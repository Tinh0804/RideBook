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
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Table(name = "INVALID_TOKEN")
public class InvalidToken {
    @Id
    @Column(name = "ID_TOKEN", nullable = false, unique = true)
    String id;

    @Column(name = "REASON", nullable = false)
    String reason;

    @Column(name = "EXPIRY_TIME", nullable = false)
    Date expiryTime;
}
