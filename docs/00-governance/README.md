# Docs Index

`docs` 目录用于提供工程级规则、业务需求、数据库设计和专项设计文档。

默认原则：

- 先读最少必要文档
- 先读工程级规则，再读业务域文档
- 涉及数据库时，再补数据库规则和对应设计文档

## 默认加载顺序

实现或评审任务默认按以下顺序加载：

1. [`ARCHITECTURE.md`](./ARCHITECTURE.md)
2. 对应业务域 `*-REQUIREMENTS.md`
3. 如涉及数据库，加载 [`DATABASE-RULES.md`](./DATABASE-RULES.md)
4. 如涉及具体持久化，加载对应 `*-DATABASE-DESIGN.md`
5. 如涉及统一 ID，加载 [`UNIFIED-ID-DESIGN.md`](./UNIFIED-ID-DESIGN.md)
6. 如涉及文档编写规则，加载 [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md)

目录分层固定如下：

- `00-governance`：工程级规则、架构、文档索引
- `10-requirements`：业务需求文档
- `20-database`：数据库设计文档
- `30-designs`：专项设计与规划文档
- `40-readiness`：上线准备、运行手册、发布清单

## 文档索引

### 工程级文档

- [`ARCHITECTURE.md`](./ARCHITECTURE.md)
  - 定义模块边界、分层依赖、mono/micro 装配规则、工程约束
- [`DATABASE-RULES.md`](./DATABASE-RULES.md)
  - 定义统一数据库规范
- [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md)
  - 定义文档写作规范
- [`UNIFIED-ID-DESIGN.md`](./UNIFIED-ID-DESIGN.md)
  - 定义领域 ID、数据库主键、业务单号的建模边界

### 业务需求文档

- [`AUTH-REQUIREMENTS.md`](../10-requirements/AUTH-REQUIREMENTS.md)
  - 认证、会话、OAuth2、认证审计
- [`UPMS-REQUIREMENTS.md`](../10-requirements/UPMS-REQUIREMENTS.md)
  - 用户、租户、组织、角色、菜单、资源、数据权限
- [`ORDER-REQUIREMENTS.md`](../10-requirements/ORDER-REQUIREMENTS.md)
  - 订单、取消、超时关闭、支付结果处理
- [`INVENTORY-REQUIREMENTS.md`](../10-requirements/INVENTORY-REQUIREMENTS.md)
  - 库存主数据、预占、释放、扣减、审计
- [`PAYMENT-REQUIREMENTS.md`](../10-requirements/PAYMENT-REQUIREMENTS.md)
  - 支付单、回调、关闭、支付审计
- [`STORAGE-REQUIREMENTS.md`](../10-requirements/STORAGE-REQUIREMENTS.md)
  - 统一存储对象、上传、引用、访问地址

### 专项设计文档

- [`AUTH-LOGIN-DESIGN.md`](../30-designs/AUTH-LOGIN-DESIGN.md)
  - 账号密码登录中的验证码、RSA、跨域凭据读取与 mono/micro 实现设计
- [`PRODUCTION-UPGRADE-ROADMAP.md`](../30-designs/PRODUCTION-UPGRADE-ROADMAP.md)
  - 项目未来 4 周的生产就绪度升级路线、交付物与验收门槛
- [`STORAGE-LAUNCH-READINESS.md`](../40-readiness/STORAGE-LAUNCH-READINESS.md)
  - `Storage` 模块上线准备清单与验收项

### 数据库设计文档

- [`AUTH-DATABASE-DESIGN.md`](../20-database/AUTH-DATABASE-DESIGN.md)
- [`UPMS-DATABASE-DESIGN.md`](../20-database/UPMS-DATABASE-DESIGN.md)
- [`ORDER-DATABASE-DESIGN.md`](../20-database/ORDER-DATABASE-DESIGN.md)
- [`INVENTORY-DATABASE-DESIGN.md`](../20-database/INVENTORY-DATABASE-DESIGN.md)
- [`PAYMENT-DATABASE-DESIGN.md`](../20-database/PAYMENT-DATABASE-DESIGN.md)
- [`STORAGE-DATABASE-DESIGN.md`](../20-database/STORAGE-DATABASE-DESIGN.md)

这些文档都应在对应业务域 `*-REQUIREMENTS.md` 和 [`DATABASE-RULES.md`](./DATABASE-RULES.md) 之后再读取。

## 使用约定

- 只做某一业务域实现时，不默认加载其他业务域文档
- 只做数据库设计时，不默认加载所有需求文档
- 处理跨域链路时，才同时加载多个业务域需求文档
- 工程级规则优先级高于业务域文档
