# ORDER DATABASE DESIGN

## 1. Purpose

本文档定义 `Order` 业务域的数据库设计。  
本文档可直接用于生成 `DDL`、`DataObject`、`Mapper`、`Repository`、分页查询和详情查询实现。  
本文档只定义 `Order` 自有的持久化对象、字段、索引、快照边界和查询模型，不重复业务需求文档中的流程描述。  
本文档必须遵守 [DATABASE-RULES.md](../00-governance/DATABASE-RULES.md)。如与工程级数据库规范冲突，以 [DATABASE-RULES.md](../00-governance/DATABASE-RULES.md) 为准。

## 2. Scope

本文档定义以下持久化对象：
- `Order`
- `OrderItem`
- `OrderPaymentSnapshot`
- `OrderInventorySnapshot`
- `OrderAuditLog`
- `OrderIdempotencyRecord`

本文档不定义以下持久化对象：
- `OrderAmount`
- `OrderSnapshot`
- 库存主数据
- 支付主数据

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库实例可用的 `utf8mb4` 排序规则（推荐 `utf8mb4_unicode_ci`）
- 时间字段统一使用 `datetime(3)`
- 主键字段默认使用 `bigint`
- `bacon_order_order.id` 显式使用 `varchar(64)`，直接承载文本型 `OrderId`
- 数量字段统一使用 `int`
- 金额字段统一使用 `decimal(18,2)`
- 枚举字段统一使用 `varchar`
- 所有订单数据必须包含 `tenant_id`
- 订单主表使用 `created_at`、`updated_at`
- 明细表、快照表、审计表不强制增加 `created_by`、`updated_by`
- 不使用逻辑删除字段

## 4. Naming Rules

- 表名前缀固定使用 `bacon_order_`
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 审计发生时间统一命名为 `occurred_at`

## 5. Enum Storage Rules

### 5.1 Fixed Enums

- `order_status`: `CREATED`、`RESERVING_STOCK`、`PENDING_PAYMENT`、`PAID`、`CANCELLED`、`CLOSED`
- `pay_status`: `UNPAID`、`PAYING`、`PAID`、`FAILED`、`CLOSED`
- `inventory_status`: `UNRESERVED`、`RESERVING`、`RESERVED`、`RELEASED`、`DEDUCTED`、`FAILED`
- `cancel_reason`: `USER_CANCELLED`、`SYSTEM_CANCELLED`
- `close_reason`: `INVENTORY_RESERVE_FAILED`、`PAYMENT_CREATE_FAILED`、`PAYMENT_FAILED`、`TIMEOUT_CLOSED`
- `action_type`: `ORDER_CREATE`、`ORDER_CANCEL`、`ORDER_MARK_PAID`、`ORDER_MARK_PAYMENT_FAILED`、`ORDER_CLOSE_EXPIRED`、`OUTBOX_RESERVE_FAILED`、`OUTBOX_RESERVE_OK`、`OUTBOX_CREATE_PAYMENT_FAILED`、`OUTBOX_CREATE_PAYMENT_OK`、`OUTBOX_RELEASE`
- `operator_type`: `SYSTEM`、`USER`、`ADMIN`

### 5.2 Fixed Length Rules

- `order_no`: `varchar(64)`
- `payment_no`: `varchar(64)`
- `reservation_no`: `varchar(64)`
- `event_code`: `varchar(64)`
- `currency_code`: `varchar(16)`
- `channel_code`: `varchar(32)`
- `sku_name`: `varchar(128)`
- `image_url`: `varchar(512)`
- `remark`: `varchar(255)`
- `failure_reason`: `varchar(255)`
- `channel_status`: `varchar(64)`
- `action_type`: `varchar(64)`
- `operator_type`: `varchar(32)`
- `operator_id`: `varchar(64)`
- `warehouse_no`: `varchar(64)`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `Order` | `bacon_order_order` |
| `OrderItem` | `bacon_order_item` |
| `OrderPaymentSnapshot` | `bacon_order_payment_snapshot` |
| `OrderInventorySnapshot` | `bacon_order_inventory_snapshot` |
| `OrderAuditLog` | `bacon_order_audit_log` |
| `OrderOutboxEvent` | `bacon_order_outbox` |
| `OrderOutboxDeadLetter` | `bacon_order_dead_letter` |
| `OrderIdempotencyRecord` | `bacon_order_idempotency_record` |

## 7. Table Design

### 7.1 `bacon_order_order`

表类型：`Runtime Table`

用途：

- 持久化订单主聚合
- 承载订单状态、支付状态、库存状态和金额快照

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `varchar(64)` | N | 雪花主键，由应用侧发号写入 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键，全局唯一 |
| `user_id` | `varchar(64)` | N | 下单用户标识 |
| `order_status` | `varchar(32)` | N | 订单状态，取值见 `order_status` |
| `pay_status` | `varchar(16)` | N | 支付状态，取值见 `pay_status` |
| `inventory_status` | `varchar(16)` | N | 库存状态，取值见 `inventory_status` |
| `currency_code` | `varchar(16)` | N | 币种编码 |
| `total_amount` | `decimal(18,2)` | N | 订单总金额 |
| `payable_amount` | `decimal(18,2)` | N | 应付金额 |
| `remark` | `varchar(255)` | Y | 备注 |
| `cancel_reason` | `varchar(32)` | Y | 取消原因，取值见 `cancel_reason` |
| `close_reason` | `varchar(64)` | Y | 关闭原因，取值见 `close_reason` |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |
| `expired_at` | `datetime(3)` | N | 订单过期时间 |
| `paid_at` | `datetime(3)` | Y | 支付完成时间 |
| `closed_at` | `datetime(3)` | Y | 订单关闭时间 |

索引与约束：

- `pk(id)`
- `uk_order_no(order_no)`
- `idx_tenant_user_created(tenant_id, user_id, created_at)`
- `idx_tenant_order_status_created(tenant_id, order_status, created_at)`
- `idx_tenant_expired_status(tenant_id, expired_at, order_status)`

### 7.2 `bacon_order_item`

表类型：`Runtime Table`

用途：

- 持久化订单明细
- 保存下单时的商品名称、图片和单价快照

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 雪花主键，由应用侧发号写入 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_id` | `varchar(64)` | N | 订单主键，关联 `bacon_order_order.id` |
| `sku_id` | `varchar(64)` | N | SKU 主键文本值 |
| `sku_name` | `varchar(128)` | N | SKU 名称快照 |
| `image_url` | `varchar(512)` | Y | 商品图片地址快照 |
| `quantity` | `int` | N | 下单数量 |
| `sale_price` | `decimal(18,2)` | N | 销售价 |
| `line_amount` | `decimal(18,2)` | N | 行金额 |

索引与约束：

- `pk(id)`
- `idx_tenant_order(tenant_id, order_id)`

### 7.3 `bacon_order_payment_snapshot`

表类型：`Runtime Table`

用途：

- 持久化订单侧支付快照
- 保存支付单号、渠道、支付结果摘要

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 雪花主键，由应用侧发号写入 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_id` | `varchar(64)` | N | 订单主键，关联 `bacon_order_order.id` |
| `payment_no` | `varchar(64)` | N | 支付单号，全局唯一 |
| `channel_code` | `varchar(32)` | N | 渠道编码 |
| `pay_status` | `varchar(16)` | N | 支付状态，取值见 `pay_status` |
| `paid_amount` | `decimal(18,2)` | Y | 已付金额 |
| `paid_time` | `datetime(3)` | Y | 已付时间 |
| `failure_reason` | `varchar(255)` | Y | 支付失败原因摘要 |
| `channel_status` | `varchar(64)` | Y | 渠道状态摘要 |
| `updated_at` | `datetime(3)` | N | 快照更新时间 |

索引与约束：

- `pk(id)`
- `uk_order_id(order_id)`
- `uk_payment_no(payment_no)`

### 7.4 `bacon_order_inventory_snapshot`

表类型：`Runtime Table`

用途：

- 持久化订单侧库存快照
- 保存库存预占单号、仓库和库存处理结果摘要

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 雪花主键，由应用侧发号写入 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `reservation_no` | `varchar(64)` | N | 预占单号，全局唯一 |
| `inventory_status` | `varchar(16)` | N | 库存状态，取值见 `inventory_status` |
| `warehouse_no` | `varchar(64)` | Y | 仓库业务编号 |
| `failure_reason` | `varchar(255)` | Y | 库存失败原因摘要 |
| `updated_at` | `datetime(3)` | N | 快照更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_order_no(tenant_id, order_no)`
- `uk_reservation_no(reservation_no)`

### 7.5 `bacon_order_audit_log`

表类型：`Audit Log Table`

用途：

- 记录订单状态流转、取消、超时关闭和支付结果处理相关审计事件
- 只追加，不更新历史记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `action_type` | `varchar(64)` | N | 操作类型，取值见 `action_type` |
| `before_status` | `varchar(32)` | Y | 变更前订单状态，取值见 `order_status` |
| `after_status` | `varchar(32)` | Y | 变更后订单状态，取值见 `order_status` |
| `operator_type` | `varchar(32)` | Y | 操作人类型，取值见 `operator_type` |
| `operator_id` | `varchar(64)` | Y | 操作人标识 |
| `occurred_at` | `datetime(3)` | N | 事件发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_order_no(order_no)`

### 7.6 `bacon_order_outbox`

表类型：`Runtime Table`

用途：

- 持久化跨域编排事件
- 提供重试、抢占、租约与死信流转基础

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 雪花主键，由应用侧发号写入 |
| `event_code` | `varchar(64)` | N | 事件业务标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `event_type` | `varchar(64)` | N | 事件类型 |
| `business_key` | `varchar(128)` | N | 幂等业务键 |
| `payload` | `text` | Y | 事件载荷 |
| `status` | `varchar(32)` | N | 事件状态：`NEW`、`RETRYING`、`PROCESSING`、`DEAD` |
| `retry_count` | `int` | N | 重试次数 |
| `next_retry_at` | `datetime(3)` | Y | 下次重试时间 |
| `processing_owner` | `varchar(128)` | Y | 处理者标识 |
| `lease_until` | `datetime(3)` | Y | 租约到期时间 |
| `claimed_at` | `datetime(3)` | Y | 抢占时间 |
| `error_message` | `varchar(512)` | Y | 最近错误信息 |
| `dead_reason` | `varchar(128)` | Y | 死信原因 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_event_code(event_code)`
- `uk_biz_event(tenant_id, business_key, event_type)`
- `idx_status_next_retry(status, next_retry_at)`
- `idx_tenant_order(tenant_id, order_no)`

### 7.7 `bacon_order_dead_letter`

表类型：`Runtime Table`

用途：

- 持久化 outbox 重试耗尽事件
- 支撑运营重放与补偿

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 雪花主键，由应用侧发号写入 |
| `outbox_id` | `bigint` | N | outbox 主键 |
| `event_code` | `varchar(64)` | N | 事件业务标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `event_type` | `varchar(64)` | N | 事件类型 |
| `business_key` | `varchar(128)` | N | 幂等业务键 |
| `payload` | `text` | Y | 事件载荷 |
| `retry_count` | `int` | N | 重试次数 |
| `error_message` | `varchar(512)` | Y | 错误信息 |
| `dead_reason` | `varchar(128)` | N | 死信原因 |
| `dead_at` | `datetime(3)` | N | 死信时间 |
| `replay_status` | `varchar(32)` | N | 重放状态 |
| `replay_count` | `int` | N | 重放次数 |
| `last_replay_at` | `datetime(3)` | Y | 最近重放时间 |
| `last_replay_message` | `varchar(512)` | Y | 最近重放信息 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_dead_at(tenant_id, dead_at)`
- `idx_order_no(order_no)`

### 7.8 `bacon_order_idempotency_record`

表类型：`Runtime Table`

用途：

- 持久化订单命令幂等记录
- 支撑支付回调、取消、超时关闭的重复请求短路

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `tenant_id` | `bigint` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `event_type` | `varchar(64)` | N | 幂等事件类型 |
| `status` | `varchar(16)` | N | 处理状态：`PROCESSING`、`SUCCESS`、`FAILED` |
| `attempt_count` | `int` | N | 尝试次数 |
| `last_error` | `varchar(512)` | Y | 最近失败摘要 |
| `processing_owner` | `varchar(128)` | Y | 租约持有者 |
| `lease_until` | `datetime(3)` | Y | 租约到期时间 |
| `claimed_at` | `datetime(3)` | Y | 最近抢占时间 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `uk_tenant_order_event(tenant_id, order_no, event_type)`
- `idx_status_updated(status, updated_at)`
- `idx_status_lease(status, lease_until)`

## 8. Relationship Rules

- `bacon_order_item.order_id` 关联 `bacon_order_order.id`
- `bacon_order_payment_snapshot.order_id` 关联 `bacon_order_order.id`
- `bacon_order_inventory_snapshot.order_no` 关联 `bacon_order_order.order_no`
- 当前设计不强制数据库外键
- `payment_no` 和 `reservation_no` 是跨域快照业务键，不替代对端主表

## 9. Persistence Rules

- `Order.order_no` 全局唯一
- `Order.order_no` 固定由发号中心客户端在 `Order` 模块 `infra` 层生成
- 发号失败时必须直接失败，不得降级为本地临时号段
- `OrderPaymentSnapshot.payment_no` 全局唯一
- `OrderInventorySnapshot.reservation_no` 全局唯一
- `OrderAmount` 是值对象，不单独建表
- `OrderSnapshot` 是详情聚合视图，不单独建表
- 订单和订单明细不支持物理删除，也不增加逻辑删除字段
- `Order.total_amount` 固定等于全部 `OrderItem.line_amount` 之和
- `Order.payable_amount` 固定等于 `Order.total_amount`
- `id` 是数据库主键；`order_no` 是业务单号；二者不得混用
- `OrderPaymentSnapshot` 和 `OrderInventorySnapshot` 是订单侧只读快照，不承载对端主数据
- `OrderOutboxEvent.business_key + event_type` 必须全局幂等
- `OrderIdempotencyRecord` 必须按 `(tenant_id, order_no, event_type)` 建唯一约束
- `OrderIdempotencyRecord` 的 `PROCESSING` 状态必须维护 `processing_owner + lease_until`

## 10. Query Model Rules

- 订单详情查询主表固定为 `bacon_order_order + bacon_order_item + bacon_order_payment_snapshot + bacon_order_inventory_snapshot`
- 订单分页查询主表固定为 `bacon_order_order`
- 订单分页查询必须在仓储层完成条件过滤与分页，不允许应用层全量加载后过滤
- 订单分页查询排序固定为 `created_at desc, id desc`
- 订单分页查询默认使用 `limit/offset`，并可按场景扩展 `seek` 模式
- 审计查询主表固定为 `bacon_order_audit_log`

## 11. Repository Mode And Startup Rule

- `Order` 仓储模式固定支持：`strict`、`memory`
- 默认模式固定为 `strict`
- `strict` 模式下，必须存在基于 MyBatis 的持久化仓储实现；否则应用启动必须失败（fail-fast）
- `memory` 模式仅允许测试环境启用，不得在生产环境启用
- 内存仓储实现只用于测试和演示，不作为生产级持久化能力
