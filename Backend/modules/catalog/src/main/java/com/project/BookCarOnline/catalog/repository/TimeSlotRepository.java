package com.project.BookCarOnline.catalog.repository;

import com.project.BookCarOnline.catalog.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, String> {

    @Query("SELECT t FROM TimeSlot t WHERE " +
            "(t.startTime <= t.endTime AND CAST(:currentTime AS time) BETWEEN t.startTime AND t.endTime) " +
            "OR " +
            "(t.startTime > t.endTime AND (CAST(:currentTime AS time) >= t.startTime OR CAST(:currentTime AS time) <= t.endTime))")
    List<TimeSlot> findAllValidTimes(@Param("currentTime") LocalTime currentTime);
}
