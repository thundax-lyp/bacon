# Bacon

面向 AI Agent 的工程操作系统。

Bacon 不是一个普通的后端示例项目。它尝试回答一个更具体的问题：

> 当 AI 不只是生成片段，而是长期参与工程交付时，一个仓库应该怎样组织，才能让它读得准、写得对、改得稳？

这个项目把文档、目录、模块、测试、TODO 和 Git 历史组织成一套协作协议。人类负责判断，AI 负责执行，工程结构负责让执行不散。

## 核心想法

- 文档决定 AI 先读什么，也决定什么不该默认读取。
- 目录决定代码应该写在哪里。
- 模块边界决定代码可以依赖谁。
- 测试和质量规则负责拦住一部分误改。
- TODO 只保存下一步任务，完成历史交给 Git。

Bacon 的重点不在“堆技术栈”，而在让 AI Agent 可以在复杂工程里持续、稳定、可审计地工作。

## Highlights

- `AI-first docs`：`docs/AGENT.md` 负责上下文路由，区分治理文档、业务需求、数据库设计、专项方案和人类阅读材料。
- `Stable architecture`：业务域统一采用 `api / interfaces / application / domain / infra` 五层结构，跨域调用固定通过 `api.facade`。
- `Mono + micro`：业务代码只写一份，`bacon-app` 同时支持单体聚合启动和按域微服务装配。
- `Rich domains`：覆盖 `Auth / UPMS / Product / Order / Inventory / Payment / Storage` 等业务域。
- `Guardrails`：通过 Checkstyle、Spotless、ArchUnit、测试和 Git 约定，把一部分 AI 误改拦在工程系统内。
- `Delivery loop`：`TODO.md` 保存待执行任务，Git 保存完成历史，文档只保留稳定规则和当前上下文。

## Examples

`Product` 是一个完整业务域落地样板。

它从需求与数据设计开始，按提交历史逐步落到工程骨架、领域模型、应用编排、跨域契约、接口适配、基础设施、启动装配和架构规则测试。当前 `bacon-product` 包含 `api / interfaces / application / domain / infra` 五个模块，覆盖商品主数据、SKU、分类、图片引用、商品快照、搜索投影、Outbox 和命令幂等。

这个样板展示了 Bacon 想表达的工作方式：不是一次性生成一堆代码，而是让 AI Agent 沿着文档、任务、分层、测试和 Git 历史一步步完成可审计的业务交付。

`Sandwich` 是另一个完整样板，关注旧 Java 三层架构项目的 Agent-first 方法论迁移。它不是把三层架构改造成 DDD，也不只是代码迁移，而是在保留原有三层 API 形态的前提下，把文档路由、治理规则、任务生命周期、质量门禁、前后端协作规则和提交闭环迁入既有工程。

这两个样板分别对应两类真实场景：`Product` 展示 DDD 新业务域如何落地，`Sandwich` 展示三层旧系统如何迁移到 Agent-first 工作方式，并在不推翻原架构的前提下获得 AI Agent 可持续协作能力。

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
