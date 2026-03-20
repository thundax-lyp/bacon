# ORDER DATABASE DESIGN

## 1. Purpose

本文档定义 `Order` 的数据结构与数据库设计。  
目标是让 AI 可直接据此生成表结构、实体、`Mapper`、`Repository` 和查询实现。  
本文档只保留数据库设计所需的稳定信息，不重复业务需求文档中的冗余描述。

## 2. Scope

当前范围覆盖：

- `Order`
- `OrderItem`
- `OrderPaymentSnapshot`
- `OrderInventorySnapshot`
- `OrderAuditLog`

不覆盖：

- `OrderAmount`
- `OrderSnapshot`
- 库存主数据与支付主数据

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则固定使用 `utf8mb4_0900_ai_ci`
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 数量字段统一使用 `int`
- 金额字段统一使用 `decimal(18,2)`
- 枚举字段统一使用 `varchar`
- 所有订单数据必须包含 `tenant_id`

## 3.1 Common Field Rules

- 订单主表使用 `created_at`、`updated_at`
- 明细表、快照表、审计表不强制引入 `created_by`、`updated_by`
- `Order` 当前范围内不引入逻辑删除字段

## 4. Naming Rules

- 表名固定格式使用 `bacon_${domain}_${model}`
- `Order` 表名统一使用 `bacon_order_` 前缀
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 审计发生时间统一命名为 `occurred_at`

## 5. Enum Storage Rules

- `order_status`: `CREATED`、`RESERVING_STOCK`、`PENDING_PAYMENT`、`PAID`、`CANCELLED`、`CLOSED`
- `pay_status`: `UNPAID`、`PAYING`、`PAID`、`FAILED`、`CLOSED`
- `inventory_status`: `UNRESERVED`、`RESERVING`、`RESERVED`、`RELEASED`、`DEDUCTED`、`FAILED`
- `cancel_reason`: `USER_CANCELLED`、`SYSTEM_CANCELLED`
- `close_reason`: `INVENTORY_RESERVE_FAILED`、`PAYMENT_CREATE_FAILED`、`PAYMENT_FAILED`、`TIMEOUT_CLOSED`

## 5.1 Length Rules

- `tenant_id`: `varchar(64)`
- `order_no`: `varchar(64)`
- `payment_no`: `varchar(64)`
- `reservation_no`: `varchar(64)`
- `currency_code`: `varchar(16)`
- `channel_code`: `varchar(32)`
- `warehouse_id`: `varchar(64)`
- `sku_name`: `varchar(128)`
- `remark`: `varchar(255)`
- `failure_reason`: `varchar(255)`
- `channel_status`: `varchar(64)`
- `action_type`: `varchar(64)`
- `operator_type`: `varchar(32)`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `Order` | `bacon_order_order` |
| `OrderItem` | `bacon_order_item` |
| `OrderPaymentSnapshot` | `bacon_order_payment_snapshot` |
| `OrderInventorySnapshot` | `bacon_order_inventory_snapshot` |
| `OrderAuditLog` | `bacon_order_audit_log` |

## 7. Table Design

### 7.1 `bacon_order_order`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `order_status` | `varchar(32)` | N | 订单状态 |
| `pay_status` | `varchar(16)` | N | 支付状态 |
| `inventory_status` | `varchar(16)` | N | 库存状态 |
| `currency_code` | `varchar(16)` | N | 币种编码 |
| `total_amount` | `decimal(18,2)` | N | 订单总金额 |
| `payable_amount` | `decimal(18,2)` | N | 应付金额 |
| `remark` | `varchar(255)` | Y | 备注 |
| `cancel_reason` | `varchar(32)` | Y | 取消原因 |
| `close_reason` | `varchar(64)` | Y | 关闭原因 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |
| `expired_at` | `datetime(3)` | N | 过期时间 |
| `paid_at` | `datetime(3)` | Y | 支付时间 |
| `closed_at` | `datetime(3)` | Y | 关闭时间 |

索引与约束：

- `pk(id)`
- `uk_order_no(order_no)`
- `idx_tenant_user_created(tenant_id, user_id, created_at)`
- `idx_tenant_order_status_created(tenant_id, order_status, created_at)`
- `idx_tenant_expired_status(tenant_id, expired_at, order_status)`

### 7.2 `bacon_order_item`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_id` | `bigint` | N | 订单主键 |
| `sku_id` | `bigint` | N | SKU 主键 |
| `sku_name` | `varchar(128)` | N | SKU 名称快照 |
| `quantity` | `int` | N | 数量 |
| `sale_price` | `decimal(18,2)` | N | 销售价 |
| `line_amount` | `decimal(18,2)` | N | 行金额 |

索引与约束：

- `pk(id)`
- `idx_tenant_order(tenant_id, order_id)`

### 7.3 `bacon_order_payment_snapshot`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_id` | `bigint` | N | 订单主键 |
| `payment_no` | `varchar(64)` | N | 支付单号 |
| `channel_code` | `varchar(32)` | N | 渠道编码 |
| `pay_status` | `varchar(16)` | N | 支付状态 |
| `paid_amount` | `decimal(18,2)` | Y | 已付金额 |
| `paid_time` | `datetime(3)` | Y | 已付时间 |
| `failure_reason` | `varchar(255)` | Y | 失败原因 |
| `channel_status` | `varchar(64)` | Y | 渠道状态摘要 |
| `updated_at` | `datetime(3)` | N | 快照更新时间 |

索引与约束：

- `pk(id)`
- `uk_order_id(order_id)`
- `uk_payment_no(payment_no)`

### 7.4 `bacon_order_inventory_snapshot`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_id` | `bigint` | N | 订单主键 |
| `reservation_no` | `varchar(64)` | N | 预占单号 |
| `inventory_status` | `varchar(16)` | N | 库存状态 |
| `warehouse_id` | `varchar(64)` | Y | 仓库标识 |
| `failure_reason` | `varchar(255)` | Y | 失败原因 |
| `updated_at` | `datetime(3)` | N | 快照更新时间 |

索引与约束：

- `pk(id)`
- `uk_order_id(order_id)`
- `uk_reservation_no(reservation_no)`

### 7.5 `bacon_order_audit_log`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `before_status` | `varchar(32)` | Y | 变更前订单状态 |
| `after_status` | `varchar(32)` | Y | 变更后订单状态 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人标识 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_order_no(order_no)`

## 8. Relationship Rules

- `bacon_order_item.order_id -> bacon_order_order.id`
- `bacon_order_payment_snapshot.order_id -> bacon_order_order.id`
- `bacon_order_inventory_snapshot.order_id -> bacon_order_order.id`

固定约束：

- 当前设计不强制数据库外键
- `payment_no` 和 `reservation_no` 是跨域快照业务键，不替代对端主表

## 9. Persistence Rules

- `Order.order_no` 全局唯一
- `OrderPaymentSnapshot.payment_no` 全局唯一
- `OrderInventorySnapshot.reservation_no` 全局唯一
- `OrderAmount` 是值对象，不单独建表
- `OrderSnapshot` 是详情聚合视图，不单独建表
- 订单和订单明细不支持物理删除，也不增加逻辑删除字段

## 10. Query Model Rules

- 订单详情查询主表为 `bacon_order_order + bacon_order_item + bacon_order_payment_snapshot + bacon_order_inventory_snapshot`
- 订单分页查询主表为 `bacon_order_order`
- 审计查询主表为 `bacon_order_audit_log`

## 11. DDL Generation Notes

- 订单金额字段固定两位小数
- `Order.total_amount` = `sum(OrderItem.line_amount)`
- `Order.payable_amount` 当前固定等于 `Order.total_amount`
- 快照表为订单侧只读快照，不承载对端业务主数据

## 12. Open Items

无
