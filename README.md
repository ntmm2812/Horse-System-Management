# Horse Management System

This repository contains two separate projects:

- [FE](FE/README.md): the existing React frontend.
- [BE](BE/README.md): the initial Java 21 / Spring Boot backend.

Run frontend commands from `FE` and backend commands from `BE`.

## Run the backend on Windows

```powershell
cd BE
.\mvnw.cmd spring-boot:run
```

Check <http://localhost:8080/api/test>. It returns:

```text
Horse Management Backend is running
```

Database configuration and business features are deferred. See the backend README
for build instructions and package responsibilities.
