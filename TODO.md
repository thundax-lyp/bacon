# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `ArchUnit-layer-overlap`：标注 Layer 白名单与专项规则关系
  - 任务类型：执行任务
  - 依据文档：`docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`
  - 范围对象：`LAYER_INTERFACES_DEPENDENCY_WHITELIST` 及相关专项 ArchUnit 规则
  - 处理动作：明确 Layer 白名单规则与接口依赖专项规则的诊断增强关系
  - 验收点：文档能说明专项规则为何保留，且 ArchUnit rule ID 不表现为互相竞争的重复定义
  - 重要度：6/10

## 待讨论项
