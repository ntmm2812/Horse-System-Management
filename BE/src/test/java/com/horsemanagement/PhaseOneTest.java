package com.horsemanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horsemanagement.controller.TestController;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.Role;
import com.horsemanagement.entity.User;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PhaseOneTest {
    @Test
    void testEndpointKeepsItsContractWithoutDatabaseAccess() throws Exception {
        MockMvcBuilders.standaloneSetup(new TestController()).build()
            .perform(get("/api/test"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/plain"))
            .andExpect(content().string("Horse Management Backend is running"));
    }

    @Test
    void passwordHashIsExcludedEvenWhenUserIsNestedInHorse() throws Exception {
        User user = new User();
        user.setUsername("serialization-check");
        user.setPasswordHash("test-only-secret-marker");
        Horse horse = new Horse();
        horse.setOwner(user);
        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.writeValueAsString(user))
            .contains("serialization-check")
            .doesNotContain("passwordHash", "password_hash", "test-only-secret-marker");
        assertThat(mapper.writeValueAsString(horse))
            .doesNotContain("passwordHash", "password_hash", "test-only-secret-marker");
    }

    @Test
    void baseMappingsInitializeOfflineWithoutSchemaGeneration() {
        // Hibernate metadata only: this does not connect to or validate a live DB.
        var registry = new StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect")
            .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
            .applySetting("hibernate.hbm2ddl.auto", "none")
            .build();
        try {
            var metadata = new MetadataSources(registry)
                .addAnnotatedClass(Role.class)
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Horse.class)
                .buildMetadata();
            try (var sessionFactory = metadata.buildSessionFactory()) {
                assertThat(sessionFactory.getMetamodel().getEntities()).hasSize(3);
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
