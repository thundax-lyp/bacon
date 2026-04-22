## TODO List

### 当前主线顺序（按模块执行）

1. `payment`
   - 收口 `/payment` 根路径下查询/审计/回调分组语义
   - 同步 OpenAPI tag 与 controller 命名
2. `*-interfaces/request`
   - 收敛请求模型放置与校验注解门禁
3. 横切收尾
   - 增加剩余 ArchUnit 规则并逐步加严
   - 保持“先接口收口、后门禁加严”的节奏，避免治理反向阻塞主线

### P0 - 五域风格/手法/功能对齐（inventory/payment/order/storage/upms）

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

### P1 - ArchUnit 可落地增强

  - 重要度：9/10

### P2 - 持续治理增强

- [ ] 增加 ArchUnit 规则：目录反向命名校验
  - 现状：当前大多是“某后缀应该放在哪个目录”，但还缺少“某目录下的类必须使用该后缀”的反向门禁
  - 处理动作：限制 `interfaces.controller` 目录下类必须以 `Controller` 结尾，`domain.repository` 下接口必须以 `Repository` 结尾，`infra.persistence.mapper` 下类必须以 `Mapper` 结尾，其他关键目录同理
  - 验收点：目录语义和类名语义双向一致，存量偏差更容易被发现
  - 重要度：7/10

- [ ] 评估 `@SysLog` 的统一策略
  - 需要决策：仅后台管理域保留，还是其他核心域也补齐
  - 重要度：4/10

### 建议执行顺序（细化）

1. `payment`：收口 `/payment` 路由语义 -> 对齐 OpenAPI tag 与 controller 命名
2. `*-interfaces/request`：统一请求模型放置与字段校验门禁
3. 横切治理：目录反向命名门禁 -> `@SysLog` 策略评估
