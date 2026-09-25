# Horse Management System backend

Initial Java 21 / Spring Boot backend. Open this `BE` folder as a Maven project
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
| `repository` | Future Spring Data JPA repositories |
| `entity` | Future mappings to the supplied SQL Server tables |
| `dto` | Future API request/response objects instead of exposing entities |
| `mapper` | Future conversions between entities and DTOs |
| `config` | General application configuration |
| `security` | Empty, reserved for future security work |
| `exception` | Future exceptions and global exception handling |

`HorseManagementApplication.java` is the entry point. Configuration lives in
`src/main/resources/application.properties`. The empty
`src/test/java/com/horsemanagement` directory is reserved for future tests.
Reserved directories contain only `.gitkeep` placeholders so Git preserves them.

## SQL Server database

The SQL scripts and sample data are in [database](database/README.md), together
with the ERD. They create `HorseManagement` with 20 tables, 25 foreign keys,
and 950 sample rows. Club Manager corresponds to the `Admin` role.

On a new database, run the files in SSMS in this order:

```text
database/00_create_database.sql
database/01_schema.sql
database/02_seed.sql
database/03_verify.sql
```

Alternatively, from `BE/database`, run:

```powershell
sqlcmd -S '.\SQLEXPRESS' -E -C -b -f 65001 -i setup.sql
```

Use your own SQL Server instance name. The schema script requires no existing
`dbo` tables, and the seed requires empty tables. Do not run both installation
methods on the same populated database. Sample password fields are placeholders,
not working login credentials. See the database README for each script's purpose.

### Backend connection

The JPA and SQL Server driver dependencies are present, but datasource
auto-configuration is temporarily disabled through `spring.autoconfigure.exclude`.
This lets the application start without a database URL, username, or password.
When SQL Server connection information is supplied later, remove that exclusion
and configure the connection. Keep `spring.jpa.hibernate.ddl-auto=none` and
`spring.sql.init.mode=never` to prevent automatic schema changes and SQL scripts.
The SQL scripts are run manually; this backend scaffold does not yet include
entities, CRUD, authentication, JWT, or automatic migrations.

Dependencies are Spring Web, Spring Data JPA, Bean Validation, Microsoft SQL Server
JDBC, Lombok, DevTools, and Spring Boot Test. Spring Boot manages their versions.

The shared repository contains separate `FE` and `BE` projects. Run backend Maven
commands from `BE` and frontend commands from `FE`.
