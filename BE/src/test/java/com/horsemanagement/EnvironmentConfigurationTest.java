package com.horsemanagement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentConfigurationTest {
    @TempDir
    Path temporaryDirectory;

    private StandardEnvironment load(String importLocation, Map<String, Object> variables) {
        var environment = new StandardEnvironment();
        // Isolate tests from real credentials; never connect to a datasource.
        environment.getPropertySources().remove("systemProperties");
        environment.getPropertySources().replace("systemEnvironment",
            new SystemEnvironmentPropertySource("systemEnvironment", variables));
        environment.getPropertySources().addFirst(new MapPropertySource("testLocation",
            importLocation == null
                ? Map.of("spring.config.location", "classpath:/application.properties")
                : Map.of("spring.config.location", "classpath:/application.properties",
                         "spring.config.import", importLocation)));
        ConfigDataEnvironmentPostProcessor.applyTo(environment);
        return environment;
    }

    @Test
    void importsPropertiesEnvAndBindsDatasourceWithoutConnecting() throws Exception {
        Path file = temporaryDirectory.resolve(".env");
        String fakePassword = "fixture-only-value";
        Files.writeString(file, "DB_HOST=fixture.invalid\nDB_PORT=1544\n"
            + "DB_NAME=horse_management\nDB_USERNAME=fixture_user\nDB_PASSWORD="
            + fakePassword + "\nDB_ENCRYPT=true\nDB_TRUST_SERVER_CERTIFICATE=false\n");
        var environment = load("optional:" + file.toUri() + "[.properties]", Map.of());
        var datasource = Binder.get(environment)
            .bind("spring.datasource", DataSourceProperties.class).get();
        assertThat(datasource.getUrl()).isEqualTo(
            "jdbc:sqlserver://fixture.invalid:1544;databaseName=horse_management;"
            + "encrypt=true;trustServerCertificate=false");
        assertThat(datasource.getUsername()).isEqualTo("fixture_user");
        // Boolean assertion prevents secret values from appearing in failure output.
        assertThat(fakePassword.equals(datasource.getPassword())).isTrue();
        assertThat(datasource.getDriverClassName())
            .isEqualTo("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("none");
        assertThat(environment.getProperty("spring.sql.init.mode")).isEqualTo("never");
        var overridden = load("optional:" + file.toUri() + "[.properties]",
            Map.of("DB_HOST", "override.invalid"));
        assertThat(overridden.getProperty("spring.datasource.url"))
            .startsWith("jdbc:sqlserver://override.invalid:1544;");
    }

    @Test
    void environmentVariablesOverrideLocalFileWhenOptionalImportIsMissing() {
        var environment = load("optional:"
            + temporaryDirectory.resolve("missing.env").toUri() + "[.properties]",
            Map.of("DB_HOST", "localhost", "DB_PORT", "1433",
                "DB_NAME", "horse_management", "DB_ENCRYPT", "true",
                "DB_TRUST_SERVER_CERTIFICATE", "true",
                "DB_USERNAME", "fixture_user", "DB_PASSWORD", "fixture-only-value"));
        assertThat(environment.getProperty("spring.datasource.url")).isEqualTo(
            "jdbc:sqlserver://localhost:1433;databaseName=horse_management;"
            + "encrypt=true;trustServerCertificate=true");
    }

    @Test
    void actualImportPathLoadsLocalFileWhenPresent() {
        // This also works in CI where the ignored local file is absent.
        var environment = load(null, Map.of());
        boolean loaded = StreamSupport.stream(
            environment.getPropertySources().spliterator(), false)
            .anyMatch(source -> source.getName().contains(".env"));
        assertThat(loaded).isEqualTo(Files.isRegularFile(Path.of(".env")));
        if (loaded) {
            assertThat(environment.containsProperty("DB_HOST")).isTrue();
            assertThat(environment.containsProperty("DB_PASSWORD")).isTrue();
        }
    }
}
