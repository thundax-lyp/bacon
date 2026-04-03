# TODO

## 架构规则工程化落地

目标：把当前已经明确的分层、目录、命名、转换职责规则尽量收敛为可执行检查，减少后续只靠 code review 和记忆维持一致性的风险。

### 1. 第一优先级：使用 ArchUnit 落地硬规则

- 校验 `infra` 只能作为实现层依赖 `domain.repository`

### 2. 第二优先级：使用自定义测试落地项目特有规则

这类规则不适合只靠包依赖判断，建议用 `JUnit + 反射/类扫描` 实现。

- 校验 `application` 对外公开方法的租户参数优先使用 `TenantId`
- 校验 `application` 不再新增字符串 `tenantId` 重载方法
- 校验 `application` 不再分散定义 `requireExistingTenantId`
- 校验 `interfaces` 入口层负责字符串 `tenantId` 到 `TenantId` 的解析
- 校验 `interfaces.assembler` 允许出现 `toResponse`、`toCommand`、`toQuery`
- 校验 `application.assembler` 允许出现 `toDto`、`toResult`
- 校验 `interfaces` 不返回 `application.result` 之外的协议外模型
- 校验 `application` 不依赖 `interfaces.response`
- 校验 `Response` 类型只出现在 `interfaces`
- 校验 `Request` 类型只出现在 `interfaces`

### 3. 第三优先级：保留为文档约束，不做强阻塞

这类规则主观性较强，工具可以做提示，但不建议第一阶段做成强失败。

- 简单且只在单类内使用一次的 `toDto` 可保留为私有方法
- 多处复用的转换必须提取到对应层的 `assembler`
- 同一业务域内目录语义必须保持一致
- 同一业务域内不要同时混用多套转换风格
- 文档中定义的标准结构要持续与代码结构对齐

### 4. 推荐实施顺序

1. 再补 2 到 4 个最关键的自定义架构测试：
   - `application` 不新增字符串 `tenantId` 重载
   - `Response` 只在 `interfaces`
2. 最后再考虑把 `toDto` / `assembler` 的规则细化成更严格的测试

### 5. 实施注意事项

- 第一批规则要优先选择误报率低的检查，先建立团队信任
- 对历史代码允许阶段性豁免清单，不要一次性阻断全部存量问题
- 架构测试要进入 `mvn test` 或 `verify` 流程，避免变成摆设
- 规则命名要直接对应文档术语，保持文档和测试一一映射
