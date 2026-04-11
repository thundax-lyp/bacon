# BACON CONTEXT PROPAGATION RULES

## 1. Purpose

本文档定义项目内 `BaconContextHolder` 数据透传的统一规则。  
目标是固定 `UserId` 与 `TenantId` 在请求入口、远程调用、消息边界、异步线程、持久化访问中的建立方式、恢复方式、透传方式与验收口径。

本文档用于：

- 指导实现：新增链路时，直接判断上下文应在哪里建立、恢复、延续、校验
- 指导评审：统一判断某条链路是否真正完成了 `BaconContextHolder` 透传
- 指导 AI：让 AI 在检查代码和补全实现时使用稳定一致的口径

## 2. Scope

当前范围：

- 请求入口基于 `Token` 与 `TenantCode` 建立 `BaconContextHolder`
- controller 与 provider 对上下文的恢复与校验
- 本地 `Facade`、远程 `Facade` 对上下文的延续
- MQ 生产与消费时的上下文透传
- `@Async`、线程池、`CompletableFuture` 等异步场景的上下文透传
- application 在上下文模式下的边界约束
- repository 与 MyBatis 插件对上下文的消费方式
- 不经过 MyBatis 插件的数据访问通道的上下文约束
- 缓存键与缓存值写入时的租户隔离约束
- 判断“上下文透传是否完成”时的统一验收口径

不在当前范围：

- 数据库表结构设计细节
- 网关流量治理
- 外部开放协议格式

## 3. Bounded Context

本规则属于全局工程治理规则，适用于所有存在租户隔离要求的业务域，包括但不限于 `order`、`inventory`、`payment`、`upms`。

系统采用严格租户隔离 SaaS 模式。  
每次请求都必须显式携带租户选择信息，系统不允许把 `TenantId` 视为一次登录后永久固定不变的全局状态。

## 4. Core Model

### 4.1 Request Context Model

每次请求的上下文建立流程固定如下：

1. 客户端提交 `Token` 与 `TenantCode`
2. 系统根据 `Token` 解析 `UserId`
3. 系统根据 `TenantCode` 解析 `TenantId`
4. 系统校验 `UserId` 是否拥有该 `TenantId` 的访问权限
5. 无权限时直接拒绝请求
6. 有权限时，把 `TenantId` 与 `UserId` 一起写入 `BaconContextHolder`
7. 后续系统内部统一通过 `BaconContextHolder` 获取这两个值

### 4.2 Terminology

- `BaconContextHolder`：线程上下文，保存当前调用链的 `TenantId` 与 `UserId`
- `TenantId`：当前请求被授权访问的租户标识
- `UserId`：当前请求的用户标识
- 上下文模式：业务层默认不显式传 `TenantId`、`UserId`，而是依赖 `BaconContextHolder`
- 显式透传：特殊契约场景下，方法签名直接接收并继续传递 `TenantId`
- MyBatis 插件透传：仓储方法不显式接收 `TenantId`，但依赖 `BaconContextHolder` 与 MyBatis 租户插件补充租户条件
- 非标准入口：不经过 controller、provider、mq consumer 的入口，例如启动任务、命令行工具、人工回放工具、数据修复脚本

## 5. Module Mapping

- `common.web`：根据请求恢复 `UserId`、`TenantId`，建立 `BaconContextHolder`
- `interfaces.controller`：使用已建立的上下文，不重新发明租户逻辑
- `interfaces.provider`：对内 HTTP 入口，规则与 controller 一致
- `interfaces.facade`：本地 facade 从 `BaconContextHolder` 读取并延续上下文
- `infra.facade.remote`：远程 facade 调用前负责把上下文带到远程边界
- `common.mq`：MQ header 负责跨消息边界传递上下文
- `common.core.context`：`BaconContextTaskDecorator` 与 `AsyncTaskWrapper` 负责跨线程恢复上下文
- `application`：默认依赖当前 `BaconContextHolder` 执行业务编排
- `domain.repository`：根据仓储语义决定是否需要显式 `TenantId`
- `infra.repository`：保证查询、插入、更新最终带有正确的租户约束
- `common.mybatis`：`TenantScoped`、`TenantLineHandler`、自动填充器负责基于上下文消费租户信息
- `common.test.architecture`：通过 ArchUnit 约束并发设施，防止绕过上下文透传

## 6. Global Constraints

### 6.1 Source Of Truth

运行时上下文的唯一可信来源是 `BaconContextHolder`。  
系统内部读取 `TenantId` 与 `UserId` 时，默认统一从 `BaconContextHolder` 获取。

### 6.2 Entry Rule

所有入口都必须先建立或恢复 `BaconContextHolder`，再进入业务方法。

标准入口固定规则：

- HTTP 请求必须基于 `Token` 解析 `UserId`
- HTTP 请求必须基于 `TenantCode` 解析 `TenantId`
- 必须在建立上下文前完成“`UserId` 是否有 `TenantId` 权限”的校验
- 校验通过后必须把 `TenantId` 与 `UserId` 一起放入 `BaconContextHolder`
- 请求处理完成后必须清理 `BaconContextHolder`
- provider controller 使用与 controller 完全一致的上下文规则
- MQ 消费端处理消息前必须先恢复上下文

非标准入口固定规则：

- 启动任务、命令行工具、人工回放工具、数据修复脚本在进入 application 前必须先手工建立 `BaconContextHolder`
- 非标准入口如果无法建立完整上下文，不得进入依赖租户隔离的业务逻辑

### 6.3 Application Rule

application 层默认运行在已建立的 `BaconContextHolder` 中。

固定规则：

- application 公共命令方法默认不显式接收 `TenantId`
- application 公共查询方法默认不显式接收 `TenantId`
- application 公共方法默认也不显式接收 `UserId`
- application 必须直接依赖当前 `BaconContextHolder` 所代表的请求上下文
- application 编排多个下游能力时，不允许中途丢失当前上下文
- 只有在脱离当前上下文、跨进程重装配、人工回放、后台工具、或业务契约明确要求时，application 才允许显式接收 `TenantId`

### 6.4 Facade Rule

本地与远程 facade 的职责是延续上下文，不是重建业务租户逻辑。

固定规则：

- 本地 facade 不从外部参数重新发明 `TenantId`、`UserId` 来源
- 本地 facade 必须从 `BaconContextHolder` 读取当前上下文
- 读取到空值时必须立即失败，不允许静默降级
- 远程 facade 调用前必须先把当前上下文放到远程边界可消费的位置
- 远程边界默认必须同时透传 `TenantId` 与 `UserId`
- 远程边界任一关键上下文字段缺失时必须立即失败，不允许静默降级

### 6.5 Async Rule

所有跨线程执行都必须视为新的上下文边界。  
一旦执行路径离开当前线程，`BaconContextHolder` 不再天然存在，必须通过统一设施恢复。

固定规则：

- Spring 异步线程池必须使用带 `TaskDecorator` 的受控执行器
- 默认 `taskExecutor` 必须统一注册 `BaconContextTaskDecorator`
- 任何自定义命名的 `Executor` 只要用于上下文隔离业务，也必须具备等价的上下文装饰能力
- 需要手工提交到线程池的 `Runnable`、`Callable`、`Supplier` 必须使用 `AsyncTaskWrapper.wrap`
- `CompletableFuture.runAsync`、`CompletableFuture.supplyAsync` 必须显式传入受控 `Executor`
- 任务执行完成后必须清理线程上下文，防止线程复用导致串线

### 6.6 Repository Rule

repository 分两类。

第一类，业务语义必须显式表达租户的仓储。

这类仓储接口必须显式接收 `TenantId`。典型场景包括：

- 离开 `TenantId` 后业务键不完整的查询
- 按租户分页、统计、认领、回放、补偿
- 后台工具或回放任务中不依赖当前请求上下文的仓储操作

第二类，由 MyBatis 租户插件兜底的仓储。

这类仓储可以不在方法签名中声明 `TenantId`，但必须同时满足以下条件：

- 对应 DO 标记了 `@TenantScoped`
- 调用发生时 `BaconContextHolder` 中已存在正确的 `TenantId`
- 查询或更新最终经过 MyBatis-Plus 租户插件可拦截的路径

### 6.7 Non-MyBatis Access Rule

凡是不经过 MyBatis 租户插件的数据访问通道，都必须自行显式带入租户条件。  
不允许假定统一框架会自动补充。

适用对象包括但不限于：

- 直连 JDBC
- 原生 SQL 执行器
- 搜索引擎查询
- 第三方数据 SDK
- 自定义缓存加载器
- 任何绕过 `TenantScoped` 与租户插件的访问路径

固定规则：

- 读路径必须显式带租户过滤条件
- 写路径必须显式校验租户归属
- 无法证明租户条件已生效的访问路径，视为不合规

### 6.8 MyBatis Rule

参与租户隔离的 DO 必须通过 `TenantScoped` 明确声明租户读写规则。

固定规则：

- `read = true` 表示读 SQL 受租户拦截器约束
- `insert = true` 表示插入时自动回填 `TenantId`
- `verifyOnUpdate = true` 表示更新时校验实体租户与当前上下文租户一致
- 未标记 `@TenantScoped` 的 DO 不得假定租户插件自动生效

### 6.9 Concurrency Architecture Rule

并发编程必须同时受到配置、包装器、架构测试三层约束。

固定规则：

- 配置层：`BaconAsyncAutoConfiguration` 统一注册 `TaskDecorator` 与受控 `taskExecutor`
- 包装层：`AsyncTaskWrapper` 负责把 `BaconContextHolder` 快照恢复到新线程，并在执行后清理线程变量
- 架构层：`AbstractConcurrencyArchitectureTest` 与 `ConcurrencyArchitectureRuleSupport` 禁止以下行为：
  - 直接 `new Thread`
  - 直接使用 `Executors.new*`
  - 调用未显式传入 `Executor` 的 `CompletableFuture.runAsync` / `supplyAsync`

### 6.10 Cache Rule

缓存规则以“租户隔离优先”作为默认原则。

固定规则：

- 只要缓存内容在不同租户之间可能不同，缓存键必须显式包含 `TenantId`
- 不允许依赖线程上下文在缓存读写阶段隐式区分租户
- 缓存键中的租户段必须稳定、直接、可检索，默认使用 `tenant:{tenantId}` 或等价稳定前缀
- 只有在数据被明确认定为全局共享、且不会因租户不同而返回不同结果时，缓存键才允许不带 `TenantId`
- 写入缓存前，结果本身必须已经完成租户过滤
- 不允许把未经过租户过滤的结果写入带租户键的缓存

### 6.11 Acceptance Rule

判断某条链路“是否完成了上下文透传”时，必须区分两种口径。

第一种口径，上下文隔离已生效。

满足以下任一条件即可：

- 当前链路正确建立并延续了 `BaconContextHolder`
- repository / MyBatis / 非 MyBatis 访问层最终消费了正确的上下文租户信息

最低要求：

- `TenantId` 与 `UserId` 必须被成对建立、成对延续、成对恢复
- 不允许只透传 `TenantId` 而丢失 `UserId`
- 不允许只透传 `UserId` 而丢失 `TenantId`

第二种口径，显式透传已完成。

必须同时满足：

- 当前场景属于文档允许的特殊显式契约
- application 已显式接收 `TenantId`
- repository 接口也显式接收 `TenantId`
- infra 查询方法显式使用 `TenantId`

评审结论必须明确写明采用的是哪一种口径。  
不允许只说“已透传”而不说明是上下文透传还是显式透传。

## 7. Functional Requirements

### 7.1 Request Context Establishment

- 每次请求必须同时具备 `Token` 与 `TenantCode`
- 系统必须先解析 `UserId`，再解析 `TenantId`
- 系统必须在写入 `BaconContextHolder` 前完成用户与租户权限校验
- 权限校验失败时必须直接拒绝请求
- 权限校验通过后必须把 `TenantId` 与 `UserId` 一起写入 `BaconContextHolder`
- 请求处理完成后必须清理 `BaconContextHolder`

### 7.2 Controller And Provider

- controller 方法必须使用已建立的请求上下文
- controller 拿到 `@CurrentTenant` 的值时，应视为上下文校验结果，而不是 application 默认显式入参
- provider controller 规则与 controller 完全一致
- controller 不负责业务层租户编排，只负责消费已经建立好的上下文

### 7.3 Local Facade

- 本地 facade 必须从 `BaconContextHolder` 读取当前上下文
- 读取到空的 `TenantId` 或 `UserId` 时必须立即失败
- 本地 facade 调用 application 时默认延续当前上下文
- 只有 application 方法被定义为特殊显式租户契约时，本地 facade 才显式传入 `TenantId`

### 7.4 Remote Facade

- 远程 facade 调用前必须把当前上下文带到远程边界
- 远程边界默认必须同时带出 `TenantId` 与 `UserId`
- 远程 HTTP 头部透传依赖统一客户端设施，不允许每个业务域重复拼接
- 远程 facade 不允许遗漏上下文后直接调用对端 provider

### 7.5 MQ

- 生产消息时必须把当前上下文写入 MQ header
- 消费消息时必须优先从 MQ header 恢复 `BaconContextHolder`
- 消费端进入 application 前，线程上下文中必须已经存在正确的 `TenantId` 与 `UserId`
- MQ 场景默认先恢复上下文，再进入 application
- MQ header 默认必须同时包含 `TenantId` 与 `UserId`
- 上下文消费完成后必须清理 `BaconContextHolder`

### 7.6 Async

- `@Async` 方法必须运行在受控 `taskExecutor` 上
- 手工提交线程池任务时，任务对象必须先经过 `AsyncTaskWrapper.wrap`
- 跨线程任务执行完成后必须清理 `BaconContextHolder`
- 定时任务、补偿任务、重试任务、批处理任务只要内部存在跨线程执行，同样适用本规则

### 7.7 Application To Repository

- application 默认依赖当前上下文，不默认显式传 `TenantId`
- repository 方法如果不带 `TenantId`，调用侧必须确认该仓储已受 `TenantScoped` 插件保护
- application 调用非 MyBatis 数据访问组件时，必须显式确认租户条件已在该组件内部生效

## 8. Key Flows

### 8.1 Request Flow

`Token + TenantCode -> UserId + TenantId resolve -> permission check -> BaconContextHolder -> business chain`

要求：

- 先鉴权，再建上下文
- `TenantId` 与 `UserId` 必须同时进入 `BaconContextHolder`
- 请求结束后必须清理 `BaconContextHolder`

### 8.2 HTTP Flow

`HTTP Request -> BaconContextFilter -> BaconContextHolder -> @CurrentTenant -> controller -> application -> repository -> MyBatis Tenant Plugin -> DB`

要求：

- 请求进入业务方法前必须已建立上下文
- application 默认依赖当前上下文
- repository 若依赖 MyBatis 插件透传，则 DO 必须为 `TenantScoped`
- 请求结束后必须清理上下文

### 8.3 Local Facade Flow

`upstream application -> BaconContextHolder -> local facade -> application -> repository`

要求：

- facade 从上下文取值
- application 默认依赖当前上下文
- repository 根据口径选择上下文透传或显式透传

### 8.4 Remote Flow

`upstream application -> remote facade -> context propagation -> provider controller -> application -> repository`

要求：

- 远程边界前必须显式带出上下文
- 远程边界默认必须同时带出 `TenantId` 与 `UserId`
- 对端 provider 必须恢复上下文后再进入业务

### 8.5 MQ Flow

`producer -> MQ header(context) -> consumer -> restore BaconContextHolder -> application -> repository`

要求：

- 消息跨线程、跨进程后，上下文仍然必须可恢复
- 仅有消息体业务参数，不等于上下文透传完成
- 消费完成后必须清理上下文

### 8.6 Async Flow

`caller thread -> BaconContextHolder.snapshot -> TaskDecorator / AsyncTaskWrapper -> worker thread -> BaconContextHolder.restore -> business logic -> BaconContextHolder.clear`

要求：

- 快照必须在提交任务前获取
- 恢复必须发生在工作线程执行用户逻辑前
- 清理必须发生在任务结束后

## 9. Non-Functional Requirements

- 上下文规则必须稳定，不允许因调用入口变化而切换口径
- 代码评审时必须检查“上下文模式”“显式透传”“MyBatis 插件透传”是否被混淆
- 代码评审时必须检查异步任务是否使用受控 `Executor`、`TaskDecorator` 或 `AsyncTaskWrapper`
- 代码评审时必须检查缓存值是否在写入前已经完成租户过滤
- 代码评审时必须检查 `TenantId` 与 `UserId` 是否被成对透传
- 测试必须至少覆盖一种入口场景，验证 `TenantId` 与 `UserId` 已被正确建立到 `BaconContextHolder`
- 测试必须至少覆盖一种异步场景，验证跨线程后上下文仍能恢复且执行后被清理
- 测试必须至少覆盖一种远程或消息场景，验证 `TenantId` 与 `UserId` 能被成对恢复
- 如果 repository 依赖 MyBatis 插件透传，测试必须证明查询或更新在有上下文时不会跨租户读写

## 10. Open Items

- 无
