# Purpose

定义 `inventory` 模块在 `application` / `interfaces` / `infra` 之间透传 `userId`、`tenantId` 的改造清单，作为后续持续重构的唯一执行基线。

# Scope

当前范围：

- `inventory` 模块内的 `interfaces -> application -> infra` 上下文透传
- `thread`、`mq`、`scheduled/retry/replay` 场景下的上下文延续
- `domain` 中移除 `operationId`、逐步移除 `tenantId`
- 引入只读 `AuditData` 或 `AuditInfo`，用于聚合的展示型审计信息
- `createdBy`、`createdAt`、`updatedBy`、`updatedAt` 的数据库落库责任收敛到 `infra`

不在当前范围：

- 其他业务域的同类改造
- 认证体系本身的重构
- 网关鉴权协议重构
- 全仓库统一上下文框架的一次性落地

# Bounded Context

- 所属限界上下文：`Inventory`
- 主改造模块：`bacon-biz/bacon-inventory/*`
- 共享基础能力：`bacon-common-security`、`bacon-common-web`、`bacon-common-mybatis`、`bacon-common-mq`

# Module Mapping

- `interfaces`
  - 负责从 HTTP / provider / consumer 入口获取 `userId`、`tenantId`
  - 负责把上下文转换为应用层显式参数
- `application`
  - 负责显式传递上下文
  - 负责跨线程、定时任务、消息投递前的上下文组装
- `domain`
  - 不持有 `operationId`
  - 不承担 `userId`、`tenantId` 的透传职责
  - 仅保留只读 `AuditData`
- `infra`
  - 负责把上下文写入数据库、消息、日志、出站记录
  - 负责在重建读模型时回填展示型审计信息

# Core Domain Objects

## Fixed Fields

- `OperatorContext`
  - `tenantId`
  - `userId`
  - `source`
  - `traceId`
- `AuditData`
  - `createdBy` (`String`)
  - `createdAt`
  - `updatedBy` (`String`)
  - `updatedAt`

# Global Constraints

- `domain` 不再持有 `operationId`
- `domain` 不再新增对 `userId`、`tenantId` 的直接字段依赖
- `tenantId` 仅在确认不参与领域规则后，才允许从既有聚合中移除
- `AuditData` 是只读信息对象，`domain` 不得在行为方法中写入或修改
- `AuditData.createdBy`、`AuditData.updatedBy` 使用 `String`
- `createdBy`、`createdAt`、`updatedBy`、`updatedAt` 的写入责任固定归属 `infra`
- `thread`、`mq`、`scheduled/retry/replay` 不得依赖隐式线程状态碰运气获取上下文
- 所有新透传链路必须优先走显式对象，不允许继续散落传 `String userId`、`Long tenantId`

# Functional Requirements

## 1. 统一上下文对象

功能对象：

- `OperatorContext`

功能能力：

- 作为 `interfaces -> application -> infra` 的统一透传载体
- 承载 `tenantId`、`userId`、`source`、`traceId`
- 支持从 HTTP 请求、消息、定时任务参数构造

必要补充约束：

- `OperatorContext` 不进入 `domain entity`
- `OperatorContext` 不作为数据库持久化对象

## 2. HTTP 入口透传

功能对象：

- `controller`
- `provider`

功能能力：

- 从 `CurrentTenantProvider` / `CurrentUserProvider` 或显式请求参数获取上下文
- 将上下文组装后传入应用服务

必要补充约束：

- HTTP 入口不得依赖 `domain` 保存 `tenantId`
- 应用服务签名必须可显式接收上下文

## 3. Thread / Async 透传

功能对象：

- `Executor`
- `CompletableFuture`
- 后续 `@Async` 执行链路

功能能力：

- 在线程切换前捕获 `OperatorContext`
- 在线程执行体中恢复 `OperatorContext`

必要补充约束：

- 不允许直接依赖 `SecurityContextHolder` 在线程池中天然可用
- 如使用统一线程池，必须提供统一包装或 `TaskDecorator`

## 4. MQ 透传

功能对象：

- `outbox`
- `BaconMqMessage`
- producer / consumer

功能能力：

- 发送前将 `tenantId`、`userId`、`traceId` 写入消息载体
- 消费时从消息恢复 `OperatorContext`

必要补充约束：

- `BaconMqMessage` 需要支持扩展 header 或 metadata
- 不允许只在 payload 中隐式埋字段而不定义契约

## 5. Scheduled / Retry / Replay 透传

功能对象：

- `@Scheduled`
- 审计重试
- 死信回放

功能能力：

- 在无请求上下文时显式构造系统上下文
- 对已有业务数据中的 `tenantId` 做恢复使用

必要补充约束：

- 定时任务必须明确 `userId` 的固定值
- 不允许定时任务依赖当前线程遗留的请求上下文

## 6. Infra 落库责任收敛

功能对象：

- persistence assembler
- repository impl
- MyBatis 自动填充

功能能力：

- `tenantId` 由 `infra` 写入业务表、审计表、outbox 表
- `createdBy`、`updatedBy` 由 `CurrentUserProvider` 或显式上下文统一填充
- `AuditData` 由持久化重建时回填

必要补充约束：

- 不允许为了落库方便把 `tenantId`、`operationId` 留在聚合里
- `AuditData` 只用于展示，不作为聚合行为决策条件

# Key Flows

## Flow 1: HTTP 命令链路

1. `controller` 获取 `tenantId`、`userId`
2. 组装 `OperatorContext`
3. 调用 `application service`
4. `application service` 执行业务编排
5. `infra` 落库时写入 `tenantId`、审计字段

## Flow 2: 异步线程链路

1. 调用方创建 `OperatorContext`
2. 在线程切换前封装上下文
3. 在线程执行体中恢复上下文
4. 业务执行与落库使用同一上下文

## Flow 3: MQ 链路

1. `application` 生成出站消息
2. `infra` 将上下文写入消息 metadata
3. consumer 侧恢复 `OperatorContext`
4. 后续应用服务按统一上下文执行

## Flow 4: 定时任务链路

1. scheduler 启动任务
2. 构造固定系统上下文
3. 从业务记录恢复 `tenantId`
4. 执行重试、回放、补偿
5. `infra` 继续按统一规则落库

# Non-Functional Requirements

- 每一批改造只允许覆盖一个明确链路
- 每一批改造完成后必须补齐对应测试
- 所有上下文字段命名保持一致：`tenantId`、`userId`
- 透传方案必须兼容单体运行模式与微服务运行模式
- 文档变更必须和代码改造同步更新

# Checklist

## Phase 1: 建立统一模型

- [ ] 新增 `OperatorContext`
- [ ] 明确 `source` 枚举或固定值
- [ ] 明确 `traceId` 是否固定纳入
- [ ] 新增 `AuditData`
- [ ] 明确 `AuditData` 放置模块

## Phase 2: 打通 HTTP 主链路

- [ ] 盘点 `inventory` 的 `controller` / `provider`
- [ ] 统一从入口组装 `OperatorContext`
- [ ] 调整 application service 签名
- [ ] 验证 `interfaces -> application -> infra` 主链路透传

## Phase 3: 收敛 Infra 落库职责

- [ ] 盘点 `InventoryDO`、审计 DO、outbox DO 的 `tenantId` 与审计字段
- [ ] 明确哪些字段走 MyBatis 自动填充
- [ ] 明确哪些字段必须由 repository / assembler 显式写入
- [ ] 验证脱离 domain 字段后仍可正确落库

## Phase 4: 处理 Thread / Async

- [ ] 盘点 `CompletableFuture`、线程池、异步执行点
- [ ] 建立上下文捕获与恢复方案
- [ ] 验证线程切换后 `tenantId`、`userId` 不丢失

## Phase 5: 处理 MQ

- [ ] 扩展 `BaconMqMessage` 支持 metadata
- [ ] 统一 producer 写入上下文
- [ ] 统一 consumer 恢复上下文
- [ ] 明确 payload 与 metadata 的边界

## Phase 6: 处理 Scheduled / Retry / Replay

- [ ] 盘点 `@Scheduled`、审计补偿、回放链路
- [ ] 统一系统任务的 `userId`
- [ ] 明确 `tenantId` 的恢复来源
- [ ] 验证无请求线程下的落库行为

## Phase 7: 收缩 Domain

- [ ] 从 `Inventory` 移除 `operationId`
- [ ] 评估并移除 `Inventory` 中仅用于隔离的 `tenantId`
- [ ] 为聚合回填只读 `AuditData`
- [ ] 验证聚合行为不再依赖上下文字段

# Open Items

- `tenantId` 在 `inventory` 中是否完全不参与领域规则，需要在实际删字段前逐个聚合确认
- `OperatorContext` 应放入 `bacon-common-core`、`bacon-common-security` 还是 `bacon-common-web`，需要在第一步改造前定版
- `BaconMqMessage` 采用 header 模式还是 metadata 字段模式，需要在 MQ 改造前定版
