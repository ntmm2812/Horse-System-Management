package com.horsemanagement.repository;

import com.horsemanagement.entity.TrainingLock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainingLockRepository extends JpaRepository<TrainingLock, Integer>,
    JpaSpecificationExecutor<TrainingLock> {
    @Override
    @EntityGraph(attributePaths = {"horse", "injury", "lockedBy", "releasedBy"})
    Page<TrainingLock> findAll(Specification<TrainingLock> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"horse", "injury", "lockedBy", "releasedBy"})
    Optional<TrainingLock> findByLockId(Integer lockId);

    @EntityGraph(attributePaths = {"horse", "injury", "lockedBy", "releasedBy"})
    Page<TrainingLock> findByHorse_HorseId(Integer horseId, Pageable pageable);

    @EntityGraph(attributePaths = {"horse", "injury", "lockedBy", "releasedBy"})
    List<TrainingLock> findByHorse_HorseIdAndStatusOrderByLockedAtDescLockIdDesc(
        Integer horseId, TrainingLock.Status status);

    boolean existsByHorse_HorseIdAndStatus(Integer horseId, TrainingLock.Status status);

    // Read the scalar first, then lock the horse row before loading this lock entity.
    @Query(value = "SELECT horse_id FROM dbo.training_locks WHERE lock_id = :lockId",
        nativeQuery = true)
    Optional<Integer> findHorseIdByLockId(@Param("lockId") Integer lockId);
}
