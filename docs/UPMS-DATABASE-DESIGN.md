# UPMS DATABASE DESIGN

## 1. Purpose

本文档定义 `UPMS` 的数据结构与数据库设计。  
目标是让 AI 可直接据此生成表结构、实体、`Mapper`、`Repository` 和查询实现。  
本文档只保留数据库设计所需的稳定信息，不重复业务需求文档中的冗余描述。

## 2. Scope

当前范围覆盖：

- `Tenant`
- `User`
- `UserIdentity`
- `Department`
- `Post`
- `Role`
- `Menu`
- `Resource`
- `UserRoleRelation`
- `UserPostRelation`
- `RoleMenuRelation`
- `RoleResourceRelation`
- `DataPermissionRule`
- `RoleDataScopeRelation`
- `UPMS Audit Log`

不覆盖：

- `Auth` 会话与令牌表
- `Order`、`Inventory`、`Payment` 业务表
- 导入导出临时文件表

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则固定使用 `utf8mb4_0900_ai_ci`
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 业务布尔字段统一使用 `tinyint(1)`
- 金额字段不在 `UPMS` 范围内
- 枚举字段统一使用 `varchar`
- 所有需要逻辑删除的主数据表统一使用 `deleted`
- 所有跨租户数据表必须包含 `tenant_id`
- 所有查询高频索引必须把 `tenant_id` 放在联合索引前缀

## 4. Naming Rules

- 表名固定格式使用 `bacon_${domain}_${model}`
- `UPMS` 表名统一使用 `bacon_upms_` 前缀
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 状态列统一命名为 `status`
- 逻辑删除列统一命名为 `deleted`
- 创建时间统一命名为 `created_at`
- 更新时间统一命名为 `updated_at`
- 审计发生时间统一命名为 `occurred_at`
- 版本控制当前不单独引入 `version` 字段

## 5. Enum Storage Rules

- `status`: `ENABLED`、`DISABLED`
- `identity_type`: `ACCOUNT`、`PHONE`、`WECOM`、`GITHUB`
- `role_type`: `SYSTEM_ROLE`、`TENANT_ROLE`、`CUSTOM_ROLE`
- `data_scope_type`: `ALL`、`DEPARTMENT`、`DEPARTMENT_AND_CHILDREN`、`SELF`、`CUSTOM`
- `menu_type`: `DIRECTORY`、`MENU`、`BUTTON`
- `resource_type`: `API`、`RPC`、`EVENT`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `Tenant` | `bacon_upms_tenant` |
| `User` | `bacon_upms_user` |
| `UserIdentity` | `bacon_upms_user_identity` |
| `Department` | `bacon_upms_department` |
| `Post` | `bacon_upms_post` |
| `Role` | `bacon_upms_role` |
| `Menu` | `bacon_upms_menu` |
| `Resource` | `bacon_upms_resource` |
| `UserRoleRelation` | `bacon_upms_user_role_rel` |
| `UserPostRelation` | `bacon_upms_user_post_rel` |
| `RoleMenuRelation` | `bacon_upms_role_menu_rel` |
| `RoleResourceRelation` | `bacon_upms_role_resource_rel` |
| `DataPermissionRule` | `bacon_upms_data_permission_rule` |
| `RoleDataScopeRelation` | `bacon_upms_role_data_scope_rel` |
| `UPMS Audit Log` | `bacon_upms_audit_log` |

## 7. Table Design

### 7.1 `bacon_upms_tenant`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 租户编码 |
| `name` | `varchar(128)` | N | 租户名称 |
| `status` | `varchar(16)` | N | `ENABLED` / `DISABLED` |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_id(tenant_id)`
- `uk_code(code)`

### 7.2 `bacon_upms_user`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `account` | `varchar(64)` | N | 全局唯一账号 |
| `name` | `varchar(128)` | N | 用户名称 |
| `phone` | `varchar(32)` | Y | 手机号 |
| `department_id` | `bigint` | Y | 部门主键 |
| `password_hash` | `varchar(255)` | N | `BCrypt` 哈希 |
| `need_change_password` | `tinyint(1)` | N | 首次登录是否必须改密 |
| `status` | `varchar(16)` | N | `ENABLED` / `DISABLED` |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_account(account)`
- `idx_tenant_department_status(tenant_id, department_id, status)`

### 7.3 `bacon_upms_user_identity`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `identity_type` | `varchar(16)` | N | 登录标识类型 |
| `identity_value` | `varchar(255)` | N | 登录标识值 |
| `enabled` | `tinyint(1)` | N | 是否可用于认证 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_identity(identity_type, identity_value)`
- `idx_tenant_user(tenant_id, user_id)`

### 7.4 `bacon_upms_department`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 部门编码 |
| `name` | `varchar(128)` | N | 部门名称 |
| `parent_id` | `bigint` | Y | 上级部门主键 |
| `leader_user_id` | `bigint` | Y | 负责人用户主键 |
| `status` | `varchar(16)` | N | `ENABLED` / `DISABLED` |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_code(code)`
- `idx_tenant_parent_status(tenant_id, parent_id, status)`

### 7.5 `bacon_upms_post`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 岗位编码 |
| `name` | `varchar(128)` | N | 岗位名称 |
| `sort` | `int` | N | 排序值，越小越靠前 |
| `status` | `varchar(16)` | N | `ENABLED` / `DISABLED` |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_code(code)`

### 7.6 `bacon_upms_role`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 角色编码 |
| `name` | `varchar(128)` | N | 角色名称 |
| `role_type` | `varchar(32)` | N | 角色类型 |
| `data_scope_type` | `varchar(32)` | N | 数据范围类型 |
| `status` | `varchar(16)` | N | `ENABLED` / `DISABLED` |
| `built_in` | `tinyint(1)` | N | 是否内置 |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_code(code)`
- `idx_tenant_role_type_status(tenant_id, role_type, status)`

### 7.7 `bacon_upms_menu`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `menu_type` | `varchar(16)` | N | `DIRECTORY` / `MENU` / `BUTTON` |
| `name` | `varchar(128)` | N | 菜单名称 |
| `parent_id` | `bigint` | Y | 上级菜单主键 |
| `route_path` | `varchar(255)` | Y | 路由路径 |
| `component_name` | `varchar(255)` | Y | 前端组件标识 |
| `icon` | `varchar(128)` | Y | 图标 |
| `sort` | `int` | N | 排序值 |
| `visible` | `tinyint(1)` | N | 是否可见 |
| `status` | `varchar(16)` | N | `ENABLED` / `DISABLED` |
| `permission_code` | `varchar(128)` | N | 全局唯一权限编码 |
| `built_in` | `tinyint(1)` | N | 是否内置 |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_permission_code(permission_code)`
- `idx_tenant_parent_status_visible(tenant_id, parent_id, status, visible)`

### 7.8 `bacon_upms_resource`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 资源编码 |
| `name` | `varchar(128)` | N | 资源名称 |
| `resource_type` | `varchar(16)` | N | `API` / `RPC` / `EVENT` |
| `module` | `varchar(64)` | N | 所属模块 |
| `path` | `varchar(255)` | N | 资源路径 |
| `method` | `varchar(16)` | N | 请求方法或调用方式 |
| `status` | `varchar(16)` | N | `ENABLED` / `DISABLED` |
| `permission_code` | `varchar(128)` | N | 全局唯一权限编码 |
| `built_in` | `tinyint(1)` | N | 是否内置 |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_code(code)`
- `uk_path_method(path, method)`
- `uk_permission_code(permission_code)`
- `idx_tenant_resource_type_status(tenant_id, resource_type, status)`

### 7.9 `bacon_upms_user_role_rel`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `role_id` | `bigint` | N | 角色主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_user_role(tenant_id, user_id, role_id)`
- `idx_tenant_user(tenant_id, user_id)`
- `idx_tenant_role(tenant_id, role_id)`

### 7.10 `bacon_upms_user_post_rel`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `post_id` | `bigint` | N | 岗位主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_user_post(tenant_id, user_id, post_id)`
- `idx_tenant_user(tenant_id, user_id)`
- `idx_tenant_post(tenant_id, post_id)`

### 7.11 `bacon_upms_role_menu_rel`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `role_id` | `bigint` | N | 角色主键 |
| `menu_id` | `bigint` | N | 菜单主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_role_menu(tenant_id, role_id, menu_id)`
- `idx_tenant_role(tenant_id, role_id)`
- `idx_tenant_menu(tenant_id, menu_id)`

### 7.12 `bacon_upms_role_resource_rel`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `role_id` | `bigint` | N | 角色主键 |
| `resource_id` | `bigint` | N | 资源主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_role_resource(tenant_id, role_id, resource_id)`
- `idx_tenant_role(tenant_id, role_id)`
- `idx_tenant_resource(tenant_id, resource_id)`

### 7.13 `bacon_upms_data_permission_rule`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `role_id` | `bigint` | N | 角色主键 |
| `data_scope_type` | `varchar(32)` | N | 数据范围类型 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_role(tenant_id, role_id)`
- `idx_tenant_role(tenant_id, role_id)`

### 7.14 `bacon_upms_role_data_scope_rel`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `role_id` | `bigint` | N | 角色主键 |
| `department_id` | `bigint` | N | 部门主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_role_department(tenant_id, role_id, department_id)`

### 7.15 `bacon_upms_audit_log`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `operator_id` | `bigint` | Y | 操作人用户主键 |
| `object_type` | `varchar(64)` | N | 对象类型 |
| `object_id` | `varchar(64)` | N | 对象标识 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `before_summary` | `json` | Y | 变更前摘要 |
| `after_summary` | `json` | Y | 变更后摘要 |
| `request_source` | `varchar(64)` | Y | 请求来源 |
| `result_status` | `varchar(32)` | N | 结果状态 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_object(object_type, object_id)`
- `idx_operator(operator_id, occurred_at)`

## 8. Relationship Rules

- `bacon_upms_user.department_id -> bacon_upms_department.id`
- `bacon_upms_user_identity.user_id -> bacon_upms_user.id`
- `bacon_upms_department.leader_user_id -> bacon_upms_user.id`
- `bacon_upms_user_role_rel.user_id -> bacon_upms_user.id`
- `bacon_upms_user_role_rel.role_id -> bacon_upms_role.id`
- `bacon_upms_user_post_rel.user_id -> bacon_upms_user.id`
- `bacon_upms_user_post_rel.post_id -> bacon_upms_post.id`
- `bacon_upms_role_menu_rel.role_id -> bacon_upms_role.id`
- `bacon_upms_role_menu_rel.menu_id -> bacon_upms_menu.id`
- `bacon_upms_role_resource_rel.role_id -> bacon_upms_role.id`
- `bacon_upms_role_resource_rel.resource_id -> bacon_upms_resource.id`
- `bacon_upms_data_permission_rule.role_id -> bacon_upms_role.id`
- `bacon_upms_role_data_scope_rel.role_id -> bacon_upms_role.id`
- `bacon_upms_role_data_scope_rel.department_id -> bacon_upms_department.id`

固定约束：

- 当前设计不强制数据库外键
- 关联一致性由应用层和领域层保证
- 所有跨表写入必须校验 `tenant_id` 一致性

## 9. Persistence Rules

- `User`、`Department`、`Post`、`Role`、`Menu`、`Resource` 统一逻辑删除
- `Tenant`、关系表、规则表、审计表当前不使用逻辑删除
- `User.account` 全局唯一，逻辑删除后也不得复用
- `Tenant.code`、`Department.code`、`Post.code`、`Role.code`、`Resource.code` 全局唯一
- `Menu.permission_code`、`Resource.permission_code` 全局唯一
- `Resource(path, method)` 全局唯一
- `UserIdentity(identity_type, identity_value)` 全局唯一
- 每个 `User` 至少保留一个 `ACCOUNT` 类型且 `enabled=1` 的身份标识
- `DataPermissionRule` 每个 `Role` 固定一条
- 当 `data_scope_type != CUSTOM` 时，`bacon_upms_role_data_scope_rel` 不应存在有效记录

## 10. Query Model Rules

- 用户详情查询主表为 `bacon_upms_user`
- 登录标识定位主表为 `bacon_upms_user_identity`
- 菜单树查询主表为 `bacon_upms_menu + bacon_upms_role_menu_rel + bacon_upms_user_role_rel`
- 权限码查询主表为 `bacon_upms_menu + bacon_upms_resource + 关联关系表`
- 数据权限查询主表为 `bacon_upms_role + bacon_upms_data_permission_rule + bacon_upms_role_data_scope_rel + bacon_upms_user_role_rel`
- 审计查询主表为 `bacon_upms_audit_log`

## 11. Cache Mapping Rules

- `upms:menu-tree:{tenantId}:{userId}` 来源于 `bacon_upms_menu`、`bacon_upms_role_menu_rel`、`bacon_upms_user_role_rel`
- `upms:perm-codes:{tenantId}:{userId}` 来源于 `bacon_upms_menu`、`bacon_upms_resource`、`bacon_upms_role_menu_rel`、`bacon_upms_role_resource_rel`、`bacon_upms_user_role_rel`
- `upms:data-scope:{tenantId}:{userId}` 来源于 `bacon_upms_role`、`bacon_upms_data_permission_rule`、`bacon_upms_role_data_scope_rel`、`bacon_upms_user_role_rel`

## 12. DDL Generation Notes

- 主键生成策略由应用统一控制，数据库侧不依赖自增语义
- 所有 `varchar` 长度不足以表达业务时，应先更新需求文档再调整表结构
- `json` 字段只允许用于审计摘要，不进入核心业务查询条件
- 高频读模型由 `Mapper` 查询组装，不新增冗余快照表

## 13. Open Items

- `Tenant.name`、`Department.name`、`Role.name`、`Menu.name`、`Resource.name` 的最大长度未在需求文档明确，当前按通用管理后台长度设计
- `request_source`、`result_status`、`object_type`、`action_type` 的固定值域未在需求文档完全展开，当前作为稳定字符串字段处理
