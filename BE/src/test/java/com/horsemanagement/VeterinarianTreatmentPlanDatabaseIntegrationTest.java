package com.horsemanagement;

import com.horsemanagement.dto.TreatmentPlanSummaryDto;
import com.horsemanagement.repository.SupplyRepository;
import com.horsemanagement.service.VeterinarianTreatmentPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "RUN_DB_INTEGRATION_TESTS", matches = "true")
class VeterinarianTreatmentPlanDatabaseIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired VeterinarianTreatmentPlanService service;
    @Autowired SupplyRepository supplies;

    @Test
    @Transactional(readOnly = true)
    void realTreatmentAndPrescriptionMappingsAreReadOnly() {
        assertThat(jdbc.queryForObject("SELECT DB_NAME()", String.class))
            .isEqualTo("horse_management");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables", Integer.class))
            .isEqualTo(40);

        var planCountBefore = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.treatment_plans", Integer.class);
        var prescriptionCountBefore = jdbc.queryForObject(
            "SELECT COUNT(*) FROM dbo.prescription_items", Integer.class);
        var horseStateBefore = jdbc.queryForList(
            "SELECT horse_id, health_status, is_training_locked FROM dbo.horses ORDER BY horse_id");
        var lockCountBefore = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.training_locks", Integer.class);

        var page = service.list(null, null, 0, 20);
        assertThat(page.totalElements()).isEqualTo(planCountBefore.longValue());
        assertThat(page.content()).isNotEmpty();
        var first = page.content().getFirst();
        var detail = service.get(first.treatmentId());
        assertThat(detail.recordId()).isEqualTo(first.recordId());
        assertThat(detail.description()).isEqualTo(first.description());
        assertThat(service.list(first.recordId(), null, 0, 20).content())
            .extracting(TreatmentPlanSummaryDto::treatmentId).contains(first.treatmentId());
        assertThat(service.list(null, first.status(), 0, 20).content())
            .extracting(TreatmentPlanSummaryDto::treatmentId).contains(first.treatmentId());
        assertThat(service.listPrescriptions(first.treatmentId()))
            .isEqualTo(detail.prescriptions());

        var existingSupplyId = jdbc.queryForObject(
            "SELECT TOP 1 supply_id FROM dbo.supplies ORDER BY supply_id", Integer.class);
        var supply = supplies.findBySupplyId(existingSupplyId).orElseThrow();
        assertThat(supply.getSupplyName()).isNotBlank();
        assertThat(supply.getCategory().getCategoryType()).isNotNull();

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.treatment_plans", Integer.class))
            .isEqualTo(planCountBefore);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.prescription_items", Integer.class))
            .isEqualTo(prescriptionCountBefore);
        assertThat(jdbc.queryForList(
            "SELECT horse_id, health_status, is_training_locked FROM dbo.horses ORDER BY horse_id"))
            .isEqualTo(horseStateBefore);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.training_locks", Integer.class))
            .isEqualTo(lockCountBefore);
    }
}
