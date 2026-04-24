# HOW-TO HOW-TO

## 1. Purpose

本文档定义项目内 `HOW-TO-XXX.md` 的统一写法。  
目标不是重复治理红线，而是把高频任务的进入路径写清楚，让开发者和 AI 都能按同一套最小闭环执行。

## 2. Scope

当前范围：

- `docs/00-governance/how-to/` 目录下全部 `HOW-TO-XXX.md`
- 面向开发者与 AI 的高频实现/治理操作说明
- “如何安全地下第一刀”的最小闭环步骤

不在范围内：

- 不替代 `ARCHITECTURE.md`、`ARCHITECTURE-INTENT.md`、`DOCUMENT-RULES.md`
- 不定义新的架构红线
- 不承载完整业务需求
- 不沉淀一次性任务记录

## 3. Bounded Context

`HOW-TO` 文档是治理文档和具体实现之间的操作层说明。

- 治理文档回答“边界是什么、为什么这样做”
- `HOW-TO` 回答“遇到某类高频任务时，第一步到最后一步怎么做”

`HOW-TO` 的主要读者不是完全陌生的外部读者，而是已经处在本仓库上下文中的开发者和 AI。

## 4. Module Mapping

- 目录固定为 `docs/00-governance/how-to/`
- 文件命名固定为 `HOW-TO-XXX.md`
- `XXX` 必须表达一个明确、高频、可复用的任务类型
- 一个 `HOW-TO` 只解决一类任务，不混合多个主题

推荐主题：

- 新增一个接口入口
- 新增一个跨域 `Facade`
- 新增一个查询链路
- 新增一个持久化闭环
- 更新 `TODO.md` 与提交

禁止主题：

- 某次临时修复复盘
- 同时覆盖多个不相关任务的大而全教程
- 重新解释已经在治理文档中固定的全部规则

## 5. Core Domain Objects

### 5.1 Required Sections

每篇 `HOW-TO` 优先采用以下结构：

1. `Purpose`
2. `Scope`
3. `Bounded Context`
4. `When To Use`
5. `Do Not Use For`
6. `Pre-Checks`
7. `Steps`
8. `Files To Touch`
9. `Common Mistakes`
10. `Verification`
11. `Commit Guidance`
12. `Open Items`

如某节确实不适用，可以省略，但不得把这些信息打散到多个不相关章节。

### 5.2 Task Granularity

每篇 `HOW-TO` 必须满足以下粒度要求：

- 读者读完后可以立刻开始做一类具体任务
- 能列出最小必要改动范围
- 能指出本任务最容易踩错的边界
- 能说明该任务通常需要同步哪些测试、文档或 `TODO.md`

如果一个主题无法在一篇文档内保持单一任务焦点，应拆成多篇 `HOW-TO`。

## 6. Global Constraints

- `HOW-TO` 不得重复抄写大段治理规则；只引用当前任务必需的规则
- `HOW-TO` 必须优先描述最小闭环，不默认扩大成全局重构
- `HOW-TO` 必须显式说明“不该碰什么边界”
- `HOW-TO` 中出现的目录、命名、分层规则必须与现有治理文档一致
- `HOW-TO` 必须适合 AI 读取，使用确定性表达，不写“建议”“视情况”“大概”
- `HOW-TO` 必须以当前仓库现有结构为前提，不为了教学新增抽象层

## 7. Functional Requirements

### 7.1 When To Create A HOW-TO

满足以下条件之一时，应该新增 `HOW-TO`：

- 同类任务会被重复执行
- 新人或 AI 在该任务上反复放错位置
- 现有治理文档能定义边界，但不能直接指导落地步骤
- 该任务天然需要跨越多个固定层次，但路径稳定

### 7.2 How To Write Steps

`Steps` 章节必须遵守以下要求：

- 按执行顺序编排
- 每一步只写一个明确动作
- 先写判断，再写修改
- 先写入口层，再写编排层，再写持久化/适配层，再写测试和提交
- 需要引用治理规则时，只引用最相关文档

### 7.3 Files To Touch

`Files To Touch` 章节必须回答：

- 本任务通常新增或修改哪些类型的文件
- 哪些文件是可选的
- 哪些目录原则上不该被触碰

这里写的是“文件类型与目录模式”，不是一次具体任务的文件清单。

## 8. Key Flows

### 8.1 Reading Flow

阅读 `HOW-TO` 时固定按以下顺序判断：

1. 当前任务是否真的属于该 `HOW-TO` 的适用范围。
2. 当前任务是否已经被更具体的 `HOW-TO` 覆盖。
3. 当前任务是否需要先读关联治理文档。
4. 当前任务是否会超出该 `HOW-TO` 约定的最小闭环。

### 8.2 Writing Flow

新增 `HOW-TO` 时固定按以下顺序编写：

1. 先明确任务边界和适用场景。
2. 再列出不适用场景。
3. 再写最小闭环步骤。
4. 再补常见错误、验证和提交要求。
5. 最后在 `TODO.md` 或相关入口文档中补索引。

## 9. Non-Functional Requirements

- `HOW-TO` 必须短、稳、可执行，避免写成长篇概念解释
- 不同 `HOW-TO` 之间的章节命名应保持一致
- `HOW-TO` 必须优先帮助新人和 AI 安全起步，而不是展示完整知识地图
- `HOW-TO` 变更后，如影响 AI 默认读取路径，必须同步 `docs/AGENT.md`

## 10. Open Items

无
