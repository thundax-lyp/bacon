# INVENTORY REQUIREMENTS

## 1. Purpose

Inventory 是 Bacon 的统一库存业务域。  
本文档定义 Inventory 模块的需求边界、实现约束和稳定契约。  
本文档是后续设计、任务拆解、实现和测试的唯一基线。  
当前范围内全部功能属于同一交付范围，不做分期交付。

## 2. Scope

### 2.1 In Scope

- 库存主数据管理
- 可售库存查询
- 库存预占
- 库存释放
- 库存扣减
- 库存预占单查询
- 库存流水记录
- 库存只读跨域查询
- 库存审计日志

### 2.2 Out Of Scope

- 采购入库
- 调拨
- 批次管理
- 序列号管理
- 盘点
- 多仓分单

## 3. Bounded Context

### 3.1 Inventory

- `Inventory` 负责库存主数据、现存量、预占量、可售量和库存预占单
- `Inventory` 负责保证任何时候不得出现负库存
- `Inventory` 负责为 `Order` 提供同步库存预占、释放、扣减能力
- `Inventory` 不负责订单状态持久化
- `Inventory` 不负责支付状态持久化

### 3.2 Order

- `Order` 负责下单编排和订单状态流转
- `Order` 通过 `Inventory` 发起库存预占、释放、扣减
- `Inventory` 不向 `Order` 反向回调结果，结果由同步命令直接返回

### 3.3 Payment

- `Payment` 不直接调用 `Inventory`
- 支付成功后的库存扣减由 `Order` 触发

### 3.4 Cross-Domain Rule

- `Inventory` 对外只暴露 `bacon-inventory-api`
- `Inventory` 不得依赖 `Order`、`Payment` 的内部实现
- 库存预占、释放、扣减必须通过稳定 `Facade` 暴露
- 库存预占、释放、扣减必须使用同步请求-响应模型
- 单体模式使用本地 `Facade` 实现
- 微服务模式使用远程 `Facade` 实现，并保持同一契约

### 3.5 Resource API Exposure Rule

- 面向前端或第三方调用方的资源接口必须由 `Inventory` 业务域提供，不得直接暴露 `Storage` 业务语义接口
- 若接入商品图片或附件能力，对外路径必须使用 `Inventory` 业务语义路径，例如 `/inventory/product/{productId}/image`
- `Inventory` 负责认证、鉴权、数据可见性与业务主键校验
- `Storage` 仅负责对象上传、引用管理与访问地址生成

## 4. Module Mapping

### 4.1 `bacon-inventory-api`

- 跨域 `Facade`
- `DTO`
- 对外共享枚举

固定接口：

- `InventoryReadFacade`
- `InventoryCommandFacade`

`InventoryReadFacade` 固定方法：

- `getAvailableStock(tenantNo, skuId)`，返回固定 `InventoryStockDTO`
- `batchGetAvailableStock(tenantNo, skuIds)`，返回 `List<InventoryStockDTO>`
- `getReservationByOrderNo(tenantNo, orderNo)`，返回固定 `InventoryReservationDTO`

`InventoryStockDTO` 至少包含：

- `tenantNo`
- `skuId`
- `warehouseId`
- `onHandQuantity`
- `reservedQuantity`
- `availableQuantity`
- `status`
- `updatedAt`

`InventoryReservationDTO` 至少包含：

- `tenantNo`
- `orderNo`
- `reservationNo`
- `reservationStatus`
- `warehouseId`
- `items`
- `failureReason`
- `releaseReason`
- `createdAt`
- `releasedAt`
- `deductedAt`

`InventoryReservationItemDTO` 至少包含：

- `skuId`
- `quantity`

`InventoryCommandFacade` 固定方法：

- `reserveStock(tenantNo, orderNo, items)`，返回固定 `InventoryReservationResultDTO`
- `releaseReservedStock(tenantNo, orderNo, reason)`，返回固定 `InventoryReservationResultDTO`
- `deductReservedStock(tenantNo, orderNo)`，返回固定 `InventoryReservationResultDTO`

`InventoryReservationResultDTO` 至少包含：

- `tenantNo`
- `orderNo`
- `reservationNo`
- `reservationStatus`
- `inventoryStatus`
- `warehouseId`
- `failureReason`
- `releaseReason`
- `releasedAt`
- `deductedAt`

### 4.2 `bacon-inventory-interfaces`

- `Controller`
- 请求 `DTO`
- 响应 `VO`
- 对外适配端点

固定端点：

- `POST /inventories`
- `GET /inventories/{skuId}`
- `GET /inventories`
- `GET /inventories/page`
- `PUT /inventories/{skuId}/status`
- `GET /inventory-reservations/{orderNo}`
- `GET /inventory-ledgers`
- `GET /inventory-audit-logs`
- `GET /inventory-audit-dead-letters`
- `POST /inventory-audit-dead-letters/{deadLetterId}/replay`
- `POST /inventory-audit-dead-letters/replay-batch`
- `POST /inventory-audit-dead-letters/replay-tasks`
- `GET /inventory-audit-dead-letters/replay-tasks/{taskId}`
- `POST /inventory-audit-dead-letters/replay-tasks/{taskId}/pause`
- `POST /inventory-audit-dead-letters/replay-tasks/{taskId}/resume`

### 4.3 `bacon-inventory-application`

固定服务：

- `InventoryApplicationService`
- `InventoryManagementApplicationService`
- `InventoryQueryService`
- `InventoryOperationLogService`
- `InventoryAuditReplayTaskService`
- `InventoryAuditReplayTaskWorker`
- `InventoryReservationApplicationService`
- `InventoryReleaseApplicationService`
- `InventoryDeductionApplicationService`

### 4.4 `bacon-inventory-domain`

- 聚合、实体、值对象
- 领域服务
- 按职责拆分的 `Repository` 接口
- 领域规则和不变量

### 4.5 `bacon-inventory-infra`

- `MyBatis-Plus Mapper`
- `DO`
- 按职责拆分的 `Repository` 实现
- 无数据源场景的内存 `Repository` fallback
- 审计日志持久化

## 5. Core Domain Objects

- `Inventory`
- `InventoryReservation`
- `InventoryReservationItem`
- `InventoryLedger`
- `InventoryAuditLog`

## 5.1 Fixed Enums

- `status` 固定为 `ENABLED`、`DISABLED`
- `reservationStatus` 固定为 `CREATED`、`RESERVED`、`RELEASED`、`DEDUCTED`、`FAILED`
- `ledgerType` 固定为 `RESERVE`、`RELEASE`、`DEDUCT`
- `releaseReason` 固定为 `USER_CANCELLED`、`SYSTEM_CANCELLED`、`PAYMENT_CREATE_FAILED`、`PAYMENT_FAILED`、`TIMEOUT_CLOSED`

## 5.2 Terminology

- `Inventory` 是库存主数据对象
- `InventoryReservation` 是订单维度的库存预占单
- `InventoryReservationItem` 是库存预占单明细
- `InventoryLedger` 是按订单和 SKU 记录的库存变更流水
- `InventoryStockDTO` 是库存主数据读模型
- `InventoryReservationDTO` 是库存预占单读模型
- `InventoryReservationResultDTO` 是库存命令返回模型
- `可售库存` 指当前可继续预占的数量，固定等于 `onHandQuantity - reservedQuantity`

## 5.3 Fixed Fields

- `Inventory` 至少包含 `id`、`tenantNo`、`skuId`、`warehouseId`、`onHandQuantity`、`reservedQuantity`、`availableQuantity`、`status`、`updatedAt`
- `InventoryReservation` 至少包含 `id`、`tenantNo`、`reservationNo`、`orderNo`、`reservationStatus`、`warehouseId`、`failureReason`、`releaseReason`、`createdAt`、`releasedAt`、`deductedAt`
- `InventoryReservationItem` 至少包含 `id`、`tenantNo`、`reservationNo`、`skuId`、`quantity`
- `InventoryLedger` 至少包含 `id`、`tenantNo`、`orderNo`、`reservationNo`、`skuId`、`warehouseId`、`ledgerType`、`quantity`、`occurredAt`
- `InventoryAuditLog` 至少包含 `id`、`tenantNo`、`orderNo`、`reservationNo`、`actionType`、`operatorType`、`operatorId`、`occurredAt`

固定约束：

- `InventoryStockDTO` 字段由 `Inventory` 组装
- `InventoryReservationDTO.items` 由 `InventoryReservationItem` 组装
- `InventoryReservationDTO` 字段由 `InventoryReservation` 和 `InventoryReservationItem` 组装
- `InventoryReservationResultDTO.inventoryStatus` 必须遵守 `6.4 InventoryStatus Mapping Rule`
- `InventoryReservationResultDTO` 字段由命令处理后的 `InventoryReservation` 组装

## 5.4 Fixed Request Contracts

- `ReserveStockRequest` 至少包含 `tenantNo`、`orderNo`、`items`
- `ReserveStockRequest.items` 至少包含 `skuId`、`quantity`
- `ReleaseReservedStockRequest` 至少包含 `tenantNo`、`orderNo`、`reason`
- `DeductReservedStockRequest` 至少包含 `tenantNo`、`orderNo`

## 5.5 Uniqueness And Index Rules

- `Inventory.id` 全局唯一
- `Inventory.id` 由持久化层生成，应用层不得自行发号
- 当前范围 `Inventory` 必须保证 `(tenantNo, skuId)` 唯一
- `InventoryReservation.id` 全局唯一
- `InventoryReservation.id` 由持久化层生成，应用层不得自行发号
- `InventoryReservation.reservationNo` 全局唯一
- `InventoryReservation` 必须保证 `(tenantNo, orderNo)` 唯一
- `InventoryReservationItem.id` 全局唯一
- `InventoryReservationItem.id` 由持久化层生成，应用层不得自行发号
- `InventoryReservationItem` 必须保证 `(tenantNo, reservationNo, skuId)` 唯一
- `InventoryLedger.id` 全局唯一
- `InventoryLedger` 必须建立 `(tenantNo, orderNo, ledgerType)` 索引
- `Inventory` 必须建立 `(tenantNo, skuId)` 索引
- `Inventory` 必须具备版本控制字段，用于并发写入冲突检测

## 6. Global Constraints

### 6.1 Quantity Rule

- 当前范围固定单仓模型，同一 `tenantNo + skuId` 只允许一条 `Inventory` 记录
- `warehouseId` 必须使用系统固定默认仓标识
- `onHandQuantity` 不得小于 `0`
- `reservedQuantity` 不得小于 `0`
- `availableQuantity` 不得小于 `0`
- `availableQuantity` 必须始终等于 `onHandQuantity - reservedQuantity`
- 预占成功后，必须增加 `reservedQuantity`
- 释放成功后，必须减少 `reservedQuantity`
- 扣减成功后，必须同时减少 `onHandQuantity` 和 `reservedQuantity`
- 库存数量写入必须显式持久化，不得依赖内存对象引用副作用

### 6.1.1 Numbering Rule

- `reservationNo` 必须由 `Inventory` 模块内部生成
- 默认采用严格发号模式（`strict`）：`reservationNo` 生成失败时，库存预占必须直接失败
- 发号模式必须通过配置显式选择，不得在运行时因失败自动从远端发号切换到本地发号
- 可选本地模式仅允许通过显式配置启用（例如统一 `id provider=snowflake`），且启用后应全链路保持单一 provider，不得混用号段策略

### 6.2 Reservation Rule

- 同一 `orderNo` 只能存在一个库存预占单
- 库存预占必须按 `orderNo` 幂等
- 释放和扣减必须基于已成功预占单执行
- 已释放预占不得再次扣减
- 已扣减预占不得再次释放
- 任一明细预占失败时，整单预占必须失败

### 6.3 Reservation Presence Rule

- 未成功创建预占单时，`releaseReservedStock` 不得修改库存数量，并必须返回幂等成功结果；此时 `reservationStatus` 必须为 `FAILED`，`inventoryStatus` 必须为 `FAILED`
- 未成功创建预占单时，`deductReservedStock` 不得修改库存数量，并必须返回失败结果；此时 `reservationStatus` 必须为 `FAILED`，`inventoryStatus` 必须为 `FAILED`
- `releaseReservedStock` 仅允许对 `reservationStatus=RESERVED` 的预占单执行实际释放
- `deductReservedStock` 仅允许对 `reservationStatus=RESERVED` 的预占单执行实际扣减

### 6.4 InventoryStatus Mapping Rule

- `reservationStatus=CREATED` 时，返回给 `Order` 的 `inventoryStatus` 必须为 `RESERVING`
- `reservationStatus=RESERVED` 时，返回给 `Order` 的 `inventoryStatus` 必须为 `RESERVED`
- `reservationStatus=RELEASED` 时，返回给 `Order` 的 `inventoryStatus` 必须为 `RELEASED`
- `reservationStatus=DEDUCTED` 时，返回给 `Order` 的 `inventoryStatus` 必须为 `DEDUCTED`
- `reservationStatus=FAILED` 时，返回给 `Order` 的 `inventoryStatus` 必须为 `FAILED`

### 6.5 Release Reason Rule

- `releaseReservedStock.reason` 必须使用固定枚举 `USER_CANCELLED`、`SYSTEM_CANCELLED`、`PAYMENT_CREATE_FAILED`、`PAYMENT_FAILED`、`TIMEOUT_CLOSED`
- `Inventory.releaseReason` 不得写入固定枚举之外的新值

### 6.6 Reservation Result Nullability Rule

- 预占成功时，`InventoryReservationResultDTO.failureReason` 和 `releaseReason` 必须为空
- 预占失败时，`InventoryReservationResultDTO.failureReason` 必须有值，`releaseReason` 必须为空
- 释放成功时，`InventoryReservationResultDTO.releaseReason` 必须有值，`failureReason` 必须为空
- 释放成功时，`InventoryReservationResultDTO.inventoryStatus` 必须为 `RELEASED`
- 扣减成功时，`InventoryReservationResultDTO.failureReason` 和 `releaseReason` 必须为空

### 6.7 Status Rule

- `Inventory.status=DISABLED` 的库存主数据不得参与预占
- 库存预占请求进入处理后，`reservationStatus` 必须先进入 `CREATED`
- `reservationStatus` 固定状态集为 `CREATED`、`RESERVED`、`RELEASED`、`DEDUCTED`、`FAILED`，不得新增 `PROCESSING` 等中间持久化状态
- 全部明细预占成功后，`reservationStatus` 必须进入 `RESERVED`
- 预占失败后，`reservationStatus` 必须进入 `FAILED`
- 释放成功后，`reservationStatus` 必须进入 `RELEASED`
- 扣减成功后，`reservationStatus` 必须进入 `DEDUCTED`

### 6.8 Idempotency Rule

- `reserveStock` 按 `orderNo` 幂等
- `releaseReservedStock` 按 `orderNo` 幂等
- `deductReservedStock` 按 `orderNo` 幂等
- 同一 `orderNo` 重复调用 `reserveStock` 时，若已成功预占，必须返回同一 `reservationNo`
- 同一 `orderNo` 重复调用 `releaseReservedStock` 时，若已释放，必须返回成功且不得重复回补库存
- 同一 `orderNo` 重复调用 `deductReservedStock` 时，若已扣减，必须返回成功且不得重复扣减库存

### 6.9 Transaction And Concurrency Rule

- `reserveStock`、`releaseReservedStock`、`deductReservedStock` 必须在明确事务边界内执行
- 单次库存命令必须保证 `Inventory`、`InventoryReservation`、`InventoryReservationItem` 的写入原子性
- 正式持久化实现必须对 `Inventory` 写入启用乐观锁或等价并发控制
- `InventoryReservation` 的 `(tenantNo, orderNo)` 唯一约束必须作为库存预占幂等的最终兜底
- 并发写冲突时，不得静默覆盖库存数量，必须返回明确失败或抛出可识别异常
- 并发冲突重试的退避等待不得在活动事务中执行，禁止在事务内 `Thread.sleep`
- 并发冲突重试必须在事务外层编排，每次重试尝试都必须开启新的短事务
- 并发冲突重试策略必须由统一应用层组件（如 `InventoryWriteRetrier`）承载，禁止在多个写服务重复实现重试模板
- 审计日志写入失败不得破坏库存主业务提交结果，优先采用提交后异步/延后记录
- 审计日志写入失败必须记录失败指标并触发告警日志（`ALERT` 前缀）
- 审计日志写入失败必须落库到审计 outbox，后续由补偿任务重试
- 审计 outbox 重试采用退避策略；达到最大重试次数后必须进入死信
- 多实例场景下，审计 outbox 重试必须先抢占再处理，禁止“先查后处理”的无锁并发消费
- outbox 抢占后必须记录 `processingOwner` 与 `leaseUntil`，并通过 owner + 状态 CAS 更新提交重试结果
- claim 租约超时后必须可回收为可重试状态，避免实例异常导致记录永久卡死
- 审计死信必须提供可运营处理闭环：支持分页查询、单条重放、按条件批量重放
- 审计死信批量重放应支持异步任务模式：创建任务并返回 `taskId`
- 任务状态固定为 `PENDING`、`RUNNING`、`PAUSED`、`SUCCEEDED`、`FAILED`、`CANCELED`
- 异步任务必须支持进度查询（total/processed/success/failed）与暂停/恢复
- 审计死信重放必须记录幂等 `replayKey` 与操作追踪字段（操作人类型、操作人、重放时间、重放结果）
- 审计死信重放状态固定为 `PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`
- 审计死信重放成功后必须记录补偿成功追踪日志；重放失败后必须记录补偿失败追踪日志
- 审计死信重放在 claim 成功后，必须通过独立事务原子提交“审计写入 + 死信状态迁移 + 重放追踪日志”
- 若重放事务提交失败，必须执行失败补偿（将死信状态更新为 `FAILED` 并写失败追踪日志），并记录指标与 `ALERT` 告警日志

### 6.10 Repository Mode Rule

- 库存仓储模式固定为显式配置：`strict` 或 `memory`
- 默认模式必须是 `strict`
- `strict` 模式下，若无可用持久化仓储（如 MyBatis + 数据源），应用必须启动失败（fail-fast）
- `memory` 模式仅允许在开发、测试或演练场景显式启用，不得作为生产默认策略
- 禁止因基础设施异常自动从 `strict` 切换到 `memory`

## 7. Functional Requirements

### 7.1 Inventory Management

- 维护库存主数据
- 查询库存详情
- 查询可售库存
- 分页查询必须由数据库侧执行，不得先全量拉取后在应用层分页

### 7.2 Stock Reservation

- 为订单预占库存
- 返回预占结果和预占单号

固定约束：

- 请求字段遵守 `5.4 Fixed Request Contracts`
- 预占失败时必须返回明确原因
- 单次 `reserveStock` 请求必须先按 SKU 批量读取库存，再在应用层内存完成校验与变更计划，不得对同一批 SKU 做逐条重复读取

### 7.3 Stock Release

- 释放订单预占库存

固定约束：

- 释放操作必须幂等
- 仅 `reservationStatus=RESERVED` 的预占单允许释放

### 7.4 Stock Deduction

- 扣减已预占库存

固定约束：

- 扣减操作必须幂等
- 仅 `reservationStatus=RESERVED` 的预占单允许扣减

### 7.5 Read Capability

- 为 `Order` 提供可售库存和预占单只读查询

固定约束：

- 跨域接口只读
- `DTO` 契约必须稳定
- `Order` 跨域读取只依赖库存状态和关键关联字段
- `InventoryStockDTO` 主要用于跨域库存状态读取
- `InventoryReservationDTO` 主要用于后台排障和详情查询

### 7.6 Audit Log

- 记录预占
- 记录释放
- 记录扣减
- 审计 `actionType` 必须使用统一动作码常量，固定值至少包含：`RESERVE`、`RESERVE_FAILED`、`RELEASE`、`DEDUCT`、`AUDIT_REPLAY_SUCCEEDED`、`AUDIT_REPLAY_FAILED`
- 写入失败时记录 outbox 并保留失败原因
- outbox 重试成功后删除 outbox 记录
- 超过最大重试次数必须写入死信并保留最终失败原因

## 8. Key Flows

### 8.1 Reserve Stock

1. `Order` 发起库存预占
2. `Inventory` 校验库存主数据状态和可售库存
3. `Inventory` 生成 `InventoryReservation`
4. `Inventory` 增加 `reservedQuantity`
5. `Inventory` 返回 `reservationNo` 和预占结果

### 8.2 Release Stock

1. `Order` 发起库存释放
2. `Inventory` 校验预占单状态
3. `Inventory` 减少 `reservedQuantity`
4. `Inventory` 更新 `reservationStatus=RELEASED`

### 8.3 Deduct Stock

1. `Order` 在支付成功后发起库存扣减
2. `Inventory` 校验预占单状态
3. `Inventory` 减少 `onHandQuantity` 和 `reservedQuantity`
4. `Inventory` 更新 `reservationStatus=DEDUCTED`

### 8.4 Resource Upload And Access (When Enabled)

1. 前端调用 `Inventory` 资源业务接口（如 `POST /inventory/product/{productId}/image`）
2. `Inventory` 完成认证、鉴权、可见性与业务参数校验
3. `Inventory` 调用 `Storage` 上传资源并获取 `objectId`
4. `Inventory` 回写资源字段（如 `imageObjectId`）
5. 如为替换场景，`Inventory` 先绑定新对象引用，再解除旧对象引用
6. 前端调用 `Inventory` 资源读取接口（如 `GET /inventory/product/{productId}/image`），由 `Inventory` 通过 `Storage` 派生 `imageUrl`

## 9. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-001 | Consistency | 预占、释放、扣减必须幂等 |
| NFR-002 | Inventory Safety | 任何时候不得出现负库存 |
| NFR-003 | Architecture | 必须同时支持单体和微服务装配 |
| NFR-004 | Compatibility | `Facade + DTO` 契约必须同时支持本地和远程实现 |
| NFR-005 | Auditability | 审计日志必须持久化、可检索、可追溯 |

### 9.1 Testing Baseline

- `application` 层必须覆盖：幂等、状态流转、失败原因、批量读取与重试分支
- `infra` 层必须覆盖：仓储读写、outbox 状态迁移、死信落库、远程异常翻译
- `interfaces` 层必须覆盖：`@Valid` 参数校验、分页上限约束、核心响应契约
- 并发场景必须覆盖：同 SKU 并发预占下的乐观锁冲突重试与结果一致性
- 关键链路必须至少有 1 组跨层测试：审计写失败 -> outbox -> 重试成功/死信

## 10. Open Items

- 是否支持多仓优先级分配未确认
- 库存初始化和补货来源未确认
