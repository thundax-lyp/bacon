# Bacon项目结构

mono-app 与微服务并存的 Maven 多模块结构图

## 关键信息
- project: maven
- groupId: com.github.thundax
- articleId: bacon
- package: com.github.thundax.bacon
- version: 0.0.1-SNAPSHOT

## 总览结构图
```
bacon
│
├── pom.xml                          # 父POM
│
├── bacon-app/                       # 启动与部署层（装配层）
│   ├── bacon-boot/                  # 单体启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconApplication.java
│   │
│   ├── bacon-order-starter/         # 订单微服务启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconOrderApplication.java
│   │
│   ├── bacon-auth-starter/          # 登陆、授权服务启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconAuthApplication.java
│   │
│   ├── bacon-upms-starter/          # 用户、菜单、权限微服务启动模块
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconUpmsApplication.java
│   │
│   ├── bacon-register/              # Nacos服务器，微服务注册、配置服务器
│   │   ├── pom.xml
│   │   └── src/main/java/.../BaconNacosApplication.java
│   │
│   └── bacon-gateway/               # 网关
│       ├── pom.xml
│       └── src/main/java/.../BaconGatewayApplication.java
│
├── bacon-biz/                             # 业务域层
│   ├── bacon-order/
│   │   ├── bacon-order-interfaces/        # controller / facade / dto / vo
│   │   ├── bacon-order-application/       # app service / command / query
│   │   ├── bacon-order-domain/            # entity / domain service / repository接口
│   │   └── bacon-order-infra/             # mapper / repository impl / rpc client / cache
│   │
│   ├── bacon-upms/
│   │   ├── bacon-upms-interfaces/
│   │   ├── bacon-upms-application/
│   │   ├── bacon-upms-domain/
│   │   └── bacon-upms-infra/
│   │
│   ├── bacon-inventory/
│   │   ├── bacon-inventory-interfaces/
│   │   ├── bacon-inventory-application/
│   │   ├── bacon-inventory-domain/
│   │   └── bacon-inventory-infra/
│   │
│   └── bacon-payment/
│       ├── bacon-payment-interfaces/
│       ├── bacon-payment-application/
│       ├── bacon-payment-domain/
│       └── bacon-payment-infra/
│
├── bacon-common/                    # 平台公共能力
│   ├── pom.xml                      # 公共能力POM，统一依赖/插件/版本
│   ├── bacon-common-bom/            # 标定版本
│   ├── bacon-common-core/           # 异常/枚举/常量/工具
│   ├── bacon-common-web/            # web统一返回/拦截器/鉴权
│   ├── bacon-common-mysql/          # MyBatisPlug/MySQL基础封装
│   ├── bacon-common-cache/          # Redis/Caffeine封装
│   ├── bacon-common-mq/             # MQ封装
│   ├── bacon-common-mybatis/        # Mybatis-plus封装
│   ├── bacon-common-oss/            # s3api封装
│   ├── bacon-common-feign/          # Feign封装
│   ├── bacon-common-seata/          # Seata封装
│   ├── bacon-common-security/       # spring-security封装
│   ├── bacon-common-swagger/        # swagger封装
│   └── bacon-common-test/           # 测试基类/测试工具
│
└── deploy/                          # 部署脚本、Docker、配置样例
    ├── bacon-boot/
    ├── bacon-register/
    ├── bacon-gateway/
    ├── bacon-auth/
    ├── bacon-umps/
    ├── bacon-order/
    ├── bacon-invetory/
    └── bacon-payment/
```

## 装配关系图

核心思路是：业务模块只写一份，启动模块按需装配。

### 单体装配
```
bacon-boot
├── depends on bacon-order-interfaces
├── depends on bacon-order-application
├── depends on bacon-order-domain
├── depends on bacon-order-infrastructure
│
├── depends on bacon-upms-interfaces
├── depends on bacon-upms-application
├── depends on bacon-upms-domain
├── depends on bacon-upms-infrastructure
│
├── depends on bacon-inventory-*
├── depends on bacon-payment-*
└── depends on bacon-common/*
```
也就是：```bacon-boot = order + upms + inventory + payment + common```

### 单体和微服务共存时的调用关系

#### 在 bacon-boot 里
```
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

跨域调用时：
```
order-application
   ↓
UserReadFacade
   ↓
user-application
```
此时是本地调用。

#### 在微服务里

```
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

跨域调用时：

```
order-application
   ↓
UserReadFacade
   ↓
RPC Client / Feign
   ↓
user-service
```
此时是远程调用。

所以关键点是：

业务代码依赖 Facade 接口，不直接依赖对方实现

这样单体和微服务才能平滑切换。


## 一个业务域内部的标准结构

以 order 为例：

```
bacon-biz/bacon-order
├── bacon-order-interfaces
│   └── com.github.thundax.bacon.order.interfaces
│       ├── controller
│       ├── facade
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

## 对象放置规则

```
Request DTO / Response DTO / VO
    -> interfaces

Command / Query
    -> application

Entity / Aggregate / ValueObject
    -> domain

DO / PO / DataObject / Mapper
    -> infrastructure.persistence
```

## Maven 依赖方向

必须单向依赖，不能反过来。

```
interfaces      -> application
application     -> domain
infrastructure  -> domain
apps            -> interfaces + application + domain + infrastructure
common          -> 被各层依赖
```

说明：
- domain 定义仓储接口
- infrastructure 实现这些接口

## 补充说明
- 
- domain 是否禁止依赖 MyBatis
