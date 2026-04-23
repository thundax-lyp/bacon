# ARCHITECTURE INTENT

## 1. Purpose

本文档解释 `ARCHITECTURE.md` 背后的设计意图，供 AI 在实现、重构、排查边界问题时判断“为什么这样设计”。

本文档不替代 `ARCHITECTURE.md`。发生冲突时，以 `ARCHITECTURE.md` 的实现红线为准。

## 2. Scope

当前范围：

- mono-app 与 microservice 并存的设计意图
- 业务域五层结构的设计意图
- 跨域 `Facade` 规则的设计意图
- `common`、`app`、`biz`、`deploy` 的职责边界意图
- `create(...)` / `reconstruct(...)` 语义拆分意图
- 数据库初始化、部署脚本与业务模块的边界意图
- AI 实现时的架构判断原则

不在范围内：

- 不重新定义目录结构
- 不新增业务需求
- 不定义数据库表结构
- 不替代 ArchUnit、Checkstyle、Spotless 等门禁规则

## 3. Bounded Context

本项目的架构意图固定为：

- 用 mono-app 保证首次交付、联调和低复杂度部署。
- 用 microservice starter 保留后续拆分路径。
- 用业务域分层保证代码可以长期演进。
- 用 `api.facade` 保证跨域调用只依赖稳定契约。
- 用文档和门禁持续收敛 AI 生成代码的边界。

本项目不追求一次性完成最大化架构设计。架构规则随业务复杂度逐步收紧，新增规则必须服务于已经出现的复杂度或明确的生产风险。

## 4. Module Mapping

### 4.1 `bacon-app`

`bacon-app` 的意图是装配运行模式，不承载业务规则。

- `bacon-mono-boot` 用于单体装配全部业务域。
- `bacon-*-starter` 用于单域微服务启动。
- `bacon-gateway` 用于微服务模式入口。

AI 不得因为某个业务功能修改而把业务逻辑放入 `bacon-app`。

### 4.2 `bacon-biz`

`bacon-biz` 的意图是承载业务域内的真实业务能力。

每个业务域固定按 `api`、`interfaces`、`application`、`domain`、`infra` 分层，是为了让 AI 和开发者在新增能力时能稳定判断代码归属。

AI 新增业务能力时，必须优先在对应业务域内完成垂直切片，不得先横向创建空抽象。

### 4.3 `bacon-common`

`bacon-common` 的意图是承载跨域稳定通用技术能力，不承载业务域事实。

允许放入 `common` 的内容：

- 通用异常类型
- 通用分页模型
- 通用 ID 能力
- 通用 Web / Security / MyBatis 技术支撑
- 多个业务域立即共用且不包含业务事实的基础能力

不得放入 `common` 的内容：

- 业务域 SQL
- 业务初始化数据
- 业务流程编排
- 某个业务域专属规则
- 只为一个功能预留的未来抽象

`common` 不是“放不下的代码”的兜底目录。

### 4.4 `deploy` 与 `scripts`

`deploy` 与 `scripts` 的意图是承载部署、运行和人工操作入口。

首次上线的数据库初始化属于部署/运维动作，不属于业务模块能力。此类问题优先放在 `scripts` 或 `deploy`，不得为了“看起来更工程化”把初始化脚本接入 `common` 或所有 starter。

## 5. Core Domain Objects

### 5.1 Layer Intent

#### `interfaces`

`interfaces` 的意图是隔离协议世界。

HTTP、Provider、Controller、Request、Response、OpenAPI 注解、权限注解等外部协议相关内容固定在这一层形成。

AI 不得在 `interfaces` 中直接访问数据库或直接操作 `domain.repository`。

#### `api`

`api` 的意图是承载跨域稳定契约。

`api` 不代表 HTTP API，不承载 Controller 语义。它只表达其他业务域可以依赖的 `Facade`、request、response 契约。

AI 不得把领域实体、Controller Request 或基础设施实现暴露到 `api`。

#### `application`

`application` 的意图是承载用例编排。

事务、幂等、跨域协调、状态推进顺序、领域对象装载都固定在这一层表达。它负责把一个业务动作组织成可提交、可回滚、可测试的用例。

AI 不得把 application 写成技术适配层，也不得让它依赖 `infra` 实现。

#### `domain`

`domain` 的意图是承载业务不变量。

领域实体和值对象负责状态合法性、业务规则和一致性约束。`domain` 不知道 HTTP、MyBatis、Redis、MQ、SDK。

AI 不得为了调用方便把技术模型带入 `domain`。

#### `infra`

`infra` 的意图是承载技术实现。

数据库、缓存、MQ、远程 HTTP、第三方 SDK、Mapper、DO、RepositoryImpl 都属于这一层。它负责把技术载荷转换为领域对象，或把领域对象持久化。

AI 不得把业务流程放入 `infra`。

### 5.2 `create(...)` And `reconstruct(...)`

`create(...)` 表达新业务对象的创建语义，由 `application` 调用。

`reconstruct(...)` 表达从持久化状态恢复领域对象，由 `infra` 调用。

拆分这两个方法的意图是避免 AI 把“业务创建”和“数据库恢复”混为一谈。

`domain` 测试允许使用 `reconstruct(...)` 构造已存在对象、历史状态、终态和异常边界状态。该用法本质上是在测试中模拟对象已经由 `infra` 从数据库加载完成后的状态。

这个测试例外的意图是降低领域行为测试的状态准备成本，不改变生产代码的调用边界。

### 5.3 `Facade`

`Facade` 的意图是让跨域调用依赖稳定契约，而不是依赖对方实现。

- mono 模式通过 `interfaces.facade.*LocalImpl` 适配本地调用。
- micro 模式通过 `infra.facade.remote.*RemoteImpl` 适配远程调用。
- 调用方只依赖对方域的 `api.facade`。

该设计让同一份业务代码可以在 mono 和 micro 两种模式下复用。

### 5.4 URL Shape

外部 Controller URL 的意图是同时表达业务域边界和公开资源集合。

`/{domain}` 让 AI 和开发者先判断请求属于哪个业务域，`/{resources}` 让后续实现落到明确资源对象上。即使两者词根接近，也保留两段语义，避免后续在一个域根路径下混放查询、审计、回调和命令动作。

Provider URL 的意图不是模拟公开 REST 资源，而是表达内部跨域能力。

Provider 固定使用 `queries` / `commands` 区分读能力和写能力，便于 AI 判断调用方是否在进行跨域查询或跨域命令，不把内部能力误当成前端可直接访问的资源路径。

## 6. Global Constraints

- AI 必须先判断改动属于业务能力、技术支撑、部署操作还是治理规则。
- 能在业务域内解决的问题，不放入 `common`。
- 能在 `scripts` / `deploy` 解决的问题，不进入业务模块。
- 能用现有分层完成的问题，不新增架构层。
- 不为未来可能需要的能力提前引入抽象。
- 不把局部需求扩展成全局重构。
- 新依赖默认放在最窄可行模块，不先放 parent 或 `common`。
- 改动跨越多个边界时，必须先确认每个边界的必要性。

## 7. Functional Requirements

### 7.1 AI Implementation Decision Flow

AI 实现前固定按以下顺序判断：

1. 当前任务属于哪个业务域或治理边界。
2. 当前任务是否需要修改业务代码。
3. 当前任务是否可以通过脚本、文档或部署配置完成。
4. 当前任务是否必须新增依赖。
5. 当前任务是否必须修改 `common`。
6. 当前任务是否会改变 mono/micro 装配行为。
7. 当前任务是否需要同步 `TODO.md`、`docs` 或 `db/AGENT.md`。

只有当答案明确时，才开始修改代码。

### 7.2 Over-Engineering Brake

以下场景必须优先选择轻量方案：

- 首次上线初始化数据库。
- 单次部署操作。
- 单业务域内部的小功能。
- 只影响一个启动模块的配置。
- 只为本地验证服务的脚本。

轻量方案不能满足验收点时，才允许升级为框架依赖或架构改造。

### 7.3 Common Boundary Check

修改 `common` 前必须满足以下条件：

- 至少两个现有业务域立即需要该能力。
- 该能力不包含业务域事实。
- 该能力不要求其他业务域感知它的业务数据。
- 该能力可以独立于具体业务流程复用。

不满足以上条件时，不得修改 `common`。

## 8. Key Flows

### 8.1 Feature Flow

新增业务能力时固定按垂直切片推进：

1. `interfaces` 接收协议模型。
2. `interfaces.assembler` 转换为应用契约。
3. `application` 编排用例。
4. `domain` 执行业务规则。
5. `domain.repository` 表达持久化契约。
6. `infra.repository.impl` 和 Mapper 完成技术实现。
7. 测试覆盖业务行为和架构边界。
8. 完成后删除对应 `TODO.md` 项并提交。

### 8.2 Governance Flow

发现一类混乱时固定按以下方式治理：

1. 先修当前任务涉及的最小范围。
2. 再把规则写入治理文档。
3. 再补 ArchUnit、脚本或 CI 门禁。
4. 再扩大到全域治理。
5. 最后删除对应 `TODO.md` 项并提交。

### 8.3 Deployment Flow

部署侧问题优先在 `deploy` 或 `scripts` 中解决。

只有当运行时确实需要应用参与，且不能由部署脚本完成时，才允许进入启动模块配置。

## 9. Non-Functional Requirements

- 架构意图必须帮助 AI 减少误改范围。
- 架构意图必须能解释规则背后的边界原因。
- 架构意图不得重复列举 `ARCHITECTURE.md` 已经定义的全部规则。
- 架构意图不得成为新增抽象的理由。
- 架构意图变更后，如影响任务读取入口，必须同步 `docs/AGENT.md`。

## 10. Open Items

无
