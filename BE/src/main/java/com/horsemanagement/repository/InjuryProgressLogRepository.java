package com.horsemanagement.repository;

import com.horsemanagement.entity.InjuryProgressLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InjuryProgressLogRepository extends JpaRepository<InjuryProgressLog, Integer> {
    @EntityGraph(attributePaths = "loggedBy")
    Page<InjuryProgressLog> findByInjury_InjuryId(Integer injuryId, Pageable pageable);
}
