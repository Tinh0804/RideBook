package com.project.BookCarOnline.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class FavoritePlace {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
    String favoritePlaceId;

    @Column(nullable = false, length = 36)
    String customerId;

    @Column(nullable = false, length = 255)
    String label;

    @Column(nullable = false, columnDefinition = "TEXT")
    String address;

    @Column(nullable = false)
    Double lat;

    @Column(nullable = false)
    Double lng;

    @Column(length = 100)
    String icon;

    @Column(insertable = false, updatable = false)
    Timestamp createdAt;
}
