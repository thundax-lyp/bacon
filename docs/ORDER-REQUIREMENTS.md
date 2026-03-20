# ORDER REQUIREMENTS

## 1. Purpose

Order 是 Bacon 的统一订单业务域。  
本文档定义 Order 模块的需求边界、实现约束和稳定契约。  
本文档是后续设计、任务拆解、实现和测试的唯一基线。  
当前范围内全部功能属于同一交付范围，不做分期交付。

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

### 3.5 Cross-Domain Rule

- `Order` 只能依赖 `bacon-inventory-api`、`bacon-payment-api`、`bacon-upms-api`
- `Order` 不得依赖其他业务域内部实现
- 订单创建时必须先落库 `Order`，再发起库存预占；库存预占成功后才能创建支付单
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

- `getById(tenantId, orderId)`，返回固定 `DTO`
- `getByOrderNo(tenantId, orderNo)`，返回固定 `DTO`
- `pageOrders(query)`，返回固定分页结果

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

`OrderReadFacade` 返回值至少包含：

- `id`
- `tenantId`
- `orderNo`
- `userId`
- `orderStatus`
- `payStatus`
- `inventoryStatus`
- `currencyCode`
- `totalAmount`
- `payableAmount`
- `cancelReason`
- `closeReason`
- `createdAt`
- `expiredAt`

`OrderCommandFacade` 固定方法：

- `markPaid(tenantId, orderNo, paymentNo, channelCode, paidAmount, paidTime)`
- `markPaymentFailed(tenantId, orderNo, paymentNo, reason, failedTime)`
- `closeExpiredOrder(tenantId, orderNo, reason)`

固定约束：

- `markPaid` 只负责处理支付成功结果，并在内部触发库存扣减
- `markPaymentFailed` 只负责处理支付失败结果，并在内部触发库存释放
- `closeExpiredOrder` 只允许关闭未支付且已超时订单，并在内部触发支付关闭和库存释放

### 4.2 `bacon-order-interfaces`

- `Controller`
- 请求 `DTO`
- 响应 `VO`
- `Assembler`
- 对外适配端点

固定端点：

- `POST /orders`
- `GET /orders/{orderId}`
- `GET /orders`
- `POST /orders/{orderId}/cancel`

### 4.3 `bacon-order-application`

固定服务：

- `OrderApplicationService`
- `OrderQueryService`
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
- `Repository` 实现
- `InventoryFacade` 远程适配器
- `PaymentFacade` 远程适配器
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

## 5.2 Terminology

- `Order` 是订单主聚合
- `OrderItem` 是订单明细
- `OrderPaymentSnapshot` 是订单侧支付快照，不替代 `PaymentOrder`
- `OrderInventorySnapshot` 是订单侧库存快照，不替代 `InventoryReservation`
- `订单取消` 指业务取消，不等于逻辑删除
- `订单关闭` 指超时或失败后结束支付流程的终态

## 5.3 Fixed Fields

- `Order` 至少包含 `id`、`tenantId`、`orderNo`、`userId`、`orderStatus`、`payStatus`、`inventoryStatus`、`currencyCode`、`totalAmount`、`payableAmount`、`remark`、`cancelReason`、`closeReason`、`createdAt`、`expiredAt`、`paidAt`、`closedAt`
- `OrderItem` 至少包含 `id`、`tenantId`、`orderId`、`skuId`、`skuName`、`quantity`、`salePrice`、`lineAmount`
- `OrderPaymentSnapshot` 至少包含 `orderId`、`paymentNo`、`channelCode`、`payStatus`、`paidAmount`、`paidTime`、`failureReason`
- `OrderInventorySnapshot` 至少包含 `orderId`、`reservationNo`、`inventoryStatus`、`warehouseId`、`failureReason`
- `OrderAuditLog` 至少包含 `id`、`tenantId`、`orderNo`、`actionType`、`beforeStatus`、`afterStatus`、`operatorType`、`operatorId`、`occurredAt`

## 5.4 Uniqueness And Index Rules

- `Order.id` 全局唯一
- `Order.orderNo` 全局唯一
- `OrderItem.id` 全局唯一
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

### 6.2 Amount Rule

- 金额字段使用两位小数语义
- `Order.totalAmount` 必须等于全部 `OrderItem.lineAmount` 之和
- `Order.payableAmount` 当前固定等于 `Order.totalAmount`
- 当前范围内不支持优惠抵扣和运费追加

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

### 6.4 Cross-Domain Coordination Rule

- `Order` 创建完成后，必须立即调用 `InventoryCommandFacade.reserveStock`
- 只有库存预占成功，`Order` 才能调用 `PaymentCommandFacade.createPayment`
- 库存预占失败时，不得创建支付单
- 支付单创建失败时，`Order` 必须调用 `InventoryCommandFacade.releaseReservedStock`
- 订单取消时，如库存已预占且未扣减，必须调用 `InventoryCommandFacade.releaseReservedStock`
- 订单取消时，如支付单已创建且未支付，必须调用 `PaymentCommandFacade.closePayment`
- 支付失败时，如库存已预占且未扣减，`Order` 必须调用 `InventoryCommandFacade.releaseReservedStock`
- 支付成功后，`Order` 必须调用 `InventoryCommandFacade.deductReservedStock`
- 库存扣减失败不得回滚已确认的支付成功；`Order` 必须保留 `inventoryStatus=RESERVED` 并进入重试机制，直到扣减成功
- `Payment` 只允许通过 `OrderCommandFacade` 通知支付结果，不允许直接修改 `Order` 表

### 6.5 Idempotency Rule

- 创建订单按 `orderNo` 幂等
- 取消订单按 `orderNo` 幂等
- 超时关闭按 `orderNo` 幂等
- `markPaid` 按 `paymentNo` 幂等
- `markPaymentFailed` 按 `paymentNo` 幂等
- 同一订单只允许绑定一个 `paymentNo`
- 同一订单只允许绑定一个 `reservationNo`

### 6.6 Delete Strategy

- 订单和订单明细不支持物理删除
- 已关闭和已取消订单默认可查询

## 7. Functional Requirements

### 7.1 Create Order

- 创建 `Order`
- 创建 `OrderItem`
- 校验 `orderNo` 唯一性
- 发起库存预占
- 在库存预占成功后发起支付单创建

补充约束：

- 创建请求至少包含 `orderNo`、`userId`、`items`、`currencyCode`
- `items` 至少包含 `skuId`、`skuName`、`quantity`、`salePrice`
- 订单必须至少包含一个 `OrderItem`
- 若库存预占失败，创建结果必须明确返回失败原因，且不得创建支付单
- 若支付单创建失败，必须关闭订单并释放已预占库存

### 7.2 Query Order

- 查询订单详情
- 分页查询订单列表
- 查询订单当前状态

补充约束：

- 列表查询至少支持按 `orderNo`、`userId`、`orderStatus`、`payStatus`、创建时间范围过滤

### 7.3 Cancel Order

- 用户或系统取消订单
- 订单取消后释放已预占库存
- 订单取消后关闭未完成支付

补充约束：

- 已支付订单不得取消
- 已取消订单重复取消不得产生脏数据
- 已关闭订单重复取消不得改变终态

### 7.4 Close Expired Order

- 关闭已到期且未支付订单
- 关闭后释放已预占库存
- 关闭后关闭未完成支付单

补充约束：

- 仅 `PENDING_PAYMENT` 且 `expiredAt` 已到达的订单允许超时关闭
- 超时关闭和手动取消必须复用同一状态约束

### 7.5 Handle Payment Result

- 接收支付成功结果
- 接收支付失败结果
- 更新订单支付快照和订单状态
- 在支付成功后触发库存扣减

补充约束：

- 支付结果处理必须幂等
- `paidAmount` 必须等于 `payableAmount`
- 未找到订单或 `paymentNo` 不匹配时必须拒绝处理
- 支付失败处理后，必须释放已预占且未扣减库存

### 7.6 Read Capability

- 为其他业务域提供订单只读查询

补充约束：

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
4. `Order` 调用 `InventoryCommandFacade.releaseReservedStock`
5. `Order` 更新 `orderStatus=CANCELLED`、`payStatus=CLOSED`、`inventoryStatus=RELEASED`

## 9. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-001 | Consistency | 支付结果、取消、超时关闭必须幂等 |
| NFR-002 | Architecture | 必须同时支持单体和微服务装配 |
| NFR-003 | Maintainability | 实现必须严格遵守 `interfaces -> application -> domain -> infra` 分层 |
| NFR-004 | Compatibility | `Facade + DTO` 契约必须同时支持本地和远程实现 |
| NFR-005 | Auditability | 审计日志必须持久化、可检索、可追溯 |

## 10. Open Items

- 订单是否需要支持收货地址快照未确认
- 订单超时关闭时长未确认
- 订单号生成规则未确认
