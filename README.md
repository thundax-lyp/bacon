# Bacon

`Bacon` 是一个基于 `Spring Boot 3.5`、`Spring Cloud 2025`、`Spring Cloud Alibaba 2025` 的混合架构后端工程，支持单体装配与微服务装配并存。

## 项目定位

- 统一技术栈：`Java 17`、`Maven`、`Spring Boot 3`
- 统一业务方向：认证、用户权限、订单、库存、支付
- 统一工程形态：同一套业务模块同时支持单体和微服务运行模式

## 目录结构

```text
bacon
├── bacon-app/      # 启动与装配层
├── bacon-biz/      # 业务域层
├── bacon-common/   # 公共基础能力
├── docs/           # 架构、需求、数据库、文档规范
└── deploy/         # 部署脚本与配置样例
```

核心公共模块包括：

- `bacon-common-core`：配置、异常、上下文、缓存验证码等公共基础能力
- `bacon-common-web`：统一 Web 响应模型
- `bacon-common-security`：安全上下文与权限能力
- `bacon-common-mybatis`：MyBatis / MyBatis-Plus 基础封装
- `bacon-common-feign`：Feign 基础封装
- `bacon-common-mq`：消息队列基础封装

## 业务域

- `Auth`：登录、令牌、会话、`OAuth2`
- `UPMS`：用户、组织、角色、菜单、资源、数据权限
- `Order`：订单下单、查询、支付结果处理
- `Inventory`：库存查询、预占、释放、扣减
- `Payment`：支付单、回调、关闭、状态查询

## 开发说明

首次阅读建议按以下顺序：

1. [docs/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)
2. [docs/README.md](/Volumes/storage/workspace/bacon/docs/README.md)
3. 当前任务所属业务域的 `*-REQUIREMENTS.md`

常用命令：

```bash
mvn clean verify
mvn test
mvn checkstyle:check
mvn -pl bacon-app/bacon-mono-boot spring-boot:run
```

Inventory 模块补充测试（可按需单独执行）：

```bash
mvn -q -pl bacon-biz/bacon-inventory/bacon-inventory-infra -am test -Dtest=InMemoryInventoryRepositorySupportTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl bacon-biz/bacon-inventory/bacon-inventory-interfaces -am test -Dtest=InventoryControllerContractTest,InventoryProviderControllerContractTest -Dsurefire.failIfNoSpecifiedTests=false
```

## JDK 约定

- 日常开发可以使用 JDK 19
- Maven 编译、单元测试、集成测试统一使用 JDK 17
- 根 `pom.xml` 已通过 Maven Toolchains 固定要求 JDK 17

首次配置时，将 [` .mvn/toolchains.example.xml`](/Volumes/storage/workspace/bacon/.mvn/toolchains.example.xml) 复制为 `~/.m2/toolchains.xml`，并将其中的 `jdkHome` 改为本机实际安装路径。

## 运行模式

- 单体模式：使用 `bacon-app/bacon-mono-boot`
- 微服务模式：按业务域分别启动 `bacon-auth-starter`、`bacon-upms-starter`、`bacon-order-starter`、`bacon-inventory-starter`、`bacon-payment-starter`

业务代码保持统一分层：

```text
interfaces -> application -> domain -> infra
```

跨域调用统一依赖 `Facade` 契约，不直接依赖其他业务域内部实现。

## 文档索引

- [docs/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)：项目架构与模块边界
- [docs/README.md](/Volumes/storage/workspace/bacon/docs/README.md)：文档加载索引
- [docs/AUTH-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/AUTH-REQUIREMENTS.md)
- [docs/UPMS-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/UPMS-REQUIREMENTS.md)
- [docs/ORDER-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/ORDER-REQUIREMENTS.md)
- [docs/INVENTORY-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/INVENTORY-REQUIREMENTS.md)
- [docs/PAYMENT-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/PAYMENT-REQUIREMENTS.md)

## License

本项目使用 Apache License 2.0，详见 [LICENSE](/Volumes/storage/workspace/bacon/LICENSE)。
