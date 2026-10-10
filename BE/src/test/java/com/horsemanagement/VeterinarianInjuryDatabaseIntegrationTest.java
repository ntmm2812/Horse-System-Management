package com.horsemanagement;

import com.horsemanagement.dto.InjurySummaryDto;
import com.horsemanagement.service.VeterinarianInjuryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "RUN_DB_INTEGRATION_TESTS", matches = "true")
class VeterinarianInjuryDatabaseIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired VeterinarianInjuryService service;

    @Test
    @Transactional(readOnly = true)
    void realInjuryAndProgressMappingsAndFiltersAreReadOnly() {
        assertThat(jdbc.queryForObject("SELECT DB_NAME()", String.class))
            .isEqualTo("horse_management");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables", Integer.class))
            .isEqualTo(40);

        var injuryCountBefore = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.injuries", Integer.class);
        var progressCountBefore = jdbc.queryForObject(
            "SELECT COUNT(*) FROM dbo.injury_progress_logs", Integer.class);
        var lockCountBefore = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.training_locks", Integer.class);
        var lockStateBefore = jdbc.queryForList("SELECT lock_id, status, released_at "
            + "FROM dbo.training_locks ORDER BY lock_id");
        var treatmentCountBefore = jdbc.queryForObject(
            "SELECT COUNT(*) FROM dbo.treatment_plans", Integer.class);
        var horseStateBefore = jdbc.queryForList("SELECT horse_id, health_status, "
            + "is_training_locked FROM dbo.horses ORDER BY horse_id");

        var page = service.list(null, null, null, null, 0, 20);
        assertThat(page.totalElements()).isEqualTo(injuryCountBefore.longValue());
        assertThat(page.content()).isNotEmpty();
        var first = page.content().getFirst();
        var detail = service.get(first.injuryId());
        assertThat(detail.recordId()).isEqualTo(first.recordId());
        assertThat(detail.horseId()).isEqualTo(first.horseId());
        assertThat(detail.horseName()).isNotBlank();
        assertThat(service.list(first.horseId(), null, null, null, 0, 20).content())
            .extracting(InjurySummaryDto::injuryId).contains(first.injuryId());
        assertThat(service.list(null, first.status(), null, null, 0, 20).content())
            .extracting(InjurySummaryDto::injuryId).contains(first.injuryId());
        assertThat(service.list(null, null, first.severity(), first.bodySystem(), 0, 20).content())
            .extracting(InjurySummaryDto::injuryId).contains(first.injuryId());

        var logs = service.progress(first.injuryId(), 0, 20);
        var expectedLogs = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.injury_progress_logs "
            + "WHERE injury_id = ?", Integer.class, first.injuryId());
        assertThat(logs.totalElements()).isEqualTo(expectedLogs.longValue());
        if (!logs.content().isEmpty()) {
            assertThat(logs.content().getFirst().injuryId()).isEqualTo(first.injuryId());
        }

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.injuries", Integer.class))
            .isEqualTo(injuryCountBefore);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.injury_progress_logs", Integer.class))
            .isEqualTo(progressCountBefore);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.training_locks", Integer.class))
            .isEqualTo(lockCountBefore);
        assertThat(jdbc.queryForList("SELECT lock_id, status, released_at "
            + "FROM dbo.training_locks ORDER BY lock_id")).isEqualTo(lockStateBefore);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.treatment_plans", Integer.class))
            .isEqualTo(treatmentCountBefore);
        assertThat(jdbc.queryForList("SELECT horse_id, health_status, "
            + "is_training_locked FROM dbo.horses ORDER BY horse_id"))
            .isEqualTo(horseStateBefore);
    }
}
