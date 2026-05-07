# 业务描述语言备忘录

本文不是治理规则，也不是代码生成规范。

它只记录一次关于“业务如何直接映射为应用”的讨论。这里面的概念还没有固定，不应被 AI 当作默认执行规范。

---

## 想法从哪里来

Sandwich 的旧系统治理已经证明了一件事：AI 的工程效果，很大程度取决于边界是否清楚、业务定义是否稳定、修改面是否足够窄。

Java、DDD、Command、ValueObject、Enum、Audit、RUNBOOK、TODO 和 commit 收口共同形成了一套可控工程结构。它们让 AI 不再只是在文本里猜代码，而是在明确的业务边界、类型边界和提交边界里执行。

下一步自然会出现一个问题：

> 如果业务本身也能被结构化描述，是否可以从业务语义直接映射为应用？

这不是传统代码生成器，也不是 low-code 表单配置。更接近一种“业务语义编译器”：人类描述业务判断，AI 将这些判断投影到 DDD 模型、应用服务、数据库、API、前端、审计和测试中。

## 核心判断

业务描述语言不应该描述代码。

它应该描述业务判断。

也就是说，它不关心“生成哪个 Controller、Mapper、Page”，而是关心：

- 业务对象是什么。
- 谁能操作它。
- 它有哪些状态。
- 每个动作改变什么。
- 哪些动作需要审计。
- 哪些动作需要并发控制。
- 哪些动作需要幂等。
- 后台是否需要管理。
- 前台是否需要展示。
- 查询有哪些维度。
- UI 需要哪些操作入口。

代码、SQL、API、admin-web、audit、test 和 docs 都是这些业务判断的工程投影。

## 为什么 Java 和 DDD 适合作为底座

Java 的优势不只是稳定，而是显式。

类型、包结构、方法签名、接口、枚举和值对象都能成为文档。`CreatePostCommand`、`EntityId`、`PostStatus.PUBLISHED`、`AuditObjectRef` 这类结构，比散落的 `String`、`Long` 和 `Map` 更能帮助 AI 判断业务含义。

DDD 的优势也不只是分层，而是把业务边界切成可判断单元。Aggregate、ValueObject、Command、Repository contract、Application Service 都能缩小 AI 的推理面。

如果业务描述语言能和这些结构对齐，它就不需要直接生成所有代码细节，而是先稳定业务语义，再让工程结构承接语义。

## 一个暂存例子

下面这个例子不是固定语法，只是保留讨论时出现的一个方向。

```yaml
domain: timeline

objects:
  - name: Post
    kind: aggregate
    id: EntityId
    owner: Member
    states:
      - DRAFT
      - PUBLISHED
      - ARCHIVED

    commands:
      - name: CreatePost
        actor: Member
        idempotent: true
        audit: true
        input:
          - content: Text
          - visibility: PostVisibility
          - attachmentIds: List<EntityId>

      - name: PublishPost
        actor: Member
        concurrency: expectedVersion
        audit: true
        rule:
          - only DRAFT can publish

      - name: ArchivePost
        actor: Member
        concurrency: expectedVersion
        audit: true
        rule:
          - only PUBLISHED can archive

queries:
  - name: TimelineFeed
    actor: Member
    result: Page<PostSummary>
    filter:
      - visibility
      - authorId

admin:
  manage:
    - Post
  actions:
    - ArchivePost
```

这个例子的价值不在 YAML，而在它能自然逼出工程结构：

- `Post` 是 aggregate。
- `PostStatus` 和 `PostVisibility` 会成为 enum，并可进入系统字典。
- `CreatePostCommand`、`PublishPostCommand`、`ArchivePostCommand` 会成为应用层命令。
- `expectedVersion` 会触发并发控制设计。
- `audit: true` 会触发审计对象和审计动作设计。
- `idempotent: true` 会触发幂等键设计。
- `TimelineFeed` 会触发 query、response、mapper 和分页语义。
- `admin.manage` 会触发后台页面、权限点和操作入口。

## 它和 Bacon 的关系

Bacon 当前更像 AI 工程控制系统：它约束文档、任务、提交、架构、门禁和复盘。

业务描述语言会把 Bacon 再往上推一层：

- 旧项目治理证明 AI 可以稳定修改系统。
- Audit 证明系统可以长出治理型新业务。
- admin-web 证明治理方法可以扩展到不同技术栈入口。
- timeline 这类业务证明系统可以长出产品型新业务。
- 业务描述语言则尝试把“长业务”的过程结构化。

如果这条路成立，Bacon 就不只是让 AI 可控地改工程，而是让业务语义可控地生成应用。

## 暂不固定的部分

当前还不应该急着把它做成正式 DSL。

需要继续观察的问题包括：

- 业务描述语言应该使用 YAML、Markdown 表格，还是更接近领域文本。
- `rule` 应该如何表达状态流转、不变量、权限和失败语义。
- `audit`、`idempotent`、`concurrency` 是否应该成为固定关键字。
- 它应该生成代码，还是生成 RUNBOOK、TODO 和设计草案。
- 人类确认点应该放在业务描述、工程投影，还是提交收口之前。
- 它如何避免变成另一套过重、过早固定的抽象。

## 留给后面的自己

以后再看这份备忘录时，可以先问：

- Timeline 是否已经成为业务描述语言的第一个验证对象。
- Audit 的 `objectType`、`objectId`、`version`、`AuditAction` 是否能反推 DSL 的审计表达。
- admin-web 的页面和权限是否能由业务描述自然投影出来。
- Java 的类型、DDD 的边界和 Bacon 的任务治理是否已经形成稳定闭环。
- 业务描述语言是否真的减少了 AI 猜测，而不是增加了新的解释成本。

真正值得保留的判断是：不要让 AI 从自然语言里直接猜应用，也不要让人类用技术细节描述业务。中间应该存在一层足够业务化、足够结构化、足够可判断的语言，让业务语义先稳定下来，再投影为可验证的工程实现。
