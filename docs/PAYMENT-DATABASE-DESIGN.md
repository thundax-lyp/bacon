# PAYMENT DATABASE DESIGN

## 1. Purpose

本文档定义 `Payment` 的数据结构与数据库设计。  
目标是让 AI 可直接据此生成表结构、实体、`Mapper`、`Repository` 和查询实现。  
本文档只保留数据库设计所需的稳定信息，不重复业务需求文档中的冗余描述。
本文档必须遵守 [DATABASE-REQUIREMENTS.md](./DATABASE-REQUIREMENTS.md)。
如与工程级数据库规范冲突，以 [DATABASE-REQUIREMENTS.md](./DATABASE-REQUIREMENTS.md) 为准。

## 2. Scope

当前范围覆盖：

- `PaymentOrder`
- `PaymentCallbackRecord`
- `PaymentAuditLog`

不覆盖：

- `PaymentChannelPayload`
- 退款、分账、对账相关表

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则固定使用 `utf8mb4_0900_ai_ci`
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 金额字段统一使用 `decimal(18,2)`
- 枚举字段统一使用 `varchar`

## 3.1 Common Field Rules

- 支付主表使用 `created_at`、`updated_at`
- 回调记录表和审计表使用领域时间字段，不额外重复声明通用时间字段
- `Payment` 当前范围内不引入逻辑删除字段

## 4. Naming Rules

- 表名固定格式使用 `bacon_${domain}_${model}`
- `Payment` 表名统一使用 `bacon_payment_` 前缀
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 审计发生时间统一命名为 `occurred_at`
- 回调接收时间统一命名为 `received_at`

## 5. Enum Storage Rules

- `payment_status`: `CREATED`、`PAYING`、`PAID`、`FAILED`、`CLOSED`
- `channel_code`: `MOCK`
- `close_result`: `SUCCESS`、`FAILED`
- `close_reason`: `USER_CANCELLED`、`SYSTEM_CANCELLED`、`TIMEOUT_CLOSED`

## 5.1 Length Rules

- `tenant_id`: `varchar(64)`
- `payment_no`: `varchar(64)`
- `order_no`: `varchar(64)`
- `channel_code`: `varchar(32)`
- `channel_transaction_no`: `varchar(128)`
- `channel_status`: `varchar(64)`
- `subject`: `varchar(255)`
- `failure_reason`: `varchar(255)`
- `action_type`: `varchar(64)`
- `operator_type`: `varchar(32)`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `PaymentOrder` | `bacon_payment_order` |
| `PaymentCallbackRecord` | `bacon_payment_callback_record` |
| `PaymentAuditLog` | `bacon_payment_audit_log` |

## 7. Table Design

### 7.1 `bacon_payment_order`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `payment_no` | `varchar(64)` | N | 支付业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `channel_code` | `varchar(32)` | N | 渠道编码 |
| `payment_status` | `varchar(16)` | N | 支付状态 |
| `amount` | `decimal(18,2)` | N | 应付金额 |
| `paid_amount` | `decimal(18,2)` | Y | 实付金额 |
| `subject` | `varchar(255)` | N | 支付主题 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |
| `expired_at` | `datetime(3)` | N | 过期时间 |
| `paid_at` | `datetime(3)` | Y | 支付时间 |
| `closed_at` | `datetime(3)` | Y | 关闭时间 |

索引与约束：

- `pk(id)`
- `uk_payment_no(payment_no)`
- `uk_order_no(order_no)`
- `idx_tenant_user_created(tenant_id, user_id, created_at)`
- `idx_tenant_status_expired(tenant_id, payment_status, expired_at)`

### 7.2 `bacon_payment_callback_record`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `payment_no` | `varchar(64)` | N | 支付业务键 |
| `order_no` | `varchar(64)` | N | 订单业务键 |
| `channel_code` | `varchar(32)` | N | 渠道编码 |
| `channel_transaction_no` | `varchar(128)` | N | 渠道交易号 |
| `channel_status` | `varchar(64)` | N | 渠道状态摘要 |
| `raw_payload` | `json` | N | 原始回调内容 |
| `received_at` | `datetime(3)` | N | 接收时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_channel_txn(tenant_id, channel_code, channel_transaction_no)`
- `idx_payment_no(payment_no)`

### 7.3 `bacon_payment_audit_log`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `payment_no` | `varchar(64)` | N | 支付业务键 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `before_status` | `varchar(16)` | Y | 变更前状态 |
| `after_status` | `varchar(16)` | Y | 变更后状态 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人标识 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_payment_no(payment_no)`

## 8. Relationship Rules

- `bacon_payment_callback_record.payment_no -> bacon_payment_order.payment_no`

固定约束：

- 当前设计不强制数据库外键
- `PaymentChannelPayload` 是返回模型，不单独建表
- `PaymentDetailDTO.callbackSummary` 由最近一次有效 `PaymentCallbackRecord.raw_payload` 摘要组装

## 9. Persistence Rules

- `PaymentOrder.payment_no` 全局唯一
- `PaymentOrder.order_no` 全局唯一
- `PaymentCallbackRecord(tenant_id, channel_code, channel_transaction_no)` 唯一
- `raw_payload` 固定保存渠道原始回调，不在读模型中原样返回
- `PaymentCreateResultDTO`、`PaymentCloseResultDTO` 是命令返回模型，不单独建表

## 10. Query Model Rules

- 支付详情查询主表为 `bacon_payment_order + bacon_payment_callback_record`
- 支付状态查询主表为 `bacon_payment_order`
- 审计查询主表为 `bacon_payment_audit_log`

## 11. DDL Generation Notes

- `amount`、`paid_amount` 固定两位小数
- 支付回调按 `(tenant_id, channel_code, channel_transaction_no)` 幂等
- `PAID`、`FAILED`、`CLOSED` 后不得重新进入 `PAYING`

## 12. Open Items

无
