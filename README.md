# Horse Management System

This repository contains two application projects and the team's current SQL Server schema:

- [FE](FE/README.md): React frontend.
- [BE](BE/README.md): Java 21 / Spring Boot backend, Phase 1 base entities and repositories.
- [database](database/horse_management_sqlserver.sql): the current 40-table SQL Server schema for `horse_management`.
- [reports](reports/): project inspection documents. The existing inspection document describes the former 20-table schema and is historical.

Run frontend commands from `FE` and backend commands from `BE`. The backend loads its optional local `BE/.env` from the process working directory; actual credentials belong only in that ignored file or the process environment. Copy the variable names from `BE/.env.example` and verify the actual SQL Server settings before starting the application.

The backend preserves `spring.jpa.hibernate.ddl-auto=none` and `spring.sql.init.mode=never`; it does not run the schema script automatically.
