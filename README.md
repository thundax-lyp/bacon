# Bacon

面向企业级场景的后端业务平台，支持 `mono` 与 `micro` 双运行模式。

一个以 `AI-native engineering workflow` 为核心方法构建的后端工程样本：需求、设计、实现与文档在同一条智能协作链路中持续生成、校准与收敛。

## 项目概览

`Bacon` 是一个基于 `Java 17`、`Spring Boot 3.5`、`Spring Cloud 2025`、`Spring Cloud Alibaba 2025` 的 Maven 多模块工程。

当前核心业务域：

- `Auth`：认证、会话、OAuth2
- `UPMS`：用户、租户、组织、角色、菜单、资源、数据权限
- `Order`：订单创建、状态流转、取消、超时关闭
- `Inventory`：库存查询、预占、释放、扣减、审计
- `Payment`：支付单、回调、关单、状态查询
- `Storage`：统一存储对象、上传、引用、访问地址

这个仓库关注的不是单点功能堆砌，而是完整的工程表达：

- 清晰的分层与模块边界
- 可以同时支撑 `mono` 与 `micro` 的装配方式
- 需求、数据库、架构、上线准备的文档化治理
- 面向 AI / harness 的最小上下文加载协议

## 仓库结构

```text
bacon
├── bacon-app/                  # 启动与装配层
├── bacon-biz/                  # 业务域模块
├── bacon-common/               # 公共能力模块
├── deploy/                     # 部署样例与脚本
└── docs/
    ├── AGENT.md                # harness / AI 文档加载入口
    ├── 00-governance/          # 架构与全局规则
    ├── 10-requirements/        # 业务需求
    ├── 20-database/            # 数据库设计
    ├── 30-designs/             # 专项设计与路线图
    └── 40-readiness/           # 上线准备与运行手册
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

更多细节见 [ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/00-governance/ARCHITECTURE.md)。

## Engineering Style

这个仓库采用“文档先行、约束显式、结构优先”的工程风格：

- 需求文档、数据库设计、架构约束与代码保持同步演进
- AI 执行链路不依赖全仓扫描，而依赖最小上下文加载
- 规则、实现、脚本和运行准备保持同源收敛
- 目标不是展示框架堆叠，而是展示可持续维护的工程组织能力

## 规格说明

`docs/` 目录主要服务 harness / AI 执行链路，不作为人类主阅读入口。

其中：

- `docs/AGENT.md`：AI 文档加载协议
- `docs/00-governance`：架构与工程级规则
- `docs/10-requirements`：业务需求
- `docs/20-database`：数据库设计
- `docs/30-designs`：专项方案与路线图
- `docs/40-readiness`：上线准备与运行手册

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

- [docs/00-governance/ARCHITECTURE.md](/Volumes/storage/workspace/bacon/docs/00-governance/ARCHITECTURE.md)
- [docs/AGENT.md](/Volumes/storage/workspace/bacon/docs/AGENT.md)
- [docs/10-requirements/AUTH-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/10-requirements/AUTH-REQUIREMENTS.md)
- [docs/10-requirements/UPMS-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/10-requirements/UPMS-REQUIREMENTS.md)
- [docs/10-requirements/ORDER-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/10-requirements/ORDER-REQUIREMENTS.md)
- [docs/10-requirements/INVENTORY-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/10-requirements/INVENTORY-REQUIREMENTS.md)
- [docs/10-requirements/PAYMENT-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/10-requirements/PAYMENT-REQUIREMENTS.md)
- [docs/10-requirements/STORAGE-REQUIREMENTS.md](/Volumes/storage/workspace/bacon/docs/10-requirements/STORAGE-REQUIREMENTS.md)

## Project Positioning

`Bacon` 不是传统意义上的示例仓库，也不是单纯的脚手架演示。

它更接近一个可持续演进的 `AI-first` 工程基座：让业务建模、架构约束、数据库设计、交付规范与实现过程在同一套语义系统中保持一致。

## License

Apache License 2.0，详见 [LICENSE](/Volumes/storage/workspace/bacon/LICENSE)。
