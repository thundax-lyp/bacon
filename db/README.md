# DB README

## 1. Purpose

本文档说明 `db/` 目录下数据库脚本的用途、目录结构、执行顺序和维护规则。

本文档面向工程师快速定位脚本，也作为数据库脚本与 `docs` 文档之间的导航入口。

## 2. Scope

当前范围覆盖：

- `schema/` 目录中的建表脚本
- `data/` 目录中的初始化数据脚本
- 当前已存在业务域脚本的目录约定
- 手工执行脚本时的推荐顺序

当前范围不覆盖：

- `Flyway` 或 `Liquibase` 的最终接入方案
- 本地 `MySQL` 安装说明
- 生产环境发布流程

## 3. Related Docs

阅读数据库脚本前，固定先看以下文档：

1. [docs/AGENT.md](/Volumes/storage/workspace/bacon/docs/AGENT.md)
2. [docs/00-governance/DATABASE-RULES.md](/Volumes/storage/workspace/bacon/docs/00-governance/DATABASE-RULES.md)
3. 对应业务域数据库设计文档，位于 `docs/20-database`

常用入口：

- [AUTH-DATABASE-DESIGN.md](/Volumes/storage/workspace/bacon/docs/20-database/AUTH-DATABASE-DESIGN.md)
- [UPMS-DATABASE-DESIGN.md](/Volumes/storage/workspace/bacon/docs/20-database/UPMS-DATABASE-DESIGN.md)
- [ORDER-DATABASE-DESIGN.md](/Volumes/storage/workspace/bacon/docs/20-database/ORDER-DATABASE-DESIGN.md)
- [INVENTORY-DATABASE-DESIGN.md](/Volumes/storage/workspace/bacon/docs/20-database/INVENTORY-DATABASE-DESIGN.md)
- [PAYMENT-DATABASE-DESIGN.md](/Volumes/storage/workspace/bacon/docs/20-database/PAYMENT-DATABASE-DESIGN.md)
- [STORAGE-DATABASE-DESIGN.md](/Volumes/storage/workspace/bacon/docs/20-database/STORAGE-DATABASE-DESIGN.md)

## 4. Directory Structure

```text
db/
├── README.md
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

固定规则：

- `schema/` 只放建表脚本
- `data/` 只放初始化和测试数据脚本
- 文件名固定使用业务域名小写
- 新增业务域脚本时，优先保持 `schema/<domain>.sql` 与 `data/<domain>.sql` 成对出现
- 若某业务域暂不提供初始化数据，可只存在 `schema/<domain>.sql`

## 5. File Usage

### 5.1 `schema/`

- `schema/upms.sql`：`UPMS` 全量表结构
- `schema/auth.sql`：`Auth` 全量表结构
- `schema/order.sql`：`Order` 全量表结构
- `schema/inventory.sql`：`Inventory` 全量表结构
- `schema/payment.sql`：`Payment` 全量表结构
- `schema/storage.sql`：`Storage` 全量表结构

### 5.2 `data/`

- `data/upms.sql`：`UPMS` 初始化和测试数据
- `data/auth.sql`：`Auth` 初始化和测试数据
- `data/inventory.sql`：`Inventory` 初始化和测试数据
- `data/payment.sql`：`Payment` 初始化和测试数据
- `data/storage.sql`：`Storage` 初始化和测试数据模板

当前状态：

- `Order` 当前没有独立的 `data/order.sql`
- `Storage` 当前不提供默认业务种子数据，脚本可为空模板

## 6. Execution Order

手工初始化数据库时，推荐按以下顺序执行：

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

固定原则：

- 先执行 `schema/`，再执行 `data/`
- 存在上游主数据依赖的业务域，必须排在依赖方之后
- 若某业务域没有 `data` 脚本，则跳过初始化步骤

## 7. Seed Data Rules

- 初始化数据使用固定主键和固定业务键
- 初始化脚本统一使用幂等写法，重复执行后应收敛到固定状态
- 初始化数据中的密码和客户端密钥只提供说明，不在表中保存明文
- 表中实际保存值固定为哈希结果或安全存储值
- 如脚本依赖其他业务域主数据，依赖关系必须在本文件或对应数据库设计文档中写明

## 8. Example Usage

示例命令：

```bash
mysql -u root -p < db/schema/upms.sql
mysql -u root -p < db/data/upms.sql
mysql -u root -p < db/schema/auth.sql
mysql -u root -p < db/data/auth.sql
mysql -u root -p < db/schema/order.sql
mysql -u root -p < db/schema/inventory.sql
mysql -u root -p < db/data/inventory.sql
mysql -u root -p < db/schema/payment.sql
mysql -u root -p < db/data/payment.sql
mysql -u root -p < db/schema/storage.sql
mysql -u root -p < db/data/storage.sql
```

如果需要指定数据库：

```bash
mysql -u root -p bacon < db/schema/upms.sql
mysql -u root -p bacon < db/data/upms.sql
mysql -u root -p bacon < db/schema/auth.sql
mysql -u root -p bacon < db/data/auth.sql
mysql -u root -p bacon < db/schema/order.sql
mysql -u root -p bacon < db/schema/inventory.sql
mysql -u root -p bacon < db/data/inventory.sql
mysql -u root -p bacon < db/schema/payment.sql
mysql -u root -p bacon < db/data/payment.sql
mysql -u root -p bacon < db/schema/storage.sql
mysql -u root -p bacon < db/data/storage.sql
```

## 9. Maintenance Rules

- 修改表结构时，先同步 `docs/20-database` 下对应设计文档，再修改 `schema/*.sql`
- 修改固定初始化数据时，先同步 `docs/10-requirements` 下对应需求文档，再修改 `data/*.sql`
- 若规则层发生变化，先同步 [DATABASE-RULES.md](/Volumes/storage/workspace/bacon/docs/00-governance/DATABASE-RULES.md)
- 后续若接入数据库迁移工具，本目录默认作为基线来源，不直接删除已有脚本

## 10. Open Items

- 数据库迁移工具最终固定为 `Flyway` 还是 `Liquibase`
- `Order` 是否需要补齐独立 `data/order.sql`
