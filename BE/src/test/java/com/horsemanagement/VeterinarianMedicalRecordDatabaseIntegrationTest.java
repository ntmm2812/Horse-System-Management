package com.horsemanagement;

import com.horsemanagement.dto.MedicalRecordSummaryDto;
import com.horsemanagement.service.VeterinarianMedicalRecordService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "RUN_DB_INTEGRATION_TESTS", matches = "true")
class VeterinarianMedicalRecordDatabaseIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired VeterinarianMedicalRecordService service;

    @Test
    @Transactional(readOnly = true)
    void realMedicalRecordMappingsAndFiltersAreReadOnly() {
        assertThat(jdbc.queryForObject("SELECT DB_NAME()", String.class))
            .isEqualTo("horse_management");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables", Integer.class))
            .isEqualTo(40);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.indexes "
            + "WHERE object_id = OBJECT_ID('dbo.medical_records') "
            + "AND name = 'UX_medical_incident' AND is_unique = 1 AND has_filter = 1",
            Integer.class)).isEqualTo(1);

        var countBefore = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.medical_records",
            Integer.class);
        var page = service.list(null, null, null, null, 0, 20);
        assertThat(page.totalElements()).isEqualTo(countBefore.longValue());
        assertThat(page.content()).isNotEmpty();
        var first = page.content().getFirst();
        var horseBefore = horseState(first.horseId());

        var detail = service.get(first.recordId());
        assertThat(detail.horseId()).isEqualTo(first.horseId());
        assertThat(detail.vetId()).isEqualTo(first.vetId());
        assertThat(detail.horseName()).isNotBlank();
        assertThat(detail.vetName()).isNotBlank();
        assertThat(service.list(first.horseId(), null, null, null, 0, 20).content())
            .extracting(MedicalRecordSummaryDto::recordId).contains(first.recordId());
        assertThat(service.list(null, first.vetId(), null, null, 0, 20).content())
            .extracting(MedicalRecordSummaryDto::recordId).contains(first.recordId());
        var examDay = first.examDate().toLocalDate();
        assertThat(service.list(null, null, examDay, examDay, 0, 20).content())
            .extracting(MedicalRecordSummaryDto::recordId).contains(first.recordId());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.medical_records",
            Integer.class)).isEqualTo(countBefore);
        assertThat(horseState(first.horseId())).isEqualTo(horseBefore);
    }

    private Map<String, Object> horseState(Integer horseId) {
        return jdbc.queryForMap("SELECT health_status, readiness_status, is_training_locked "
            + "FROM dbo.horses WHERE horse_id = ?", horseId);
    }
}
