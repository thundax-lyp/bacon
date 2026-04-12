# Repository Guidelines

## Read Order

- Read `docs/AGENT.md` first.
- For implementation work, read `docs/00-governance/ARCHITECTURE.md`.
- Do not treat root `README.md` as implementation authority.

## Working Rules

- Load the minimum docs needed for the task.
- Prefer the simplest workable solution.
- Do not add abstraction, config, directories, or helper layers without a concrete need.
- Keep the existing Maven multi-module structure and existing domain layering.

## Project Layout

- Root project: Maven multi-module, Java 17, root `pom.xml`
- Business code: `bacon-biz/`
- Shared code: `bacon-common/`
- App assembly and startup: `bacon-app/`
- AI routing docs: `docs/`

Business domains keep this layer split:

- `*-api`
- `*-interfaces`
- `*-application`
- `*-domain`
- `*-infra`

## Code Rules

- Java/XML/YAML use 4-space indentation.
- Base package: `com.github.thundax.bacon`
- Class: `PascalCase`
- Method/field: `camelCase`
- Constant: `UPPER_SNAKE_CASE`

Layer responsibilities:

- `interfaces.controller`: external HTTP entry
- `interfaces.provider`: internal HTTP entry
- `interfaces.facade`: local facade adapter
- `application`: use-case orchestration
- `domain`: business rules and repository contracts
- `infra`: persistence and external integration

## Testing

- Tests live in `src/test/java`.
- Shared test support belongs in `bacon-common/bacon-common-test`.
- Behavior changes require test updates.

## Commits

- Every file modification must be committed before ending the task.
- Commit format: `Type(domain): 中文说明`
- Split unrelated changes into separate commits.
- Commit message must state the concrete capability changed.

Examples:

- `Feat(boot): 初始化工程`
- `Test(order): 补充订单创建测试`
