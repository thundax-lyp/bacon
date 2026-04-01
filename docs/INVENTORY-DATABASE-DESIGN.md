# INVENTORY DATABASE DESIGN

## 1. Purpose

本文档定义 `Inventory` 业务域的数据库设计。  
本文档可直接用于生成 `DDL`、`MyBatis-Plus DO`、`Mapper`、按职责拆分的 `Repository`、库存查询和库存命令持久化实现。  
本文档只定义 `Inventory` 自有的持久化对象、字段、索引、流水规则和查询模型，不重复业务需求文档中的流程描述。  
本文档必须遵守 [DATABASE-RULES.md](./DATABASE-RULES.md)。如与工程级数据库规范冲突，以 [DATABASE-RULES.md](./DATABASE-RULES.md) 为准。

## 2. Scope

本文档定义以下持久化对象：
- `Inventory`
- `InventoryReservation`
- `InventoryReservationItem`
- `InventoryLedger`
- `InventoryAuditLog`
- `InventoryAuditOutbox`
- `InventoryAuditDeadLetter`
- `InventoryAuditReplayTask`
- `InventoryAuditReplayTaskItem`

本文档不定义以下持久化对象：
- 多仓分配表
- 批次表
- 序列号表
- 采购相关表
- 调拨相关表

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库实例可用的 `utf8mb4` 排序规则（推荐 `utf8mb4_unicode_ci`）
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 数量字段统一使用 `int`
- 枚举字段统一使用 `varchar`
- 固定单仓模型
- 库存主数据表统一包含 `created_at`、`created_by`、`updated_at`、`updated_by`
- 预占表、预占明细表、流水表、审计表不强制增加 `created_by`、`updated_by`
- 不使用逻辑删除字段

## 4. Naming Rules

- 表名前缀固定使用 `bacon_inventory_`
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 审计发生时间统一命名为 `occurred_at`

## 5. Enum Storage Rules

### 5.1 Fixed Enums

- `status`: `ENABLED`、`DISABLED`
- `reservation_status`: `CREATED`、`RESERVED`、`RELEASED`、`DEDUCTED`、`FAILED`
- `ledger_type`: `RESERVE`、`RELEASE`、`DEDUCT`
- `release_reason`: `USER_CANCELLED`、`SYSTEM_CANCELLED`、`PAYMENT_CREATE_FAILED`、`PAYMENT_FAILED`、`TIMEOUT_CLOSED`

### 5.2 Fixed Length Rules

- `reservation_no`: `varchar(64)`
- `order_no`: `varchar(64)`
- `failure_reason`: `varchar(255)`
- `release_reason`: `varchar(255)`
- `action_type`: `varchar(64)`
- `operator_type`: `varchar(32)`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `Inventory` | `bacon_inventory_inventory` |
| `InventoryReservation` | `bacon_inventory_reservation` |
| `InventoryReservationItem` | `bacon_inventory_reservation_item` |
| `InventoryLedger` | `bacon_inventory_ledger` |
| `InventoryAuditLog` | `bacon_inventory_audit_log` |
| `InventoryAuditOutbox` | `bacon_inventory_audit_outbox` |
| `InventoryAuditDeadLetter` | `bacon_inventory_audit_dead_letter` |
| `InventoryAuditReplayTask` | `bacon_inventory_audit_replay_task` |
| `InventoryAuditReplayTaskItem` | `bacon_inventory_audit_replay_task_item` |

## 7. Table Design

### 7.1 `bacon_inventory_inventory`

表类型：`Master Table`

用途：

- 持久化库存主数据
- 承载现存量、预占量、可售量和库存状态

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `sku_id` | `bigint` | N | SKU 主键 |
| `warehouse_id` | `bigint` | N | 仓库标识，固定为默认仓 |
| `on_hand_quantity` | `int` | N | 现存量 |
| `reserved_quantity` | `int` | N | 预占量 |
| `available_quantity` | `int` | N | 可售量 |
| `status` | `varchar(32)` | N | 库存状态，取值见 `status` |
| `version` | `bigint` | N | 乐观锁版本号 |
| `created_by` | `bigint` | Y | 创建人用户主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `bigint` | Y | 更新人用户主键 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_sku(tenant_id, sku_id)`
- `idx_tenant_status(tenant_id, status)`

### 7.2 `bacon_inventory_reservation`

表类型：`Runtime Table`

用途：

- 持久化订单维度的库存预占单
- 以 `order_no` 作为幂等业务键

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `reservation_no` | `varchar(64)` | N | 预占单号，同租户唯一 |
| `order_no` | `varchar(64)` | N | 订单号，同租户唯一 |
| `reservation_status` | `varchar(32)` | N | 预占状态，取值见 `reservation_status` |
| `warehouse_id` | `bigint` | N | 仓库标识 |
| `failure_reason` | `varchar(255)` | Y | 失败原因 |
| `release_reason` | `varchar(255)` | Y | 释放原因，取值见 `release_reason` |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `released_at` | `datetime(3)` | Y | 释放时间 |
| `deducted_at` | `datetime(3)` | Y | 扣减时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_order(tenant_id, order_no)`
- `uk_tenant_reservation_no(tenant_id, reservation_no)`
- `idx_tenant_status(tenant_id, reservation_status)`

### 7.3 `bacon_inventory_reservation_item`

表类型：`Runtime Table`

用途：

- 持久化库存预占单明细

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `reservation_no` | `varchar(64)` | N | 预占单号，关联 `bacon_inventory_reservation.reservation_no` |
| `sku_id` | `bigint` | N | SKU 主键 |
| `quantity` | `int` | N | 预占数量 |

索引与约束：

- `pk(id)`
- `uk_tenant_reservation_sku(tenant_id, reservation_no, sku_id)`
- `idx_tenant_reservation_no(tenant_id, reservation_no)`

### 7.4 `bacon_inventory_ledger`

表类型：`Ledger Table`

用途：

- 记录库存预占、释放、扣减流水
- 只追加，不更新历史记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单号 |
| `reservation_no` | `varchar(64)` | N | 预占单号 |
| `sku_id` | `bigint` | N | SKU 主键 |
| `warehouse_id` | `bigint` | N | 仓库标识 |
| `ledger_type` | `varchar(16)` | N | 流水类型，取值见 `ledger_type` |
| `quantity` | `int` | N | 变更数量 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_order_ledger(tenant_id, order_no, ledger_type)`
- `idx_tenant_reservation(tenant_id, reservation_no)`

### 7.5 `bacon_inventory_audit_log`

表类型：`Audit Log Table`

用途：

- 记录库存主数据变更、库存预占、释放、扣减相关审计事件
- 只追加，不更新历史记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `order_no` | `varchar(64)` | Y | 订单号 |
| `reservation_no` | `varchar(64)` | Y | 预占单号 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人标识 |
| `occurred_at` | `datetime(3)` | N | 事件发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_order_no(order_no)`
- `idx_reservation_no(reservation_no)`

### 7.6 `bacon_inventory_audit_outbox`

表类型：`Outbox Table`

用途：

- 记录库存审计日志写入失败事件
- 作为审计补偿任务重试数据源

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `order_no` | `varchar(64)` | Y | 订单号 |
| `reservation_no` | `varchar(64)` | Y | 预占单号 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人标识 |
| `occurred_at` | `datetime(3)` | N | 业务事件发生时间 |
| `error_message` | `varchar(512)` | N | 审计写入失败原因 |
| `status` | `varchar(16)` | N | outbox 状态，初始为 `NEW` |
| `retry_count` | `int` | N | 重试次数，初始 `0` |
| `next_retry_at` | `datetime(3)` | Y | 下次可重试时间 |
| `processing_owner` | `varchar(128)` | Y | 当前处理实例标识 |
| `lease_until` | `datetime(3)` | Y | 处理租约到期时间 |
| `claimed_at` | `datetime(3)` | Y | 最近一次抢占时间 |
| `dead_reason` | `varchar(64)` | Y | 进入死信原因 |
| `failed_at` | `datetime(3)` | N | 失败落库时间 |
| `updated_at` | `datetime(3)` | N | 最近状态更新时间 |

索引与约束：

- `pk(id)`
- `idx_status_failed(status, failed_at)`
- `idx_tenant_order(tenant_id, order_no)`
- `idx_status_next_retry(status, next_retry_at)`
- `idx_status_next_retry_lease(status, next_retry_at, lease_until)`
- `idx_processing_owner(processing_owner)`

### 7.7 `bacon_inventory_audit_dead_letter`

表类型：`Dead Letter Table`

用途：

- 记录 outbox 达到最大重试次数后的终态失败事件
- 作为人工补偿和运维排查数据源

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `outbox_id` | `bigint` | N | outbox 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `order_no` | `varchar(64)` | Y | 订单号 |
| `reservation_no` | `varchar(64)` | Y | 预占单号 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人标识 |
| `occurred_at` | `datetime(3)` | N | 业务事件发生时间 |
| `retry_count` | `int` | N | 达到死信时重试次数 |
| `error_message` | `varchar(512)` | N | 最后一次失败原因 |
| `dead_reason` | `varchar(64)` | N | 死信原因 |
| `dead_at` | `datetime(3)` | N | 进入死信时间 |
| `replay_status` | `varchar(16)` | N | 重放状态，`PENDING/RUNNING/SUCCEEDED/FAILED` |
| `replay_count` | `int` | N | 重放次数 |
| `last_replay_at` | `datetime(3)` | Y | 最近一次重放时间 |
| `last_replay_result` | `varchar(16)` | Y | 最近一次重放结果 |
| `last_replay_error` | `varchar(512)` | Y | 最近一次重放失败原因 |
| `replay_key` | `varchar(128)` | Y | 重放幂等键 |
| `replay_operator_type` | `varchar(32)` | Y | 最近一次重放操作人类型 |
| `replay_operator_id` | `bigint` | Y | 最近一次重放操作人标识 |

索引与约束：

- `pk(id)`
- `idx_dead_at(dead_at)`
- `idx_tenant_order(tenant_id, order_no)`
- `idx_outbox_id(outbox_id)`
- `idx_tenant_replay_status_dead(tenant_id, replay_status, dead_at)`
- `uk_replay_key(replay_key)`

### 7.8 `bacon_inventory_audit_replay_task`

表类型：`Async Task Table`

用途：

- 记录死信批量重放任务头
- 提供任务状态、进度和租约信息

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `task_no` | `varchar(64)` | N | 任务号 |
| `status` | `varchar(16)` | N | 任务状态：`PENDING/RUNNING/PAUSED/SUCCEEDED/FAILED/CANCELED` |
| `total_count` | `int` | N | 总条数 |
| `processed_count` | `int` | N | 已处理条数 |
| `success_count` | `int` | N | 成功条数 |
| `failed_count` | `int` | N | 失败条数 |
| `replay_key_prefix` | `varchar(64)` | Y | 任务级重放键前缀 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人标识 |
| `processing_owner` | `varchar(128)` | Y | 当前处理实例 |
| `lease_until` | `datetime(3)` | Y | 处理租约到期时间 |
| `last_error` | `varchar(512)` | Y | 最近错误摘要 |
| `created_at` | `datetime(3)` | N | 任务创建时间 |
| `started_at` | `datetime(3)` | Y | 首次启动时间 |
| `paused_at` | `datetime(3)` | Y | 最近暂停时间 |
| `finished_at` | `datetime(3)` | Y | 完成时间 |
| `updated_at` | `datetime(3)` | N | 最近更新时间 |

索引与约束：

- `pk(id)`
- `uk_task_no(task_no)`
- `idx_tenant_status_created(tenant_id, status, created_at)`
- `idx_status_lease(status, lease_until)`

### 7.9 `bacon_inventory_audit_replay_task_item`

表类型：`Async Task Item Table`

用途：

- 记录批量重放任务的每条死信执行结果
- 支撑进度统计与失败排查

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `task_id` | `bigint` | N | 任务主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `dead_letter_id` | `bigint` | N | 死信主键 |
| `item_status` | `varchar(16)` | N | 项状态：`PENDING/SUCCEEDED/FAILED` |
| `replay_status` | `varchar(16)` | Y | 死信重放状态 |
| `replay_key` | `varchar(128)` | Y | 本条重放幂等键 |
| `result_message` | `varchar(512)` | Y | 执行结果描述 |
| `started_at` | `datetime(3)` | Y | 本条开始时间 |
| `finished_at` | `datetime(3)` | Y | 本条结束时间 |
| `updated_at` | `datetime(3)` | N | 最近更新时间 |

索引与约束：

- `pk(id)`
- `idx_task_status_id(task_id, item_status, id)`
- `idx_tenant_task(tenant_id, task_id)`
- `uk_task_dead_letter(task_id, dead_letter_id)`

## 8. Relationship Rules

- `bacon_inventory_reservation_item.reservation_no` 关联 `bacon_inventory_reservation.reservation_no`
- `bacon_inventory_ledger.reservation_no` 关联 `bacon_inventory_reservation.reservation_no`
- 当前设计不强制数据库外键
- 固定 `(tenant_id, sku_id)` 唯一，`warehouse_id` 固定为单仓默认仓标识

## 9. Persistence Rules

- `Inventory` 必须保证 `(tenant_id, sku_id)` 唯一
- `InventoryReservation` 必须保证 `(tenant_id, order_no)` 唯一
- `InventoryReservation.reservation_no` 必须由 `Inventory` 模块内发号组件生成，发号 provider 通过配置显式选择（`tinyid/leaf/snowflake`）
- 默认发号策略必须为 `strict`：发号失败时库存预占必须直接失败，不得自动降级到本地号段
- `InventoryReservationItem` 必须保证 `(tenant_id, reservation_no, sku_id)` 唯一
- `available_quantity` 必须始终等于 `on_hand_quantity - reserved_quantity`
- 任意时刻不得出现负库存
- `Inventory.version` 用于乐观锁控制，库存写入必须带版本条件更新
- 正式持久化实现优先使用 `MyBatis-Plus BaseMapper + DO`
- 仓储模式必须显式配置为 `strict` 或 `memory`；默认 `strict`
- `strict` 模式下，未装配可用持久化仓储（如 `DataSource` / `SqlSessionFactory`）必须启动失败（fail-fast）
- `memory` 模式仅允许开发、测试或演练场景显式启用，不得作为生产默认，也不得在运行时自动从 `strict` 切换
- 预占、释放、扣减都以 `order_no` 为幂等键
- `Inventory`、`InventoryReservation`、`InventoryReservationItem` 的主键由持久化层生成，应用层不得自行发号
- `releaseReservedStock` 和 `deductReservedStock` 只允许基于已存在预占单执行语义判断
- 库存数量变更必须显式更新 `bacon_inventory_inventory`，不得依赖运行时对象引用副作用
- `Inventory`、`InventoryReservation`、`InventoryReservationItem` 的命令写入应运行在同一事务中
- 并发冲突重试的退避等待必须在事务外执行，每次重试应开启新的短事务，避免长事务持锁
- `InventoryAuditLog` 优先在主事务提交后以 best effort 方式记录，失败不回滚主业务
- `InventoryAuditLog` 写入失败时必须落库 `bacon_inventory_audit_outbox`
- 审计相关表（`bacon_inventory_audit_log`、`bacon_inventory_audit_outbox`、`bacon_inventory_audit_dead_letter`）的 `action_type` 必须使用统一动作码：`RESERVE`、`RESERVE_FAILED`、`RELEASE`、`DEDUCT`、`AUDIT_REPLAY_SUCCEEDED`、`AUDIT_REPLAY_FAILED`
- `InventoryAuditOutbox` 重试成功后应删除；达到最大重试次数后状态置为 `DEAD`
- `InventoryAuditOutbox` 在多实例下必须先抢占（claim）再处理：仅允许 `NEW/RETRYING` 且 `next_retry_at <= now` 的记录转为 `PROCESSING`
- claim 后必须写入 `processing_owner` 与 `lease_until`，并使用 owner + 状态的条件更新/删除（CAS）完成重试结果提交
- 超过 `lease_until` 的 `PROCESSING` 记录必须可被回收并重新进入 `RETRYING`
- `InventoryAuditOutbox` 进入 `DEAD` 后必须写入 `bacon_inventory_audit_dead_letter`
- `InventoryAuditDeadLetter` 必须支持运营分页查询和按 `replay_status` 过滤
- `InventoryAuditDeadLetter` 重放必须写入幂等 `replay_key`，并更新重放状态与追踪字段
- `InventoryAuditDeadLetter` 在 claim 成功后的重放链路必须在独立事务中完成：原始审计重写、重放状态迁移、重放追踪日志写入需原子提交
- 若上述事务失败，必须通过补偿事务将死信记录置为 `FAILED`，并写入失败追踪日志与可观测告警指标
- `InventoryAuditReplayTask` 必须通过租约抢占执行，避免多实例重复推进同一任务
- `InventoryAuditReplayTaskItem` 必须按 `task_id + item_status + id` 顺序分页拉取，禁止全量加载大批次
- 任务暂停时必须释放 `processing_owner` 与 `lease_until`；恢复时只能从 `PAUSED` 回到 `PENDING`
- `InventoryStockDTO` 是读模型，不单独建表
- `InventoryReservationDTO` 和 `InventoryReservationResultDTO` 由预占表和预占明细表组装，不单独建表
- 预占单和库存流水不做逻辑删除
- `id` 是数据库主键；`reservation_no` 是业务单号；二者不得混用

## 10. Query Model Rules

- 可售库存查询主表固定为 `bacon_inventory_inventory`
- 预占单详情查询主表固定为 `bacon_inventory_reservation + bacon_inventory_reservation_item`
- 库存流水查询主表固定为 `bacon_inventory_ledger`
- 审计查询主表固定为 `bacon_inventory_audit_log`
- 审计补偿查询主表固定为 `bacon_inventory_audit_outbox`
- 死信查询主表固定为 `bacon_inventory_audit_dead_letter`
- 重放任务查询主表固定为 `bacon_inventory_audit_replay_task + bacon_inventory_audit_replay_task_item`
- 库存分页查询必须由数据库分页实现，不得在应用层做全量拉取后再分页
