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

## P0 - 生产级代码与业务功能补齐

- [ ] `auth` / `upms` / `order` / `inventory` / `payment` / `storage`：补齐错误码分段与业务异常映射
  - 范围对象：各域 `*DomainException`、`*ErrorCode`、应用层通用异常抛出点
  - 处理动作：按 `AUTH`、`UPMS`、`ORDER`、`INVENTORY`、`PAYMENT`、`STORAGE` 分段定义稳定错误码；将已稳定的业务错误从通用字符串异常迁移到域错误码
  - 验收点：业务异常不暴露底层异常信息，各域核心参数错误、资源不存在、业务冲突和领域规则错误有稳定错误码与测试覆盖
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

- [ ] `docs/00-governance/how-to`：起草 `HOW-TO-START-A-NEW-BUSINESS-DOMAIN.md`
  - 范围对象：从“是否应该新增业务域”到“确认进入实现前”这一整段前置判断
  - 文档必须覆盖：
    - 如何判断这是新业务域、现有业务域扩展，还是纯治理/部署问题
    - 如何识别主数据归属、主流程归属、跨域依赖和范围外能力
    - 如何判断是否必须新建 `10-requirements/*-REQUIREMENTS.md`
    - 如何判断是否需要新增 `api.facade`、是否需要 mono/micro 装配变更
    - 需求未固定时不得进入实现的停止条件
  - 处理动作：按 `HOW-TO-HOW-TO.md` 起草适用场景、不适用场景、前置判断问题、最小收敛步骤、落文档入口、常见错误、验证和提交要求
  - 验收点：新人和 AI 能按文档先完成新业务域讨论、边界收敛和实现前置判断，不在需求未固定时直接搭模块写代码
  - 重要度：9/10

- [ ] `docs/00-governance/how-to`：起草 `HOW-TO-WRITE-OR-REFINE-REQUIREMENTS.md`
  - 范围对象：新增或收敛 `10-requirements/*-REQUIREMENTS.md`，把需求写到可直接指导实现
  - 文档必须覆盖：
    - 何时新建需求文档，何时只在既有文档中补章节
    - 如何固定 `Purpose/Scope/Bounded Context/Global Constraints/Key Flows/Open Items`
    - 如何写主数据、主流程、跨域调用、失败补偿、幂等、缓存、权限、审计等关键口径
    - 如何避免把关键实现约束散落到其他需求文档
    - 需求文档达到“可进入实现”的完成标准
  - 处理动作：按 `HOW-TO-HOW-TO.md` 起草适用场景、前置判断、章节编写顺序、常见遗漏、验证和提交要求
  - 验收点：新人和 AI 能按文档把需求收敛到稳定、确定、可执行的状态，不遗留关键规则空白
  - 重要度：9/10

- [ ] `docs/00-governance/how-to`：起草 `HOW-TO-DISTINGUISH-APPLICATION-AND-DOMAIN.md`
  - 范围对象：`application` 与 `domain` 的职责边界判断，不拆成多篇子 HOW-TO
  - 文档必须覆盖：
    - 什么属于用例编排、事务、幂等、跨域协调、状态推进顺序
    - 什么属于业务不变量、聚合内一致性、状态合法性、对象创建/变更规则
    - 什么信号说明逻辑放错层了
    - 如何在不改变外部行为的前提下把逻辑从 `application` 下沉到 `domain` 或反向上提
    - 如何用测试验证边界是否收敛正确
  - 处理动作：按 `HOW-TO-HOW-TO.md` 起草核心判断、层内/层外示例、决策清单、常见错误、重构信号、验证和提交要求
  - 验收点：新人和 AI 能按文档判断业务规则到底放 `application` 还是 `domain`，减少“大 ApplicationService” 与“Domain 空壳化”
  - 重要度：9/10

- [ ] `docs/00-governance/how-to`：起草 `HOW-TO-ADD-CONTROLLER-ENDPOINT.md`
  - 范围对象：外部 `Controller` 新增一个端点的最小闭环
  - 文档必须覆盖：
    - 适用前提：需求已经固定、业务域已经明确、是否需要先补需求文档
    - 从 `interfaces.request/response`、`InterfaceAssembler`、`ApplicationService` 到测试与提交的最小路径
    - 哪些场景只改 Controller，哪些场景会自然牵动 `application/domain/repository`
    - 哪些模型不得出现在 Controller 签名里
    - 常见错误：直接返回 `DTO/DO/domain model`、绕过 assembler、在 Controller 拼业务规则
  - 处理动作：按 `HOW-TO-HOW-TO.md` 起草适用场景、前置判断、最小步骤、文件类型、常见错误、验证和提交要求
  - 验收点：新人和 AI 能按文档完成一个标准 Controller 入口新增，并知道何时停止在接口层、何时继续进入后续闭环
  - 重要度：8/10

- [ ] `docs/00-governance/how-to`：起草 `HOW-TO-ADD-APPLICATION-TO-INFRA-FLOW.md`
  - 范围对象：从 `application` 到 `domain.repository` 再到 `infra` 的实现主链路，合并查询/命令/持久化的核心落地路径
  - 文档必须覆盖：
    - 查询与命令各自的最小闭环，但不拆成两篇独立 HOW-TO
    - `ApplicationService`、`domain.repository`、`RepositoryImpl`、`Mapper`、`DO`、`PersistenceAssembler` 的职责与衔接
    - 何时需要 `application.assembler`，何时不需要
    - 何时需要新增领域对象行为，何时只补仓储查询
    - 常见错误：`application` 直接拼持久化模型、`infra` 承载业务流程、`domain` 感知技术细节
  - 处理动作：按 `HOW-TO-HOW-TO.md` 起草适用场景、前置判断、步骤顺序、文件类型、常见错误、验证和提交要求
  - 验收点：新人和 AI 能按文档完成一条标准应用到持久化主链路的新增，不把查询、命令、仓储和持久化职责写散
  - 重要度：8/10

- [ ] `docs/00-governance/how-to`：起草 `HOW-TO-ADD-CROSS-DOMAIN-FACADE.md`
  - 范围对象：跨域 `api.facade` 契约、本地适配、远程适配和调用侧接入的完整闭环
  - 文档必须覆盖：
    - 何时真的需要新增 `Facade`，何时只是本域能力扩展
    - `api.facade/request/response`、`interfaces.facade.*LocalImpl`、`infra.facade.remote.*RemoteImpl` 各自职责
    - 调用方如何只依赖稳定契约，不依赖对方实现
    - 新增 Facade 后通常需要同步哪些测试、文档和装配
    - 常见错误：直接调对方 `application/infra`、用错 request/response 命名、把本地/远程语义混在一起
  - 处理动作：按 `HOW-TO-HOW-TO.md` 起草适用场景、前置判断、最小步骤、文件类型、常见错误、验证和提交要求
  - 验收点：新人和 AI 能按文档完成一个标准跨域 Facade 新增，不直接依赖对方域实现
  - 重要度：8/10

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
8. 横切治理：限流防重放 -> 安全基线
