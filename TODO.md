## TODO List

### 当前主线顺序（按模块执行）

1. `upms`
   - 再回收 `UserRepositoryImpl` 中残留业务
   - 最后处理 `upms api.dto` 下沉与 facade `Request/Response` 规约
2. `storage`
   - 先统一 `objectId -> storedObjectNo`
   - 再清理 `StoredObjectPageQueryDTO` 和 interfaces/application 合同
3. `auth`
   - 先收口 `interfaces -> application` 合同
   - 再评审 `auth-api` 中哪些 DTO 真正属于稳定跨域契约
4. `order`
   - 先收口 DTO 装配到 assembler
   - 再补 provider / application / domain 关键回归测试
5. `payment` / `inventory`
   - `payment` 先收口 controller 路由语义
   - 然后把 `inventory` 固化成接口层校验模板域
6. 横切收尾
   - 最后统一横切注解策略、租户边界与 ArchUnit 增量规则
   - 这些规则以“前面主线稳定后再加门禁”为原则，避免先上门禁把治理动作卡死

### P0 - 2026-04-17 跨域扫描新增（统一性优先）

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

- [ ] 统一横切注解策略（`@SysLog/@HasPermission/@Operation`）
  - 现状对比：`upms` 注解密度远高于其他域；`auth` 基本无权限与审计注解
  - 处理动作：定义“后台管理接口必备注解矩阵”，区分 BFF/回调/provider 的最小集合
  - 验收点：各域注解策略可解释、可检查，不再出现风格割裂
  - 重要度：7/10

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

### P0 - `upms` 先拆大类

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
  - 当前状态：`StoredObjectPageQueryDTO` 仍滞留在 `application.dto`，且仓内已无实际调用，属于待清理旧模型
  - 处理动作：删除未使用的 `StoredObjectPageQueryDTO`，如仍需保留分页查询契约，则改为 `application.query.StoredObjectPageQuery`
  - 验收点：`rg -n "StoredObjectPageQueryDTO" bacon-biz` 结果为空；查询对象命名与 inventory/order 新规一致
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

- [ ] `upms-api`：把修改密码类输入从 `UserPasswordChangeDTO` 改到 `api.request`
  - 影响范围：`UserPasswordFacadeRemoteImpl`、provider/controller
  - 验收点：写操作入参不再落在 `api.dto`
  - 重要度：8/10

- [ ] `upms-api`：逐个 facade 补 `FacadeRequest` / `FacadeResponse` 命名规约
  - 目标接口：`DepartmentReadFacade`、`RoleReadFacade`、`UserReadFacade`、`PermissionReadFacade`、`UserPasswordFacade`
  - 前置关系：优先完成上面的 domain 依赖清理，再统一命名
  - 验收点：upms facade 契约和 inventory/payment/order 新规一致
  - 重要度：9/10

### P1 - Repository 命名统一治理清单

- [ ] `upms`：评审剩余 Repository 是否继续压短，先不直接批量改名
  - 现状对比：关系型仓储已拆出 `UserRoleRepository`、`RoleMenuRepository`、`RoleResourceRepository`、`RoleDataScopeRepository`；剩余待评审的主要是 `PermissionRepository.listMenuTreeByUserId`、`UserRoleRepository.updateRoleIds`、`SysLogRepository.insertToDatabase/insertToFile`、`DepartmentRepository.listTree`
  - 处理动作：逐个判断哪些是必要条件信息、哪些仍然带过重业务前缀；输出结论后再改名，不做无结论扫改
  - 验收点：形成一份 `建议改 / 建议保留 / 暂缓` 结论，且结论基于现有拆分后的仓储版图
  - 重要度：6/10

### P2 - DTO 装配位置统一

### P2 - 测试覆盖对齐

- [ ] 对齐五域测试深度（重点补 `auth/upms` 的复杂流程用例）
  - 当前对比：`inventory` 测试数量与场景深度领先；`order-domain` 已补最小但关键的领域单测闭环；`auth/upms` 在复杂路径（鉴权、导入、密码、回调异常分支）覆盖仍偏薄
  - 处理动作：后续重点转到 `auth/upms`，按“成功路径 + 参数非法 + 状态冲突 + 资源不存在”补最小闭环测试集
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

### P2 - ArchUnit 可落地增强

- [ ] 增加 ArchUnit 规则：`interfaces.controller/provider` 方法签名只使用协议模型
  - 现状：当前已限制层间依赖方向，但尚未门禁 controller/provider 方法入参和返回值的稳定性
  - 处理动作：限制 `interfaces.controller/provider` 公共方法入参只允许 `interfaces.request`、基础 Web 注解参数与稳定基础类型；返回值只允许 `interfaces.response`、统一包装或 `void`
  - 验收点：controller/provider 不再直接暴露 `domain entity`、`DO`、`FacadeResponse`、跨域 `DTO`
  - 重要度：9/10

- [ ] 增加 ArchUnit 规则：`RepositoryImpl` 必须且只实现本域 `Repository`
  - 现状：当前已落地 `RepositoryImpl` 不得依赖其他 `RepositoryImpl`、不得依赖其他聚合 `PersistenceSupport` 的硬规则，但尚未门禁实现关系本身
  - 处理动作：在现有硬规则基础上，继续限制 `*RepositoryImpl` 必须实现一个且仅一个本域 `domain.repository.*Repository`，并禁止实现外域 `Repository`
  - 验收点：仓储实现和领域仓储契约一一对应，避免“工具类伪装成 RepositoryImpl”或“一类多仓储”漂移
  - 重要度：9/10

- [ ] 增加 ArchUnit 规则：`interfaces` 依赖收敛为白名单
  - 现状：当前重点禁止依赖 `infra` 和他域 `application/infra`，但对 `domain.repository`、外域散包、历史遗留协议模型仍缺少白名单约束
  - 处理动作：限制 `interfaces` 只允许依赖本域 `application`、本域 `domain.model`、本域 `interfaces`、外域 `api.facade` 与 `bacon-common`
  - 验收点：接口层依赖面可预测，不再顺手引用 repository、mapper、外域内部对象
  - 重要度：9/10

- [ ] 增加 ArchUnit 规则：`application` 跨域调用只允许依赖外域 `api.facade`
  - 现状：当前已禁止依赖外域 `infra`，但对外域 `api.dto`、其他散包的使用边界还不够明确
  - 处理动作：限制 `application` 对外域依赖只允许落在 `api.facade`；如需跨域模型转换，固定收敛到指定 assembler/adapter 包
  - 验收点：跨域编排统一经由 facade 入口，避免外域契约对象渗透进业务流程主体
  - 重要度：8/10

- [ ] 增加 ArchUnit 规则：协议模型分层隔离
  - 现状：application 公共方法已禁止直接使用协议模型，但 `domain/infra/api` 对 `interfaces.request/response`、`api.dto` 的反向依赖仍可继续硬化
  - 处理动作：限制 `domain` 不得依赖 `interfaces.request/response`、`api.dto`；限制 `infra` 不得依赖 `interfaces.request/response`；限制 `api.facade` 不得使用 `interfaces.*` 模型
  - 验收点：协议对象停留在各自边界层，防止模型职责串层
  - 重要度：8/10

- [ ] 增加 ArchUnit 规则：注解使用位置白名单扩展
  - 现状：当前已限制 `@Transactional` 与 `@SysLog` 的位置，但 `@RestController`、`@FeignClient`、`@Mapper`、MyBatis-Plus 注解尚未统一门禁
  - 处理动作：限制 `@RestController` 仅出现在 `interfaces.controller/provider`，`@FeignClient` 仅出现在 `infra.facade.remote`，`@Mapper` 仅出现在 `infra.persistence.mapper`，`@TableName/@TableField` 仅出现在 `infra.persistence.dataobject`
  - 验收点：技术注解位置与目录职责一致，异常用法可直接阻断
  - 重要度：8/10

- [ ] 增加 ArchUnit 规则：`application/infra` 禁止新增 `IllegalArgumentException` 作为业务异常出口
  - 现状：架构文档已明确异常约定，TODO 里也已有治理项，但尚未形成自动门禁
  - 处理动作：扫描 `application` 与 `infra.repository.impl/support` 的 `throw new IllegalArgumentException(...)` 和等价调用，要求改为稳定业务异常
  - 验收点：业务链路异常语义稳定，`IllegalArgumentException` 不再成为默认兜底
  - 重要度：8/10

- [ ] 增加 ArchUnit 规则：`application.assembler` 独占 DTO/Response 装配职责
  - 现状：当前仅限制 `ApplicationService` 不得本地 `toDto` 或直接 `new api.dto`，还可以继续扩大到 `interfaces.response` 与跨域 DTO 装配
  - 处理动作：限制 `application.command/query/audit` 不得直接构造 `api.dto`、`interfaces.response`，DTO/Response 装配统一收敛到 `application.assembler`
  - 验收点：应用服务只保留编排逻辑，模型装配职责有唯一归属
  - 重要度：7/10

- [ ] 增加 ArchUnit 规则：目录反向命名校验
  - 现状：当前大多是“某后缀应该放在哪个目录”，但还缺少“某目录下的类必须使用该后缀”的反向门禁
  - 处理动作：限制 `interfaces.controller` 目录下类必须以 `Controller` 结尾，`domain.repository` 下接口必须以 `Repository` 结尾，`infra.persistence.mapper` 下类必须以 `Mapper` 结尾，其他关键目录同理
  - 验收点：目录语义和类名语义双向一致，存量偏差更容易被发现
  - 重要度：7/10

### P3 - 持续治理

- [ ] 评估 `@SysLog` 的统一策略
  - 需要决策：仅后台管理域保留，还是其他核心域也补齐
  - 重要度：4/10

### 建议执行顺序（细化）

1. 先走 `upms` 主线：`UserController` 拆分 -> `UserApplicationService` 拆分 -> `UserRepositoryImpl` 业务外提
2. 再走 `storage` 主线：`objectId -> storedObjectNo` -> 删除 `StoredObjectPageQueryDTO` -> 收口 interfaces/application 合同
3. 然后处理 `auth`：收口应用层合同 -> 评审 `auth-api dto`
4. 再处理 `order`：provider 读写拆分 -> assembler 收口 -> 用例补齐
5. 然后处理 `payment` 与 `inventory` 的局部整形任务
6. 最后再上 ArchUnit 增量门禁、注解矩阵和租户边界统一
