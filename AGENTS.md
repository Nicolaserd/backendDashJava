# Repository Guidelines

## Project Structure & Module Organization

This repository is a minimal Spring Boot backend built with Maven. Main Java source files live under `src/main/java/com/example/demo`, with `DemoApplication.java` as the application entry point. Runtime configuration belongs in `src/main/resources`, currently `application.properties`. Tests mirror the main package structure under `src/test/java/com/example/demo`. Maven wrapper files (`mvnw`, `mvnw.cmd`) should be used so contributors do not need a global Maven install.

## Build, Test, and Development Commands

Use Java 21 for all commands.

```powershell
.\mvnw.cmd test
```

Compiles the project and runs the JUnit/Spring Boot test suite.

```powershell
.\mvnw.cmd spring-boot:run
```

Starts the backend locally using the current `application.properties`.

```powershell
.\mvnw.cmd clean package
```

Removes generated build output and creates the application JAR in `target/`.

## Coding Style & Naming Conventions

Use standard Java conventions: 4-space indentation, `PascalCase` for classes, `camelCase` for methods and fields, and uppercase constants with underscores. Keep packages lowercase and grouped by responsibility as the project grows, for example `controller`, `service`, `repository`, and `model`. Prefer constructor injection for Spring beans. Avoid unrelated formatting churn in files you are not changing.

## Testing Guidelines

The project uses JUnit 5 with Spring Boot test support. Name test classes with the `*Tests` suffix, matching the current `DemoApplicationTests` pattern. Add focused tests for new controllers, services, or configuration changes. Run `.\mvnw.cmd test` before submitting changes. There is no fixed coverage threshold yet, but new behavior should include meaningful tests.

## Commit & Pull Request Guidelines

The current history only contains an initial commit, so use clear imperative commit messages going forward, such as `Add health check endpoint` or `Configure database connection`. Pull requests should include a short summary, testing performed, linked issues when applicable, and notes for configuration or environment changes. Include screenshots only when a change affects visible API documentation or UI assets.

## Security & Configuration Tips

Do not commit secrets, API keys, database passwords, or local machine paths. Put environment-specific values in local environment variables or an ignored profile-specific properties file. Keep `application.properties` limited to safe defaults.
