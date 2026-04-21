# APPLICATION-REFACTOR

## Purpose

This document captures the proven operating pattern for application-service refactoring in this repository.
Use it as the default playbook for DDD-oriented service evolution.

## Scope

Applies when refactoring `*ApplicationService`, especially to:
- separate command/query responsibilities,
- replace multi-arg service methods with command/query objects,
- standardize pagination contract,
- simplify redundant method names in bounded context,
- standardize interfaces-layer assembly.

## Operating Rules (Must Follow)

1. Read minimum required docs first:
- `docs/AGENT.md`
- `docs/00-governance/ARCHITECTURE.md`

2. Keep existing module/layer structure unchanged:
- `interfaces -> application -> domain -> infra`
- no extra abstraction or helper layers without concrete need.

3. Apply small, safe, incremental changes.
- each step must compile before next step.

4. Never mix unrelated changes.

## Step-by-Step Workflow

### Step 1: Split service responsibilities
- If one service mixes write/read use-cases, split into:
  - `XxxCommandApplicationService`
  - `XxxQueryApplicationService`
- Keep business behavior unchanged while splitting.

### Step 2: Build API mapping checklist (mandatory)
- Before editing, create explicit mapping list:
  - old API -> new API
- Enumerate call sites by category:
  - controller
  - provider
  - facade/local adapter
  - tests
- Refactor with checklist-based burn-down; each category must be checked off.
- Do not end task with unmigrated legacy signatures.

### Step 3: Stabilize application contracts
- Convert service signatures from:
  - `xxx(a, b, c)`
- To:
  - `xxx(XxxCommand)` for write actions
  - `xxx(XxxQuery)` for read/filter actions
- Command/Query objects must live in `application` layer.

### Step 4: Standardize paging
- For paged query methods, use:
  - `page(XxxPageQuery)`
- Let `XxxPageQuery` extend `common-core PageQuery` to centralize page normalization.

### Step 5: Remove redundant method naming
Inside domain-specific service classes, remove repeated context words:
- In `TenantCommandApplicationService`:
  - `createTenant` -> `create`
  - `updateTenant` -> `update`
  - `updateTenantStatus` -> `updateStatus`
- In `TenantQueryApplicationService`:
  - `getTenantByTenantId` -> `getById`

### Step 6: Standardize interfaces assembly
- Add `interfaces.assembler.XxxAssembler` when request/response assembly appears in multiple entry points.
- Responsibilities:
  - `XxxRequest/FacadeRequest -> XxxCommand/XxxQuery`
  - `XxxDTO -> XxxResponse/FacadeResponse`
- Keep assembly-only behavior in interfaces assembler.
- Do not place business rules in interfaces assembler.
- Do not overlap responsibilities with application assembler (domain <-> DTO).

### Step 7: Keep interfaces thin
- `interfaces.controller/provider/facade` only does:
  - validation,
  - delegation,
  - response return.
- Do not pass HTTP request DTOs directly into application core methods.

### Step 8: Update all call sites immediately
- Update controllers, providers, facades, and tests.
- No legacy signature should remain after refactor.

### Step 9: Validate after each meaningful step
Recommended fast check:

```bash
mvn -q -pl bacon-biz/bacon-upms/bacon-upms-application,bacon-biz/bacon-upms/bacon-upms-interfaces -am -DskipTests compile
```

Optionally add test compile:

```bash
mvn -q -pl bacon-biz/bacon-upms/bacon-upms-application,bacon-biz/bacon-upms/bacon-upms-interfaces -am -DskipTests test-compile
```

### Step 10: Commit strategy
- Use one small commit for one coherent capability change.
- Commit message format:
  - `Type(domain): 中文说明`

## DDD Review Checklist

Before declaring “stable”, verify:

- Application service API exposes stable command/query contracts.
- Layering is respected (`interfaces` adapts, `application` orchestrates, `domain` owns rules).
- Exception semantics are explicit (`BadRequestException`, `ConflictException`, `NotFoundException`, etc.).
- Pagination normalization is not duplicated across services.
- Naming is concise and context-aware.
- Interface assembly logic is centralized and not duplicated across controllers/providers/facades.

## Lifecycle For This Record

- This file can be committed as process trace for the current refactor phase.
- After the phase is complete, it can be deleted in a later commit while keeping full Git history.
