## TODO List

### 当前主线顺序（按模块执行）

1. `auth`
   - 先收口 `interfaces -> application` 合同
   - 再评审 `auth-api` 中哪些 DTO 真正属于稳定跨域契约
2. `upms`
   - 再回收 `UserRepositoryImpl` 中残留业务
3. `storage`
   - 先统一 `objectId -> storedObjectNo`
   - 再清理 `StoredObjectPageQueryDTO` 和 interfaces/application 合同
4. `order`
   - 先收口 provider/interfaces/application 合同
   - 再补 provider / application / domain 关键回归测试
5. `payment` / `inventory`
   - `payment` 先收口 controller 路由语义
   - 然后把 `inventory` 固化成接口层校验模板域
6. 横切收尾
   - 最后统一横切注解策略、租户边界与剩余 ArchUnit 增量规则
   - 保持“先业务收口、后门禁加严”的节奏，避免治理反向阻塞主线

### P0 - 2026-04-17 跨域扫描新增（统一性优先）

- [ ] `auth-interfaces/application`：收口 OAuth2 合同为 Command/Query/VO
  - 范围对象：`OAuth2Controller`、`OAuth2AuthorizationApplicationService`、`OAuth2ClientApplicationService`
  - 处理动作：授权、客户端查询/更新入口统一为 `OAuth2*Command|Query`
  - 验收点：OAuth2 application 公共方法不再暴露多 primitive 入参
  - 重要度：9/10

- [ ] `auth-interfaces/application`：收口 Session/Password 合同为 Command/Query/VO
  - 范围对象：`AuthController`、`AuthProviderController`、`TokenApplicationService`、密码相关 application service
  - 处理动作：会话校验、刷新、失效、密码修改/重置改为 `Session*Command|Query`、`Password*Command`
  - 验收点：会话与密码链路 application 公共方法签名可预测且一致
  - 重要度：9/10

- [ ] `storage-interfaces/application`：收口 StoredObject 合同为 Command/Query/VO
  - 范围对象：`StorageController`、`StorageProviderController`、`StoredObjectApplicationService`、`StoredObjectQueryApplicationService`
  - 处理动作：对象创建、分页、状态变更、引用绑定/解绑入口统一为 `StoredObject*Command|Query`
  - 验收点：storage application 公共方法不再新增多 primitive 入参
  - 重要度：9/10

- [ ] `payment-interfaces/application`：收口 Payment 查询/回调合同为 Command/Query/VO
  - 范围对象：`PaymentQueryController`、`PaymentCallbackController`、`PaymentCreateApplicationService`、`PaymentCallbackApplicationService`、`PaymentAuditQueryApplicationService`
  - 处理动作：支付创建、回调处理、查询与审计查询统一为 `Payment*Command|Query`
  - 验收点：payment application 公共方法签名统一，不再按参数堆叠扩展
  - 重要度：8/10

- [ ] 统一横切注解策略（`@SysLog/@HasPermission/@Operation`）
  - 现状对比：`upms` 注解密度远高于其他域；`auth` 基本无权限与审计注解
  - 处理动作：定义“后台管理接口必备注解矩阵”，区分 BFF/回调/provider 的最小集合
  - 验收点：各域注解策略可解释、可检查，不再出现风格割裂
  - 重要度：7/10

### P0 - 五域风格/手法/功能对齐（inventory/payment/order/storage/upms）

- [ ] `inventory-interfaces`：对齐 controller 校验注解门禁（`@Validated/@Valid/@HasPermission`）
  - 范围对象：`InventoryReservationController`、`InventoryAuditLogController`、`InventoryAuditCompensationController` 及对应 `interfaces.request.*`
  - 处理动作：新增 ArchUnit 规则，强制上述 controller 保持 `@Validated`（类/方法）与权限注解位置一致，请求对象参数使用 `@Valid`
  - 验收点：inventory 入口不再出现“裸参数 + 无校验注解”回退
  - 重要度：8/10

- [ ] `upms/payment/order/storage/auth-interfaces`：补齐 controller 参数校验门禁
  - 范围对象：各域 `interfaces.controller` 与 `interfaces.provider` 公共入口方法
  - 处理动作：新增 ArchUnit 规则，限制 request 对象参数必须显式 `@Valid`，禁止 controller 公共方法出现未约束裸对象参数
  - 验收点：跨域入口参数约束粒度与 inventory 对齐，由门禁统一阻断
  - 重要度：8/10

- [ ] `*-interfaces/request`：收敛请求模型放置与校验注解门禁
  - 范围对象：`interfaces.request..` 包下请求模型
  - 处理动作：新增 ArchUnit 规则，限制请求模型仅放在 `interfaces.request`，并对关键字段缺失 Bean Validation 注解的类进行阻断
  - 验收点：请求模型职责清晰，校验不再依赖人工约定
  - 重要度：7/10

- [ ] `payment`：收口 `/payment` 根路径下的查询/审计/回调分组语义
  - 当前状态：`PaymentQueryController`、`PaymentAuditLogController` 共用 `/payment`，`callback` 独立为 `/payment/callbacks`
  - 处理动作：统一资源名与动作语义（按支付单、审计日志、回调）并同步 OpenAPI tag 命名
  - 验收点：支付域路由和 controller 命名一一对应，便于 AI 稳定路由推断
  - 重要度：6/10

### P1 - `upms` 仓储职责回收

- [ ] 把 `UserRepositoryImpl` 中的密码策略装配从 repository impl 提升到 application 或独立 domain service
  - 目标类：默认密码、密码过期时间、失败上限、needChangePassword 规则
  - 验收点：密码规则不再写死在 infra
  - 重要度：8/10

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

### P2 - ArchUnit 可落地增强

- [ ] 增加 ArchUnit 规则：`interfaces.controller/provider` 方法签名只使用协议模型
  - 现状：当前已限制层间依赖方向，但尚未门禁 controller/provider 方法入参和返回值的稳定性
  - 处理动作：限制 `interfaces.controller/provider` 公共方法入参只允许 `interfaces.request`、基础 Web 注解参数与稳定基础类型；返回值只允许 `interfaces.response`、统一包装或 `void`
  - 验收点：controller/provider 不再直接暴露 `domain entity`、`DO`、`FacadeResponse`、跨域 `DTO`
  - 重要度：9/10

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

1. 先走 `auth` 主线：收口 `interfaces -> application` 合同 -> 评审并收口 `auth-api dto`
2. 再走 `upms` 主线：`UserRepositoryImpl` 业务外提
3. 然后处理 `storage`：`objectId -> storedObjectNo` -> 删除 `StoredObjectPageQueryDTO` -> 收口 interfaces/application 合同
4. 再处理 `order`：provider/interfaces/application 合同收口 -> assembler 收口 -> 用例补齐
5. 然后处理 `payment` 与 `inventory` 的局部整形任务
6. 最后处理横切：注解矩阵、租户边界、剩余 ArchUnit 门禁统一
