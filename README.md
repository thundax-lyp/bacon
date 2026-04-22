# Bacon

面向企业级场景的后端业务平台。

> 通过 DDD 分层与强语义命名构建稳定的代码语法结构，以提升 AI 生成代码的一致性与可预测性。

> 仓库中的全部文档与代码均由 AI 生成，并在统一约束下持续迭代，覆盖业务建模、架构设计、数据库设计与交付实现。

> 本 README 仅提供项目总览与导航；实现细节和约束规则以 `docs/` 下治理与需求文档为准。

## Highlights

- `Runtime:` 支持 `mono` 与 `micro` 双运行模式，业务模块只维护一份核心实现
- `Architecture:` 采用 `api / interfaces / application / domain / infra` 五层结构
- `Domains:` 覆盖 `Auth / UPMS / Order / Inventory / Payment / Storage` 六大业务域
- `Docs:` 需求、数据库、架构、上线准备、提示词模板与代码保持同步收敛
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

- `Auth`：账号登录、会话管理、OAuth2 授权码与令牌链路、认证审计
- `UPMS`：租户、用户、身份、凭据、部门、岗位、角色、菜单、资源与数据权限
- `Order`：下单编排、状态流转、支付/库存快照、Outbox/DeadLetter、幂等记录
- `Inventory`：库存主数据、预占/释放/扣减、审计日志、Outbox 补偿与死信回放
- `Payment`：支付单创建、渠道回调处理、关单与审计链路
- `Storage`：对象上传、引用关系、分片上传会话、审计与补偿出站

## 仓库结构

```text
bacon
├── bacon-app/                  # 启动与装配层
├── bacon-biz/                  # 业务域模块
├── bacon-common/               # 公共能力模块
├── db/                         # 数据库 schema 与种子数据
├── deploy/                     # 部署样例与脚本
└── docs/
    ├── AGENT.md                # harness / AI 文档加载入口
    ├── 00-governance/          # 架构与全局规则
    ├── 10-requirements/        # 业务需求
    ├── 20-database/            # 数据库设计
    ├── 30-designs/             # 专项设计与路线图
    ├── 40-readiness/           # 上线准备与运行手册
    └── 50-prompts/             # 固定格式文档提示词模板
```

关键目录：

- `bacon-app`
  - `bacon-mono-boot`：单体启动模块
  - `bacon-*-starter`：各业务域微服务启动模块
  - `bacon-gateway`：网关模块
  - `bacon-register`：注册中心模块
- `bacon-biz`
  - 每个业务域保持 `api / interfaces / application / domain / infra` 五层结构
- `bacon-common`
  - 公共基础设施能力，如 `mybatis`、`cache`、`mq`、`security`、`swagger`
- `db`
  - `schema/*.sql`：各域建表脚本
  - `data/*.sql`：本地验证种子数据

## 架构约定

- 分层调用链固定为：`HTTP -> interfaces -> application -> domain -> repository -> MySQL/Redis`
- 跨域调用固定通过 `api.facade`
- 本地适配实现固定放在 `interfaces.facade`
- 远程适配实现固定放在 `infra.facade.remote`
- `{module}-api` 仅承载本域跨域契约，不依赖其他业务域实现分层
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
- `docs/50-prompts`：固定格式文档提示词模板（按需加载，非默认实现输入）

## Quick Start

### Build

在仓库根目录执行：

```bash
mvn clean verify
mvn test
mvn checkstyle:check
mvn spotless:check
mvn -Pquality-gates verify
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

### Docker Deployment

`deploy/` 提供两套 `docker compose` 样例：

- `mono`：`deploy/bacon-mono-boot/docker-compose.yml`
- `micro`：`deploy/bacon-micro/docker-compose.yml`

`mono` 快速启动：

```bash
mvn -q -pl bacon-app/bacon-mono-boot -am -DskipTests package
cp deploy/bacon-mono-boot/.env.example deploy/bacon-mono-boot/.env
docker compose --env-file deploy/bacon-mono-boot/.env -f deploy/bacon-mono-boot/docker-compose.yml up -d
cat db/schema/*.sql | docker compose --env-file deploy/bacon-mono-boot/.env -f deploy/bacon-mono-boot/docker-compose.yml exec -T mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> bacon_sample
cat db/data/*.sql | docker compose --env-file deploy/bacon-mono-boot/.env -f deploy/bacon-mono-boot/docker-compose.yml exec -T mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> bacon_sample
```

`micro` 快速启动：

```bash
mvn -q -pl bacon-app/bacon-gateway,bacon-app/bacon-auth-starter,bacon-app/bacon-upms-starter,bacon-app/bacon-order-starter,bacon-app/bacon-inventory-starter,bacon-app/bacon-payment-starter,bacon-app/bacon-storage-starter -am -DskipTests package
cp deploy/bacon-micro/.env.example deploy/bacon-micro/.env
docker compose --env-file deploy/bacon-micro/.env -f deploy/bacon-micro/docker-compose.yml up -d
cat db/schema/*.sql | docker compose --env-file deploy/bacon-micro/.env -f deploy/bacon-micro/docker-compose.yml exec -T mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> bacon_sample
cat db/data/*.sql | docker compose --env-file deploy/bacon-micro/.env -f deploy/bacon-micro/docker-compose.yml exec -T mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> bacon_sample
```

停止示例：

```bash
docker compose --env-file deploy/bacon-mono-boot/.env -f deploy/bacon-mono-boot/docker-compose.yml down
docker compose --env-file deploy/bacon-micro/.env -f deploy/bacon-micro/docker-compose.yml down
```

部署细节见：

- [deploy/README.md](deploy/README.md)
- [deploy/bacon-mono-boot/README.md](deploy/bacon-mono-boot/README.md)
- [deploy/bacon-micro/README.md](deploy/bacon-micro/README.md)

## 质量工具

- `spotless`：负责 formatter、import 排序与版式整理
- `checkstyle`：负责规约检查与阻断，不负责改代码
- `ArchUnit`：负责分层、命名与关键架构约束
- `jacoco`：覆盖率统计与门禁（通过 `quality-gates` profile 启用）
- `spotbugs`：静态缺陷检查（通过 `quality-gates` profile 启用）

推荐执行顺序：

```bash
mvn spotless:check
mvn checkstyle:check
mvn test
mvn -Pquality-gates verify
```

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
