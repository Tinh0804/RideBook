package com.project.BookCarOnline.catalog.repository;

import com.project.BookCarOnline.catalog.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> {
}
