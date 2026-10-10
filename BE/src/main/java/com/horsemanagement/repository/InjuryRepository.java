package com.horsemanagement.repository;

import com.horsemanagement.entity.Injury;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InjuryRepository extends JpaRepository<Injury, Integer>,
    JpaSpecificationExecutor<Injury> {
    @Override
    @EntityGraph(attributePaths = {"medicalRecord", "medicalRecord.horse"})
    Page<Injury> findAll(Specification<Injury> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"medicalRecord", "medicalRecord.horse"})
    Optional<Injury> findByInjuryId(Integer injuryId);
}
