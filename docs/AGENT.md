# Docs Agent

只给 AI / harness 读。目标：少读，快读，读对。

## Core Rules

- 只加载完成当前任务必需的文档。
- 先读治理文档，再读业务文档。
- `docs/` 不是默认全量输入目录。
- 工程级规则优先于业务域文档。
- `STORY.md` 永不默认加载。

## Mandatory Entry

实现、修改、评审代码前固定先读：

1. [`00-governance/ARCHITECTURE.md`](./00-governance/ARCHITECTURE.md)

## Task Router

- 纯实现、修 bug、重构业务逻辑：
  读 `ARCHITECTURE.md`，再读对应 `10-requirements/*-REQUIREMENTS.md`
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

## Directory Map

- `00-governance/`: 全局规则
- `10-requirements/`: 业务需求
- `20-database/`: 数据库设计
- `30-designs/`: 专项设计
- `40-readiness/`: 上线与运维
