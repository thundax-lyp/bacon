# Database Rules

本文件只定义工程级数据库红线。

## Platform

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 时间字段统一使用 `datetime(3)`
- 主键字段默认使用 `bigint`
- 布尔字段统一使用 `tinyint(1)`
- 金额字段统一使用 `decimal(18,2)`
- 枚举字段统一使用 `varchar`

## Naming

- 表名格式：`bacon_${domain}_${model}`
- 不得省略 `bacon_` 前缀
- 域前缀必须与模块名一致：
  - `auth` -> `bacon_auth_`
  - `upms` -> `bacon_upms_`
  - `order` -> `bacon_order_`
  - `inventory` -> `bacon_inventory_`
  - `payment` -> `bacon_payment_`
- 关系表后缀显式表达语义，例如 `*_user_role_rel`
- 审计日志表统一使用 `*_audit_log`
- 主键列默认命名为 `id`
- 租户列统一命名为 `tenant_id`
- 逻辑删除列统一命名为 `deleted`
- 审计发生时间统一命名为 `occurred_at`

## Table Types

- 主数据表：
  后台维护的主数据和配置表；需要逻辑删除时统一使用 `deleted`
- 运行时业务表：
  保留业务主状态和必要领域时间字段，不机械补齐无意义通用字段
- 关系表：
  只保留关系本身的最小字段，关系唯一性用联合唯一约束表达
- 台账表：
  只追加，不回写历史
- 审计日志表：
  只追加，不逻辑删除，不保存敏感明文

## Field Rules

- 是否增加时间字段，取决于对象是否有独立生命周期
- Java 中绝对时间点统一使用 `Instant`
- `infra` 负责 `Instant` 与 `datetime(3)` 的 UTC 转换
- `LocalDateTime` 不作为跨服务和持久化真相时间

## Relationship Rules

- 默认不强制数据库外键
- 关联一致性由应用层和领域层保证
- 跨表写入必须校验业务键与 `tenant_id` 一致性
- 跨域引用只保留必要业务键或快照，不复制对端主表结构

## Index And Uniqueness

- 主键必须唯一
- 稳定业务键必须建立唯一约束
- 联合唯一约束必须直接表达业务不变量
- 高频查询条件必须有显式索引
- 多租户查询索引优先让 `tenant_id` 位于联合索引前缀
- 状态 + 时间类批处理查询必须建立组合索引

## Storage Rules

- 非高频过滤的集合结构可使用 `json`
- 审计摘要可使用 `json`
- 不保存明文密码、明文令牌、明文密钥、明文验证码
- 哈希类字段只保存哈希结果

## Audit Rules

- 每个业务域都应有独立审计日志表
- 审计日志必须支持按租户、对象、操作人、时间范围查询
- 审计日志写入失败不得影响主业务提交
- 状态变更、关系变更、密码管理、导入导出等关键操作必须入审计日志

## Cache Boundary

- 缓存对象必须能映射回稳定持久化表，或明确声明为“只缓存不落库”
- DTO、VO、值对象、聚合视图、命令返回模型默认不建表
- 运行时短期对象可只走缓存不建表

## Document Requirements

- 业务域数据库设计文档必须遵守本文件
- 每份数据库设计文档至少包含：
  - `Purpose`
  - `Scope`
  - `Database Rules`
  - `Naming Rules`
  - `Table Mapping`
  - `Table Design`
  - `Relationship Rules`
  - `Persistence Rules`
  - `Query Model Rules`
  - `Open Items`
- `Open Items` 为空时必须写 `无`
- 文档中的表名、字段名、索引名必须与后续 `DDL`、`DO`、`Mapper` 一致
