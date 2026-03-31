# 统一 ID 体系设计

## 1. Purpose

本文档定义项目内统一 ID 体系的工程设计。  
目标是统一 `UserId`、`RoleId`、`TenantId`、`OrderId`、`SkuId` 等标识的建模方式，减少重复样板代码，保留类型安全，并支持 `Jackson`、`MyBatis` 和后续 `JPA` 集成。

## 2. Scope

当前范围：

- 定义统一 ID 抽象：`Identifier<T>`、`BaseId<T>`
- 定义具体 ID 类型的固定写法：`UserId`、`RoleId`、`TenantId`、`OrderId`、`SkuId`
- 定义统一生成入口：`IdGenerator`、`Ids`
- 定义统一适配层：`IdConverters`
- 定义 `Jackson`、`MyBatis`、`JPA` 的接入规则
- 定义数据库字段与迁移边界

不在当前范围：

- 一次性替换仓库内所有现有 `Long id`、`String tenantNo`、`String orderNo`
- 修改所有业务域接口契约
- 引入新的分布式发号中心
- 为每个业务域单独设计一套专用 ID 框架

## 3. Bounded Context

- 本设计属于工程级公共能力，固定落在 `bacon-common-id`
- `domain` 层优先使用强类型 ID 表达领域标识
- `interfaces`、`api.dto`、`infra.persistence.dataobject` 可按协议需要保留基础类型字段，但转换必须通过统一适配层完成
- `Order`、`Payment`、`Inventory` 的业务单号规则仍以 `ARCHITECTURE.md` 既有定义为准
- 本设计中的“领域 ID”不等于“业务单号”

## 4. Module Mapping

- `bacon-common-id`
  - `core`: `Identifier<T>`、`BaseId<T>`、`Ids`
  - `generator`: 统一 ID 生成入口
  - `domain`: 具体 ID 类型，如 `UserId`、`RoleId`、`TenantId`
  - `converter`: `Jackson` / `JPA` 通用转换支持
- `bacon-common-mybatis`
  - `handler`: `MyBatis` 统一 `TypeHandler`
- 各业务域
  - `domain.model.valueobject` 或 `domain.model.identifier` 只保留业务语义类型声明
  - 不重复实现公共比较、判空、序列化、持久化逻辑

## 5. Core Domain Objects

### 5.1 Terminology

- `Database Primary Key`：数据库主键，如表的 `id`
- `Business No`：业务单号，如 `orderNo`、`paymentNo`、`reservationNo`
- `Domain Identifier`：领域内强类型标识，如 `UserId`、`TenantId`、`OrderId`

固定边界：

- `Database Primary Key` 用于数据库关系和站内持久化定位
- `Business No` 用于跨域调用、幂等键和外部查询
- `Domain Identifier` 用于 Java 代码中的类型安全表达
- 三者不得混用

### 5.2 Fixed Interfaces

- `Identifier<T>`
  - 固定表示“可暴露底层值的领域标识”
  - 至少提供 `value()`、`type()`、`asString()`
- `BaseId<T>`
  - 固定为抽象基类
  - 负责 `equals`、`hashCode`、`toString`
  - 负责空值校验、类型校验、统一文本表达

### 5.3 Fixed Concrete IDs

当前统一纳入首批范围：

- `UserId`
- `RoleId`
- `TenantId`
- `OrderId`
- `SkuId`

固定要求：

- 每个具体 ID 只保留极薄的一层类型声明
- 每个具体 ID 固定提供 `of(...)`
- 如该类型支持自动生成，则固定提供 `newId(Ids ids)` 或由 `Ids` 直接暴露工厂方法
- 不允许每个 ID 重新手写 `equals`、`hashCode`、`toString`、`@JsonValue`、`TypeHandler`

## 6. Global Constraints

- 统一 ID 体系优先采用“单值包装 + 强类型”模型
- `BaseId<T>` 必须保持不可变
- `BaseId<T>` 的底层值当前固定优先支持 `String` 与 `Long`
- `UserId` 当前固定使用 `String`
- `TenantId` 当前优先承载租户业务标识，例如 `T001`
- `RoleId`、`SkuId`、`OrderId` 可按业务演进分别承载字符串型或数值型值，但一个具体类型在同一阶段只能固定一种底层值类型
- `BaseId<T>` 不得直接依赖 `MyBatis`、`JPA`、`Spring MVC`
- 框架适配逻辑固定放在 `converter` 或 `handler`，不得回灌到领域模型
- `domain` 中允许出现具体 ID 类型，不允许出现框架注解污染领域对象
- 新增具体 ID 类型时，优先复用统一基类与统一转换器，不新增第二套模式

## 7. Functional Requirements

### 7.1 Unified Type Model

固定写法：

```java
UserId userId = UserId.of("U1001");
TenantId tenantId = TenantId.of("T001");
OrderId orderId = ids.orderId();
```

固定要求：

- `UserId.of(...)`、`TenantId.of(...)`、`OrderId.of(...)` 必须为静态工厂
- 构造器固定非公开，避免绕过校验
- 判等必须基于“具体类型 + 底层值”
- `UserId.of("U1001")` 不得与 `RoleId.of(1001L)` 判等

### 7.2 Unified Factory Entry

- `Ids` 固定为统一工厂入口
- `Ids` 负责屏蔽不同发号来源
- `Ids` 可组合现有 `IdGenerator`

固定方法风格：

- `ids.userId()`
- `ids.roleId()`
- `ids.orderId()`
- `ids.skuId()`

固定要求：

- `Ids` 不感知具体业务流程，只负责生成标识
- `Ids` 生成领域 ID 时，底层值来源必须可配置
- 业务单号如 `orderNo`、`paymentNo` 仍走既有单号生成器，不与 `OrderId` 混用

### 7.3 Jackson Rules

- 统一 ID 对外序列化为单一基础值
- `UserId("U1001")` 序列化后固定为 `"U1001"`
- `Jackson` 反序列化必须支持从基础值恢复具体 ID 类型
- 不允许把统一 ID 序列化成 `{"value":1001}` 这类额外包裹结构

### 7.4 MyBatis Rules

- `MyBatis` 持久化固定通过统一 `TypeHandler` 或统一转换基类完成
- `DataObject` 层字段可直接使用具体 ID 类型，前提是已注册统一处理器
- 如某阶段为了控制改造范围，也允许 `DataObject` 继续使用基础类型，在 `RepositoryImpl` 做双向转换
- 同一业务域内同一类 ID 的持久化方式必须统一，不允许半数 `DO` 用 `Long`、半数 `DO` 用 `UserId`

### 7.5 JPA Rules

- 当前仓库未使用 `JPA` 作为正式持久化实现
- `JPA` 适配规则先在公共层预留 `AttributeConverter` 模式
- 未正式引入 `JPA` 前，不在业务域增加 `Entity` 专用实现
- 后续如接入 `JPA`，统一 ID 体系不得要求重写领域模型

## 8. Key Flows

### 8.1 Domain Create Flow

1. `application` 调用 `Ids`
2. `Ids` 使用统一生成策略产生底层值
3. `Ids` 包装为具体 ID 类型
4. `domain` 持有具体 ID 类型参与业务逻辑
5. `infra` 在持久化边界转换为数据库字段

### 8.2 Read Flow

1. `infra` 从数据库读取基础类型字段
2. 通过统一转换器恢复为具体 ID 类型
3. `domain` 与 `application` 全程使用强类型 ID
4. `interfaces` 输出时通过 `Jackson` 统一序列化为基础值

## 9. Database And Migration Rules

### 9.1 Current Database Baseline

- 现有数据库设计中，主键 `id` 大量使用 `bigint`
- `tenantNo`、`orderNo`、`paymentNo`、`reservationNo` 等业务标识使用 `varchar`
- 当前数据库结构本身不需要为了引入 `BaseId` 立即改表

### 9.2 Fixed Persistence Mapping

- `UserId` 固定使用 `varchar(64)`
- `RoleId`、`SkuId`、`OrderId` 若底层值为 `Long`，数据库字段继续使用 `bigint`
- `TenantId` 若底层值为 `String`，数据库字段继续使用 `varchar`
- 统一 ID 体系优先改变 Java 类型系统，不强制改变既有列类型

### 9.3 UserId Rules

- `UserId` 当前固定为文本型统一 ID
- `UserId` 的 Java 底层类型固定为 `String`
- `UserId` 的数据库字段当前固定建议使用 `varchar(64)`
- `UserId` 默认生成规则可采用 `U` 前缀加数值序列，例如 `U1001`
- `UserId` 作为 `UPMS` 用户主体主标识时，允许作为表主键和关联字段直接落库

### 9.4 Adjustment Rules

仅在以下场景允许调整数据库结构：

- 某领域当前把同一概念同时存成 `varchar` 和 `bigint`
- 某领域需要把外部业务标识升级为内部强类型主标识
- 某领域已经确认数据库列类型与领域语义不匹配

固定要求：

- 数据库改造必须按业务域单独设计和迁移
- 不允许为了引入 `BaseId` 在全库做一次性大迁移
- 文档、代码、数据库三者必须先统一“这是主键、业务单号还是领域标识”

## 10. Non-Functional Requirements

- 统一 ID 基础设施必须放在公共模块，避免每个业务域重复实现
- 新增一个具体 ID 类型时，除类型声明外的新增代码应控制在极小范围
- 统一 ID 体系不得明显增加序列化和持久化复杂度
- 改造必须支持渐进式迁移，允许老代码和新类型在边界层共存
- 统一 ID 体系必须支持单元测试直接构造，不依赖 Spring 容器

## 11. Recommended Implementation Baseline

固定采用以下总体方案：

- `Identifier<T>`：标识接口
- `BaseId<T>`：抽象基类
- `UserId` / `TenantId` / `RoleId` / `OrderId` / `SkuId`：具体 ID
- `IdGenerator`：统一底层值生成器
- `IdConverters`：统一序列化与持久化适配
- `Ids`：统一工厂入口

示例目标：

```java
UserId userId = UserId.of("U1001");
TenantId tenantId = TenantId.of("T001");
OrderId orderId = ids.orderId();
```

## 12. Open Items

- `RoleId`、`SkuId`、`OrderId` 首批是否统一收敛为 `Long`
- `TenantId` 是否直接替代现有 `tenantNo` 命名，还是保留“字段名仍为 `tenantNo`、Java 类型升级为 `TenantId`”
- `DataObject` 首阶段是否允许继续保留基础类型字段，以降低一次性改造范围
