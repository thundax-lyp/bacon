# Bacon

面向企业级场景的后端业务平台，支持 `mono` 与 `micro` 双运行模式。

## 项目概览

`Bacon` 是一个基于 `Java 17`、`Spring Boot 3.5`、`Spring Cloud 2025`、`Spring Cloud Alibaba 2025` 的 Maven 多模块工程。

当前核心业务域：

- `Auth`：认证、会话、OAuth2
- `UPMS`：用户、租户、组织、角色、菜单、资源、数据权限
- `Order`：订单创建、状态流转、取消、超时关闭
- `Inventory`：库存查询、预占、释放、扣减、审计
- `Payment`：支付单、回调、关单、状态查询
- `Storage`：统一存储对象、上传、引用、访问地址

## 仓库结构

```text
bacon
├── bacon-app/      # 启动与装配层
├── bacon-biz/      # 业务域模块
├── bacon-common/   # 公共能力模块
├── deploy/         # 部署样例与脚本
└── docs/           # 架构、需求、数据库设计文档
```

关键目录：

- `bacon-app`
  - `bacon-mono-boot`：单体启动模块
  - `bacon-*-starter`：各业务域微服务启动模块
- `bacon-biz`
  - 每个业务域保持 `api / interfaces / application / domain / infra` 五层结构
- `bacon-common`
  - 公共基础设施能力，如 `mybatis`、`cache`、`mq`、`security`、`swagger`

## 架构约定

- 分层依赖方向固定为：`interfaces -> application -> domain <- infra`
- 跨域调用固定通过 `api.facade`
- 本地适配实现固定放在 `interfaces.facade`
- 远程适配实现固定放在 `infra.facade.remote`
- 正式仓储默认注册，不使用 `@ConditionalOnBean` 规避主链路装配
- 多实现互斥装配使用显式配置项配合 `@ConditionalOnProperty`
- 环境边界切换优先使用 `@Profile`

更多细节见 [ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)。

## 文档入口

开始任何实现前，先读：

1. [docs/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)
2. [docs/README.md](/Volumes/storage/workspace/bacon/docs/README.md)
3. 对应业务域 `*-REQUIREMENTS.md`

涉及数据库设计时，继续读：

1. [docs/DATABASE-RULES.md](/Volumes/storage/workspace/bacon/docs/DATABASE-RULES.md)
2. 对应业务域 `*-DATABASE-DESIGN.md`

统一 ID 建模见：

- [docs/UNIFIED-ID-DESIGN.md](/Volumes/storage/workspace/bacon/docs/UNIFIED-ID-DESIGN.md)

## 本地构建

在仓库根目录执行：

```bash
mvn clean verify
mvn test
mvn checkstyle:check
```

按模块构建示例：

```bash
mvn -pl bacon-app/bacon-order-starter -am -DskipTests install
```

## 本地运行

单体模式：

```bash
./scripts/run-mono.sh
```

或：

```bash
mvn -pl bacon-app/bacon-mono-boot spring-boot:run
```

微服务模式示例：

```bash
mvn -pl bacon-app/bacon-order-starter -am -DskipTests install
mvn -pl bacon-app/bacon-order-starter spring-boot:run
```

## 开发规则

- 基础包名：`com.github.thundax.bacon`
- Java、XML、YAML 统一使用 4 空格缩进
- HTTP 入参放在 `interfaces.dto`，统一使用 `*Request`
- HTTP 出参放在 `interfaces.response`，统一使用 `*Response`
- 跨域 DTO 放在 `api.dto`，统一使用 `*DTO`
- 领域对象、业务单号、数据库主键必须分开建模
- 文档、代码、数据库结构必须同步演进

## 常用文档

- [docs/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)
- [docs/AUTH-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/AUTH-REQUIREMENTS.md)
- [docs/UPMS-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/UPMS-REQUIREMENTS.md)
- [docs/ORDER-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/ORDER-REQUIREMENTS.md)
- [docs/INVENTORY-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/INVENTORY-REQUIREMENTS.md)
- [docs/PAYMENT-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/PAYMENT-REQUIREMENTS.md)
- [docs/STORAGE-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/STORAGE-REQUIREMENTS.md)

## License

Apache License 2.0，详见 [LICENSE](/Volumes/storage/workspace/bacon/LICENSE)。
