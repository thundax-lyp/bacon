# PRODUCT REQUIREMENTS

## 1. Purpose

`Product` 是 Bacon 的商品业务域。  
本文档定义 Product 模块的需求边界、核心对象、跨域契约、一致性规则和测试要求。  
本文档用于指导设计、实现和测试。

## 2. Scope

### 2.1 In Scope

- 商品 `SPU` 主数据管理
- 商品 `SKU` 主数据管理
- 商品分类管理
- 商品图片引用管理
- 商品销售价管理
- 商品上下架与归档
- 商品快照创建
- 商品搜索与筛选
- 商品 MySQL 到 Elasticsearch 查询投影同步
- 商品命令幂等
- 商品只读跨域 `Facade`
- 面向订单的最小商品快照 `Facade`

### 2.2 Out Of Scope

- 库存数量、预占、扣减和释放
- 支付、退款和资金流
- 订单生命周期
- 文件上传和对象存储
- 优惠券、活动价、会员价、阶梯价
- 多店铺、多仓、多渠道价格
- 商品评价、问答、推荐
- 第一版不改造既有 `Order`、`Inventory`、`Payment`、`Storage` 主链路

## 3. Bounded Context

### 3.1 Product Owns

- `ProductSpu`
- `ProductSku`
- `ProductCategory`
- `ProductImage`
- `ProductSnapshot`
- `ProductArchive`
- `ProductIdempotencyRecord`
- `ProductOutbox`
- `ProductSearchDocument`

### 3.2 Product Does

- 维护商品、SKU、分类、销售价、图片引用和状态
- 以 `SPU` 作为对外商品聚合
- 以 `version` 表达 `SPU` 聚合的顺序版本
- 通过 MySQL 保存商品事实数据
- 通过 Elasticsearch 承载商品搜索和列表查询投影
- 通过 outbox 保证 MySQL 到 Elasticsearch 的最终一致
- 为订单创建提供商品快照能力

### 3.3 Product Does Not Do

- 不拥有库存事实
- 不保存文件内容
- 不修改订单、库存、支付主数据
- 不直接依赖其他业务域的 `application` 或 `infra`
- 不把 Elasticsearch 作为商品事实来源

### 3.4 Cross-Domain Rule

- `Product` 对外只暴露 `bacon-product-api`
- 其他域只允许依赖 `product-api` 的稳定 `Facade`
- `Product` 可以依赖 `storage-api` 查询对象引用信息，不依赖 `storage` 内部实现
- `Order` 后续接入商品快照时，只通过 `ProductCommandFacade` 创建快照
- `Inventory` 继续拥有库存数量、预占、扣减和释放
- 第一版商品域实现不得要求同步改造 `Order`、`Inventory`、`Payment`、`Storage`

## 4. Module Mapping

### 4.1 `bacon-product-api`

职责：

- 跨域 `Facade`
- `DTO`
- 对外共享枚举

固定接口：

- `ProductReadFacade`
- `ProductCommandFacade`

`ProductReadFacade` 固定方法：

- `getSkuSaleInfo(tenantId, skuId)`，返回固定 `ProductSkuSaleInfoDTO`

`ProductSkuSaleInfoDTO` 至少包含：

- `tenantId`
- `spuId`
- `spuCode`
- `spuName`
- `skuId`
- `skuCode`
- `skuName`
- `categoryId`
- `categoryName`
- `specAttributes`
- `salePrice`
- `mainImageObjectId`
- `productStatus`
- `skuStatus`
- `productVersion`
- `saleable`
- `failureReason`

`ProductCommandFacade` 固定方法：

- `createOrderProductSnapshot(tenantId, orderNo, orderItemNo, skuId, quantity)`，返回固定 `ProductSnapshotDTO`

`ProductSnapshotDTO` 至少包含：

- `tenantId`
- `snapshotId`
- `orderNo`
- `orderItemNo`
- `spuId`
- `spuCode`
- `spuName`
- `skuId`
- `skuCode`
- `skuName`
- `categoryId`
- `categoryName`
- `specAttributes`
- `salePrice`
- `quantity`
- `mainImageObjectId`
- `productVersion`
- `createdAt`

### 4.2 `bacon-product-interfaces`

职责：

- 管理端 `Controller`
- 内部 `Provider`
- 本地 `Facade` 适配
- 请求模型与响应模型转换

管理端接口：

- `POST /products/categories`
- `PUT /products/categories/{categoryId}`
- `PUT /products/categories/{categoryId}/status`
- `GET /products/categories`
- `POST /products`
- `PUT /products/{spuId}`
- `PUT /products/{spuId}/status`
- `POST /products/{spuId}/archive`
- `GET /products/{spuId}`
- `GET /products`
- `GET /products/search`
- `POST /products/{spuId}/reindex`

内部 `Provider` 接口：

- `GET /providers/products/skus/{skuId}/sale-info`
- `POST /providers/products/snapshots/order-items`

### 4.3 `bacon-product-application`

职责：

- 商品创建、编辑、上下架、归档用例编排
- 分类管理用例编排
- 商品图片引用用例编排
- 商品快照创建用例编排
- 商品命令幂等
- 商品版本推进
- MySQL 事务与 outbox 写入
- Elasticsearch 查询投影同步调度

固定服务：

- `ProductManagementApplicationService`
- `ProductCategoryApplicationService`
- `ProductSnapshotApplicationService`
- `ProductSearchApplicationService`
- `ProductIndexSyncApplicationService`
- `ProductIdempotencyExecutor`

### 4.4 `bacon-product-domain`

职责：

- 商品聚合、实体、值对象、枚举
- 商品状态规则
- 商品版本规则
- 商品快照规则
- 仓储接口

### 4.5 `bacon-product-infra`

职责：

- `MyBatis-Plus Mapper`
- `DO`
- `Repository` 实现
- Elasticsearch document 和 gateway
- outbox 持久化与消费实现
- 远程 `Facade` 适配

## 5. Core Domain Objects

- `ProductSpu`
- `ProductSku`
- `ProductCategory`
- `ProductImage`
- `ProductSnapshot`
- `ProductArchive`
- `ProductIdempotencyRecord`
- `ProductOutbox`
- `ProductSearchDocument`

## 5.1 Fixed Enums

- `productStatus` 固定为 `DRAFT`、`ON_SALE`、`OFF_SALE`、`ARCHIVED`
- `skuStatus` 固定为 `ENABLED`、`DISABLED`
- `categoryStatus` 固定为 `ENABLED`、`DISABLED`
- `imageType` 固定为 `MAIN`、`GALLERY`、`DETAIL`
- `archiveType` 固定为 `CREATE`、`UPDATE_BASE`、`UPDATE_SKU`、`UPDATE_IMAGE`、`STATUS_CHANGE`、`ARCHIVE`
- `idempotencyStatus` 固定为 `PROCESSING`、`SUCCESS`、`FAILED`
- `outboxStatus` 固定为 `PENDING`、`PROCESSING`、`SUCCEEDED`、`FAILED`、`DEAD`
- `outboxEventType` 固定为 `PRODUCT_CREATED`、`PRODUCT_UPDATED`、`PRODUCT_STATUS_CHANGED`、`PRODUCT_ARCHIVED`

## 5.2 Terminology

- `Product` 是对外商品聚合视图
- `SPU` 是商品主对象
- `SKU` 是具体可售规格单元
- `ProductCategory` 是商品域内置分类
- `ProductImage` 是商品图片对象引用
- `ProductSnapshot` 是订单商品明细维度的商品快照
- `ProductArchive` 是商品聚合历史版本归档
- `ProductSearchDocument` 是 Elasticsearch 查询投影，不是商品事实来源
- `productVersion` 是每个 `SPU` 内顺序递增的聚合版本
- `OrderItemNo` 是订单明细业务行号，第一版商品域假设该业务键已存在；订单域补充该字段属于后续低优先级任务

## 5.3 Fixed Fields

- `ProductSpu` 至少包含 `spuId`、`tenantId`、`spuCode`、`spuName`、`categoryId`、`description`、`mainImageObjectId`、`productStatus`、`version`
- `ProductSku` 至少包含 `skuId`、`tenantId`、`spuId`、`skuCode`、`skuName`、`specAttributes`、`salePrice`、`skuStatus`
- `ProductCategory` 至少包含 `categoryId`、`tenantId`、`parentId`、`categoryCode`、`categoryName`、`sortOrder`、`categoryStatus`
- `ProductImage` 至少包含 `imageId`、`tenantId`、`spuId`、`skuId`、`objectId`、`imageType`、`sortOrder`
- `ProductSnapshot` 至少包含 `snapshotId`、`tenantId`、`orderNo`、`orderItemNo`、`spuId`、`spuCode`、`spuName`、`skuId`、`skuCode`、`skuName`、`categoryId`、`categoryName`、`specAttributes`、`salePrice`、`quantity`、`mainImageObjectId`、`productVersion`
- `ProductArchive` 至少包含 `archiveId`、`tenantId`、`spuId`、`productVersion`、`archiveType`、`archiveContent`、`archivedAt`
- `ProductIdempotencyRecord` 至少包含 `idempotencyId`、`tenantId`、`operationType`、`idempotencyKey`、`requestHash`、`resultRefType`、`resultRefId`、`resultPayload`、`idempotencyStatus`
- `ProductOutbox` 至少包含 `eventId`、`tenantId`、`aggregateId`、`aggregateType`、`eventType`、`productVersion`、`payload`、`outboxStatus`、`retryCount`、`nextRetryAt`、`processingOwner`、`leaseUntil`

## 5.4 Fixed Request Contracts

- 创建商品请求必须包含 `spuName`、`categoryId`、至少一个 `SKU`
- 创建商品请求可以包含 `spuCode`；若为空，由 Product 域生成
- 创建或编辑商品请求中的 `SKU` 必须包含 `skuName`、`specAttributes`、`salePrice`
- 编辑商品请求必须包含 `expectedVersion`
- 创建商品和编辑商品请求必须包含 `idempotencyKey`
- 创建订单商品快照请求必须包含 `orderNo`、`orderItemNo`、`skuId`、`quantity`

## 5.5 Fixed Response Contracts

- 商品管理命令返回模型必须包含 `spuId`、`spuCode`、`productStatus`、`productVersion`
- 商品详情返回模型必须包含 `SPU` 主信息、`SKU` 列表、分类信息和图片列表
- 商品搜索返回模型必须来自 `ProductSearchDocument`
- 商品快照返回模型必须来自 `ProductSnapshot`

## 5.6 Uniqueness And Index Rules

- `ProductSpu.spuCode` 在同一租户内唯一
- `ProductSku.skuCode` 在同一租户内唯一
- `ProductCategory.categoryCode` 在同一租户内唯一
- `ProductSnapshot` 必须按 `tenantId + orderNo + orderItemNo` 唯一
- `ProductIdempotencyRecord` 必须按 `tenantId + operationType + idempotencyKey` 唯一
- `ProductArchive` 必须按 `tenantId + spuId + productVersion` 唯一

## 6. Global Constraints

### 6.1 Source Of Truth Rule

- Product 主数据固定以 MySQL 为事实来源
- Elasticsearch 只承载商品搜索、筛选和列表查询投影
- Elasticsearch 不得参与商品可售校验、订单快照创建和业务状态判断
- 商品详情、商品快照、SKU 可售校验必须以 MySQL 为准

### 6.2 Version Rule

- `version` 是 `SPU` 聚合版本
- `version` 初始值固定为 `1`
- `version` 在每个 `spuId` 内顺序递增
- 一次业务操作最多推进一次 `version`
- SPU、SKU、分类绑定、销售价、图片、状态等影响展示、搜索、快照或可售校验的字段变更必须推进 `version`
- outbox 事件必须携带变更后的 `productVersion`
- Elasticsearch 文档必须保存已同步的 `productVersion`
- 订单商品快照必须保存创建快照时的 `productVersion`
- 管理端编辑商品必须携带 `expectedVersion` 执行乐观锁校验
- 重复命中幂等记录不得再次推进 `version`

### 6.3 Idempotency Rule

- 商品命令幂等固定在 `application` 层编排
- 领域对象状态机不得静默吞掉重复命令
- 创建商品按 `tenantId + CREATE_PRODUCT + idempotencyKey` 幂等
- 编辑商品按 `tenantId + UPDATE_PRODUCT + idempotencyKey` 幂等
- 创建订单商品快照按 `tenantId + orderNo + orderItemNo` 幂等
- 同一幂等键重复请求且 `requestHash` 相同，必须返回首次成功结果
- 同一幂等键重复请求但 `requestHash` 不同，必须返回幂等键冲突
- `expectedVersion` 只负责并发覆盖控制，不替代 `idempotencyKey`
- 数据库唯一约束必须作为幂等最终兜底

### 6.4 MySQL And Elasticsearch Consistency Rule

- 商品写操作必须先在 MySQL 事务内提交事实数据
- 同一 MySQL 事务内必须写入 `ProductOutbox`
- Elasticsearch 同步必须由 outbox 异步驱动
- Elasticsearch 与 MySQL 的一致性固定为最终一致
- MySQL 提交成功即商品命令成功；ES 同步失败不得回滚商品写入
- ES 同步失败必须可重试、可观测、可进入死信
- ES 消费必须基于 `productVersion` 阻止旧版本覆盖新版本

### 6.5 Status Rule

- `DRAFT` 商品不得被订单创建快照
- `ON_SALE` 商品允许创建订单商品快照
- `OFF_SALE` 商品不得被新订单创建快照
- `ARCHIVED` 商品不得编辑、上架或创建新快照
- `SPU` 上架前必须至少包含一个 `ENABLED` SKU
- `SKU.status=DISABLED` 时不得创建订单商品快照
- 分类停用后，不得创建或上架绑定该分类的新商品

### 6.6 Price Rule

- 第一版只支持 `salePrice`
- `salePrice` 使用金额字段，必须大于等于 `0`
- 不支持划线价、成本价、会员价和活动价
- 订单商品快照必须固化创建时的 `salePrice`

### 6.7 Image Rule

- Product 只保存 `Storage` 返回的对象引用
- Product 不保存文件内容和底层存储路径
- 主图固定由 `ProductSpu.mainImageObjectId` 表达
- 图片列表由 `ProductImage` 表达
- 商品快照必须固化创建时的 `mainImageObjectId`

## 7. Functional Requirements

### 7.1 Category Management

- Product 必须支持树形分类
- `parentId` 为空表示一级分类
- 禁用分类后，不得新增商品绑定该分类
- 已绑定商品的分类禁用不自动下架商品
- 删除分类第一版不支持，只允许启用和禁用

### 7.2 Product Management

- 创建商品默认进入 `DRAFT`
- 编辑商品必须携带 `expectedVersion`
- 上架商品必须至少存在一个可用 SKU
- 下架商品不得影响已创建订单商品快照
- 归档商品必须进入 `ARCHIVED`
- `ARCHIVED` 商品不得恢复为其他状态
- 商品变更后必须写入 outbox 触发 ES 同步

### 7.3 SKU Management

- SKU 归属于一个 SPU
- SKU 编码在同一租户内唯一
- SKU 规格属性使用稳定 JSON 结构保存
- SKU 销售价变更必须推进 SPU `version`
- SKU 禁用后不得创建新订单商品快照
- SKU 禁用不得影响已创建订单商品快照

### 7.4 Product Snapshot

- 商品快照用于订单商品明细
- 一个 `orderNo + orderItemNo` 只能创建一个商品快照
- 重复创建同一订单明细商品快照必须返回同一 `snapshotId`
- 商品快照必须从 MySQL 读取商品事实数据
- 商品快照不得从 Elasticsearch 创建
- 商品快照创建后不得因商品后续变更而改变
- 商品快照必须保存 `productVersion`

### 7.5 Product Archive

- 商品归档用于保留商品聚合历史版本
- 商品归档内容必须包含 SPU、SKU、分类摘要、图片摘要和销售价
- 商品归档必须按 `productVersion` 唯一
- 商品归档用于追溯，不作为订单商品快照的唯一事实来源
- 第一版归档由商品状态归档和关键变更触发

### 7.6 Product Search

- 商品搜索必须查询 Elasticsearch
- 商品详情必须查询 MySQL
- 商品搜索结果允许存在短暂延迟
- 商品搜索结果命中已下架或已归档商品时，订单创建前必须再次通过 MySQL 快照校验
- 商品搜索至少支持关键词、分类、状态、价格区间、更新时间排序

### 7.7 Product Index Sync

- 商品创建、编辑、上下架、归档必须写入 `ProductOutbox`
- outbox 消费必须重建当前 `SPU` 的 Elasticsearch 文档
- outbox 消费成功后标记 `SUCCEEDED`
- outbox 消费失败后按退避策略重试
- 重试耗尽后进入 `DEAD`
- 进入 `DEAD` 的事件不得丢弃，必须保留 payload 和失败原因

## 8. Key Flows

### 8.1 Create Product

1. 管理端提交创建商品请求
2. `application` 校验幂等键和请求摘要
3. `application` 校验分类可用
4. `domain` 创建 `ProductSpu`、`ProductSku`、`ProductImage`
5. `ProductSpu.version` 固定为 `1`
6. MySQL 事务内写入商品事实数据和 outbox
7. 返回 `spuId`、`spuCode`、`productVersion`

### 8.2 Update Product

1. 管理端提交编辑商品请求并携带 `expectedVersion`
2. `application` 校验幂等键和请求摘要
3. `application` 加载当前商品聚合
4. 若当前 `version` 不等于 `expectedVersion`，返回版本冲突
5. `domain` 执行业务字段变更
6. `ProductSpu.version` 增加 `1`
7. MySQL 事务内写入商品事实数据和 outbox
8. 返回新的 `productVersion`

### 8.3 Create Order Product Snapshot

1. `Order` 通过 `ProductCommandFacade` 传入 `orderNo`、`orderItemNo`、`skuId`、`quantity`
2. `application` 按 `tenantId + orderNo + orderItemNo` 校验幂等
3. 若快照已存在，返回已有快照
4. `application` 从 MySQL 加载 SKU、SPU、分类和图片
5. 校验商品状态和 SKU 状态可售
6. 创建 `ProductSnapshot`
7. 返回 `ProductSnapshotDTO`

### 8.4 Sync Product Search Document

1. outbox worker 抢占 `PENDING` 或可重试事件
2. worker 按 `spuId` 从 MySQL 重建当前商品搜索投影
3. worker 写入 Elasticsearch
4. ES 写入时校验 `productVersion`，旧版本不得覆盖新版本
5. 成功后 outbox 标记 `SUCCEEDED`
6. 失败后增加 `retryCount` 并计算 `nextRetryAt`
7. 重试耗尽后标记 `DEAD`

## 9. Non-Functional Requirements

| ID | Type | Requirement |
|----|----|----|
| NFR-001 | Consistency | 商品写入以 MySQL 为事实来源，ES 最终一致 |
| NFR-002 | Consistency | 商品创建、编辑、快照创建必须幂等 |
| NFR-003 | Concurrency | 商品编辑必须使用 `expectedVersion` 执行乐观锁 |
| NFR-004 | Observability | ES 同步失败必须记录告警日志和可重试记录 |
| NFR-005 | Performance | 商品搜索必须由 ES 承载，不得在 MySQL 中做复杂全文检索 |
| NFR-006 | Compatibility | 第一版不要求改造既有订单、库存、支付主链路 |

## 10. Open Items

无
