package com.horsemanagement;

import com.horsemanagement.repository.HorseHealthMetricRepository;
import com.horsemanagement.service.VeterinarianHorseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "RUN_DB_INTEGRATION_TESTS", matches = "true")
class VeterinarianHorseDatabaseIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired VeterinarianHorseService service;
    @Autowired HorseHealthMetricRepository metrics;

    @Test
    @Transactional(readOnly = true)
    void readsActualHorsesAndMetricHistoryWithoutChangingData() {
        assertThat(jdbc.queryForObject("SELECT DB_NAME()", String.class))
            .isEqualTo("horse_management");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables", Integer.class))
            .isEqualTo(40);
        var horseId = jdbc.queryForObject("SELECT TOP (1) horse_id FROM dbo.horses ORDER BY horse_id",
            Integer.class);
        var before = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.horse_health_metrics",
            Integer.class);

        var overview = service.listHorses(null, 0, 20);
        assertThat(overview.content()).isNotEmpty();
        assertThat(overview.content().getFirst().ownerName()).isNotBlank();
        var detail = service.getHorse(horseId);
        assertThat(detail.horseId()).isEqualTo(horseId);
        assertThat(detail.ownerName()).isNotBlank();
        var history = service.getHealthMetrics(horseId, 0, 20);
        assertThat(history.totalElements()).isEqualTo(metrics
            .findByHorse_HorseId(horseId, org.springframework.data.domain.PageRequest.of(0, 20))
            .getTotalElements());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.horse_health_metrics",
            Integer.class)).isEqualTo(before);
    }
}
