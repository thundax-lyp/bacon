# Bacon系统架构

mono-app 与微服务并存的 Maven 多模块结构说明。

## 关键信息
- project: maven
- groupId: com.github.thundax
- artifactId: bacon
- base package: com.github.thundax.bacon
- version: 0.0.1-SNAPSHOT
- java: 17
- spring-boot: 3.5.11
- spring-cloud: 2025.0.1
- spring-cloud-alibaba: 2025.0.0.0
- commons-lang3: 3.20.0

## 总览结构图
```text
bacon
│
├── pom.xml                          # 根聚合 POM
│
├── bacon-app/                       # 启动与装配层
│   ├── pom.xml
│   ├── bacon-mono-boot/             # 单体启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconMonoApplication.java
│   │
│   ├── bacon-order-starter/         # 订单微服务启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconOrderApplication.java
│   │
│   ├── bacon-auth-starter/          # 登录、授权服务启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconAuthApplication.java
│   │
│   ├── bacon-upms-starter/          # 用户、菜单、权限服务启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconUpmsApplication.java
│   │
│   ├── bacon-register/              # 注册中心、配置中心
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconNacosApplication.java
│   │
│   └── bacon-gateway/               # 网关
│       ├── pom.xml
│       └── src/main/java/.../BaconGatewayApplication.java
│
├── bacon-biz/                       # 业务域层
│   ├── pom.xml
│   ├── bacon-order/
│   │   ├── pom.xml
│   │   ├── bacon-order-api/         # 跨域调用契约：facade / dto
│   │   ├── bacon-order-interfaces/  # controller / provider / http dto / vo
│   │   ├── bacon-order-application/ # app service / command / query
│   │   ├── bacon-order-domain/      # entity / domain service / repository 接口
│   │   └── bacon-order-infra/       # mapper / repository impl / rpc client / cache
│   │
│   ├── bacon-upms/
│   │   ├── pom.xml
│   │   ├── bacon-upms-api/
│   │   ├── bacon-upms-interfaces/
│   │   ├── bacon-upms-application/
│   │   ├── bacon-upms-domain/
│   │   └── bacon-upms-infra/
│   │
│   ├── bacon-inventory/
│   │   ├── pom.xml
│   │   ├── bacon-inventory-api/
│   │   ├── bacon-inventory-interfaces/
│   │   ├── bacon-inventory-application/
│   │   ├── bacon-inventory-domain/
│   │   └── bacon-inventory-infra/
│   │
│   └── bacon-payment/
│       ├── pom.xml
│       ├── bacon-payment-api/
│       ├── bacon-payment-interfaces/
│       ├── bacon-payment-application/
│       ├── bacon-payment-domain/
│       └── bacon-payment-infra/
│
├── bacon-common/                    # 平台公共能力
│   ├── pom.xml
│   ├── bacon-common-bom/            # 统一版本管理
│   ├── bacon-common-core/           # 异常 / 枚举 / 常量 / 工具
│   ├── bacon-common-log/            # 日志字段 / 事件类型 / 日志基础定义
│   ├── bacon-common-web/            # 统一返回 / 全局异常 / Web 拦截
│   ├── bacon-common-mybatis/        # MyBatis / MyBatis-Plus 基础封装
│   ├── bacon-common-cache/          # JetCache / Redis / Caffeine 基础封装
│   ├── bacon-common-mq/             # MQ 封装，按配置切换 RocketMQ / RabbitMQ / Kafka
│   ├── bacon-common-oss/            # 对象存储封装
│   ├── bacon-common-feign/          # Feign 封装
│   ├── bacon-common-seata/          # Seata 封装
│   ├── bacon-common-security/       # Spring Security 封装
│   ├── bacon-common-swagger/        # OpenAPI / Swagger 封装
│   └── bacon-common-test/           # 测试基类 / 测试工具
│
└── deploy/                          # 部署脚本、Docker、配置样例
    ├── bacon-mono-boot/
    ├── bacon-register/
    ├── bacon-gateway/
    ├── bacon-auth/
    ├── bacon-upms/
    ├── bacon-order/
    ├── bacon-inventory/
    └── bacon-payment/
```

## 装配关系图

核心思路是：业务模块只写一份，启动模块按需装配。

### 单体装配
```text
bacon-mono-boot
├── depends on bacon-order-api
├── depends on bacon-order-interfaces
├── depends on bacon-order-application
├── depends on bacon-order-domain
├── depends on bacon-order-infra
│
├── depends on bacon-upms-api
├── depends on bacon-upms-interfaces
├── depends on bacon-upms-application
├── depends on bacon-upms-domain
├── depends on bacon-upms-infra
│
├── depends on bacon-inventory-*
├── depends on bacon-payment-*
└── depends on bacon-common/*
```

也就是：`bacon-mono-boot = order + upms + inventory + payment + common`

### 单体和微服务共存时的调用关系

无论在单体还是微服务中，域内主调用链都保持一致：
```text
HTTP Request
   ↓
order-controller
   ↓
order-application
   ↓
order-domain
   ↓
order-repository
   ↓
MySQL / Redis
```

差异只在跨域调用方式：

#### 单体模式
```text
order-application
   ↓
UserReadFacade
   ↓
UserReadFacadeLocalImpl
   ↓
user-application
```

#### 微服务模式
```text
order-application
   ↓
UserReadFacade
   ↓
UserReadFacadeRemoteImpl
   ↓
RPC Client / Feign
   ↓
user-service
```

关键点是：业务代码依赖 Facade 接口，不直接依赖对方实现。同一个 `facade` 接口在单体和微服务下应存在两个不同的 Bean 实现，通过运行模式完成装配切换。

## 一个业务域内部的标准结构

以 `order` 为例：

```text
bacon-biz/bacon-order
├── bacon-order-api
│   └── com.github.thundax.bacon.order.api
│       ├── facade
│       └── dto
│
├── bacon-order-interfaces
│   └── com.github.thundax.bacon.order.interfaces
│       ├── controller
│       ├── provider
│       ├── facade
│       ├── dto
│       ├── response
│       ├── assembler
│       └── consumer
│
├── bacon-order-application
│   └── com.github.thundax.bacon.order.application
│       ├── service
│       ├── command
│       ├── query
│       ├── executor
│       └── assembler
│
├── bacon-order-domain
│   └── com.github.thundax.bacon.order.domain
│       ├── model
│       │   ├── entity
│       │   ├── aggregate
│       │   └── valueobject
│       ├── service
│       ├── repository
│       ├── event
│       └── factory
│
└── bacon-order-infra
    └── com.github.thundax.bacon.order.infra
        ├── persistence
        │   ├── mapper
        │   ├── dataobject
        │   └── repositoryimpl
        ├── facade
        │   └── remote
        ├── rpc
        ├── cache
        ├── mq
        └── config
```

## 分层职责

### interfaces
- 面向统一接入协议层，承载 HTTP、消息消费以及服务提供方的 provider 入口适配。
- 对外暴露 HTTP 接口、MQ consumer，并承载服务提供方的 provider 入口。
- 承载本地 Facade 适配实现，固定放在 `interfaces.facade`，例如 `UserReadFacadeLocalImpl`。
- 负责接收请求、参数校验、协议适配、返回值组装。
- provider 只是 `api.facade` 的传输适配入口，不额外定义第二套业务契约。
- 可以依赖 `application`，不能直接访问 `domain repository` 或 `infra mapper`。

### api
- 面向内部跨域调用契约层，承载业务域之间稳定的调用边界。
- 负责定义跨业务域调用契约，只放 `facade` 和跨域 `dto`。
- 只表达稳定的业务能力，不承载 HTTP 语义，也不暴露领域实体。
- 可被其他业务域的 `application` 或 `infra.rpc` 依赖。

### application
- 负责用例编排、事务边界、权限校验、幂等控制、跨域协调。
- 只表达业务动作，不关心数据库、RPC、缓存具体实现。
- 可以依赖 `domain` 和外部 `api.facade` 抽象，不能直接依赖其他域的 infra 实现。

### domain
- 负责核心业务规则、聚合、一致性约束、领域服务、仓储接口定义。
- 不关心 Spring MVC、MyBatis、Feign、Redis、MQ 等技术细节。
- 应尽量保持纯 Java，对框架依赖最小化。

### infra
- 负责持久化、RPC 客户端、缓存、消息发送、三方适配。
- 实现 `domain.repository` 中定义的接口。
- 承载远程 Facade 适配实现，固定放在 `infra.facade.remote`，例如 `UserReadFacadeRemoteImpl`。
- 可以依赖数据库、中间件、SDK，但不能反向让 `domain` 依赖这些技术细节。
- `Order`、`Payment`、`Inventory` 的业务单号生成固定由 `infra` 层集成发号中心客户端完成。

## 业务单号策略

- `Order`、`Payment`、`Inventory` 的业务单号固定使用发号中心方案。
- 发号客户端固定使用 `tinyid-client`。
- 发号模式固定使用“发号中心 + 本地缓存号段”。
- `orderNo`、`paymentNo`、`reservationNo` 属于业务单号，必须通过 `tinyid-client` 生成。
- 业务单号生成职责属于各业务域 `infra` 层，不得在 `controller`、`application` 或 `domain` 层手写 `AtomicLong`、时间戳拼接或数据库 `max(id)+1` 发号。
- 业务审计日志的持久化实现固定放在各业务域 `infra`。

### starter/app
- 负责 Bean 装配、组件扫描、配置加载、运行入口。
- 决定当前进程是单体模式还是某个微服务模式。
- 不承载具体业务实现，只做装配。

## 对象约定

- HTTP 入参对象放在 `interfaces.dto`，统一使用 `*Request`
- 前端 Controller 返回对象放在 `interfaces.response`，统一使用 `*Response`
- `interfaces.vo` 仅允许保留历史兼容对象，不再新增
- 跨域调用 DTO 放在 `api.dto`，统一使用 `*DTO`
- Command / Query / Result 放在 `application`
- Entity / Aggregate / ValueObject 放在 `domain`
- DO / PO / DataObject / Mapper 放在 `infra.persistence`
- `Assembler` 优先放在调用方所在层，用于本层对象转换。
- `facade` 放在 `api.facade`，不再放在 `interfaces`
- `api.facade` 只定义接口；`interfaces.facade` 只放本地实现 `*LocalImpl`；`infra.facade.remote` 只放远程实现 `*RemoteImpl`
- `Command` 用于应用层入参，不向外暴露，也不复用为 HTTP DTO 或 RPC DTO
- `Query` 用于应用层读模型入参，不向外暴露，也不复用为 HTTP DTO 或 RPC DTO
- `Result` 用于应用层用例输出，不向外暴露，也不复用为 HTTP DTO 或 RPC DTO
- `interfaces.dto` / `interfaces.response` 只服务接入层，不参与跨域调用
- provider 接口默认直接复用 `api.dto`，不再单独定义 provider DTO
- provider 接口如需对内请求模型，使用 `interfaces.dto` 下的 `*Request`；如直接复用简单参数或 `api.dto`，不得再额外派生一套 provider response
- HTTP DTO、HTTP Response、跨域 DTO、领域对象默认不复用，避免协议层和领域层互相污染
- 枚举、异常码、通用常量优先沉淀到 `bacon-common-core`，业务私有的留在各自域内。

### 命名规则

- `interfaces.controller`
  - 入参统一使用 `*Request`
  - 出参统一使用 `*Response`
  - 禁止新增 `*DTO`
- `interfaces.provider`
  - 默认直接复用 `api.dto`
  - 如必须定义接入层入参对象，只允许新增 `*Request`
  - 禁止新增 provider `*Response`
- `api.dto`
  - 统一使用 `*DTO`
  - 禁止新增 `*Request` / `*Response`
- `application.command`
  - 统一使用 `*Command`
- `application.query`
  - 统一使用 `*Query`
- `application.result`
  - 统一使用 `*Result`
  - 禁止继续使用 `application.dto`

## Maven 依赖方向

必须单向依赖，不能反过来。

```text
api         -> 无业务实现依赖
interfaces  -> application
application -> domain + 外部 api
infra       -> domain + 外部 api
app/starter -> api + interfaces + application + domain + infra
common      -> 被各层依赖
```

说明：
- `domain` 定义仓储接口。
- `infra` 实现这些接口。
- `api` 只定义契约，不依赖 `application`、`domain`、`infra` 实现。
- `interfaces` 不得直接依赖 `infra`。
- 一个业务域原则上不得直接依赖另一个业务域的 `application` 实现。

## 跨域调用约定

### 调用原则
- 调用方依赖被调用方暴露的 `api.facade` 接口，不直接依赖其 `service`、`repository`、`mapper`。
- 单体模式下，由被调用方提供 `facade` 的本地实现 Bean，内部转调本域 `application`。
- 微服务模式下，由调用方在 `infra.rpc` 中提供 `facade` 的远程实现 Bean，内部通过 Feign / RPC client 调目标服务。
- 同一个 `facade` 接口在容器中只能有一个生效 Bean，避免本地实现和远程实现同时注入。

### 推荐放置方式
- 对外读写能力抽象放在 `<domain>-api` 模块的 `api.facade`。
- 跨域 DTO 放在 `<domain>-api` 模块的 `api.dto`。
- 本地实现放在被调用方，作为 `api.facade` 的本地适配实现，例如 `UserReadFacadeLocalImpl`。
- 远程调用实现放在调用方的 `infra.rpc` 或公共 `common-feign` 扩展中，例如 `UserReadFacadeRemoteImpl`。
- 服务提供方的 provider 入口固定放在 `interfaces.provider`，本地 `Facade` 适配实现固定放在 `interfaces.facade`，但都必须直接对齐 `api.facade`。
- 业务编排始终写在 `application`，不要写进 Feign client。

## Mono-App 约定

### 运行模式
- `mono-app` 中，各业务域运行在同一个 JVM 中，但仍保持业务边界不变。
- `bacon-mono-boot` 负责统一装配多个业务域，并选择 `facade` 的本地实现 Bean。
- 即使在同一进程内，也不因为“调用方便”而绕过 `api.facade`。

### Facade 双 Bean 设计
- 每个跨域 `facade` 接口都应预留两种实现：
    - 本地实现 Bean：`<FacadeName>LocalImpl`
    - 远程实现 Bean：`<FacadeName>RemoteImpl`
- `mono-app` 模式只启用 `LocalImpl`。
- 微服务模式只启用 `RemoteImpl`。
- 两种实现必须实现同一个 `api.facade` 接口，对 `application` 层保持透明。

### Spring 装配规范
- 统一使用运行模式配置项控制 Bean 生效，例如：`bacon.runtime.mode=mono` 或 `bacon.runtime.mode=micro`。
- `LocalImpl` 使用条件装配，仅在 `mono` 模式生效。
- `RemoteImpl` 使用条件装配，仅在 `micro` 模式生效。
- 不允许通过 `@Primary`、字段名注入、手工排除扫描等方式“碰运气”解决冲突，必须通过显式条件装配保证容器内只有一个实现。
- 建议统一使用 `@ConditionalOnProperty` 或等价的自定义条件注解，例如：

```java
@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono")
public class UserReadFacadeLocalImpl implements UserReadFacade {
}
```

```java
@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class UserReadFacadeRemoteImpl implements UserReadFacade {
}
```

- 如果项目后续需要统一治理，可进一步封装为：
    - `@ConditionalOnMonoApp`
    - `@ConditionalOnMicroservice`
- 所有 `facade` 实现都应遵循同一套装配规则，不能某些域用配置切换，某些域靠代码硬编码。

### 单体装配规则
- `bacon-mono-boot` 启动时默认装配各域的本地 `facade` 实现。
- `infra.rpc` 中的远程 `facade` 实现、Feign Client、远程专用配置在 `mono-app` 模式下默认禁用。
- MQ consumer、定时任务、RPC provider 等需要根据运行模式和服务职责决定是否启用，不能因为模块被引入就自动全部生效。
- 单体模式下跨域调用路径应固定为：
    - `caller-application -> callee-api.facade -> callee LocalImpl -> callee-application`

### 单体事务约定
- 跨域编排由发起方 `application` 负责，不能跨域共享 `repository` 或 `mapper`。
- 单体模式下如果多个域在同一个本地事务中协作，事务边界仍定义在发起方 `application`。
- 是否允许跨域事务应谨慎控制，优先保证领域边界，再考虑事务便利性。

## 微服务约定

### 服务提供方暴露模型
- 微服务中的服务提供方负责暴露可远程访问的 provider 入口，但 provider 只是传输适配层，不承载业务编排。
- 项目选择不拆分 external / internal provider，统一由 `interfaces` 承载 provider 入口。
- provider 接口必须与 `api.facade` / `api.dto` 对齐，统一使用一套 API 契约，不再额外定义第二套内部服务接口。
- provider 内部调用本域 `application`，不允许直接调用 `domain.repository` 或 `infra.persistence`。

### 独立 Starter 装配规则
- `bacon-order-starter` 只装配：
    - `bacon-order-api`
    - `bacon-order-interfaces`
    - `bacon-order-application`
    - `bacon-order-domain`
    - `bacon-order-infra`
    - `bacon-common-*`
    - 当前服务需要的外部 `api` 模块与对应 `RemoteImpl`
- `bacon-order-starter` 不装配其他业务域的本地 `facade` 实现。
- 非 `bacon-mono-boot` 的独立服务原则上只拥有本域业务实现，跨域能力通过外部 `api + RemoteImpl` 获取。
- 各 starter 必须显式声明自身运行模式，不能在微服务 starter 中复用单体装配逻辑。

### 同步与异步调用边界
- 查询类跨域调用优先使用同步 `facade`。
- 写操作、状态变更、长链路编排优先考虑事件驱动、消息通知或最终一致性方案。
- 不要把所有跨域写操作都设计成同步 RPC，否则服务边界会退化为远程单体。
- 如果必须同步写入跨域服务，需要明确调用原因、失败补偿和超时策略。

### 服务治理约定
- 服务注册名保持统一命名，例如 `bacon-order-service`、`bacon-upms-service`。
- 配置中心需统一约定 `namespace`、`group`、`dataId` 命名规则。
- Feign / RPC client 需统一超时、重试、熔断、日志追踪策略，避免各服务各自配置。
- `api.dto` 作为跨服务契约对象，需要关注兼容性，新增字段优先向后兼容，避免破坏性变更。
- `bacon-common-feign`、`bacon-common-security` 作为微服务基础能力模块使用；`bacon-common-seata` 仅在确有分布式事务需求时启用，不视为默认标配。

## 禁止事项

- `domain` 禁止依赖 MyBatis、JPA、Feign、Redis、MQ SDK。
- `domain` 禁止出现 `@RestController`、`@Service`、`@Mapper`、`@TableName` 等面向基础设施的注解。
- `application` 禁止直接操作 `Mapper`、`DAO`、`FeignClient`。
- `interfaces` 禁止直接编写数据库访问逻辑。
- `interfaces` 禁止定义跨域调用契约。
- `interfaces` 禁止再单独定义 provider DTO 或第二套内部服务契约。
- `api` 禁止放 `VO`、`Command`、Controller DTO、领域实体。
- `starter` 禁止沉淀业务规则和业务状态。
- 即使在 `mono-app` 中，也禁止因为同 JVM 部署而直接依赖其他业务域的 `application` 实现、`repository`、`mapper`。
- 禁止同时激活同一个 `facade` 接口的本地实现和远程实现。
- 微服务 starter 禁止引入其他业务域的本地 `facade` 实现。
- 禁止把跨域写操作默认设计成同步 RPC 链路。
- 不允许跨域直接引用对方 `infra`、`controller`、`application.service` 实现类。
- 禁止在业务域模块中重复声明工程级依赖版本。
- 禁止在 `domain` 中直接依赖 `nacos`、`jasypt`、`actuator`、`spring-boot-admin`。
- 禁止在各业务模块内各自维护一套 `springdoc`、`mybatis-plus`、`checkstyle` 配置。
- 禁止在同一模块内同时把 Hutool `StrUtil` 和 Apache `StringUtils` 作为常规字符串工具混用。
- 禁止在 `jetcache` 之外再抽象一套通用缓存门面。
- 禁止在 `mybatis-plus + mapper + repositoryimpl` 之外再叠加通用 DAO 框架。
- 禁止在网关模块中继续使用 Spring Cloud 2025 已废弃的旧 gateway starter 名称。

## 模块创建清单

当新增一个业务域时，至少创建以下模块：

```text
bacon-biz/bacon-<domain>/
├── pom.xml
├── bacon-<domain>-api
├── bacon-<domain>-interfaces
├── bacon-<domain>-application
├── bacon-<domain>-domain
└── bacon-<domain>-infra
```

建议同时补齐以下基础内容：
- 四层模块各自的 `pom.xml`
- 标准包结构
- 一个最小闭环示例：controller/provider -> application -> domain -> repository
- 启动模块中的依赖装配
- `deploy/bacon-<domain>/` 下的配置样例和 Dockerfile

## 配置与部署约定

至少需要补齐以下内容，否则结构完整但工程无法启动：

- 统一运行模式配置，例如：
    - `bacon.runtime.mode=mono`
    - `bacon.runtime.mode=micro`
- 每个 starter 模块提供独立 `application.yml`
- 环境差异配置通过 profile 或配置中心管理
- `deploy/` 下为每个可部署服务提供：
    - `Dockerfile`
    - 启动脚本
    - 配置样例
    - 数据库初始化脚本或链接说明
- 注册中心、网关、认证服务的端口和服务名统一约定

## 数据库命名约定

- 表名固定格式使用 `bacon_${domain}_${model}`
- 各业务域表名前缀固定如下：
    - `Auth` 使用 `bacon_auth_`
    - `UPMS` 使用 `bacon_upms_`
    - `Order` 使用 `bacon_order_`
    - `Inventory` 使用 `bacon_inventory_`
    - `Payment` 使用 `bacon_payment_`
- 关系表在 `model` 后缀中显式体现关系语义，例如 `*_user_role_rel`
- 审计日志表统一使用 `*_audit_log`
- 表名不得省略 `bacon_` 系统前缀
- 新增业务域时，必须同步确定该业务域的数据库前缀并保持与模块名一致
- 数据结构设计文档、DDL、`DataObject`、`Mapper` XML、缓存来源说明中的表名必须保持一致

## 日志归属约定

- 业务审计日志属于业务域基础设施能力，固定落在各业务域 `infra`
- `application` 负责定义记录时机、记录内容和失败处理策略
- `domain` 负责提供业务语义与状态变化，不直接依赖日志框架或审计落库实现
- `interfaces` 可补充请求来源、客户端 `ip`、`userAgent` 等接入层上下文，但不直接承担审计日志持久化
- `starter/app` 负责技术日志、访问日志、链路追踪、日志级别、日志格式和日志输出配置
- 业务域不得把审计日志实现放入 `api`、`application`、`domain`、`starter`
- 通用日志配置放在工程级配置或启动模块，不在业务域内重复维护一套日志框架配置
- 审计日志写入失败不得影响主业务提交结果，具体降级策略由各业务域 `application + infra` 实现

## 工程级依赖与插件归属

### 父 POM 统一管理
- 以下依赖或插件在根父 `pom.xml` 中统一声明版本、依赖管理或插件管理：
    - `flatten-maven-plugin`
    - `maven-checkstyle-plugin`
    - `lombok`
- 这些属于工程构建和代码治理能力，不进入业务模块职责划分。

### 公共模块归属
- `bacon-common-core`
    - `hutool`
- `bacon-common-mybatis`
    - `mybatis-plus`
- `bacon-common-cache`
    - `jetcache`
- `bacon-common-swagger`
    - `springdoc`

### Starter / 运行时治理层归属
- 以下依赖统一在 starter 层接入和配置：
    - `nacos`
    - `jasypt`
    - `spring-boot-starter-actuator`
    - `spring-boot-admin-starter-client`
- 这些能力属于运行时配置、服务注册、配置管理、可观测性和运维治理，不进入 `api`、`application`、`domain` 的业务设计。

### 使用约束
- `lombok`
    - 可用于 `interfaces.dto`、`interfaces.vo`、`api.dto`、`application.command`、`application.query`、`infra.persistence.dataobject`
    - `domain` 中谨慎使用，不建议领域实体直接使用 `@Data`
- `hutool`
    - 用于通用工具能力，优先放在 `bacon-common-core`
    - `domain` 中避免重度依赖，尽量保持纯 Java
    - 字符串处理不作为默认首选，优先使用 `org.apache.commons.lang3.StringUtils`
- `commons-lang3`
    - 作为基础字符串与对象工具库统一使用
    - 默认使用 `org.apache.commons.lang3.StringUtils`
    - 不建议与 Hutool 的字符串工具在同一模块内混用
- `mybatis-plus`
    - 由 `bacon-common-mybatis` 统一封装分页、通用字段填充、基础配置
    - 业务域 `infra.persistence` 只保留 mapper、dataobject、repositoryimpl 等实现
    - 持久化统一主实现采用 `mybatis-plus + mapper + repositoryimpl`，不再叠加 DAO 基础框架
- `jetcache`
    - 由 `bacon-common-cache` 统一封装缓存配置、key 规范、过期策略和序列化策略
    - 缓存统一主实现采用 `jetcache`，不再额外封装一套通用缓存门面
- `springdoc`
    - 由 `bacon-common-swagger` 统一封装分组、鉴权头、文档开关
    - 各 starter 按需启用，不在业务模块内重复配置
- `nacos`
    - 由 starter 统一接入服务注册与配置中心
    - 统一约定服务名、`namespace`、`group`、`dataId`
- `jasypt`
    - 用于加密配置项，例如数据库密码、Nacos 密钥、第三方密钥
    - 不进入业务域对象与业务逻辑
- `spring-boot-starter-actuator`
    - 由 starter 统一暴露健康检查、指标、探针
    - management 端口、路径、暴露端点和鉴权策略统一配置
- `spring-boot-admin-starter-client`
    - 由 starter 统一配置 admin server 地址、实例名、metadata、鉴权信息
    - 业务代码不得依赖 admin client API
- 网关
    - `bacon-gateway` 直接使用 Spring Cloud 2025 对应的新 gateway starter 名称
    - 不再引入已废弃的旧 gateway starter 名称

## 测试与治理约定

- `bacon-common-test` 提供单元测试和集成测试基类。
- application 层优先做用例测试，domain 层优先做纯业务规则测试。
- 对依赖方向做静态校验，避免四层被逐步打穿。
- 父 POM 统一插件版本、编码、JDK、测试插件、覆盖率和代码格式化规则。

## 新增业务域的标准步骤

1. 在 `bacon-biz` 下创建领域聚合模块和四层子模块。
2. 在 `api` 中定义 facade 和跨域 dto。
3. 在 `domain` 中定义实体、聚合、仓储接口和领域服务。
4. 在 `application` 中定义 command、query、service、executor。
5. 在 `interfaces` 中定义 controller、provider、http dto、vo。
6. 在 `infra` 中实现 repository、mapper、rpc、cache。
7. 在对应 starter 中引入相关模块并完成装配。
8. 在 `deploy` 中补齐配置样例和镜像脚本。
9. 先跑通一个最小业务闭环，再逐步扩展。
