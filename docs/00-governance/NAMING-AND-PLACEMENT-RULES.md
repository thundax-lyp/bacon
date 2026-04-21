# 命名与目录规则

本文件只回答三个问题：

1. 该建什么类型的类
2. 该放哪一层、哪个目录
3. 该叫什么名字

本文件采用二维结构：按 `Path/Layer/Naming` 组织规则，并按 `Hard Rules（可门禁）` 与 `Review Rules（AI/人工审阅）` 分区。
新增规则必须先完成分类归位：先判定 `Hard/Review`，再归入 `Path/Layer/Naming`，禁止新增未分类规则。

维护约束：

1. 新增规则必须先判定 `Hard Rules` 或 `Review Rules`
2. 新增规则必须归入 `Path`、`Layer`、`Naming & Placement` 之一
3. `Hard Rules` 必须使用稳定编号（前缀+缩写），编号不要求连续

## Fast Choice

- HTTP 外部入口：`Controller`
- HTTP 内部入口：`ProviderController`
- 接口层请求解析：`Resolver`
- 用例编排入口：`ApplicationService`
- 应用层内部复用：`Helper` / `Factory` / `Resolver` / `Executor`
- 核心领域规则：`DomainService`
- 领域读写契约：`Repository`
- 仓储实现：`RepositoryImpl`
- 数据库访问：`Mapper`
- 持久化对象：`DO`
- 持久化桥接转换：`PersistenceAssembler`
- 通用对象转换：`Converter`
- 基础类型和值对象互转：`Codec`
- 跨域调用契约：`Facade`
- 单体跨域适配：`FacadeLocalImpl`
- 微服务跨域适配：`FacadeRemoteImpl`
- 通用技术能力：`Service`

## Hard Rules（可门禁，必须稳定）

硬规则使用稳定编号（前缀+缩写）用于条目与测试映射，编号不要求连续。

### Path

- `PATH_CONTROLLER_PREFIX`：`interfaces.controller` 类级路径必须以 `/{domain}` 开头
- `PATH_PROVIDER_PREFIX`：`interfaces.provider` 类级路径必须以 `/providers/{domain}` 开头
- `PATH_CONTROLLER_NO_PROVIDERS`：`interfaces.controller` 不得使用 `/providers/**` 前缀
- `PATH_DOMAIN_CANONICAL`：`{domain}` 固定与业务域目录名一致，不混用别名

### Layer

- `LAYER_CROSS_DOMAIN_FACADE_ONLY`：跨域调用统一依赖对方域 `api.facade`，不直接依赖对方域 `application`、`infra`
- `LAYER_INTERFACES_DEPENDENCY_WHITELIST`：`interfaces` 依赖固定白名单为本域 `application`、本域 `domain.model`、本域 `interfaces`、`api.facade`、`bacon-common`；禁止依赖 `domain.repository`、`infra.persistence.mapper`、外域散包
- `LAYER_FACADE_SIGNATURE_MODEL`：仅 `api.facade` 方法签名使用 `FacadeRequest` / `FacadeResponse`
- `LAYER_FACADE_SINGLE_REQUEST`：`Facade` 方法入参固定为“无入参或单个 `XxxFacadeRequest`”
- `LAYER_FACADE_RESPONSE_ONLY`：`Facade` 方法返回固定为 `XxxFacadeResponse` 或 `void`
- `LAYER_PERSISTENCE_ASSEMBLER_PLACEMENT`：`*PersistenceAssembler` 必须位于 `infra.persistence.assembler..`
- `LAYER_PERSISTENCE_ASSEMBLER_PUBLIC_METHODS`：`*PersistenceAssembler` 公开方法只允许 `toDomain(...)`、`toDataObject(...)`
- `LAYER_APPLICATION_INFRA_NO_ILLEGAL_ARGUMENT`：`application` 与 `infra.repository.impl/support` 禁止把 `IllegalArgumentException` 作为业务异常出口
- `LAYER_APPLICATION_ASSEMBLER_EXCLUSIVE_MAPPING`：`application.command/query/audit/support` 禁止直接构造业务 `*DTO/*Response`；`Domain <-> DTO/Result` 装配固定收敛到 `application.assembler`
- `LAYER_ANNOTATION_PLACEMENT_WHITELIST`：`@RestController` 仅允许在 `interfaces.controller/provider`，`@FeignClient` 仅允许在 `infra.facade.remote`，`@Mapper` 仅允许在 `infra.persistence.mapper`，`@TableName/@TableField` 仅允许在 `infra.persistence.dataobject`

### Naming & Placement

- `NAME_CONTROLLER`：`Controller` 命名 `{业务对象}{动作}Controller`，目录 `interfaces/controller/`
- `NAME_PROVIDER_CONTROLLER`：`ProviderController` 命名 `{业务对象}{动作}ProviderController`，目录 `interfaces/provider/`
- `NAME_RESOLVER`：`Resolver` 命名 `{业务对象}{动作}Resolver`，目录 `interfaces/resolver/`
- `NAME_APPLICATION_SERVICE`：`ApplicationService` 命名以 `ApplicationService` 结尾，目录 `application/command|query|audit|support/`
- `NAME_DOMAIN_SERVICE`：`DomainService` 命名 `{业务对象}DomainService`，目录 `domain/service/`
- `NAME_REPOSITORY`：`Repository` 命名 `{业务对象}Repository`，目录 `domain/repository/`
- `NAME_REPOSITORY_IMPL`：`RepositoryImpl` 命名 `{业务对象}RepositoryImpl`，目录 `infra/repository/impl/`
- `NAME_REPOSITORY_METHOD_PREFIX`：`Repository` 方法名前缀必须命中白名单：`find / list / page / count / exists / claim / release / renew / mark / pause / resume / recover / delete / insert / update`；未命中白名单的方法一律视为不合规
- `NAME_REPOSITORY_IMPL_NO_CROSS_IMPL_DEP`：`RepositoryImpl` 不得直接依赖其他 `RepositoryImpl`；跨仓储协作只能依赖 `domain.repository`
- `NAME_REPOSITORY_IMPL_NO_CROSS_SUPPORT_DEP`：`RepositoryImpl` 只允许直接依赖与自身同聚合的 `PersistenceSupport`，不得直接依赖其他聚合的 `PersistenceSupport`
- `NAME_REPOSITORY_IMPL_SINGLE_DOMAIN_REPOSITORY`：`RepositoryImpl` 必须且仅实现一个本域 `domain.repository.*Repository`；禁止实现外域 `Repository`，禁止零实现或一类多仓储实现
- `NAME_MAPPER`：`Mapper` 命名 `{业务对象}Mapper`，目录 `infra/persistence/mapper/`
- `NAME_DO`：`DO` 命名 `{业务对象}DO`，目录 `infra/persistence/dataobject/`
- `NAME_PERSISTENCE_ASSEMBLER`：`PersistenceAssembler` 命名 `{业务对象}PersistenceAssembler`，目录 `infra/persistence/assembler/`
- `NAME_CODEC`：`Codec` 命名 `{业务对象}Codec`，目录 `application/codec/`
- `NAME_FACADE`：`Facade` 命名 `{业务对象}{动作}Facade`，目录 `api/facade/`
- `NAME_FACADE_REQUEST`：`FacadeRequest` 命名 `{业务对象}{动作}FacadeRequest`，目录 `api/request/`
- `NAME_FACADE_RESPONSE`：`FacadeResponse` 命名 `{业务对象}{动作}FacadeResponse`，目录 `api/response/`
- `NAME_FACADE_LOCAL_IMPL`：`FacadeLocalImpl` 命名 `{业务对象}{动作}FacadeLocalImpl`，目录 `interfaces/facade/`
- `NAME_FACADE_REMOTE_IMPL`：`FacadeRemoteImpl` 命名 `{业务对象}{动作}FacadeRemoteImpl`，目录 `infra/facade/remote/`
- `NAME_SIMPLE_ENUM_CONVENTION`：`domain.model.enums` 简单枚举统一实现 `value() -> name()` 和 `from(...)`

## Review Rules（AI/人工审阅，暂不强门禁）

### Path

- `PATH_CONTEXT_COMPOSE`：完整外部路径由 `server.servlet.context-path` 叠加；默认 `context-path=/api` 时完整路径分别为 `/api/{...}` 与 `/api/providers/{...}`
- 路径语义推荐使用 `/{bounded-context}/{resource}/{action?}`，但该条以 AI/人工审阅为准，不作为 ArchUnit 硬门禁
- `{action}` 只在资源名不足以表达语义时出现，不使用 `query`、`list`、`detail`
- `{bounded-context}` 与 `{resource}` 同义、近义或仅单复数差异时，省略其一
- 优先使用稳定资源语义，避免重复资源命名（如 `order/orders`、`inventory/inventories`）

### Layer

- `LAYER_CODEC_SCOPE`：`Codec` 只做基础类型和值对象互转，不做业务编排；待固定目录/调用写法后再升级为 `Hard Rules`
- interfaces 层优先完成参数约束校验，避免将明显非法参数下沉到 application
- application 公共入口优先使用 `Command / Query / VO`，避免长 primitive 参数列表

### Naming & Placement

- 命名应表达职责与层次，避免泛化命名（如 `OrderService`、`InventoryService`）
- `NAME_INFRA_NO_GENERIC_SERVICE`：`infra` 层已有更精确后缀时，不再使用业务 `*Service`；待固定目录白名单后再升级为 `Hard Rules`
- Facade 契约命名保持一致，避免同语义对象出现多套后缀风格

## Good Paths

以下示例为完整外部路径（默认 `context-path=/api`）：

- `/api/order`
- `/api/payment/refunds`
- `/api/inventory/stocks`
- `/api/inventory/reservations/reserve`
- `/api/upms/users`
- `/api/providers/order/mark-paid`
- `/api/providers/inventory/reservations/release`

## Bad Paths

以下示例为完整外部路径（默认 `context-path=/api`）：

- `/api/order/orders`
- `/api/inventory/inventories`
- `/api/upms/user`
- `/api/order/detail`
- `/api/payment/query`

## Good Names

- `OrderCreateController`
- `OrderQueryProviderController`
- `TenantRequestResolver`
- `OrderCreateApplicationService`
- `OrderQueryApplicationService`
- `OrderAuditCompensationApplicationService`
- `OrderAuditReplayApplicationService`
- `OrderDomainService`
- `OrderRepository`
- `OrderRepositoryImpl`
- `OrderMapper`
- `OrderDO`
- `OrderPersistenceAssembler`
- `OrderConverter`
- `OrderNoCodec`
- `UserReadFacade`
- `UserPageFacadeRequest`
- `UserPageFacadeResponse`
- `UserReadFacadeLocalImpl`
- `UserReadFacadeRemoteImpl`
- `VerificationCodeService`

## Bad Names

- `OrderService`
- `InventoryService`
- `InventoryAuditCompensationService`
- 看不出层次和职责的名字
