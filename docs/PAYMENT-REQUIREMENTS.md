# PAYMENT REQUIREMENTS

## 1. Purpose

Payment 是 Bacon 的统一支付业务域。  
本文档定义 Payment 模块的需求边界、实现约束和稳定契约。  
本文档用于指导设计、实现和测试。

## 2. Scope

### 2.1 In Scope

- 支付单创建
- 支付单详情查询
- 支付发起参数生成
- 支付渠道回调处理
- 支付关闭
- 支付状态查询
- 支付只读跨域查询
- 支付审计日志

### 2.2 Out Of Scope

- 退款
- 分账
- 提现
- 多币种
- 钱包余额
- 对账结算

## 3. Bounded Context

### 3.1 Payment

- `Payment` 负责支付单、支付渠道适配和支付渠道回调处理
- `Payment` 负责产出稳定支付结果并回传给 `Order`
- `Payment` 不负责订单主数据维护
- `Payment` 不负责库存扣减

### 3.2 Order

- `Order` 负责订单状态流转
- `Order` 通过 `Payment` 创建支付单和关闭支付单
- `Payment` 只通过 `OrderCommandFacade` 回传支付成功和支付失败结果

### 3.3 Inventory

- `Inventory` 不直接调用 `Payment`
- 支付成功后的库存扣减由 `Order` 负责触发

### 3.4 Cross-Domain Rule

- `Payment` 只能依赖 `bacon-order-api`
- `Payment` 不得依赖其他业务域内部实现
- 支付结果必须通过稳定 `Facade` 回传 `Order`
- `Payment` 不得直接修改 `Order` 表
- 单体模式使用本地 `Facade` 实现
- 微服务模式使用远程 `Facade` 实现，并保持同一契约

## 4. Module Mapping

### 4.1 `bacon-payment-api`

- 跨域 `Facade`
- `DTO`
- 对外共享枚举

固定接口：

- `PaymentReadFacade`
- `PaymentCommandFacade`

`PaymentReadFacade` 固定方法：

- `getByPaymentNo(tenantId, paymentNo)`，返回固定 `PaymentDetailDTO`
- `getByOrderNo(tenantId, orderNo)`，返回固定 `PaymentDetailDTO`

`PaymentSummaryDTO` 至少包含：

- `tenantId`
- `paymentNo`
- `orderNo`
- `userId`
- `channelCode`
- `paymentStatus`
- `amount`
- `paidAmount`
- `createdAt`
- `expiredAt`
- `paidAt`

`PaymentDetailDTO` 至少包含：

- 全部 `PaymentSummaryDTO` 字段
- `subject`
- `closedAt`
- `channelTransactionNo`
- `channelStatus`
- `callbackSummary`

`PaymentCommandFacade` 固定方法：

- `createPayment(tenantId, orderNo, userId, amount, channelCode, subject, expiredAt)`，返回固定 `PaymentCreateResultDTO`
- `closePayment(tenantId, paymentNo, reason)`，返回固定 `PaymentCloseResultDTO`

固定约束：

- 支付成功回调后，`Payment` 调用 `OrderCommandFacade.markPaid(tenantId, orderNo, paymentNo, channelCode, paidAmount, paidTime)`
- 支付失败回调后，`Payment` 调用 `OrderCommandFacade.markPaymentFailed(tenantId, orderNo, paymentNo, reason, channelStatus, failedTime)`

`PaymentCreateResultDTO` 至少包含：

- `tenantId`
- `paymentNo`
- `orderNo`
- `channelCode`
- `paymentStatus`
- `payPayload`
- `expiredAt`
- `failureReason`

`PaymentCloseResultDTO` 至少包含：

- `tenantId`
- `paymentNo`
- `orderNo`
- `paymentStatus`
- `closeResult`
- `closeReason`
- `failureReason`

### 4.2 `bacon-payment-interfaces`

- `Controller`
- 请求 `DTO`
- 响应 `VO`
- `Assembler`
- `Facade Local` 适配实现（固定放在 `interfaces.facade`）
- 对外适配端点

固定端点：

- `GET /payments/{paymentNo}`
- `GET /payments`
- `GET /payments/{paymentNo}/audit-logs`
- `POST /payments/callback/{channelCode}`

### 4.3 `bacon-payment-application`

固定服务：

- `PaymentCreateApplicationService`
- `PaymentQueryApplicationService`
- `PaymentCallbackApplicationService`
- `PaymentCloseApplicationService`
- `PaymentAuditQueryApplicationService`
- `PaymentOperationLogSupport`

### 4.4 `bacon-payment-domain`

- 聚合、实体、值对象
- 领域服务
- 按职责拆分的 `Repository` 接口
- 领域规则和不变量

### 4.5 `bacon-payment-infra`

- `MyBatis-Plus Mapper`
- 按职责拆分的 `Repository` 实现
- `Facade Remote` 适配实现（固定放在 `infra.facade.remote`）
- 渠道适配器
- 审计日志持久化

## 5. Core Domain Objects

- `PaymentOrder`
- `PaymentChannelPayload`
- `PaymentCallbackRecord`
- `PaymentAuditLog`

## 5.1 Fixed Enums

- `paymentStatus` 固定为 `CREATED`、`PAYING`、`PAID`、`FAILED`、`CLOSED`
- `channelCode` 固定为 `MOCK`
- `closeResult` 固定为 `SUCCESS`、`FAILED`
- `closeReason` 固定为 `USER_CANCELLED`、`SYSTEM_CANCELLED`、`TIMEOUT_CLOSED`

## 5.2 Terminology

- `PaymentOrder` 是支付主聚合
- `PaymentChannelPayload` 是返回给客户端的渠道支付参数
- `PaymentCallbackRecord` 是渠道原始回调记录
- `PaymentSummaryDTO` 是支付摘要字段集合
- `PaymentDetailDTO` 是支付详情读模型
- `PaymentCreateResultDTO` 是创建支付单命令返回模型
- `PaymentCloseResultDTO` 是关闭支付单命令返回模型
- `支付关闭` 指未完成支付单进入不可继续支付的终态

## 5.3 Fixed Fields

- `PaymentOrder` 至少包含 `id`、`tenantId`、`paymentNo`、`orderNo`、`userId`、`channelCode`、`paymentStatus`、`amount`、`paidAmount`、`subject`、`expiredAt`、`createdAt`、`paidAt`、`closedAt`
- `PaymentChannelPayload` 至少包含 `paymentNo`、`channelCode`、`payUrl`
- `PaymentCallbackRecord` 至少包含 `id`、`tenantId`、`paymentNo`、`orderNo`、`channelCode`、`channelTransactionNo`、`channelStatus`、`rawPayload`、`receivedAt`
- `PaymentAuditLog` 至少包含 `id`、`tenantId`、`paymentNo`、`actionType`、`beforeStatus`、`afterStatus`、`operatorType`、`operatorId`、`occurredAt`

固定约束：

- `PaymentSummaryDTO` 字段由 `PaymentOrder` 组装
- `PaymentDetailDTO` 字段由 `PaymentOrder` 和最近一次有效 `PaymentCallbackRecord` 组装
- `PaymentDetailDTO.callbackSummary` 来自最近一次有效 `PaymentCallbackRecord.rawPayload` 的摘要结果
- `PaymentDetailDTO` 不返回 `rawPayload` 原文
- `PaymentCreateResultDTO` 字段由创建后的 `PaymentOrder` 和 `PaymentChannelPayload` 组装
- `PaymentCreateResultDTO.paymentStatus=PAYING` 时，`paymentNo`、`payPayload`、`expiredAt` 必须有值
- `PaymentCreateResultDTO.paymentStatus` 不为 `PAYING` 时，`paymentNo`、`payPayload`、`expiredAt` 允许为空
- `PaymentCloseResultDTO.closeResult=SUCCESS` 时，`paymentStatus` 必须为 `CLOSED`
- `PaymentCloseResultDTO.closeResult=FAILED` 时，`paymentStatus` 不得变更为 `CLOSED`
- `PaymentCreateResultDTO.paymentStatus=PAYING` 时，`failureReason` 必须为空
- `PaymentCreateResultDTO.paymentStatus` 不为 `PAYING` 时，`paymentStatus` 必须固定为 `FAILED`
- `PaymentCreateResultDTO.paymentStatus` 不为 `PAYING` 时，`failureReason` 必须有值
- `PaymentCloseResultDTO.closeResult=SUCCESS` 时，`failureReason` 必须为空
- `PaymentCloseResultDTO.closeReason` 必须等于关闭请求原因
- `PaymentCloseResultDTO.closeResult=FAILED` 时，`failureReason` 必须有值
- `PaymentCloseResultDTO.closeResult=FAILED` 时，`closeReason` 仍必须等于关闭请求原因

## 5.4 Fixed Request Contracts

- `CreatePaymentRequest` 至少包含 `tenantId`、`orderNo`、`userId`、`amount`、`channelCode`、`subject`、`expiredAt`
- `ClosePaymentRequest` 至少包含 `tenantId`、`paymentNo`、`reason`
- `PaymentCallbackRequest` 至少包含 `tenantId`、`paymentNo`、`success`、`channelTransactionNo`、`channelStatus`、`rawPayload`、`reason`

固定约束：

- `ClosePaymentRequest.reason` 的值域遵守 `6.5 Close Reason Rule`
- `PaymentCallbackRequest.success=true` 时，`channelTransactionNo`、`channelStatus`、`rawPayload` 必须有值，`reason` 必须为空
- `PaymentCallbackRequest.success=false` 时，`channelStatus`、`rawPayload`、`reason` 必须有值，`channelTransactionNo` 允许为空

## 5.5 Uniqueness And Index Rules

- `PaymentOrder.id` 全局唯一
- `PaymentOrder.paymentNo` 全局唯一
- `PaymentOrder.orderNo` 全局唯一
- `PaymentCallbackRecord.id` 全局唯一
- `PaymentCallbackRecord` 必须保证 `(tenantId, channelCode, channelTransactionNo)` 唯一
- `PaymentOrder` 必须建立 `(tenantId, userId, createdAt)` 索引
- `PaymentOrder` 必须建立 `(tenantId, paymentStatus, expiredAt)` 索引

## 6. Global Constraints

### 6.1 Amount Rule

- `amount` 必须大于 `0`
- `paidAmount` 在支付成功时必须大于 `0`
- 支付成功时，`paidAmount` 必须等于支付单 `amount`

### 6.1.1 Numbering Rule

- `paymentNo` 必须由 `Payment` 模块内部生成
- `paymentNo` 生成失败时，创建支付单必须直接失败，不得降级为本地雪花号、时间戳拼接、数据库 `max(id)+1` 或其他临时发号

### 6.2 Status Rule

- 支付单创建成功后，初始 `paymentStatus` 必须进入 `CREATED`
- 支付发起参数生成后，`paymentStatus` 必须进入 `PAYING`
- 支付成功后，`paymentStatus` 必须进入 `PAID`
- 支付失败后，`paymentStatus` 必须进入 `FAILED`
- 支付关闭后，`paymentStatus` 必须进入 `CLOSED`
- `PAID`、`FAILED`、`CLOSED` 支付单不得重新进入 `PAYING`

### 6.3 Callback Rule

- 支付回调处理必须幂等
- 未知支付单回调必须拒绝
- 固定只支持 `MOCK` 渠道
- 回调结果必须保留原始渠道状态摘要
- 支付成功后，`Payment` 必须调用 `OrderCommandFacade.markPaid`
- 支付失败后，`Payment` 必须调用 `OrderCommandFacade.markPaymentFailed`
- `Payment` 不得在回调处理中直接修改订单表
- 回调记录必须先持久化 `PaymentCallbackRecord`，再更新 `PaymentOrder`
- `PaymentDetailDTO.callbackSummary` 必须基于最近一次有效回调记录的 `rawPayload` 摘要组装

### 6.4 Payment Presence Rule

- 未创建支付单时，不得调用 `closePayment`
- `closePayment` 仅允许对已存在的 `PaymentOrder` 执行

### 6.5 Close Reason Rule

- `closePayment.reason` 必须使用固定枚举 `USER_CANCELLED`、`SYSTEM_CANCELLED`、`TIMEOUT_CLOSED`
- `Payment` 不得写入固定枚举之外的新关闭原因值
- `closePayment` 只用于订单取消和订单超时关闭，不用于支付失败或支付单创建失败场景

### 6.6 Idempotency Rule

- `createPayment` 按 `orderNo` 幂等
- `closePayment` 按 `paymentNo` 幂等
- 渠道回调按 `(channelCode, channelTransactionNo)` 幂等
- 同一 `orderNo` 只能存在一个 `PaymentOrder`

## 7. Functional Requirements

### 7.1 Create Payment

- 创建支付单
- 生成支付发起参数
- 返回支付单号和支付参数

固定约束：

- 请求字段遵守 `5.4 Fixed Request Contracts`
- 仅允许为 `Order` 已确认库存预占成功的订单创建支付单
- 支付发起参数固定生成 `PaymentChannelPayload.payUrl`
- `channelCode` 固定为 `MOCK`
- 创建成功时必须持久化一条 `PaymentAuditLog`，`actionType` 固定为 `CREATE`
- 创建失败时，必须通过 `PaymentCreateResultDTO.failureReason` 返回明确失败原因

### 7.2 Payment Callback

- 接收支付渠道回调
- 更新支付状态
- 回传订单支付结果

固定约束：

- 成功回调不得重复改变已支付结果
- 失败回调不得覆盖已支付结果
- 成功回调时必须持久化 `channelTransactionNo`
- 成功回调时必须写入 `PaymentAuditLog`，`actionType` 固定为 `CALLBACK_PAID`
- 失败回调时必须写入 `PaymentAuditLog`，`actionType` 固定为 `CALLBACK_FAILED`
- 幂等命中或终态忽略的回调同样必须写入 `PaymentAuditLog`；此时 `beforeStatus` 与 `afterStatus` 允许相同

### 7.3 Close Payment

- 关闭未完成支付单

固定约束：

- 请求字段遵守 `5.4 Fixed Request Contracts`
- 已支付支付单不得关闭
- 重复关闭不得产生脏数据
- 关闭成功时必须写入 `PaymentAuditLog`，`actionType` 固定为 `CLOSE`
- `closePayment` 成功关闭时不得再保留可继续支付的渠道参数语义

### 7.4 Read Capability

- 为 `Order` 提供支付状态只读查询

固定约束：

- 跨域接口只读
- `DTO` 契约必须稳定
- `Order` 跨域读取只依赖支付状态和关键关联字段
- 当前 `PaymentReadFacade` 的两个查询方法统一返回 `PaymentDetailDTO`
- `PaymentSummaryDTO` 用于定义详情模型中的摘要字段集合，不单独作为固定接口返回模型
- `PaymentDetailDTO` 主要用于后台排障和详情查询

### 7.5 Audit Log

- 记录支付单创建
- 记录支付回调
- 记录支付关闭
- 审计日志至少支持按 `tenantId`、`paymentNo`、`actionType`、`occurredAt` 查询
- 固定提供按 `tenantId + paymentNo` 查询支付审计日志的应用服务、外部查询接口和内部 provider 查询接口

## 8. Key Flows

### 8.1 Create Payment

1. `Order` 发起支付单创建
2. `Payment` 校验 `orderNo` 唯一性和金额有效性
3. `Payment` 创建 `PaymentOrder`，状态进入 `CREATED`
4. `Payment` 生成渠道支付参数，状态推进到 `PAYING`
5. `Payment` 写入 `PaymentAuditLog`
6. `Payment` 返回支付单号和支付参数

### 8.2 Payment Callback Success

1. 渠道回调支付成功
2. `Payment` 按渠道交易号执行幂等校验
3. `Payment` 持久化 `PaymentCallbackRecord`
4. `Payment` 更新 `paymentStatus=PAID`
5. `Payment` 写入 `PaymentAuditLog`
6. `Payment` 调用 `OrderCommandFacade.markPaid`

### 8.3 Payment Callback Failure

1. 渠道回调支付失败
2. `Payment` 按渠道交易号执行幂等校验
3. `Payment` 持久化 `PaymentCallbackRecord`
4. `Payment` 更新 `paymentStatus=FAILED`
5. `Payment` 写入 `PaymentAuditLog`
6. `Payment` 调用 `OrderCommandFacade.markPaymentFailed`

### 8.4 Close Payment

1. `Order` 发起支付关闭
2. `Payment` 校验支付状态
3. `Payment` 更新 `paymentStatus=CLOSED`
4. `Payment` 写入 `PaymentAuditLog`
5. `Payment` 返回 `PaymentCloseResultDTO`

## 9. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-001 | Consistency | 支付创建、回调处理、关闭必须幂等 |
| NFR-002 | Architecture | 必须同时支持单体和微服务装配 |
| NFR-003 | Compatibility | `Facade + DTO` 契约必须同时支持本地和远程实现 |
| NFR-004 | Auditability | 审计日志必须持久化、可检索、可追溯 |
