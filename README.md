# Bacon

面向企业级场景的后端业务平台。

> 仓库中的全部文档与代码均由 AI 生成，并在统一约束下持续迭代，覆盖业务建模、架构设计、数据库设计与交付实现。

## Highlights

- `Runtime:` 支持 `mono` 与 `micro` 双运行模式，业务模块只维护一份核心实现
- `Architecture:` 采用 `api / interfaces / application / domain / infra` 五层结构
- `Docs:` 需求、数据库、架构、上线准备与代码保持同步收敛
- `Harness:` 面向 `harness` 执行方式设计最小上下文加载协议
- `Delivery:` 强调幂等、补偿、重试、数据库设计与发布准备

## 项目概览

技术栈：

- `Java 17`
- `Spring Boot 3.5`
- `Spring Cloud 2025`
- `Spring Cloud Alibaba 2025`
- `MyBatis-Plus`
- `Redis / JetCache`
- `RocketMQ`

核心业务域：

- `Auth`：认证、会话、OAuth2
- `UPMS`：用户、租户、组织、角色、菜单、资源、数据权限
- `Order`：订单创建、状态流转、取消、超时关闭
- `Inventory`：库存查询、预占、释放、扣减、审计
- `Payment`：支付单、回调、关单、状态查询
- `Storage`：统一存储对象、上传、引用、访问地址

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

更多细节见 [docs/00-governance/ARCHITECTURE.md](docs/00-governance/ARCHITECTURE.md)。

## Engineering Style

- 文档先行，约束显式，结构优先
- 智能协作执行链路不依赖全仓扫描，而依赖最小上下文加载
- 规则、实现、脚本与运行准备保持同源收敛
- 目标不是展示框架堆叠，而是展示可持续维护的工程组织能力

## Docs Layout

`docs/` 目录主要服务 harness / AI 执行链路，不作为人类主阅读入口。

- `docs/AGENT.md`：AI 文档加载协议
- `docs/00-governance`：架构与工程级规则
- `docs/10-requirements`：业务需求
- `docs/20-database`：数据库设计
- `docs/30-designs`：专项方案与路线图
- `docs/40-readiness`：上线准备与运行手册

## Quick Start

### Build

在仓库根目录执行：

```bash
mvn clean verify
mvn test
mvn checkstyle:check
mvn spotless:check
```

按模块构建示例：

```bash
mvn -pl bacon-app/bacon-order-starter -am -DskipTests install
```

### Run

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

## 质量工具

- `spotless`：负责 formatter、import 排序与版式整理
- `checkstyle`：负责规约检查与阻断，不负责改代码
- `ArchUnit`：负责分层、命名与关键架构约束

## Development Rules

- 基础包名：`com.github.thundax.bacon`
- Java、XML、YAML 统一使用 4 空格缩进
- HTTP 入参放在 `interfaces.dto`，统一使用 `*Request`
- HTTP 出参放在 `interfaces.response`，统一使用 `*Response`
- 跨域 DTO 放在 `api.dto`，统一使用 `*DTO`
- 领域对象、业务单号、数据库主键必须分开建模
- 文档、代码、数据库结构必须同步演进

## Key Docs

- [docs/AGENT.md](docs/AGENT.md)
- [docs/00-governance/ARCHITECTURE.md](docs/00-governance/ARCHITECTURE.md)
- [docs/10-requirements/AUTH-REQUIREMENTS.md](docs/10-requirements/AUTH-REQUIREMENTS.md)
- [docs/10-requirements/UPMS-REQUIREMENTS.md](docs/10-requirements/UPMS-REQUIREMENTS.md)
- [docs/10-requirements/ORDER-REQUIREMENTS.md](docs/10-requirements/ORDER-REQUIREMENTS.md)
- [docs/10-requirements/INVENTORY-REQUIREMENTS.md](docs/10-requirements/INVENTORY-REQUIREMENTS.md)
- [docs/10-requirements/PAYMENT-REQUIREMENTS.md](docs/10-requirements/PAYMENT-REQUIREMENTS.md)
- [docs/10-requirements/STORAGE-REQUIREMENTS.md](docs/10-requirements/STORAGE-REQUIREMENTS.md)

## License

Apache License 2.0，详见 [LICENSE](LICENSE)。
