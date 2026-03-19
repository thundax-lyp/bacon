# Bacon项目结构

mono-app 与微服务并存的 Maven 多模块结构说明。

## 关键信息
- project: maven
- groupId: com.github.thundax
- artifactId: bacon
- base package: com.github.thundax.bacon
- version: 0.0.1-SNAPSHOT
- java: 17

## 总览结构图
```text
bacon
│
├── pom.xml                          # 根聚合 POM
│
├── bacon-app/                       # 启动与装配层
│   ├── pom.xml
│   ├── bacon-boot/                  # 单体启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconApplication.java
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
│   │   ├── bacon-order-interfaces/  # controller / http dto / vo
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
│   ├── bacon-common-web/            # 统一返回 / 全局异常 / Web 拦截
│   ├── bacon-common-mysql/          # MySQL / MyBatis-Plus 基础封装
│   ├── bacon-common-cache/          # Redis / Caffeine 封装
│   ├── bacon-common-mq/             # MQ 封装
│   ├── bacon-common-oss/            # 对象存储封装
│   ├── bacon-common-feign/          # Feign 封装
│   ├── bacon-common-seata/          # Seata 封装
│   ├── bacon-common-security/       # Spring Security 封装
│   ├── bacon-common-swagger/        # OpenAPI / Swagger 封装
│   └── bacon-common-test/           # 测试基类 / 测试工具
│
└── deploy/                          # 部署脚本、Docker、配置样例
    ├── bacon-boot/
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
bacon-boot
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

也就是：`bacon-boot = order + upms + inventory + payment + common`

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
user-application
```

#### 微服务模式
```text
order-application
   ↓
UserReadFacade
   ↓
RPC Client / Feign
   ↓
user-service
```

关键点是：业务代码依赖 Facade 接口，不直接依赖对方实现。这样单体和微服务才能平滑切换。

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
│       ├── dto
│       ├── vo
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
        │   ├── dao
        │   ├── dataobject
        │   └── repositoryimpl
        ├── rpc
        ├── cache
        ├── mq
        └── config
```

## 分层职责

### interfaces
- 面向外部接入协议层，承载 HTTP、消息消费等入口适配。
- 对外暴露 HTTP 接口、MQ consumer。
- 负责接收请求、参数校验、协议适配、返回值组装。
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
- 可以依赖数据库、中间件、SDK，但不能反向让 `domain` 依赖这些技术细节。

### starter/app
- 负责 Bean 装配、组件扫描、配置加载、运行入口。
- 决定当前进程是单体模式还是某个微服务模式。
- 不承载具体业务实现，只做装配。

## 对象约定

- HTTP Request DTO / Response DTO 放在 `interfaces.dto`
- 页面展示或接口返回对象放在 `interfaces.vo`
- 跨域调用 DTO 放在 `api.dto`，只用于业务域之间调用
- Command / Query 放在 `application`
- Entity / Aggregate / ValueObject 放在 `domain`
- DO / PO / DataObject / Mapper 放在 `infra.persistence`
- `Assembler` 优先放在调用方所在层，用于本层对象转换。
- `facade` 放在 `api.facade`，不再放在 `interfaces`
- `Command` 用于应用层入参，不向外暴露，也不复用为 HTTP DTO 或 RPC DTO
- `interfaces.dto` / `interfaces.vo` 只服务接入层，不参与跨域调用
- HTTP DTO、跨域 DTO、领域对象默认不复用，避免协议层和领域层互相污染
- 枚举、异常码、通用常量优先沉淀到 `bacon-common-core`，业务私有的留在各自域内。

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
- 单体模式下，由被调用方提供 `facade` 的本地实现，内部转调本域 `application`。
- 微服务模式下，由调用方在 `infra.rpc` 中提供 `facade` 的远程实现，内部通过 Feign / RPC client 调目标服务。

### 推荐放置方式
- 对外读写能力抽象放在 `<domain>-api` 模块的 `api.facade`。
- 跨域 DTO 放在 `<domain>-api` 模块的 `api.dto`。
- 本地实现放在被调用方，作为 `api.facade` 的适配实现。
- 远程调用实现放在调用方的 `infra.rpc` 或公共 `common-feign` 扩展中。
- 业务编排始终写在 `application`，不要写进 Feign client。
- `VO` 只用于 interfaces 出参，`Command` 只用于 application 入参，不参与跨域调用。

## 禁止事项

- `domain` 禁止依赖 MyBatis、JPA、Feign、Redis、MQ SDK。
- `domain` 禁止出现 `@RestController`、`@Service`、`@Mapper`、`@TableName` 等面向基础设施的注解。
- `application` 禁止直接操作 `Mapper`、`DAO`、`FeignClient`。
- `interfaces` 禁止直接编写数据库访问逻辑。
- `interfaces` 禁止定义跨域调用契约。
- `api` 禁止放 `VO`、`Command`、Controller DTO、领域实体。
- `starter` 禁止沉淀业务规则和业务状态。
- 不允许跨域直接引用对方 `infra`、`controller`、`application.service` 实现类。

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
- 一个最小闭环示例：controller -> application -> domain -> repository
- 启动模块中的依赖装配
- `deploy/bacon-<domain>/` 下的配置样例和 Dockerfile

## 配置与部署约定

至少需要补齐以下内容，否则结构完整但工程无法启动：

- 每个 starter 模块提供独立 `application.yml`
- 环境差异配置通过 profile 或配置中心管理
- `deploy/` 下为每个可部署服务提供：
  - `Dockerfile`
  - 启动脚本
  - 配置样例
  - 数据库初始化脚本或链接说明
- 注册中心、网关、认证服务的端口和服务名统一约定

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
5. 在 `interfaces` 中定义 controller、http dto、vo。
6. 在 `infra` 中实现 repository、mapper、rpc、cache。
7. 在对应 starter 中引入相关模块并完成装配。
8. 在 `deploy` 中补齐配置样例和镜像脚本。
9. 先跑通一个最小业务闭环，再逐步扩展。
