# Bacon 系统架构

本文件只保留架构决策和实现红线。详细目录树与装配示例见：

- [`ARCHITECTURE-REFERENCE.md`](./ARCHITECTURE-REFERENCE.md)

## Project Baseline

- project: Maven multi-module
- java: 17
- spring-boot: 3.5.11
- base package: `com.github.thundax.bacon`

## Architecture Shape

- 业务代码只写一份。
- 启动模块按运行模式装配。
- 支持 mono-app 与微服务并存。
- 域内主调用链保持一致，跨域调用方式可切换。

域内主链路固定为：

`HTTP -> interfaces -> application -> domain -> repository -> MySQL/Redis`

## Module Boundaries

- `bacon-app/`: 启动与装配
- `bacon-biz/`: 业务域
- `bacon-common/`: 共享能力
- `deploy/`: 部署样例

每个业务域固定分层：

- `api`
- `interfaces`
- `application`
- `domain`
- `infra`

## Cross-Domain Rule

- 跨域调用只依赖 `api.facade`
- 单体模式由 `interfaces.facade.*LocalImpl` 适配
- 微服务模式由 `infra.facade.remote.*RemoteImpl` 适配
- 业务代码不得直接依赖对方域的 `application` 实现或 `infra` 实现
- `{module}-api` 是跨域契约基础模块，不得依赖其他业务域的任意分层模块（`{other-domain}-api/interfaces/application/domain/infra`）

### Contract Base Detection Rule

- 规则目标：保证每个 `{module}-api` 只承载本域稳定契约，不被其他业务域实现细节反向污染
- 检测口径：`..{module}.api..` 禁止依赖 `..{other-domain}..`（`{other-domain} != {module}`）
- 允许依赖：本域 `api` 包、`bacon-common`、JDK 与通用基础设施依赖
- 推荐 because（给 AI / ArchUnit）：`{module}-api is contract base and must not depend on other domain modules`

## Layer Rules

### interfaces

- 负责 HTTP / MQ / provider 入口适配
- 负责协议参数校验与协议模型转换
- 领域含义稳定的 `XxxId` / `XxxNo` / `XxxStatus` / `XxxType` 等值对象或领域枚举固定在 `interfaces` 形成
- 本地 facade 适配实现固定放在 `interfaces.facade`
- 不直接访问 `domain repository` 或 `infra mapper`

### api

- 只定义跨域稳定契约
- 只放 `facade` 和跨域 `dto`
- 不暴露领域实体
- 不承载 HTTP 语义

### application

- 负责用例编排、事务、幂等、跨域协调
- 只表达业务动作，不落技术细节
- 可以依赖本域 `domain` 和外域 `api.facade`
- 不直接依赖其他域的 `infra`
- 对外公共方法固定接收领域值对象、领域枚举、`*Command`、`*Query`、`*PageQuery`、`*PageResult` 等稳定应用契约
- 不直接接收 `interfaces.dto.*Request`、`interfaces.response.*Response`、`api.dto.*PageQueryDTO`
- `com.github.thundax.bacon.common.application.page.PageQuery` / `PageResult` 归属 `application` 契约层，`interfaces` 不得将其作为 controller/provider/facade 的公开签名或对外响应模型；`interfaces` 仅允许在 assembler 或 response 转换中临时使用
- `XxxId` / `XxxNo` 到领域实体的装载固定在 `application`
- 创建新的领域对象时，调用 `domain entity.create(...)`

### domain

- 负责核心业务规则、不变量、聚合、一致性约束
- 定义仓储接口
- 不感知 HTTP、MyBatis、缓存、MQ、SDK
- `create(...)` 表达“新建业务对象”，给 `application` 使用
- `reconstruct(...)` 表达“从持久化状态恢复对象”，给 `infra` 使用
- 不要用 `reconstruct(...)` 承担 application 的创建语义

### infra

- 负责持久化、远程调用、缓存、消息、三方适配
- 实现 `domain.repository`
- 远程 facade 适配实现固定放在 `infra.facade.remote`
- 从数据库、缓存、消息等技术载荷恢复领域对象时，调用 `domain entity.reconstruct(...)`

## Time Modeling

- 绝对时间点统一使用 `Instant`
- 典型字段：`createdAt`、`updatedAt`、`occurredAt`、`expireAt`、`paidAt`、`closedAt`
- `LocalDateTime` 只用于本地业务时间，不用于绝对时间点
- `infra` 负责 `Instant` 与数据库 `datetime(3)` 的 UTC 转换
- `api` / `dto` / `response` 不得把绝对时间退化成无时区本地时间

## Repository And Cache Rules

- 正式业务仓储以数据库为真相源
- 正式 `RepositoryImpl` 默认注册，不得用 `@ConditionalOnBean` 静默跳过
- 依赖缺失应直接启动失败，不允许静默降级为“没注册”
- 测试替身或演示实现用 `@ConditionalOnMissingBean`
- 进程内 `Map/List/AtomicLong` 只允许用于测试夹具、演示实现、极小范围瞬时状态
- 正式链路不得依赖内存仓储作为主存储
- 分页、过滤、排序、聚合优先下推到持久化层
- `application` 与 `domain.repository` 的分页契约固定使用 `pageNo` / `pageSize`
- `infra` 如需 `offset` / `limit`，固定在实现层内部换算，不向 `application` 或 `domain.repository` 暴露
- 缓存只能保存数据库结果的派生读模型，不能替代数据库

## Exception Convention

- 参数错误（空值、格式非法、状态前置条件不满足）统一抛 `BadRequestException`
- 资源不存在（按 ID / 编码未查到）统一抛 `NotFoundException`
- 业务冲突（重复创建、父子约束冲突、被引用不可删）统一抛 `ConflictException`
- 领域规则错误优先使用各域 `*DomainException`，避免在 `application` / `infra` 直接抛 `IllegalArgumentException`
- `application` 与 `infra.repository.impl` 禁止新增 `IllegalArgumentException` 作为业务异常出口

## ID And Number Generation

- 统一 ID 走统一 ID 设计
- `Order`、`Payment`、`Inventory` 等业务单号由 `infra` 集成发号能力
- 业务域不直接在 `domain` 手写基础设施型发号逻辑

## Quality Tooling

- 给 AI 的默认判断：`spotless` 是 formatter，只负责整理代码格式、import 和版式，不承载规约语义。
- 给 AI 的默认判断：`checkstyle` 是 rule gate，只负责检查、报错和阻断，不负责改代码，也不替代 formatter。
- 新增或调整静态规则时，先判断这是“格式整理”还是“规约约束”：前者放 `spotless`，后者放 `checkstyle`。
- 不要把同一类职责同时配到 `spotless` 和 `checkstyle`，避免重复约束、相互打架和误导后续 AI。

## Implementation Default

- 先保证分层正确，再写代码
- 先保证真相源正确，再考虑缓存
- 先复用现有层次和模式，再新增抽象
