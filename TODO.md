## TODO List

### 当前主线顺序（按模块执行）

1. `storage`
   - 统一 `objectId -> storedObjectNo`
   - 收口 `interfaces/application` 合同
2. `order`
   - 收口 `provider/interfaces/application` 合同
   - 补 `provider/application/domain` 关键回归测试
3. `payment`
   - 收口 `/payment` 根路径下查询/审计/回调分组语义
   - 同步 OpenAPI tag 与 controller 命名
4. `inventory`
   - 对齐 controller 校验注解门禁（`@Validated/@Valid/@HasPermission`）
   - 固化为接口层校验模板域
5. `upms` / `auth`
   - 补齐 `controller/provider` 参数校验门禁
6. 横切收尾
   - 统一租户边界（`requireTenantId` 落点、平台级/租户级边界）
   - 增加剩余 ArchUnit 规则并逐步加严
   - 保持“先业务收口、后门禁加严”的节奏，避免治理反向阻塞主线

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

1. `storage`：`objectId -> storedObjectNo` -> 收口 `interfaces/application` 合同
2. `order`：收口 `provider/interfaces/application` 合同 -> 补关键回归测试
3. `payment`：收口 `/payment` 路由语义 -> 对齐 OpenAPI tag 与 controller 命名
4. `inventory`：补齐 `@Validated/@Valid/@HasPermission` 门禁并固化模板
5. `upms` / `auth`：补齐参数校验门禁并统一横切注解矩阵
6. 横切治理：租户边界清单 -> `interfaces.controller/provider` 协议模型门禁 -> 目录反向命名门禁
