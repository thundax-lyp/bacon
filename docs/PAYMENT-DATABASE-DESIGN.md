# PAYMENT DATABASE DESIGN

## 1. Purpose

本文档定义 `Payment` 业务域的数据库设计。  
目标是让 AI 和工程师可直接据此生成 `DDL`、`DataObject`、`Mapper`、`Repository`、支付回调持久化和支付查询实现。  
本文档只定义 `Payment` 自有的持久化对象、字段、索引、回调记录边界和查询模型，不重复业务需求文档中的流程描述。  
本文档必须遵守 [DATABASE-RULES.md](./DATABASE-RULES.md)。如与工程级数据库规范冲突，以 [DATABASE-RULES.md](./DATABASE-RULES.md) 为准。

## 2. Scope

当前范围覆盖以下持久化对象：

- `PaymentOrder`
- `PaymentCallbackRecord`
- `PaymentAuditLog`

当前范围不建表的对象：

- `PaymentChannelPayload`
- 退款相关表
- 分账相关表
- 对账相关表

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库实例可用的 `utf8mb4` 排序规则（推荐 `utf8mb4_unicode_ci`）
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 金额字段统一使用 `decimal(18,2)`
- 枚举字段统一使用 `varchar`
- 渠道原始回调固定使用 `json`
- 支付主表使用 `created_at`、`updated_at`
- 回调记录表和审计表使用领域时间字段，不额外增加 `created_at`、`updated_at`
- 当前范围不使用逻辑删除字段
- 仓储模式固定支持 `strict` 和 `memory`
- 默认模式固定为 `strict`
- `strict` 模式固定使用 `MyBatis-Plus DO + Mapper + RepositoryImpl`
- `memory` 模式只允许显式启用，用于本地调试或无数据库测试

## 4. Naming Rules

- 表名前缀固定使用 `bacon_payment_`
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 审计发生时间统一命名为 `occurred_at`
- 回调接收时间统一命名为 `received_at`

## 5. Enum Storage Rules

### 5.1 Fixed Enums

- `payment_status`: `CREATED`、`PAYING`、`PAID`、`FAILED`、`CLOSED`
- `channel_code`: `MOCK`
- `close_result`: `SUCCESS`、`FAILED`
- `close_reason`: `USER_CANCELLED`、`SYSTEM_CANCELLED`、`TIMEOUT_CLOSED`
- `action_type`: `CREATE`、`CALLBACK_PAID`、`CALLBACK_FAILED`、`CLOSE`
- `operator_type`: `SYSTEM`、`USER`、`CHANNEL`

### 5.2 Fixed Length Rules

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

表类型：`Runtime Table`

用途：

- 持久化支付主聚合
- 承载支付状态、金额和过期时间
- 当前范围固定每个 `order_no` 只对应一个支付单

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `payment_no` | `varchar(64)` | N | 支付业务键，全局唯一 |
| `order_no` | `varchar(64)` | N | 订单业务键，全局唯一 |
| `user_id` | `bigint` | N | 用户主键 |
| `channel_code` | `varchar(32)` | N | 渠道编码，取值见 `channel_code` |
| `payment_status` | `varchar(16)` | N | 支付状态，取值见 `payment_status` |
| `amount` | `decimal(18,2)` | N | 应付金额 |
| `paid_amount` | `decimal(18,2)` | Y | 实付金额 |
| `subject` | `varchar(255)` | N | 支付主题 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |
| `expired_at` | `datetime(3)` | N | 过期时间 |
| `paid_at` | `datetime(3)` | Y | 支付完成时间 |
| `closed_at` | `datetime(3)` | Y | 关闭时间 |

索引与约束：

- `pk(id)`
- `uk_payment_no(payment_no)`
- `uk_order_no(order_no)`
- `idx_tenant_user_created(tenant_id, user_id, created_at)`
- `idx_tenant_status_expired(tenant_id, payment_status, expired_at)`

固定约束：

- 创建成功并生成渠道参数后，`payment_status` 固定持久化为 `PAYING`
- `paid_amount` 在未支付前固定为 `0.00`
- `updated_at` 必须在支付成功、支付失败、支付关闭时更新

### 7.2 `bacon_payment_callback_record`

表类型：`Runtime Table`

用途：

- 持久化渠道原始回调记录
- 作为支付回调幂等和回调摘要的来源
- 同一渠道交易号在同租户同渠道下只保留一条有效记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `payment_no` | `varchar(64)` | N | 支付业务键，关联 `bacon_payment_order.payment_no` |
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
- `idx_tenant_payment_received(tenant_id, payment_no, received_at)`

固定约束：

- `raw_payload` 固定保存渠道回调原文或结构化 JSON
- `channel_transaction_no` 在成功回调场景必须有值
- 失败回调允许 `channel_transaction_no` 为空；为空时由应用层基于 `payment_no` 和失败语义执行幂等防重

### 7.3 `bacon_payment_audit_log`

表类型：`Audit Log Table`

用途：

- 记录支付单创建、回调处理、支付关闭相关审计事件
- 只追加，不更新历史记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `payment_no` | `varchar(64)` | N | 支付业务键 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `before_status` | `varchar(16)` | Y | 变更前状态 |
| `after_status` | `varchar(16)` | Y | 变更后状态 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人标识 |
| `occurred_at` | `datetime(3)` | N | 事件发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_payment_no(payment_no)`
- `idx_tenant_payment_action(tenant_id, payment_no, action_type, occurred_at)`

## 8. Relationship Rules

- `bacon_payment_callback_record.payment_no` 关联 `bacon_payment_order.payment_no`
- 当前设计不强制数据库外键
- `PaymentChannelPayload` 是返回模型，不单独建表
- `PaymentDetailDTO.callbackSummary` 由最近一次有效 `PaymentCallbackRecord.raw_payload` 摘要组装
- `PaymentDetailDTO.channelTransactionNo`、`channelStatus` 优先取最近一次有效 `PaymentCallbackRecord`

## 9. Persistence Rules

- `PaymentOrder.payment_no` 全局唯一
- `PaymentOrder.payment_no` 固定由发号中心客户端在 `Payment` 模块 `infra` 层生成
- 发号失败时必须直接失败，不得降级为本地临时号段
- `PaymentOrder.id`、`PaymentCallbackRecord.id`、`PaymentAuditLog.id` 在 `strict` 模式下固定由数据库主键生成
- `Payment` 应用层不得自行维护本地自增 `id`
- `strict` 模式下主单更新若未命中任何记录，必须按持久化冲突直接失败，不得伪造成功结果
- `PaymentOrder.order_no` 全局唯一
- `PaymentCallbackRecord(tenant_id, channel_code, channel_transaction_no)` 唯一
- `amount`、`paid_amount` 固定使用两位小数
- `raw_payload` 固定保存渠道原始回调，不在读模型中原样返回
- 支付回调按 `(tenant_id, channel_code, channel_transaction_no)` 幂等
- `PAID`、`FAILED`、`CLOSED` 后不得重新进入 `PAYING`
- 审计日志写入失败不得回滚支付主流程，失败处理遵守工程级数据库规范
- `PaymentCreateResultDTO`、`PaymentCloseResultDTO` 是命令返回模型，不单独建表
- `id` 是数据库主键；`payment_no` 是业务单号；二者不得混用
- 持久化实现固定路径：
  - `strict` 模式使用 `PaymentRepositorySupport + Payment*RepositoryImpl`
  - `memory` 模式使用 `InMemoryPaymentRepositorySupport + InMemoryPayment*RepositoryImpl`

## 10. Query Model Rules

- 支付详情查询主表固定为 `bacon_payment_order + bacon_payment_callback_record`
- 支付状态查询主表固定为 `bacon_payment_order`
- 审计查询主表固定为 `bacon_payment_audit_log`
- 支付详情查询最近一次回调记录时，固定按 `received_at desc, id desc` 取第一条
- 回调记录列表查询固定按 `received_at desc, id desc` 排序

## 11. Open Items

无
