# Horse Management System backend

Phase 1 Java 21 / Spring Boot backend. Open this `BE` folder as a Maven project
in your IDE. The frontend remains a separate project in `FE`.

## Build and run

Install JDK 21 and set `JAVA_HOME` to its installation directory.
In PowerShell, from `Horse_Management/BE`:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

On macOS/Linux, use `sh mvnw clean verify` and `sh mvnw spring-boot:run`.
The wrapper downloads Maven on first use; Maven and dependency downloads require
internet access. If Maven is already installed, `mvn clean verify` and
`mvn spring-boot:run` also work.

Wait for the startup message showing port `8080`. Stop with `Ctrl+C`.
To run the packaged application instead:

```powershell
java -jar target/horse-management-0.0.1-SNAPSHOT.jar
```

## Check the backend

Open <http://localhost:8080/api/test> or run in another PowerShell window:

```powershell
(Invoke-WebRequest http://localhost:8080/api/test).Content
```

Expected HTTP status: `200`. Expected text:

```text
Horse Management Backend is running
```

The root URL `/` has no endpoint and returns `404`.

## Packages

All packages are under `src/main/java/com/horsemanagement`.

| Package | Responsibility |
| --- | --- |
| `controller` | REST requests and responses; currently only `/api/test` |
| `service` | Future business logic |
| `repository` | RoleRepository, UserRepository, HorseRepository |
| `entity` | Role, User, Horse mapped to the new 40-table SQL schema |
| `dto` | Future API request/response objects instead of exposing entities |
| `mapper` | Future conversions between entities and DTOs |
| `config` | General application configuration |
| `security` | Empty, reserved for future security work |
| `exception` | Future exceptions and global exception handling |

`HorseManagementApplication.java` is the entry point. Configuration lives in
`src/main/resources/application.properties`. Tests in
`src/test/java/com/horsemanagement` run without database access.
Reserved directories are intentionally empty; Git does not track empty directories.

## Database connection

Source of truth: `../database/horse_management_sqlserver.sql`, the new 40-table
schema. Datasource auto-configuration is enabled. Local configuration is loaded
from BE/.env using Spring Boot's standard config import:

`spring.config.import=optional:file:./.env[.properties]`

No dotenv library is needed. The extension hint loads the file as Java properties
into Spring's Environment (not into System.getenv). Actual OS environment variables
override the file. The file is optional so deployment can use only environment
variables. Missing connection credentials must be supplied before starting.

| Variable | Value |
| --- | --- |
| DB_HOST | Actual SQL Server TCP hostname; localhost is only a development example |
| DB_PORT | Actual TCP port; 1433 is only a development example |
| DB_NAME | horse_management |
| DB_USERNAME | Actual SQL login; blank in the local file until confirmed |
| DB_PASSWORD | Actual password; blank in the local file until supplied |
| DB_ENCRYPT | Confirm the server encryption policy; development example is true |
| DB_TRUST_SERVER_CERTIFICATE | Confirm certificate policy; development example is true |

The sample .env.example contains demonstration values only; they are not confirmed
credentials and must not be used to claim connectivity. The actual .env is ignored
by Git; .env.example is explicitly allowed. Do not copy the sample over an existing
local file containing credentials. Neither file is packaged in the application JAR.

Use Java properties syntax: KEY=value, no export prefix, no surrounding quotes.
Backslashes must be escaped as double backslashes; a literal leading space in a
value must be escaped. Quotes would become part of the password. Do not print
the file or put credentials in URLs, source, command-line arguments or logs.

Maven: run from the BE directory, then use `mvn compile`, `mvn verify` or
`mvn spring-boot:run` (the Maven wrapper works too).
IntelliJ IDEA: run HorseManagementApplication, set the Run Configuration working
directory to the absolute BE directory, and use JDK 21. If BE is opened as its own
project, `$PROJECT_DIR$` points there; if the parent project is opened, select its
BE subdirectory. No EnvFile plugin is necessary. For java -jar, also start from BE.
The provided URL uses SQL login with TCP host/port. Windows integrated
authentication requires separately confirmed driver/authentication settings.

`spring.jpa.hibernate.ddl-auto=none` and `spring.sql.init.mode=never` remain set.
No schema creation, update, deletion, migration or seed execution is performed.
No business CRUD or authentication is implemented.

## Phase 1 mapping and verification

- Role maps all columns of `roles`; User maps all columns of `users`.
- User has a unidirectional lazy Role relationship. Jackson ignores passwordHash;
  entities do not generate a toString containing fields.
- Horse maps all columns of `horses`, including `health_status`, with lazy owner
  and manager relationships. `stall_id` stays a nullable Integer backed by the
  existing SQL FK; no Stall entity is created in this phase.
- Horse.HealthStatus is a nested enum, not a separate entity. Enum values match
  SQL CHECK constraints. NVARCHAR uses Unicode mapping, decimals preserve precision
  and scale, and DATETIME2 maps to LocalDateTime.
- Status defaults mirror SQL defaults. Hibernate maintains creation/update timestamps
  for future JPA writes; reads do not update records.
- No inverse collections or cascades. Table names are unqualified, matching the
  SQL script; the connection user's schema resolution must match the installation.
- Veterinarian is identified by role_code `VETERINARIAN`, never a fixed role ID.
- SQL Server UNIQUE constraints on nullable horse stall/registration/microchip
  columns are preserved, including their restrictions on NULL values.

`mvn verify` checks the endpoint contract with standalone MockMvc, password
serialization protection and offline Hibernate metadata initialization. These tests
do not connect to SQL Server and do not prove live schema compatibility.
After credentials are configured, verify startup, read-only entity queries and
the live /api/test endpoint. EnvironmentConfigurationTest also verifies .env import,
datasource binding and environment overrides without creating a database connection.
Do not insert demo records or modify team data.
`ddl-auto=none` does not validate live column mappings by itself.

Dependencies are Spring Web, Spring Data JPA, Bean Validation, Microsoft SQL Server
JDBC, Lombok, DevTools, and Spring Boot Test. Spring Boot manages their versions.

The workspace root and `BE` had no Git repository when this scaffold was created;
the existing repository is inside `FE`. No repository was initialized or moved.
