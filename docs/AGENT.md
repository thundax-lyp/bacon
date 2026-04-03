# Docs Agent

此文件只给 AI / harness 读，用于定义 `docs/` 的最小加载路径。

规则：

- 只读完成当前任务所需的最少文档
- 先读工程级规则，再读业务域文档
- 不把 `docs/` 当成必须全量遍历的目录
- 工程级规则优先级高于业务域文档

## 1. Start Here

实现、修改、评审代码前，固定先读：

1. [`00-governance/ARCHITECTURE.md`](./00-governance/ARCHITECTURE.md)

按任务补充：

- 业务实现：读取对应 [`10-requirements/*-REQUIREMENTS.md`](./10-requirements)
- 新增类、重命名、目录调整、分层判断：再读 [`00-governance/NAMING-AND-PLACEMENT-RULES.md`](./00-governance/NAMING-AND-PLACEMENT-RULES.md)
- 数据库相关：再读 [`00-governance/DATABASE-RULES.md`](./00-governance/DATABASE-RULES.md) 和对应 [`20-database/*-DATABASE-DESIGN.md`](./20-database)
- 统一 ID 相关：再读 [`00-governance/UNIFIED-ID-DESIGN.md`](./00-governance/UNIFIED-ID-DESIGN.md)
- 文档编写相关：再读 [`00-governance/DOCUMENT-RULES.md`](./00-governance/DOCUMENT-RULES.md)
- 上线准备、运行手册、发布清单：读取 [`40-readiness/`](./40-readiness)
- 专项方案或规划：读取 [`30-designs/`](./30-designs)

## 2. Directory Map

- `00-governance/`: architecture, global rules, shared constraints
- `10-requirements/`: domain requirements
- `20-database/`: database designs
- `30-designs/`: cross-cutting designs and roadmaps
- `40-readiness/`: readiness, runbooks, release checklists

## 3. Domain Docs

- `Auth`: [`10-requirements/AUTH-REQUIREMENTS.md`](./10-requirements/AUTH-REQUIREMENTS.md)
- `UPMS`: [`10-requirements/UPMS-REQUIREMENTS.md`](./10-requirements/UPMS-REQUIREMENTS.md)
- `Order`: [`10-requirements/ORDER-REQUIREMENTS.md`](./10-requirements/ORDER-REQUIREMENTS.md)
- `Inventory`: [`10-requirements/INVENTORY-REQUIREMENTS.md`](./10-requirements/INVENTORY-REQUIREMENTS.md)
- `Payment`: [`10-requirements/PAYMENT-REQUIREMENTS.md`](./10-requirements/PAYMENT-REQUIREMENTS.md)
- `Storage`: [`10-requirements/STORAGE-REQUIREMENTS.md`](./10-requirements/STORAGE-REQUIREMENTS.md)

## 4. Database Docs

- `Auth`: [`20-database/AUTH-DATABASE-DESIGN.md`](./20-database/AUTH-DATABASE-DESIGN.md)
- `UPMS`: [`20-database/UPMS-DATABASE-DESIGN.md`](./20-database/UPMS-DATABASE-DESIGN.md)
- `Order`: [`20-database/ORDER-DATABASE-DESIGN.md`](./20-database/ORDER-DATABASE-DESIGN.md)
- `Inventory`: [`20-database/INVENTORY-DATABASE-DESIGN.md`](./20-database/INVENTORY-DATABASE-DESIGN.md)
- `Payment`: [`20-database/PAYMENT-DATABASE-DESIGN.md`](./20-database/PAYMENT-DATABASE-DESIGN.md)
- `Storage`: [`20-database/STORAGE-DATABASE-DESIGN.md`](./20-database/STORAGE-DATABASE-DESIGN.md)

## 5. Load Policy

- Single-domain task: do not load other domain requirements by default
- Database-only task: do not load all requirements by default
- Cross-domain workflow: load only the involved domains
- Class creation, renaming, placement, or layering tasks: load naming and placement rules on demand
- If a design or readiness doc is referenced by the active requirement, load it on demand
