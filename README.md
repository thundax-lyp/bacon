# Bacon

> 面向企业级场景的云原生 RBAC 与业务平台，基于 Spring Cloud Alibaba 2025、Spring Boot 3 与 OAuth2。

## 项目简介

`Bacon` 是一个支持 **单体（Mono）** 与 **微服务（Micro）** 双运行模式的后端工程，统一承载以下核心领域能力：

- `Auth`：认证、会话、OAuth2
- `UPMS`：用户与组织、角色权限、菜单资源、数据权限
- `Order`：订单生命周期与跨域编排
- `Inventory`：库存查询、预占、释放、扣减与审计
- `Payment`：支付单、回调、关单与状态流转

---

## AI 快速上下文

```yaml
project:
  name: bacon
  language: Java
  build: Maven
  java: 17
  spring_boot: 3.5.x
  spring_cloud: 2025.x
  spring_cloud_alibaba: 2025.x

runtime_modes:
  - mono
  - micro

architecture:
  layers:
    - interfaces
    - application
    - domain
    - infra
  dependency_direction: interfaces -> application -> domain <- infra
  cross_domain_rule: 仅依赖 facade 契约

main_dirs:
  - bacon-app
  - bacon-biz
  - bacon-common
  - docs
  - deploy
```

---

## 仓库结构

```text
bacon
├── bacon-app/      # 启动与装配层
├── bacon-biz/      # 业务域模块
├── bacon-common/   # 公共能力模块
├── docs/           # 架构、需求、数据库设计文档
└── deploy/         # 部署脚本与配置样例
```

---

## 快速开始

### 1. 先读文档

1. [docs/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)
2. [docs/README.md](/Volumes/storage/workspace/bacon/docs/README.md)
3. 对应业务域 `*-REQUIREMENTS.md`

### 2. 构建与测试

```bash
mvn clean verify
mvn test
mvn checkstyle:check
```

### 3. 运行

单体模式：

```bash
mvn -pl bacon-app/bacon-mono-boot spring-boot:run
```

微服务模式（示例）：

```bash
mvn -pl bacon-app/bacon-order-starter spring-boot:run
```

---

## 关键工程约束

- 分层依赖方向固定：`interfaces -> application -> domain <- infra`
- 跨域调用固定通过 `Facade` 契约，禁止越层直连他域实现
- Maven 编译/测试统一使用 `JDK 17`
- 代码、数据库与文档需保持同步演进

---

## 文档索引

- [docs/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/ARCHITECTURE.md)
- [docs/README.md](/Volumes/storage/workspace/bacon/docs/README.md)
- [docs/AUTH-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/AUTH-REQUIREMENTS.md)
- [docs/UPMS-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/UPMS-REQUIREMENTS.md)
- [docs/ORDER-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/ORDER-REQUIREMENTS.md)
- [docs/INVENTORY-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/INVENTORY-REQUIREMENTS.md)
- [docs/PAYMENT-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/PAYMENT-REQUIREMENTS.md)

## License

Apache License 2.0，详见 [LICENSE](/Volumes/storage/workspace/bacon/LICENSE)。
