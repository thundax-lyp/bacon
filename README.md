# Bacon

面向 AI Agent 的工程操作系统。

Bacon 是一个 Java 17 / Spring Boot / Maven 多模块示例项目，覆盖 `Auth / UPMS / Product / Order / Inventory / Payment / Storage` 七个业务域，重点展示如何通过文档路由、分层架构、测试门禁和 Git 工作流，让 AI Agent 稳定参与复杂后端工程交付。

Bacon 不是一个普通的后端示例项目。它尝试回答一个更具体的问题：

> 当 AI 不只是生成片段，而是长期参与工程交付时，一个仓库应该怎样组织，才能让它读得准、写得对、改得稳？

这个项目把文档、目录、模块、测试、TODO 和 Git 历史组织成一套协作协议。人类负责判断，AI 负责执行，工程结构负责让执行不散。

它不是一个 AI 编程工具，而是一套让 AI Agent 能在 Java 工程中按规则读文档、改代码、跑测试、维护 TODO，并生成可审计提交的仓库级协作系统。整个项目按“人类判断边界，AI 生成代码、文档、测试和提交”的方式演进。

## 核心想法

- 文档决定 AI 先读什么，也决定什么不该默认读取。
- 目录决定代码应该写在哪里。
- 模块边界决定代码可以依赖谁。
- 测试和质量规则负责拦住一部分误改。
- TODO 只保存下一步任务，完成历史交给 Git。

Bacon 的重点不在“堆技术栈”，而在让 AI Agent 可以在复杂工程里持续、稳定、可审计地工作。

## 功能覆盖

- 七个业务域五层落地：`Auth / UPMS / Product / Order / Inventory / Payment / Storage` 都按 `api / interfaces / application / domain / infra` 拆分模块。
- 用户与认证：覆盖租户、用户、身份、凭证、角色、菜单、资源、数据权限、登录会话、OAuth2 授权码、访问令牌、刷新令牌、授权同意和审计。
- 电商交易链路：覆盖商品主数据、SKU、分类、图片引用、商品快照、订单创建、库存预占与释放、支付创建与回调、订单状态推进。
- 可靠性机制：覆盖 Outbox、命令幂等、库存审计补偿、死信、重放任务、租约抢占、CAS 条件提交和失败原因收口。
- 存储能力：覆盖对象上传、分片上传、对象引用、查询、删除、审计日志和对象存储适配边界。
- 运行形态：`bacon-mono-boot` 聚合启动七个业务域，同时保留按域 starter 和 gateway 的微服务装配入口。
- 工程治理：通过 Maven、Checkstyle、Spotless、ArchUnit 和测试把分层依赖、跨域契约、接口签名、异常出口等规则固化为门禁。
- AI 交付闭环：长期任务先拆成一组可验收 TODO；每个小任务完成时，AI 在规则约束下把代码、测试、文档和 TODO 清理收敛到同一条提交；完成历史保留在 Git 中。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 | Java 17 |
| 框架 | Spring Boot 3.5.x |
| 构建 | Maven multi-module |
| 架构 | DDD 分层、模块化单体、微服务装配 |
| 质量 | JUnit、ArchUnit、Checkstyle、Spotless |
| 数据与部署 | MySQL / Redis 集成边界、数据库脚本、部署样例 |
| AI 协作 | 文档路由、任务生命周期、架构治理、提交审计 |

## 项目规模

- 业务域：7 个
- 应用入口：`bacon-mono-boot`、`bacon-gateway`、7 个按域 starter
- 代码规模：约 1.4k 个 Java 文件，其中测试约 270 个
- 文档规模：40+ 份 Markdown，覆盖治理规则、业务需求、数据库设计、专项设计和人类复盘材料
- 演进历史：约 1.3k 条提交，提交历史承担任务拆解、自动生成、修复、回滚和复盘记录

## Highlights

- `AI-first docs`：`docs/AGENT.md` 负责上下文路由，区分治理文档、业务需求、数据库设计、专项方案和人类阅读材料。
- `Stable architecture`：业务域统一采用 `api / interfaces / application / domain / infra` 五层结构，跨域调用固定通过 `api.facade`。
- `Mono + micro`：业务代码只写一份，`bacon-app` 同时支持单体聚合启动和按域微服务装配。
- `Complete sample`：`Product` 以连续提交演示一次标准交付闭环，覆盖需求、数据设计、五层模块、接口、基础设施、测试和启动装配。
- `Migration sample`：[Sandwich](https://github.com/thundax-lyp/sandwich) 验证同一套 Agent-first 方法论可以迁移到旧三层 Java 项目，而不是绑定 DDD 架构。
- `Guardrails`：通过 Checkstyle、Spotless、ArchUnit、测试和 Git 约定，把一部分 AI 误改拦在工程系统内。
- `Delivery loop`：任务面板只保留未关闭事项，完成历史沉淀到 Git。

## Why It Matters

大多数 AI 编程停留在提示词和单次生成。Bacon 关注的是长期工程协作：如何让 AI 在复杂仓库中持续读取正确上下文、遵守架构边界、留下可审计历史，并通过测试和规则门禁降低误改风险。

## Examples

七个业务域共同构成一条典型后台业务链路：`UPMS / Auth` 负责用户、权限与认证，`Product / Order / Inventory / Payment` 负责商品和交易主链路，`Storage` 负责对象上传、引用和审计。

这些业务域都有需求文档、数据库设计、五层模块、跨域契约、测试和启动装配，并通过统一架构规则限制依赖方向。

`Product` 是最新的一次完整业务域落地样板。

它从需求与数据设计开始，按提交历史逐步落到工程骨架、领域模型、应用编排、跨域契约、接口适配、基础设施、启动装配和架构规则测试。当前 `bacon-product` 包含 `api / interfaces / application / domain / infra` 五个模块，覆盖商品主数据、SKU、分类、图片引用、商品快照、搜索投影、Outbox 和命令幂等。

这个样板展示了 Bacon 想表达的工作方式：不是一次性生成一堆代码，而是让 AI Agent 沿着文档、任务、分层、测试和 Git 历史一步步完成可审计的业务交付。

[Sandwich](https://github.com/thundax-lyp/sandwich) 是另一个重要样板，关注旧 Java 三层架构项目的 Agent-first 方法论迁移。它不是把三层架构改造成 DDD，也不只是代码迁移，而是在保留原有三层 API 形态的前提下，把文档路由、治理规则、任务生命周期、质量门禁、前后端协作规则和提交闭环迁入既有工程。

这两个样板分别对应两类真实场景：`Product` 展示 DDD 新业务域如何落地，`Sandwich` 展示既有系统如何在不推翻原架构的前提下获得 AI Agent 可持续协作能力。

`TODO.md` 当前可能是空的，这是设计结果，不是缺少任务。它只表示“当前没有未关闭任务”。历史上的 TODO 拆分、执行和清理需要看 Git，例如：

```bash
git log --oneline -- TODO.md
git log --oneline --grep='TODO\|todo\|任务'
```

项目演进复盘见 [docs/60-human/STORY.md](docs/60-human/STORY.md)。这份材料基于提交历史整理了从工程初始化、多域生成、可靠性治理、架构门禁到 Product 标准闭环的演进过程。

## 阅读路径

这个项目适合按“业务覆盖、架构约束、复杂度处理、AI 协作方式”四条线阅读：

- 业务覆盖：看 `bacon-biz/` 下七个业务域，重点关注 `Product` 的完整五层落地，以及 `Order / Inventory / Payment` 的跨域交易链路。
- 架构约束：看 [docs/00-governance/ARCHITECTURE.md](docs/00-governance/ARCHITECTURE.md)，重点关注分层职责、跨域只依赖 `api.facade`、单体与微服务共用业务代码。
- 工程质量：看 ArchUnit、Checkstyle、Spotless 和测试，重点关注架构规则如何从文档变成自动门禁。
- 复杂度处理：看 Outbox、幂等、死信、重放、租约抢占、CAS 条件提交、租户上下文和统一异常收口。
- AI 协作：看 [docs/AGENT.md](docs/AGENT.md)、`git log --oneline -- TODO.md` 和 [docs/60-human/STORY.md](docs/60-human/STORY.md)，重点关注长期任务如何拆分成小任务，并由 AI 在规则约束下完成代码、测试、文档和提交闭环。

## 接手方式

这个仓库的正确打开方式不是从根目录 `README.md` 推导实现规则，而是先理解它的上下文路由：哪些文档是任务必须读取的规则，哪些只是人类叙事或历史材料。

一个有效的起始对话是：

> 分析一下这个项目的文档治理特点，总结有哪些经验可以在其他工程里复用。

实际接手时，先读 [docs/AGENT.md](docs/AGENT.md) 判断文档路径；涉及实现时再读 [docs/00-governance/ARCHITECTURE.md](docs/00-governance/ARCHITECTURE.md) 和对应业务需求；涉及历史任务时看 `TODO.md` 与 `git log --oneline -- TODO.md`。

## 项目结构

```text
bacon
├── bacon-app/       # 启动与装配
├── bacon-biz/       # 业务域
├── bacon-common/    # 共享能力
├── db/              # 数据库脚本
├── deploy/          # 部署样例
└── docs/            # AI Agent 的上下文系统
```

每个业务域保持统一分层：

```text
api -> interfaces -> application -> domain -> infra
```

这套结构让单体和微服务可以共用一份业务代码，运行方式由 `bacon-app` 装配。

## 文档入口

- AI / Agent 入口：[docs/AGENT.md](docs/AGENT.md)
- 核心思想：[docs/60-human/AI-AGENT-ENGINEERING-OS-EXPLANATION.md](docs/60-human/AI-AGENT-ENGINEERING-OS-EXPLANATION.md)
- 架构红线：[docs/00-governance/ARCHITECTURE.md](docs/00-governance/ARCHITECTURE.md)
- 文档规则：[docs/00-governance/DOCUMENT-RULES.md](docs/00-governance/DOCUMENT-RULES.md)

实现规则以 `docs/` 下的治理文档和业务文档为准，根目录 README 只保留项目总览。

## 本地运行

构建：

```bash
mvn clean verify
```

单体启动：

```bash
./scripts/run-mono.sh
```

更多运行和部署方式见 [deploy/README.md](deploy/README.md)。

## License

Apache License 2.0，详见 [LICENSE](LICENSE)。
