# UPMS DATABASE DESIGN

## 1. Purpose

本文档定义 `UPMS` 业务域的数据库设计。  
目标是让 AI 和工程师可直接据此生成 `DDL`、`DataObject`、`Mapper`、`Repository`、权限查询和审计查询实现。  
本文档只定义 `UPMS` 自有的持久化对象、字段、索引、关系、缓存映射和查询模型，不重复业务需求文档中的流程描述。  
本文档必须遵守 [DATABASE-RULES.md](./DATABASE-RULES.md)。如与工程级数据库规范冲突，以 [DATABASE-RULES.md](./DATABASE-RULES.md) 为准。

## 2. Scope

当前范围覆盖以下持久化对象：

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
- `UpmsAuditLog`
- `UpmsSysLog`

当前范围不建表的对象：

- `Auth` 会话与令牌表
- `Order`、`Inventory`、`Payment` 业务表
- 导入导出临时文件表
- `StoredObject` 主表与引用表

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库默认值
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 布尔字段统一使用 `tinyint(1)`
- 枚举字段统一使用 `varchar`
- `UPMS` 不涉及金额字段
- 所有跨租户数据表必须包含 `tenant_id`
- 所有高频查询联合索引优先让 `tenant_id` 位于前缀
- 主数据表统一包含 `created_by`、`created_at`、`updated_by`、`updated_at`
- 支持逻辑删除的主数据表统一包含 `deleted`
- 关系表保持最小必要字段，不增加 `created_at`、`updated_at`
- 审计表使用 `occurred_at`，不使用逻辑删除
- 访问日志表使用 `occurred_at`，不使用逻辑删除
- 当前设计不单独引入 `version` 字段

## 4. Naming Rules

- 表名前缀固定使用 `bacon_upms_`
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 状态字段统一命名为 `status`
- 逻辑删除字段统一命名为 `deleted`
- 创建时间统一命名为 `created_at`
- 更新时间统一命名为 `updated_at`
- 审计发生时间统一命名为 `occurred_at`

## 5. Enum Storage Rules

### 5.1 Fixed Enums

- `status`: `ENABLED`、`DISABLED`
- `identity_type`: `ACCOUNT`、`PHONE`、`WECOM`、`GITHUB`
- `role_type`: `SYSTEM_ROLE`、`TENANT_ROLE`、`CUSTOM_ROLE`
- `data_scope_type`: `ALL`、`DEPARTMENT`、`DEPARTMENT_AND_CHILDREN`、`SELF`、`CUSTOM`
- `menu_type`: `DIRECTORY`、`MENU`、`BUTTON`
- `resource_type`: `API`、`RPC`、`EVENT`
- `audit_object_type`: `USER`、`TENANT`、`DEPARTMENT`、`POST`、`ROLE`、`MENU`、`RESOURCE`、`USER_ROLE_RELATION`、`ROLE_MENU_RELATION`、`ROLE_RESOURCE_RELATION`、`DATA_PERMISSION_RULE`、`PASSWORD_MANAGEMENT`、`USER_IMPORT`、`USER_EXPORT`
- `audit_result_status`: `SUCCESS`、`FAILED`
- `request_source`: `ADMIN_WEB`、`SYSTEM_JOB`、`INTERNAL_API`、`IMPORT_TASK`、`EXPORT_TASK`
- `sys_log_event_type`: `LOGIN`、`LOGOUT`、`CREATE`、`UPDATE`、`DELETE`、`QUERY`、`EXPORT`、`IMPORT`、`GRANT`、`REVOKE`、`OTHER`
- `sys_log_result`: `SUCCESS`、`FAILURE`

### 5.2 Fixed Length Rules

- `tenant_id`: `varchar(64)`
- `code`: `varchar(64)`
- `account`: `varchar(64)`
- `phone`: `varchar(32)`
- `Tenant.name`: `varchar(128)`
- `Department.name`: `varchar(128)`
- `Post.name`: `varchar(128)`
- `Role.name`: `varchar(128)`
- `Menu.name`: `varchar(128)`
- `Resource.name`: `varchar(128)`
- `route_path`: `varchar(255)`
- `component_name`: `varchar(255)`
- `icon`: `varchar(128)`
- `permission_code`: `varchar(128)`
- `module`: `varchar(64)`
- `path`: `varchar(255)`
- `method`: `varchar(16)`
- `object_type`: `varchar(64)`
- `object_id`: `varchar(64)`
- `action_type`: `varchar(64)`
- `request_source`: `varchar(64)`
- `result_status`: `varchar(32)`
- `identity_value`: `varchar(255)`
- `password_hash`: `varchar(255)`
- `trace_id`: `varchar(64)`
- `request_id`: `varchar(64)`
- `client_ip`: `varchar(64)`
- `request_uri`: `varchar(255)`
- `http_method`: `varchar(16)`
- `error_message`: `varchar(1000)`

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
| `UpmsAuditLog` | `bacon_upms_audit_log` |
| `UpmsSysLog` | `bacon_upms_sys_log` |

## 7. Table Design

### 7.1 `bacon_upms_tenant`

表类型：`Master Table`

用途：

- 持久化租户主数据
- 作为一级隔离边界

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键，全局唯一 |
| `code` | `varchar(64)` | N | 租户编码，全局唯一 |
| `name` | `varchar(128)` | N | 租户名称 |
| `status` | `varchar(16)` | N | 状态，取值见 `status` |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_id(tenant_id)`
- `uk_code(code)`

### 7.2 `bacon_upms_user`

表类型：`Master Table`

用途：

- 持久化用户主体
- 保存密码哈希、状态和组织归属

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `account` | `varchar(64)` | N | 用户账号，全局唯一 |
| `name` | `varchar(128)` | N | 用户名称 |
| `phone` | `varchar(32)` | Y | 手机号 |
| `department_id` | `bigint` | Y | 部门主键 |
| `avatar_object_id` | `bigint` | Y | 用户头像对象主键，引用 `Storage` 域 `StoredObject` |
| `password_hash` | `varchar(255)` | N | 密码哈希，固定保存 `BCrypt` 哈希值 |
| `need_change_password` | `tinyint(1)` | N | 首次登录是否必须改密 |
| `status` | `varchar(16)` | N | 状态，取值见 `status` |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_account(account)`
- `idx_tenant_department_status(tenant_id, department_id, status)`
- `idx_avatar_object_id(avatar_object_id)`

### 7.3 `bacon_upms_user_identity`

表类型：`Master Table`

用途：

- 持久化登录标识
- 一个 `User` 可绑定多个 `UserIdentity`

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `identity_type` | `varchar(16)` | N | 标识类型，取值见 `identity_type` |
| `identity_value` | `varchar(255)` | N | 标识值 |
| `enabled` | `tinyint(1)` | N | 是否可用于认证 |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_identity(identity_type, identity_value)`
- `idx_tenant_user(tenant_id, user_id)`

### 7.4 `bacon_upms_department`

表类型：`Master Table`

用途：

- 持久化部门树结构
- 承载用户组织归属和数据权限组织范围

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 部门编码，全局唯一 |
| `name` | `varchar(128)` | N | 部门名称 |
| `parent_id` | `bigint` | Y | 上级部门主键 |
| `leader_user_id` | `bigint` | Y | 负责人用户主键 |
| `status` | `varchar(16)` | N | 状态，取值见 `status` |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_code(code)`
- `idx_tenant_parent_status(tenant_id, parent_id, status)`

### 7.5 `bacon_upms_post`

表类型：`Master Table`

用途：

- 持久化岗位主数据

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 岗位编码，全局唯一 |
| `name` | `varchar(128)` | N | 岗位名称 |
| `sort` | `int` | N | 排序值，越小越靠前 |
| `status` | `varchar(16)` | N | 状态，取值见 `status` |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_code(code)`

### 7.6 `bacon_upms_role`

表类型：`Master Table`

用途：

- 持久化角色主数据
- 承载数据范围类型和内置标记

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 角色编码，全局唯一 |
| `name` | `varchar(128)` | N | 角色名称 |
| `role_type` | `varchar(32)` | N | 角色类型，取值见 `role_type` |
| `data_scope_type` | `varchar(32)` | N | 数据范围类型，取值见 `data_scope_type` |
| `status` | `varchar(16)` | N | 状态，取值见 `status` |
| `built_in` | `tinyint(1)` | N | 是否内置 |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_code(code)`
- `idx_tenant_role_type_status(tenant_id, role_type, status)`

### 7.7 `bacon_upms_menu`

表类型：`Master Table`

用途：

- 持久化前端菜单与按钮
- 承载菜单树和菜单型权限编码

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `menu_type` | `varchar(16)` | N | 菜单类型，取值见 `menu_type` |
| `name` | `varchar(128)` | N | 菜单名称 |
| `parent_id` | `bigint` | Y | 上级菜单主键 |
| `route_path` | `varchar(255)` | Y | 路由路径 |
| `component_name` | `varchar(255)` | Y | 前端组件标识 |
| `icon` | `varchar(128)` | Y | 图标 |
| `sort` | `int` | N | 排序值 |
| `visible` | `tinyint(1)` | N | 是否可见 |
| `status` | `varchar(16)` | N | 状态，取值见 `status` |
| `permission_code` | `varchar(128)` | N | 权限编码，全局唯一 |
| `built_in` | `tinyint(1)` | N | 是否内置 |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_permission_code(permission_code)`
- `idx_tenant_parent_status_visible(tenant_id, parent_id, status, visible)`

### 7.8 `bacon_upms_resource`

表类型：`Master Table`

用途：

- 持久化接口、RPC、事件等资源权限对象

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `code` | `varchar(64)` | N | 资源编码，全局唯一 |
| `name` | `varchar(128)` | N | 资源名称 |
| `resource_type` | `varchar(16)` | N | 资源类型，取值见 `resource_type` |
| `module` | `varchar(64)` | N | 所属模块 |
| `path` | `varchar(255)` | N | 资源路径 |
| `method` | `varchar(16)` | N | 请求方法或调用方式 |
| `status` | `varchar(16)` | N | 状态，取值见 `status` |
| `permission_code` | `varchar(128)` | N | 权限编码，全局唯一 |
| `built_in` | `tinyint(1)` | N | 是否内置 |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_code(code)`
- `uk_path_method(path, method)`
- `uk_permission_code(permission_code)`
- `idx_tenant_resource_type_status(tenant_id, resource_type, status)`

### 7.9 `bacon_upms_user_role_rel`

表类型：`Relation Table`

用途：

- 持久化用户与角色的授权关系

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `role_id` | `bigint` | N | 角色主键 |

索引与约束：

- `pk(id)`
- `uk_user_role(tenant_id, user_id, role_id)`
- `idx_tenant_user(tenant_id, user_id)`
- `idx_tenant_role(tenant_id, role_id)`

### 7.10 `bacon_upms_user_post_rel`

表类型：`Relation Table`

用途：

- 持久化用户与岗位的绑定关系

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `post_id` | `bigint` | N | 岗位主键 |

索引与约束：

- `pk(id)`
- `uk_user_post(tenant_id, user_id, post_id)`
- `idx_tenant_user(tenant_id, user_id)`
- `idx_tenant_post(tenant_id, post_id)`

### 7.11 `bacon_upms_role_menu_rel`

表类型：`Relation Table`

用途：

- 持久化角色与菜单的授权关系

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `role_id` | `bigint` | N | 角色主键 |
| `menu_id` | `bigint` | N | 菜单主键 |

索引与约束：

- `pk(id)`
- `uk_role_menu(tenant_id, role_id, menu_id)`
- `idx_tenant_role(tenant_id, role_id)`
- `idx_tenant_menu(tenant_id, menu_id)`

### 7.12 `bacon_upms_role_resource_rel`

表类型：`Relation Table`

用途：

- 持久化角色与资源的授权关系

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `role_id` | `bigint` | N | 角色主键 |
| `resource_id` | `bigint` | N | 资源主键 |

索引与约束：

- `pk(id)`
- `uk_role_resource(tenant_id, role_id, resource_id)`
- `idx_tenant_role(tenant_id, role_id)`
- `idx_tenant_resource(tenant_id, resource_id)`

### 7.13 `bacon_upms_data_permission_rule`

表类型：`Master Table`

用途：

- 持久化角色级数据权限规则
- 每个 `Role` 固定一条规则记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `role_id` | `bigint` | N | 角色主键 |
| `data_scope_type` | `varchar(32)` | N | 数据范围类型，取值见 `data_scope_type` |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_role(tenant_id, role_id)`
- `idx_tenant_role(tenant_id, role_id)`

### 7.14 `bacon_upms_role_data_scope_rel`

表类型：`Relation Table`

用途：

- 持久化 `CUSTOM` 数据范围下的部门集合

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `role_id` | `bigint` | N | 角色主键 |
| `department_id` | `bigint` | N | 部门主键 |

索引与约束：

- `pk(id)`
- `uk_role_department(tenant_id, role_id, department_id)`

### 7.15 `bacon_upms_audit_log`

表类型：`Audit Log Table`

用途：

- 记录主数据维护、授权变更、密码管理、导入导出相关审计事件
- 只追加，不更新历史记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `operator_id` | `bigint` | Y | 操作人用户主键 |
| `object_type` | `varchar(64)` | N | 审计对象类型，取值见 `audit_object_type` |
| `object_id` | `varchar(64)` | N | 对象标识 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `before_summary` | `json` | Y | 变更前摘要 |
| `after_summary` | `json` | Y | 变更后摘要 |
| `request_source` | `varchar(64)` | Y | 请求来源，取值见 `request_source` |
| `result_status` | `varchar(32)` | N | 结果状态，取值见 `audit_result_status` |
| `occurred_at` | `datetime(3)` | N | 事件发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_object(object_type, object_id)`
- `idx_operator(operator_id, occurred_at)`

固定说明：

- `before_summary`、`after_summary` 只保存审计摘要，不保存完整业务对象
- 摘要字段名固定使用英文
- 密码相关操作摘要不得出现明文密码、密码哈希、临时密码
- 手机号、邮箱、证件号等敏感字段写入摘要时必须脱敏
- `FAILED` 日志必须在 `after_summary.failureReason` 中保存失败原因摘要
- `failureRows` 固定放在 `after_summary.failureRows`
- `failureRows` 固定为 JSON 数组，每个元素固定包含 `rowNo`、`account`、`reason`
- `failureRows` 最多保留前 `100` 条失败记录
- 单条 `reason` 最长保留 `256` 个字符
- 超过 `100` 条失败记录时，额外数量写入 `after_summary.remainingFailureCount`
- 导入审计摘要不保存整行原始导入数据
- 默认查询排序固定为 `occurred_at desc, id desc`
- 查询结果只返回摘要，不返回敏感原文

固定动作集合：

- `User`: `CREATE`、`UPDATE`、`ENABLE`、`DISABLE`、`DELETE`
- `Tenant`: `CREATE`、`UPDATE`、`ENABLE`、`DISABLE`
- `Department`: `CREATE`、`UPDATE`、`DISABLE`
- `Post`: `CREATE`、`UPDATE`、`DISABLE`、`SORT`
- `Role`: `CREATE`、`UPDATE`、`ENABLE`、`DISABLE`、`DELETE`
- `Menu`: `CREATE`、`UPDATE`、`ENABLE`、`DISABLE`
- `Resource`: `CREATE`、`UPDATE`、`ENABLE`、`DISABLE`
- `USER_ROLE_RELATION`: `ASSIGN`
- `ROLE_MENU_RELATION`: `ASSIGN`
- `ROLE_RESOURCE_RELATION`: `ASSIGN`
- `DATA_PERMISSION_RULE`: `CONFIGURE`
- `PASSWORD_MANAGEMENT`: `INIT_PASSWORD`、`RESET_PASSWORD`、`CHANGE_PASSWORD`
- `USER_IMPORT`: `IMPORT`
- `USER_EXPORT`: `EXPORT`

推荐摘要字段：

- `User`: `id`、`account`、`name`、`phoneMasked`、`departmentId`、`status`、`deleted`
- `Tenant`: `tenantId`、`code`、`name`、`status`
- `Department`: `id`、`code`、`name`、`parentId`、`leaderUserId`、`status`
- `Post`: `id`、`code`、`name`、`sort`、`status`
- `Role`: `id`、`code`、`name`、`roleType`、`dataScopeType`、`status`、`builtIn`
- `Menu`: `id`、`name`、`menuType`、`parentId`、`visible`、`status`、`permissionCode`
- `Resource`: `id`、`code`、`name`、`resourceType`、`module`、`path`、`method`、`status`、`permissionCode`
- `USER_ROLE_RELATION`: `userId`、`roleIds`
- `ROLE_MENU_RELATION`: `roleId`、`menuIds`
- `ROLE_RESOURCE_RELATION`: `roleId`、`resourceIds`
- `DATA_PERMISSION_RULE`: `roleId`、`dataScopeType`、`departmentIds`
- `PASSWORD_MANAGEMENT`: `userId`、`passwordAction`、`sessionInvalidationTriggered`
- `USER_IMPORT`: `successCount`、`failedCount`、`failureRows`
- `USER_EXPORT`: `exportCount`、`queryScope`

### 7.16 `bacon_upms_sys_log`

表类型：`Access Log Table`

用途：

- 记录带有 `@SysLog` 标记的 `Controller` 访问事件
- 承接来自 `common-log` 切面发送到 MQ 的访问日志消息
- 为运维排障、接口回溯、访问链路检索提供统一查询来源
- 与业务审计日志分离，不承载业务前后镜像摘要

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | Y | 租户业务键 |
| `trace_id` | `varchar(64)` | N | 链路标识 |
| `request_id` | `varchar(64)` | N | 请求标识 |
| `module` | `varchar(64)` | N | 模块标识 |
| `action` | `varchar(128)` | N | 操作描述 |
| `event_type` | `varchar(32)` | N | 事件类型，取值见 `sys_log_event_type` |
| `result` | `varchar(32)` | N | 执行结果，取值见 `sys_log_result` |
| `operator_id` | `bigint` | Y | 操作人用户主键 |
| `operator_name` | `varchar(64)` | Y | 操作人名称 |
| `client_ip` | `varchar(64)` | Y | 客户端IP |
| `request_uri` | `varchar(255)` | Y | 请求URI |
| `http_method` | `varchar(16)` | Y | HTTP方法 |
| `cost_ms` | `bigint` | Y | 访问耗时毫秒 |
| `error_message` | `varchar(1000)` | Y | 失败原因摘要 |
| `occurred_at` | `datetime(3)` | N | 事件发生时间 |
| `created_by` | `varchar(64)` | Y | 创建人标识 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人标识 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_trace_id(trace_id)`
- `idx_request_id(request_id)`

固定说明：

- 本表是访问日志表，不保存业务对象变更前后摘要
- `error_message` 只保存异常摘要，不保存堆栈全文
- `operator_name` 允许为空，不能作为唯一身份依据
- 默认查询排序固定为 `occurred_at desc, id desc`
- 日志文件与数据库表保存同一条消费结果，但数据库查询以本表为准
- 主键生成策略由应用统一控制，数据库侧不依赖自增语义
- 业务模块不得直接写本表，必须通过 `@SysLog -> aspect -> MQ -> UPMS consumer` 链路落库

## 8. Relationship Rules

- `bacon_upms_user.department_id` 关联 `bacon_upms_department.id`
- `bacon_upms_user.avatar_object_id` 关联 `Storage` 域 `bacon_storage_object.id`
- `bacon_upms_user_identity.user_id` 关联 `bacon_upms_user.id`
- `bacon_upms_department.leader_user_id` 关联 `bacon_upms_user.id`
- `bacon_upms_user_role_rel.user_id` 关联 `bacon_upms_user.id`
- `bacon_upms_user_role_rel.role_id` 关联 `bacon_upms_role.id`
- `bacon_upms_user_post_rel.user_id` 关联 `bacon_upms_user.id`
- `bacon_upms_user_post_rel.post_id` 关联 `bacon_upms_post.id`
- `bacon_upms_role_menu_rel.role_id` 关联 `bacon_upms_role.id`
- `bacon_upms_role_menu_rel.menu_id` 关联 `bacon_upms_menu.id`
- `bacon_upms_role_resource_rel.role_id` 关联 `bacon_upms_role.id`
- `bacon_upms_role_resource_rel.resource_id` 关联 `bacon_upms_resource.id`
- `bacon_upms_data_permission_rule.role_id` 关联 `bacon_upms_role.id`
- `bacon_upms_role_data_scope_rel.role_id` 关联 `bacon_upms_role.id`
- `bacon_upms_role_data_scope_rel.department_id` 关联 `bacon_upms_department.id`
- 当前设计不强制数据库外键
- 关联一致性由应用层和领域层保证
- 所有跨表写入必须校验 `tenant_id` 一致性
- `bacon_upms_sys_log.operator_id` 可关联 `bacon_upms_user.id`
- 当前设计不强制 `bacon_upms_sys_log.operator_id` 建立数据库外键
- `UPMS` 只保存 `avatar_object_id`，不复制 `Storage` 域对象的 `bucket_name`、`object_key`、`access_url`

## 9. Persistence Rules

- `User`、`Department`、`Post`、`Role`、`Menu`、`Resource` 统一逻辑删除
- `Tenant`、关系表、规则表、审计表当前不使用逻辑删除
- `User.account` 全局唯一，逻辑删除后也不得复用
- `User.avatar_object_id` 允许为空
- `User.avatar_object_id` 不为空时必须指向状态为 `ACTIVE` 的 `StoredObject`
- `Tenant.code`、`Department.code`、`Post.code`、`Role.code`、`Resource.code` 全局唯一
- `Menu.permission_code`、`Resource.permission_code` 全局唯一
- `Resource(path, method)` 全局唯一
- `UserIdentity(identity_type, identity_value)` 全局唯一
- 每个 `User` 至少保留一个 `ACCOUNT` 类型且 `enabled = 1` 的身份标识
- 每个 `Role` 固定一条 `DataPermissionRule`
- 当 `data_scope_type != CUSTOM` 时，`bacon_upms_role_data_scope_rel` 不应存在有效记录
- `status = DISABLED` 的 `Role`、`Menu`、`Resource` 不参与权限汇总结果
- `status = DISABLED` 的 `Department`、`Post` 不得再绑定新的 `User`
- `Tenant.status = DISABLED` 或 `User.status = DISABLED` 时，认证态即时失效由跨域调用 `bacon-auth-api` 触发，不在 `UPMS` 库内额外建表承载
- 主键生成策略由应用统一控制，数据库侧不依赖自增语义
- `bacon_upms_sys_log` 只允许追加写入，不允许更新历史记录
- `bacon_upms_sys_log.trace_id`、`bacon_upms_sys_log.request_id` 不要求全局唯一，但必须支持高频检索

## 10. Query Model Rules

- 用户详情查询主表固定为 `bacon_upms_user`
- 登录标识定位主表固定为 `bacon_upms_user_identity`
- 租户读取主表固定为 `bacon_upms_tenant`
- 菜单树查询主表固定为 `bacon_upms_menu + bacon_upms_role_menu_rel + bacon_upms_user_role_rel`
- 权限码查询主表固定为 `bacon_upms_menu + bacon_upms_resource + 关联关系表`
- 数据权限查询主表固定为 `bacon_upms_role + bacon_upms_data_permission_rule + bacon_upms_role_data_scope_rel + bacon_upms_user_role_rel`
- 审计查询主表固定为 `bacon_upms_audit_log`
- 访问日志查询主表固定为 `bacon_upms_sys_log`
- 高频读模型由 `Mapper` 查询组装，不新增冗余快照表

## 11. Cache Mapping Rules

- `upms:menu-tree:{tenantId}:{userId}` 来源于 `bacon_upms_menu`、`bacon_upms_role_menu_rel`、`bacon_upms_user_role_rel`
- `upms:perm-codes:{tenantId}:{userId}` 来源于 `bacon_upms_menu`、`bacon_upms_resource`、`bacon_upms_role_menu_rel`、`bacon_upms_role_resource_rel`、`bacon_upms_user_role_rel`
- `upms:data-scope:{tenantId}:{userId}` 来源于 `bacon_upms_role`、`bacon_upms_data_permission_rule`、`bacon_upms_role_data_scope_rel`、`bacon_upms_user_role_rel`

## 12. Open Items

无
