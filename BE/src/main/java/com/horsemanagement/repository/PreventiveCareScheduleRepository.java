package com.horsemanagement.repository;

import com.horsemanagement.entity.PreventiveCareSchedule;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreventiveCareScheduleRepository
    extends JpaRepository<PreventiveCareSchedule, Integer>,
    JpaSpecificationExecutor<PreventiveCareSchedule> {
    @Override
    @EntityGraph(attributePaths = {"horse", "performedBy"})
    Page<PreventiveCareSchedule> findAll(Specification<PreventiveCareSchedule> spec,
                                         Pageable pageable);

    @EntityGraph(attributePaths = {"horse", "performedBy"})
    Optional<PreventiveCareSchedule> findByScheduleId(Integer scheduleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PreventiveCareSchedule s where s.scheduleId = :id")
    Optional<PreventiveCareSchedule> lockById(@Param("id") Integer id);
}
