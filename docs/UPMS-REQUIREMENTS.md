# UPMS REQUIREMENTS

## 1. Purpose

UPMS 是 Bacon 的统一用户、租户、组织、岗位、角色、菜单、资源、权限业务域。  
本文档是后续设计、任务拆解、实现和测试的唯一基线。  
当前范围内全部功能属于同一交付范围，不做分期交付。

## 2. Scope

### 2.1 In Scope

- 用户管理
- 租户管理
- 部门管理
- 岗位管理
- 角色管理
- 菜单管理
- 资源权限管理
- 用户-角色授权
- 角色-菜单授权
- 角色-资源授权
- 数据权限管理
- 用户菜单树查询
- 用户权限码查询
- 用户数据权限上下文查询
- 用户启用/停用
- 用户导入/导出
- 审计日志
- 租户隔离

### 2.2 Out Of Scope

- SSO
- 除 `GitHub OAuth2` 外的其他社交登录
- 审批流授权
- ABAC 规则引擎
- C 端账号体系

## 3. Bounded Context

### 3.1 Auth

- `Auth` 负责登录、令牌签发、令牌刷新、登出和认证流程
- `Auth` 负责用户本人修改密码
- `Auth` 负责会话校验和会话失效
- `Auth` 负责向第三方应用提供标准 `OAuth2` 授权能力
- `Auth` 不拥有 `UserIdentity` 主数据
- `Auth` 认证成功后必须通过 `UserIdentity` 定位唯一 `User`
- `token payload` 只保存身份信息，不保存权限数据

### 3.2 UPMS

- `UPMS` 负责 `User`、`UserIdentity`、`Tenant`、`Department`、`Post`、`Role`、`Menu`、`Resource`
- `UPMS` 负责授权关系、数据权限、只读查询能力
- `UPMS` 负责密码数据存储
- `UPMS` 负责管理员初始化密码、重置密码、修改密码
- `UPMS` 负责用户状态、租户状态、授权结果和数据权限结果

### 3.3 Cross-Domain Rule

- 其他业务域只能依赖 `bacon-upms-api`
- 其他业务域不得依赖 UPMS 内部实现
- `UPMS` 不负责会话存储和令牌签发
- `UPMS` 触发的用户停用、用户删除、管理员初始化密码、管理员重置密码、管理员修改密码、租户停用，必须通过 `bacon-auth-api` 暴露的 `SessionCommandFacade` 触发认证态即时失效
- 单体模式使用本地 `Facade` 实现
- 微服务模式使用远程 `Facade` 实现，并保持同一契约

## 4. Module Mapping

### 4.1 `bacon-upms-api`

- 跨域 `Facade`
- `DTO`
- 对外共享枚举
- 日志查询类 `Facade` 仅在其他业务域需要跨域读取日志时引入

固定接口：

- `UserReadFacade`
- `DepartmentReadFacade`
- `RoleReadFacade`
- `PermissionReadFacade`

`PermissionReadFacade` 固定方法：

- `getUserMenuTree(tenantId, userId)`，返回固定 `UserMenuTreeDTO`
- `getUserPermissionCodes(tenantId, userId)`，返回 `Set<String>`
- `getUserDataScope(tenantId, userId)`，返回固定 `UserDataScopeDTO`

`UserReadFacade` 固定方法：

- `getUserById(tenantId, userId)`，返回固定 `UserDTO`
- `getUserIdentity(tenantId, identityType, identityValue)`，返回固定 `UserIdentityDTO`
- `getTenantByTenantId(tenantId)`，返回固定 `TenantDTO`

`UserDTO` 至少包含：

- `id`
- `tenantId`
- `account`
- `name`
- `phone`
- `departmentId`
- `status`
- `deleted`

`UserIdentityDTO` 至少包含：

- `id`
- `tenantId`
- `userId`
- `identityType`
- `identityValue`
- `enabled`

`TenantDTO` 至少包含：

- `id`
- `tenantId`
- `code`
- `name`
- `status`

`DepartmentReadFacade` 固定方法：

- `getDepartmentById(tenantId, departmentId)`，返回固定 `DepartmentDTO`
- `getDepartmentByCode(tenantId, departmentCode)`，返回固定 `DepartmentDTO`
- `listDepartmentsByIds(tenantId, departmentIds)`，返回 `List<DepartmentDTO>`

`DepartmentDTO` 至少包含：

- `id`
- `tenantId`
- `code`
- `name`
- `parentId`
- `leaderUserId`
- `status`

`RoleReadFacade` 固定方法：

- `getRoleById(tenantId, roleId)`，返回固定 `RoleDTO`
- `getRolesByUserId(tenantId, userId)`，返回 `List<RoleDTO>`

`RoleDTO` 至少包含：

- `id`
- `tenantId`
- `code`
- `name`
- `roleType`
- `dataScopeType`
- `status`

`UserDataScopeDTO` 至少包含：

- `allAccess`
- `scopeTypes`
- `departmentIds`

`UserMenuTreeDTO` 至少包含：

- `id`
- `name`
- `menuType`
- `parentId`
- `routePath`
- `componentName`
- `icon`
- `sort`
- `children`

### 4.2 `bacon-upms-interfaces`

- `Controller`
- 请求 `DTO`
- 响应 `VO`
- `Assembler`
- 对外适配端点
- 系统访问日志查询接口与日志展示 `VO`
- 仅 `Controller` 方法标注 `@SysLog`，不直接处理日志采集细节

### 4.3 `bacon-upms-application`

固定服务：

- `UserApplicationService`
- `DepartmentApplicationService`
- `RoleApplicationService`
- `MenuApplicationService`
- `PermissionQueryService`
- `SysLogConsumeApplicationService`

日志职责：

- 消费 `common-log` 发送到 MQ 的系统访问日志消息
- 将日志传输 `DTO` 转换为 `UPMS` 域内日志实体
- 编排“写数据库 + 写文件”双写动作
- 不向其他业务域暴露日志写入接口

### 4.4 `bacon-upms-domain`

- 聚合、实体、值对象
- 领域服务
- `Repository` 接口
- 领域规则和不变量
- 系统访问日志实体归属 `UPMS`
- 日志持久化仓储接口归属 `UPMS`

### 4.5 `bacon-upms-infra`

- `MyBatis-Plus Mapper`
- `Repository` 实现
- 缓存实现
- 远程适配器
- 权限查询持久化支持
- MQ 消费者实现
- 系统访问日志数据库落库实现
- 系统访问日志文件落盘实现

### 4.6 System Access Log Rule

- 其他业务模块开发者只接触 `bacon-common-log` 中的 `@SysLog`
- `@SysLog` 只能标注在 `Controller` 类或方法上
- 访问日志采集由 `common-log` 切面统一完成
- 切面统一提取请求路径、方法、租户、操作人、耗时、结果等上下文
- 访问日志先发送到 MQ，不在业务 `Controller` 内同步写库
- `UPMS` 负责消费系统访问日志消息
- `UPMS` 消费后必须同时写数据库表和文件日志
- 系统访问日志与业务审计日志分离建模，不能复用 `UpmsAuditLog`

## 5. Core Domain Objects

- `User`
- `UserIdentity`
- `Tenant`
- `Department`
- `Post`
- `Role`
- `Menu`
- `Resource`
- `UserRoleRelation`
- `RoleMenuRelation`
- `RoleResourceRelation`
- `DataPermissionRule`
- `RoleDataScopeRelation`
- `SysLogRecord`

## 5.1 Fixed Enums

- `identityType` 固定为 `ACCOUNT`、`PHONE`、`WECOM`、`GITHUB`
- `roleType` 固定为 `SYSTEM_ROLE`、`TENANT_ROLE`、`CUSTOM_ROLE`
- `dataScopeType` 固定为 `ALL`、`DEPARTMENT`、`DEPARTMENT_AND_CHILDREN`、`SELF`、`CUSTOM`
- `menuType` 固定为 `DIRECTORY`、`MENU`、`BUTTON`
- `permissionTargetType` 固定为 `MENU`、`BUTTON`、`API_RESOURCE`
- `resourceType` 固定为 `API`、`RPC`、`EVENT`

## 5.2 Terminology

- `User` 是统一用户主体
- `UserIdentity` 是登录标识，不等于用户主体
- `Tenant` 是一级隔离边界
- `permission code` 同时适用于 `Menu` 和 `Resource`
- `菜单树` 仅指前端可消费的 `DIRECTORY/MENU` 树，不包含 `BUTTON`
- `数据权限上下文` 仅指 `allAccess`、`scopeTypes`、`departmentIds`

## 5.3 Fixed Fields

- `User` 至少包含 `id`、`tenantId`、`account`、`name`、`phone`、`departmentId`、`status`、`deleted`
- `UserIdentity` 至少包含 `id`、`tenantId`、`userId`、`identityType`、`identityValue`、`enabled`
- `Tenant` 至少包含 `id`、`tenantId`、`code`、`name`、`status`
- `Department` 至少包含 `id`、`tenantId`、`code`、`name`、`parentId`、`leaderUserId`、`status`
- `Post` 至少包含 `id`、`tenantId`、`code`、`name`、`sort`、`status`
- `Role` 至少包含 `id`、`tenantId`、`code`、`name`、`roleType`、`dataScopeType`、`status`、`builtIn`
- `Menu` 至少包含 `id`、`tenantId`、`menuType`、`name`、`parentId`、`routePath`、`componentName`、`icon`、`sort`、`visible`、`status`、`permissionCode`、`builtIn`
- `Resource` 至少包含 `id`、`tenantId`、`code`、`name`、`resourceType`、`module`、`path`、`method`、`status`、`permissionCode`、`builtIn`
- `UserRoleRelation` 至少包含 `id`、`tenantId`、`userId`、`roleId`
- `RoleMenuRelation` 至少包含 `id`、`tenantId`、`roleId`、`menuId`
- `RoleResourceRelation` 至少包含 `id`、`tenantId`、`roleId`、`resourceId`
- `DataPermissionRule` 至少包含 `id`、`tenantId`、`roleId`、`dataScopeType`
- `RoleDataScopeRelation` 至少包含 `id`、`tenantId`、`roleId`、`departmentId`

## 5.4 Uniqueness And Index Rules

- `User.id` 全局唯一
- `User.account` 全局唯一
- `UserIdentity.id` 全局唯一
- `UserIdentity(identityType, identityValue)` 全局唯一
- `Tenant.id` 全局唯一
- `Tenant.code` 全局唯一
- `Department.id` 全局唯一
- `Department.code` 全局唯一
- `Post.id` 全局唯一
- `Post.code` 全局唯一
- `Role.id` 全局唯一
- `Role.code` 全局唯一
- `Menu.id` 全局唯一
- `Menu.permissionCode` 全局唯一
- `Resource.id` 全局唯一
- `Resource.code` 全局唯一
- `Resource(path, method)` 全局唯一
- `UserRoleRelation(tenantId, userId, roleId)` 联合唯一
- `RoleMenuRelation(tenantId, roleId, menuId)` 联合唯一
- `RoleResourceRelation(tenantId, roleId, resourceId)` 联合唯一
- `RoleDataScopeRelation(tenantId, roleId, departmentId)` 联合唯一
- `User` 必须建立 `(tenantId, departmentId, status)` 索引
- `Department` 必须建立 `(tenantId, parentId, status)` 索引
- `Role` 必须建立 `(tenantId, roleType, status)` 索引
- `Menu` 必须建立 `(tenantId, parentId, status, visible)` 索引
- `Resource` 必须建立 `(tenantId, resourceType, status)` 索引
- `UserRoleRelation` 必须建立 `(tenantId, userId)`、`(tenantId, roleId)` 索引

## 6. Global Constraints

### 6.1 Status Rule

- `User`、`Tenant`、`Department`、`Post`、`Role`、`Menu`、`Resource` 的状态字段统一使用 `status`
- `status` 固定值为 `ENABLED`、`DISABLED`
- `UserIdentity` 单独使用 `enabled` 布尔字段，不使用 `status`
- `status = DISABLED` 的对象不得继续产生新的业务生效结果
- `User.status = DISABLED` 时，该用户不得继续登录
- `Tenant.status = DISABLED` 时，该租户下用户不得继续登录
- `Department.status = DISABLED` 时，不得再绑定新的 `User`
- `Post.status = DISABLED` 时，不得再绑定新的 `User`
- `Role.status = DISABLED` 时，该角色不再参与权限汇总
- `Menu.status = DISABLED` 时，该菜单不进入菜单树和权限结果
- `Resource.status = DISABLED` 时，该资源不进入权限结果

### 6.2 Tenant And Identity

- `Tenant` 是一级隔离边界
- 核心实体 `id` 全局唯一
- 主数据业务编码全局唯一
- `tenantId` 用于数据隔离，不用于主键唯一性
- `User`、`UserIdentity`、`Department`、`Post`、`Role`、`Menu`、`Resource`、授权关系、数据权限规则必须带 `tenantId`
- 普通用户和租户管理员受 `tenantId` 限制
- 平台超级管理员可跨租户
- 平台内置数据与租户自定义数据必须可区分

### 6.3 User And UserIdentity

- 登录标识使用 `UserIdentity` 独立建模
- 一个 `User` 可绑定多个 `UserIdentity`
- `UserIdentity` 归属 `UPMS`，不归属 `Auth`
- 登录方式包括：账号密码、手机短信、企业微信扫码、`GitHub OAuth2`
- `UserIdentity` 必须包含 `identityType`、`identityValue`、`userId`、`tenantId`、`enabled`
- `UserIdentity.enabled = false` 时，该登录方式不可用于认证
- 禁用 `UserIdentity` 不等于禁用 `User`
- 每个 `User` 至少保留一种可用登录方式：`ACCOUNT`

### 6.4 Password

- 密码数据归 `UPMS`
- 用户本人修改密码归 `Auth`
- 管理员初始化密码、重置密码、修改密码归 `UPMS`
- 密码不得明文存储
- 密码哈希算法固定为 `BCrypt`
- `BCrypt cost factor` 固定为 `12`
- `salt` 由 `BCrypt` 自动生成并内嵌到哈希结果，不单独存储
- 密码字段只保存 `BCrypt hash`
- 禁止记录明文密码、哈希值、临时密码到日志、审计日志、消息体、缓存
- 密码长度至少 `8` 位
- 密码必须同时包含大写字母、小写字母、数字
- 密码可以包含特殊字符
- 管理员不得查询用户当前密码
- 用户本人修改密码时必须校验旧密码
- 管理员初始化密码和重置密码后，用户首次登录必须修改密码
- 管理员重置密码时必须生成随机临时密码
- 随机临时密码只展示一次
- `UPMS` 完成管理员密码管理操作后，必须调用 `SessionCommandFacade` 触发相关用户会话立即失效

### 6.5 Session Invalidation

- 用户停用后，当前登录状态立即失效
- 用户删除后，当前登录状态立即失效，且已删除用户不得再次登录
- 管理员修改密码后，用户当前登录状态立即失效
- 管理员初始化密码后，用户当前登录状态立即失效
- 管理员重置密码后，用户当前登录状态立即失效
- `Tenant` 停用后，该租户下全部用户当前登录状态立即失效
- `Tenant` 停用后，该租户下全部用户不得继续登录
- 会话失效后，用户必须重新登录
- `UPMS` 只负责触发失效，不负责会话存储和令牌回收实现

### 6.6 Role, Menu, Resource, Data Scope

- 用户与岗位关系是一对多
- 内置 `Role` 不可删除
- 数据权限按 `Role` 配置，不直接配置给 `User`
- 数据权限先按 `tenantId` 收口，再计算范围
- `CUSTOM` 当前只支持 `DEPARTMENT`
- `RoleDataScopeRelation` 只用于 `CUSTOM`
- `DIRECTORY/MENU` 支持 `visible`
- `BUTTON` 不进入可见菜单树
- `Menu` 和 `Resource` 必须区分平台内置和租户自定义
- `Menu` 和 `Resource` 必须包含 `builtIn` 或等价字段
- 租户只能修改本租户自定义 `Menu` 和 `Resource`

### 6.7 Permission Code

- `Menu permission code` 和 `Resource permission code` 使用统一命名体系
- `permission code` 格式固定为 `domain:model:action`
- `permission code` 必须全小写
- 多单词 `action` 使用 `-` 连接
- 不使用空格、下划线、驼峰
- 不直接使用 HTTP 方法作为 `action`
- 权限编码全局唯一

推荐动作：

- `view`
- `list`
- `create`
- `update`
- `delete`
- `enable`
- `disable`
- `assign`
- `reset-password`
- `import`
- `export`

示例：

- `upms:user:view`
- `upms:user:list`
- `upms:user:create`
- `upms:user:update`
- `upms:user:delete`
- `upms:user:reset-password`
- `upms:role:assign`

### 6.8 Delete Strategy

- 核心主数据统一采用逻辑删除
- 已删除数据默认不展示

## 7. Functional Requirements

### 7.1 User

- 新增 `User`
- 修改 `User` 基础信息
- 启用/停用 `User`
- 逻辑删除 `User`
- 管理员初始化密码
- 管理员重置密码
- 管理员修改密码
- 按账号、姓名、手机号、部门、状态分页查询
- 查询已分配 `Role`

补充约束：

- `User` 删除策略固定为逻辑删除
- 已删除 `User` 默认不展示

### 7.2 UserIdentity

- 维护登录标识与 `User` 的绑定关系
- 支持通过登录标识定位 `User`
- 支持一个 `User` 绑定多个 `UserIdentity`

补充约束：

- `UserIdentity.enabled = false` 时，该登录方式不可用于认证
- 每个 `User` 至少保留一种可用登录方式：`ACCOUNT`

### 7.3 Tenant

- 新增/修改/启用/停用 `Tenant`
- 查询 `Tenant` 列表和详情
- 区分平台级数据和租户级数据

补充约束：

- `Tenant` 停用后，必须通过 `SessionCommandFacade` 触发该租户下全部登录态失效

### 7.4 Department

- 维护 `Department` 树
- 新增/修改/停用 `Department`
- 维护 `Department.code`
- 设置部门负责人
- 查询部门树和详情

补充约束：

- 不允许父子循环
- 用户绑定的 `Department` 必须存在且处于启用状态

### 7.5 Post

- 新增/修改/停用/排序 `Post`
- 一个 `User` 绑定一个或多个 `Post`

补充约束：

- `Post.sort` 越小排序越靠前

### 7.6 Role

- 新增/修改/停用/删除 `Role`
- 维护 `Role.code`、名称、描述、数据范围类型、状态
- 为 `Role` 绑定 `Menu`、`Resource`、`User`
- 查询 `Role` 授权对象

补充约束：

- 内置 `Role` 不可删除

### 7.7 Menu

- 维护 `Menu` 树
- 支持目录、菜单、按钮三种节点
- `DIRECTORY/MENU` 维护树结构、路由、组件标识、图标、排序、显隐、状态
- `BUTTON` 作为非导航权限节点存在
- 按 `Role` 授权 `Menu`
- 按 `User` 查询可访问菜单树

补充约束：

- 不允许菜单树循环
- 已停用 `Menu` 不返回
- 可见菜单树只返回 `visible = true` 的 `DIRECTORY/MENU`
- 隐藏节点仍可提供有效 `permission code`
- 无权限按钮的 `permission code` 不出现在查询结果中
- 权限校验只依赖 `permission code`，不依赖是否显示

### 7.8 Resource

- 维护 `Resource.code`、名称、类型、模块、路径、方法
- 按 `Role` 授权 `Resource`
- 按 `User` 汇总 `Resource permission code`
- 支持后端 API 鉴权

补充约束：

- `path + method` 冲突必须校验

### 7.9 Authorization Relations

- 一个 `User` 可分配多个 `Role`
- 一个 `Role` 可分配多个 `Menu`
- 一个 `Role` 可分配多个 `Resource`
- 授权变更后刷新或失效缓存

补充约束：

- 授权操作必须幂等
- 重复授权不能产生脏数据

### 7.10 Data Scope

- 每个 `Role` 绑定一个 `DataScopeType`
- `CUSTOM` 通过 `RoleDataScopeRelation` 维护范围明细
- 按 `User` 汇总有效数据权限范围
- 为其他业务域提供只读数据权限上下文

补充约束：

- 用户最终数据权限是全部已分配 `Role` 数据范围的并集
- `ALL` 表示当前租户内全量可见

### 7.11 Read Capability

- 查询 `User` 基础信息
- 查询 `User` 菜单树
- 查询 `User` 权限码
- 查询 `User` 数据权限上下文
- 为其他业务域提供只读的 `Department`、`Role` 等查询能力

补充约束：

- 跨域接口只读
- `DTO` 契约必须稳定
- `PermissionReadFacade` 必须返回稳定的菜单树、权限码集合、数据权限上下文
- 前端登录成功后，菜单树、权限码、数据权限上下文由 `UPMS` 只读接口提供，不由 `Auth` 登录响应直接返回

### 7.12 Cache

缓存命名约定：

- `upms:menu-tree:*` 只保存前端菜单树结果
- `upms:perm-codes:*` 只保存权限码集合
- `upms:data-scope:*` 只保存数据权限上下文

缓存键：

- `upms:menu-tree:{tenantId}:{userId}`
- `upms:perm-codes:{tenantId}:{userId}`
- `upms:data-scope:{tenantId}:{userId}`

缓存内容：

- 菜单树缓存保存前端可直接消费的树形结果，只包含 `visible = true` 的 `DIRECTORY/MENU`
- 权限码缓存保存用户全部有效 `permission code`
- 数据权限缓存保存用户有效 `DataScopeType`、有效部门范围和 `allAccess`

失效触发：

- `User-Role` 关系变更
- `Role` 启用/停用
- `Role` 删除
- `Role-Menu` 关系变更
- `Role-Resource` 关系变更
- 数据权限规则变更
- `Tenant` 启用/停用
- 用户停用
- 用户删除
- 用户部门变更
- 用户岗位变更
- `Menu` 状态、显隐、层级变更
- `Resource` 状态、编码变更
- 管理员初始化密码
- 管理员重置密码
- 管理员修改密码

补充约束：

- 缓存以主动失效为主，`TTL` 为兜底
- 菜单树缓存、权限码缓存、数据权限缓存不得混合存储
- `BUTTON` 不进入菜单树缓存
- `Tenant` 停用时，必须失效该租户下全部 `menu-tree`、`perm-codes`、`data-scope` 缓存

### 7.13 Audit Log

- 对核心主数据变更记录审计日志
- 对授权关系变更记录审计日志
- 对密码管理操作记录审计日志
- 对租户管理操作记录审计日志

必须记录的对象：

- `User`
- `Tenant`
- `Department`
- `Post`
- `Role`
- `Menu`
- `Resource`
- `UserRoleRelation`
- `RoleMenuRelation`
- `RoleResourceRelation`
- `DataPermissionRule`
- 密码管理操作

必须记录的字段：

- `tenantId`
- `operatorId`
- 操作时间
- 对象类型
- 对象 `id`
- 操作类型
- 变更前摘要
- 变更后摘要
- 请求来源
- 结果状态

补充约束：

- 审计日志必须持久化存储
- 审计日志必须可按 `tenantId`、对象类型、对象 `id`、操作人、时间范围查询
- 密码相关审计日志不得记录明文密码、哈希值、临时密码
- 审计日志中的变更摘要必须脱敏敏感字段
- 审计日志不得因业务查询接口调用而产生
- 审计日志写入失败不得影响主业务提交结果

### 7.14 User Import And Export

- 仅支持 `User` 导入
- 仅支持 `User` 导出
- 导入策略固定为新增
- 导入文件格式固定为 `xlsx`
- 导出文件格式固定为 `xlsx`
- 必须提供导入模板
- 导入必须支持结果回执

补充约束：

- 不支持更新、不支持覆盖、不支持合并
- 已存在的 `User.account` 导入时必须失败
- 导出范围受 `tenantId`、权限和数据范围约束
- 导入和导出操作必须记录审计日志
- 导入结果回执必须包含成功数、失败数、失败行、失败原因
- 导入模板至少包含 `account`、`name`、`phone`、`departmentCode`、`postCodes`、`status`
- 导入模板不包含 `roleCodes`
- 导出字段固定至少包含 `account`、`name`、`phone`、`departmentCode`、`postCodes`、`status`
- 导出结果按 `account` 升序排序
- 导出手机号必须脱敏，至少保留前 `3` 位和后 `4` 位

## 8. Key Flows

### 8.1 User Creation

1. 创建 `User` 基础信息
2. 校验唯一性
3. 保存 `UserIdentity` 绑定关系
4. 保存 `User` 与组织关系
5. 分配 `Role`
6. 用户登录后获得有效权限

### 8.2 Role Authorization

1. 创建或选择 `Role`
2. 分配 `Menu` 和 `Resource`
3. 分配 `User`
4. 刷新授权缓存
5. 后续查询返回最新权限结果

### 8.3 Post-Login Authorization Assembly

1. `Auth` 完成认证
2. `Auth` 从 UPMS 查询用户状态、菜单树、权限码、数据权限上下文
3. `Auth` 将身份信息写入 `token payload`
4. 前端通过 `UPMS` 只读接口获取菜单树、权限码、数据权限上下文
5. 前端按菜单树渲染导航
6. 网关或业务服务按权限码鉴权

### 8.4 Password Change

1. 管理员通过 UPMS 发起初始化密码、重置密码或修改密码
2. UPMS 持久化新的密码数据
3. UPMS 通过 `SessionCommandFacade` 触发该用户当前登录状态失效
4. 用户后续访问受保护资源时认证失败
5. 用户必须重新登录

### 8.5 User Disable Or Delete

1. 管理员通过 UPMS 停用或删除 `User`
2. UPMS 持久化用户状态变更
3. UPMS 通过 `SessionCommandFacade` 触发该用户当前登录状态失效
4. UPMS 删除该用户相关权限缓存
5. 用户后续访问受保护资源时认证失败
6. 已停用用户必须重新登录；已删除用户不得再次登录

## 9. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-001 | Security | 所有写接口必须鉴权 |
| NFR-002 | Consistency | 授权变更在缓存刷新或失效后，必须立即对后续查询生效 |
| NFR-003 | Performance | 高频查询必须优先走缓存 |
| NFR-004 | Architecture | 必须同时支持单体和微服务装配 |
| NFR-005 | Maintainability | 实现必须严格遵守 `interfaces -> application -> domain -> infra` 分层 |
| NFR-006 | Auditability | 审计日志必须持久化、可检索、可追溯 |
| NFR-007 | Availability | 核心查询不能成为登录瓶颈 |
| NFR-008 | Compatibility | `Facade + DTO` 契约必须同时支持本地和远程实现 |
| NFR-009 | Cache | 缓存键必须包含 `tenantId` 和 `userId` |
| NFR-010 | Cache Consistency | 影响授权结果的变更必须主动失效相关缓存 |
| NFR-011 | Password Security | 密码必须使用 `BCrypt` 哈希，禁止明文存储和明文传播 |

## 10. Open Items

- 无
