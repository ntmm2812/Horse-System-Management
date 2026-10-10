package com.horsemanagement.repository;

import com.horsemanagement.entity.TreatmentPlan;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Integer>,
    JpaSpecificationExecutor<TreatmentPlan> {
    @Override
    @EntityGraph(attributePaths = {"medicalRecord", "medicalRecord.horse"})
    Page<TreatmentPlan> findAll(Specification<TreatmentPlan> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"medicalRecord", "medicalRecord.horse"})
    Optional<TreatmentPlan> findByTreatmentId(Integer treatmentId);
}
