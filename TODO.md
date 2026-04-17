## TODO List

### P0 - 2026-04-17 跨域扫描新增（统一性优先）

- [ ] 统一 controller 校验基线：全域 `@Validated` + `@Valid`
  - 现状对比：`inventory/payment/order/storage` 多数已有 `@Validated`；`upms/auth` controller 基本缺失；`upms` 大量 `@RequestBody` 未加 `@Valid`
  - 处理动作：按域补齐类级 `@Validated`、方法参数 `@Valid`，优先 `upms`（`User/Role/Tenant/Post/Resource/Department`）
  - 验收点：外部 controller 与 provider 入口参数统一在 interfaces 层拦截
  - 重要度：9/10

- [ ] 统一 request Bean Validation 完整度（按域补齐）
  - 现状对比：`inventory/order/payment/auth` 相对完整；`storage` 基本空白；`upms` 35 个 request 中大量无约束
  - 处理动作：分域建立最小约束模板（ID: `@Positive`，编码/名称: `@NotBlank+@Size`，列表: `@NotEmpty`）
  - 验收点：`storage` 的分页/provider 请求和 `upms` 的用户/角色/租户关键写请求均具备基础约束
  - 重要度：9/10

- [ ] 统一 PathVariable / RequestParam 约束显式性
  - 现状：`upms/storage/auth` 仍有大量 `@PathVariable Long/String` 无 `@Positive/@NotBlank`
  - 处理动作：按 controller 清单补齐路径参数约束；字符串 ID/编码补 `@NotBlank + @Size`
  - 验收点：接口层不再把明显非法路径参数下沉到 application
  - 重要度：8/10

- [ ] `interfaces -> application` 合同统一为 Command/Query/VO
  - 现状对比：`inventory/order` 相对稳定；`payment/storage/auth/upms` 仍有较多 primitive 参数方法
  - 处理动作：分域消减长参数方法，优先改 `upms User/Tenant/Role/Post`、`auth OAuth2/Session/Password`、`storage StoredObject*`
  - 验收点：application 公共方法不再新增多 primitive 入参，接口签名可预测
  - 重要度：9/10

- [ ] 统一 DTO 装配职责，禁止 ApplicationService 内“就地拼装”
  - 现状：`upms/order` 仍有 service 层直接 `new DTO` / 拼 response 数据
  - 处理动作：继续下沉到 `application.assembler`，service 仅编排事务与流程
  - 验收点：application service 代码审阅聚焦业务流程而非映射细节
  - 重要度：8/10

- [ ] 统一异常语义，清理 application/infra 的 `IllegalArgumentException`
  - 现状对比：`auth`（OAuth/Session/Password）和 `storage`（upload limit）仍大量抛 `IllegalArgumentException`；`payment` 在 infra support 仍有
  - 处理动作：按“参数错误/不存在/冲突/权限”映射到稳定异常类型，并补单测
  - 验收点：业务链路不再把 `IllegalArgumentException` 作为对外异常出口
  - 重要度：9/10

- [ ] 统一横切注解策略（`@SysLog/@HasPermission/@Operation`）
  - 现状对比：`upms` 注解密度远高于其他域；`auth` 基本无权限与审计注解
  - 处理动作：定义“后台管理接口必备注解矩阵”，区分 BFF/回调/provider 的最小集合
  - 验收点：各域注解策略可解释、可检查，不再出现风格割裂
  - 重要度：7/10

- [ ] 固化五域统一治理看板（inventory/payment/order/storage/upms）
  - 处理动作：每条治理任务必须标记所属域、层级、优先级、验收点
  - 验收点：后续任务默认可分派、可并行、可追踪
  - 重要度：6/10

### P0 - 五域风格/手法/功能对齐（inventory/payment/order/storage/upms）

- [ ] `inventory`：固化为“接口层校验基准域”
  - 当前状态：`@Validated`、`@Valid`、`@HasPermission` 覆盖度高，可作为模板
  - 处理动作：沉淀 inventory controller/request 模板并迁移到脚手架或示例代码
  - 验收点：新接口默认复用同等约束粒度，不再回退到“裸参数”
  - 重要度：7/10

- [ ] `payment`：收口 `/payment` 根路径下的查询/审计/回调分组语义
  - 当前状态：`PaymentQueryController`、`PaymentAuditLogController` 共用 `/payment`，`callback` 独立为 `/payment/callbacks`
  - 处理动作：统一资源名与动作语义（按支付单、审计日志、回调）并同步 OpenAPI tag 命名
  - 验收点：支付域路由和 controller 命名一一对应，便于 AI 稳定路由推断
  - 重要度：6/10

- [ ] `order`：拆分 `OrderReadProviderController` 的读写混合职责
  - 当前状态：类名为 `Read`，但包含 `markPaid/markPaymentFailed/closeExpired` 写操作
  - 处理动作：拆成 `OrderReadProviderController` 与 `OrderCommandProviderController`（或等价命名）
  - 验收点：provider 命名语义与行为一致，AI 不会因命名误导调用写接口
  - 重要度：8/10

- [ ] `storage`：统一 `objectId` 与 `storedObjectNo` 命名语义
  - 当前状态：provider URI 参数名为 `objectId`，实际传递 `storedObjectNo`
  - 处理动作：controller/provider/facade/request 全链路统一为 `storedObjectNo`
  - 验收点：接口契约不再混淆“主键ID”和“业务No”，避免跨域调用误用
  - 重要度：9/10

- [ ] `upms`：补齐 interfaces 层参数校验后再推进服务拆分
  - 当前状态：request 校验与 `@Valid` 缺口最多，且 `UserApplicationService` 复杂度最高
  - 处理动作：先做输入校验收口，再做 `UserApplicationService/UserController` 拆分
  - 验收点：拆分前后行为一致且异常语义稳定，回归风险可控
  - 重要度：9/10

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

- [ ] `storage-application`：把 `StoredObjectPageQueryDTO` 改为 `query/StoredObjectPageQuery`
  - 影响范围：provider/controller/facade
  - 验收点：查询对象命名与 inventory/order 新规一致
  - 重要度：7/10

- [ ] `auth-api`：盘点 `api.dto` 是否属于稳定跨域契约，非契约模型下沉到 application
  - 当前对象：`UserLoginDTO`、`CurrentSessionDTO`、`OAuth2TokenDTO`、`OAuth2IntrospectionDTO`、`OAuth2UserinfoDTO` 等
  - 验收点：auth facade 仅保留跨域必要返回模型，避免 `api.dto` 扩散成应用内部模型
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

### P2 - 测试覆盖对齐

- [ ] 对齐五域测试深度（重点补 `auth/upms` 的复杂流程用例）
  - 当前对比：`inventory` 测试数量与场景深度领先；`auth/upms` 在复杂路径（鉴权、导入、密码、回调异常分支）覆盖偏薄
  - 处理动作：按“成功路径 + 参数非法 + 状态冲突 + 资源不存在”补最小闭环测试集
  - 验收点：五域关键业务链路均具备可回归的正反用例，AI 改动后能快速自检
  - 重要度：7/10

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

- [ ] 增加 ArchUnit 或同类检查：`infra.repository.impl` 不承载明显业务编排
  - 重要度：5/10

- [ ] 评估 `@SysLog` 的统一策略
  - 需要决策：仅后台管理域保留，还是其他核心域也补齐
  - 重要度：4/10

- [ ] 清理迁移遗留空目录：`order/interfaces/dto`
  - 验收点：`interfaces` 目录下不再残留空 `dto` 目录，避免 AI/开发者误判仍在使用旧包
  - 重要度：3/10

### 建议执行顺序

1. 先处理高风险输入面：补齐 `upms/auth` 的 `@Validated`、`@Valid`、PathVariable 约束和 request Bean Validation
2. 再处理命名语义冲突：`storage objectId -> storedObjectNo`、`order ReadProvider` 读写拆分
3. 然后统一 `interfaces -> application` 合同（Command/Query/VO），优先 `upms/auth/storage/payment`
4. 并行推进 `api.dto` 契约薄化（`upms` 优先、`auth` 次之）和 facade `Request/Response` 规约
5. 继续做 DTO 装配收口（`upms/order` application service -> assembler）
6. 再统一异常语义（`auth/storage/payment` 优先清理 `IllegalArgumentException`）
7. 最后收敛横切策略与持续治理（`@SysLog/@HasPermission` 矩阵、ArchUnit 增量规则、空目录清理）
