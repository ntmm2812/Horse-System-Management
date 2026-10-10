package com.horsemanagement.repository;

import com.horsemanagement.entity.Horse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorseRepository extends JpaRepository<Horse, Integer> {
    @Override
    @EntityGraph(attributePaths = {"owner", "manager"})
    Page<Horse> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "manager"})
    Page<Horse> findByHealthStatus(Horse.HealthStatus healthStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "manager"})
    Optional<Horse> findByHorseId(Integer horseId);

    // Update only this column: a medical exam must not release a training lock.
    @Modifying
    @Query("update Horse h set h.healthStatus = :status where h.horseId = :horseId")
    int updateHealthStatus(@Param("horseId") Integer horseId,
                           @Param("status") Horse.HealthStatus status);

    // Serializes all Training Lock writes for one horse until transaction commit.
    @Query(value = "SELECT * FROM dbo.horses WITH (UPDLOCK, HOLDLOCK) "
        + "WHERE horse_id = :horseId", nativeQuery = true)
    Optional<Horse> lockForTrainingLocks(@Param("horseId") Integer horseId);

    // Update only the lock flag; never overwrite health/readiness from a stale entity.
    @Modifying(flushAutomatically = true)
    @Query("update Horse h set h.trainingLocked = :locked where h.horseId = :horseId")
    int updateTrainingLocked(@Param("horseId") Integer horseId,
                             @Param("locked") boolean locked);
}
