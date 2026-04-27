# Product Implementation Runbook

## 1. Purpose

本文档定义 `Product` 业务域从文档到代码落地的执行顺序。  
本文档用于指导 AI 或研发按层分步实现，避免一次性跨层铺代码导致边界漂移。  
本文档是执行手册，不替代需求文档和数据库设计文档。

## 2. Source Documents

执行前必须读取：

1. [`../00-governance/ARCHITECTURE.md`](../00-governance/ARCHITECTURE.md)
2. [`../10-requirements/PRODUCT-REQUIREMENTS.md`](../10-requirements/PRODUCT-REQUIREMENTS.md)
3. [`../20-database/PRODUCT-DATABASE-DESIGN.md`](../20-database/PRODUCT-DATABASE-DESIGN.md)
4. [`../20-database/PRODUCT-ELASTICSEARCH-DESIGN.md`](../20-database/PRODUCT-ELASTICSEARCH-DESIGN.md)

按需读取：

- 新增类、包、目录时读取 [`../00-governance/NAMING-AND-PLACEMENT-RULES.md`](../00-governance/NAMING-AND-PLACEMENT-RULES.md)
- 新增 SQL、DO、Mapper、Repository 实现时读取 [`../00-governance/DATABASE-RULES.md`](../00-governance/DATABASE-RULES.md)
- 接入统一 ID 或业务编码生成时读取 [`../00-governance/UNIFIED-ID-DESIGN.md`](../00-governance/UNIFIED-ID-DESIGN.md)

## 3. Execution Principles

- 先搭骨架工程，再按 `domain -> application -> api -> interfaces -> infra` 顺序落代码
- 每一步只关闭一个层级或一个清晰能力，不跨层抢跑
- 每一步必须保持 Maven 模块可识别、代码可编译或明确说明暂不可编译原因
- 第一版不得主动改造既有 `Order`、`Inventory`、`Payment`、`Storage` 主链路
- 第一版假设 `OrderItemNo` 已存在；订单域补充 `OrderItemNo` 是后续低优先级任务
- MySQL 是商品事实源，Elasticsearch 只做查询投影
- `version` 是 `SPU` 聚合顺序版本，重复幂等命中不得再次推进版本
- 每个 TODO 任务完成后，必须同步测试、清理现场、删除已完成 TODO 项并提交

## 4. Step 0 - Skeleton

目标：

- 创建 `bacon-product` Maven 多模块骨架
- 将 Product 域挂入 `bacon-biz`
- 建立固定五层模块

修改范围：

- `bacon-biz/pom.xml`
- `bacon-biz/bacon-product/pom.xml`
- `bacon-biz/bacon-product/bacon-product-api/pom.xml`
- `bacon-biz/bacon-product/bacon-product-domain/pom.xml`
- `bacon-biz/bacon-product/bacon-product-application/pom.xml`
- `bacon-biz/bacon-product/bacon-product-interfaces/pom.xml`
- `bacon-biz/bacon-product/bacon-product-infra/pom.xml`

验收：

- Product 模块目录只包含必要 Maven 骨架和最小 package
- 模块顺序与既有业务域保持一致
- 不在本步骤实现业务类
- `mvn -q -pl bacon-biz/bacon-product -am test` 能运行到模块解析阶段

停止条件：

- 父子 POM 关系不清楚时停止
- 发现既有业务域模块顺序与手册冲突时停止

## 5. Step 1 - Domain

目标：

- 落地 Product 核心领域对象、枚举、值对象和仓储契约
- 固定状态机、版本规则和快照规则

修改范围：

- `bacon-product-domain`

固定内容：

- `ProductSpu`
- `ProductSku`
- `ProductCategory`
- `ProductImage`
- `ProductSnapshot`
- `ProductArchive`
- `ProductIdempotencyRecord`
- `ProductOutbox`
- Product 相关枚举
- Product 相关值对象
- Product 仓储接口
- `ProductDomainException`
- `ProductErrorCode`

实现要求：

- `domain` 不依赖 HTTP、MyBatis、Elasticsearch、Spring Web
- 新建业务对象使用 `create(...)`
- 持久化恢复使用 `reconstruct(...)`
- 状态流转必须由领域对象显式约束
- `ProductSpu.version` 初始为 `1`
- 影响展示、搜索、快照、可售校验的变更必须通过领域行为推进版本
- `ProductSnapshot` 创建后不得修改业务字段

测试要求：

- 覆盖商品创建默认状态和初始版本
- 覆盖上架前 SKU 校验
- 覆盖下架、归档、归档后禁止编辑
- 覆盖 SKU 禁用后不可售
- 覆盖快照固化字段和不可变语义
- 覆盖版本一次业务操作只推进一次

验收：

- `bacon-product-domain` 单模块测试通过
- 领域层不出现 `mapper`、`DO`、`Controller`、`Request`、`Response`、`Elasticsearch` 字样的依赖

## 6. Step 2 - Application

目标：

- 落地 Product 用例编排、命令幂等、事务边界和查询模型
- 暂不实现 HTTP 入口和持久化细节

修改范围：

- `bacon-product-application`
- 必要时补充 `bacon-product-domain` 仓储契约

固定服务：

- `ProductManagementApplicationService`
- `ProductCategoryApplicationService`
- `ProductSnapshotApplicationService`
- `ProductSearchApplicationService`
- `ProductIndexSyncApplicationService`
- `ProductIdempotencyExecutor`

实现要求：

- `application` 只依赖本域 `domain` 和必要外域 `api.facade`
- 创建商品、编辑商品、上下架、归档必须写入 `ProductOutbox`
- 创建商品、编辑商品、创建订单商品快照必须走命令幂等
- `expectedVersion` 只负责并发覆盖控制，不替代 `idempotencyKey`
- 商品快照创建必须读取 MySQL 事实仓储抽象，不得读取 ES 查询投影
- ES 同步应用服务只编排 outbox claim、重建文档、写入 gateway 和状态回写

测试要求：

- 使用仓储测试替身覆盖应用层编排
- 覆盖幂等重复请求返回首次结果
- 覆盖同一幂等键不同请求摘要返回冲突
- 覆盖编辑版本冲突
- 覆盖快照按 `tenantId + orderNo + orderItemNo` 幂等
- 覆盖 ES 旧版本事件跳过和新版本事件写入

验收：

- `bacon-product-application` 测试通过
- 应用层公开方法不接收 `interfaces` 的 `Request` 或 `Response`
- 应用层不直接依赖 MyBatis、Elasticsearch 客户端或 HTTP 细节

## 7. Step 3 - API

目标：

- 落地 Product 跨域最小 `Facade` 契约
- 固定订单侧后续接入所需的只读和快照能力

修改范围：

- `bacon-product-api`

固定接口：

- `ProductReadFacade`
- `ProductCommandFacade`

固定模型：

- `ProductSkuSaleInfoDTO`
- `ProductSnapshotDTO`
- 必要 request/response DTO
- 必要共享枚举

实现要求：

- `api` 不依赖其他业务域任意模块
- `api` 不暴露领域实体
- `api` 不承载 HTTP 语义
- `createOrderProductSnapshot` 使用 `orderNo + orderItemNo` 作为订单商品明细来源

测试要求：

- 添加或扩展契约架构测试
- 验证 `product-api` 不依赖其他业务域

验收：

- `bacon-product-api` 编译通过
- Facade 方法与 `PRODUCT-REQUIREMENTS.md` 保持一致

## 8. Step 4 - Interfaces

目标：

- 落地管理端 Controller、Provider、本地 Facade 适配和协议转换

修改范围：

- `bacon-product-interfaces`

固定入口：

- 管理端商品、分类、搜索、重建索引入口
- 内部 Provider：SKU 销售信息查询、订单商品快照创建
- 本地 `ProductReadFacade` 和 `ProductCommandFacade` 实现

实现要求：

- Controller 只接收 `interfaces.request`，只返回 `interfaces.response`
- Provider 只处理内部 HTTP 协议转换
- Local Facade 只做 `api` 契约到 `application` 契约转换
- 不直接访问 `domain repository` 或 `infra mapper`
- 不在 Controller 中拼业务规则

测试要求：

- Controller API 回归测试
- Provider 契约测试
- Local Facade 契约测试
- Bean Validation 和异常响应测试

验收：

- `bacon-product-interfaces` 测试通过
- 对外响应不暴露领域实体、DO 或 ES document

## 9. Step 5 - Infra

目标：

- 落地 MySQL 持久化、Elasticsearch 投影、outbox 消费和远程 Facade 适配

修改范围：

- `bacon-product-infra`
- 必要配置文件
- 必要测试资源

固定内容：

- MySQL `DO`
- MyBatis `Mapper`
- `PersistenceAssembler`
- `RepositoryImpl`
- Elasticsearch `ProductSearchDocument`
- Elasticsearch gateway
- `ProductOutbox` claim、重试、死信实现
- 远程 `Facade` 适配

实现要求：

- MySQL 表字段与 `PRODUCT-DATABASE-DESIGN.md` 一致
- 本步骤可以新增 SQL 或 migration，但必须由数据库设计文档驱动
- ES 文档字段与 `PRODUCT-ELASTICSEARCH-DESIGN.md` 一致
- outbox 事件 payload 只作为触发信息，ES 文档必须从 MySQL 当前事实重建
- ES 写入必须基于 `productVersion` 阻止旧版本覆盖新版本
- 正式 `RepositoryImpl` 默认注册，不得静默降级为内存仓储

测试要求：

- Repository 持久化测试
- DO / domain assembler 测试
- outbox claim、重试、死信测试
- ES gateway 使用测试替身覆盖版本防旧覆盖规则

验收：

- `bacon-product-infra` 测试通过
- MySQL、ES、outbox 三类实现边界清楚
- infra 不承载业务状态规则

## 10. Step 6 - Assembly And Verification

目标：

- 将 Product 接入启动装配和必要的模块依赖
- 执行跨模块验证

修改范围：

- `bacon-app`
- 根 POM 或启动模块 POM
- 必要配置样例

实现要求：

- mono 模式能装配 Product 本地 Facade
- 微服务模式预留远程 Facade 适配
- 不因为 Product 接入破坏既有业务域启动
- 第一版不强制订单域调用 Product

测试要求：

- Product 相关模块测试
- 受影响启动模块编译
- 必要 ArchUnit 规则

验收：

- Product 模块可被 Maven 构建
- 既有核心模块不因 Product 接入编译失败

## 11. Step 7 - Site Cleanup

目标：

- 清理实现过程中的临时状态，保证任务关闭干净

必须执行：

- 删除已完成的 `TODO.md` 任务项
- 确认未提交临时文件、IDE 文件、调试输出、生成残留
- 确认没有无意修改既有域代码
- 确认文档、代码、测试在同一任务 commit 内闭环
- 执行 `git status --short`
- 提交信息使用 `Type(domain): 中文说明`

验收：

- 工作区干净
- TODO 中只保留未关闭任务
- 每个 commit 只包含一个清晰任务范围

## 12. TODO Deployment Rule

Product 落地 TODO 必须按层拆分：

1. `Product Step 0`：搭建 `bacon-product` Maven 骨架
2. `Product Step 1`：落地 `domain`
3. `Product Step 2`：落地 `application`
4. `Product Step 3`：落地 `api`
5. `Product Step 4`：落地 `interfaces`
6. `Product Step 5`：落地 `infra`
7. `Product Step 6`：接入装配与验证
8. `Product Step 7`：清理现场并关闭任务

每个 TODO 项必须引用本文档对应步骤。  
执行 TODO 时不得跳过前置步骤，除非人工明确调整执行顺序。

## 13. Open Items

无
