# Repository Guidelines

**Notice**
- read docs/ARCHITECTURE.md first

## Project Structure & Module Organization
This repository is a Maven multi-module Java 17 project rooted at `pom.xml`.

- `bacon-app/`: runnable Spring Boot entrypoints such as `bacon-boot`, `bacon-gateway`, `bacon-register`, and service starters.
- `bacon-biz/`: business domains split by bounded context, for example `bacon-order`, `bacon-upms`, `bacon-inventory`, and `bacon-payment`.
- `bacon-common/`: shared platform modules such as `bacon-common-web`, `bacon-common-security`, and `bacon-common-test`.
- `docs/ARCHITECTURE.md`: module boundaries and mono-app vs microservice assembly notes.

Within each domain, keep the existing layer pattern: `*-api`, `*-interfaces`, `*-application`, `*-domain`, `*-infra`.

## Build, Test, and Development Commands
- `mvn clean verify`: full multi-module build with tests.
- `mvn test`: run unit and integration tests across the repo.
- `mvn checkstyle:check`: run the repository Checkstyle rules from `checkstyle.xml`.
- `mvn -pl bacon-app/bacon-boot spring-boot:run`: run the monolith entrypoint locally.
- `mvn -pl bacon-app/bacon-gateway spring-boot:run`: run the gateway only.

Run commands from the repository root unless you are intentionally targeting one module with `-pl`.

## Coding Style & Naming Conventions
Use 4-space indentation for Java, XML, and YAML. Follow the base package `com.github.thundax.bacon`.

- Class names: `PascalCase`
- methods and fields: `camelCase`
- constants: `UPPER_SNAKE_CASE`
- module names: `bacon-<scope>` or `bacon-<domain>-<layer>`

Checkstyle currently enforces filename/type matching, no unused imports, no wildcard imports, and braces on control blocks. Keep controllers in `interfaces`, orchestration in `application`, core business rules in `domain`, and persistence/RPC code in `infra`.

## Testing Guidelines
Place tests under `src/test/java`, mirroring production packages. Use `spring-boot-starter-test`; shared test support belongs in `bacon-common/bacon-common-test`.

Name test classes `*Test` for unit coverage and `*IT` for broader integration scenarios. Add tests for application services, domain services, and controller contracts when behavior changes.

## Commit & Pull Request Guidelines
Current git history is mostly `init`, so there is no strong existing convention to preserve. Prefer short imperative commit messages such as `order: add create command validation`.

For pull requests, include:
- a brief problem/solution summary
- affected modules, for example `bacon-order-domain`
- linked issue or task ID when available
- request/response examples or screenshots for API/UI changes
- confirmation that `mvn clean verify` and `mvn checkstyle:check` passed
