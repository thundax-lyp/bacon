# INVENTORY DATABASE DESIGN

## 1. Purpose

本文档定义 `Inventory` 的数据结构与数据库设计。  
目标是让 AI 可直接据此生成表结构、实体、`Mapper`、`Repository` 和查询实现。  
本文档只保留数据库设计所需的稳定信息，不重复业务需求文档中的冗余描述。

## 2. Scope

当前范围覆盖：

- `Inventory`
- `InventoryReservation`
- `InventoryReservationItem`
- `InventoryLedger`
- `InventoryAuditLog`

不覆盖：

- 多仓分配表
- 批次、序列号、采购、调拨相关表

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

## 3.1 Common Field Rules

- 库存主数据表统一包含 `created_at`、`created_by`、`updated_at`、`updated_by`
- 预占表、预占明细表、流水表、审计表不强制引入 `created_by`、`updated_by`
- `Inventory` 当前范围内不引入逻辑删除字段

## 4. Naming Rules

- 表名固定格式使用 `bacon_${domain}_${model}`
- `Inventory` 表名统一使用 `bacon_inventory_` 前缀
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 审计发生时间统一命名为 `occurred_at`

## 5. Enum Storage Rules

- `status`: `ENABLED`、`DISABLED`
- `reservation_status`: `CREATED`、`RESERVED`、`RELEASED`、`DEDUCTED`、`FAILED`
- `ledger_type`: `RESERVE`、`RELEASE`、`DEDUCT`
- `release_reason`: `USER_CANCELLED`、`SYSTEM_CANCELLED`、`PAYMENT_CREATE_FAILED`、`PAYMENT_FAILED`、`TIMEOUT_CLOSED`

## 5.1 Length Rules

- `tenant_id`: `varchar(64)`
- `reservation_no`: `varchar(64)`
- `order_no`: `varchar(64)`
- `warehouse_id`: `varchar(64)`
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

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `sku_id` | `bigint` | N | SKU 主键 |
| `warehouse_id` | `varchar(64)` | N | 仓库标识 |
| `on_hand_quantity` | `int` | N | 现存量 |
| `reserved_quantity` | `int` | N | 预占量 |
| `available_quantity` | `int` | N | 可售量 |
| `status` | `varchar(16)` | N | 库存状态 |
| `created_by` | `bigint` | Y | 创建人用户主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `bigint` | Y | 更新人用户主键 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_sku(tenant_id, sku_id)`
- `idx_tenant_sku(tenant_id, sku_id)`

### 7.2 `bacon_inventory_reservation`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `reservation_no` | `varchar(64)` | N | 预占单号 |
| `order_no` | `varchar(64)` | N | 订单号 |
| `reservation_status` | `varchar(16)` | N | 预占状态 |
| `warehouse_id` | `varchar(64)` | N | 仓库标识 |
| `failure_reason` | `varchar(255)` | Y | 失败原因 |
| `release_reason` | `varchar(64)` | Y | 释放原因 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `released_at` | `datetime(3)` | Y | 释放时间 |
| `deducted_at` | `datetime(3)` | Y | 扣减时间 |

索引与约束：

- `pk(id)`
- `uk_reservation_no(reservation_no)`
- `uk_tenant_order_no(tenant_id, order_no)`

### 7.3 `bacon_inventory_reservation_item`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `reservation_no` | `varchar(64)` | N | 预占单号 |
| `sku_id` | `bigint` | N | SKU 主键 |
| `quantity` | `int` | N | 预占数量 |

索引与约束：

- `pk(id)`
- `uk_reservation_sku(tenant_id, reservation_no, sku_id)`

### 7.4 `bacon_inventory_ledger`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单号 |
| `reservation_no` | `varchar(64)` | N | 预占单号 |
| `sku_id` | `bigint` | N | SKU 主键 |
| `warehouse_id` | `varchar(64)` | N | 仓库标识 |
| `ledger_type` | `varchar(16)` | N | 流水类型 |
| `quantity` | `int` | N | 变更数量 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_order_ledger(tenant_id, order_no, ledger_type)`

### 7.5 `bacon_inventory_audit_log`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_no` | `varchar(64)` | Y | 订单号 |
| `reservation_no` | `varchar(64)` | Y | 预占单号 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人标识 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_order_no(order_no)`
- `idx_reservation_no(reservation_no)`

## 8. Relationship Rules

- `bacon_inventory_reservation_item.reservation_no -> bacon_inventory_reservation.reservation_no`
- `bacon_inventory_ledger.reservation_no -> bacon_inventory_reservation.reservation_no`

固定约束：

- 当前设计不强制数据库外键
- `Inventory` 当前范围固定 `(tenant_id, sku_id)` 唯一
- `warehouse_id` 固定为单仓默认仓标识

## 9. Persistence Rules

- `Inventory` 当前范围必须保证 `(tenant_id, sku_id)` 唯一
- `InventoryReservation` 当前范围必须保证 `(tenant_id, order_no)` 唯一
- `InventoryReservationItem` 当前范围必须保证 `(tenant_id, reservation_no, sku_id)` 唯一
- `available_quantity` 必须始终等于 `on_hand_quantity - reserved_quantity`
- `InventoryStockDTO` 是读模型，不单独建表
- `InventoryReservationDTO` 和 `InventoryReservationResultDTO` 由预占表与明细表组装，不单独建表

## 10. Query Model Rules

- 可售库存查询主表为 `bacon_inventory_inventory`
- 预占单详情查询主表为 `bacon_inventory_reservation + bacon_inventory_reservation_item`
- 库存流水查询主表为 `bacon_inventory_ledger`
- 审计查询主表为 `bacon_inventory_audit_log`

## 11. DDL Generation Notes

- 任何时候不得出现负库存
- 预占、释放、扣减都以 `order_no` 为幂等键
- 预占单和库存流水不做逻辑删除

## 12. Open Items

无
