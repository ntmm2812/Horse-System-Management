package com.horsemanagement;

import com.horsemanagement.dto.PreventiveCareDto;
import com.horsemanagement.entity.PreventiveCareSchedule;
import com.horsemanagement.service.VeterinarianPreventiveCareService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "RUN_DB_INTEGRATION_TESTS", matches = "true")
class VeterinarianPreventiveCareDatabaseIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired VeterinarianPreventiveCareService service;

    @Test
    @Transactional(readOnly = true)
    void realScheduleMappingFiltersAndOverdueIdentificationAreReadOnly() {
        assertThat(jdbc.queryForObject("SELECT DB_NAME()", String.class)).isEqualTo("horse_management");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables", Integer.class)).isEqualTo(40);
        var before = jdbc.queryForList("SELECT schedule_id, horse_id, care_type, due_date, status, "
            + "completed_date, performed_by, notes FROM dbo.preventive_care_schedules ORDER BY schedule_id");
        var horseBefore = jdbc.queryForList("SELECT horse_id, health_status, readiness_status, "
            + "is_training_locked FROM dbo.horses ORDER BY horse_id");
        var page = service.list(null, null, null, null, null, false, 0, 100);
        assertThat(page.totalElements()).isEqualTo(before.size());
        assertThat(page.content()).isNotEmpty();
        var first = page.content().getFirst();
        assertThat(service.get(first.scheduleId()).horseName()).isNotBlank();
        assertThat(service.horseHistory(first.horseId(), null, 0, 100).content())
            .extracting(PreventiveCareDto::scheduleId).contains(first.scheduleId());
        assertThat(service.list(first.horseId(), first.careType(), first.status(),
            first.dueDate(), first.dueDate(), false, 0, 100).content())
            .extracting(PreventiveCareDto::scheduleId).contains(first.scheduleId());
        var pastDue = service.list(null, null, null, null, null, true, 0, 100);
        var expectedPastDue = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.preventive_care_schedules "
            + "WHERE status = 'PENDING' AND due_date < ?", Integer.class, LocalDate.now());
        assertThat(pastDue.totalElements()).isEqualTo(expectedPastDue.longValue());
        assertThat(pastDue.content()).allSatisfy(row -> {
            assertThat(row.status()).isEqualTo(PreventiveCareSchedule.Status.PENDING);
            assertThat(row.pendingPastDue()).isTrue();
        });
        assertThat(jdbc.queryForList("SELECT schedule_id, horse_id, care_type, due_date, status, "
            + "completed_date, performed_by, notes FROM dbo.preventive_care_schedules ORDER BY schedule_id"))
            .isEqualTo(before);
        assertThat(jdbc.queryForList("SELECT horse_id, health_status, readiness_status, "
            + "is_training_locked FROM dbo.horses ORDER BY horse_id")).isEqualTo(horseBefore);
    }
}
