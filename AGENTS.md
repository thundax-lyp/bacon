# Repository Guidelines

## Basic Rules

- Read `docs/README.md` first.
- Follow `docs/README.md` to load only the minimum required docs.
- Read `docs/ARCHITECTURE.md` before implementation work.
- Prefer the simplest workable solution.
- Do not add configuration, abstraction, directory levels, or code unless necessary.

## Project Structure

This repository is a Maven multi-module Java 17 project rooted at `pom.xml`.

- `bacon-app/`: runnable entry modules such as `bacon-mono-boot`, `bacon-gateway`, `bacon-register`, and service starters.
- `bacon-biz/`: business domains such as `bacon-order`, `bacon-upms`, `bacon-inventory`, and `bacon-payment`.
- `bacon-common/`: shared platform modules.
- `docs/`: architecture, requirements, database, and documentation rules.

Within each business domain, keep the existing layer split:
- `*-api`
- `*-interfaces`
- `*-application`
- `*-domain`
- `*-infra`

## Build And Run

Run commands from the repository root unless you intentionally target a module with `-pl`.

- `mvn clean verify`
- `mvn test`
- `mvn checkstyle:check`
- `mvn -pl bacon-app/bacon-mono-boot spring-boot:run`
- `mvn -pl bacon-app/bacon-gateway spring-boot:run`

## Coding Rules

- Use 4-space indentation for Java, XML, and YAML.
- Base package: `com.github.thundax.bacon`
- Class names: `PascalCase`
- Methods and fields: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Module names: `bacon-<scope>` or `bacon-<domain>-<layer>`

Layer responsibilities:
- `interfaces.controller`: external business HTTP endpoints
- `interfaces.provider`: internal provider HTTP endpoints
- `interfaces.facade`: local facade adapter implementations
- `application`: orchestration and use-case logic
- `domain`: business rules and repository contracts
- `infra`: persistence, RPC, cache, MQ, and external integration

## Testing

- Put tests under `src/test/java`, mirroring production packages.
- Use `spring-boot-starter-test`.
- Shared test support belongs in `bacon-common/bacon-common-test`.
- Test class names use `*Test`; broader integration scenarios use `*IT`.
- Add or update tests when behavior changes.

## Commits

Every file modification must be followed by a git commit before ending the task.

Commit message format:
- `Type(domain): 中文说明`

Examples:
- `Feat(boot): 初始化工程`
- `Test(boot): 测试单例`

Each commit should clearly describe the change. Add a body when scope or verification needs explanation.
