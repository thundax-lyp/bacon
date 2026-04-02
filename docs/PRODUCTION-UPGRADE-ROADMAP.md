# PRODUCTION UPGRADE ROADMAP

## 1. Purpose

本文档定义项目从当前工程完成度提升到更高生产就绪度的 4 周升级路线。

本文档固定关注工程治理、测试质量、可观测性、部署交付和安全基线，不重复业务需求文档内容。

## 2. Scope

覆盖范围：

- 根工程 `pom.xml`
- `.github/workflows`
- `docs`
- `deploy`
- `bacon-app/*-starter`
- `bacon-app/bacon-mono-boot`
- `bacon-common/*`
- 涉及主链路的 `bacon-order`、`bacon-inventory`、`bacon-payment`

不在本次路线范围内：

- 新增业务域
- 重做领域模型
- 替换当前 mono/micro 并存装配方案
- 引入新的基础设施产品作为前置条件

## 3. Bounded Context

本路线图只处理“生产就绪度升级”，不处理业务能力扩展。

升级目标固定为以下五类能力：

- 数据变更可版本化
- 质量门禁可量化
- 主链路运行状态可观测
- 部署与回滚可执行
- 安全基线可检查

## 4. Module Mapping

模块分工固定如下：

- `docs`：记录升级策略、发布门槛、运行手册
- `pom.xml` 与各模块 `pom.xml`：接入测试、扫描与质量门禁插件
- `.github/workflows`：固化 PR 与主干分支质量流水线
- `deploy`：沉淀部署样例、环境变量、告警规则和回滚步骤
- `bacon-common`：承载共性的观测、安全、配置与测试支撑
- `bacon-app/*`：暴露健康检查、指标、运行参数和装配验证
- `bacon-order`、`bacon-inventory`、`bacon-payment`：承载跨域主链路联调验证

## 5. Core Domain Objects

### 5.1 Fixed Deliverables

4 周内必须固定产出以下对象：

- `Database Migration Scripts`
- `Coverage Report`
- `Static Analysis Report`
- `Dependency Vulnerability Report`
- `Production Dashboard`
- `Alert Rules`
- `Runbook`
- `Release Checklist`

### 5.2 Terminology

- `Quality Gate`：PR 或主干流水线中的强制检查项
- `Runbook`：值班与故障处理操作手册
- `Release Checklist`：发布前逐项确认清单
- `Main Flow`：订单创建、库存预占、支付创建、支付回调、订单状态流转
- `Production Readiness`：具备版本化变更、可验证质量、可观测运行和可回滚发布的状态

## 6. Global Constraints

- 升级过程不得破坏现有 mono/micro 双装配规则
- 新增质量门禁必须先在 CI 中落地，再写入文档
- 所有新增检查项必须可自动执行，不接受纯人工口径
- 涉及数据库结构变化时，必须引入版本化迁移脚本，不再接受手工改表作为默认流程
- 所有新增运行参数必须进入 `application.yml` 或部署模板，不得只存在于口头说明
- 主链路升级必须以订单、库存、支付三域联动结果作为最终验收依据

## 7. Functional Requirements

### 7.1 Week 1: Database Migration And Quality Gate

本周目标固定为建立基础工程门禁，解决“能改代码但难以稳定发布”的问题。

本周必须完成：

- 选定并接入 `Flyway` 或 `Liquibase` 作为统一数据库迁移方案
- 为当前已存在的核心表建立基线迁移目录和命名规则
- 在 CI 中新增 `JaCoCo` 覆盖率统计
- 在 CI 中新增静态分析，固定选择 `SpotBugs` 或 `PMD`
- 在 CI 中新增依赖漏洞扫描，固定纳入 PR 质量门禁
- 在 `docs` 中补充数据库迁移与质量门禁使用说明

本周验收标准固定为：

- 本地与 CI 均可执行数据库迁移校验
- PR 流水线能产出覆盖率、静态分析和漏洞扫描结果
- 核心模块已有初始覆盖率门槛，且门槛写入流水线
- 新增门禁失败时能直接阻断 PR 合并

### 7.2 Week 2: Observability And Alerting

本周目标固定为建立“出了问题能看见、能定位”的运行基础。

本周必须完成：

- 统一关键业务指标命名规范
- 为订单 outbox、库存审计重试、支付回调等主链路补齐关键计数器和耗时指标
- 为应用实例暴露健康、版本、构建信息和 Prometheus 指标
- 产出第一版 `Production Dashboard`
- 产出第一版 `Alert Rules`
- 在 `deploy` 中沉淀可直接使用的告警配置样例

本周验收标准固定为：

- 主链路关键动作均能在指标中看到成功、失败和重试结果
- 至少存在 1 套可直接导入的 dashboard 配置
- 至少存在 1 套可直接执行的告警规则，覆盖错误率、重试耗尽、outbox 堆积和实例健康异常
- 运行团队可仅凭 dashboard 与日志定位主链路故障范围

### 7.3 Week 3: Main Flow Integration And Release Standardization

本周目标固定为建立高置信度交付能力，解决“单模块能测，整链路不稳”的问题。

本周必须完成：

- 为订单、库存、支付主链路补齐集成测试
- 主链路测试至少覆盖成功、幂等命中、重试接管和终态失败四类场景
- 建立发布前 `Release Checklist`
- 为各 starter 补齐部署模板、环境变量清单和健康检查说明
- 为常见回滚场景定义固定步骤

本周验收标准固定为：

- 主链路集成测试进入 CI
- 发布清单可逐项核验数据库、配置、依赖服务、指标和告警状态
- 部署模板可以支撑新环境最小化启动
- 回滚步骤可在文档中单独执行，不依赖作者口头说明

### 7.4 Week 4: Security Baseline And Operations Runbook

本周目标固定为把“能运行”提升为“能值班、能审计、能交接”。

本周必须完成：

- 为敏感配置、内部接口令牌和密钥来源补齐固定规则
- 补充敏感字段日志禁止项与脱敏约束
- 补充依赖升级与安全告警处理流程
- 编写主链路运行 `Runbook`
- 编写常见故障场景处置手册
- 形成最终版 `Production Readiness Checklist`

本周验收标准固定为：

- 安全基线规则进入 `docs`
- 值班人员可根据 `Runbook` 独立处理常见故障
- 发布前检查项覆盖数据库、配置、指标、告警、安全和回滚准备
- 项目达到可试运行的统一交付口径

## 8. Key Flows

### 8.1 Upgrade Execution Flow

固定执行顺序如下：

1. 建立迁移与质量门禁
2. 补齐观测与告警
3. 固化主链路集成测试与发布清单
4. 固化安全基线与运行手册

不得跳过前置阶段直接进入后续阶段。

### 8.2 Release Verification Flow

每周交付完成后必须按以下顺序验收：

1. 本地执行对应检查
2. CI 执行对应检查
3. 文档补齐操作说明
4. 将产物纳入 `deploy` 或 `docs`
5. 对主链路执行回归验证

## 9. Non-Functional Requirements

### 9.1 Delivery Requirements

- 每周必须产出可落库、可执行、可回归的真实结果，不接受只写方案不落地
- 每周结束时必须有对应文档更新
- 每周必须至少有一项能力进入自动化门禁

### 9.2 Stability Requirements

- 新增门禁后，主干分支必须保持可构建
- 主链路相关变更必须优先保证幂等、重试、补偿规则不被回退
- 任一周不得通过关闭原有告警或跳过测试来换取通过率

### 9.3 Documentation Requirements

- 所有新增规则必须写入 `docs`
- `docs/README.md` 必须能索引到本路线图
- 部署、告警、运行手册必须能独立阅读，不依赖代码上下文猜测

## 10. Open Items

- 第 1 周数据库迁移方案最终固定为 `Flyway` 还是 `Liquibase`
- 第 1 周静态分析方案最终固定为 `SpotBugs` 还是 `PMD`
- Dashboard 与告警配置最终使用的导出格式
