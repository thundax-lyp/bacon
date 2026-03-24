# INVENTORY DATABASE DESIGN

## 1. Purpose

本文档定义 `Inventory` 业务域的数据库设计。  
目标是让 AI 和工程师可直接据此生成 `DDL`、`MyBatis-Plus DO`、`Mapper`、按职责拆分的 `Repository`、库存查询和库存命令持久化实现。  
本文档只定义 `Inventory` 自有的持久化对象、字段、索引、流水规则和查询模型，不重复业务需求文档中的流程描述。  
本文档必须遵守 [DATABASE-RULES.md](./DATABASE-RULES.md)。如与工程级数据库规范冲突，以 [DATABASE-RULES.md](./DATABASE-RULES.md) 为准。

## 2. Scope

当前范围覆盖以下持久化对象：

- `Inventory`
- `InventoryReservation`
- `InventoryReservationItem`
- `InventoryLedger`
- `InventoryAuditLog`

当前范围不建表的对象：

- 多仓分配表
- 批次表
- 序列号表
- 采购相关表
- 调拨相关表

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则固定使用 `utf8mb4_0900_ai_ci`
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 数量字段统一使用 `int`
- 枚举字段统一使用 `varchar`
- 当前范围固定单仓模型
- 库存主数据表统一包含 `created_at`、`created_by`、`updated_at`、`updated_by`
- 预占表、预占明细表、流水表、审计表不强制增加 `created_by`、`updated_by`
- 当前范围不使用逻辑删除字段

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
- `release_reason`: `varchar(64)`
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
| `warehouse_id` | `bigint` | N | 仓库标识，当前固定为默认仓 |
| `on_hand_quantity` | `int` | N | 现存量 |
| `reserved_quantity` | `int` | N | 预占量 |
| `available_quantity` | `int` | N | 可售量 |
| `status` | `varchar(16)` | N | 库存状态，取值见 `status` |
| `version` | `bigint` | N | 乐观锁版本号 |
| `created_by` | `bigint` | Y | 创建人用户主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `bigint` | Y | 更新人用户主键 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_sku(tenant_id, sku_id)`
- `idx_tenant_sku(tenant_id, sku_id)`

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
| `reservation_no` | `varchar(64)` | N | 预占单号，全局唯一 |
| `order_no` | `varchar(64)` | N | 订单号，同租户唯一 |
| `reservation_status` | `varchar(16)` | N | 预占状态，取值见 `reservation_status` |
| `warehouse_id` | `bigint` | N | 仓库标识 |
| `failure_reason` | `varchar(255)` | Y | 失败原因 |
| `release_reason` | `varchar(64)` | Y | 释放原因，取值见 `release_reason` |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `released_at` | `datetime(3)` | Y | 释放时间 |
| `deducted_at` | `datetime(3)` | Y | 扣减时间 |

索引与约束：

- `pk(id)`
- `uk_reservation_no(reservation_no)`
- `uk_tenant_order_no(tenant_id, order_no)`

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
- `uk_reservation_sku(tenant_id, reservation_no, sku_id)`

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

## 8. Relationship Rules

- `bacon_inventory_reservation_item.reservation_no` 关联 `bacon_inventory_reservation.reservation_no`
- `bacon_inventory_ledger.reservation_no` 关联 `bacon_inventory_reservation.reservation_no`
- 当前设计不强制数据库外键
- 当前范围固定 `(tenant_id, sku_id)` 唯一，`warehouse_id` 固定为单仓默认仓标识

## 9. Persistence Rules

- `Inventory` 必须保证 `(tenant_id, sku_id)` 唯一
- `InventoryReservation` 必须保证 `(tenant_id, order_no)` 唯一
- `InventoryReservation.reservation_no` 固定由 `tinyid-client` 在 `Inventory` 模块内生成，并使用本地缓存号段模式
- `InventoryReservationItem` 必须保证 `(tenant_id, reservation_no, sku_id)` 唯一
- `available_quantity` 必须始终等于 `on_hand_quantity - reserved_quantity`
- 任意时刻不得出现负库存
- `Inventory.version` 用于乐观锁控制，库存写入必须带版本条件更新
- 正式持久化实现优先使用 `MyBatis-Plus BaseMapper + DO`
- 在未装配 `DataSource` / `SqlSessionFactory` 的启动或测试场景下，可退回内存 `Repository` 实现
- 预占、释放、扣减都以 `order_no` 为幂等键
- `Inventory`、`InventoryReservation`、`InventoryReservationItem` 的主键由持久化层生成，应用层不得自行发号
- `releaseReservedStock` 和 `deductReservedStock` 只允许基于已存在预占单执行语义判断
- 库存数量变更必须显式更新 `bacon_inventory_inventory`，不得依赖运行时对象引用副作用
- `Inventory`、`InventoryReservation`、`InventoryReservationItem` 的命令写入应运行在同一事务中
- `InventoryAuditLog` 优先在主事务提交后以 best effort 方式记录，失败只告警不回滚主业务
- `InventoryStockDTO` 是读模型，不单独建表
- `InventoryReservationDTO` 和 `InventoryReservationResultDTO` 由预占表和预占明细表组装，不单独建表
- 预占单和库存流水不做逻辑删除
- `id` 是数据库主键；`reservation_no` 是业务单号；二者不得混用

## 10. Query Model Rules

- 可售库存查询主表固定为 `bacon_inventory_inventory`
- 预占单详情查询主表固定为 `bacon_inventory_reservation + bacon_inventory_reservation_item`
- 库存流水查询主表固定为 `bacon_inventory_ledger`
- 审计查询主表固定为 `bacon_inventory_audit_log`
- 库存分页查询必须由数据库分页实现，不得在应用层做全量拉取后再分页

## 11. Open Items

无
