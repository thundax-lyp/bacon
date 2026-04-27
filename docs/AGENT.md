# Docs Agent

只给 AI / harness 读。目标：少读，快读，读对。

## Core Rules

- 只加载完成当前任务必需的文档。
- 先读治理文档，再读业务文档。
- `docs/` 不是默认全量输入目录。
- 工程级规则优先于业务域文档。
- `50-prompts/` 与 `60-human/` 永不默认加载。

## Mandatory Entry

实现、修改、评审代码前固定先读：

1. [`00-governance/ARCHITECTURE.md`](./00-governance/ARCHITECTURE.md)

## Task Router

- 纯实现、修 bug、重构业务逻辑：
  读 `ARCHITECTURE.md`，再读对应 `10-requirements/*-REQUIREMENTS.md`
- 需要解释架构意图、规则冲突、分层取舍或 AI 误改风险：
  读 `00-governance/ARCHITECTURE-INTENT.md`
- 新增类、改类名、改目录、判断分层：
  再读 `00-governance/NAMING-AND-PLACEMENT-RULES.md`
- 数据库、DO、Mapper、持久化查询：
  再读 `00-governance/DATABASE-RULES.md`
  再读对应 `20-database/*-DATABASE-DESIGN.md`
- 统一 ID、发号、ID 落库：
  再读 `00-governance/UNIFIED-ID-DESIGN.md`
- 改文档：
  再读 `00-governance/DOCUMENT-RULES.md`
- 上线准备、运维、发布：
  先读 `00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`
  读 `40-readiness/`
- 专项方案、路线图、跨域设计：
  按需读 `30-designs/`

## Domain Router

- `Auth` -> [`10-requirements/AUTH-REQUIREMENTS.md`](./10-requirements/AUTH-REQUIREMENTS.md)
- `UPMS` -> [`10-requirements/UPMS-REQUIREMENTS.md`](./10-requirements/UPMS-REQUIREMENTS.md)
- `Order` -> [`10-requirements/ORDER-REQUIREMENTS.md`](./10-requirements/ORDER-REQUIREMENTS.md)
- `Inventory` -> [`10-requirements/INVENTORY-REQUIREMENTS.md`](./10-requirements/INVENTORY-REQUIREMENTS.md)
- `Payment` -> [`10-requirements/PAYMENT-REQUIREMENTS.md`](./10-requirements/PAYMENT-REQUIREMENTS.md)
- `Storage` -> [`10-requirements/STORAGE-REQUIREMENTS.md`](./10-requirements/STORAGE-REQUIREMENTS.md)

## Load Limits

- 单域任务：不要默认加载其他域文档。
- 数据库任务：不要顺手加载全部需求文档。
- 跨域任务：只加载涉及的域。
- commit 整理、纯格式调整、无实现判断的机械修改：
  不额外加载业务需求文档。
- 只有当当前文档明确引用下一个文档时，才继续向下追。

## TODO Lifecycle

- 根目录 `TODO.md` 是任务执行队列，不是完成历史。
- 宏观任务进入 `TODO.md` 后，按 [`00-governance/how-to/HOW-TO-RUN-TODO-COLLABORATION.md`](./00-governance/how-to/HOW-TO-RUN-TODO-COLLABORATION.md) 完成人机讨论、任务拆解、人工审阅和执行关闭。
- 已完成任务不得在 `TODO.md` 中打勾长期保留，必须直接删除。
- 删除已完成 TODO 项必须和完成该任务的代码、文档或测试修改放在同一个 commit。
- 任务只完成一部分时，不得删除整项；必须拆分或收窄为剩余未完成内容。
- 待讨论项完成决策后，必须删除待讨论项；若仍需执行，新增明确执行项。
- 完成历史以 GitHub commit / PR 保留，不在 `TODO.md` 中重复记录。

## Directory Map

- `00-governance/`: 全局规则
- `10-requirements/`: 业务需求
- `20-database/`: 数据库设计
- `30-designs/`: 专项设计
- `40-readiness/`: 上线与运维
- `50-prompts/`: 人工触发的生成提示词
- `60-human/`: 人类阅读材料与项目叙事
