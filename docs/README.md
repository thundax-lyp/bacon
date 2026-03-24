# Docs README

## 1. Purpose

本文档用于说明 `docs` 目录下各文档的功能与适用场景。  
目标是让 AI 按需加载文档，而不是一次性读取全部文档。  
默认原则：先加载最少必要文档，缺信息时再追加读取。

## 2. Default Loading Order

标准起点：

1. [`ARCHITECTURE.md`](./ARCHITECTURE.md)
2. 按任务所属业务域加载对应 `*-REQUIREMENTS.md`
3. 如果任务涉及数据库设计，再加载 [`DATABASE-RULES.md`](./DATABASE-RULES.md)
4. 如果任务涉及某业务域数据库，再加载对应 `*-DATABASE-DESIGN.md`
5. 如果任务涉及写文档规范，再加载 [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md)

## 3. File Index

### 3.1 Architecture

- [`ARCHITECTURE.md`](./ARCHITECTURE.md)
  - 用途：项目总架构、模块边界、分层依赖、单体与微服务装配规则、工程级约束。
  - 何时加载：几乎所有实现任务的第一步。
  - 不解决的问题：具体业务规则、具体表结构、具体接口字段。

### 3.2 Documentation Rules

- [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md)
  - 用途：需求文档、设计文档、AI 输入文档的统一写作规范。
  - 何时加载：需要新增或修改 `docs` 下文档时。
  - 不解决的问题：具体业务实现与数据库字段定义。

### 3.3 Database Rules

- [`DATABASE-RULES.md`](./DATABASE-RULES.md)
  - 用途：数据库平台、命名、通用字段、关系表、索引、审计、JSON 存储等统一数据库规范。
  - 何时加载：任何数据库设计任务的第一步。
  - 不解决的问题：单一业务域的专有表结构。

### 3.4 Business Requirements

- [`AUTH-REQUIREMENTS.md`](./AUTH-REQUIREMENTS.md)
  - 用途：认证、会话、OAuth2、认证审计的业务边界与固定契约。
  - 何时加载：涉及登录、令牌、会话、OAuth2、认证审计时。

- [`AUTH-LOGIN-DESIGN.md`](./AUTH-LOGIN-DESIGN.md)
  - 用途：账号密码登录中的验证码、RSA 传输、跨域凭据读取与 mono/micro 双模式实现设计。
  - 何时加载：涉及账号密码登录设计或实现时。

- [`UPMS-REQUIREMENTS.md`](./UPMS-REQUIREMENTS.md)
  - 用途：用户、租户、组织、角色、菜单、资源、数据权限的业务边界与固定契约。
  - 何时加载：涉及用户、权限、组织、导入导出、UPMS 审计时。

- [`ORDER-REQUIREMENTS.md`](./ORDER-REQUIREMENTS.md)
  - 用途：订单、订单状态流转、取消、超时关闭、支付结果处理的业务边界与固定契约。
  - 何时加载：涉及下单、订单查询、取消、支付结果回传时。

- [`INVENTORY-REQUIREMENTS.md`](./INVENTORY-REQUIREMENTS.md)
  - 用途：库存主数据、预占、释放、扣减、库存流水与审计的业务边界与固定契约。
  - 何时加载：涉及库存查询、库存预占/释放/扣减时。

- [`PAYMENT-REQUIREMENTS.md`](./PAYMENT-REQUIREMENTS.md)
  - 用途：支付单、支付回调、支付关闭、支付审计的业务边界与固定契约。
  - 何时加载：涉及支付创建、回调处理、关闭、支付状态查询时。

### 3.5 Business Database Designs

- [`AUTH-DATABASE-DESIGN.md`](./AUTH-DATABASE-DESIGN.md)
  - 用途：`Auth` 数据表、字段、索引、持久化边界。
  - 何时加载：设计或实现 `Auth` 持久化时。
  - 前置：先加载 [`DATABASE-RULES.md`](./DATABASE-RULES.md)。

- [`UPMS-DATABASE-DESIGN.md`](./UPMS-DATABASE-DESIGN.md)
  - 用途：`UPMS` 数据表、字段、索引、审计表、查询模型。
  - 何时加载：设计或实现 `UPMS` 持久化时。
  - 前置：先加载 [`DATABASE-RULES.md`](./DATABASE-RULES.md)。

- [`ORDER-DATABASE-DESIGN.md`](./ORDER-DATABASE-DESIGN.md)
  - 用途：`Order` 主表、明细、快照、审计表的数据库设计。
  - 何时加载：设计或实现订单持久化时。
  - 前置：先加载 [`DATABASE-RULES.md`](./DATABASE-RULES.md)。

- [`INVENTORY-DATABASE-DESIGN.md`](./INVENTORY-DATABASE-DESIGN.md)
  - 用途：`Inventory` 主表、预占单、明细、流水、审计表的数据库设计。
  - 何时加载：设计或实现库存持久化时。
  - 前置：先加载 [`DATABASE-RULES.md`](./DATABASE-RULES.md)。

- [`PAYMENT-DATABASE-DESIGN.md`](./PAYMENT-DATABASE-DESIGN.md)
  - 用途：`Payment` 主表、回调记录、审计表的数据库设计。
  - 何时加载：设计或实现支付持久化时。
  - 前置：先加载 [`DATABASE-RULES.md`](./DATABASE-RULES.md)。

## 4. Task Routing

### 4.1 Implement Feature

按以下顺序加载：

1. [`ARCHITECTURE.md`](./ARCHITECTURE.md)
2. 对应业务域 `*-REQUIREMENTS.md`

如果涉及数据库：

3. [`DATABASE-RULES.md`](./DATABASE-RULES.md)
4. 对应业务域 `*-DATABASE-DESIGN.md`

### 4.2 Write Or Revise Docs

按以下顺序加载：

1. [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md)
2. 目标业务域 `*-REQUIREMENTS.md` 或 `*-DATABASE-DESIGN.md`
3. 若涉及工程约束，再补 [`ARCHITECTURE.md`](./ARCHITECTURE.md) 或 [`DATABASE-RULES.md`](./DATABASE-RULES.md)

### 4.3 Database Design Task

按以下顺序加载：

1. [`ARCHITECTURE.md`](./ARCHITECTURE.md)
2. [`DATABASE-RULES.md`](./DATABASE-RULES.md)
3. 对应业务域 `*-REQUIREMENTS.md`
4. 对应业务域 `*-DATABASE-DESIGN.md`

### 4.4 Code Review Task

按以下顺序加载：

1. [`ARCHITECTURE.md`](./ARCHITECTURE.md)
2. 对应业务域 `*-REQUIREMENTS.md`
3. 如果改动涉及数据库，再加载 [`DATABASE-RULES.md`](./DATABASE-RULES.md) 与对应 `*-DATABASE-DESIGN.md`

## 5. Minimal Loading Rules

- 只做业务实现时，不默认加载所有数据库设计文档。
- 只做数据库设计时，不默认加载所有业务需求文档，只加载目标业务域。
- 只改某一域时，不默认加载其他业务域数据库设计文档。
- 只有在处理跨域协作链路时，才同时加载多个业务域需求文档。
- 工程级规则优先级高于业务域文档：
  - 架构规则以 [`ARCHITECTURE.md`](./ARCHITECTURE.md) 为准
  - 数据库规则以 [`DATABASE-RULES.md`](./DATABASE-RULES.md) 为准
  - 文档写作规则以 [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md) 为准

## 6. Open Items

无
