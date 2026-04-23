# TODO List

## 说明

- 生产升级路线图已拆解合并到本清单，原独立路线图文档已删除。
- 同类能力只保留一个任务项，路线图任务与代码/业务任务合并表达，避免重复待办。

## 当前主线顺序（按模块执行）

1. 质量门禁
   - 补齐覆盖率、静态分析、依赖漏洞扫描和 CI 阻断
2. 生产级代码与业务能力补齐
   - 统一错误码与错误响应
   - 补齐订单、支付、库存状态机、幂等、补偿和对账
   - 补齐契约测试、集成测试、限流、防重放、日志上下文和业务指标
3. 观测告警、部署发布与安全运行
   - 补齐健康检查、Prometheus 指标、Dashboard、Alert Rules
   - 补齐部署模板、环境变量清单、Release Checklist、Runbook
4. 横切收尾
   - 增加剩余 ArchUnit 规则并逐步加严
   - 保持“先接口收口、后门禁加严”的节奏，避免治理反向阻塞主线

## P0 - 五域风格/手法/功能对齐（inventory/payment/order/storage/upms）

## P0 - 生产级代码与业务功能补齐

- [ ] `common-web` / `common-core`：统一错误码与错误响应模型
  - 范围对象：`BadRequestException`、`NotFoundException`、`ConflictException`、各域 `*DomainException`、全局异常处理
  - 处理动作：固定错误响应字段 `code`、`message`、`requestId`、`timestamp`；按 `AUTH`、`UPMS`、`ORDER`、`INVENTORY`、`PAYMENT`、`STORAGE` 分段定义错误码
  - 验收点：Controller 异常响应结构统一，业务异常不暴露底层异常信息，测试覆盖参数错误、资源不存在、业务冲突和领域规则错误
  - 重要度：10/10

- [ ] `order` / `inventory` / `payment`：补齐主链路状态机治理
  - 范围对象：`OrderStatus`、`PayStatus`、`InventoryStatus`、支付单状态、库存预占/扣减/释放状态
  - 处理动作：固定合法状态迁移表；在 domain 层集中表达非法迁移阻断；补齐状态流转测试
  - 验收点：非法状态迁移有明确异常，订单、支付、库存终态不能被普通接口反向改写
  - 重要度：10/10

- [ ] `order` / `inventory` / `payment`：补齐幂等、重试、补偿和死信回放闭环
  - 范围对象：下单、库存预占、库存扣减、库存释放、支付创建、支付回调、订单状态推进、outbox dead letter
  - 处理动作：固定幂等键生成规则、重复请求返回语义、重试触发条件、补偿动作和死信人工回放入口
  - 验收点：重复消息、乱序消息、重试耗尽、补偿成功、补偿失败均有自动化测试覆盖
  - 重要度：10/10

- [ ] `order` / `inventory` / `payment`：增加订单-库存-支付对账能力
  - 范围对象：订单主状态、支付事实、库存事实、跨域快照和审计记录
  - 处理动作：新增对账查询与差异识别能力；固定差异类型、处理状态和人工确认入口
  - 验收点：能识别订单已支付但库存未扣减、支付成功但订单未标记、库存释放失败等主链路不一致场景
  - 重要度：9/10

- [ ] `auth` / `upms`：补齐会话与权限闭环
  - 范围对象：登录态、刷新 token、退出、踢下线、用户停用、租户停用、角色资源授权
  - 处理动作：固定会话失效规则；补齐管理员改密、用户停用、租户停用后的 token 失效行为；补齐权限变更后的缓存失效规则
  - 验收点：用户、租户、角色、资源变更后权限判断不读取过期授权，核心场景有应用层测试
  - 重要度：9/10

- [ ] `storage`：补齐文件上传安全校验
  - 范围对象：普通上传、分片上传、对象查询、对象删除
  - 处理动作：固定文件大小、扩展名、MIME、对象归属、租户隔离和下载权限校验规则
  - 验收点：非法类型、超限文件、跨租户访问、非归属对象删除均被阻断并有测试覆盖
  - 重要度：8/10

- [ ] `common-web` / `common-security`：补齐限流与防重放基础能力
  - 范围对象：关键写接口、支付回调、内部 Provider、管理端高风险接口
  - 处理动作：固定按接口、租户、用户、IP、业务动作的限流维度；固定请求签名、时间戳、`nonce` 校验与过期规则
  - 验收点：重复 `nonce`、过期时间戳、签名错误和限流触发均返回统一错误码
  - 重要度：8/10

## P1 - 测试与质量补齐

- [ ] `pom.xml` / `.github/workflows`：补齐质量门禁流水线
  - 范围对象：根工程、各模块 POM、PR 流水线、主干流水线
  - 处理动作：接入 `JaCoCo` 覆盖率统计；固定 `SpotBugs` 或 `PMD` 静态分析；接入依赖漏洞扫描；将检查结果纳入 PR 阻断
  - 验收点：CI 能产出覆盖率、静态分析和依赖漏洞报告，核心模块初始覆盖率门槛写入流水线
  - 重要度：10/10

- [ ] `order` / `inventory` / `payment`：补齐主链路集成测试
  - 范围对象：订单创建、库存预占、支付创建、支付回调、库存扣减、订单终态推进
  - 处理动作：使用 Testcontainers 或现有测试替身建立可重复执行的端到端测试；覆盖成功、幂等命中、重试接管和终态失败；将测试纳入 CI
  - 验收点：主链路集成测试进入 CI，失败时能定位到订单、库存或支付具体阶段，幂等、重试、补偿规则不会被回退
  - 重要度：10/10

- [ ] `payment-api` / `payment-interfaces.facade` / `payment-infra.facade.remote`：补齐 Payment 跨域 Facade 契约测试
  - 范围对象：`PaymentReadFacade`、`PaymentCommandFacade`、Payment Provider Controller、Payment Facade request/response
  - 处理动作：固定支付创建、支付关闭、支付详情读取和按订单读取支付单的本地调用语义、远程调用语义、异常映射和空值语义
  - 验收点：Payment mono 模式与 micro 模式 Facade 行为一致，Payment 契约变更会被测试阻断
  - 重要度：9/10

- [ ] `storage-api` / `storage-interfaces.facade` / `storage-infra.facade.remote`：补齐 Storage 跨域 Facade 契约测试
  - 范围对象：`StoredObjectCommandFacade`、`StoredObjectReadFacade`、Storage Provider Controller、Storage Facade request/response
  - 处理动作：固定对象上传、分片上传初始化、分片上传、分片完成、分片终止、对象查询、对象引用标记、引用清理和对象删除的本地调用语义、远程调用语义、异常映射和空值语义
  - 验收点：Storage mono 模式与 micro 模式 Facade 行为一致，Storage 契约变更会被测试阻断
  - 重要度：9/10

- [ ] `*-interfaces.controller`：补齐 Controller API 回归测试
  - 范围对象：外部 HTTP 接口入参、出参、权限、异常响应
  - 处理动作：覆盖 Bean Validation、权限不足、资源不存在、业务冲突和成功响应
  - 验收点：接口层只暴露 `*Request` / `*Response`，异常响应结构和错误码稳定
  - 重要度：8/10

- [ ] `*-application` / `*-domain`：补齐应用层与领域行为测试
  - 范围对象：应用服务编排、事务边界、领域实体行为、领域服务
  - 处理动作：应用层覆盖编排顺序和异常路径；领域层覆盖不变量、状态迁移和非法操作
  - 验收点：业务行为变更必须伴随测试更新，领域规则不依赖 Controller 测试间接兜底
  - 重要度：8/10

## P1 - 观测告警与部署发布补齐

- [ ] `common-core` / `common-web` / `bacon-app`：统一日志上下文、健康检查和指标暴露
  - 范围对象：`traceId`、`requestId`、`tenantId`、`userId`、应用健康、版本信息、构建信息、Prometheus 指标
  - 处理动作：固定日志上下文字段；为应用实例暴露健康、版本、构建信息和 Prometheus 指标；为下单成功率、支付成功率、库存扣减成功率、上传成功率增加计数器和耗时指标
  - 验收点：主链路日志可按 `requestId` 串联，核心业务指标可按域和动作聚合，实例健康异常能被独立识别
  - 重要度：9/10

- [ ] `order` / `inventory` / `payment` / `deploy`：补齐主链路 Dashboard 与 Alert Rules
  - 范围对象：订单 outbox、库存审计重试、支付回调、错误率、重试耗尽、outbox 堆积、实例健康
  - 处理动作：固定关键业务指标命名规范；产出可导入的 `Production Dashboard`；产出可执行的 `Alert Rules`；在 `deploy` 中沉淀告警配置样例
  - 验收点：主链路关键动作能看到成功、失败和重试结果，运行人员可通过 dashboard 与日志定位故障范围
  - 重要度：9/10

- [ ] `bacon-app/*-starter` / `bacon-app/bacon-mono-boot` / `deploy`：补齐部署模板、环境变量和回滚步骤
  - 范围对象：各 starter、mono boot、部署样例、运行参数、依赖服务、健康检查
  - 处理动作：为各启动模块补齐部署模板、环境变量清单、健康检查说明和常见回滚步骤；所有新增运行参数进入 `application.yml` 或部署模板
  - 验收点：部署模板可以支撑新环境最小化启动，回滚步骤可独立执行且不依赖口头说明
  - 重要度：8/10

- [ ] `docs` / `.github/workflows`：建立 Release Checklist 与发布验收流
  - 范围对象：数据库、配置、依赖服务、指标、告警、安全、回滚准备、CI 检查
  - 处理动作：编写发布前 `Release Checklist`；固定每周或每批交付的验收顺序：本地检查、CI 检查、文档补齐、产物纳入 `deploy` 或 `docs`、主链路回归
  - 验收点：发布清单可逐项核验，质量门禁失败能阻断发布或合并
  - 重要度：8/10

## P1 - ArchUnit 可落地增强

- [ ] 增加 ArchUnit 规则：应用层、接口层公开契约稳定性校验
  - 现状：已有分层、命名、注解和路径规则，仍需继续收紧公开方法签名、异常模型和跨域契约变更口径
  - 处理动作：限制 `application` 公开方法不得接收协议层模型，限制 Controller/Provider 公开方法异常响应归一，限制 Facade request/response 变更必须伴随契约测试
  - 验收点：公开契约变更能被 ArchUnit 或契约测试阻断，减少接口和应用层模型漂移
  - 重要度：9/10

## P2 - 持续治理增强

- [ ] `docs` / `common-security` / `bacon-app`：补齐安全基线规则
  - 范围对象：敏感配置、内部接口令牌、密钥来源、敏感字段日志、脱敏约束、依赖升级
  - 处理动作：固定敏感配置和密钥来源规则；补充敏感字段日志禁止项与脱敏约束；补充依赖升级和安全告警处理流程
  - 验收点：安全基线进入 `docs`，发布前检查项覆盖配置、密钥、日志脱敏和依赖风险
  - 重要度：8/10

- [ ] `docs`：补齐主链路 Runbook 与常见故障处置手册
  - 范围对象：订单创建、库存预占、支付创建、支付回调、订单状态流转、outbox 堆积、重试耗尽、实例健康异常
  - 处理动作：编写主链路运行 `Runbook`；编写常见故障场景处置手册；形成 `Production Readiness Checklist`
  - 验收点：值班人员可根据 Runbook 独立处理常见故障，发布前检查覆盖数据库、配置、指标、告警、安全和回滚准备
  - 重要度：8/10

- [ ] `docs/AGENT.md`：索引 TODO 执行入口
  - 范围对象：`docs/AGENT.md`、`TODO.md`
  - 处理动作：在 AI 路由文档中固定生产升级、发布准备、质量门禁相关任务读取 `TODO.md`
  - 验收点：后续 AI 任务能按入口找到 TODO，不需要默认全量加载 `docs`
  - 重要度：6/10

- [ ] 增加 ArchUnit 规则：目录反向命名校验
  - 现状：当前大多是“某后缀应该放在哪个目录”，但还缺少“某目录下的类必须使用该后缀”的反向门禁；该规则容易诱导 AI 机械改名，执行前必须逐项确认收益
  - 处理动作：限制 `interfaces.controller` 目录下类必须以 `Controller` 结尾，`domain.repository` 下接口必须以 `Repository` 结尾，`infra.persistence.mapper` 下类必须以 `Mapper` 结尾，其他关键目录同理
  - 验收点：目录语义和类名语义双向一致，存量偏差更容易被发现
  - 重要度：7/10

- [ ] 评估 `@SysLog` 的统一策略
  - 需要决策：仅后台管理域保留，还是其他核心域也补齐
  - 重要度：4/10

## 待讨论项

- [ ] 静态分析方案固定为 `SpotBugs` 还是 `PMD`
  - 关联任务：`pom.xml` / `.github/workflows`：补齐质量门禁流水线
  - 决策要求：固定一种方案后再写入 POM、CI 和质量门禁说明
  - 重要度：8/10

- [ ] Dashboard 与 Alert Rules 的导出格式
  - 关联任务：`order` / `inventory` / `payment` / `deploy`：补齐主链路 Dashboard 与 Alert Rules
  - 决策要求：固定 dashboard 和告警规则的文件格式、目录位置和导入方式
  - 重要度：7/10

## 建议执行顺序（细化）

1. 质量门禁：覆盖率 -> 静态分析 -> 依赖漏洞扫描
2. `common-web` / `common-core`：统一错误码和错误响应
3. `order` / `inventory` / `payment`：固定状态机 -> 幂等补偿 -> 对账
4. `auth` / `upms` / `storage`：补齐会话权限闭环和上传安全校验
5. 测试补齐：主链路集成测试 -> Facade 契约测试 -> Controller 回归测试
6. 观测告警：日志上下文 -> 指标暴露 -> Dashboard -> Alert Rules
7. 发布运行：部署模板 -> Release Checklist -> 回滚步骤 -> Runbook
8. 横切治理：限流防重放 -> 安全基线 -> `@SysLog` 策略评估
