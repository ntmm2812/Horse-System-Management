package com.horsemanagement;

import com.horsemanagement.repository.HorseRepository;
import com.horsemanagement.repository.RoleRepository;
import com.horsemanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "RUN_DB_INTEGRATION_TESTS", matches = "true")
class PhaseOneDatabaseIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired RoleRepository roles;
    @Autowired UserRepository users;
    @Autowired HorseRepository horses;

    @Test
    @Transactional(readOnly = true)
    void existingRowsCanBeReadThroughAllThreeMappings() {
        assertThat(jdbc.queryForObject("SELECT DB_NAME()", String.class))
            .isEqualTo("horse_management");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables", Integer.class))
            .isEqualTo(40);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.columns "
            + "WHERE object_id = OBJECT_ID('dbo.users') AND name = 'password_hash'", Integer.class))
            .isEqualTo(1);

        var role = roles.findAll().stream().findFirst().orElseThrow();
        var user = users.findAll().stream().findFirst().orElseThrow();
        var horse = horses.findAll().stream().findFirst().orElseThrow();
        assertThat(role.getRoleCode()).isNotBlank();
        assertThat(user.getPasswordHash()).isNotBlank();
        assertThat(user.getRole().getRoleCode()).isNotBlank();
        assertThat(horse.getName()).isNotBlank();
        assertThat(horse.getOwner().getUsername()).isNotBlank();
        assertThat(horse.getHealthStatus()).isNotNull();
    }
}
