# Unified ID Design

本文件只定义统一 ID 的工程边界和默认写法。

## Scope

- 统一 `UserId`、`RoleId`、`TenantId`、`OrderId`、`SkuId` 等领域标识建模
- 保留类型安全
- 统一 `Jackson`、`MyBatis` 和持久化边界适配
- 不覆盖业务单号设计

## Core Distinction

- `Database Primary Key`
  数据库主键
- `Business No`
  业务单号，例如 `orderNo`、`paymentNo`
- `Domain Identifier`
  Java 里的强类型领域标识，例如 `UserId`、`TenantId`

三者不得混用。

## Module Boundary

- 统一 ID 公共能力固定放在 `bacon-common-id`
- `domain` 优先使用强类型 ID
- `interfaces`、`api.dto`、`infra.dataobject` 可按协议需要保留基础类型
- 边界转换必须通过统一适配层完成
- 业务单号规则仍以架构文档为准，不属于统一 ID 体系

## Core Model

- `Identifier<T>`
  标识接口
- `BaseId<T>`
  抽象基类
- 具体 ID
  例如 `DepartmentId`、`UserId`、`RoleId`、`TenantId`、`OrderId`、`SkuId`
- `IdGenerator`
  统一底层值生成器
- `Ids`
  统一工厂入口
- `IdConverters`
  统一适配层

## Hard Rules

- 统一 ID 采用“单值包装 + 强类型”模型
- `BaseId<T>` 必须不可变
- 构造器固定非公开，统一使用 `of(...)`
- 判等必须基于“具体类型 + 底层值”
- 不允许每个具体 ID 重写 `equals/hashCode/toString`
- 框架适配逻辑固定放在转换器或 `TypeHandler`
- `domain` 不得被 `MyBatis`、`JPA`、`Spring MVC` 注解污染
- 新增 ID 类型时，优先复用统一基类和统一转换器

## Value Type Rules

- `BaseId<T>` 的底层值优先支持 `String` 或 `Long`
- 文本型 ID 底层值不得为空白
- 一个具体 ID 类型只能固定一种底层值类型
- `UserId` 固定使用 `String`
- `UserIdentityId` 固定使用 `String`
- `TenantId` 固定使用文本型租户主标识
- `RoleId`、`SkuId`、`OrderId` 可使用 `String` 或 `Long`，但具体类型一旦确定后不可混用

## Generation Rules

- `Ids` 是统一工厂入口
- `Ids` 负责屏蔽不同发号来源
- `Ids` 负责把底层值包装成具体 ID
- `IdGenerator` 以业务标签区分不同发号命名空间
- 同一种 ID 的业务标签必须稳定
- 发号提供方返回非法结果时必须失败，不能静默兜底

默认风格：

```java
UserId userId = UserId.of("U1001");
TenantId tenantId = TenantId.of("T001");
OrderId orderId = ids.orderId();
```

## Serialization And Persistence

- `Jackson` 对外序列化为单一基础值
- 不序列化成额外包裹结构，如 `{\"value\":1001}`
- `MyBatis` 通过统一 `TypeHandler` 或统一转换器完成持久化
- 同一业务域内同一类 ID 的持久化方式必须统一
- 仓库当前未正式使用 `JPA`，但允许公共层预留 `AttributeConverter` 模式

## Persistence Defaults

- `UserId` 默认 `varchar(64)`
- `TenantId` 默认 `varchar(64)`
- `RoleId` 若为文本型，默认 `varchar(64)`
- `SkuId`、`OrderId` 若为 `Long`，可继续使用 `bigint`
- 统一 ID 设计优先改变 Java 类型系统，不强制全库重写

## Migration Rules

- 只在语义明显不匹配时调整数据库结构
- 数据库、代码、文档必须先统一“这是主键、业务单号还是领域标识”
- 不允许把统一 ID 改造理解成全库字段类型统一重写

## Non-Functional Rules

- 统一 ID 基础设施必须在公共模块，不能每个业务域各写一套
- 新增具体 ID 类型时，除类型声明外的新增代码应控制在极小范围
- 统一 ID 不得明显增加序列化和持久化复杂度
- 必须支持单元测试直接构造，不依赖 Spring 容器
