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

- [x] `bacon-auth-interfaces/src/main/java/com/github/thundax/bacon/auth/interfaces/controller/AuthController.java`：补齐 auth 接口层 assembler 收口
  - 范围对象：`passwordLogin`、`smsLogin`、`wecomLogin`、`githubLogin`
  - 处理动作：新增 `auth.interfaces.assembler`，把 `PasswordLoginRequest` / `SmsLoginRequest` / `WecomLoginRequest` 与 URL 参数转换统一下沉到 assembler，controller 只保留校验、委派、响应返回
  - 验收点：`AuthController` 不再直接 `new PasswordLoginCommand(...)`，不再在 controller 中拼装登录入参
  - 重要度：8/10

- [ ] `bacon-auth-application/src/main/java/com/github/thundax/bacon/auth/application/command/LoginApplicationService.java`：统一 auth 登录命令合同
  - 范围对象：`loginBySms(String, String)`、`loginByWecom(String)`、`loginByGithub(String)`
  - 处理动作：按 `APPLICATION-REFACTOR` 收口为 `*Command` 入参，去除公开多参数/裸参数登录方法，并同步更新全部调用点
  - 验收点：`LoginApplicationService` 对外公开写方法只接收稳定 `*Command`
  - 重要度：9/10

- [ ] `bacon-order-application/src/main/java/com/github/thundax/bacon/order/application/query/OrderPageQuery.java`：对齐分页查询基类
  - 范围对象：`OrderPageQuery`
  - 处理动作：按统一分页约定改为复用 `common.application.page.PageQuery`，收口页码归一化入口
  - 验收点：order 分页查询合同与 `inventory` / `storage` / `upms` 保持一致，不再单独携带分页归一化职责
  - 重要度：8/10

- [ ] `bacon-order-application/src/main/java/com/github/thundax/bacon/order/application/query/OrderQueryApplicationService.java`：移除 order 查询层重复分页归一化
  - 范围对象：`page(OrderPageQuery query)`
  - 处理动作：在 `OrderPageQuery` 完成统一后，删除 service 内部 `PageParamNormalizer` 手工归一化，直接使用标准分页契约
  - 验收点：`OrderQueryApplicationService` 不再重复处理 `pageNo/pageSize` 归一化
  - 重要度：7/10

- [ ] `bacon-upms-application/src/main/java/com/github/thundax/bacon/upms/application/audit/SysLogQueryApplicationService.java`：收口 syslog 查询合同
  - 范围对象：`page(String, String, String, String, Integer, Integer)`、`getLogById(SysLogId)`
  - 处理动作：新增 `SysLogPageQuery`，将分页查询改为 `page(XxxPageQuery)`；同时按上下文内命名约定收口查询方法名
  - 验收点：syslog 查询服务不再暴露多参数分页方法，查询命名与 `APPLICATION-REFACTOR` 约定一致
  - 重要度：9/10

- [ ] `bacon-upms-application/src/main/java/com/github/thundax/bacon/upms/application/command/UserProfileApplicationService.java`：收口 user profile 命名冗余
  - 范围对象：`createUser`、`updateUser`、`updateUserStatus`
  - 处理动作：按 bounded context 内部命名约定分别收口为 `create`、`update`、`updateStatus`，并同步修正 controller / facade / tests 调用点
  - 验收点：用户资料 command service 不再重复 `User` 上下文词
  - 重要度：8/10

- [ ] `bacon-upms-application/src/main/java/com/github/thundax/bacon/upms/application/query/UserQueryApplicationService.java`：收口 user query 命名冗余
  - 范围对象：`getUserById`、`getTenantByTenantId`
  - 处理动作：按 query service 命名约定分别收口为 `getById`、`getTenantById` 或等价上下文内简名，并同步修正 controller / facade / tests 调用点
  - 验收点：用户查询 service 不再保留 `getXxxByXxx` 冗余命名残留
  - 重要度：8/10

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
