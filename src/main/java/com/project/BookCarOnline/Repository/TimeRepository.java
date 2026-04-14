package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Time;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeRepository extends JpaRepository<Time, String> {

    @Query("SELECT t FROM Time t WHERE " +
            "(t.startTime <= t.endTime AND CAST(:currentTime AS time) BETWEEN t.startTime AND t.endTime) " +
            "OR " +
            "(t.startTime > t.endTime AND (CAST(:currentTime AS time) >= t.startTime OR CAST(:currentTime AS time) <= t.endTime))")
    List<Time> findAllValidTimes(@Param("currentTime") LocalTime currentTime);
}
