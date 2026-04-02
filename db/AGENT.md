# DB Agent

此文件只给 AI / harness 读，用于定义 `db/` 目录下数据库脚本的读取与执行规则。

## 1. Start Here

处理数据库相关任务前，固定先读：

1. [`../docs/AGENT.md`](../docs/AGENT.md)
2. [`../docs/00-governance/DATABASE-RULES.md`](../docs/00-governance/DATABASE-RULES.md)
3. 对应业务域数据库设计文档，位于 [`../docs/20-database/`](../docs/20-database)

规则：

- 只读当前任务涉及的业务域数据库设计文档
- 先读规则与设计，再改 `schema/` 或 `data/`
- 不默认读取所有业务域数据库文档

## 2. Directory Map

```text
db/
├── AGENT.md
├── schema/
│   ├── auth.sql
│   ├── inventory.sql
│   ├── order.sql
│   ├── payment.sql
│   ├── storage.sql
│   └── upms.sql
└── data/
    ├── auth.sql
    ├── inventory.sql
    ├── payment.sql
    ├── storage.sql
    └── upms.sql
```

- `schema/`: DDL scripts only
- `data/`: seed and initialization scripts only
- 文件名固定使用业务域名小写
- 新增业务域时，优先保持 `schema/<domain>.sql` 与 `data/<domain>.sql` 成对出现
- 若某业务域暂不提供初始化数据，可只存在 `schema/<domain>.sql`

## 3. Domain Mapping

- `Auth`: `schema/auth.sql`, `data/auth.sql`
- `UPMS`: `schema/upms.sql`, `data/upms.sql`
- `Order`: `schema/order.sql`
- `Inventory`: `schema/inventory.sql`, `data/inventory.sql`
- `Payment`: `schema/payment.sql`, `data/payment.sql`
- `Storage`: `schema/storage.sql`, `data/storage.sql`

当前状态：

- `Order` 当前没有独立的 `data/order.sql`
- `Storage` 当前不提供默认业务种子数据，`data/storage.sql` 可为空模板

## 4. Execution Order

手工初始化数据库时，推荐顺序：

1. `db/schema/upms.sql`
2. `db/data/upms.sql`
3. `db/schema/auth.sql`
4. `db/data/auth.sql`
5. `db/schema/order.sql`
6. `db/schema/inventory.sql`
7. `db/data/inventory.sql`
8. `db/schema/payment.sql`
9. `db/data/payment.sql`
10. `db/schema/storage.sql`
11. `db/data/storage.sql`

执行原则：

- 先执行 `schema/`，再执行 `data/`
- 存在上游主数据依赖的业务域，必须排在依赖方之后
- 若某业务域没有 `data` 脚本，则跳过初始化步骤

## 5. Change Policy

- 修改表结构前，先同步对应 `../docs/20-database/*-DATABASE-DESIGN.md`
- 修改固定初始化数据前，先同步对应 `../docs/10-requirements/*-REQUIREMENTS.md`
- 若规则层发生变化，先同步 [`../docs/00-governance/DATABASE-RULES.md`](../docs/00-governance/DATABASE-RULES.md)
- 后续若接入数据库迁移工具，本目录默认作为基线来源，不直接删除已有脚本

## 6. Seed Rules

- 初始化数据使用固定主键和固定业务键
- 初始化脚本必须保持幂等，重复执行后收敛到固定状态
- 密码和客户端密钥不在表中保存明文
- 如脚本依赖其他业务域主数据，依赖关系必须写在数据库设计文档或本文件中
