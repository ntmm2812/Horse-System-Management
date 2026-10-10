package com.horsemanagement.repository;

import com.horsemanagement.entity.HorseHealthMetric;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorseHealthMetricRepository extends JpaRepository<HorseHealthMetric, Long> {
    Page<HorseHealthMetric> findByHorse_HorseId(Integer horseId, Pageable pageable);

    Optional<HorseHealthMetric> findFirstByHorse_HorseIdOrderByRecordedAtDescMetricIdDesc(Integer horseId);
}
