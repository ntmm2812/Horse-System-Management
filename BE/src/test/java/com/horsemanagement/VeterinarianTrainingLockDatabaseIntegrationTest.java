package com.horsemanagement;

import com.horsemanagement.dto.TrainingLockDto;
import com.horsemanagement.entity.TrainingLock;
import com.horsemanagement.service.VeterinarianTrainingLockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "RUN_DB_INTEGRATION_TESTS", matches = "true")
class VeterinarianTrainingLockDatabaseIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired VeterinarianTrainingLockService service;

    @Test
    @Transactional(readOnly = true)
    void realLockHistoryFiltersAndEligibilityAreReadOnly() {
        assertThat(jdbc.queryForObject("SELECT DB_NAME()", String.class))
            .isEqualTo("horse_management");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables", Integer.class))
            .isEqualTo(40);
        // Only the primary key is unique: the database permits multiple ACTIVE locks per horse.
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.indexes "
            + "WHERE object_id = OBJECT_ID('dbo.training_locks') "
            + "AND is_unique = 1 AND is_primary_key = 0", Integer.class)).isZero();

        var lockStateBefore = jdbc.queryForList("SELECT lock_id, horse_id, lock_level, "
            + "status, released_at, released_by FROM dbo.training_locks ORDER BY lock_id");
        var horseStateBefore = jdbc.queryForList("SELECT horse_id, health_status, "
            + "readiness_status, is_training_locked FROM dbo.horses ORDER BY horse_id");
        var sessionCountBefore = jdbc.queryForObject(
            "SELECT COUNT(*) FROM dbo.training_sessions", Integer.class);

        var page = service.list(null, null, null, 0, 20);
        assertThat(page.totalElements()).isEqualTo(lockStateBefore.size());
        assertThat(page.content()).isNotEmpty();
        var first = page.content().getFirst();
        var detail = service.get(first.lockId());
        assertThat(detail.horseId()).isEqualTo(first.horseId());
        assertThat(detail.horseName()).isNotBlank();
        assertThat(detail.lockedByName()).isNotBlank();
        assertThat(service.list(first.horseId(), first.status(), first.lockLevel(), 0, 20)
            .content()).extracting(TrainingLockDto::lockId).contains(first.lockId());
        assertThat(service.horseHistory(first.horseId(), 0, 20).content())
            .extracting(TrainingLockDto::lockId).contains(first.lockId());

        var horse = service.eligibility(first.horseId());
        assertThat(horse.horseId()).isEqualTo(first.horseId());
        var activeLevels = horse.activeLocks().stream().map(TrainingLockDto::lockLevel).toList();
        boolean full = activeLevels.contains(TrainingLock.LockLevel.FULL);
        boolean heavy = full || activeLevels.contains(TrainingLock.LockLevel.HEAVY_ONLY);
        assertThat(horse.restrictedByLocks().light()).isEqualTo(full);
        assertThat(horse.restrictedByLocks().moderate()).isEqualTo(full);
        assertThat(horse.restrictedByLocks().heavy()).isEqualTo(heavy);
        assertThat(horse.isTrainingLocked()).isEqualTo(jdbc.queryForObject(
            "SELECT is_training_locked FROM dbo.horses WHERE horse_id = ?",
            Boolean.class, first.horseId()));

        assertThat(jdbc.queryForList("SELECT lock_id, horse_id, lock_level, "
            + "status, released_at, released_by FROM dbo.training_locks ORDER BY lock_id"))
            .isEqualTo(lockStateBefore);
        assertThat(jdbc.queryForList("SELECT horse_id, health_status, "
            + "readiness_status, is_training_locked FROM dbo.horses ORDER BY horse_id"))
            .isEqualTo(horseStateBefore);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.training_sessions", Integer.class))
            .isEqualTo(sessionCountBefore);
    }
}
