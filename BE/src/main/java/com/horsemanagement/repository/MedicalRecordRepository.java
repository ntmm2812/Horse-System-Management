package com.horsemanagement.repository;

import com.horsemanagement.entity.MedicalRecord;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer>,
    JpaSpecificationExecutor<MedicalRecord> {
    @Override
    @EntityGraph(attributePaths = {"horse", "vet"})
    Page<MedicalRecord> findAll(Specification<MedicalRecord> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"horse", "vet"})
    Optional<MedicalRecord> findByRecordId(Integer recordId);

    boolean existsByIncident_IncidentId(Integer incidentId);
}
