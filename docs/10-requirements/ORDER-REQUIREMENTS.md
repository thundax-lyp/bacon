# ORDER REQUIREMENTS

## 1. Purpose

Order 是 Bacon 的统一订单业务域。  
本文档定义 Order 模块的需求边界、实现约束和稳定契约。  
本文档用于指导设计、实现和测试。

## 2. Scope

### 2.1 In Scope

- 订单创建
- 订单详情查询
- 订单列表查询
- 订单状态流转
- 订单取消
- 订单超时关闭
- 支付结果接收
- 库存预占、释放、扣减编排
- 订单只读跨域查询
- 订单审计日志

### 2.2 Out Of Scope

- 购物车
- 营销优惠
- 运费模板
- 发货履约
- 售后退款
- 拆单
- 多币种

## 3. Bounded Context

### 3.1 Order

- `Order` 负责订单主数据、订单明细、金额快照和订单生命周期编排
- `Order` 负责调用 `Inventory` 完成库存预占、释放和扣减
- `Order` 负责调用 `Payment` 创建支付单和关闭支付单
- `Order` 负责接收 `Payment` 的支付结果通知并更新订单状态
- `Order` 不负责库存主数据维护
- `Order` 不负责支付渠道交互

### 3.2 Inventory

- `Inventory` 负责库存主数据、库存预占、库存释放、库存扣减
- `Order` 只通过 `bacon-inventory-api` 调用 `Inventory`
- 库存预占、释放、扣减的结果由 `Inventory` 同步返回给 `Order`
- `Inventory` 不负责订单状态持久化

### 3.3 Payment

- `Payment` 负责支付单、支付渠道适配和支付结果回调处理
- `Order` 只通过 `bacon-payment-api` 创建或关闭支付单
- `Payment` 只通过 `bacon-order-api` 回传支付结果
- `Payment` 不负责订单主数据维护

### 3.4 UPMS

- `UPMS` 负责下单用户身份、租户和权限信息
- `Order` 只通过 `bacon-upms-api` 读取用户和租户相关信息
- `Order` 读取用户和租户状态时，只能通过 `UserReadFacade`

### 3.5 Cross-Domain Rule

- `Order` 只能依赖 `bacon-inventory-api`、`bacon-payment-api`、`bacon-upms-api`
- `Order` 不得依赖其他业务域内部实现
- 订单创建时必须先落库 `Order` 并写入 outbox；库存预占与支付创建固定由 outbox saga 执行
- 支付结果由 `Payment` 异步通知 `Order`
- 库存预占、释放、扣减由 `Order` 同步调用 `Inventory`
- 单体模式使用本地 `Facade` 实现
- 微服务模式使用远程 `Facade` 实现，并保持同一契约

## 4. Module Mapping

### 4.1 `bacon-order-api`

- 跨域 `Facade`
- `DTO`
- 对外共享枚举

固定接口：

- `OrderReadFacade`
- `OrderCommandFacade`

`OrderReadFacade` 固定方法：

- `getById(tenantId, orderId)`，返回固定 `OrderDetailDTO`
- `getByOrderNo(tenantId, orderNo)`，返回固定 `OrderDetailDTO`
- `pageOrders(query)`，返回固定分页结果，记录项使用 `OrderSummaryDTO`

`pageOrders(query)` 的 `query` 至少包含：

- `tenantId`
- `userId`
- `orderNo`
- `orderStatus`
- `payStatus`
- `inventoryStatus`
- `createdAtFrom`
- `createdAtTo`
- `pageNo`
- `pageSize`

分页返回结果至少包含：

- `records`
- `total`
- `pageNo`
- `pageSize`

固定名称：

- 分页返回结果固定命名为 `OrderPageResultDTO`
- `OrderPageResultDTO.records` 的元素类型固定为 `OrderSummaryDTO`

`OrderSummaryDTO` 至少包含：

- `id`
- `tenantId`
- `orderNo`
- `userId`
- `orderStatus`
- `payStatus`
- `inventoryStatus`
- `paymentNo`
- `reservationNo`
- `currencyCode`
- `totalAmount`
- `payableAmount`
- `cancelReason`
- `closeReason`
- `createdAt`
- `expiredAt`

`OrderDetailDTO` 至少包含：

- 全部 `OrderSummaryDTO` 字段
- `items`
- `paymentSnapshot`
- `inventorySnapshot`
- `paidAt`
- `closedAt`

`OrderCommandFacade` 固定方法：

- `markPaid(tenantId, orderNo, paymentNo, channelCode, paidAmount, paidTime)`
- `markPaymentFailed(tenantId, orderNo, paymentNo, reason, channelStatus, failedTime)`
- `closeExpiredOrder(tenantId, orderNo, reason)`

固定约束：

- `markPaid` 只负责处理支付成功结果，并在内部触发库存扣减
- `markPaymentFailed` 只负责处理支付失败结果，并在内部触发库存释放
- `closeExpiredOrder` 只允许关闭未支付且已超时订单，并在内部触发支付关闭和库存释放
- `closeExpiredOrder` 的请求字段遵守 `CloseExpiredOrderRequest`

### 4.2 `bacon-order-interfaces`

- `Controller`
- 请求 `DTO`
- 响应 `VO`
- `Assembler`
- `Facade Local` 适配实现（固定放在 `interfaces.facade`）
- 对外适配端点

固定端点：

- `POST /orders`
- `GET /orders/{orderId}`
- `GET /orders`
- `POST /orders/{orderNo}/cancel`

### 4.3 `bacon-order-application`

固定服务：

- `OrderApplicationService`
- `OrderQueryApplicationService`
- `OrderCancelApplicationService`
- `OrderTimeoutApplicationService`
- `OrderPaymentResultApplicationService`

### 4.4 `bacon-order-domain`

- 聚合、实体、值对象
- 领域服务
- `Repository` 接口
- 领域规则和不变量

### 4.5 `bacon-order-infra`

- `MyBatis-Plus Mapper`
- 按职责拆分的 `Repository` 实现
- `Facade Remote` 适配实现（固定放在 `infra.facade.remote`）
- `InventoryFacade`、`PaymentFacade` 远程适配器
- 审计日志持久化

## 5. Core Domain Objects

- `Order`
- `OrderItem`
- `OrderAmount`
- `OrderSnapshot`
- `OrderPaymentSnapshot`
- `OrderInventorySnapshot`
- `OrderAuditLog`

## 5.1 Fixed Enums

- `orderStatus` 固定为 `CREATED`、`RESERVING_STOCK`、`PENDING_PAYMENT`、`PAID`、`CANCELLED`、`CLOSED`
- `payStatus` 固定为 `UNPAID`、`PAYING`、`PAID`、`FAILED`、`CLOSED`
- `inventoryStatus` 固定为 `UNRESERVED`、`RESERVING`、`RESERVED`、`RELEASED`、`DEDUCTED`、`FAILED`
- `cancelReason` 固定为 `USER_CANCELLED`、`SYSTEM_CANCELLED`
- `closeReason` 固定为 `INVENTORY_RESERVE_FAILED`、`PAYMENT_CREATE_FAILED`、`PAYMENT_FAILED`、`TIMEOUT_CLOSED`

## 5.2 Terminology

- `orderNo` 是订单唯一业务键，用于跨域调用、幂等控制和外部查询
- `orderId` 是订单数据库唯一键，只用于本域持久化和站内主键定位
- `Order` 是订单主聚合
- `OrderItem` 是订单明细
- `OrderAmount` 是订单金额值对象，承载 `totalAmount` 和 `payableAmount`
- `OrderSnapshot` 是订单详情聚合快照，包含主单、明细、支付快照和库存快照
- `OrderDetailDTO` 与 `OrderSnapshot` 字段一致
- `Create Order` 指创建订单、库存预占、支付单创建组成的完整下单链路
- `Payment Result Handling` 指 `Payment` 通过 `OrderCommandFacade` 回传支付结果后的订单侧处理
- `OrderPaymentSnapshot` 是订单侧支付快照，不替代 `PaymentOrder`
- `OrderInventorySnapshot` 是订单侧库存快照，不替代 `InventoryReservation`
- `订单取消` 指业务取消，不等于逻辑删除
- `订单关闭` 指超时或失败后结束支付流程的终态

## 5.3 Fixed Fields

- `Order` 至少包含 `id`、`tenantId`、`orderNo`、`userId`、`orderStatus`、`payStatus`、`inventoryStatus`、`currencyCode`、`totalAmount`、`payableAmount`、`remark`、`cancelReason`、`closeReason`、`createdAt`、`expiredAt`、`paidAt`、`closedAt`
- `OrderItem` 至少包含 `tenantId`、`orderId`、`skuId`、`skuName`、`imageUrl`、`quantity`、`salePrice`、`lineAmount`
- `OrderAmount` 至少包含 `totalAmount`、`payableAmount`、`currencyCode`
- `OrderSnapshot` 至少包含 `order`、`items`、`paymentSnapshot`、`inventorySnapshot`
- `OrderPaymentSnapshot` 至少包含 `tenantId`、`orderId`、`paymentNo`、`channelCode`、`payStatus`、`paidAmount`、`paidTime`、`failureReason`、`channelStatus`
- `OrderInventorySnapshot` 至少包含 `tenantId`、`orderNo`、`reservationNo`、`inventoryStatus`、`warehouseNo`、`failureReason`
- `OrderAuditLog` 至少包含 `id`、`tenantId`、`orderNo`、`actionType`、`beforeStatus`、`afterStatus`、`operatorType`、`operatorId`、`occurredAt`
- `OrderOutboxEvent` 至少包含 `id`、`eventCode`、`tenantId`、`orderNo`、`eventType`、`businessKey`、`payload`、`status`、`retryCount`、`createdAt`、`updatedAt`
- `OrderOutboxDeadLetter` 至少包含 `outboxId`、`eventCode`、`tenantId`、`orderNo`、`eventType`、`businessKey`、`payload`、`retryCount`、`deadReason`、`deadAt`、`replayStatus`、`replayCount`

### 5.3.1 Snapshot Mapping Rule

- `OrderSummaryDTO.paymentNo` 来自 `OrderPaymentSnapshot.paymentNo`
- `OrderSummaryDTO.reservationNo` 来自 `OrderInventorySnapshot.reservationNo`
- `OrderPaymentSnapshot.channelCode`、`paidAmount`、`paidTime` 来自 `Payment`
- `OrderPaymentSnapshot.failureReason`、`channelStatus` 来自支付失败结果
- `OrderInventorySnapshot.warehouseNo`、`failureReason` 来自 `Inventory`
- `OrderInventorySnapshot.inventoryStatus` 来自 `InventoryReservationResultDTO.inventoryStatus`

固定约束：

- `Order.currencyCode` 使用 `CurrencyCode`
- `OrderPaymentSnapshot.tenantId` 使用 `TenantId`
- `OrderPaymentSnapshot.orderId` 使用 `OrderId`
- `OrderPaymentSnapshot.paymentNo` 使用 `PaymentNo`
- `OrderPaymentSnapshot.channelCode` 使用 `PaymentChannel`
- `OrderPaymentSnapshot.payStatus` 使用 `PayStatus`
- `OrderPaymentSnapshot.paidAmount` 使用 `Money`
- `OrderPaymentSnapshot.channelStatus` 使用 `PaymentChannelStatus`

## 5.4 Fixed Request Contracts

- `CreateOrderRequest` 至少包含 `tenantId`、`userId`、`currencyCode`、`channelCode`、`items`、`remark`
- `CreateOrderRequest.items` 至少包含 `skuId`、`skuName`、`imageUrl`、`quantity`、`salePrice`
- `CancelOrderRequest` 至少包含 `tenantId`、`orderNo`、`reason`、`operatorType`、`operatorId`
- `CloseExpiredOrderRequest` 至少包含 `tenantId`、`orderNo`、`reason`
- `OrderPageQuery` 至少包含 `tenantId`、`userId`、`orderNo`、`orderStatus`、`payStatus`、`inventoryStatus`、`createdAtFrom`、`createdAtTo`、`pageNo`、`pageSize`

固定约束：

- `CancelOrderRequest.reason` 的值域遵守 `6.4 Terminal Reason Rule` 中的 `cancelReason`
- `CloseExpiredOrderRequest.reason` 必须固定为 `TIMEOUT_CLOSED`

## 5.5 Uniqueness And Index Rules

- `Order.id` 全局唯一
- `Order.orderNo` 全局唯一
- `OrderPaymentSnapshot.paymentNo` 全局唯一
- `OrderInventorySnapshot.reservationNo` 全局唯一
- `Order` 必须建立 `(tenantId, userId, createdAt)` 索引
- `Order` 必须建立 `(tenantId, orderStatus, createdAt)` 索引
- `Order` 必须建立 `(tenantId, expiredAt, orderStatus)` 索引
- `OrderItem` 必须建立 `(tenantId, orderId)` 索引

## 6. Global Constraints

### 6.1 Tenant And Identity

- `Order` 必须带 `tenantId`
- `Order` 必须带 `userId`
- 不允许跨租户访问订单
- `orderNo` 是跨域调用唯一业务键
- `orderId` 不得用于跨域 `Facade` 入参与幂等键

### 6.1.1 Numbering Rule

- `orderNo` 必须由 `Order` 模块内部生成，外部请求不得自带订单号
- `orderNo` 生成失败时，创建订单必须直接失败，不得降级为本地雪花号、时间戳拼接、数据库 `max(id)+1` 或其他临时发号

### 6.2 Amount Rule

- 金额字段使用两位小数语义
- `Order.totalAmount` 必须等于全部 `OrderItem.lineAmount` 之和
- `Order.payableAmount` 固定等于 `Order.totalAmount`
- 不支持优惠抵扣和运费追加

### 6.3 Status Rule

- 订单创建落库后，初始状态固定为 `orderStatus=CREATED`、`payStatus=UNPAID`、`inventoryStatus=UNRESERVED`
- 发起库存预占前，`orderStatus` 必须进入 `RESERVING_STOCK`，`inventoryStatus` 必须进入 `RESERVING`
- 库存预占成功后，`inventoryStatus` 必须进入 `RESERVED`
- 库存预占失败后，`inventoryStatus` 必须进入 `FAILED`，`orderStatus` 必须进入 `CLOSED`
- 支付单创建成功后，`payStatus` 必须进入 `PAYING`，`orderStatus` 必须进入 `PENDING_PAYMENT`
- 支付单创建失败后，`orderStatus` 必须进入 `CLOSED`，`payStatus` 保持 `UNPAID`，已预占库存必须释放
- 支付成功后，`payStatus` 必须进入 `PAID`，`orderStatus` 必须进入 `PAID`
- 支付成功后的库存扣减成功后，`inventoryStatus` 必须进入 `DEDUCTED`
- 支付失败后，`payStatus` 必须进入 `FAILED`，`orderStatus` 必须进入 `CLOSED`，已预占库存必须释放
- 订单取消后，`orderStatus` 必须进入 `CANCELLED`，`payStatus` 必须进入 `CLOSED`
- 订单取消后的库存释放成功后，`inventoryStatus` 必须进入 `RELEASED`
- 订单超时关闭后，`orderStatus` 必须进入 `CLOSED`，`payStatus` 必须进入 `CLOSED`
- `PAID`、`CANCELLED`、`CLOSED` 订单不得重新进入可支付状态

### 6.4 Terminal Reason Rule

- `orderStatus=CANCELLED` 时，`cancelReason` 必须有值
- 用户主动取消时，`cancelReason` 必须固定为 `USER_CANCELLED`
- 系统自动取消时，`cancelReason` 必须固定为 `SYSTEM_CANCELLED`
- `orderStatus=CLOSED` 时，`closeReason` 必须有值
- 库存预占失败关闭时，`closeReason` 必须固定为 `INVENTORY_RESERVE_FAILED`
- 支付单创建失败关闭时，`closeReason` 必须固定为 `PAYMENT_CREATE_FAILED`
- 支付失败关闭时，`closeReason` 必须固定为 `PAYMENT_FAILED`
- 超时关闭时，`closeReason` 必须固定为 `TIMEOUT_CLOSED`
- 正常支付完成的订单不得写入 `cancelReason` 或 `closeReason`

### 6.5 Cross-Domain Coordination Rule

- `Order` 创建完成后，必须立即写入库存预占 outbox 事件，再由 saga 执行器调用 `InventoryCommandFacade.reserveStock`
- 只有库存预占成功，`Order` 才能由 saga 执行器调用 `PaymentCommandFacade.createPayment`
- 库存预占失败时，不得创建支付单
- 支付单创建失败时，`Order` 必须调用 `InventoryCommandFacade.releaseReservedStock`
- 订单取消时，如库存已预占且未扣减，必须调用 `InventoryCommandFacade.releaseReservedStock`
- 订单取消时，如支付单已创建且未支付，必须调用 `PaymentCommandFacade.closePayment`
- 未创建支付单时，取消和超时关闭不得调用 `PaymentCommandFacade.closePayment`，并视为支付关闭步骤已完成
- `PaymentCommandFacade.closePayment.reason` 只允许使用 `USER_CANCELLED`、`SYSTEM_CANCELLED`、`TIMEOUT_CLOSED`
- 支付失败时，如库存已预占且未扣减，`Order` 必须调用 `InventoryCommandFacade.releaseReservedStock`
- 支付成功后，`Order` 必须调用 `InventoryCommandFacade.deductReservedStock`
- 库存扣减失败不得回滚已确认的支付成功
- `Payment` 只允许通过 `OrderCommandFacade` 通知支付结果，不允许直接修改 `Order` 表

### 6.6 Idempotency Rule

- 创建订单按 `orderNo` 幂等
- 取消订单按 `tenantId + orderNo + paymentNo + eventType` 幂等
- 超时关闭按 `tenantId + orderNo + paymentNo + eventType` 幂等
- `markPaid` 按 `tenantId + orderNo + paymentNo + eventType` 幂等
- `markPaymentFailed` 按 `tenantId + orderNo + paymentNo + eventType` 幂等
- 非支付事件（取消、超时关闭）的 `paymentNo` 固定使用空字符串参与幂等键计算
- 幂等记录 `PROCESSING` 状态必须带租约字段 `processingOwner + leaseUntil`
- 命令请求遇到未过期 `PROCESSING` 记录时可短路返回；遇到已过期记录时必须先抢占租约后再执行
- 系统必须提供过期 `PROCESSING` 回收任务，避免僵尸记录长期卡死
- 同一订单只允许绑定一个 `paymentNo`
- 同一订单只允许绑定一个 `reservationNo`

### 6.7 Retry Rule

- `PaymentCommandFacade.closePayment` 返回 `PaymentCloseResultDTO.closeResult=FAILED` 时，`Order` 必须进入支付关闭重试机制
- 支付关闭重试期间，`Order` 不得直接进入 `CANCELLED` 或 `CLOSED` 终态，必须保留原状态直到支付关闭成功
- 库存扣减失败时，`Order` 不得回滚已确认的支付成功，必须保留 `inventoryStatus=RESERVED` 并进入重试机制，直到扣减成功
- 重试机制属于 `Order` 内部实现能力，不改变跨域 `Facade` 契约

### 6.8 Snapshot Nullability Rule

- 未成功创建支付单前，`paymentNo` 允许为空，`paymentSnapshot` 必须为空
- 未成功创建库存预占前，`reservationNo` 允许为空，`inventorySnapshot` 必须为空
- 库存预占失败后，`inventorySnapshot` 必须保留失败结果摘要
- 支付单创建失败后，`paymentSnapshot` 允许为空
- 支付失败后，`paymentSnapshot` 必须保留失败结果摘要
- 支付成功后，`paymentSnapshot.failureReason` 必须为空
- 支付成功后，`paymentSnapshot.channelStatus` 必须为支付成功对应渠道状态

### 6.9 Delete Strategy

- 订单和订单明细不支持物理删除
- 已关闭和已取消订单默认可查询

### 6.10 Repository Mode Rule

- `Order` 仓储模式固定为 `strict` 与 `memory` 两种
- 默认模式固定为 `strict`
- `strict` 模式下必须启用 DB 持久化仓储；若缺失持久化仓储 Bean，服务必须启动失败（fail-fast）
- `memory` 模式仅允许用于测试环境（`test` profile）
- 非测试环境禁止启用 `memory` 模式

### 6.11 Outbox Saga Rule

- 跨域编排固定采用“本地状态事务 + Outbox 事件驱动补偿”模型
- `Order` 在发起库存预占、支付创建、库存释放等跨域动作前，必须先落库对应 outbox 事件
- outbox 事件固定状态：`NEW`、`RETRYING`、`PROCESSING`、`DEAD`
- 重试必须采用 claim + lease 机制，确保同一事件同一时刻只被一个实例处理
- 重试耗尽后必须进入 dead-letter，并提供后续重放能力
- outbox 事件必须携带幂等业务键 `businessKey`，并在数据库层做唯一约束
- 允许同步“立即尝试”，但同步失败不得丢弃事件，必须由 outbox 重试接管
- `OrderOutboxEvent.eventCode` 使用 `EventCode`，作为事件业务标识
- `OrderOutboxEvent.tenantId` 使用 `TenantId`
- `OrderOutboxEvent.orderNo` 使用 `OrderNo`
- `OrderOutboxEvent.eventType` 使用 `OrderOutboxEventType`
- `OrderOutboxEvent.status` 使用 `OrderOutboxStatus`
- `OrderOutboxDeadLetter.eventCode` 使用 `EventCode`
- `OrderOutboxDeadLetter.tenantId` 使用 `TenantId`
- `OrderOutboxDeadLetter.orderNo` 使用 `OrderNo`
- `OrderOutboxDeadLetter.eventType` 使用 `OrderOutboxEventType`
- `OrderOutboxDeadLetter.replayStatus` 使用 `OrderOutboxReplayStatus`

## 7. Functional Requirements

### 7.1 Create Order

- 创建 `Order`
- 创建 `OrderItem`
- 校验 `orderNo` 唯一性
- 发起库存预占
- 在库存预占成功后发起支付单创建

固定约束：

- 请求字段遵守 `5.4 Fixed Request Contracts`
- 订单必须至少包含一个 `OrderItem`
- 若库存预占失败，创建结果必须明确返回失败原因，且不得创建支付单
- 若支付单创建失败，必须关闭订单并释放已预占库存

### 7.2 Query Order

- 查询订单详情
- 分页查询订单列表
- 查询订单当前状态

固定约束：

- 详情查询通过 `getById(tenantId, orderId)` 或 `getByOrderNo(tenantId, orderNo)` 执行
- 列表查询请求遵守 `OrderPageQuery`
- 分页结果字段遵守 `OrderPageResultDTO`
- 分页查询必须下推到 `OrderRepository`，禁止应用层全量拉取后内存过滤
- 分页排序固定为 `createdAt desc, id desc`，确保翻页稳定
- `pageSize` 必须遵守通用分页上限约束

### 7.3 Cancel Order

- 用户或系统取消订单
- 订单取消后释放已预占库存
- 订单取消后关闭未完成支付

固定约束：

- 请求字段遵守 `5.4 Fixed Request Contracts`
- 已支付订单不得取消
- 已取消订单重复取消不得产生脏数据
- 已关闭订单重复取消不得改变终态

### 7.4 Close Expired Order

- 关闭已到期且未支付订单
- 关闭后释放已预占库存
- 关闭后关闭未完成支付单

固定约束：

- 仅 `PENDING_PAYMENT` 且 `expiredAt` 已到达的订单允许超时关闭
- 超时关闭和手动取消必须复用同一状态约束

### 7.5 Handle Payment Result

- 接收支付成功结果
- 接收支付失败结果
- 更新订单支付快照和订单状态
- 在支付成功后触发库存扣减

固定约束：

- 支付结果处理必须幂等
- `paidAmount` 必须等于 `payableAmount`
- 未找到订单或 `paymentNo` 不匹配时必须拒绝处理
- 支付失败结果至少包含 `reason`、`channelStatus`、`failedTime`
- 支付失败处理后，必须释放已预占且未扣减库存

### 7.6 Read Capability

- 为其他业务域提供订单只读查询

固定约束：

- 跨域接口只读
- `DTO` 契约必须稳定

### 7.7 Audit Log

- 记录订单创建
- 记录订单取消
- 记录订单超时关闭
- 记录支付结果变更
- 记录库存预占和扣减结果变更

## 8. Key Flows

### 8.1 Order Creation

1. 客户端提交下单请求
2. `Order` 创建订单和订单明细，状态初始化为 `CREATED`
3. `Order` 调用 `InventoryCommandFacade.reserveStock`
4. 库存预占成功后，`Order` 更新 `inventoryStatus=RESERVED`
5. `Order` 调用 `PaymentCommandFacade.createPayment`
6. 支付单创建成功后，`Order` 更新 `orderStatus=PENDING_PAYMENT`、`payStatus=PAYING`

### 8.2 Order Creation With Inventory Failure

1. 客户端提交下单请求
2. `Order` 创建订单和订单明细
3. `Order` 调用 `InventoryCommandFacade.reserveStock`
4. `Inventory` 返回预占失败
5. `Order` 更新 `inventoryStatus=FAILED`、`orderStatus=CLOSED`
6. `Order` 返回下单失败结果

### 8.3 Order Creation With Payment Failure

1. 客户端提交下单请求
2. `Order` 创建订单和订单明细
3. `Order` 调用 `InventoryCommandFacade.reserveStock`
4. `Inventory` 返回预占成功
5. `Order` 调用 `PaymentCommandFacade.createPayment`
6. `Payment` 返回创建失败
7. `Order` 调用 `InventoryCommandFacade.releaseReservedStock`
8. `Order` 更新 `orderStatus=CLOSED`、`payStatus=UNPAID`、`inventoryStatus=RELEASED`

### 8.4 Payment Success

1. 支付渠道回调 `Payment`
2. `Payment` 完成支付回调幂等处理并更新支付单为 `PAID`
3. `Payment` 调用 `OrderCommandFacade.markPaid`
4. `Order` 更新 `payStatus=PAID`、`orderStatus=PAID`
5. `Order` 调用 `InventoryCommandFacade.deductReservedStock`
6. 扣减成功后，`Order` 更新 `inventoryStatus=DEDUCTED`

### 8.5 Payment Failure

1. 支付渠道回调 `Payment`
2. `Payment` 完成支付回调幂等处理并更新支付单为 `FAILED`
3. `Payment` 调用 `OrderCommandFacade.markPaymentFailed`
4. `Order` 更新 `payStatus=FAILED`、`orderStatus=CLOSED`
5. `Order` 调用 `InventoryCommandFacade.releaseReservedStock`
6. 释放成功后，`Order` 更新 `inventoryStatus=RELEASED`

### 8.6 Order Cancel

1. 用户或系统发起取消
2. `Order` 校验订单当前状态
3. `Order` 调用 `PaymentCommandFacade.closePayment`
4. `Payment` 返回关闭成功
5. `Order` 调用 `InventoryCommandFacade.releaseReservedStock`
6. `Order` 更新 `orderStatus=CANCELLED`、`payStatus=CLOSED`、`inventoryStatus=RELEASED`

## 9. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-001 | Consistency | 支付结果、取消、超时关闭必须幂等 |
| NFR-002 | Architecture | 必须同时支持单体和微服务装配 |
| NFR-003 | Maintainability | 实现必须严格遵守 `interfaces -> application -> domain -> infra` 分层 |
| NFR-004 | Compatibility | `Facade + DTO` 契约必须同时支持本地和远程实现 |
| NFR-005 | Auditability | 审计日志必须持久化、可检索、可追溯 |
