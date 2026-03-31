# DATABASE REQUIREMENTS

## 1. Purpose

本文档定义 Bacon 项目的统一数据库要求。  
目标是让 AI 在设计任意业务域数据库时，先遵守本文件，再编写具体业务域数据库设计文档。  
本文件是工程级数据库规范，不承载单一业务域的专有规则。

## 2. Scope

当前范围覆盖：

- 数据库基础约束
- 表命名规则
- 字段命名规则
- 通用字段规则
- 关系表规则
- 索引与唯一性规则
- 枚举与 JSON 存储规则
- 审计日志规则
- 数据库设计文档写法要求

不覆盖：

- 某个业务域的状态流转细节
- 某个业务域的 DTO 契约
- SQL 迁移脚本内容

## 3. Database Platform Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库实例可用的 `utf8mb4` 排序规则（推荐 `utf8mb4_unicode_ci`）
- 时间字段统一使用 `datetime(3)`
- 主键字段默认使用 `bigint`
- 当某业务域已明确采用统一文本型领域主标识，且该标识需要作为跨域稳定用户主体标识时，可在业务域数据库设计文档中显式声明主键字段使用 `varchar`
- 布尔字段统一使用 `tinyint(1)`
- 金额字段统一使用 `decimal(18,2)`
- 枚举字段统一使用 `varchar`
- 高频查询索引优先围绕业务键、状态字段、时间字段设计

## 4. Naming Rules

- 表名固定格式使用 `bacon_${domain}_${model}`
- 表名不得省略 `bacon_` 系统前缀
- 业务域数据库前缀必须与模块名一致：
    - `Auth` 使用 `bacon_auth_`
    - `UPMS` 使用 `bacon_upms_`
    - `Order` 使用 `bacon_order_`
    - `Inventory` 使用 `bacon_inventory_`
    - `Payment` 使用 `bacon_payment_`
- 关系表在 `model` 后缀中显式体现关系语义，例如 `*_user_role_rel`
- 审计日志表统一使用 `*_audit_log`
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 逻辑删除列统一命名为 `deleted`
- 创建时间统一命名为 `created_at`
- 更新时间统一命名为 `updated_at`
- 审计发生时间统一命名为 `occurred_at`
- 其他领域时间字段使用业务语义命名，例如 `issued_at`、`received_at`、`granted_at`、`released_at`、`deducted_at`

## 5. Table Type Rules

### 5.1 Master Table

- 面向后台维护的主数据表和配置表
- 统一包含 `created_at`、`created_by`、`updated_at`、`updated_by`
- 如业务支持逻辑删除，统一包含 `deleted`

### 5.2 Runtime Table

- 面向运行时状态的业务表，例如订单、支付单、会话、令牌、预占单
- 必须保留业务主状态和必要领域时间字段
- 不强制包含 `created_by`、`updated_by`
- 不得为了“字段统一”机械增加无业务意义的通用字段

### 5.3 Relation Table

- 只保存关联关系本身所需的最小字段
- 当前规范下，关系表不增加 `created_at`、`created_by`、`updated_at`、`updated_by`
- 关系表应以联合唯一约束表达关系唯一性
- 关系变更历史由审计日志承载，不由关系表自身承载

### 5.4 Ledger Table

- 只追加，不更新历史记录
- 使用 `occurred_at` 等领域时间字段
- 不强制包含通用审计字段

### 5.5 Audit Log Table

- 只追加，不更新历史记录
- 使用 `occurred_at`
- 不使用逻辑删除
- 保存摘要，不保存敏感明文

## 6. Common Field Rules

- `created_by`、`updated_by` 的类型统一使用 `bigint`
- 如审计人或操作人字段明确复用文本型统一 ID，可在业务域数据库设计文档中显式声明使用 `varchar`
- `deleted` 的类型统一使用 `tinyint(1)`
- 使用领域时间字段的表，不再额外重复声明 `created_at` / `updated_at`
- 运行态表是否需要 `updated_at`，取决于该表是否存在持续更新语义
- 单纯快照表可只保留 `updated_at`
- 明细表是否需要时间字段，取决于是否存在独立生命周期；没有则不加
- Java 领域模型中表达绝对时间点的字段统一使用 `Instant`
- `createdAt`、`updatedAt`、`occurredAt`、`expireAt`、`issuedAt`、`releasedAt`、`deductedAt`、`lockedUntil` 统一视为绝对时间点
- `LocalDateTime` 只用于表达本地业务时间，不用于跨服务传递或持久化真相时间点
- `infra` 持久化层负责 `Instant` 与数据库 `datetime(3)` 的 `UTC` 转换

## 7. Relationship Rules

- 当前设计默认不强制数据库外键
- 关联一致性由应用层和领域层保证
- 所有跨表写入必须校验业务键与 `tenant_id` 一致性
- 跨域引用对端数据时，当前域只保留必要业务键或快照，不复制对端主表结构

## 8. Uniqueness And Index Rules

- 主键 `id` 全局唯一
- 稳定业务键必须显式建立唯一约束
- 联合唯一约束必须直接表达业务不变量，不能只依赖应用层判断
- 高频查询条件必须对应显式索引
- 多租户查询索引优先让 `tenant_id` 位于联合索引前缀，但全局唯一业务键可直接做全局唯一索引
- 状态 + 时间类批处理查询必须建立组合索引

## 9. Storage Rules

- 集合类结构优先使用 `json`，前提是它们不作为高频过滤条件
- 审计摘要可使用 `json`
- 渠道原始回调、授权范围、重定向地址等结构化集合可使用 `json`
- 不允许在数据库中保存明文密码、明文令牌、明文客户端密钥、短信验证码等敏感数据
- 哈希类字段统一保存哈希结果，不保存明文

## 10. Audit Rules

- 每个业务域都应有独立审计日志表
- 审计日志必须可按租户、对象、操作人、时间范围查询
- 审计日志写入失败不得影响主业务提交结果
- 审计日志不得记录敏感明文
- 关系变更、密码管理、状态变更、导入导出等关键操作必须进入审计日志

## 11. Cache And Persistence Boundary Rules

- 缓存对象必须能映射回稳定持久化表或明确声明为“只缓存不落库”
- JWT payload、授权请求上下文、短信验证码等运行时短期对象，可以只走缓存不建表
- DTO、VO、值对象、聚合视图、命令返回模型默认不建表
- 快照对象是否建表，取决于是否需要独立持久化与查询

## 12. Database Design Document Rules

- 每个业务域数据库设计文档都必须遵守本文件
- 每份数据库设计文档至少包含：
    - `Purpose`
    - `Scope`
    - `Database Rules`
    - `Naming Rules`
    - `Enum Storage Rules`
    - `Table Mapping`
    - `Table Design`
    - `Relationship Rules`
    - `Persistence Rules`
    - `Query Model Rules`
    - `Open Items`
- 已确认的统一规则不得在各业务域文档中写成模糊口径
- `Open Items` 为空时必须写 `无`
- 文档中的表名、字段名、索引名必须与后续 `DDL`、`DataObject`、`Mapper` 保持一致

## 13. Open Items

无
