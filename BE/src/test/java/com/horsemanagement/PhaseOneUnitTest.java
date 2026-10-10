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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PhaseOneUnitTest {
    @Test
    void testEndpointContractWithoutDatabase() throws Exception {
        MockMvcBuilders.standaloneSetup(new TestController()).build()
            .perform(get("/api/test"))
            .andExpect(status().isOk())
            .andExpect(content().string("Horse Management Backend is running"));
    }

    @Test
    void passwordHashIsNeverSerialized() throws Exception {
        var user = new User();
        user.setUsername("test-user");
        user.setPasswordHash("test-only-marker");
        var horse = new Horse();
        horse.setOwner(user);
        var mapper = new ObjectMapper();
        assertThat(mapper.writeValueAsString(user))
            .doesNotContain("passwordHash", "password_hash", "test-only-marker");
        assertThat(mapper.writeValueAsString(horse))
            .doesNotContain("passwordHash", "password_hash", "test-only-marker");
    }

    @Test
    void entityMetadataInitializesWithoutDatabaseOrDdl() {
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
            try (var factory = metadata.buildSessionFactory()) {
                assertThat(factory.getMetamodel().getEntities()).hasSize(3);
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
