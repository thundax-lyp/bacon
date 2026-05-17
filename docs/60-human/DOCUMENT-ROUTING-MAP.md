# DOCUMENT ROUTING MAP

## 1. Purpose

本文档用图表达 Bacon 文档加载路径，帮助人类检查和优化文档路由。

本文档是 human 文档，不是 AI 默认加载入口。AI 执行任务时仍以 [`../../AGENTS.md`](../../AGENTS.md)、[`../AGENT.md`](../AGENT.md) 和具体文档内的直接链接为准。

本文档只用于回答这些问题：

- 某类任务从入口会读到哪些文档。
- 某条文档依赖链是否清晰。
- 某个文档是否缺少上游加载路径。
- `docs/AGENT.md` 是否存在漏路由、重复路由或过度路由。

## 2. Scope

当前覆盖关键文档加载路径：

- 仓库入口。
- `docs/AGENT.md` 任务路由。
- 治理文档入口。
- 需求文档、数据库设计文档和专项设计文档的典型路径。
- API、异常、上下文和架构参考路径。
- TODO / RUNBOOK 协作路径。

当前不覆盖范围：

- 不作为全量文档索引。
- 不替代 `docs/AGENT.md`。
- 不要求 AI 在普通任务中读取本文档。
- 不记录每个文件的完整出入边。

## 3. Reading Rule

真实加载规则固定在两处：

1. `docs/AGENT.md`：任务类型到文档的路由规则。
2. 具体文档内部链接：当前文档到直接依赖文档的下一级链接。

本文档中的连线只表达“人类理解上的加载条件”。当本文档和真实加载规则不一致时，必须修改真实加载规则或本文档，使二者重新一致。

## 4. Entry Map

```mermaid
%%{init: {"flowchart": {"defaultRenderer": "elk"}} }%%
flowchart LR
  Root["./AGENTS.md"] -->|"所有任务先读仓库规则"| DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"实现 / 修改 / 评审代码"| Architecture["00-governance/ARCHITECTURE.md"]
  DocsAgent -->|"TODO 协作 / 任务收口 / 提交收口"| TodoRules["00-governance/TODO-RULES.md"]
  DocsAgent -->|"改文档"| DocumentRules["00-governance/DOCUMENT-RULES.md"]
  DocsAgent -->|"新增类 / 改类名 / 判断分层"| NamingRules["00-governance/NAMING-AND-PLACEMENT-RULES.md"]
  DocsAgent -->|"数据库 / DO / Mapper / 持久化查询"| DatabaseRules["00-governance/DATABASE-RULES.md"]
  DocsAgent -->|"统一 ID / 发号 / ID 落库"| UnifiedId["00-governance/UNIFIED-ID-DESIGN.md"]
  DocsAgent -->|"上线准备 / 运维 / 发布"| DeployRules["00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md"]
```

## 5. Business Route Map

```mermaid
%%{init: {"flowchart": {"defaultRenderer": "elk"}} }%%
flowchart LR
  DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"Auth 业务实现 / 修复 / 重构"| AuthReq["10-requirements/AUTH-REQUIREMENTS.md"]
  DocsAgent -->|"Auth 数据库 / DO / Mapper / SQL"| AuthDb["20-database/AUTH-DATABASE-DESIGN.md"]
  DocsAgent -->|"Auth 登录专项方案按需读取"| AuthLogin["30-designs/AUTH-LOGIN-DESIGN.md"]

  DocsAgent -->|"UPMS 业务实现 / 修复 / 重构"| UpmsReq["10-requirements/UPMS-REQUIREMENTS.md"]
  DocsAgent -->|"UPMS 数据库 / DO / Mapper / SQL"| UpmsDb["20-database/UPMS-DATABASE-DESIGN.md"]

  DocsAgent -->|"Order 业务实现 / 修复 / 重构"| OrderReq["10-requirements/ORDER-REQUIREMENTS.md"]
  DocsAgent -->|"Order 数据库 / DO / Mapper / SQL"| OrderDb["20-database/ORDER-DATABASE-DESIGN.md"]
  DocsAgent -->|"订单库存支付专项链路按需读取"| OrderFlow["30-designs/ORDER-INVENTORY-PAYMENT-FLOW.md"]
  DocsAgent -->|"订单 Outbox 专项方案按需读取"| OrderOutbox["30-designs/ORDER-OUTBOX-FLOW.md"]

  DocsAgent -->|"Inventory 业务实现 / 修复 / 重构"| InventoryReq["10-requirements/INVENTORY-REQUIREMENTS.md"]
  DocsAgent -->|"Inventory 数据库 / DO / Mapper / SQL"| InventoryDb["20-database/INVENTORY-DATABASE-DESIGN.md"]
  DocsAgent -->|"库存 Outbox 专项方案按需读取"| InventoryOutbox["30-designs/INVENTORY-OUTBOX-FLOW.md"]

  DocsAgent -->|"Payment 业务实现 / 修复 / 重构"| PaymentReq["10-requirements/PAYMENT-REQUIREMENTS.md"]
  DocsAgent -->|"Payment 数据库 / DO / Mapper / SQL"| PaymentDb["20-database/PAYMENT-DATABASE-DESIGN.md"]

  DocsAgent -->|"Product 业务实现 / 修复 / 重构"| ProductReq["10-requirements/PRODUCT-REQUIREMENTS.md"]
  DocsAgent -->|"Product MySQL 数据库 / DO / Mapper / SQL"| ProductDb["20-database/PRODUCT-DATABASE-DESIGN.md"]
  DocsAgent -->|"Product Elasticsearch 结构 / 查询投影"| ProductEs["20-database/PRODUCT-ELASTICSEARCH-DESIGN.md"]

  DocsAgent -->|"Storage 业务实现 / 修复 / 重构"| StorageReq["10-requirements/STORAGE-REQUIREMENTS.md"]
  DocsAgent -->|"Storage 数据库 / DO / Mapper / SQL"| StorageDb["20-database/STORAGE-DATABASE-DESIGN.md"]
```

## 6. API And Error Route Map

```mermaid
%%{init: {"flowchart": {"defaultRenderer": "elk"}} }%%
flowchart LR
  DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"HTTP API 注解 / 静态资源 / 统一响应 / 响应包装"| ApiMatrix["00-governance/API-ANNOTATION-MATRIX.md"]
  DocsAgent -->|"异常分层 / 统一异常响应 / error code"| Architecture["00-governance/ARCHITECTURE.md"]
  Architecture -->|"异常约定与分层红线"| ApiMatrix

  ApiMatrix -->|"Provider 完整路径规则"| NamingRules["00-governance/NAMING-AND-PLACEMENT-RULES.md"]
```

## 7. Context And Architecture Reference Route Map

```mermaid
%%{init: {"flowchart": {"defaultRenderer": "elk"}} }%%
flowchart LR
  DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"登录态 / 当前用户 / 当前主体 / 线程上下文 / 异步身份透传"| ContextRules["00-governance/BACON-CONTEXT-PROPAGATION-RULES.md"]
  DocsAgent -->|"架构参考 / 目录树 / 模块装配示例"| Architecture["00-governance/ARCHITECTURE.md"]
  Architecture -->|"详细目录树与装配示例"| ArchRef["00-governance/ARCHITECTURE-REFERENCE.md"]
  DocsAgent -->|"规则冲突 / 分层取舍 / AI 误改风险"| ArchIntent["00-governance/ARCHITECTURE-INTENT.md"]
```

## 8. TODO And RUNBOOK Route Map

```mermaid
%%{init: {"flowchart": {"defaultRenderer": "elk"}} }%%
flowchart LR
  DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"TODO 协作 / 任务拆解 / 人机审阅 / 任务列表重写"| TodoRules["00-governance/TODO-RULES.md"]
  DocsAgent -->|"任务收口 / 测试检查 / 文档同步 / 小步提交"| TodoRules

  DocsAgent -->|"一次性复杂任务 / 跨模块迁移 / 删除 / 重构"| DocumentRules["00-governance/DOCUMENT-RULES.md"]
  DocumentRules -->|"RUNBOOK 位置 / 命名 / 生命周期"| Runbook["30-designs/RUNBOOK-*.md"]
  Runbook -->|"拆解后等待人工审阅"| Todo["../TODO.md"]
```

## 9. Maintenance Checklist

新增或调整稳定文档路由时，人类维护者检查以下问题：

- 这个文档是否需要进入 `docs/AGENT.md` 的任务路由。
- 这个文档是否只需要被上游文档直接链接。
- 这条路由的加载条件是否足够明确。
- 是否存在另一个文档已经表达同一规则。
- 是否会让 AI 在普通任务中多读无关文档。
- 本文档的图是否需要同步调整。
- 对应生成提示词 `docs/50-prompts/DOCUMENT-ROUTING-MAP-PROMPT.md` 是否需要同步调整。

## 10. Open Items

无
