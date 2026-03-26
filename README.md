# Bacon

> Enterprise-grade RBAC and business platform based on Spring Cloud Alibaba 2025, Spring Boot 3, and OAuth2.

## TL;DR

Bacon is a multi-module Java 17 backend project that supports both:

- `mono` deployment (single boot app)
- `micro` deployment (domain starters)

It provides a unified architecture for:

- `Auth` (identity, session, OAuth2)
- `UPMS` (RBAC, org, menu, resource, data permission)
- `Order`
- `Inventory`
- `Payment`

---

## AI Quick Context

This section is intentionally structured for AI agents and tooling.

```yaml
project:
  name: bacon
  language: Java
  build: Maven
  java: 17
  spring_boot: 3.5.x
  spring_cloud: 2025.x
  spring_cloud_alibaba: 2025.x

runtime_modes:
  - mono
  - micro

architecture:
  layers:
    - interfaces
    - application
    - domain
    - infra
  dependency_direction: interfaces -> application -> domain <- infra
  cross_domain_rule: depend on facade contracts only

main_dirs:
  - bacon-app
  - bacon-biz
  - bacon-common
  - docs
  - deploy

entrypoints:
  mono: bacon-app/bacon-mono-boot
  micro:
    - bacon-app/bacon-auth-starter
    - bacon-app/bacon-upms-starter
    - bacon-app/bacon-order-starter
    - bacon-app/bacon-inventory-starter
    - bacon-app/bacon-payment-starter
```

---

## Repository Layout

```text
bacon
├── bacon-app/      # Bootstrapping and runtime assembly
├── bacon-biz/      # Business domains (auth/upms/order/inventory/payment)
├── bacon-common/   # Shared platform capabilities
├── docs/           # Architecture, requirements, DB design, doc standards
└── deploy/         # Deployment scripts and environment samples
```

### Core Shared Modules

- `bacon-common-core`: exceptions, context, common utilities
- `bacon-common-web`: response envelope and global exception handling
- `bacon-common-security`: security context and permission abstraction
- `bacon-common-mybatis`: MyBatis/MyBatis-Plus conventions
- `bacon-common-feign`: RPC client foundation
- `bacon-common-mq`: MQ abstraction

---

## Domain Map

- `Auth`: authentication, token/session lifecycle, OAuth2
- `UPMS`: RBAC, tenant/user/org/menu/resource/data-scope
- `Order`: order lifecycle and cross-domain orchestration
- `Inventory`: stock query, reservation/release/deduction, audit outbox
- `Payment`: payment order, callback, close, reconciliation baseline

---

## Getting Started

### 1) Read Docs in Correct Order

1. [docs/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)
2. [docs/README.md](/Volumes/storage/workspace/bacon/docs/README.md)
3. target domain `*-REQUIREMENTS.md`

### 2) Build and Test

```bash
mvn clean verify
mvn test
mvn checkstyle:check
```

### 3) Run

Mono:

```bash
mvn -pl bacon-app/bacon-mono-boot spring-boot:run
```

Micro (example):

```bash
mvn -pl bacon-app/bacon-order-starter spring-boot:run
```

---

## Testing Notes

Inventory focused checks:

```bash
mvn -q -pl bacon-biz/bacon-inventory/bacon-inventory-infra -am test -Dtest=InMemoryInventoryRepositorySupportTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl bacon-biz/bacon-inventory/bacon-inventory-interfaces -am test -Dtest=InventoryControllerContractTest,InventoryProviderControllerContractTest -Dsurefire.failIfNoSpecifiedTests=false
```

---

## JDK and Toolchain

- Daily development may use `JDK 19`
- Maven compile/test must use `JDK 17`
- Toolchain is enforced by root `pom.xml`

Setup:

1. copy `.mvn/toolchains.example.xml` to `~/.m2/toolchains.xml`
2. update `jdkHome` to your local JDK 17 path

---

## Engineering Rules

- Keep layer boundaries strict
- Do not bypass facade contracts for cross-domain calls
- Keep docs and schema in sync with code changes
- Prefer simple, explicit, production-safe designs

---

## Documentation Index

- [docs/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)
- [docs/README.md](/Volumes/storage/workspace/bacon/docs/README.md)
- [docs/AUTH-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/AUTH-REQUIREMENTS.md)
- [docs/UPMS-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/UPMS-REQUIREMENTS.md)
- [docs/ORDER-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/ORDER-REQUIREMENTS.md)
- [docs/INVENTORY-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/INVENTORY-REQUIREMENTS.md)
- [docs/PAYMENT-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/PAYMENT-REQUIREMENTS.md)

## License

Apache License 2.0. See [LICENSE](/Volumes/storage/workspace/bacon/LICENSE).
