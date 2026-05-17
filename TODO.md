# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `ArchUnit-assembler-boundary`：对齐 Assembler 调用边界规则编号
  - 任务类型：执行任务
  - 依据文档：`docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`
  - 范围对象：`LayerArchitectureRuleSupport`、`NAMING-AND-PLACEMENT-RULES.md`
  - 处理动作：统一 `LAYER_ASSEMBLER_CALL_BOUNDARY` 与实际三个 assembler boundary ArchUnit 规则的编号口径
  - 验收点：文档 rule ID 与 ArchUnit `.because()` 使用同一套编号，不再出现文档和代码账目不一致
  - 重要度：7/10

- [ ] `ArchUnit-path-rules`：对齐 Path hard rules 与实际检测粒度
  - 任务类型：执行任务
  - 依据文档：`docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`
  - 范围对象：`PathArchitectureRuleSupport`、`NamingAndPlacementRuleSupport`、`NAMING-AND-PLACEMENT-RULES.md`
  - 处理动作：收敛或拆分 `PATH_CONTROLLER_PREFIX`、`PATH_CONTROLLER_RESOURCE_PATH`、`PATH_CONTROLLER_NO_PROVIDERS`、`PATH_DOMAIN_CANONICAL` 的文档与 ArchUnit 实现粒度
  - 验收点：每个 Path hard rule 要么有独立可追踪 ArchUnit 检测，要么在文档中合并为单一规则
  - 重要度：7/10

- [ ] `ArchUnit-layer-overlap`：标注 Layer 白名单与专项规则关系
  - 任务类型：执行任务
  - 依据文档：`docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`
  - 范围对象：`LAYER_INTERFACES_DEPENDENCY_WHITELIST` 及相关专项 ArchUnit 规则
  - 处理动作：明确 Layer 白名单规则与接口依赖专项规则的诊断增强关系
  - 验收点：文档能说明专项规则为何保留，且 ArchUnit rule ID 不表现为互相竞争的重复定义
  - 重要度：6/10

## 待讨论项
