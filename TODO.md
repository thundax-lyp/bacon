## TODO List

### P0 - 前置问题整理（由原差异分析转 TODO）

- [ ] 固化治理范围：统一按 `inventory / payment / order / storage / upms` 五个域推进
  - 验收点：后续治理任务默认标注所属域，不再出现“跨域但无归属”条目
  - 重要度：6/10

- [ ] 统一 `controller` 命名与根路径规则并输出可执行规范
  - 当前差异：`PaymentQueryController`（用例型）与 `InventoryController/UserController`（聚合型）并存，根路径存在 `/payment`、`/order`、`/storage/objects`、`/inventory/inventories`、`/upms/users`
  - 验收点：新接口命名与路径有唯一规则，能支持稳定的 ArchUnit / lint 检查
  - 重要度：8/10

- [ ] 统一分页与过滤入口风格
  - 当前差异：`@ModelAttribute *PageRequest`、`@PathVariable + @RequestParam`、`/page` 子路径策略并存
  - 验收点：同类查询接口入参模式统一，OpenAPI 展示与校验位置统一
  - 重要度：7/10

- [ ] 统一 `interfaces -> application` 输入边界
  - 当前差异：`inventory/order` 边界较清晰，`storage/payment` primitive 入参偏多，`upms` 处于混合态
  - 验收点：application 合同风格一致，VO 与协议边界不再继续漂移
  - 重要度：8/10

- [ ] 统一 DTO 装配位置，收敛到 assembler
  - 当前差异：`order/upms` 仍有 application service 内手写 DTO 映射
  - 验收点：application service 以编排为主，不再承担大量 DTO 拼装与协议适配
  - 重要度：8/10

- [ ] 收敛 application service 厚度，优先治理 `upms` 与 `order`
  - 当前差异：`payment/storage` 较薄，`order` 偏厚，`upms` 最复杂
  - 验收点：各域 service 职责颗粒度接近，review 口径可统一
  - 重要度：7/10

- [ ] 统一异常风格，替换分散的 `IllegalArgumentException` 用法
  - 当前差异：`inventory/payment` 偏稳定业务异常，`storage` 偏 `NotFoundException`，`order/upms` 仍有较多 `IllegalArgumentException`
  - 验收点：参数错误、业务冲突、资源不存在、权限错误有明确且一致的异常语义
  - 重要度：7/10

- [ ] 统一租户约束显式性，建立多租户安全基线
  - 当前差异：`inventory` 明确 `requireTenantId`，`order/payment/storage` 显式性不一致，`upms` 平台级与租户级边界复杂
  - 验收点：关键写操作默认显式租户校验，平台能力与租户能力边界清晰
  - 重要度：8/10

- [ ] 统一横切注解策略（`@SysLog` / `@HasPermission` / `@Operation`）
  - 验收点：审计与权限注解策略可落地到各域，避免“只有 upms 密集、其他域稀疏”的不一致
  - 重要度：5/10

- [ ] 固化当前复杂度分层并用作迭代优先级基线
  - 当前分层：第一梯队 `inventory/payment`，第二梯队 `storage`，第三梯队 `order`，最复杂 `upms`
  - 验收点：任务排序默认遵循该基线，后续可按阶段复评更新
  - 重要度：6/10

### P0 - `upms` 先拆大类

- [ ] 拆分 `UserController`：把用户基础信息、密码、角色、头像、导入导出拆成独立 controller
  - 输出物：多个小 controller，原路由语义保持不变或有明确迁移方案
  - 验收点：单个 controller 不再同时承担 8+ 类职责
  - 重要度：9/10

- [ ] 拆分 `UserApplicationService`：先拆出 `UserQueryApplicationService`
  - 范围：`getUserById`、`getUserIdentity`、`getUserLoginCredential`、`pageUsers`、`getRolesByUserId`、`exportUsers`
  - 验收点：查询逻辑不再和写操作混在同一个类里
  - 重要度：9/10

- [ ] 拆分 `UserApplicationService`：再拆出 `UserProfileApplicationService`
  - 范围：`createUser`、`updateUser`、`updateUserStatus`、`deleteUser`
  - 验收点：用户基础资料写操作聚合到单独服务
  - 重要度：8/10

- [ ] 拆分 `UserApplicationService`：再拆出 `UserPasswordApplicationService`
  - 范围：`initPassword`、`resetPassword`、`changePassword`
  - 验收点：密码流程与资料维护彻底分开
  - 重要度：8/10

- [ ] 拆分 `UserApplicationService`：再拆出 `UserAvatarApplicationService`
  - 范围：`getAvatarAccessUrl`、`updateAvatar`
  - 验收点：文件上传与用户资料主流程分离
  - 重要度：7/10

- [ ] 拆分 `UserApplicationService`：再拆出 `UserImportExportApplicationService`
  - 范围：`importUsers`、`exportUsers`
  - 验收点：批量导入导出不再堆在核心用户服务中
  - 重要度：7/10

### P1 - `upms` 仓储职责回收

- [ ] 把 `UserRepositoryImpl` 中的“账号/手机号身份替换流程”从 repository impl 提升到 application 层编排
  - 目标类：`replaceAccountIdentity`、`replacePhoneIdentity` 相关流程
  - 验收点：repository 只保留存取动作，不再主导业务步骤顺序
  - 重要度：8/10

- [ ] 把 `UserRepositoryImpl` 中的密码策略装配从 repository impl 提升到 application 或独立 domain service
  - 目标类：默认密码、密码过期时间、失败上限、needChangePassword 规则
  - 验收点：密码规则不再写死在 infra
  - 重要度：8/10

- [ ] 把 `UserRepositoryImpl` 中角色绑定后的缓存清理从 repository impl 外提
  - 目标：让 repository 返回结果，缓存失效由 application 编排
  - 验收点：infra 不再混入明显横切业务动作
  - 重要度：7/10

### P1 - 各模块 `api.dto` 残留治理清单

- [ ] `storage-api`：把 `StoredObjectPageQueryDTO` 改为 `query/StoredObjectPageQuery`
  - 影响范围：provider/controller/facade
  - 验收点：查询对象命名与 inventory/order 新规一致
  - 重要度：7/10

- [ ] `upms-application`：把通用分页 `PageResultDTO` 下沉到 `application.dto`
  - 影响范围：租户、用户、角色、岗位、资源、日志等分页查询
  - 验收点：分页结果不再作为跨模块通用 `api.dto`
  - 重要度：9/10

- [ ] `upms-application`：按领域拆分并下沉用户/租户/角色/部门/资源/岗位读模型
  - 当前对象：`UserDTO`、`TenantDTO`、`RoleDTO`、`DepartmentDTO`、`DepartmentTreeDTO`、`ResourceDTO`、`PostDTO`
  - 验收点：这些对象只在 upms 内部查询与装配链路流转
  - 重要度：9/10

- [ ] `upms-application`：下沉菜单与权限读模型
  - 当前对象：`MenuTreeDTO`、`UserMenuTreeDTO`、`UserDataScopeDTO`
  - 验收点：权限/菜单内部模型退出 `upms.api.dto`
  - 重要度：8/10

- [ ] `upms-application`：下沉身份与认证辅助模型
  - 当前对象：`UserIdentityDTO`、`UserLoginCredentialDTO`
  - 验收点：身份查询内部模型不再暴露为通用 `api.dto`
  - 重要度：8/10

- [ ] `upms-application`：下沉日志读模型
  - 当前对象：`SysLogDTO`
  - 验收点：审计/系统日志 DTO 不再放在 `upms.api.dto`
  - 重要度：7/10

- [ ] `upms-api`：把修改密码类输入从 `UserPasswordChangeDTO` 改到 `api.request`
  - 影响范围：`UserPasswordFacadeRemoteImpl`、provider/controller
  - 验收点：写操作入参不再落在 `api.dto`
  - 重要度：8/10

- [ ] `upms-api`：逐个 facade 补 `FacadeRequest` / `FacadeResponse` 命名规约
  - 目标接口：`DepartmentReadFacade`、`RoleReadFacade`、`UserReadFacade`、`PermissionReadFacade`、`UserPasswordFacade`
  - 前置关系：优先完成上面的 domain 依赖清理，再统一命名
  - 验收点：upms facade 契约和 inventory/payment/order 新规一致
  - 重要度：9/10

- [ ] 形成统一约束：application 公共入口优先使用 `Command / Query / VO`，禁止新增多 primitive 长参数方法
  - 输出物：规则文档或代码检查项
  - 重要度：6/10

### P2 - DTO 装配位置统一

- [ ] `order`：把 `OrderCreateApplicationService` 中手写 `OrderSummaryDTO` 装配迁移到 assembler
  - 验收点：application service 只保留编排和状态推进
  - 重要度：6/10

- [ ] `order`：继续检查 query application service 是否还存在 response/dto 手写拼装
  - 验收点：详情、分页、snapshot 装配逐步收敛到 assembler
  - 重要度：6/10

- [ ] `upms`：排查 application service 内是否还有历史遗留 DTO 手写映射
  - 验收点：统一回收进 assembler
  - 重要度：5/10

### P2 - controller 与路径规范

- [ ] 定一版 controller 命名规范
  - 需要决策：采用“聚合型 controller”还是“用例型 controller”
  - 验收点：新代码命名不再摇摆
  - 重要度：5/10

- [ ] 定一版根路径规范
  - 需要决策：是否统一复数资源路径、是否统一 `/page`
  - 验收点：后续新增接口按统一规则落地
  - 重要度：5/10

- [ ] 先列出现有不一致路径清单，再决定是否批量重构
  - 范围：`/payment`、`/order`、`/storage/objects`、`/inventory/inventories`、`/upms/users`
  - 重要度：4/10

### P2 - 租户边界统一

- [ ] 梳理五个域 application 层中 `requireTenantId` 的落点
  - 输出物：一张方法级清单
  - 重要度：6/10

- [ ] 明确 `upms` 中平台级能力和租户级能力的边界
  - 输出物：哪些接口必须要求租户，哪些接口允许平台上下文
  - 重要度：6/10

- [ ] 补一条实现规则：跨域写操作默认显式校验租户上下文
  - 重要度：5/10

### P3 - 持续治理

- [ ] 增加 ArchUnit 或同类检查：`api` 不直接依赖 domain
  - 重要度：5/10

- [ ] 增加 ArchUnit 或同类检查：`application` 不直接接收 `interfaces.dto.request/response`
  - 重要度：5/10

- [ ] 增加 ArchUnit 或同类检查：`infra.repository.impl` 不承载明显业务编排
  - 重要度：5/10

- [ ] 评估 `@SysLog` 的统一策略
  - 需要决策：仅后台管理域保留，还是其他核心域也补齐
  - 重要度：4/10

### 建议执行顺序

1. 先做基线盘点与冻结：确认 `api.dto` 引用清单、`requireTenantId` 落点清单、`IllegalArgumentException` 清单，并在本文件勾选基线任务
2. 继续推进 `payment-api` 与 `upms-api` 的 `api.dto` 下沉和 facade `Request/Response` 命名统一，先做“契约薄化”，暂不做大规模业务拆分
3. 收口 `storage` 剩余查询模型命名（`StoredObjectPageQueryDTO` -> `StoredObjectPageQuery`）并补齐相关规则检查
4. 然后继续处理 `order` 的 assembler 收敛，统一 interfaces/application DTO 装配边界
5. 接着拆 `upms` 的 `UserApplicationService`（先 `UserQueryApplicationService`，再 `Profile/Password/Avatar/ImportExport`），最后再拆 `UserController`
6. 在 `upms` 服务拆分稳定后，再把 `UserRepositoryImpl` 中的业务编排上移到 application/domain service，避免“边拆边搬”导致回归复杂度过高
7. 再统一异常风格与租户边界（优先 `upms/order`），并同步补充规则文档，确保新代码不再回退到旧写法
8. 最后落 ArchUnit/规则检查（`api` 依赖、application 入参边界、infra 编排约束）和 `@SysLog` 策略评估，形成持续治理闭环
