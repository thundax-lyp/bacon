# UPMS REQUIREMENTS

## 1. 用途

UPMS 是 Bacon 的统一用户、组织、角色、菜单、权限业务域。  
本文档是后续设计、任务拆解、实现和测试的基线。

## 2. 范围

### 2.1 当前范围

- 用户管理
- 部门管理
- 岗位管理
- 角色管理
- 菜单管理
- 资源权限管理
- 用户-角色授权
- 角色-菜单授权
- 角色-资源授权
- 用户菜单树查询
- 用户权限码查询
- 用户启用/停用
- 审计字段
- 数据权限模型
- 租户隔离
- 角色模板
- 导入/导出
- 第三方组织同步

### 2.2 不在范围内

- SSO
- 社交登录
- 审批流授权
- ABAC 规则引擎
- C 端账号体系

## 3. 边界

- `Auth` 负责登录、令牌签发、令牌刷新、登出和认证流程。
- `UPMS` 负责 `User`、`Department`、`Post`、`Role`、`Menu`、`Resource`、授权关系、数据权限和查询能力。
- 其他业务域只能依赖 `bacon-upms-api`。
- 其他业务域不得依赖 UPMS 内部实现。
- 单体模式使用本地 `Facade` 实现。
- 微服务模式使用远程 `Facade` 实现，并保持同一契约。

## 4. 模块映射

### 4.1 `bacon-upms-api`

- 跨域 `Facade` 接口
- `DTO`
- 对外共享枚举

接口：

- `UserReadFacade`
- `DepartmentReadFacade`
- `RoleReadFacade`
- `PermissionReadFacade`

### 4.2 `bacon-upms-interfaces`

- `Controller`
- 请求 `DTO`
- 响应 `VO`
- `Assembler`
- 对外适配端点

### 4.3 `bacon-upms-application`

- 应用服务
- `Command`
- `Query`
- 用例编排

服务：

- `UserApplicationService`
- `DepartmentApplicationService`
- `RoleApplicationService`
- `MenuApplicationService`
- `PermissionQueryService`

### 4.4 `bacon-upms-domain`

- 聚合、实体、值对象
- 领域服务
- `Repository` 接口
- 领域规则和不变量

### 4.5 `bacon-upms-infra`

- `MyBatis-Plus Mapper`
- `Repository` 实现
- 缓存实现
- 远程适配器
- 权限查询持久化支持

## 5. 核心领域对象

- `User`
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

## 6. 功能需求

### 6.1 用户管理

需求：

- 新增 `User`
- 修改 `User` 基础信息
- 重置/初始化密码
- 启用/停用 `User`
- 逻辑删除 `User`
- 按账号、姓名、手机号、部门、状态分页查询
- 查询已分配 `Role`

约束：

- 账号唯一
- 已删除用户不展示
- 已停用用户不能通过有效授权校验

### 6.2 部门管理

需求：

- 维护 `Department` 树
- 新增/修改/停用 `Department`
- 设置部门负责人
- 查询部门树和详情

约束：

- 不允许父子循环
- 用户绑定的 `Department` 必须存在且处于启用状态

### 6.3 岗位管理

需求：

- 新增/修改/停用/排序 `Post`
- 一个 `User` 绑定一个或多个 `Post`

约束：

- `Post.code` 唯一

### 6.4 角色管理

需求：

- 新增/修改/停用/删除 `Role`
- 维护 `Role.code`、名称、描述、数据范围类型、状态
- 为 `Role` 绑定 `Menu`、`Resource`、`User`
- 查询 `Role` 授权对象
- 区分内置 `Role` 和自定义 `Role`

约束：

- `Role.code` 唯一
- 内置 `Role` 不可删除
- 用户有效权限是全部已分配 `Role` 权限的并集

### 6.5 菜单管理

需求：

- 支持目录、菜单、按钮三种 `Menu` 节点类型
- 维护树结构、路由、组件标识、图标、排序、显隐、状态
- 按 `Role` 授权 `Menu`
- 按 `User` 查询可访问菜单树
- 使用统一的 `permission code` 命名体系

约束：

- 不允许菜单树循环
- 已停用 `Menu` 不返回
- 无权限的按钮 `permission code` 不出现在查询结果中
- 必须显式建模 `permission target type`

### 6.6 资源权限管理

需求：

- 维护 `Resource.code`、名称、类型、模块、路径、方法
- 按 `Role` 授权 `Resource`
- 按 `User` 汇总 `Resource permission code`
- 支持后端 API 鉴权
- 使用统一的 `permission code` 命名体系

约束：

- `Resource.code` 全局唯一
- `path + method` 冲突必须校验
- 必须显式建模 `permission target type`

### 6.7 授权关系管理

需求：

- 一个 `User` 可分配多个 `Role`
- 一个 `Role` 可分配多个 `Menu`
- 一个 `Role` 可分配多个 `Resource`
- 授权变更后刷新或失效缓存

约束：

- 授权操作必须幂等
- 重复授权不能产生脏数据

### 6.8 查询能力

需求：

- 查询 `User` 基础信息
- 查询 `User` 菜单树
- 查询 `User` 权限码
- 为其他业务域提供只读的 `Department`、`Role` 等查询能力

约束：

- 跨域接口只读
- `DTO` 契约必须稳定
- 高频查询必须支持缓存优先访问

## 7. 关键流程

### 7.1 用户创建

1. 创建 `User` 基础信息。
2. 校验唯一性。
3. 保存 `User` 与组织关系。
4. 分配 `Role`。
5. 用户登录后获得有效权限。

### 7.2 角色授权

1. 创建或选择 `Role`。
2. 分配 `Menu` 和 `Resource`。
3. 分配 `User`。
4. 刷新授权缓存。
5. 后续查询返回最新权限结果。

### 7.3 登录后权限装配

1. `Auth` 完成认证。
2. `Auth` 从 UPMS 查询用户状态、角色、菜单、权限码。
3. `Auth` 将必要授权数据写入 `token payload` 或缓存。
4. 前端按菜单树渲染导航。
5. 网关或业务服务按权限码鉴权。

## 8. 优先级

| ID | 项目 | 优先级 |
|----|------|--------|
| FR-001 | 用户增删改查与状态管理 | P0 |
| FR-002 | 部门树管理 | P0 |
| FR-003 | 岗位管理 | P1 |
| FR-004 | 角色增删改查与状态管理 | P0 |
| FR-005 | 菜单树管理 | P0 |
| FR-006 | 资源权限管理 | P0 |
| FR-007 | 用户-角色授权 | P0 |
| FR-008 | 角色-菜单授权 | P0 |
| FR-009 | 角色-资源授权 | P0 |
| FR-010 | 用户菜单树查询 | P0 |
| FR-011 | 用户权限码查询 | P0 |
| FR-012 | 数据权限 | P1 |
| FR-013 | 租户隔离 | P1 |
| FR-014 | 审计日志 | P1 |

## 9. 非功能要求

| ID | 类别 | 要求 |
|----|------|------|
| NFR-001 | 安全性 | 所有写接口必须鉴权 |
| NFR-002 | 一致性 | 授权变更在缓存刷新或失效后，必须立即对后续查询生效 |
| NFR-003 | 性能 | 高频查询必须优先走缓存 |
| NFR-004 | 架构 | 必须同时支持单体和微服务装配 |
| NFR-005 | 可维护性 | 实现必须严格遵守 `interfaces -> application -> domain -> infra` 分层 |
| NFR-006 | 可审计性 | 核心变更必须持久化审计字段和操作日志 |
| NFR-007 | 可用性 | 核心查询不能成为登录瓶颈 |
| NFR-008 | 兼容性 | `Facade + DTO` 契约必须同时支持本地和远程实现 |

## 10. 基础规则

- 用户状态：启用、停用、删除
- 菜单类型：目录、菜单、按钮
- 权限目标类型：`menu`、`button`、`api/resource`
- 角色类型：内置、自定义
- 用户与岗位关系：一对多
- 权限编码：`Menu permission code` 和 `Resource permission code` 使用统一命名体系
- 权限聚合：用户权限 = 全部已分配角色权限的并集
- 删除策略：核心主数据统一采用逻辑删除

## 11. 待确认项

- 权限数据写入 `token payload`，还是登录后实时查缓存

## 12. 后续产物

- 领域模型文档
- 表结构文档
- `Facade/API` 契约文档
- 管理后台接口清单
- `Auth-UPMS` 时序图
