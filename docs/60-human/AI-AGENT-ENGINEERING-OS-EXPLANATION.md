# 面向 AI Agent 的工程操作系统

## 1. 背景

软件工程正在从“人类独立编码”进入“人类判断、AI 执行”的新阶段。

在这个阶段，AI 不再只是回答问题或生成零散代码片段，而是开始作为工程执行者参与需求理解、代码生成、测试补充、文档维护和提交交付。问题也随之变化：核心挑战不再是“AI 能不能写代码”，而是“AI 能不能在一个复杂工程中持续、稳定、可控地写对代码”。

`Bacon` 项目的价值在于，它不是简单把 AI 放进已有仓库，而是把项目组织结构、目录结构、代码分层、文档系统、任务生命周期和 Git 历史组合成一套可供 AI Agent 运行的工程环境。

因此，可以把这个项目定义为：

```text
面向 AI Agent 的工程操作系统
```

它的目标不是让 AI 自由发挥，而是让 AI 在明确的上下文、规则、边界和交付闭环中执行工程任务。

## 2. 要解决的问题

### 2.1 AI 容易读错上下文

传统仓库通常把 README、需求文档、历史设计、临时记录、人工说明和实现细节混在一起。人类可以凭经验判断哪些文档可信、哪些内容过期、哪些只是背景材料，但 AI 很容易把所有文本都当成同等权威。

这会导致几类问题：

- 读取过多无关文档，导致上下文污染。
- 使用过期说明覆盖当前架构规则。
- 把人类叙事材料误当成实现约束。
- 在没有足够上下文时凭模式补全。

`Bacon` 的解决方式是把文档变成上下文路由系统。`docs/AGENT.md` 明确规定 AI 先读什么、实现前必须读什么、不同任务继续读哪些文档，以及哪些目录不默认读取。

### 2.2 AI 容易写错位置

复杂工程中，代码写在哪里往往比代码本身更重要。

如果 AI 不理解分层边界，就可能把 HTTP Request 传进 application，把 MyBatis Mapper 放到 interfaces，或者让一个业务域直接依赖另一个业务域的 infra 实现。代码短期可能能跑，但工程结构会被逐步破坏。

`Bacon` 通过固定业务域五层结构解决这个问题：

```text
api
interfaces
application
domain
infra
```

每一层都有明确职责，每一类代码都有稳定落点。目录不只是文件容器，而是 AI 的行动地图。

### 2.3 AI 容易破坏依赖边界

AI 生成代码时往往倾向于选择“最近、最方便、最能编译通过”的依赖路径，而不是最符合架构意图的路径。

典型风险包括：

- 跨域直接调用对方 application。
- api 模块反向依赖其他业务域。
- domain 感知 HTTP、缓存、Mapper 或 SDK。
- application 直接操作 infra 技术对象。
- interfaces 绕过 application 直接访问 repository。

`Bacon` 在 `ARCHITECTURE.md` 中把这些规则写成实现红线。跨域调用固定依赖 `api.facade`，域内主链路固定为：

```text
HTTP -> interfaces -> application -> domain -> repository -> MySQL/Redis
```

这让 AI 执行时不是“凭感觉设计”，而是沿着已经写明的依赖轨道行动。

### 2.4 AI 容易过度抽象

AI 常见问题之一是为了“看起来专业”而新增不必要的 helper、config、manager、factory、adapter 或目录层级。这样的代码会增加维护成本，也会削弱工程原本清晰的分层。

项目规则明确要求：

- 优先最简单可工作的方案。
- 不因猜测未来需求新增抽象。
- 不无理由新增配置、目录或 helper 层。
- 复用现有模块结构和分层模式。

这把 AI 的创造力限制在正确区域：解决当前问题，而不是制造额外结构。

### 2.5 AI 容易让任务历史和文档混乱

很多 AI 参与的项目会出现一个问题：TODO、完成记录、设计解释、临时说明、失败尝试都堆进文档，最后文档越来越厚，却越来越不可信。

`Bacon` 对此做了明确切分：

- `TODO.md` 是任务执行队列，不是完成历史。
- 完成的 TODO 必须删除。
- 完成历史由 Git commit / PR 保存。
- 文档只保留稳定规则和可执行上下文。

这让 Git 成为工程时间轴，文档成为当前规则系统，TODO 成为待执行队列，三者不互相污染。

## 3. 核心创新点

### 3.1 从“提示词协作”升级为“仓库内协议协作”

普通 AI 编程依赖聊天窗口中的提示词。提示词一旦离开当前对话，约束就消失了。

`Bacon` 的创新是把 AI 协作协议写进仓库本身：

- `AGENT.md` 定义 AI 的读取路径。
- `ARCHITECTURE.md` 定义工程红线。
- `DOCUMENT-RULES.md` 定义文档写法。
- 领域需求文档定义业务口径。
- 数据库文档定义数据事实。
- Git commit 规范定义交付记录。

这样，AI 每次进入项目都能重新加载同一套规则，不依赖某一次对话中的临时说明。

### 3.2 文档成为上下文路由层

这个项目的文档系统不是普通知识库，而是 AI 的上下文路由层。

它明确区分：

- 工程治理文档。
- 业务需求文档。
- 数据库设计文档。
- 专项设计文档。
- 上线准备文档。
- 人类阅读材料。
- 人工触发提示词。

更关键的是，它规定了读取条件。例如：

- 实现前读 `ARCHITECTURE.md`。
- 数据库任务读 `DATABASE-RULES.md` 和对应数据库设计。
- 改类名、改目录读 `NAMING-AND-PLACEMENT-RULES.md`。
- 单域任务不默认读取其他域文档。
- `50-prompts/` 和 `60-human/` 不默认加载。

这是一种面向 AI 的上下文预算管理。它不只告诉 AI “读什么”，还告诉 AI “什么时候读”和“不要读什么”。

### 3.3 架构规则被写成 AI 可执行约束

很多架构文档只解释理念，对 AI 来说太抽象。`Bacon` 的架构文档更接近规则表。

例如：

- `interfaces` 不直接访问 `domain repository` 或 `infra mapper`。
- `application` 不直接接收 `interfaces.dto.*Request`。
- `domain` 不感知 HTTP、MyBatis、缓存、MQ、SDK。
- `infra` 从数据库恢复领域对象时调用 `reconstruct(...)`。
- 创建领域对象时由 application 调用 `domain entity.create(...)`。
- 正式 Repository 不允许用内存 Map 作为主存储。

这些约束足够明确，AI 可以直接用来判断代码应该放哪里、依赖谁、调用什么方法。

### 3.4 目录结构成为执行轨道

项目的目录结构承担了“行为约束”的功能。

业务域固定拆分为：

```text
bacon-<domain>-api
bacon-<domain>-interfaces
bacon-<domain>-application
bacon-<domain>-domain
bacon-<domain>-infra
```

这让 AI 在新增代码时不需要重新设计结构，只需要判断当前代码属于哪一层。

包结构也进一步细化了落点：

```text
interfaces/controller
interfaces/provider
interfaces/facade
application/command
application/query
application/result
domain/model/entity
domain/model/valueobject
domain/repository
infra/persistence/dataobject
infra/persistence/mapper
infra/repository/impl
infra/facade/remote
```

这是一种降低 AI 幻觉空间的工程设计。自由度被控制住以后，生成质量会更稳定。

### 3.5 单体和微服务通过装配层统一

`Bacon` 的业务代码只写一份，运行形态由 `bacon-app` 装配。

单体模式由 `bacon-mono-boot` 聚合所有业务域。微服务模式由各领域 starter 单独装配本域模块。跨域调用则通过 `api.facade` 和本地/远程适配切换：

```text
单体模式：application -> Facade -> LocalImpl -> peer application
微服务模式：application -> Facade -> RemoteImpl -> remote service
```

这对 AI 友好，因为 AI 不需要为单体和微服务分别维护两套业务逻辑。它只需要遵守同一套业务分层，装配差异由启动模块承担。

### 3.6 Git 被设计为完成历史层

项目明确要求每个文件修改最终都进入 commit，且 commit message 使用：

```text
Type(domain): 中文说明
```

这不是普通提交规范，而是工程操作系统中的“历史落盘协议”。

TODO 描述当前待执行任务，代码表达当前实现状态，commit 保存完成历史。这样可以避免 AI 在文档里反复堆积“已完成记录”，也方便后来追溯每个能力是在什么时候、以什么目的改动的。

### 3.7 反误改机制前置

这个项目对 AI 常见误改做了前置防御。

防御点包括：

- 通过读文档顺序避免读取错误权威。
- 通过分层规则避免跨层调用。
- 通过模块依赖避免跨域污染。
- 通过命名规则避免模型命名混乱。
- 通过数据库规则避免持久化语义漂移。
- 通过 TODO 生命周期避免文档变成完成历史。
- 通过 commit 规范保留可审计交付轨迹。

这使项目不是在 AI 犯错后靠人类 review 补救，而是在结构上减少 AI 犯错空间。

## 4. 实现路径

### 4.1 第一层：建立工程治理文档

第一步是把工程级规则从口头约定变成仓库内文档。

核心文档包括：

- `docs/AGENT.md`
- `docs/00-governance/ARCHITECTURE.md`
- `docs/00-governance/DOCUMENT-RULES.md`
- `docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`
- `docs/00-governance/DATABASE-RULES.md`
- `docs/00-governance/UNIFIED-ID-DESIGN.md`

这些文档不追求长，而追求稳定、明确、可执行。它们构成 AI Agent 的基础系统调用接口。

### 4.2 第二层：建立任务路由规则

AI 不应该默认读取所有文档。项目需要一份入口文档告诉 AI 如何按任务类型加载上下文。

例如：

```text
实现业务逻辑 -> ARCHITECTURE.md + 对应 REQUIREMENTS
数据库修改 -> DATABASE-RULES.md + 对应 DATABASE-DESIGN
新增类或改目录 -> NAMING-AND-PLACEMENT-RULES.md
部署上线 -> DEPLOYMENT 规则 + readiness 文档
```

这样 AI 能快速进入正确上下文，而不是把整个 `docs/` 当成输入。

### 4.3 第三层：固定业务域模块结构

每个业务域固定五层：

```text
api
interfaces
application
domain
infra
```

这一步的关键是让目录结构和依赖方向一致。不是只有目录名字相似，而是 Maven 模块依赖也要反映分层边界。

典型依赖方向是：

```text
interfaces -> application -> domain
infra -> domain
application -> other-domain-api
```

跨域依赖只允许走 `api.facade`，避免业务域之间互相穿透实现层。

### 4.4 第四层：沉淀稳定包名和模型命名

AI 生成代码时非常依赖已有模式。稳定的包名和命名规则会显著提升生成一致性。

项目中应固定：

- `*Request` 放在 `interfaces/request`。
- `*Response` 放在 `interfaces/response`。
- `*Command` 放在 `application/command`。
- `*Query` 放在 `application/query`。
- `*Result` 放在 `application/result`。
- 领域实体放在 `domain/model/entity`。
- 领域值对象放在 `domain/model/valueobject`。
- 数据库对象放在 `infra/persistence/dataobject`。
- Mapper 放在 `infra/persistence/mapper`。
- Repository 实现放在 `infra/repository/impl`。

当命名和目录稳定后，AI 可以通过局部模式推断正确实现方式。

### 4.5 第五层：把运行形态放到装配层

业务代码不应该因为单体或微服务形态而复制。

实现路径是：

- `bacon-biz` 保存业务域代码。
- `bacon-common` 保存共享基础能力。
- `bacon-app` 保存启动装配。
- `bacon-mono-boot` 聚合所有业务域。
- `bacon-*-starter` 装配单个业务域。
- `bacon-gateway` 承担网关入口。

这样业务逻辑维持一份，运行方式由 app 层决定。

### 4.6 第六层：建立测试和质量门禁

AI 生成代码必须通过工程反馈闭环验证。

项目中质量工具分工应明确：

- `spotless` 负责格式化。
- `checkstyle` 负责规则检查。
- 单元测试验证行为。
- 架构测试验证依赖边界。
- CI 验证提交状态。

这一步的重点是避免把所有问题都交给人工 review。工程系统本身要能发现一部分 AI 误改。

### 4.7 第七层：用 Git 固化交付闭环

最后一步是把任务生命周期和 Git 绑定。

推荐闭环是：

```text
人类提出目标
-> TODO.md 形成执行队列
-> AI 按规则读取上下文
-> AI 修改代码和测试
-> 本地验证
-> 删除已完成 TODO
-> Git commit 保存完成历史
-> PR / CI 进入审计
```

这里的关键是分工清晰：

- TODO 保存“还要做什么”。
- 文档保存“规则是什么”。
- 代码保存“当前系统如何运行”。
- Git 保存“过去完成了什么”。

## 5. 整体价值

### 5.1 对人类的价值

人类不需要每次都向 AI 重复解释项目结构、分层边界、依赖规则和提交规范。

人类的职责回到更高价值的部分：

- 判断业务方向。
- 决定取舍。
- 审阅关键结果。
- 维护工程规则。
- 处理模糊和冲突。

### 5.2 对 AI 的价值

AI 获得了稳定的执行环境。

它知道：

- 先读什么。
- 哪些文档是权威。
- 哪些目录不能默认读取。
- 代码应该写在哪一层。
- 跨域应该依赖什么。
- 完成任务后如何留痕。

这降低了 AI 在复杂工程中的不确定性。

### 5.3 对工程的价值

工程获得了更强的可持续性。

AI 可以持续参与实现，但不会因为每次执行都重新理解架构而制造随机结构。项目组织、目录、代码、文档和 Git 共同形成约束，使工程能够在 AI 高频参与下仍保持一致。

## 6. 结论

“面向 AI Agent 的工程操作系统”不是一个单独工具，而是一套工程组织方式。

它把 AI 协作从提示词层面推进到仓库结构层面：

```text
文档决定 AI 怎么读
目录决定 AI 写到哪
模块决定代码能依赖谁
测试决定行为是否成立
Git 决定历史如何保存
TODO 决定下一步执行什么
```

这套结构的核心思想是：

```text
人类负责判断，AI 负责执行，工程结构负责让执行不散。
```

当这些层次交织在一起，项目就不再只是代码仓库，而成为一个可以承载 AI Agent 长期工作的立体工程系统。
